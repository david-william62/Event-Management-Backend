package com.college.eventmanagement.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.college.eventmanagement.model.College;

@Repository
public interface CollegeRepo extends JpaRepository<College, Long> {

    Optional<College> findByName(String name);

    List<College> findByIsActive(boolean isActive);

    boolean existsByName(String name);
}
