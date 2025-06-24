package com.example.course_recommender.repository;

import com.example.course_recommender.model.Rating;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA Repository for the Rating entity.
 * Provides standard CRUD operations and custom query methods.
 */
@Repository
public interface RatingJpaRepository extends JpaRepository<Rating, UUID> {

    /**
     * Custom query method to find ratings associated with a specific course ID, with pagination.
     * Spring Data JPA automatically generates the implementation based on method name.
     *
     * @param courseId The UUID of the course.
     * @param pageable Pagination information.
     * @return A Page of Rating entities.
     */
    Page<Rating> findByCourseId(UUID courseId, Pageable pageable);

    /**
     * Custom query method to find all ratings for a specific course ID.
     * Used for calculating the simple mean rating.
     *
     * @param courseId The UUID of the course.
     * @return A list of Rating entities.
     */
    List<Rating> findByCourseId(UUID courseId);
}
