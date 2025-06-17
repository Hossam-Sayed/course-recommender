package com.example.course_recommender;

import com.example.course_recommender.model.Author;
import com.example.course_recommender.model.Course;
import com.example.course_recommender.service.AuthorService;
import com.example.course_recommender.service.CourseService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.LocalDate;
import java.util.*;

@SpringBootApplication
public class CourseRecommenderApplication {

    public static void main(String[] args) {
        SpringApplication.run(CourseRecommenderApplication.class, args);
    }

    /**
     * Defines a CommandLineRunner bean for testing CRUD operations on Course and Author
     * entities, including their many-to-many relationship.
     *
     * @param courseService The CourseService instance.
     * @param authorService The AuthorService instance.
     * @return A CommandLineRunner implementation.
     */
    @Bean
    public CommandLineRunner commandLineRunner(CourseService courseService, AuthorService authorService) {
        return args -> {
            System.out.println("--- Starting Full CRUD Operations and M2M Relationship Test ---");

            // --- PART 1: Author Operations ---
            System.out.println("\n--- PART 1: Author Operations ---");

            // 1. Add Authors
            System.out.println("\n1. Adding new Authors...");
            Author author1 = new Author(null, "Alice Smith", "alice.smith@example.com", LocalDate.of(1980, 5, 15));
            Author savedAuthor1 = authorService.addAuthor(author1);
            System.out.println("Added Author: " + savedAuthor1);

            Author author2 = new Author(null, "Bob Johnson", "bob.j@example.com", LocalDate.of(1975, 11, 22));
            Author savedAuthor2 = authorService.addAuthor(author2);
            System.out.println("Added Author: " + savedAuthor2);

            Author author3 = new Author(null, "Charlie Brown", "charlie.b@example.com", LocalDate.of(1990, 1, 1));
            Author savedAuthor3 = authorService.addAuthor(author3);
            System.out.println("Added Author: " + savedAuthor3);

            // 2. View all Authors
            System.out.println("\n2. Viewing all Authors:");
            List<Author> allAuthors = authorService.getAllAuthors();
            allAuthors.forEach(System.out::println);

            // 3. Update an Author
            System.out.println("\n3. Updating Author " + savedAuthor1.getName() + "...");
            savedAuthor1.setName("Alice Williams");
            savedAuthor1.setEmail("alice.williams@example.com");
            Optional<Author> updatedAuthor1Opt = authorService.updateAuthor(savedAuthor1);
            updatedAuthor1Opt.ifPresentOrElse(
                    a -> System.out.println("Updated Author: " + a),
                    () -> System.out.println("Failed to update author " + savedAuthor1.getId())
            );

            // 4. View a specific Author
            System.out.println("\n4. Viewing Author by ID: " + savedAuthor2.getId());
            Optional<Author> retrievedAuthor2Opt = authorService.viewAuthor(savedAuthor2.getId());
            retrievedAuthor2Opt.ifPresentOrElse(
                    a -> System.out.println("Found Author: " + a),
                    () -> System.out.println("Author with ID " + savedAuthor2.getId() + " not found.")
            );

            // --- PART 2: Course Operations with Authors (Many-to-Many) ---
            System.out.println("\n--- PART 2: Course Operations with Authors ---");

            // 5. Add a Course linked to multiple Authors
            System.out.println("\n5. Adding 'Introduction to Spring Boot' course linked to Alice and Bob...");
            List<UUID> course1AuthorIds = Arrays.asList(savedAuthor1.getId(), savedAuthor2.getId());
            Course course1 = new Course(null, "Introduction to Spring Boot", "Learn the basics of Spring Boot", 3);
            Course savedCourse1 = courseService.addCourse(course1, course1AuthorIds);
            System.out.println("Added Course: " + savedCourse1);
            savedCourse1.getAuthors().forEach(a -> System.out.println("  -> Linked Author: " + a.getName()));
            UUID courseId1 = savedCourse1.getId();

            // 6. Add another Course linked to a single Author
            System.out.println("\n6. Adding 'Data Structures' course linked to Charlie...");
            List<UUID> course2AuthorIds = Collections.singletonList(savedAuthor3.getId());
            Course course2 = new Course(null, "Data Structures and Algorithms", "Core CS concepts", 4);
            Course savedCourse2 = courseService.addCourse(course2, course2AuthorIds);
            System.out.println("Added Course: " + savedCourse2);
            savedCourse2.getAuthors().forEach(a -> System.out.println("  -> Linked Author: " + a.getName()));
            UUID courseId2 = savedCourse2.getId();


            // 7. View a Course by ID (with its authors)
            System.out.println("\n7. Viewing Course by ID: " + courseId1 + " (should show authors)");
            Optional<Course> retrievedCourse1Opt = courseService.viewCourse(courseId1);
            retrievedCourse1Opt.ifPresentOrElse(
                    c -> {
                        System.out.println("Found Course: " + c);
                        if (c.getAuthors() != null && !c.getAuthors().isEmpty()) {
                            System.out.println("  Associated Authors:");
                            c.getAuthors().forEach(a -> System.out.println("    - " + a.getName() + " (" + a.getEmail() + ")"));
                        } else {
                            System.out.println("  No authors associated.");
                        }
                    },
                    () -> System.out.println("Course with ID " + courseId1 + " not found.")
            );


            // 8. Update Course (details only, authors should remain)
            System.out.println("\n8. Updating Course (details only): " + courseId1);
            Course courseToUpdate1 = retrievedCourse1Opt.get(); // Assuming it's present
            courseToUpdate1.setName("Introduction to Spring Boot 6.0"); // Update name
            courseToUpdate1.setCredit(5); // Update credit
            // Pass null or empty list for authors to keep existing ones per your logic
            Optional<Course> updatedCourseDetailsOnlyOpt = courseService.updateCourse(courseToUpdate1, null);
            updatedCourseDetailsOnlyOpt.ifPresentOrElse(
                    c -> {
                        System.out.println("Updated Course (details only): " + c);
                        if (c.getAuthors() != null && !c.getAuthors().isEmpty()) {
                            System.out.println("  Associated Authors (should be same):");
                            c.getAuthors().forEach(a -> System.out.println("    - " + a.getName() + " (" + a.getEmail() + ")"));
                        }
                    },
                    () -> System.out.println("Failed to update course " + courseId1 + " details.")
            );

            // 9. Update Course (change authors)
            System.out.println("\n9. Updating Course (changing authors): " + courseId1 + " to include Charlie only.");
            Course courseToUpdate2 = updatedCourseDetailsOnlyOpt.get(); // Get the latest version
            courseToUpdate2.setDescription("An in-depth guide to Spring Boot (Revised)"); // Update description again
            List<UUID> newAuthorsForCourse1 = Collections.singletonList(savedAuthor3.getId()); // Now only Charlie
            Optional<Course> updatedCourseWithNewAuthorsOpt = courseService.updateCourse(courseToUpdate2, newAuthorsForCourse1);
            updatedCourseWithNewAuthorsOpt.ifPresentOrElse(
                    c -> {
                        System.out.println("Updated Course (with new authors): " + c);
                        if (c.getAuthors() != null && !c.getAuthors().isEmpty()) {
                            System.out.println("  Associated Authors (should be Charlie):");
                            c.getAuthors().forEach(a -> System.out.println("    - " + a.getName() + " (" + a.getEmail() + ")"));
                        }
                    },
                    () -> System.out.println("Failed to update course " + courseId1 + " with new authors.")
            );


            // 10. View all Courses (with their authors)
            System.out.println("\n10. Viewing all Courses (should include authors):");
            List<Course> allCourses = courseService.getAllCourses();
            if (allCourses.isEmpty()) {
                System.out.println("No courses found.");
            } else {
                allCourses.forEach(c -> {
                    System.out.println("Course: " + c);
                    if (c.getAuthors() != null && !c.getAuthors().isEmpty()) {
                        System.out.println("  Associated Authors:");
                        c.getAuthors().forEach(a -> System.out.println("    - " + a.getName()));
                    } else {
                        System.out.println("  No authors associated.");
                    }
                });
            }


            // --- PART 3: Deletion Scenarios ---
            System.out.println("\n--- PART 3: Deletion Scenarios ---");

            // 11. Delete a Course
            System.out.println("\n11. Deleting Course with ID: " + courseId2);
            boolean deletedCourse = courseService.deleteCourse(courseId2);
            if (deletedCourse) {
                System.out.println("Course with ID " + courseId2 + " successfully deleted.");
            } else {
                System.out.println("Failed to delete course with ID " + courseId2 + " (perhaps not found).");
            }

            // Verify Course deletion (and cascade of its author links)
            System.out.println("\n11a. Verifying Course deletion: Viewing all Courses after deletion:");
            courseService.getAllCourses().forEach(c -> {
                System.out.println("Course: " + c);
                if (c.getAuthors() != null && !c.getAuthors().isEmpty()) {
                    System.out.println("  Associated Authors:");
                    c.getAuthors().forEach(a -> System.out.println("    - " + a.getName()));
                }
            });


            // 12. Delete an Author (and check if it cascades from course_authors)
            System.out.println("\n12. Deleting Author with ID: " + savedAuthor2.getId() + " (Bob Johnson)");
            boolean deletedAuthor = authorService.deleteAuthor(savedAuthor2.getId());
            if (deletedAuthor) {
                System.out.println("Author with ID " + savedAuthor2.getId() + " successfully deleted.");
            } else {
                System.out.println("Failed to delete author with ID " + savedAuthor2.getId() + ".");
            }

            // Verify Author deletion
            System.out.println("\n12a. Verifying Author deletion: Viewing all Authors after deletion:");
            authorService.getAllAuthors().forEach(System.out::println);

            // Verify that the remaining course still has its correct authors (Charlie only for courseId1)
            System.out.println("\n12b. Verifying remaining course after author deletion: " + courseId1);
            Optional<Course> finalCourseCheckOpt = courseService.viewCourse(courseId1);
            finalCourseCheckOpt.ifPresentOrElse(
                    c -> {
                        System.out.println("Found Course: " + c);
                        if (c.getAuthors() != null && !c.getAuthors().isEmpty()) {
                            System.out.println("  Associated Authors:");
                            c.getAuthors().forEach(a -> System.out.println("    - " + a.getName() + " (" + a.getEmail() + ")"));
                        } else {
                            System.out.println("  No authors associated.");
                        }
                    },
                    () -> System.out.println("Course with ID " + courseId1 + " not found after author deletion.")
            );

            System.out.println("\n--- Full CRUD Operations and M2M Relationship Test Completed ---");
        };
    }
}
