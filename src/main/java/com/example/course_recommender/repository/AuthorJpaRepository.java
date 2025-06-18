package com.example.course_recommender.repository;

import com.example.course_recommender.model.Author;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA Repository for the Author entity.
 */
@Repository
public interface AuthorJpaRepository extends JpaRepository<Author, UUID> {
    // Custom query method to find an Author by email.
    // Spring Data JPA automatically generates the implementation based on method name.
    Optional<Author> findByEmail(String email);
}
