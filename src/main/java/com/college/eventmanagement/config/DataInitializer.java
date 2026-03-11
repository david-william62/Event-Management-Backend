package com.college.eventmanagement.config;

import com.college.eventmanagement.model.User;
import com.college.eventmanagement.model.enums.Role;
import com.college.eventmanagement.model.enums.SubRole;
import com.college.eventmanagement.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Value("${admin.default.email:admin@eventmanager.com}")
    private String adminEmail;

    @Value("${admin.default.password:Admin@123}")
    private String adminPassword;

    @Value("${admin.default.first-name:System}")
    private String adminFirstName;

    @Value("${admin.default.last-name:Admin}")
    private String adminLastName;

    @Bean
    CommandLineRunner seedDefaultAdmin(UserRepo userRepo, PasswordEncoder encoder) {
        return args -> {
            if (userRepo.findByEmail(adminEmail).isEmpty()) {
                User admin = User.builder()
                        .email(adminEmail)
                        .password(encoder.encode(adminPassword))
                        .firstName(adminFirstName)
                        .lastName(adminLastName)
                        .role(Role.ADMIN)
                        .subRole(SubRole.NONE)
                        .isActive(true)
                        .isVerified(true)
                        .department("Administration")
                        .institution("Event Manager Platform")
                        .avatarColor("#C0392B")
                        .build();

                userRepo.save(admin);
                log.info("✅ Default admin user created: {}", adminEmail);
            } else {
                log.info("ℹ️  Default admin user already exists: {}", adminEmail);
            }
        };
    }
}
