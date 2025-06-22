package com.example.course_recommender.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Data Transfer Object (DTO) for Rating entity.
 * Used for sending rating data to the client via REST API.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RatingDto {
    private UUID id;
    private int number;
    private UUID courseId;
}
