package com.college.eventmanagement.controller;

import com.college.eventmanagement.dto.UserDto;
import com.college.eventmanagement.dto.UserUpdateRequest;
import com.college.eventmanagement.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /** Get the authenticated user's profile. */
    @GetMapping("/me")
    public ResponseEntity<UserDto> getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userService.getProfile(userDetails.getUsername()));
    }

    /** Update the authenticated user's profile. */
    @PutMapping("/me")
    public ResponseEntity<UserDto> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(userService.updateProfile(userDetails.getUsername(), request));
    }

    /**
     * Get users managed by the authenticated user.
     * Available to: FACULTY, HOD, MANAGEMENT, ADMIN.
     */
    @GetMapping("/me/managed-members")
    public ResponseEntity<List<UserDto>> getManagedMembers(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userService.getManagedMembers(userDetails.getUsername()));
    }

    /**
     * Verify a user account.
     * Enforces hierarchy rules.
     */
    @PutMapping("/{id}/verify")
    public ResponseEntity<UserDto> verifyUser(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userService.verifyUser(id, userDetails.getUsername()));
    }
}
