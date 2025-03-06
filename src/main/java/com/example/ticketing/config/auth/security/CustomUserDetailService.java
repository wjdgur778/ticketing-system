package com.example.ticketing.config.auth.security;

import com.example.ticketing.api.user.User;
import com.example.ticketing.api.user.UserRepository;
import com.example.ticketing.common.exception.CommonErrorCode;
import com.example.ticketing.common.exception.RestApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/*

 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService {
    private final UserRepository userRepository;


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        User user = userRepository.findByEmail(email)
                .orElseThrow(

                        // not found exception
                        ()->new RestApiException(CommonErrorCode.NOT_FOUND)
                );
        CustomUserDetail customUserDetail = new CustomUserDetail(user);
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                customUserDetail,
                null,
                customUserDetail.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authToken);
        return new CustomUserDetail(user);
    }
}
