package com.example.course_recommender.service;

import com.example.course_recommender.model.Course;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component("beginnerRecommender") // Assign a specific name to the bean
@Primary
public class BeginnerCourseRecommender implements CourseRecommender {

    @Override
    public List<Course> recommendedCourses() {
        return Arrays.asList(
                new Course("B001", "Introduction to Programming", "Beginner"),
                new Course("B002", "Web Development Fundamentals", "Beginner")
        );
    }
}
