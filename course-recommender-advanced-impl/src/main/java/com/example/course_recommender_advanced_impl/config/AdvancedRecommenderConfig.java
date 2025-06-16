package com.example.course_recommender_advanced_impl.config;

import com.example.course_recommender_api.service.CourseRecommender;
import com.example.course_recommender_advanced_impl.service.AdvancedCourseRecommender;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AdvancedRecommenderConfig {

    @Bean("advancedRecommender")
    public CourseRecommender advancedCourseRecommender() {
        System.out.println("AdvancedCourseRecommender bean explicitly defined in advanced-impl project.");
        return new AdvancedCourseRecommender();
    }
}