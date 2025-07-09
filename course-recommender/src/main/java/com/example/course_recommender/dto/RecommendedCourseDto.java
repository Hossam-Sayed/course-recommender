package com.example.course_recommender.dto;

import com.example.course_recommender_bean.dto.CourseDto;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Data Transfer Object (DTO) for recommended courses, extending CourseDto
 * to include recommendation-specific fields.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class RecommendedCourseDto extends CourseDto {
    private Double recommendationScore;
    private String recommendedBy;
}
