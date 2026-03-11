package com.college.eventmanagement.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.college.eventmanagement.model.College;
import com.college.eventmanagement.model.User;
import com.college.eventmanagement.model.enums.Role;

@Repository
public interface UserRepo extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    List<User> findByRole(Role role);

    List<User> findByDepartment(String department);

    List<User> findByCollege(College college);

    List<User> findByCollegeAndRole(College college, Role role);

    List<User> findByIsVerified(boolean isVerified);

    boolean existsByEmail(String email);
}
