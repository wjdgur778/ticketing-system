package com.example.ticketing.config.auth.jwt;

import com.example.ticketing.config.auth.security.CustomUserDetailService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailService customUserDetailService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        final String authorizationHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

//        //그냥 지나치기
//        filterChain.doFilter(request,response);

        // 헤더에 토큰을 포함하고 있지 않으면 다음 필터로 넘기자
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        jwt = authorizationHeader.substring(7);
        userEmail = jwtService.getUserNameFromJwtToken(jwt); // todo extract the uesrEmail from JWT token;
        // 유저가 존재하지만, springsecurity가 이미 검증을 완료하고 UserDetailService의
        // loadUserByUsername을 통해 contextholder에 그 정보를 저장하고 있지 않다면,
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = customUserDetailService.loadUserByUsername(userEmail);
            if (jwtService.isTokenValid(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                // securiyContextHolder에 저장
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        //jwtfilter를 거친 후 다음 필터로 향하게 한다. (UsernamePasswordAuthenticationfilter)
        filterChain.doFilter(request,response);
    }
}
