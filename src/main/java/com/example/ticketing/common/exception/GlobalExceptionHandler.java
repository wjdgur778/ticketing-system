package com.example.ticketing.common.exception;
import com.example.ticketing.common.dto.Result;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.http.HttpStatus;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RestApiException.class)
    public ResponseEntity<Result> handleRestApiException(RestApiException re) {
        return ResponseEntity.status(re.getErrorCode().getStatus()).body(Result.builder()
                .message(re.getErrorCode().getMessage())
                .build()
        );
    }

}