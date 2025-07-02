package com.example.course_recommender.service;

import com.example.course_recommender.dto.RatingDto;
import com.example.course_recommender.mapper.RatingMapper;
import com.example.course_recommender.model.Course;
import com.example.course_recommender.model.Rating;
import com.example.course_recommender.repository.CourseJpaRepository;
import com.example.course_recommender.repository.RatingJpaRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service layer for managing Rating-related business logic.
 * Uses Spring Data JPA repositories, MapStruct for DTO mapping.
 */
@Slf4j
@Service
public class RatingService {

    private final RatingJpaRepository ratingJpaRepository;
    private final CourseJpaRepository courseJpaRepository; // To find the associated Course
    private final RatingMapper ratingMapper;

    /**
     * Constructor for RatingService. Spring injects JPA repositories and mapper.
     *
     * @param ratingJpaRepository The RatingJpaRepository instance.
     * @param courseJpaRepository The CourseJpaRepository instance.
     * @param ratingMapper        The RatingMapper instance.
     */
    @Autowired
    public RatingService(RatingJpaRepository ratingJpaRepository, CourseJpaRepository courseJpaRepository, RatingMapper ratingMapper) {
        this.ratingJpaRepository = ratingJpaRepository;
        this.courseJpaRepository = courseJpaRepository;
        this.ratingMapper = ratingMapper;
    }

    /**
     * Adds a new rating to the system and links it to a specific course.
     *
     * @param ratingDto The RatingDto object to be added, must contain courseId.
     * @return The added RatingDto object with its generated ID.
     * @throws IllegalArgumentException if courseId is null or the associated course does not exist.
     */
    @Transactional
    public RatingDto addRating(RatingDto ratingDto) {
        if (ratingDto.getCourseId() == null) {
            throw new IllegalArgumentException("Rating must be associated with a course.");
        }

        // 1. Find the Course entity by ID
        Optional<Course> courseOpt = courseJpaRepository.findById(ratingDto.getCourseId());
        if (courseOpt.isEmpty()) {
            throw new IllegalArgumentException("Course with ID " + ratingDto.getCourseId() + " not found for rating.");
        }
        Course course = courseOpt.get();

        Rating rating = ratingMapper.toEntity(ratingDto);
        rating.setCourse(course);

        log.info("Attempting to add rating for course: {}", course.getName());
        Rating savedRating = ratingJpaRepository.save(rating);

        return ratingMapper.toDto(savedRating);
    }

    /**
     * Updates an existing rating.
     * It is not allowed to change the course association of a rating via update.
     *
     * @param id        The UUID of the rating to update.
     * @param ratingDto The RatingDto object with updated details.
     * @return An Optional containing the updated RatingDto if found, otherwise Optional.empty().
     */
    @Transactional
    public Optional<RatingDto> updateRating(UUID id, RatingDto ratingDto) {
        log.info("Attempting to update rating with ID: {}", id);
        Optional<Rating> existingRatingOpt = ratingJpaRepository.findById(id);

        if (existingRatingOpt.isPresent()) {
            Rating existingRating = existingRatingOpt.get();

            ratingMapper.updateEntityFromDto(ratingDto, existingRating);

            Rating updatedRating = ratingJpaRepository.save(existingRating);
            return Optional.of(ratingMapper.toDto(updatedRating));
        }
        log.info("No rating found with ID: {} to update.", id);
        return Optional.empty();
    }

    /**
     * Retrieves a rating by its unique identifier.
     *
     * @param id The UUID of the rating to retrieve.
     * @return An Optional containing the RatingDto if found, otherwise Optional.empty().
     */
    @Transactional(readOnly = true)
    public Optional<RatingDto> viewRating(UUID id) {
        log.info("Attempting to view rating with ID: {}", id);
        return ratingJpaRepository.findById(id)
                .map(ratingMapper::toDto);
    }

    /**
     * Retrieves all available ratings with pagination support.
     *
     * @param pageable Pagination information (page number, page size, sort order).
     * @return A Page of RatingDto objects.
     */
    @Transactional(readOnly = true)
    public Page<RatingDto> getAllRatings(Pageable pageable) {
        log.info("Attempting to retrieve all ratings with pagination: Page {}, Size {}", pageable.getPageNumber(), pageable.getPageSize());
        Page<Rating> ratingPage = ratingJpaRepository.findAll(pageable);
        return ratingPage.map(ratingMapper::toDto);
    }

    /**
     * Deletes a rating by its unique identifier.
     *
     * @param id The UUID of the rating to delete.
     * @return true if the rating was successfully deleted, false otherwise.
     */
    @Transactional
    public boolean deleteRating(UUID id) {
        log.info("Attempting to delete rating with ID: {}", id);
        if (ratingJpaRepository.existsById(id)) {
            ratingJpaRepository.deleteById(id);
            log.info("Rating with ID: {} deleted successfully.", id);
            return true;
        } else {
            log.info("No rating found with ID: {} to delete.", id);
            return false;
        }
    }

    /**
     * Retrieves ratings for a specific course with pagination.
     *
     * @param courseId The UUID of the course whose ratings are to be retrieved.
     * @param pageable Pagination information.
     * @return A Page of RatingDto objects for the given course.
     */
    @Transactional(readOnly = true)
    public Page<RatingDto> getRatingsByCourseId(UUID courseId, Pageable pageable) {
        log.info("Attempting to get ratings for course {} with pagination.", courseId);
        Page<Rating> ratingPage = ratingJpaRepository.findByCourseId(courseId, pageable);
        return ratingPage.map(ratingMapper::toDto);
    }

    /**
     * Calculates the simple mean rating for a given course.
     *
     * @param courseId The UUID of the course.
     * @return The simple mean rating, or 0.0 if no ratings exist.
     */
    @Transactional(readOnly = true)
    public double getSimpleMeanRatingForCourse(UUID courseId) {
        log.info("Calculating simple mean rating for course: {}", courseId);
        List<Rating> ratings = ratingJpaRepository.findByCourseId(courseId);
        if (ratings.isEmpty()) {
            return 0.0;
        }
        double sum = ratings.stream().mapToInt(Rating::getNumber).sum();
        return sum / ratings.size();
    }
}
