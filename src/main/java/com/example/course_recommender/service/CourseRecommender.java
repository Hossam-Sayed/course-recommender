package com.example.course_recommender.service;

import com.example.course_recommender.model.Course;

import java.util.List;

public interface CourseRecommender {
    List<Course> recommendedCourses();
}