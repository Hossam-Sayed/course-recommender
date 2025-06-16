package com.example.course_recommender.config;

import com.example.course_recommender.service.CustomAdvancedCourseRecommender;
import com.example.course_recommender_api.service.CourseRecommender;
import com.example.course_recommender.service.BeginnerCourseRecommender;
import com.example.course_recommender.service.IntermediateCourseRecommender;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean("beginnerRecommender")
    public CourseRecommender beginnerCourseRecommender() {
        System.out.println("BeginnerCourseRecommender bean explicitly defined in main app.");
        return new BeginnerCourseRecommender();
    }

    @Bean("midLevelRecommender")
    public CourseRecommender midLevelCourseRecommender() {
        System.out.println("MidLevelCourseRecommender bean explicitly defined in main app.");
        return new IntermediateCourseRecommender();
    }

    @Bean("advancedRecommender")
    public CourseRecommender customAdvancedCourseRecommender() {
        System.out.println("Overriding advancedRecommender bean with a custom version.");
        return new CustomAdvancedCourseRecommender();
    }
}