package com.example.course_recommender.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Data Transfer Object (DTO) for Assessment entity.
 * Used for sending assessment data to the client via REST API.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssessmentDto {
    private UUID id;
    private String content;
    private UUID courseId;
}
