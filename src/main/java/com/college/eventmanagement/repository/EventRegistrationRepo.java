package com.college.eventmanagement.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.college.eventmanagement.model.Event;
import com.college.eventmanagement.model.EventRegistration;
import com.college.eventmanagement.model.User;
import com.college.eventmanagement.model.enums.EventStatus;

@Repository
public interface EventRegistrationRepo extends JpaRepository<EventRegistration, Long> {

    List<EventRegistration> findByEvent(Event event);

    List<EventRegistration> findByUser(User user);

    Optional<EventRegistration> findByEventAndUser(Event event, User user);

    boolean existsByEventAndUser(Event event, User user);

    long countByEvent(Event event);

    @Query("SELECT COUNT(r) FROM EventRegistration r WHERE r.user = :user AND r.event.status = :status AND r.event.startTime > :now")
    long countUpcomingByUser(@Param("user") User user, @Param("status") EventStatus status, @Param("now") LocalDateTime now);
}
