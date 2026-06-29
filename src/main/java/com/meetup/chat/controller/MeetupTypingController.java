package com.meetup.chat.controller;

import com.meetup.chat.dto.TypingHeartbeatRequest;
import com.meetup.chat.dto.TypingListResponse;
import com.meetup.chat.service.MeetupTypingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Meetup Typing", description = "모임 채팅 입력중 상태 API")
@RestController
@RequestMapping("/api/meetups/{meetupId}/typing")
@RequiredArgsConstructor
public class MeetupTypingController {

    private final MeetupTypingService meetupTypingService;

    @Operation(summary = "입력중 상태 갱신", description = "heartbeat 방식으로 입력중 상태를 갱신하거나 해제합니다.")
    @PostMapping
    public ResponseEntity<Void> heartbeat(
            @RequestHeader("X-Member-Id") Long memberId,
            @PathVariable Long meetupId,
            @RequestBody TypingHeartbeatRequest request
    ) {
        meetupTypingService.heartbeat(meetupId, memberId, request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "입력중 사용자 조회", description = "현재 입력중인 사용자 목록을 조회합니다. 요청자 본인은 목록에서 제외됩니다.")
    @GetMapping
    public ResponseEntity<TypingListResponse> getTypingUsers(
            @RequestHeader("X-Member-Id") Long memberId,
            @PathVariable Long meetupId
    ) {
        return ResponseEntity.ok(meetupTypingService.getTypingUsers(meetupId, memberId));
    }
}
