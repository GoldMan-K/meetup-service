package com.meetup.meetup.domain;

import com.meetup.global.exception.BusinessException;
import com.meetup.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "meetup")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Meetup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long hostMemberId;
    private String title;
    private String typeCode;
    private String regionCode;
    private String place;

    @Column(columnDefinition = "TEXT")
    private String description;

    private int capacity;
    private LocalDateTime meetAt;

    private String status; // OPEN | FULL | CLOSED

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    @Builder
    public Meetup(Long hostMemberId, String title, String typeCode, String regionCode,
                  String place, String description, int capacity, LocalDateTime meetAt) {
        this.hostMemberId = hostMemberId;
        this.title = title;
        this.typeCode = typeCode;
        this.regionCode = regionCode;
        this.place = place;
        this.description = description;
        this.capacity = capacity;
        this.meetAt = meetAt;
        this.status = "OPEN";
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // 비즈니스 메서드

    public void validateHost(Long memberId) {
        if (!this.hostMemberId.equals(memberId)) {
            throw new BusinessException(ErrorCode.MEETUP_NOT_HOST);
        }
    }

    public void validateNotCanceled() {
        if (this.deletedAt != null) {
            throw new BusinessException(ErrorCode.MEETUP_CANCELED);
        }
    }

    public void validateNotClosed() {
        if ("CLOSED".equals(this.status)) {
            throw new BusinessException(ErrorCode.MEETUP_ALREADY_CLOSED);
        }
    }

    public void validateNotFull() {
        if ("FULL".equals(this.status)) {
            throw new BusinessException(ErrorCode.MEETUP_FULL);
        }
    }

    public void update(String title, String typeCode, String regionCode,
                       String place, String description, int capacity, LocalDateTime meetAt) {
        this.title = title;
        this.typeCode = typeCode;
        this.regionCode = regionCode;
        this.place = place;
        this.description = description;
        this.capacity = capacity;
        this.meetAt = meetAt;
    }

    public void cancel() {
        this.status = "CLOSED";
        this.deletedAt = LocalDateTime.now();
    }

    public void close() {
        this.status = "CLOSED";
    }

    public void markFull() {
        this.status = "FULL";
    }

    public void markOpen() {
        this.status = "OPEN";
    }

    public boolean isCanceled() {
        return this.deletedAt != null;
    }
}
