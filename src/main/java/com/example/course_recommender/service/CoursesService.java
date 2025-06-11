package com.example.course_recommender.service;

import com.example.course_recommender.model.Course;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CoursesService {
    private final CourseRecommender courseRecommender;

    @Autowired
    // Use @Qualifier to specify which bean to inject based on its name
    public CoursesService(@Qualifier("advancedRecommender") CourseRecommender courseRecommender) {
        this.courseRecommender = courseRecommender;
        System.out.println("CoursesService initialized with @Qualifier: " + courseRecommender.getClass().getSimpleName());
    }

    public List<Course> getRecommendedCourses() {
        return courseRecommender.recommendedCourses();
    }
}
