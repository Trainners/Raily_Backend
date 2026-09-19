package io.trainners.raily_backend.global.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 존재하는 이메일입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 사용자입니다."),
    PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않습니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 리프레시 토큰입니다."),
    REFRESH_TOKEN_MISMATCH(HttpStatus.UNAUTHORIZED, "일치하지 않는 리프레시 토큰입니다."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    STATION_NOT_ON_ROUTE(HttpStatus.NOT_FOUND, "해당 열차는 입력하신 역에 정차하지 않습니다."),
    INVALID_STATION_ORDER(HttpStatus.BAD_REQUEST, "출발역이 도착역보다 뒤에 있습니다."),
    TRAIN_NOT_FOUND(HttpStatus.NOT_FOUND, "선택한 열차를 찾을 수 없습니다."),
    CAR_INFO_NOT_FOUND(HttpStatus.BAD_REQUEST, "해당 열차의 호차 정보를 조회할 수 없습니다."),
    SEAT_WATCH_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 좌석 감시입니다."),
    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 알림입니다.");


    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}