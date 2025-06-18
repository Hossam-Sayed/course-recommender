package com.example.course_recommender.controller;

import com.example.course_recommender.service.CourseService;
import com.example.course_recommender.service.CourseRecommender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for managing Course resources.
 * Exposes endpoints for CRUD operations and course recommendations.
 */
@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService courseService;
    private final CourseRecommender courseRecommender;

    /**
     * Constructor for CourseController. Spring automatically injects the service beans.
     *
     * @param courseService     The CourseService instance.
     * @param courseRecommender The CourseRecommender instance.
     */
    @Autowired
    public CourseController(CourseService courseService, CourseRecommender courseRecommender) {
        this.courseService = courseService;
        this.courseRecommender = courseRecommender;
    }

    // TODO: Implement necessary controller methods after refactoring to JPA
}
