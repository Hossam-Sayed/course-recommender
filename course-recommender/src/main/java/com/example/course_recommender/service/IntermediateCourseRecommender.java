package com.example.course_recommender.service;

import com.example.course_recommender_api.model.Course;
import com.example.course_recommender_api.service.CourseRecommender;

import java.util.Arrays;
import java.util.List;

public class IntermediateCourseRecommender implements CourseRecommender {
    @Override
    public List<Course> recommendedCourses() {
        return Arrays.asList(
                new Course("M001", "Intermediate Java Development", "Mid-Level"),
                new Course("M002", "Advanced SQL & Database Design", "Mid-Level")
        );
    }
}
