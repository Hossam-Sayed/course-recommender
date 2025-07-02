package com.example.course_recommender.config;

import com.example.course_recommender.filter.XValidationReportFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter; // For filter placement

/**
 * Spring Security configuration for the application.
 * Configures endpoint authorization, HTTP Basic authentication, and password encoding.
 */
@Configuration
@EnableWebSecurity // Enables Spring Security's web security features
public class SecurityConfig {

    private final XValidationReportFilter xValidationReportFilter;

    public SecurityConfig(XValidationReportFilter xValidationReportFilter) {
        this.xValidationReportFilter = xValidationReportFilter;
    }

    /**
     * Configures the security filter chain.
     *
     * @param http The HttpSecurity object to configure.
     * @return The configured SecurityFilterChain.
     * @throws Exception if an error occurs during configuration.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Course Endpoints
                        .requestMatchers(HttpMethod.POST, "/api/courses").authenticated() // /add
                        .requestMatchers(HttpMethod.PUT, "/api/courses/{id}").authenticated() // /update{id}
                        .requestMatchers(HttpMethod.DELETE, "/api/courses/{id}").authenticated() // /delete/{id}
                        .requestMatchers(HttpMethod.GET, "/api/courses/{id}").permitAll() // View course endpoint

                        // Applying similar rules to other endpoints based on HTTP methods
                        // Author Endpoints
                        .requestMatchers(HttpMethod.POST, "/api/authors").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/authors/{id}").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/authors/{id}").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/authors/**").permitAll() // View all/by ID/by email

                        // Assessment Endpoints
                        .requestMatchers(HttpMethod.POST, "/api/assessments").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/assessments/{id}").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/assessments/{id}").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/assessments/**").permitAll()

                        // Rating Endpoints
                        .requestMatchers(HttpMethod.POST, "/api/ratings").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/ratings/{id}").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/ratings/{id}").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/ratings/**").permitAll()

                        // Allow Swagger UI access
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                        // All other requests require authentication by default
                        .anyRequest().authenticated()
                )
                .httpBasic(basic -> {
                }) // Enable HTTP Basic authentication
                // Add the custom filter to the security chain
                .addFilterBefore(xValidationReportFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Provides a PasswordEncoder bean for hashing passwords.
     * BCryptPasswordEncoder is recommended for strong password hashing.
     *
     * @return A BCryptPasswordEncoder instance.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
