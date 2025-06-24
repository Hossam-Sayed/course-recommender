package com.example.course_recommender_bean.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * Data Transfer Object (DTO) for Course entity.
 * Used for sending course data to the client via REST API.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseDto {
    private UUID id;
    private String name;
    private String description;
    private int credit;
    private List<AuthorDto> authors;
    // TODO: include AssessmentDto and RatingDto for full graph representation in the API response.
}
