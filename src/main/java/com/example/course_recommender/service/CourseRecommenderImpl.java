package com.example.course_recommender.service;

import com.example.course_recommender.dto.CourseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * A placeholder implementation of the CourseRecommender interface.
 */
@Service
public class CourseRecommenderImpl implements CourseRecommender {

    private final CourseService courseService;

    @Autowired
    public CourseRecommenderImpl(CourseService courseService) {
        this.courseService = courseService;
    }

    /**
     * Provides a paginated list of recommended courses.
     * Currently, this method returns all available courses, paginated.
     *
     * @param pageable Pagination information.
     * @return A Page of CourseDto objects.
     */
    @Override
    public Page<CourseDto> recommendedCourses(Pageable pageable) {
        System.out.println("Executing recommendedCourses: returning all available courses with pagination.");
        // TODO: Enhance recommendation logic
        return courseService.getAllCourses(pageable);
    }
}
