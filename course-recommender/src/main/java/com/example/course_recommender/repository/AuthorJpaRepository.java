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
    Optional<Author> findByEmail(String email);
}
