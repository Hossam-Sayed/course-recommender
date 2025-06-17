package com.example.course_recommender.repository;

import com.example.course_recommender.model.Course;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Course operations.
 * Defines the contract for data access methods related to the Course entity.
 */
public interface CourseRepository {

    /**
     * Inserts a new course into the database.
     *
     * @param course The Course object to be added.
     * @return The added Course object with its generated ID.
     */
    Course save(Course course);

    /**
     * Updates an existing course in the database.
     *
     * @param course The Course object with updated details.
     * @return An Optional containing the updated Course if found, otherwise Optional.empty().
     */
    Optional<Course> update(Course course);

    /**
     * Retrieves a course by its ID.
     *
     * @param id The UUID of the course to retrieve.
     * @return An Optional containing the Course if found, otherwise Optional.empty().
     */
    Optional<Course> findById(UUID id);

    /**
     * Retrieves all courses from the database.
     *
     * @return A list of all Course objects.
     */
    List<Course> findAll();

    /**
     * Deletes a course by its ID.
     *
     * @param id The UUID of the course to delete.
     * @return true if the course was deleted, false otherwise.
     */
    boolean deleteById(UUID id);
}
