package com.example.course_recommender.controller;

import com.example.course_recommender.model.Course;
import com.example.course_recommender.service.CourseService;
import com.example.course_recommender.service.CourseRecommender;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * REST Controller for managing Course resources.
 * Exposes endpoints for CRUD operations and course recommendations.
 */
@RestController // Marks this class as a REST controller, combining @Controller and @ResponseBody
@RequestMapping("/api/courses") // Base path for all endpoints in this controller
public class CourseController {

    private final CourseService courseService;
    private final CourseRecommender courseRecommender;

    /**
     * Constructor for CourseController. Spring automatically injects the service beans.
     *
     * @param courseService     The CourseService instance.
     * @param courseRecommender The CourseRecommender instance.
     */
    @Autowired
    public CourseController(CourseService courseService, CourseRecommender courseRecommender) {
        this.courseService = courseService;
        this.courseRecommender = courseRecommender;
    }

    /**
     * Endpoint to retrieve a specific course by its ID.
     * Maps to GET /api/courses/{id}
     *
     * @param id The UUID of the course to retrieve, passed as a path variable.
     * @return ResponseEntity with the Course object and HTTP status 200 (OK) if found,
     * or HTTP status 404 (Not Found) if no course matches the ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Course> viewCourse(@PathVariable UUID id) {
        Optional<Course> course = courseService.viewCourse(id);
        return course.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Request body for adding or updating a Course.
     * This nested class helps structure the input for the API, specifically to include author IDs.
     */
    @Setter
    @Getter
    static class CourseRequest {
        private String name;
        private String description;
        private int credit;
        private List<UUID> authorIds;
    }


    /**
     * Endpoint to add a new course.
     * Maps to POST /api/courses
     * Expects a JSON request body containing course details and a list of author IDs.
     *
     * @param courseRequest The CourseRequest object containing course details and author IDs.
     * @return ResponseEntity with the created Course object and HTTP status 201 (Created),
     * or HTTP status 400 (Bad Request) if input is invalid.
     */
    @PostMapping
    public ResponseEntity<Course> addCourse(@RequestBody CourseRequest courseRequest) {
        if (courseRequest.getAuthorIds() == null || courseRequest.getAuthorIds().isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST); // A course must have authors
        }
        Course newCourse = new Course(null, courseRequest.getName(), courseRequest.getDescription(), courseRequest.getCredit());
        try {
            Course savedCourse = courseService.addCourse(newCourse, courseRequest.getAuthorIds());
            return new ResponseEntity<>(savedCourse, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            // Catch validation errors from service layer, e.g., "A course must be linked to at least one author."
            System.err.println("Error adding course: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            System.err.println("An unexpected error occurred while adding course: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Endpoint to update an existing course.
     * Maps to PUT /api/courses/{id}
     * Expects a JSON request body with updated course details and potentially new author IDs.
     *
     * @param id            The UUID of the course to update, passed as a path variable.
     * @param courseRequest The CourseRequest object with updated course details and new author IDs.
     * @return ResponseEntity with the updated Course object and HTTP status 200 (OK) if found and updated,
     * or HTTP status 404 (Not Found) if the course does not exist.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Course> updateCourse(@PathVariable UUID id, @RequestBody CourseRequest courseRequest) {
        Optional<Course> existingCourseOpt = courseService.viewCourse(id); // Check if course exists first
        if (existingCourseOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Course courseToUpdate = existingCourseOpt.get();
        courseToUpdate.setName(courseRequest.getName());
        courseToUpdate.setDescription(courseRequest.getDescription());
        courseToUpdate.setCredit(courseRequest.getCredit());

        // Call the service's update method with the modified course and new author IDs
        Optional<Course> updatedCourse = courseService.updateCourse(courseToUpdate, courseRequest.getAuthorIds());

        return updatedCourse.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Endpoint to delete a specific course by its ID.
     * Maps to DELETE /api/courses/{id}
     *
     * @param id The UUID of the course to delete, passed as a path variable.
     * @return ResponseEntity with HTTP status 204 (No Content) if deleted successfully,
     * or HTTP status 404 (Not Found) if the course does not exist.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable UUID id) {
        boolean deleted = courseService.deleteCourse(id);
        if (deleted) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT); // 204 indicates successful deletion with no content to return
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // 404 if the course to delete was not found
        }
    }

    /**
     * Endpoint to discover all courses (using the recommendation service).
     * Maps to GET /api/courses/recommendations
     *
     * @return ResponseEntity with a list of recommended Course objects and HTTP status 200 (OK).
     */
    @GetMapping("/recommendations")
    public ResponseEntity<List<Course>> discoverAllCourses() {
        List<Course> recommendedCourses = courseRecommender.recommendedCourses();
        return new ResponseEntity<>(recommendedCourses, HttpStatus.OK);
    }
}
