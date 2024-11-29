package com.example.ticketing.api.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class LoginRequest {
    String email;
    String password;
    String name;
    public LoginRequest(String email, String password,String name) {
        this.email = email;
        this.password = password;
        this.name =name;
    }
}
