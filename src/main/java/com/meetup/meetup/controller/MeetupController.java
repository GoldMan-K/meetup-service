package com.meetup.meetup.controller;

import com.meetup.meetup.dto.CreateMeetupRequest;
import com.meetup.meetup.dto.MeetupResponse;
import com.meetup.meetup.dto.UpdateMeetupRequest;
import com.meetup.meetup.service.MeetupService;
import com.meetup.participant.dto.ParticipantResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "Meetup", description = "모임 API")
@RestController
@RequestMapping("/api/meetups")
@RequiredArgsConstructor
public class MeetupController {

    private final MeetupService meetupService;

    // ─── 목록 조회 ───────────────────────────────────────────────────────────

    @Operation(summary = "모임 목록 조회", description = "지역·유형·날짜 필터로 모임 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<Page<MeetupResponse>> getMeetupList(
            @RequestParam(required = false) String regionCode,
            @RequestParam(required = false) String typeCode,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime meetAtFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime meetAtTo,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(meetupService.getMeetupList(regionCode, typeCode, meetAtFrom, meetAtTo, pageable));
    }

    // ─── 상세 조회 ───────────────────────────────────────────────────────────

    @Operation(summary = "모임 상세 조회", description = "모임 상세 정보와 참가자 수를 조회합니다.")
    @GetMapping("/{meetupId}")
    public ResponseEntity<MeetupResponse> getMeetupDetail(@PathVariable Long meetupId) {
        return ResponseEntity.ok(meetupService.getMeetupDetail(meetupId));
    }

    @Operation(summary = "참가자 목록 조회", description = "각 참가자 항목에 nickname 필드를 포함해 반환합니다.")
    @GetMapping("/{meetupId}/participants")
    public ResponseEntity<List<ParticipantResponse>> getParticipants(@PathVariable Long meetupId) {
        return ResponseEntity.ok(meetupService.getParticipants(meetupId));
    }

    // ─── 생성 ────────────────────────────────────────────────────────────────

    @Operation(summary = "모임 생성", description = "모임을 생성합니다. 생성자는 HOST로 자동 참가됩니다.")
    @PostMapping
    public ResponseEntity<MeetupResponse> createMeetup(
            @RequestHeader("X-Member-Id") Long memberId,
            @RequestBody CreateMeetupRequest request
    ) {
        return ResponseEntity.ok(meetupService.createMeetup(memberId, request));
    }

    // ─── 수정 ────────────────────────────────────────────────────────────────

    @Operation(summary = "모임 수정", description = "호스트만 수정 가능합니다.")
    @PatchMapping("/{meetupId}")
    public ResponseEntity<MeetupResponse> updateMeetup(
            @RequestHeader("X-Member-Id") Long memberId,
            @PathVariable Long meetupId,
            @RequestBody UpdateMeetupRequest request
    ) {
        return ResponseEntity.ok(meetupService.updateMeetup(memberId, meetupId, request));
    }

    // ─── 취소 ────────────────────────────────────────────────────────────────

    @Operation(summary = "모임 취소", description = "호스트만 취소 가능합니다. meetup.canceled 이벤트가 발행됩니다.")
    @DeleteMapping("/{meetupId}")
    public ResponseEntity<Void> cancelMeetup(
            @RequestHeader("X-Member-Id") Long memberId,
            @PathVariable Long meetupId
    ) {
        meetupService.cancelMeetup(memberId, meetupId);
        return ResponseEntity.noContent().build();
    }

    // ─── 모집 마감 ───────────────────────────────────────────────────────────

    @Operation(summary = "모집 마감", description = "호스트 또는 관리자가 모집을 마감합니다.")
    @PostMapping("/{meetupId}/close")
    public ResponseEntity<Void> closeMeetup(
            @RequestHeader("X-Member-Id") Long memberId,
            @PathVariable Long meetupId
    ) {
        meetupService.closeMeetup(memberId, meetupId);
        return ResponseEntity.noContent().build();
    }

    // ─── 참가 신청 ───────────────────────────────────────────────────────────

    @Operation(summary = "참가 신청", description = "모임에 참가합니다. 정원 초과 시 409를 반환합니다.")
    @PostMapping("/{meetupId}/join")
    public ResponseEntity<Void> joinMeetup(
            @RequestHeader("X-Member-Id") Long memberId,
            @PathVariable Long meetupId
    ) {
        meetupService.joinMeetup(memberId, meetupId);
        return ResponseEntity.ok().build();
    }

    // ─── 참가 취소 ───────────────────────────────────────────────────────────

    @Operation(summary = "참가 취소")
    @PostMapping("/{meetupId}/leave")
    public ResponseEntity<Void> leaveMeetup(
            @RequestHeader("X-Member-Id") Long memberId,
            @PathVariable Long meetupId
    ) {
        meetupService.leaveMeetup(memberId, meetupId);
        return ResponseEntity.ok().build();
    }
}
