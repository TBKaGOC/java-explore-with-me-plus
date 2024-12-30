package ru.practicum.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.request.model.EventRequest;

import java.util.List;

public interface RequestRepository extends JpaRepository<EventRequest, Long> {

    @Query("SELECT r FROM EventRequest r " +
            "WHERE r.requester.id = :userId " +
            "AND r.event.initiator.id != :userId")
    List<EventRequest> findByUserId(Long userId);

    @Query("SELECT r FROM EventRequest r " +
            "WHERE r.event.id = :eventId")
    List<EventRequest> findByEventId(Long eventId);

    @Query("SELECT r FROM EventRequest r " +
            "WHERE r.event.id in :eventIds ")
    List<EventRequest> findByEventIds(List<Long> eventIds);

    List<EventRequest> findRequestByEventIdAndStatus(Long eventId, String status);

    @Query("SELECT COUNT(r) FROM ParticipationRequest r " +
            "WHERE r.event.id = :eventId " +
            "AND r.status in :statuses")
    Long countByEventAndStatuses(Long eventId, List<String> statuses);

}
