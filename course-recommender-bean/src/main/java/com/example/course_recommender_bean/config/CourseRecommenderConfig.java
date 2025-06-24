package com.example.course_recommender_bean.config;

import com.example.course_recommender_bean.service.CourseRecommender;
import com.example.course_recommender_bean.service.CourseRecommenderImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CourseRecommenderConfig {

    @Bean("courseRecommender")
    public CourseRecommender courseRecommender() {
        return new CourseRecommenderImpl();
    }
}
