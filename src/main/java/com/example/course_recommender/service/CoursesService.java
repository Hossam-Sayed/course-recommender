package com.example.course_recommender.service;

import com.example.course_recommender.model.Course;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CoursesService {
    private final CourseRecommender courseRecommender;

    // Constructor injection: Spring will automatically find a suitable CourseRecommender bean
    // and inject it when creating an instance of CoursesService.
    @Autowired
    public CoursesService(CourseRecommender courseRecommender) {
        this.courseRecommender = courseRecommender;
        System.out.println("CoursesService initialized with: " + courseRecommender.getClass().getSimpleName());
    }

    public List<Course> getRecommendedCourses() {
        return courseRecommender.recommendedCourses();
    }
}
