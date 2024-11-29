package com.example.ticketing.api.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SignUpRequest {
    String email;
    String password;
    String name;
    public SignUpRequest(String email, String password,String name) {
        this.email = email;
        this.password = password;
        this.name =name;
    }
}
