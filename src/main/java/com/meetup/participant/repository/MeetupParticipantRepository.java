package com.meetup.participant.repository;

import com.meetup.participant.domain.MeetupParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MeetupParticipantRepository extends JpaRepository<MeetupParticipant, Long> {

    boolean existsByMeetupIdAndMemberId(Long meetupId, Long memberId);

    Optional<MeetupParticipant> findByMeetupIdAndMemberId(Long meetupId, Long memberId);

    List<MeetupParticipant> findAllByMeetupId(Long meetupId);

    int countByMeetupId(Long meetupId);

    void deleteByMeetupIdAndMemberId(Long meetupId, Long memberId);
}
