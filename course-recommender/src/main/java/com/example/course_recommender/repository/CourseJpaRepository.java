package com.example.course_recommender.repository;

import com.example.course_recommender.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Spring Data JPA Repository for the Course entity.
 */
@Repository
public interface CourseJpaRepository extends JpaRepository<Course, UUID> {
}
