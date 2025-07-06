package com.example.course_recommender.config;

import com.example.course_recommender.model.User;
import com.example.course_recommender.repository.UserJpaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

/**
 * Configuration to populate initial user data for testing purposes.
 */
@Configuration
public class InitialUserDataConfig {

    @Bean
    CommandLineRunner initUsers(UserJpaRepository userJpaRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // Create a default user if not exists
            if (userJpaRepository.findByUsername("user").isEmpty()) {
                User user = new User(
                        "user",
                        passwordEncoder.encode("password"),
                        Set.of("ROLE_USER")
                );
                userJpaRepository.save(user);
                System.out.println("Created default user: 'user' with password 'password'");
            }

            // Create an admin user if not exists
            if (userJpaRepository.findByUsername("admin").isEmpty()) {
                User admin = new User(
                        "admin",
                        passwordEncoder.encode("adminpass"),
                        Set.of("ROLE_USER", "ROLE_ADMIN")
                );
                userJpaRepository.save(admin);
                System.out.println("Created admin user: 'admin' with password 'adminpass'");
            }
        };
    }
}
