package com.college.eventmanagement.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.college.eventmanagement.model.Event;
import com.college.eventmanagement.model.User;
import com.college.eventmanagement.model.enums.EventCategory;
import com.college.eventmanagement.model.enums.EventStatus;

@Repository
public interface EventRepo extends JpaRepository<Event, Long> {

    Optional<Event> findByTitle(String title);

    List<Event> findByCategory(EventCategory category);

    List<Event> findByStatus(EventStatus status);

    List<Event> findByOrganizer(User organizer);

    @Query("SELECT e FROM Event e WHERE e.startTime >= :startDate AND e.endTime <= :endDate")
    List<Event> findByDateBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT e FROM Event e WHERE e.status = :status ORDER BY e.startTime ASC")
    List<Event> findUpcomingApprovedEvents(@Param("status") EventStatus status);
}
