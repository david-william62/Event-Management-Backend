package com.college.eventmanagement.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.college.eventmanagement.model.enums.Role;
import com.college.eventmanagement.model.enums.SubRole;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = true)
    private String phone;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private SubRole subRole = SubRole.NONE;

    @Column(nullable = false)
    @Builder.Default
    private boolean isActive = true;

    @Column(nullable = false)
    @Builder.Default
    private boolean isVerified = false;

    @Column(nullable = true)
    private String department;

    @Column(nullable = true)
    private String institution;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "college_id")
    private College college;

    /**
     * Number of management approvals received (for management account
     * creation). Management accounts need >= 2 approvals from existing
     * management users.
     */
    @Column(nullable = false)
    @Builder.Default
    private int approvalCount = 0;

    /**
     * Roll number for students, employee ID for staff
     */
    @Column(nullable = true)
    private String rollOrEmpNo;

    /**
     * Relevant for students: e.g. "3rd Year"
     */
    @Column(nullable = true)
    private String year;

    /**
     * Hex color string for avatar background, e.g. "#4A90D9"
     */
    @Column(nullable = true)
    private String avatarColor;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrgMembership> orgMemberships = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
