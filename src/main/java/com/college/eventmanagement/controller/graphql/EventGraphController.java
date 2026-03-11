package com.college.eventmanagement.controller.graphql;

import com.college.eventmanagement.model.Event;
import com.college.eventmanagement.model.User;
import com.college.eventmanagement.model.enums.EventStatus;
import com.college.eventmanagement.model.enums.Role;
import com.college.eventmanagement.repository.EventRepo;
import com.college.eventmanagement.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import com.college.eventmanagement.dto.EventDto;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class EventGraphController {

    private final EventRepo eventRepo;
    private final UserRepo userRepo;
    private final com.college.eventmanagement.service.EventService eventService;

    @QueryMapping
    public List<Event> allEvents(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return List.of();
        }
        User user = userRepo.findByEmail(userDetails.getUsername()).orElse(null);
        if (user == null || user.getRole() == Role.STUDENT) {
            return List.of();
        }
        return eventRepo.findAll();
    }

    @QueryMapping
    public List<Event> approvedEvents() {
        return eventRepo.findUpcomingApprovedEvents(EventStatus.APPROVED);
    }

    @QueryMapping
    public Event eventById(@Argument Long id) {
        return eventRepo.findById(id).orElse(null);
    }

    @QueryMapping
    public List<Event> myEvents(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return List.of();
        }
        User user = userRepo.findByEmail(userDetails.getUsername()).orElse(null);
        if (user == null) {
            return List.of();
        }
        return eventRepo.findByOrganizer(user);
    }

    @SchemaMapping(typeName = "Event", field = "participantCount")
    public int getParticipantCount(Event event) {
        return event.getRegistrations() != null ? event.getRegistrations().size() : 0;
    }

    @MutationMapping
    public Event approveEvent(@Argument Long id, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return null;
        }
        EventDto dto = eventService.advanceApproval(id, userDetails.getUsername());
        return eventRepo.findById(dto.getId()).orElse(null);
    }

    @MutationMapping
    public Event rejectEvent(@Argument Long id, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return null;
        }
        EventDto dto = eventService.rejectEvent(id, userDetails.getUsername());
        return eventRepo.findById(dto.getId()).orElse(null);
    }
}
