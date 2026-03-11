package com.college.eventmanagement.service;

import com.college.eventmanagement.dto.EventCreateRequest;
import com.college.eventmanagement.dto.EventDto;
import com.college.eventmanagement.model.Event;
import com.college.eventmanagement.model.EventRegistration;
import com.college.eventmanagement.model.User;
import com.college.eventmanagement.model.enums.EventCategory;
import com.college.eventmanagement.model.enums.EventStatus;
import com.college.eventmanagement.model.enums.RegistrationStatus;
import com.college.eventmanagement.model.enums.Role;
import com.college.eventmanagement.repository.EventRegistrationRepo;
import com.college.eventmanagement.repository.EventRepo;
import com.college.eventmanagement.repository.OrganisationRepo;
import com.college.eventmanagement.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepo eventRepo;
    private final UserRepo userRepo;
    private final OrganisationRepo orgRepo;
    private final EventRegistrationRepo registrationRepo;

    /**
     * Returns all approved events (public listing).
     */
    @Cacheable("approvedEvents")
    public List<EventDto> getAllApprovedEvents() {
        return eventRepo.findUpcomingApprovedEvents(EventStatus.APPROVED)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    /**
     * Returns all events for admin/organizer views.
     */
    public List<EventDto> getAllEvents() {
        return eventRepo.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    /**
     * Returns events by category.
     */
    public List<EventDto> getByCategory(EventCategory category) {
        return eventRepo.findByCategory(category).stream().map(this::toDto).collect(Collectors.toList());
    }

    /**
     * Returns events organised by the authenticated user.
     */
    public List<EventDto> getMyEvents(String email) {
        User user = findUser(email);
        return eventRepo.findByOrganizer(user).stream().map(this::toDto).collect(Collectors.toList());
    }

    /**
     * Returns event by ID.
     */
    public EventDto getById(Long id) {
        return toDto(findEvent(id));
    }

    /**
     * Create a new event — sets status PENDING_FACULTY for approval flow.
     */
    @Transactional
    public EventDto createEvent(String organizerEmail, EventCreateRequest request) {
        User organizer = findUser(organizerEmail);
        com.college.eventmanagement.model.Organisation org = orgRepo.findById(request.getOrganisationId())
                .orElseThrow(() -> new RuntimeException("Organisation not found"));

        Event event = Event.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .venue(request.getVenue())
                .category(request.getCategory())
                .registrationEnd(request.getRegistrationEnd())
                .contactEmail(request.getContactEmail())
                .maxParticipants(request.getMaxParticipants())
                .organizer(organizer)
                .organisation(org)
                .status(EventStatus.PENDING_FACULTY)
                .build();

        return toDto(eventRepo.save(event));
    }

    /**
     * Update an event — only the organizer or admin may do this.
     */
    @Transactional
    @CacheEvict(value = "approvedEvents", allEntries = true)
    public EventDto updateEvent(Long id, String requesterEmail, EventCreateRequest request) {
        Event event = findEvent(id);
        User requester = findUser(requesterEmail);

        if (!event.getOrganizer().getEmail().equals(requesterEmail)
                && requester.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("You are not allowed to edit this event.");
        }

        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setStartTime(request.getStartTime());
        event.setEndTime(request.getEndTime());
        event.setVenue(request.getVenue());
        event.setCategory(request.getCategory());
        event.setRegistrationEnd(request.getRegistrationEnd());
        event.setContactEmail(request.getContactEmail());
        event.setMaxParticipants(request.getMaxParticipants());

        return toDto(eventRepo.save(event));
    }

    /**
     * Delete an event.
     */
    @Transactional
    @CacheEvict(value = "approvedEvents", allEntries = true)
    public void deleteEvent(Long id, String requesterEmail) {
        Event event = findEvent(id);
        User requester = findUser(requesterEmail);

        if (!event.getOrganizer().getEmail().equals(requesterEmail)
                && requester.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("You are not allowed to delete this event.");
        }

        eventRepo.delete(event);
    }

    /**
     * Register a user for an event. Checks: registration open, not already
     * registered, capacity not full.
     */
    @Transactional
    public void registerForEvent(Long eventId, String userEmail) {
        Event event = findEvent(eventId);
        User user = findUser(userEmail);

        if (event.getStatus() != EventStatus.APPROVED) {
            throw new IllegalStateException("Event is not open for registration.");
        }

        if (LocalDateTime.now().isAfter(event.getRegistrationEnd())) {
            throw new IllegalStateException("Registration for this event has closed.");
        }

        if (registrationRepo.existsByEventAndUser(event, user)) {
            throw new IllegalStateException("You are already registered for this event.");
        }

        long currentCount = registrationRepo.countByEvent(event);
        if (currentCount >= event.getMaxParticipants()) {
            throw new IllegalStateException("Event has reached maximum capacity.");
        }

        EventRegistration registration = EventRegistration.builder()
                .event(event)
                .user(user)
                .status(RegistrationStatus.CONFIRMED)
                .build();

        registrationRepo.save(registration);
    }

    /**
     * Advance the event approval status. Department format: PENDING_FACULTY →
     * PENDING_HOD → APPROVED Organization format: PENDING_FACULTY →
     * PENDING_MANAGEMENT → APPROVED
     */
    @Transactional
    @CacheEvict(value = "approvedEvents", allEntries = true)
    public EventDto advanceApproval(Long eventId, String approverEmail) {
        Event event = findEvent(eventId);
        User approver = findUser(approverEmail);
        com.college.eventmanagement.model.enums.OrgType orgType = event.getOrganisation().getOrgType();

        switch (event.getStatus()) {
            case PENDING_FACULTY -> {
                if (orgType == com.college.eventmanagement.model.enums.OrgType.DEPARTMENT) {
                    if (approver.getRole() == Role.FACULTY || approver.getRole() == Role.HOD || approver.getRole() == Role.ADMIN) {
                        event.setStatus(EventStatus.PENDING_HOD);
                    } else {
                        throw new AccessDeniedException("Only Faculty can forward department events.");
                    }
                } else {
                    if (approver.getRole() == Role.FACULTY || approver.getRole() == Role.MANAGEMENT || approver.getRole() == Role.ADMIN) {
                        event.setStatus(EventStatus.PENDING_MANAGEMENT);
                    } else {
                        throw new AccessDeniedException("Only Faculty Advisor can forward organization events.");
                    }
                }
            }
            case PENDING_HOD -> {
                if (approver.getRole() == Role.HOD || approver.getRole() == Role.ADMIN) {
                    event.setStatus(EventStatus.APPROVED);
                } else {
                    throw new AccessDeniedException("Only HOD or Admin can approve department events.");
                }
            }
            case PENDING_MANAGEMENT -> {
                if ((approver.getRole() == Role.MANAGEMENT && approver.getSubRole().name().equals("PRINCIPAL"))
                        || approver.getRole() == Role.ADMIN) {
                    event.setStatus(EventStatus.APPROVED);
                } else {
                    throw new AccessDeniedException("Only Management (Principal) or Admin can approve organization events.");
                }
            }
            default ->
                throw new IllegalStateException("Event cannot be approved in current state: " + event.getStatus());
        }

        return toDto(eventRepo.save(event));
    }

    /**
     * Reject an event.
     */
    @Transactional
    @CacheEvict(value = "approvedEvents", allEntries = true)
    public EventDto rejectEvent(Long eventId, String rejectorEmail) {
        Event event = findEvent(eventId);
        User rejector = findUser(rejectorEmail);

        List<Role> canReject = List.of(Role.FACULTY, Role.HOD, Role.MANAGEMENT, Role.ADMIN);
        if (!canReject.contains(rejector.getRole())) {
            throw new AccessDeniedException("You are not authorized to reject events.");
        }

        event.setStatus(EventStatus.REJECTED);
        return toDto(eventRepo.save(event));
    }

    private Event findEvent(Long id) {
        return eventRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found: " + id));
    }

    private User findUser(String email) {
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }

    public EventDto toDto(Event event) {
        return EventDto.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .startTime(event.getStartTime())
                .endTime(event.getEndTime())
                .venue(event.getVenue())
                .category(event.getCategory())
                .registrationEnd(event.getRegistrationEnd())
                .organizerName(event.getOrganizer() != null
                        ? event.getOrganizer().getFirstName() + " " + event.getOrganizer().getLastName()
                        : "Unknown")
                .organizerId(event.getOrganizer() != null ? event.getOrganizer().getId() : null)
                .organisationId(event.getOrganisation() != null ? event.getOrganisation().getId() : null)
                .organisationName(event.getOrganisation() != null ? event.getOrganisation().getName() : null)
                .status(event.getStatus())
                .contactEmail(event.getContactEmail())
                .maxParticipants(event.getMaxParticipants())
                .participantCount(registrationRepo.countByEvent(event))
                .createdAt(event.getCreatedAt())
                .build();
    }
}
