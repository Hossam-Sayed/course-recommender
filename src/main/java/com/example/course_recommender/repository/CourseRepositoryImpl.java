package com.example.course_recommender.repository;

import com.example.course_recommender.model.Course;
import com.example.course_recommender.model.Author; // Import Author
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
 * Handles all database interactions for the Course entity, including many-to-many
 * relationship with Author.
 */
@Repository
public class CourseRepositoryImpl implements CourseRepository {

    private final JdbcTemplate jdbcTemplate;
    private final AuthorRepository authorRepository; // Inject AuthorRepository

    /**
     * Constructor for CourseRepositoryImpl.
     *
     * @param jdbcTemplate     The JdbcTemplate instance.
     * @param authorRepository The AuthorRepository instance, injected to fetch associated authors.
     */
    @Autowired
    public CourseRepositoryImpl(JdbcTemplate jdbcTemplate, AuthorRepository authorRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.authorRepository = authorRepository;
    }

    /**
     * Internal static RowMapper class to map a ResultSet row to a Course object.
     */
    private static final class CourseRowMapper implements RowMapper<Course> {
        @Override
        public Course mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new Course(
                    UUID.fromString(rs.getString("id")),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getInt("credit")
            );
            // Authors are not mapped here; they will be fetched in the service layer
            // or in a separate query to avoid N+1 problems in the repository level.
        }
    }

    /**
     * Saves a new Course to the database.
     *
     * @param course The Course object to save.
     * @return The Course object with its ID populated.
     */
    @Override
    public Course save(Course course) {
        String sql = "INSERT INTO course (name, description, credit) VALUES (?, ?, ?) RETURNING id";

        UUID generatedId = jdbcTemplate.queryForObject(
                sql,
                UUID.class,
                course.getName(),
                course.getDescription(),
                course.getCredit()
        );
        course.setId(generatedId);
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
            System.err.println("Error: Cannot update course with a null ID.");
            return Optional.empty();
        }
        String sql = "UPDATE course SET name = ?, description = ?, credit = ? WHERE id = ?";

        int rowsAffected = jdbcTemplate.update(
                sql,
                course.getName(),
                course.getDescription(),
                course.getCredit(),
                course.getId()
        );

        if (rowsAffected == 0) {
            System.out.println("No course found with ID: " + course.getId() + " to update.");
            return Optional.empty();
        } else {
            System.out.println("Course with ID: " + course.getId() + " updated successfully.");
            return Optional.of(course);
        }
    }

    /**
     * Finds a Course by its UUID.
     * Authors are NOT fetched at this repository level to keep the query simple.
     * The service layer will enrich the Course object with authors.
     *
     * @param id The UUID of the course to find.
     * @return An Optional containing the Course if found, otherwise Optional.empty().
     */
    @Override
    public Optional<Course> findById(UUID id) {
        String sql = "SELECT id, name, description, credit FROM course WHERE id = ?";
        try {
            Course course = jdbcTemplate.queryForObject(sql, new CourseRowMapper(), id);
            return Optional.ofNullable(course);
        } catch (EmptyResultDataAccessException e) {
            System.out.println("No course found with ID: " + id);
            return Optional.empty();
        } catch (Exception e) {
            System.err.println("Error retrieving course with ID " + id + ": " + e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Retrieves all Courses from the database.
     * Authors are NOT fetched at this repository level.
     *
     * @return A List of all Course objects.
     */
    @Override
    public List<Course> findAll() {
        String sql = "SELECT id, name, description, credit FROM course";
        return jdbcTemplate.query(sql, new CourseRowMapper());
    }

    /**
     * Deletes a Course by its UUID.
     * `ON DELETE CASCADE` in schema handles the `course_author` entries.
     *
     * @param id The UUID of the course to delete.
     * @return true if the course was successfully deleted, false otherwise.
     */
    @Override
    public boolean deleteById(UUID id) {
        String sql = "DELETE FROM course WHERE id = ?";
        int rowsAffected = jdbcTemplate.update(sql, id);
        if (rowsAffected > 0) {
            System.out.println("Course with ID: " + id + " deleted successfully.");
            return true;
        } else {
            System.out.println("No course found with ID: " + id + " to delete.");
            return false;
        }
    }

    /**
     * Links a course with an author in the `course_author` join table.
     *
     * @param courseId The UUID of the course.
     * @param authorId The UUID of the author.
     */
    @Override
    public void addCourseAuthor(UUID courseId, UUID authorId) {
        String sql = "INSERT INTO course_author (course_id, author_id) VALUES (?, ?) ON CONFLICT (course_id, author_id) DO NOTHING";
        jdbcTemplate.update(sql, courseId, authorId);
        System.out.println("Linked course " + courseId + " with author " + authorId);
    }

    /**
     * Removes a link between a course and an author from the `course_author` join table.
     *
     * @param courseId The UUID of the course.
     * @param authorId The UUID of the author.
     * @return true if the link was removed, false otherwise.
     */
    @Override
    public boolean removeCourseAuthor(UUID courseId, UUID authorId) {
        String sql = "DELETE FROM course_author WHERE course_id = ? AND author_id = ?";
        int rowsAffected = jdbcTemplate.update(sql, courseId, authorId);
        if (rowsAffected > 0) {
            System.out.println("Unlinked course " + courseId + " from author " + authorId);
            return true;
        }
        return false;
    }

    /**
     * Removes all author links for a given course from the `course_author` join table.
     * This is useful when updating the authors of a course: delete all existing, then add new ones.
     *
     * @param courseId The UUID of the course.
     * @return The number of links removed.
     */
    @Override
    public int removeAllAuthorsForCourse(UUID courseId) {
        String sql = "DELETE FROM course_author WHERE course_id = ?";
        int rowsAffected = jdbcTemplate.update(sql, courseId);
        System.out.println("Removed " + rowsAffected + " author links for course " + courseId);
        return rowsAffected;
    }
}
