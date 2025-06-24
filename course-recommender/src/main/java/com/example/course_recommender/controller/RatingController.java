package com.example.course_recommender.controller;

import com.example.course_recommender.dto.RatingDto;
import com.example.course_recommender.service.RatingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

/**
 * REST Controller for managing Rating resources.
 * Exposes endpoints for CRUD operations and retrieving ratings by course.
 */
@RestController
@RequestMapping("/api/ratings")
public class RatingController {

    private final RatingService ratingService;

    /**
     * Constructor for RatingController. Spring automatically injects the RatingService bean.
     *
     * @param ratingService The RatingService instance.
     */
    @Autowired
    public RatingController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    /**
     * Endpoint to retrieve a specific rating by its ID.
     * Maps to GET /api/ratings/{id}
     *
     * @param id The UUID of the rating to retrieve, passed as a path variable.
     * @return ResponseEntity with the RatingDto object and HTTP status 200 (OK) if found,
     * or HTTP status 404 (Not Found) if no rating matches the ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<RatingDto> viewRating(@PathVariable UUID id) {
        Optional<RatingDto> ratingDto = ratingService.viewRating(id);
        return ratingDto.map(dto -> new ResponseEntity<>(dto, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Endpoint to retrieve all ratings with pagination.
     * Maps to GET /api/ratings?page=0&size=10&sort=number,desc
     *
     * @param pageable Pagination information (page number, page size, sort order).
     * @return ResponseEntity with a Page of RatingDto objects and HTTP status 200 (OK).
     */
    @GetMapping
    public ResponseEntity<Page<RatingDto>> getAllRatings(Pageable pageable) {
        Page<RatingDto> ratings = ratingService.getAllRatings(pageable);
        return new ResponseEntity<>(ratings, HttpStatus.OK);
    }

    /**
     * Endpoint to add a new rating.
     * Maps to POST /api/ratings
     * Expects a JSON request body containing rating details, including the associated courseId.
     *
     * @param ratingDto The RatingDto object to be added.
     * @return ResponseEntity with the created RatingDto object and HTTP status 201 (Created),
     * or HTTP status 400 (Bad Request) if input is invalid.
     */
    @PostMapping
    public ResponseEntity<RatingDto> addRating(@RequestBody RatingDto ratingDto) {
        try {
            RatingDto savedRating = ratingService.addRating(ratingDto);
            return new ResponseEntity<>(savedRating, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            System.err.println("Error adding rating: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            System.err.println("An unexpected error occurred while adding rating: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Endpoint to update an existing rating.
     * Maps to PUT /api/ratings/{id}
     * Expects a JSON request body with updated rating details.
     *
     * @param id        The UUID of the rating to update, passed as a path variable.
     * @param ratingDto The RatingDto object with updated details.
     * @return ResponseEntity with the updated RatingDto object and HTTP status 200 (OK) if found and updated,
     * or HTTP status 404 (Not Found) if the rating does not exist.
     */
    @PutMapping("/{id}")
    public ResponseEntity<RatingDto> updateRating(@PathVariable UUID id, @RequestBody RatingDto ratingDto) {
        Optional<RatingDto> updatedRating = ratingService.updateRating(id, ratingDto);
        return updatedRating.map(dto -> new ResponseEntity<>(dto, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Endpoint to delete a specific rating by its ID.
     * Maps to DELETE /api/ratings/{id}
     *
     * @param id The UUID of the rating to delete, passed as a path variable.
     * @return ResponseEntity with HTTP status 204 (No Content) if deleted successfully,
     * or HTTP status 404 (Not Found) if the rating does not exist.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRating(@PathVariable UUID id) {
        boolean deleted = ratingService.deleteRating(id);
        return deleted ? new ResponseEntity<>(HttpStatus.NO_CONTENT)
                : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    /**
     * Endpoint to retrieve all ratings for a specific course with pagination.
     * Maps to GET /api/ratings/course/{courseId}?page=0&size=5
     *
     * @param courseId The UUID of the course.
     * @param pageable Pagination information.
     * @return ResponseEntity with a Page of RatingDto objects for the given course and HTTP status 200 (OK).
     */
    @GetMapping("/course/{courseId}")
    public ResponseEntity<Page<RatingDto>> getRatingsByCourseId(
            @PathVariable UUID courseId,
            Pageable pageable) {
        Page<RatingDto> ratings = ratingService.getRatingsByCourseId(courseId, pageable);
        return new ResponseEntity<>(ratings, HttpStatus.OK);
    }

    /**
     * Endpoint to get the simple mean rating for a specific course.
     * Maps to GET /api/ratings/course/{courseId}/mean
     *
     * @param courseId The UUID of the course.
     * @return ResponseEntity with the mean rating (as a Double) and HTTP status 200 (OK),
     * or HTTP status 404 (Not Found) if the course does not exist.
     */
    @GetMapping("/course/{courseId}/mean")
    public ResponseEntity<Double> getSimpleMeanRatingForCourse(@PathVariable UUID courseId) {
        // TODO: check if the course itself exists first here,
        double meanRating = ratingService.getSimpleMeanRatingForCourse(courseId);
        return new ResponseEntity<>(meanRating, HttpStatus.OK);
    }
}
