package com.meetup.meetup.repository;

import com.meetup.meetup.domain.Meetup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface MeetupRepository extends JpaRepository<Meetup, Long> {

    // 지역·유형·날짜 필터 목록 (취소된 모임 제외)
    @Query("""
            SELECT m FROM Meetup m
            WHERE m.deletedAt IS NULL
              AND (:regionCode IS NULL OR m.regionCode = :regionCode)
              AND (:typeCode   IS NULL OR m.typeCode   = :typeCode)
              AND (:meetAtFrom IS NULL OR m.meetAt    >= :meetAtFrom)
              AND (:meetAtTo   IS NULL OR m.meetAt    <= :meetAtTo)
            ORDER BY m.meetAt ASC
            """)
    Page<Meetup> findAllByFilter(
            @Param("regionCode") String regionCode,
            @Param("typeCode") String typeCode,
            @Param("meetAtFrom") LocalDateTime meetAtFrom,
            @Param("meetAtTo") LocalDateTime meetAtTo,
            Pageable pageable
    );
}
