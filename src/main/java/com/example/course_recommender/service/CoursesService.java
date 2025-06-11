package com.example.course_recommender.service;

import com.example.course_recommender.model.Course;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CoursesService {
    private CourseRecommender courseRecommender;

    // Setter injection: Spring will call this method after instantiating CoursesService
    // to inject a suitable CourseRecommender bean.
    @Autowired
    public void setCourseRecommender(CourseRecommender courseRecommender) {
        this.courseRecommender = courseRecommender;
        System.out.println("CoursesService (setter) initialized with: " + courseRecommender.getClass().getSimpleName());
    }

    public List<Course> getRecommendedCourses() {
        return courseRecommender.recommendedCourses();
    }
}
