package com.meetup.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 공통
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "리소스를 찾을 수 없습니다."),

    // 모임
    MEETUP_NOT_FOUND(HttpStatus.NOT_FOUND, "모임을 찾을 수 없습니다."),
    MEETUP_ALREADY_CLOSED(HttpStatus.BAD_REQUEST, "이미 종료된 모임입니다."),
    MEETUP_FULL(HttpStatus.CONFLICT, "모임 정원이 가득 찼습니다."),
    MEETUP_NOT_HOST(HttpStatus.FORBIDDEN, "모임 호스트만 수행할 수 있습니다."),
    MEETUP_CANCELED(HttpStatus.BAD_REQUEST, "취소된 모임입니다."),

    // 참가자
    ALREADY_JOINED(HttpStatus.CONFLICT, "이미 참가한 모임입니다."),
    NOT_PARTICIPANT(HttpStatus.BAD_REQUEST, "참가하지 않은 모임입니다."),
    HOST_CANNOT_LEAVE(HttpStatus.BAD_REQUEST, "호스트는 모임을 탈퇴할 수 없습니다."),

    // 채팅
    CHAT_MESSAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "채팅 메시지를 찾을 수 없습니다."),
    CHAT_NOT_PARTICIPANT(HttpStatus.FORBIDDEN, "모임 참가자만 채팅할 수 있습니다."),
    TYPING_DISABLED(HttpStatus.CONFLICT, "입력중 상태를 사용할 수 없는 모임입니다.");

    private final HttpStatus status;
    private final String message;
}
