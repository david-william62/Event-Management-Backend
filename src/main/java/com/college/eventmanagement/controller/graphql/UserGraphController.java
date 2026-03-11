package com.college.eventmanagement.controller.graphql;

import com.college.eventmanagement.dto.UserDto;
import com.college.eventmanagement.model.User;
import com.college.eventmanagement.model.enums.EventStatus;
import com.college.eventmanagement.model.enums.Role;
import com.college.eventmanagement.repository.EventRegistrationRepo;
import com.college.eventmanagement.repository.EventRepo;
import com.college.eventmanagement.repository.UserRepo;
import com.college.eventmanagement.service.AdminService;
import com.college.eventmanagement.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class UserGraphController {

    private final UserRepo userRepo;
    private final UserService userService;
    private final AdminService adminService;
    private final EventRepo eventRepo;
    private final EventRegistrationRepo registrationRepo;

    @QueryMapping
    public User me(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return null;
        }
        return userRepo.findByEmail(userDetails.getUsername()).orElse(null);
    }

    @QueryMapping
    public User userById(@Argument Long id, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return null;
        }
        User requester = userRepo.findByEmail(userDetails.getUsername()).orElse(null);
        if (requester == null) {
            return null;
        }
        // Only admins/management can look up arbitrary users; others can only view themselves
        if (requester.getRole() == Role.ADMIN
                || requester.getRole() == Role.MANAGEMENT
                || requester.getId().equals(id)) {
            return userRepo.findById(id).orElse(null);
        }
        return null;
    }

    @SchemaMapping(typeName = "User", field = "managedMembers")
    public List<User> getManagedMembers(User user) {
        return userService.getManagedMembers(user.getEmail())
                .stream().map(dto -> userRepo.findById(dto.getId()).orElse(null))
                .filter(u -> u != null)
                .toList();
    }

    @SchemaMapping(typeName = "User", field = "eventsOrganised")
    public int getEventsOrganised(User user) {
        return eventRepo.findByOrganizer(user).size();
    }

    @SchemaMapping(typeName = "User", field = "eventsAttended")
    public int getEventsAttended(User user) {
        return registrationRepo.findByUser(user).size();
    }

    @SchemaMapping(typeName = "User", field = "eventsUpcoming")
    public int getEventsUpcoming(User user) {
        return (int) registrationRepo.countUpcomingByUser(user, EventStatus.APPROVED, LocalDateTime.now());
    }

    @SchemaMapping(typeName = "User", field = "collegeId")
    public Long getCollegeId(User user) {
        try {
            return user.getCollege() != null ? user.getCollege().getId() : null;
        } catch (Exception e) {
            return null;
        }
    }

    @SchemaMapping(typeName = "User", field = "collegeName")
    public String getCollegeName(User user) {
        try {
            return user.getCollege() != null ? user.getCollege().getName() : null;
        } catch (Exception e) {
            return null;
        }
    }

    // ─── Pending Approvals (users awaiting verification) ──────────────────
    @QueryMapping
    public List<User> pendingApprovals(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return List.of();
        }
        List<UserDto> dtos = adminService.getPendingApprovals(userDetails.getUsername());
        return dtos.stream()
                .map(dto -> userRepo.findById(dto.getId()).orElse(null))
                .filter(u -> u != null)
                .toList();
    }

    // ─── Verify User Mutation ─────────────────────────────────────────────
    @MutationMapping
    public User verifyUser(@Argument Long userId, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            throw new org.springframework.security.access.AccessDeniedException("Authentication required.");
        }
        UserDto dto = userService.verifyUser(userId, userDetails.getUsername());
        return userRepo.findById(dto.getId()).orElse(null);
    }
}
