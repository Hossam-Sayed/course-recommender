package com.example.course_recommender.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Custom filter to check for the 'x-validation-report' header.
 * Requests without 'x-validation-report: true' will be rejected with a 400 Bad Request.
 */
@Component
public class XValidationReportFilter extends OncePerRequestFilter {

    private static final String X_VALIDATION_HEADER = "x-validation-report";
    private static final String REQUIRED_HEADER_VALUE = "true";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String validationHeader = request.getHeader(X_VALIDATION_HEADER);

        // Check if the header is present and has the required value
        if (validationHeader == null || !validationHeader.equalsIgnoreCase(REQUIRED_HEADER_VALUE)) {
            // If not valid, send 400 Bad Request and stop the filter chain
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Missing or invalid '" + X_VALIDATION_HEADER + "' header. It must be '" + REQUIRED_HEADER_VALUE + "'.\"}");
            return; // Stop processing the request
        }

        // If the header is valid, continue the filter chain
        filterChain.doFilter(request, response);
    }
}
