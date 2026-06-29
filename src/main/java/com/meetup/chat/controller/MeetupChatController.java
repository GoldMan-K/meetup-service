package com.meetup.chat.controller;

import com.meetup.chat.dto.ChatMessageRequest;
import com.meetup.chat.dto.ChatMessageResponse;
import com.meetup.chat.dto.ChatPageResponse;
import com.meetup.chat.service.MeetupChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Meetup Chat", description = "모임 채팅 API (REST, 실시간은 WebSocket 별도)")
@RestController
@RequestMapping("/api/meetups/{meetupId}/chats")
@RequiredArgsConstructor
public class MeetupChatController {

    private final MeetupChatService chatService;

    @Operation(summary = "채팅 메시지 조회", description = "커서 기반 페이징으로 채팅 이력을 조회합니다. 각 메시지에 senderNickname을 포함하며 시스템 메시지는 senderNickname=SYSTEM 으로 반환합니다.")
    @GetMapping
    public ResponseEntity<ChatPageResponse> getChatMessages(
            @PathVariable Long meetupId,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "30") int size
    ) {
        return ResponseEntity.ok(chatService.getChatMessages(meetupId, cursor, size));
    }

    @Operation(summary = "채팅 메시지 전송", description = "채팅 메시지를 전송합니다. 참가자만 전송 가능하며 응답에 senderNickname을 포함합니다.")
    @PostMapping
    public ResponseEntity<ChatMessageResponse> sendMessage(
            @RequestHeader("X-Member-Id") Long memberId,
            @PathVariable Long meetupId,
            @RequestBody ChatMessageRequest request
    ) {
        return ResponseEntity.ok(chatService.sendMessage(memberId, meetupId, request));
    }
}
