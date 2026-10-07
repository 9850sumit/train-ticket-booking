package com.trainbooking.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.trainbooking.entity.Role;
import com.trainbooking.entity.User;
import com.trainbooking.repository.UserRepository;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initializeAdmin(
            UserRepository userRepository,
            BCryptPasswordEncoder passwordEncoder) {

        return args -> {

            String enabledValue =
                    System.getenv("BOOTSTRAP_ADMIN_ENABLED");

            boolean enabled =
                    "true".equalsIgnoreCase(enabledValue);

            if (!enabled) {
                return;
            }

            String adminEmail =
                    System.getenv("BOOTSTRAP_ADMIN_EMAIL");

            String adminPassword =
                    System.getenv("BOOTSTRAP_ADMIN_PASSWORD");

            String adminPhone =
                    System.getenv("BOOTSTRAP_ADMIN_PHONE");

            String adminName =
                    System.getenv("BOOTSTRAP_ADMIN_NAME");

            if (isBlank(adminEmail)
                    || isBlank(adminPassword)
                    || isBlank(adminPhone)
                    || isBlank(adminName)) {

                throw new IllegalStateException(
                        "Bootstrap admin is enabled, but required "
                        + "admin environment variables are missing."
                );
            }

            if (userRepository.findByEmail(adminEmail).isPresent()) {
                return;
            }

            if (userRepository.findByPhone(adminPhone).isPresent()) {
                throw new IllegalStateException(
                        "Bootstrap admin phone number is already registered."
                );
            }

            User admin = new User();

            admin.setFullName(adminName.trim());
            admin.setEmail(adminEmail.trim().toLowerCase());
            admin.setPhone(adminPhone.trim());
            admin.setPassword(
                    passwordEncoder.encode(adminPassword)
            );
            admin.setRole(Role.ADMIN);
            admin.setActive(true);

            userRepository.save(admin);
        };
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}