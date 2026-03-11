package com.college.eventmanagement.controller;

import com.college.eventmanagement.dto.EventCreateRequest;
import com.college.eventmanagement.dto.EventDto;
import com.college.eventmanagement.model.enums.EventCategory;
import com.college.eventmanagement.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    /**
     * Public: list all approved events.
     */
    @GetMapping
    public ResponseEntity<List<EventDto>> getAll() {
        return ResponseEntity.ok(eventService.getAllApprovedEvents());
    }

    /**
     * Admin/organizer: list all events regardless of status.
     */
    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGEMENT', 'HOD', 'FACULTY')")
    public ResponseEntity<List<EventDto>> getAllForAdmin() {
        return ResponseEntity.ok(eventService.getAllEvents());
    }

    /**
     * Get events organized by the authenticated user.
     */
    @GetMapping("/mine")
    public ResponseEntity<List<EventDto>> getMyEvents(
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(eventService.getMyEvents(userDetails.getUsername()));
    }

    /**
     * Filter events by category.
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<List<EventDto>> getByCategory(@PathVariable EventCategory category) {
        return ResponseEntity.ok(eventService.getByCategory(category));
    }

    /**
     * Get event by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<EventDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.getById(id));
    }

    /**
     * Create a new event.
     */
    @PostMapping
    public ResponseEntity<EventDto> create(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody EventCreateRequest request) {
        EventDto created = eventService.createEvent(userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Update an event.
     */
    @PutMapping("/{id}")
    public ResponseEntity<EventDto> update(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody EventCreateRequest request) {
        return ResponseEntity.ok(eventService.updateEvent(id, userDetails.getUsername(), request));
    }

    /**
     * Delete an event.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        eventService.deleteEvent(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    /**
     * Register the authenticated user for an event.
     */
    @PostMapping("/{id}/register")
    public ResponseEntity<Void> register(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        eventService.registerForEvent(id, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }

    /**
     * Advance approval status (PENDING → FORWARDED → APPROVED).
     */
    @PutMapping("/{id}/approve")
    public ResponseEntity<EventDto> approve(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(eventService.advanceApproval(id, userDetails.getUsername()));
    }

    /**
     * Reject an event.
     */
    @PutMapping("/{id}/reject")
    public ResponseEntity<EventDto> reject(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(eventService.rejectEvent(id, userDetails.getUsername()));
    }
}
