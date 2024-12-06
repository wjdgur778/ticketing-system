package com.example.ticketing.config.auth.security;

import com.example.ticketing.config.auth.jwt.JwtFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity(debug = false)
@RequiredArgsConstructor
public class SecurityConfig {

    // Password 인코딩 방식에 BCrypt 암호화 방식 사용
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private final JwtFilter jwtFilter;
    private final CustomUserDetailService customUserDetailService;
    /*
    1. SpringBoot 3.x.x로 오면서 이전에 WebSecurityConfigurerAdapter를 상속받아 configure을 구현하는 방식
    대신에 SecurityFilterChain을 등록해서 람다식으로 처리하는 방식으로 구현
    2. 사용자의 request를 dispatcherservlet이 받기 이전에 이 filterchain을 거치게 된다.
    */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        // 토큰 기반의 인증을 거치기 때문에 csrf의 보호가 필요하지 않다.
        http.csrf(customizer -> customizer.disable());
        //(tomcat구동 오류) 세션 난수 문제 해결
        // 토큰 기반 인증이므로 세션 사용 하지않음
        http.sessionManagement((sessionManagement) ->
                sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http.authorizeHttpRequests(
                authorize -> authorize
                        //requsetMatchers를 사용할때는 url를 정확하게 작성해야한다.
                        .requestMatchers("/api/user/login").permitAll()
                        .requestMatchers("/api/user/signup").permitAll()
                        .requestMatchers("/api/reservation").permitAll()
////                        .requestMatchers("/api/v1/user/write").hasRole("UESR")
//                        .requestMatchers("/api/v1/user/list").authenticated()
//                        .anyRequest().hasRole(Role.USER.name())//위에서 언급한 url 이외의 url은 모두 허용한다.
        );

        // 기본 HTTP 인증을 활성화하여 사용자 인증을 처리하겠다는 설정
//                        .httpBasic(Customizer.withDefaults());

        // 로그인 폼을 사용할때 사용
//                        .formLogin(Customizer.withDefaults())

        //HTTP 요청에 JWT 토큰 인증 필터를 거치도록 필터를 추가
        //JWT 인증을 사용하는 경우, JwtFilter가 사용자 인증을 처리하므로 UsernamePasswordAuthenticationFilter를 거칠 필요가 없습니다.
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }


    // only security
    // 인증인 필요한 객체가 provider를 통해 인증된 객체로 변환 return 된다.
    // AuthenticationProvider가 Filterchain에 앞서 호출되어 provider를 세팅한다.

    @Bean
    AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(new BCryptPasswordEncoder(12));
        provider.setUserDetailsService(customUserDetailService);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationProvider provider) {
        return new ProviderManager(provider);
    }
}
