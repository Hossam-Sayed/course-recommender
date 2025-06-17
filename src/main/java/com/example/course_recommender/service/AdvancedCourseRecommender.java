package com.example.course_recommender.service;

import com.example.course_recommender.model.Course;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component("advancedRecommender") // Assign a specific name to the bean
public class AdvancedCourseRecommender implements CourseRecommender {

    @Override
    public List<Course> recommendedCourses() {
        return Arrays.asList(
//                new Course("A001", "Machine Learning with Python", "Advanced"),
//                new Course("A002", "Cloud Native Architectures", "Advanced")
        );
    }
}