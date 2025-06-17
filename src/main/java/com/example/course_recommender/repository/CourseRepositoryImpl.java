package com.example.course_recommender.repository;

import com.example.course_recommender.model.Course;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JDBC Template implementation of CourseRepository.
 * Handles all database interactions for the Course entity.
 */
@Repository // Marks this class as a Spring repository component for component scanning
public class CourseRepositoryImpl implements CourseRepository {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Constructor for CourseRepositoryImpl. Spring will automatically
     * inject the JdbcTemplate instance configured in application.properties.
     *
     * @param jdbcTemplate The JdbcTemplate instance provided by Spring.
     */
    @Autowired
    public CourseRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Internal static RowMapper class to map a ResultSet row to a Course object.
     * This ensures type-safe conversion from database columns to Java object fields.
     */
    private static final class CourseRowMapper implements RowMapper<Course> {
        @Override
        public Course mapRow(ResultSet rs, int rowNum) throws SQLException {
            // Retrieve data from ResultSet and create a new Course object
            return new Course(
                    UUID.fromString(rs.getString("id")), // Convert String UUID from DB to Java UUID
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getInt("credit")
            );
        }
    }

    /**
     * Saves a new Course to the database.
     * Uses PostgreSQL's RETURNING clause to get the generated UUID back.
     *
     * @param course The Course object to save.
     * @return The Course object with its ID populated.
     */
    @Override
    public Course save(Course course) {
        // SQL query to insert a new course, letting the database generate the ID
        // RETURNING id is essential for getting the auto-generated UUID back
        String sql = "INSERT INTO courses (name, description, credit) VALUES (?, ?, ?) RETURNING id";

        // Execute the insert and retrieve the generated UUID
        UUID generatedId = jdbcTemplate.queryForObject(
                sql,
                UUID.class, // Expected return type is UUID
                course.getName(),
                course.getDescription(),
                course.getCredit()
        );
        course.setId(generatedId); // Set the generated ID back to the Course object
        return course;
    }

    /**
     * Updates an existing Course in the database based on its ID.
     *
     * @param course The Course object containing updated data and the ID.
     * @return An Optional containing the updated Course if found, otherwise Optional.empty().
     */
    @Override
    public Optional<Course> update(Course course) {
        if (course.getId() == null) {
            // Cannot update if the course ID is null
            System.err.println("Error: Cannot update course with a null ID.");
            return Optional.empty();
        }
        // SQL query to update course details by ID
        String sql = "UPDATE courses SET name = ?, description = ?, credit = ? WHERE id = ?";

        // Execute the update query. `update` returns the number of rows affected.
        int rowsAffected = jdbcTemplate.update(
                sql,
                course.getName(),
                course.getDescription(),
                course.getCredit(),
                course.getId()
        );

        // If no rows were affected, it means no course with that ID was found
        if (rowsAffected == 0) {
            System.out.println("No course found with ID: " + course.getId() + " to update.");
            return Optional.empty();
        } else {
            System.out.println("Course with ID: " + course.getId() + " updated successfully.");
            return Optional.of(course); // Return the successfully updated course object
        }
    }

    /**
     * Finds a Course by its UUID.
     *
     * @param id The UUID of the course to find.
     * @return An Optional containing the Course if found, otherwise Optional.empty().
     */
    @Override
    public Optional<Course> findById(UUID id) {
        // SQL query to select a course by its ID
        String sql = "SELECT id, name, description, credit FROM courses WHERE id = ?";
        try {
            // queryForObject is used when a single row is expected.
            // If no row is found, EmptyResultDataAccessException is thrown.
            Course course = jdbcTemplate.queryForObject(sql, new CourseRowMapper(), id);
            return Optional.ofNullable(course); // Wrap the result in an Optional
        } catch (EmptyResultDataAccessException e) {
            System.out.println("No course found with ID: " + id);
            return Optional.empty(); // Return empty Optional if not found
        } catch (Exception e) {
            System.err.println("Error retrieving course with ID " + id + ": " + e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Retrieves all Courses from the database.
     *
     * @return A List of all Course objects.
     */
    @Override
    public List<Course> findAll() {
        // SQL query to select all courses
        String sql = "SELECT id, name, description, credit FROM courses";
        // query is used when multiple rows are expected.
        return jdbcTemplate.query(sql, new CourseRowMapper());
    }

    /**
     * Deletes a Course by its UUID.
     *
     * @param id The UUID of the course to delete.
     * @return true if the course was successfully deleted, false otherwise.
     */
    @Override
    public boolean deleteById(UUID id) {
        // SQL query to delete a course by its ID
        String sql = "DELETE FROM courses WHERE id = ?";
        // update returns the number of rows affected.
        int rowsAffected = jdbcTemplate.update(sql, id);
        if (rowsAffected > 0) {
            System.out.println("Course with ID: " + id + " deleted successfully.");
            return true;
        } else {
            System.out.println("No course found with ID: " + id + " to delete.");
            return false;
        }
    }
}
