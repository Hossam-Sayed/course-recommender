package com.example.course_recommender;

import com.example.course_recommender.service.CoursesService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class CourseRecommenderApplication implements CommandLineRunner {

    private final ApplicationContext applicationContext;

    public CourseRecommenderApplication(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public static void main(String[] args) {
        SpringApplication.run(CourseRecommenderApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("\nApplication Started. Demonstrating bean injection and usage:");

        CoursesService coursesService = applicationContext.getBean(CoursesService.class);

        System.out.println("\n--- Beginner Courses ---");
        coursesService.getBeginnerCourses().forEach(System.out::println);

        System.out.println("\n--- Mid-Level Courses ---");
        coursesService.getMidLevelCourses().forEach(System.out::println);

        System.out.println("\n--- Recommended Courses (Default/Overridden Advanced) ---");
        coursesService.getRecommendedCourses().forEach(System.out::println);

        System.out.println("\nAll done!");
    }
}
