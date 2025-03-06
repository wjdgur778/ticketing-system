package com.example.ticketing.api.user;

import com.example.ticketing.api.user.dto.LoginRequest;
import com.example.ticketing.api.user.dto.SignUpRequest;
import com.example.ticketing.common.exception.CommonErrorCode;
import com.example.ticketing.common.exception.RestApiException;
import com.example.ticketing.config.auth.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    final private UserRepository userRepository;
    private final RedisTemplate<String,Object> redisTemplate;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder bCryptPasswordEncoder;

    @Transactional
    public String signup(SignUpRequest signUpRequest){
        if(userRepository.findByEmail(signUpRequest.getEmail()).isPresent()) {
            throw new RuntimeException();
        }
        //user 저장
        User user = User.builder()
                .email(signUpRequest.getEmail())
                //패스워드 인코딩
                .password(bCryptPasswordEncoder.encode(signUpRequest.getPassword()))
                .name(signUpRequest.getName())
                .build();
        userRepository.save(user);
        Authentication authentication;

        try{
            // 사용자 인증 시도
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(signUpRequest.getEmail(),signUpRequest.getPassword()));
        }
        catch (AuthenticationException e){
            return null;
        }
        //security context에 인증 정보 저장
        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        String jwt = jwtService.generateToken(userDetails.getUsername());


        return jwt;
    }

    @Transactional
    public String login(LoginRequest loginRequest) {
        Authentication authentication;
        try{
            System.out.println("????????????");
            // 사용자 인증 시도
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(),loginRequest.getPassword()));
        }
        catch (AuthenticationException e){
            throw new RestApiException(CommonErrorCode.MEMBER_NOT_FOUND);
        }
        String jwt = jwtService.generateToken(authentication.getName());
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        String refreshToken = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(email, refreshToken, Duration.ofDays(30));

        // 인증 성공 시 JWT 생성
        return jwt;
    }
}
