package com.example.course_recommender_api.service;

import com.example.course_recommender_api.model.Course;

import java.util.List;

public interface CourseRecommender {
    List<Course> recommendedCourses();
}