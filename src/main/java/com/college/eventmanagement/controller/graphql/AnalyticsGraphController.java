package com.college.eventmanagement.controller.graphql;

import com.college.eventmanagement.dto.AnalyticsDto;
import com.college.eventmanagement.model.Event;
import com.college.eventmanagement.model.User;
import com.college.eventmanagement.repository.EventRepo;
import com.college.eventmanagement.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class AnalyticsGraphController {

    private final EventRepo eventRepo;
    private final UserRepo userRepo;

    @QueryMapping
    public AnalyticsDto collegeAnalytics() {
        List<Event> allEvents = eventRepo.findAll();
        return calculateAnalytics(allEvents);
    }

    @QueryMapping
    public AnalyticsDto myDepartmentAnalytics(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return emptyAnalytics();
        }
        User user = userRepo.findByEmail(userDetails.getUsername()).orElse(null);
        if (user == null || user.getDepartment() == null) {
            return emptyAnalytics();
        }

        List<Event> deptEvents = eventRepo.findAll().stream()
                .filter(e -> e.getOrganizer() != null
                && e.getOrganizer().getDepartment() != null
                && e.getOrganizer().getDepartment().equals(user.getDepartment()))
                .toList();

        return calculateAnalytics(deptEvents);
    }

    private AnalyticsDto calculateAnalytics(List<Event> events) {
        int total = events.size();
        int approved = 0;
        int pending = 0;
        int completed = 0;
        int participants = 0;

        for (Event e : events) {
            String status = e.getStatus().name();
            if (status.equals("APPROVED")) {
                approved++; 
            }else if (status.equals("COMPLETED")) {
                completed++; 
            }else if (status.startsWith("PENDING_") || status.equals("DRAFT")) {
                pending++;
            }

            participants += (e.getRegistrations() != null ? e.getRegistrations().size() : 0);
        }

        return AnalyticsDto.builder()
                .totalEvents(total)
                .approvedEvents(approved)
                .pendingEvents(pending)
                .completedEvents(completed)
                .totalParticipants(participants)
                .build();
    }

    private AnalyticsDto emptyAnalytics() {
        return AnalyticsDto.builder().build();
    }
}
