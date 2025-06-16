package com.example.course_recommender.service;

import com.example.course_recommender_api.model.Course;
import com.example.course_recommender_api.service.CourseRecommender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CoursesService {
    private final CourseRecommender beginnerRecommender;
    private final CourseRecommender midLevelRecommender;
    private final CourseRecommender courseRecommender;


    @Autowired
    public CoursesService(
            @Qualifier("beginnerRecommender") CourseRecommender beginnerRecommender,
            @Qualifier("midLevelRecommender") CourseRecommender midLevelRecommender,
            @Qualifier("advancedRecommender") CourseRecommender courseRecommender
    ) {
        this.beginnerRecommender = beginnerRecommender;
        this.midLevelRecommender = midLevelRecommender;
        this.courseRecommender = courseRecommender;

        System.out.println("CoursesService initialized.");
        System.out.println("Beginner recommender: " + this.beginnerRecommender.getClass().getSimpleName() + " - " + this.beginnerRecommender.recommendedCourses().get(0).getLevel());
        System.out.println("Mid-Level recommender: " + this.midLevelRecommender.getClass().getSimpleName() + " - " + this.midLevelRecommender.recommendedCourses().get(0).getLevel());
        System.out.println("Overridden Advanced recommender: : " + this.courseRecommender.getClass().getSimpleName() + " - " + this.courseRecommender.recommendedCourses().get(0).getLevel());
    }

    public List<Course> getBeginnerCourses() {
        System.out.println("Getting courses from the beginner recommender:");
        return beginnerRecommender.recommendedCourses();
    }

    public List<Course> getMidLevelCourses() {
        System.out.println("Getting courses from the mid-level recommender:");
        return midLevelRecommender.recommendedCourses();
    }

    public List<Course> getRecommendedCourses() {
        System.out.println("Getting courses from the default (overridden advanced) recommender:");
        return courseRecommender.recommendedCourses();
    }
}