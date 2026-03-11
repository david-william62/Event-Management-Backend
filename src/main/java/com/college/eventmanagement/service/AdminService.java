package com.college.eventmanagement.service;

import com.college.eventmanagement.dto.CollegeDto;
import com.college.eventmanagement.dto.UserDto;
import com.college.eventmanagement.model.College;
import com.college.eventmanagement.model.Organisation;
import com.college.eventmanagement.model.User;
import com.college.eventmanagement.model.enums.OrgType;
import com.college.eventmanagement.model.enums.Role;
import com.college.eventmanagement.model.enums.SubRole;
import com.college.eventmanagement.repository.CollegeRepo;
import com.college.eventmanagement.repository.OrganisationRepo;
import com.college.eventmanagement.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class AdminService {

    private final CollegeRepo collegeRepo;
    private final UserRepo userRepo;
    private final OrganisationRepo orgRepo;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;

    // ─── College Management (ADMIN only) ──────────────────────────────────

    public List<CollegeDto> getAllColleges() {
        return collegeRepo.findAll().stream()
                .map(this::toCollegeDto)
                .collect(Collectors.toList());
    }

    public List<CollegeDto> getActiveColleges() {
        return collegeRepo.findByIsActive(true).stream()
                .map(this::toCollegeDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public CollegeDto createCollege(String name, String address, String city, String adminEmail) {
        User admin = findUser(adminEmail);
        if (admin.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Only admins can create colleges.");
        }
        if (collegeRepo.existsByName(name)) {
            throw new IllegalArgumentException("A college with this name already exists.");
        }

        College college = College.builder()
                .name(name)
                .address(address)
                .city(city)
                .isActive(true)
                .build();

        return toCollegeDto(collegeRepo.save(college));
    }

    @Transactional
    public CollegeDto updateCollege(Long id, String name, String address, String city, String adminEmail) {
        User admin = findUser(adminEmail);
        if (admin.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Only admins can update colleges.");
        }

        College college = collegeRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("College not found"));

        if (name != null) college.setName(name);
        if (address != null) college.setAddress(address);
        if (city != null) college.setCity(city);

        return toCollegeDto(collegeRepo.save(college));
    }

    // ─── Management User Creation (ADMIN creates Principal for a college) ─

    @Transactional
    public UserDto createManagementUser(String email, String password, String firstName,
            String lastName, Long collegeId, SubRole subRole, String creatorEmail) {
        User creator = findUser(creatorEmail);

        if (creator.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Only admins can create management accounts.");
        }

        if (userRepo.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already in use.");
        }

        College college = collegeRepo.findById(collegeId)
                .orElseThrow(() -> new RuntimeException("College not found"));

        User mgmtUser = User.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .firstName(firstName)
                .lastName(lastName)
                .role(Role.MANAGEMENT)
                .subRole(subRole != null ? subRole : SubRole.PRINCIPAL)
                .college(college)
                .institution(college.getName())
                .department("Administration")
                .isActive(true)
                .isVerified(true) // Admin-created management users are pre-verified
                .build();

        return userService.toDto(userRepo.save(mgmtUser));
    }

    // ─── Department/Organisation Management (MANAGEMENT users) ────────────

    @Transactional
    public Organisation createDepartment(String name, String description, String managerEmail) {
        User manager = findUser(managerEmail);
        assertManagementOrAdmin(manager);

        College college = manager.getCollege();
        if (college == null && manager.getRole() != Role.ADMIN) {
            throw new IllegalStateException("Manager must be associated with a college.");
        }

        Organisation dept = Organisation.builder()
                .name(name)
                .orgType(OrgType.DEPARTMENT)
                .description(description)
                .college(college)
                .isActive(true)
                .build();

        return orgRepo.save(dept);
    }

    @Transactional
    public Organisation createOrganisation(String name, OrgType orgType, String description, String managerEmail) {
        User manager = findUser(managerEmail);
        assertManagementOrAdmin(manager);

        College college = manager.getCollege();

        Organisation org = Organisation.builder()
                .name(name)
                .orgType(orgType)
                .description(description)
                .college(college)
                .isActive(true)
                .build();

        return orgRepo.save(org);
    }

    /**
     * Get departments (OrgType.DEPARTMENT) for a given college.
     */
    public List<Organisation> getDepartmentsByCollege(Long collegeId) {
        College college = collegeRepo.findById(collegeId)
                .orElseThrow(() -> new RuntimeException("College not found"));
        return orgRepo.findByCollegeAndOrgType(college, OrgType.DEPARTMENT);
    }

    /**
     * Get non-department organisations for a given college.
     */
    public List<Organisation> getOrganisationsByCollege(Long collegeId) {
        College college = collegeRepo.findById(collegeId)
                .orElseThrow(() -> new RuntimeException("College not found"));
        return orgRepo.findByCollege(college).stream()
                .filter(o -> o.getOrgType() != OrgType.DEPARTMENT)
                .collect(Collectors.toList());
    }

    /**
     * Get all pending (unverified) users that the given manager can approve.
     */
    public List<UserDto> getPendingApprovals(String managerEmail) {
        User manager = findUser(managerEmail);
        List<User> pending;

        switch (manager.getRole()) {
            case ADMIN -> pending = userRepo.findByIsVerified(false);
            case MANAGEMENT -> {
                // Management can approve HODs in their college
                pending = userRepo.findByIsVerified(false).stream()
                        .filter(u -> u.getRole() == Role.HOD || u.getRole() == Role.MANAGEMENT)
                        .filter(u -> sameCollege(u, manager))
                        .collect(Collectors.toList());
            }
            case HOD -> {
                // HODs approve faculty in same department
                pending = userRepo.findByIsVerified(false).stream()
                        .filter(u -> u.getRole() == Role.FACULTY)
                        .filter(u -> sameDepartment(u, manager))
                        .collect(Collectors.toList());
            }
            case FACULTY -> {
                // Faculty approve students in same department
                pending = userRepo.findByIsVerified(false).stream()
                        .filter(u -> u.getRole() == Role.STUDENT)
                        .filter(u -> sameDepartment(u, manager))
                        .collect(Collectors.toList());
            }
            default -> pending = List.of();
        }

        return pending.stream().map(userService::toDto).collect(Collectors.toList());
    }

    // ─── Helpers ──────────────────────────────────────────────────────────

    private User findUser(String email) {
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }

    private void assertManagementOrAdmin(User user) {
        if (user.getRole() != Role.MANAGEMENT && user.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Only management or admin users can perform this action.");
        }
    }

    private boolean sameCollege(User a, User b) {
        if (a.getCollege() == null || b.getCollege() == null) return false;
        return a.getCollege().getId().equals(b.getCollege().getId());
    }

    private boolean sameDepartment(User a, User b) {
        if (a.getDepartment() == null || b.getDepartment() == null) return false;
        return a.getDepartment().equals(b.getDepartment()) && sameCollege(a, b);
    }

    private CollegeDto toCollegeDto(College college) {
        long deptCount = orgRepo.findByCollege(college).stream()
                .filter(o -> o.getOrgType() == OrgType.DEPARTMENT).count();
        long orgCount = orgRepo.findByCollege(college).stream()
                .filter(o -> o.getOrgType() != OrgType.DEPARTMENT).count();
        long userCount = userRepo.findByCollege(college).size();

        return CollegeDto.builder()
                .id(college.getId())
                .name(college.getName())
                .address(college.getAddress())
                .city(college.getCity())
                .isActive(college.isActive())
                .departmentCount((int) deptCount)
                .organisationCount((int) orgCount)
                .userCount((int) userCount)
                .build();
    }
}
