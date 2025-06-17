package com.example.course_recommender;

import com.example.course_recommender.model.Course;
import com.example.course_recommender.service.CourseService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@SpringBootApplication
public class CourseRecommenderApplication {

    public static void main(String[] args) {
        SpringApplication.run(CourseRecommenderApplication.class, args);
    }

    /**
     * Defines a CommandLineRunner bean that will execute after the Spring application context
     * has been fully initialized. This is ideal for performing startup tasks like testing
     * database operations.
     *
     * @param courseService The CourseService instance, automatically injected by Spring.
     * @return A CommandLineRunner implementation.
     */
    @Bean
    public CommandLineRunner commandLineRunner(CourseService courseService) {
        return args -> {
            System.out.println("--- Starting Course CRUD Operations Test ---");

            // 1. Add a new Course
            System.out.println("\n1. Adding a new Course...");
            Course newCourse = new Course(null, "Introduction to Spring Boot", "Learn the basics of Spring Boot", 3);
            Course savedCourse = courseService.addCourse(newCourse);
            System.out.println("Added Course: " + savedCourse);

            UUID courseId1 = savedCourse.getId();

            // 2. Add another Course
            System.out.println("\n2. Adding another Course...");
            Course anotherCourse = new Course(null, "Advanced PostgreSQL", "Deep dive into PostgreSQL features", 4);
            Course savedAnotherCourse = courseService.addCourse(anotherCourse);
            System.out.println("Added Course: " + savedAnotherCourse);

            // 3. View a Course by ID
            System.out.println("\n3. Viewing Course by ID: " + courseId1);
            Optional<Course> retrievedCourse = courseService.viewCourse(courseId1);
            retrievedCourse.ifPresentOrElse(
                    c -> System.out.println("Found Course: " + c),
                    () -> System.out.println("Course with ID " + courseId1 + " not found.")
            );

            // 4. Update an existing Course
            System.out.println("\n4. Updating Course with ID: " + courseId1);
            if (retrievedCourse.isPresent()) {
                Course courseToUpdate = retrievedCourse.get();
                courseToUpdate.setName("Spring Boot for Beginners (Updated)");
                courseToUpdate.setCredit(4);
                Optional<Course> updatedCourse = courseService.updateCourse(courseToUpdate);
                updatedCourse.ifPresentOrElse(
                        c -> System.out.println("Updated Course: " + c),
                        () -> System.out.println("Failed to update course with ID " + courseId1 + ".")
                );
            } else {
                System.out.println("Cannot update, original course not found.");
            }

            // 5. View all Courses
            System.out.println("\n5. Viewing all Courses:");
            List<Course> allCourses = courseService.getAllCourses();
            if (allCourses.isEmpty()) {
                System.out.println("No courses found.");
            } else {
                allCourses.forEach(System.out::println);
            }

            // 6. Delete a Course by ID
            System.out.println("\n6. Deleting Course with ID: " + courseId1);
            boolean deleted = courseService.deleteCourse(courseId1);
            if (deleted) {
                System.out.println("Course with ID " + courseId1 + " successfully deleted.");
            } else {
                System.out.println("Failed to delete course with ID " + courseId1 + " (perhaps not found).");
            }

            // 7. Verify deletion by trying to view all courses again
            System.out.println("\n7. Verifying deletion: Viewing all Courses after deletion:");
            List<Course> remainingCourses = courseService.getAllCourses();
            if (remainingCourses.isEmpty()) {
                System.out.println("No courses found after deletion.");
            } else {
                remainingCourses.forEach(System.out::println);
            }

            // Example: Try to find the deleted course
            System.out.println("\n8. Attempting to view the deleted course (should not be found): " + courseId1);
            Optional<Course> deletedCheck = courseService.viewCourse(courseId1);
            deletedCheck.ifPresentOrElse(
                    c -> System.out.println("ERROR: Deleted Course found: " + c),
                    () -> System.out.println("Correct: Course with ID " + courseId1 + " not found after deletion.")
            );

            System.out.println("\n--- Course CRUD Operations Test Completed ---");
        };
    }
}
