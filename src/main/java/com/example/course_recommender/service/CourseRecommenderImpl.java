package com.example.course_recommender.service;

import com.example.course_recommender.model.Course;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * A placeholder implementation of the CourseRecommender interface.
 * For now, it simply returns all courses available in the system.
 * This can be extended later with actual recommendation algorithms.
 */
@Service // Marks this class as a Spring service component
public class CourseRecommenderImpl implements CourseRecommender {

    private final CourseService courseService;

    /**
     * Constructor for CourseRecommenderImpl. Spring will automatically
     * inject the CourseService instance.
     *
     * @param courseService The CourseService instance.
     */
    @Autowired
    public CourseRecommenderImpl(CourseService courseService) {
        this.courseService = courseService;
    }

    /**
     * Provides a list of recommended courses.
     * Currently, this method returns all available courses.
     *
     * @return A list of all Course objects.
     */
    @Override
    public List<Course> recommendedCourses() {
        System.out.println("Executing recommendedCourses: returning all available courses.");
        // TODO: Enhance recommendation logic
        // TODO: Update after refactoring to JPA
        return Arrays.asList();
    }
}
