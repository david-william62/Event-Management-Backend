package com.college.eventmanagement.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.college.eventmanagement.model.Event;
import com.college.eventmanagement.model.enums.EventCategory;

@Repository
public interface EventRepo extends JpaRepository<Event, Long> {

  Optional<Event> findById(Long id);

  Optional<Event> findByTitle(String title);

  List<Event> findByCategory(EventCategory category);

  @Query("SELECT e from Event e where e.startTime >= :startDate AND e.endTime <= :endDate")
  List<Event> findByDateBetween(LocalDateTime startDate, LocalDateTime endDate);
}
