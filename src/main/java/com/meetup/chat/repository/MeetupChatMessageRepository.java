package com.meetup.chat.repository;

import com.meetup.chat.domain.MeetupChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MeetupChatMessageRepository extends JpaRepository<MeetupChatMessage, Long> {

    // 커서 기반 페이징: cursor(id) 이전 메시지 조회
    @Query("""
            SELECT m FROM MeetupChatMessage m
            WHERE m.meetupId = :meetupId
              AND (:cursor IS NULL OR m.id < :cursor)
            ORDER BY m.id DESC
            """)
    List<MeetupChatMessage> findByMeetupIdWithCursor(
            @Param("meetupId") Long meetupId,
            @Param("cursor") Long cursor,
            Pageable pageable
    );
}
