package com.college.eventmanagement.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.college.eventmanagement.model.enums.*;
import com.college.eventmanagement.model.User;

public interface UserRepo extends JpaRepository<User, Long> {

  Optional<User> findById(Long id);

  Optional<User> findByEmail(String email);

  Optional<User> findByUsername(String username);

  List<User> findByRoles(Role role);

  boolean existsByEmail(String email);

  boolean existsByUsername(String username);
}