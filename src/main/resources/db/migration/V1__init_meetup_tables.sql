CREATE TABLE IF NOT EXISTS meetup (
    id              BIGINT        NOT NULL AUTO_INCREMENT       COMMENT '모임 PK',
    host_member_id  BIGINT        NOT NULL                      COMMENT '호스트 member_id',
    title           VARCHAR(255)  NOT NULL                      COMMENT '제목',
    type_code       VARCHAR(30)   NULL                          COMMENT '모임 유형 코드',
    region_code     VARCHAR(30)   NULL                          COMMENT '지역 코드',
    place           VARCHAR(255)  NULL                          COMMENT '장소',
    description     TEXT          NULL                          COMMENT '설명',
    capacity        INT           NOT NULL                      COMMENT '정원',
    meet_at         DATETIME(3)   NOT NULL                      COMMENT '모임 일시',
    status          VARCHAR(10)   NOT NULL DEFAULT 'OPEN'       COMMENT 'OPEN|FULL|CLOSED',
    created_at      DATETIME(3)   NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at      DATETIME(3)   NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    deleted_at      DATETIME(3)   NULL,
    PRIMARY KEY (id),
    KEY idx_meetup_host     (host_member_id),
    KEY idx_meetup_status   (status),
    KEY idx_meetup_meet_at  (meet_at),
    KEY idx_meetup_region   (region_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='모임';

CREATE TABLE IF NOT EXISTS meetup_participant (
    id          BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '참가자 PK',
    meetup_id   BIGINT        NOT NULL                 COMMENT '모임 FK',
    member_id   BIGINT        NOT NULL                 COMMENT '회원 member_id',
    role        VARCHAR(10)   NOT NULL DEFAULT 'MEMBER' COMMENT 'HOST|MEMBER',
    joined_at   DATETIME(3)   NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uq_meetup_member (meetup_id, member_id),
    KEY idx_participant_meetup  (meetup_id),
    KEY idx_participant_member  (member_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='모임 참가자';

CREATE TABLE IF NOT EXISTS meetup_chat_message (
    id                BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '메시지 PK',
    meetup_id         BIGINT        NOT NULL                 COMMENT '모임 FK',
    sender_member_id  BIGINT        NULL                     COMMENT '발신자 member_id (시스템 메시지는 NULL)',
    is_system         TINYINT(1)    NOT NULL DEFAULT 0       COMMENT '1=시스템 메시지',
    message           TEXT          NOT NULL                 COMMENT '메시지 내용',
    created_at        DATETIME(3)   NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    KEY idx_chat_meetup   (meetup_id),
    KEY idx_chat_created  (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='모임 채팅 메시지';
