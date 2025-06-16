package com.example.course_recommender_advanced_impl.service;

import com.example.course_recommender_api.model.Course;
import com.example.course_recommender_api.service.CourseRecommender;
import jakarta.annotation.PostConstruct;

import java.util.Arrays;
import java.util.List;

public class AdvancedCourseRecommender implements CourseRecommender {

    @Override
    public List<Course> recommendedCourses() {
        return Arrays.asList(
                new Course("A001", "Machine Learning with Python", "Advanced"),
                new Course("A002", "Cloud Native Architectures", "Advanced")
        );
    }

    @PostConstruct
    public void init() {
        System.out.println(">>> " + getClass().getSimpleName() + " bean initialized");
    }
}