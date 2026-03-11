package com.college.eventmanagement.dto;

import com.college.eventmanagement.model.enums.EventCategory;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EventCreateRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String description;

    @NotNull
    @Future
    private LocalDateTime startTime;

    @NotNull
    @Future
    private LocalDateTime endTime;

    @NotBlank
    private String venue;

    @NotNull
    private EventCategory category;

    @NotNull
    @Future
    private LocalDateTime registrationEnd;

    private String contactEmail;

    @NotNull
    @Min(1)
    private Integer maxParticipants;

    @NotNull
    private Long organisationId;
}
