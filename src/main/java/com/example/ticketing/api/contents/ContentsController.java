package com.example.ticketing.api.contents;

import com.example.ticketing.common.dto.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/contents")
@RequiredArgsConstructor
public class ContentsController {
    final private ContentsService contentsService;

    //redis의 캐싱을 사용한 contents 조회
    @GetMapping("")
    ResponseEntity<Result> getContents() {
        return ResponseEntity.status(200).body(
                Result.builder()
                        .message("redis 캐싱을 이용한 contents 조회")
                        .data(contentsService.getContents())
                        .build()
        );
    }
    @GetMapping("{contentsId}")
    ResponseEntity<Result> getContentsById(@PathVariable Long contentsId) {
        return ResponseEntity.status(200).body(
                Result.builder()
                        .message("redis 캐싱을 이용한 contents 조회")
                        .data(contentsService.getContents())
                        .build()
        );
    }
}
