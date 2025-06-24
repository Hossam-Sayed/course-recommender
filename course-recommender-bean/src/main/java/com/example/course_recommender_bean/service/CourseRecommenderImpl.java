package com.example.course_recommender_bean.service;

import com.example.course_recommender_bean.dto.CourseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * A placeholder implementation of the CourseRecommender interface.
 */
@Service
public class CourseRecommenderImpl implements CourseRecommender {

    /**
     * Provides a paginated list of recommended courses.
     * Currently, this method returns all available courses, paginated.
     *
     * @param pageable Pagination information.
     * @return A Page of CourseDto objects.
     */
    @Override
    public Page<CourseDto> recommendedCourses(Pageable pageable) {
        System.out.println("Executing CourseRecommenderImpl: Applying Dummy recommendation logic.");
        // Dummy recommendation logic
        List<CourseDto> dummyList = List.of(new CourseDto());
        return new org.springframework.data.domain.PageImpl<>(dummyList, pageable, dummyList.size());
    }
}
