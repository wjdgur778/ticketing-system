package com.example.ticketing.api.user;

import com.example.ticketing.api.user.dto.LoginRequest;
import com.example.ticketing.api.user.dto.SignUpRequest;
import com.example.ticketing.config.auth.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    final private UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public String signup(SignUpRequest signUpRequest){
        if(userRepository.findByEmail(signUpRequest.getEmail()).isPresent())
            throw new RuntimeException();
        //user 저장
        User user = User.builder()
                .email(signUpRequest.getEmail())
                .password(signUpRequest.getPassword())
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
            System.out.println("call - > login in service");
            // 사용자 인증 시도
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(),loginRequest.getPassword()));
        }
        catch (AuthenticationException e){
            return null;
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        String jwt = jwtService.generateToken(userDetails.getUsername());

        // 인증 성공 시 JWT 생성
        return jwt;
    }
}
