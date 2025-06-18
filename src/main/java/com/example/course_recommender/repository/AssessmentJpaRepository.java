package com.example.course_recommender.repository;

import com.example.course_recommender.model.Assessment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Spring Data JPA Repository for the Assessment entity.
 */
@Repository
public interface AssessmentJpaRepository extends JpaRepository<Assessment, UUID> {
}
