package com.example.course_recommender.service;

import com.example.course_recommender_api.model.Course;
import com.example.course_recommender_api.service.CourseRecommender;
import jakarta.annotation.PostConstruct;

import java.util.Arrays;
import java.util.List;

public class CustomAdvancedCourseRecommender implements CourseRecommender {

    @Override
    public List<Course> recommendedCourses() {
        return Arrays.asList(
                new Course("OA001", "Overridden ML Basics", "Advanced-Custom"),
                new Course("OA002", "Overridden Cloud Practices", "Advanced-Custom"),
                new Course("OA003", "New Custom Advanced Topic", "Advanced-Custom")
        );
    }

    @PostConstruct
    public void init() {
        System.out.println(">>> " + getClass().getSimpleName() + " bean initialized");
    }
}

