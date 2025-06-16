package com.example.course_recommender.service;

import com.example.course_recommender_api.model.Course;
import com.example.course_recommender_api.service.CourseRecommender;

import java.util.Arrays;
import java.util.List;

public class BeginnerCourseRecommender implements CourseRecommender {

    @Override
    public List<Course> recommendedCourses() {
        return Arrays.asList(
                new Course("B001", "Introduction to Programming", "Beginner"),
                new Course("B002", "Web Development Fundamentals", "Beginner")
        );
    }
}
