package com.example.event_mgm.config;

import com.example.event_mgm.Repository.role.RoleRepository;
import com.example.event_mgm.Repository.role.UserRepository;
import com.example.event_mgm.enums.RoleName;
import com.example.event_mgm.model.Role;
import com.example.event_mgm.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Starting data seeding...");


        if (roleRepository.count() == 0) {
            log.info("Seeding roles...");

            Arrays.stream(RoleName.values()).forEach(roleName -> {
                Role role = Role.builder()
                        .name(roleName.name())
                        .description(roleName.name() + " role")
                        .isActive(true)
                        .build();
                roleRepository.save(role);
                log.info("Created role: {}", roleName);
            });
        }

        // Seed admin user if no admin exists
        if (!userRepository.existsByRoleName(RoleName.ADMIN.name())) {
            log.info("Seeding admin user...");

            Role adminRole = roleRepository.findByName(RoleName.ADMIN.name())
                    .orElseThrow(() -> new RuntimeException("Admin role not found"));

            User admin = User.builder()
                    .username("admin")
                    .email("admin@eventmgm.com")
                    .password(passwordEncoder.encode("Admin@123"))
                    .fullName("System Administrator")
                    .phoneNumber("+1234567890")
                    .isEnabled(true)
                    .role(adminRole)
                    .build();

            userRepository.save(admin);
            log.info("Admin user created successfully");
        }

        log.info("Data seeding completed");
    }
}