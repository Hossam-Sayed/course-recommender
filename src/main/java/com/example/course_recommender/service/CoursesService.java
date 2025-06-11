package com.example.course_recommender.service;

import com.example.course_recommender.model.Course;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CoursesService {
    private final CourseRecommender beginnerRecommender;

    @Autowired
    public CoursesService(CourseRecommender beginnerRecommender) {
        this.beginnerRecommender = beginnerRecommender;
        System.out.println("CoursesService initialized by name: " + beginnerRecommender.getClass().getSimpleName());
    }

    public List<Course> getRecommendedCourses() {
        return beginnerRecommender.recommendedCourses();
    }
}
