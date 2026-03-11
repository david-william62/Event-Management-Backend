package com.college.eventmanagement.model;

import java.time.LocalDateTime;

import com.college.eventmanagement.model.enums.RegistrationStatus;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "event_registrations",
    uniqueConstraints = @UniqueConstraint(columnNames = {"event_id", "user_id"}))
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EventRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private RegistrationStatus status = RegistrationStatus.CONFIRMED;

    @Column(nullable = false, updatable = false)
    private LocalDateTime registeredAt;

    @PrePersist
    protected void onCreate() {
        registeredAt = LocalDateTime.now();
    }
}
