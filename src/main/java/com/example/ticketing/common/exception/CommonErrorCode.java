package com.example.ticketing.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum CommonErrorCode implements ErrorCode{
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "회원정보가 존재하지 않습니다."),
    MEMBER_SIGN_IN_FAILED(HttpStatus.NOT_FOUND, "아이디 또는 비밀번호가 일치하지 않습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증되지 않은 유저입니다."),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 파라미터 타입입니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "잘못된 HTTP 요청입니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "데이터를 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 에러가 발생했습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "유저를 조회할 수 없습니다."),
    INVALID_INPUT_DATE(HttpStatus.BAD_REQUEST, "잘못된 데이터 입력입니다."),
    LOCKED(HttpStatus.BAD_REQUEST,"다른 사용자가 좌석을 예약 중입니다."),
    WAITING(HttpStatus.BAD_REQUEST,"현재 사용자의 차례가 아닙니다. 기다려주세요"),
    NOT_AVAILABLE(HttpStatus.BAD_REQUEST,"이미 예약된 좌석입니다."),
    ALREADY_EXIST(HttpStatus.BAD_REQUEST,"이미 대기열에 존재합니다.")
    ;

    private final HttpStatus status;
    private final String message;

    CommonErrorCode(HttpStatus httpStatus,String message){
        this.status = httpStatus;
        this.message = message;
    }
//    @Override
//    public HttpStatus getStatus() {
//        return this.status;
//    }
//    @Override
//    public String getMessage(){
//        return this.message;
//    }
}
