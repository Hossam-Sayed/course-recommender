package com.example.course_recommender_bean.service;

import com.example.course_recommender_bean.dto.CourseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CourseRecommender {
    Page<CourseDto> recommendedCourses(Pageable pageable);
}