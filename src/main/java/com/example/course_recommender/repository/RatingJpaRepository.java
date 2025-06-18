package com.example.course_recommender.repository;

import com.example.course_recommender.model.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA Repository for the Rating entity.
 */
@Repository
public interface RatingJpaRepository extends JpaRepository<Rating, UUID> {
    // Find ratings for a specific course:
    List<Rating> findByCourseId(UUID courseId);
}
