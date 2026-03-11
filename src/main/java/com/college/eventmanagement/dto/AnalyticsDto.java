package com.college.eventmanagement.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AnalyticsDto {
    private int totalEvents;
    private int approvedEvents;
    private int pendingEvents;
    private int completedEvents;
    private int totalParticipants;
}
