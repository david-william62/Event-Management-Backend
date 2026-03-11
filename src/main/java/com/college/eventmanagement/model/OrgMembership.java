package com.college.eventmanagement.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "org_memberships",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "organisation_id"}))
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrgMembership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organisation_id", nullable = false)
    private Organisation organisation;

    /**
     * e.g. "Student Coordinator", "Secretary", "Faculty Advisor"
     */
    @Column(nullable = false)
    private String position;

    /**
     * e.g. "2022"
     */
    @Column(nullable = false)
    private String joinedYear;

    @Column(nullable = false)
    @Builder.Default
    private boolean isActive = true;
}
