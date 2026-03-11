package com.college.eventmanagement.dto;

import com.college.eventmanagement.model.enums.EventCategory;
import com.college.eventmanagement.model.enums.EventStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class EventDto {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String venue;
    private EventCategory category;
    private LocalDateTime registrationEnd;
    private String organizerName;
    private Long organizerId;
    private Long organisationId;
    private String organisationName;
    private EventStatus status;
    private String contactEmail;
    private Integer maxParticipants;
    private long participantCount;
    private LocalDateTime createdAt;
}
