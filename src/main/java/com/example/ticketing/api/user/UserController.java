package com.example.ticketing.api.user;

import com.example.ticketing.api.user.dto.LoginRequest;
import com.example.ticketing.api.user.dto.SignUpRequest;
import com.example.ticketing.common.dto.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    final private UserService userService;


    // 회원가입은 성공했지만 그 이후에 403이 리턴되었다.
    // 반환 data객체인 Result가 직렬화되지 않아 403 발생
    // Result에 @Data 어노테이션을 추가
    @PostMapping("/signup")
    public ResponseEntity<Result> signup(@RequestBody SignUpRequest signUpRequest){
        return  ResponseEntity.status(200).body(
                Result.builder()
                        .message("회원가입 성공")
                        .data(userService.signup(signUpRequest))
                        .build()
        );
    }

    //로그인 시에 jwt 토큰을 발급해주어야 한다.
    @PostMapping("/login")
    public ResponseEntity<Result> login(@RequestBody LoginRequest loginRequest){
        System.out.println("call - > /login");
        return ResponseEntity.status(200).body(
                Result.builder()
                        .message("로그인 성공")
                        .data(userService.login(loginRequest))
                        .build()
        );
    }

}
