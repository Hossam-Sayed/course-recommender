package com.example.course_recommender.repository;

import com.example.course_recommender.model.Assessment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA Repository for the Assessment entity.
 * Provides standard CRUD operations and custom query methods.
 */
@Repository
public interface AssessmentJpaRepository extends JpaRepository<Assessment, UUID> {

    /**
     * Custom query method to find an Assessment by its associated Course ID.
     *
     * @param courseId The UUID of the course.
     * @return An Optional containing the Assessment if found, otherwise Optional.empty().
     */
    Optional<Assessment> findByCourseId(UUID courseId);
}
