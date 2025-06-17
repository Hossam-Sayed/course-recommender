package com.example.course_recommender.repository;

import com.example.course_recommender.model.Author;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Author operations.
 * Defines the contract for data access methods related to the Author entity.
 */
public interface AuthorRepository {

    /**
     * Inserts a new author into the database.
     *
     * @param author The Author object to be added.
     * @return The added Author object with its generated ID.
     */
    Author save(Author author);

    /**
     * Updates an existing author in the database.
     *
     * @param author The Author object with updated details.
     * @return An Optional containing the updated Author if found, otherwise Optional.empty().
     */
    Optional<Author> update(Author author);

    /**
     * Retrieves an author by its ID.
     *
     * @param id The UUID of the author to retrieve.
     * @return An Optional containing the Author if found, otherwise Optional.empty().
     */
    Optional<Author> findById(UUID id);

    /**
     * Retrieves all authors from the database.
     *
     * @return A list of all Author objects.
     */
    List<Author> findAll();

    /**
     * Deletes an author by its ID.
     *
     * @param id The UUID of the author to delete.
     * @return true if the author was deleted, false otherwise.
     */
    boolean deleteById(UUID id);

    /**
     * Finds authors associated with a specific course.
     *
     * @param courseId The UUID of the course.
     * @return A list of Author objects associated with the given course.
     */
    List<Author> findAuthorsByCourseId(UUID courseId);
}
