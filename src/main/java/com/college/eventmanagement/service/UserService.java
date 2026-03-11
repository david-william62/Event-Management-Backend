package com.college.eventmanagement.service;

import com.college.eventmanagement.dto.OrgMembershipDto;
import com.college.eventmanagement.dto.UserDto;
import com.college.eventmanagement.dto.UserUpdateRequest;
import com.college.eventmanagement.model.User;
import com.college.eventmanagement.model.enums.Role;
import com.college.eventmanagement.repository.EventRegistrationRepo;
import com.college.eventmanagement.repository.EventRepo;
import com.college.eventmanagement.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepo userRepo;
    private final EventRepo eventRepo;
    private final EventRegistrationRepo registrationRepo;

    /**
     * Get the full profile for the currently authenticated user.
     */
    public UserDto getProfile(String email) {
        User user = findByEmail(email);
        return toDto(user);
    }

    /**
     * Update mutable profile fields.
     */
    public UserDto updateProfile(String email, UserUpdateRequest request) {
        User user = findByEmail(email);

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getDepartment() != null) {
            user.setDepartment(request.getDepartment());
        }
        if (request.getInstitution() != null) {
            user.setInstitution(request.getInstitution());
        }
        if (request.getRollOrEmpNo() != null) {
            user.setRollOrEmpNo(request.getRollOrEmpNo());
        }
        if (request.getYear() != null) {
            user.setYear(request.getYear());
        }
        if (request.getAvatarColor() != null) {
            user.setAvatarColor(request.getAvatarColor());
        }
        if (request.getSubRole() != null) {
            user.setSubRole(request.getSubRole());
        }

        user = userRepo.save(user);
        return toDto(user);
    }

    /**
     * Returns users that the given manager can see: - HOD → everyone in same
     * department - FACULTY → same department students - MANAGEMENT / ADMIN →
     * all users
     */
    public List<UserDto> getManagedMembers(String managerEmail) {
        User manager = findByEmail(managerEmail);
        List<User> members;

        switch (manager.getRole()) {
            case ADMIN, MANAGEMENT ->
                members = userRepo.findAll();
            case HOD ->
                members = userRepo.findByDepartment(manager.getDepartment());
            case FACULTY ->
                members = userRepo.findByDepartment(manager.getDepartment())
                        .stream()
                        .filter(u -> u.getRole() == Role.STUDENT)
                        .collect(Collectors.toList());
            default ->
                members = List.of();
        }

        return members.stream()
                .filter(u -> !u.getEmail().equals(managerEmail))
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private User findByEmail(String email) {
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }

    public UserDto toDto(User user) {
        List<OrgMembershipDto> orgDtos = user.getOrgMemberships().stream()
                .map(m -> OrgMembershipDto.builder()
                .id(m.getId())
                .orgId(m.getOrganisation().getId())
                .orgName(m.getOrganisation().getName())
                .orgType(m.getOrganisation().getOrgType())
                .position(m.getPosition())
                .joinedYear(m.getJoinedYear())
                .isActive(m.isActive())
                .memberCount(m.getOrganisation().getMemberships().size())
                .build())
                .collect(Collectors.toList());

        long organised = eventRepo.findByOrganizer(user).size();
        long attended = registrationRepo.findByUser(user).size();

        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .role(user.getRole())
                .subRole(user.getSubRole())
                .isActive(user.isActive())
                .isVerified(user.isVerified())
                .collegeId(user.getCollege() != null ? user.getCollege().getId() : null)
                .collegeName(user.getCollege() != null ? user.getCollege().getName() : null)
                .department(user.getDepartment())
                .institution(user.getInstitution())
                .rollOrEmpNo(user.getRollOrEmpNo())
                .year(user.getYear())
                .avatarColor(user.getAvatarColor())
                .eventsOrganised((int) organised)
                .eventsAttended((int) attended)
                .organisations(orgDtos)
                .build();
    }

    @org.springframework.transaction.annotation.Transactional
    public UserDto verifyUser(Long targetUserId, String approverEmail) {
        User target = userRepo.findById(targetUserId)
                .orElseThrow(() -> new UsernameNotFoundException("Target user not found"));
        User approver = findByEmail(approverEmail);

        if (target.isVerified()) {
            throw new IllegalStateException("User is already verified.");
        }

        boolean canVerify = false;

        switch (target.getRole()) {
            case STUDENT -> {
                // Students verified by Faculty/Class Rep
                if (approver.getRole() == Role.FACULTY || approver.getRole() == Role.HOD || approver.getRole() == Role.ADMIN) {
                    canVerify = true;
                }
            }
            case FACULTY -> {
                // Faculty verified by HOD
                if (approver.getRole() == Role.HOD || approver.getRole() == Role.MANAGEMENT || approver.getRole() == Role.ADMIN) {
                    canVerify = true;
                }
            }
            case HOD, MANAGEMENT -> {
                // HODs and Management verified by Management or Admin
                if (approver.getRole() == Role.MANAGEMENT || approver.getRole() == Role.ADMIN) {
                    canVerify = true;
                }
            }
            default ->
                canVerify = false;
        }

        if (!canVerify) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "You do not have permission to verify this user."
            );
        }

        target.setVerified(true);
        return toDto(userRepo.save(target));
    }
}
