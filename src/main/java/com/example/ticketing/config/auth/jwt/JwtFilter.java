package com.example.ticketing.config.auth.jwt;

import com.example.ticketing.common.exception.RestApiException;
import com.example.ticketing.config.auth.security.CustomUserDetailService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static com.example.ticketing.common.exception.CommonErrorCode.REFRESH_TOKEN_NOT_FOUND;
import static com.example.ticketing.common.exception.CommonErrorCode.UNAUTHORIZED;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailService customUserDetailService;
    private final RedisTemplate<String, Object> redisTemplate;


    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        final String authorizationHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // 헤더에 토큰을 포함하고 있지 않으면 리턴
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authorizationHeader.substring(7);
        try{
            //jwt가 유효하면 security contextholder에 사용자를 저장한다.
            if(jwtService.isTokenValid(jwt)){
                String username = jwtService.getUserNameFromJwtToken(jwt);
                setAuthentication(username);
            }
        }
        catch (ExpiredJwtException e) {
            //Access Token이 만료된 경우 → Refresh Token 검증 로직 실행
            handleExpiredAccessToken(request, response, e);
        } catch (JwtException e) {
            // 위조된 토큰 → 401 에러 응답
            throw new RestApiException(UNAUTHORIZED);
        }

        filterChain.doFilter(request,response);
    }

    private void setAuthentication(String username) {
        Authentication authentication = new UsernamePasswordAuthenticationToken(username, null, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private void handleExpiredAccessToken(HttpServletRequest request, HttpServletResponse response, ExpiredJwtException e){
        String username = e.getClaims().getSubject();
        String refreshToken = (String) redisTemplate.opsForValue().get(username); // Redis에서 Refresh Token 가져오기

        //등록된 refreshToken이 존재한다면 accesstoken을 재발급해야한다.
        if(refreshToken!=null){
            String newAccessToken = jwtService.generateToken(username);
            setAuthentication(username);
            response.setHeader("Authorization", "Bearer " + newAccessToken);
        }
        //등록된 refreshToken이 없다면
        else{
            System.out.println("there is no refresh token");
            throw new RestApiException(REFRESH_TOKEN_NOT_FOUND);
        }
    }
}
