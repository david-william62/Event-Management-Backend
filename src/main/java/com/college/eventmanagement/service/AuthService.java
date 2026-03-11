package com.college.eventmanagement.service;

import com.college.eventmanagement.dto.*;
import com.college.eventmanagement.model.College;
import com.college.eventmanagement.model.User;
import com.college.eventmanagement.model.enums.Role;
import com.college.eventmanagement.model.enums.SubRole;
import com.college.eventmanagement.repository.CollegeRepo;
import com.college.eventmanagement.repository.UserRepo;
import com.college.eventmanagement.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepo userRepo;
    private final CollegeRepo collegeRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    public AuthResponse register(RegisterRequest request) {
        if (userRepo.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Registration failed. Please check your details and try again.");
        }

        // Resolve college if provided
        College college = null;
        if (request.getCollegeId() != null) {
            college = collegeRepo.findById(request.getCollegeId())
                    .orElseThrow(() -> new IllegalArgumentException("College not found."));
        }

        // Determine verification status:
        // Students are auto-verified=false (need faculty/class rep approval)
        // Faculty are auto-verified=false (need HOD approval)
        // HOD are auto-verified=false (need management approval)
        // Management are auto-verified=false (need 2 other management approvals)
        // ADMIN cannot self-register
        Role role = request.getRole();
        if (role == Role.ADMIN) {
            throw new IllegalArgumentException("Admin accounts cannot be created through registration.");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(role)
                .subRole(SubRole.NONE)
                .phone(request.getPhone())
                .department(request.getDepartment())
                .institution(college != null ? college.getName() : request.getInstitution())
                .college(college)
                .rollOrEmpNo(request.getRollOrEmpNo())
                .year(request.getYear())
                .isActive(true)
                .isVerified(false) // all users start unverified
                .build();

        user = userRepo.save(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtUtil.generateToken(userDetails);

        return buildAuthResponse(user, token);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepo.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtUtil.generateToken(userDetails);

        return buildAuthResponse(user, token);
    }

    private AuthResponse buildAuthResponse(User user, String token) {
        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .subRole(user.getSubRole())
                .collegeId(user.getCollege() != null ? user.getCollege().getId() : null)
                .collegeName(user.getCollege() != null ? user.getCollege().getName() : null)
                .department(user.getDepartment())
                .institution(user.getInstitution())
                .rollOrEmpNo(user.getRollOrEmpNo())
                .year(user.getYear())
                .phone(user.getPhone())
                .avatarColor(user.getAvatarColor())
                .isVerified(user.isVerified())
                .build();
    }
}
