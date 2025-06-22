package com.example.course_recommender.controller;

import com.example.course_recommender.dto.AssessmentDto;
import com.example.course_recommender.service.AssessmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

/**
 * REST Controller for managing Assessment resources.
 * Exposes endpoints for CRUD operations and retrieving assessments by course.
 */
@RestController
@RequestMapping("/api/assessments")
public class AssessmentController {

    private final AssessmentService assessmentService;

    /**
     * Constructor for AssessmentController. Spring automatically injects the AssessmentService bean.
     *
     * @param assessmentService The AssessmentService instance.
     */
    @Autowired
    public AssessmentController(AssessmentService assessmentService) {
        this.assessmentService = assessmentService;
    }

    /**
     * Endpoint to retrieve a specific assessment by its ID.
     * Maps to GET /api/assessments/{id}
     *
     * @param id The UUID of the assessment to retrieve, passed as a path variable.
     * @return ResponseEntity with the AssessmentDto object and HTTP status 200 (OK) if found,
     * or HTTP status 404 (Not Found) if no assessment matches the ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<AssessmentDto> viewAssessment(@PathVariable UUID id) {
        Optional<AssessmentDto> assessmentDto = assessmentService.viewAssessment(id);
        return assessmentDto.map(dto -> new ResponseEntity<>(dto, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Endpoint to retrieve all assessments with pagination.
     * Maps to GET /api/assessments?page=0&size=10&sort=id,asc
     *
     * @param pageable Pagination information (page number, page size, sort order).
     * @return ResponseEntity with a Page of AssessmentDto objects and HTTP status 200 (OK).
     */
    @GetMapping
    public ResponseEntity<Page<AssessmentDto>> getAllAssessments(Pageable pageable) {
        Page<AssessmentDto> assessments = assessmentService.getAllAssessments(pageable);
        return new ResponseEntity<>(assessments, HttpStatus.OK);
    }

    /**
     * Endpoint to add a new assessment.
     * Maps to POST /api/assessments
     * Expects a JSON request body containing assessment details, including the associated courseId.
     *
     * @param assessmentDto The AssessmentDto object to be added.
     * @return ResponseEntity with the created AssessmentDto object and HTTP status 201 (Created),
     * or HTTP status 400 (Bad Request) if input is invalid.
     */
    @PostMapping
    public ResponseEntity<AssessmentDto> addAssessment(@RequestBody AssessmentDto assessmentDto) {
        try {
            AssessmentDto savedAssessment = assessmentService.addAssessment(assessmentDto);
            return new ResponseEntity<>(savedAssessment, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            System.err.println("Error adding assessment: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            System.err.println("An unexpected error occurred while adding assessment: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Endpoint to update an existing assessment.
     * Maps to PUT /api/assessments/{id}
     * Expects a JSON request body with updated assessment details.
     *
     * @param id            The UUID of the assessment to update, passed as a path variable.
     * @param assessmentDto The AssessmentDto object with updated details.
     * @return ResponseEntity with the updated AssessmentDto object and HTTP status 200 (OK) if found and updated,
     * or HTTP status 404 (Not Found) if the assessment does not exist.
     */
    @PutMapping("/{id}")
    public ResponseEntity<AssessmentDto> updateAssessment(@PathVariable UUID id, @RequestBody AssessmentDto assessmentDto) {
        try {
            Optional<AssessmentDto> updatedAssessment = assessmentService.updateAssessment(id, assessmentDto);
            return updatedAssessment.map(dto -> new ResponseEntity<>(dto, HttpStatus.OK))
                    .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
        } catch (IllegalArgumentException e) {
            System.err.println("Error updating assessment: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            System.err.println("An unexpected error occurred while updating assessment: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Endpoint to delete a specific assessment by its ID.
     * Maps to DELETE /api/assessments/{id}
     *
     * @param id The UUID of the assessment to delete, passed as a path variable.
     * @return ResponseEntity with HTTP status 204 (No Content) if deleted successfully,
     * or HTTP status 404 (Not Found) if the assessment does not exist.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAssessment(@PathVariable UUID id) {
        boolean deleted = assessmentService.deleteAssessment(id);
        return deleted ? new ResponseEntity<>(HttpStatus.NO_CONTENT)
                : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    /**
     * Endpoint to retrieve an assessment for a specific course.
     * Maps to GET /api/assessments/course/{courseId}
     *
     * @param courseId The UUID of the course whose assessment is to be retrieved.
     * @return ResponseEntity with the AssessmentDto for the given course and HTTP status 200 (OK),
     * or HTTP status 404 (Not Found) if no assessment is found for the course.
     */
    @GetMapping("/course/{courseId}")
    public ResponseEntity<AssessmentDto> getAssessmentByCourseId(@PathVariable UUID courseId) {
        Optional<AssessmentDto> assessmentDto = assessmentService.getAssessmentByCourseId(courseId);
        return assessmentDto.map(dto -> new ResponseEntity<>(dto, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}
