package com.example.course_recommender.controller;

import com.example.course_recommender_bean.dto.CourseDto;
import com.example.course_recommender.service.CourseService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService courseService;

    /**
     * Constructor for CourseController. Spring automatically injects the service beans.
     *
     * @param courseService The CourseService instance.
     */
    @Autowired
    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    /**
     * Endpoint to retrieve a specific course by its ID.
     * Maps to GET /api/courses/{id}
     *
     * @param id The UUID of the course to retrieve, passed as a path variable.
     * @return ResponseEntity with the CourseDto object and HTTP status 200 (OK) if found,
     * or HTTP status 404 (Not Found) if no course matches the ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<CourseDto> viewCourse(@PathVariable UUID id) {
        Optional<CourseDto> courseDto = courseService.viewCourse(id);
        return courseDto.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Request body for adding or updating a Course.
     */
    @Setter
    @Getter
    static class CourseInputDto {
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
     * @param inputDto The CourseInputDto object containing course details and author IDs.
     * @return ResponseEntity with the created CourseDto object and HTTP status 201 (Created),
     * or HTTP status 400 (Bad Request) if input is invalid.
     */
    @PostMapping
    public ResponseEntity<CourseDto> addCourse(@RequestBody CourseInputDto inputDto) {
        if (inputDto.getAuthorIds() == null || inputDto.getAuthorIds().isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        // Create a CourseDto from the inputDto for the service layer
        CourseDto courseDto = new CourseDto(null, inputDto.getName(), inputDto.getDescription(), inputDto.getCredit(), null);
        try {
            CourseDto savedCourseDto = courseService.addCourse(courseDto, inputDto.getAuthorIds());
            return new ResponseEntity<>(savedCourseDto, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
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
     * @param id       The UUID of the course to update, passed as a path variable.
     * @param inputDto The CourseInputDto object with updated course details and new author IDs.
     * @return ResponseEntity with the updated CourseDto object and HTTP status 200 (OK) if found and updated,
     * or HTTP status 404 (Not Found) if the course does not exist.
     */
    @PutMapping("/{id}")
    public ResponseEntity<CourseDto> updateCourse(@PathVariable UUID id, @RequestBody CourseInputDto inputDto) {
        CourseDto courseDto = new CourseDto(id, inputDto.getName(), inputDto.getDescription(), inputDto.getCredit(), null);
        Optional<CourseDto> updatedCourseDto = courseService.updateCourse(id, courseDto, inputDto.getAuthorIds());

        return updatedCourseDto.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
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
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Endpoint to get all courses with pagination.
     * Maps to GET /api/courses/courses?page=0&size=10&sort=name,asc
     * Spring automatically resolves Pageable from request parameters.
     *
     * @param pageable Pagination information (page number, page size, sort order).
     * @return ResponseEntity with a Page of CourseDto objects and HTTP status 200 (OK).
     */
    @GetMapping("")
    public ResponseEntity<Page<CourseDto>> getAllCourses(Pageable pageable) {
        Page<CourseDto> recommendedCoursesPage = courseService.getAllCourses(pageable);
        return new ResponseEntity<>(recommendedCoursesPage, HttpStatus.OK);
    }

    /**
     * Endpoint to discover all courses (using the recommendation service) with pagination.
     * Maps to GET /api/courses/recommendations?page=0&size=10&sort=name,asc
     * Spring automatically resolves Pageable from request parameters.
     *
     * @param pageable Pagination information (page number, page size, sort order).
     * @return ResponseEntity with a Page of CourseDto objects and HTTP status 200 (OK).
     */
    @GetMapping("/recommendations")
    public ResponseEntity<Page<CourseDto>> discoverAllCourses(Pageable pageable) {
        Page<CourseDto> recommendedCoursesPage = courseService.getRecommendedCourses(pageable);
        return new ResponseEntity<>(recommendedCoursesPage, HttpStatus.OK);
    }
}
