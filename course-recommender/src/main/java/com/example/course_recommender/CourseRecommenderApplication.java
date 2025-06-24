package com.example.course_recommender;

import com.example.course_recommender_bean.config.CourseRecommenderConfig;
import com.example.course_recommender_bean.service.CourseRecommenderImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
public class CourseRecommenderApplication {

    public static void main(String[] args) {
        SpringApplication.run(CourseRecommenderApplication.class, args);
    }
}
