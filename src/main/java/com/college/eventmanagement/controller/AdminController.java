package com.college.eventmanagement.controller;

import com.college.eventmanagement.dto.CollegeDto;
import com.college.eventmanagement.dto.UserDto;
import com.college.eventmanagement.model.Organisation;
import com.college.eventmanagement.model.enums.OrgType;
import com.college.eventmanagement.model.enums.SubRole;
import com.college.eventmanagement.service.AdminService;
import com.college.eventmanagement.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final UserService userService;

    // ─── College Endpoints ────────────────────────────────────────────────
    /**
     * Public: list all active colleges (for registration dropdown).
     */
    @GetMapping("/colleges")
    public ResponseEntity<List<CollegeDto>> getActiveColleges() {
        return ResponseEntity.ok(adminService.getActiveColleges());
    }

    /**
     * Admin: list all colleges including inactive.
     */
    @GetMapping("/colleges/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CollegeDto>> getAllColleges(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(adminService.getAllColleges());
    }

    /**
     * Admin: create a new college.
     */
    @PostMapping("/colleges")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CollegeDto> createCollege(
            @Valid @RequestBody CreateCollegeRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        CollegeDto college = adminService.createCollege(
                request.getName(), request.getAddress(), request.getCity(),
                userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(college);
    }

    /**
     * Admin: update a college.
     */
    @PutMapping("/colleges/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CollegeDto> updateCollege(
            @PathVariable Long id,
            @Valid @RequestBody CreateCollegeRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(adminService.updateCollege(
                id, request.getName(), request.getAddress(), request.getCity(),
                userDetails.getUsername()));
    }

    // ─── Management User Creation ─────────────────────────────────────────
    /**
     * Admin: create a management/principal user for a college.
     */
    @PostMapping("/management-users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> createManagementUser(
            @Valid @RequestBody CreateManagementUserRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        UserDto user = adminService.createManagementUser(
                request.getEmail(), request.getPassword(),
                request.getFirstName(), request.getLastName(),
                request.getCollegeId(), request.getSubRole(),
                userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    // ─── Department / Organisation (Management users) ─────────────────────
    /**
     * Public: get departments for a college (for registration dropdown).
     */
    @GetMapping("/colleges/{collegeId}/departments")
    public ResponseEntity<List<OrgResponse>> getDepartments(@PathVariable Long collegeId) {
        return ResponseEntity.ok(
                adminService.getDepartmentsByCollege(collegeId).stream()
                        .map(OrgResponse::from)
                        .collect(Collectors.toList()));
    }

    /**
     * Public: get organisations (non-department) for a college.
     */
    @GetMapping("/colleges/{collegeId}/organisations")
    public ResponseEntity<List<OrgResponse>> getOrganisations(@PathVariable Long collegeId) {
        return ResponseEntity.ok(
                adminService.getOrganisationsByCollege(collegeId).stream()
                        .map(OrgResponse::from)
                        .collect(Collectors.toList()));
    }

    /**
     * Management: create a department for their college.
     */
    @PostMapping("/departments")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGEMENT')")
    public ResponseEntity<OrgResponse> createDepartment(
            @Valid @RequestBody CreateDeptRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Organisation dept = adminService.createDepartment(
                request.getName(), request.getDescription(),
                userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(OrgResponse.from(dept));
    }

    /**
     * Management: create an organisation for their college.
     */
    @PostMapping("/organisations")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGEMENT')")
    public ResponseEntity<OrgResponse> createOrganisation(
            @Valid @RequestBody CreateOrgRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Organisation org = adminService.createOrganisation(
                request.getName(), request.getOrgType(), request.getDescription(),
                userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(OrgResponse.from(org));
    }

    // ─── Pending Approvals ────────────────────────────────────────────────
    /**
     * Get pending user approvals for the current user's role scope.
     */
    @GetMapping("/pending-approvals")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGEMENT', 'HOD', 'FACULTY')")
    public ResponseEntity<List<UserDto>> getPendingApprovals(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(adminService.getPendingApprovals(userDetails.getUsername()));
    }

    /**
     * Verify (approve) a user account.
     */
    @PostMapping("/verify-user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGEMENT', 'HOD', 'FACULTY')")
    public ResponseEntity<UserDto> verifyUser(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userService.verifyUser(userId, userDetails.getUsername()));
    }

    // ─── Nested Request/Response DTOs ─────────────────────────────────────
    @Data
    public static class CreateCollegeRequest {

        @NotBlank
        private String name;
        private String address;
        private String city;
    }

    @Data
    public static class CreateManagementUserRequest {

        @NotBlank
        private String email;
        @NotBlank
        private String password;
        @NotBlank
        private String firstName;
        @NotBlank
        private String lastName;
        @NotNull
        private Long collegeId;
        private SubRole subRole;
    }

    @Data
    public static class CreateDeptRequest {

        @NotBlank
        private String name;
        private String description;
    }

    @Data
    public static class CreateOrgRequest {

        @NotBlank
        private String name;
        @NotNull
        private OrgType orgType;
        private String description;
    }

    @Data
    public static class OrgResponse {

        private Long id;
        private String name;
        private String orgType;
        private String description;
        private boolean isActive;

        public static OrgResponse from(Organisation org) {
            OrgResponse r = new OrgResponse();
            r.id = org.getId();
            r.name = org.getName();
            r.orgType = org.getOrgType().name();
            r.description = org.getDescription();
            r.isActive = org.isActive();
            return r;
        }
    }
}
