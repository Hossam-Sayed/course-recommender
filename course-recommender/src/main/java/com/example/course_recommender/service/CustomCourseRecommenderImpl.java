package com.example.course_recommender.service;

import com.example.course_recommender.mapper.CourseMapper;
import com.example.course_recommender.model.Course;
import com.example.course_recommender.repository.CourseJpaRepository;
import com.example.course_recommender_bean.dto.CourseDto;
import com.example.course_recommender_bean.service.CourseRecommenderImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * A placeholder implementation of the CourseRecommender interface.
 */
@Slf4j
@Service
public class CustomCourseRecommenderImpl extends CourseRecommenderImpl {

    private final CourseJpaRepository courseJpaRepository;
    private final CourseMapper courseMapper;

    @Autowired
    public CustomCourseRecommenderImpl(CourseJpaRepository courseJpaRepository, CourseMapper courseMapper) {
        this.courseJpaRepository = courseJpaRepository;
        this.courseMapper = courseMapper;
    }

    /**
     * Provides a paginated list of recommended courses.
     * Currently, this method returns all available courses, paginated.
     *
     * @param pageable Pagination information.
     * @return A Page of CourseDto objects.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<CourseDto> recommendedCourses(Pageable pageable) {
        log.info("Executing recommendedCourses: returning all available courses with pagination.");
        // TODO: Enhance recommendation logic
        log.info("Attempting to retrieve all courses with pagination: Page {}, Size {}", pageable.getPageNumber(), pageable.getPageSize());
        Page<Course> coursePage = courseJpaRepository.findAll(pageable);
        return coursePage.map(courseMapper::toDto);
    }
}