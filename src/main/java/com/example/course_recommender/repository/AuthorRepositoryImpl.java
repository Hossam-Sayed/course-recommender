package com.example.course_recommender.repository;

import com.example.course_recommender.model.Author;
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
 * JDBC Template implementation of AuthorRepository.
 * Handles all database interactions for the Author entity.
 */
@Repository // Marks this class as a Spring repository component
public class AuthorRepositoryImpl implements AuthorRepository {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Constructor for AuthorRepositoryImpl. Spring injects JdbcTemplate.
     *
     * @param jdbcTemplate The JdbcTemplate instance.
     */
    @Autowired
    public AuthorRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Internal static RowMapper class to map a ResultSet row to an Author object.
     */
    private static final class AuthorRowMapper implements RowMapper<Author> {
        @Override
        public Author mapRow(ResultSet rs, int rowNum) throws SQLException {
            // Retrieve data from ResultSet and create a new Author object
            return new Author(
                    UUID.fromString(rs.getString("id")), // Convert String UUID from DB to Java UUID
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getDate("birthdate") != null ? rs.getDate("birthdate").toLocalDate() : null // Convert SQL Date to LocalDate
            );
        }
    }

    /**
     * Saves a new Author to the database.
     * Uses PostgreSQL's RETURNING clause to get the generated UUID back.
     *
     * @param author The Author object to save.
     * @return The Author object with its ID populated.
     */
    @Override
    public Author save(Author author) {
        // SQL query to insert a new author, letting the database generate the ID
        String sql = "INSERT INTO author (name, email, birthdate) VALUES (?, ?, ?) RETURNING id";

        // Execute the insert and retrieve the generated UUID
        UUID generatedId = jdbcTemplate.queryForObject(
                sql,
                UUID.class,
                author.getName(),
                author.getEmail(),
                author.getBirthdate() // LocalDate is directly supported by JdbcTemplate
        );
        author.setId(generatedId);
        return author;
    }

    /**
     * Updates an existing Author in the database based on its ID.
     *
     * @param author The Author object containing updated data and the ID.
     * @return An Optional containing the updated Author if found, otherwise Optional.empty().
     */
    @Override
    public Optional<Author> update(Author author) {
        if (author.getId() == null) {
            System.err.println("Error: Cannot update author with a null ID.");
            return Optional.empty();
        }
        // SQL query to update author details by ID
        String sql = "UPDATE author SET name = ?, email = ?, birthdate = ? WHERE id = ?";

        int rowsAffected = jdbcTemplate.update(
                sql,
                author.getName(),
                author.getEmail(),
                author.getBirthdate(),
                author.getId()
        );

        if (rowsAffected == 0) {
            System.out.println("No author found with ID: " + author.getId() + " to update.");
            return Optional.empty();
        } else {
            System.out.println("Author with ID: " + author.getId() + " updated successfully.");
            return Optional.of(author);
        }
    }

    /**
     * Finds an Author by its UUID.
     *
     * @param id The UUID of the author to find.
     * @return An Optional containing the Author if found, otherwise Optional.empty().
     */
    @Override
    public Optional<Author> findById(UUID id) {
        // SQL query to select an author by its ID
        String sql = "SELECT id, name, email, birthdate FROM author WHERE id = ?";
        try {
            Author author = jdbcTemplate.queryForObject(sql, new AuthorRowMapper(), id);
            return Optional.ofNullable(author);
        } catch (EmptyResultDataAccessException e) {
            System.out.println("No author found with ID: " + id);
            return Optional.empty();
        } catch (Exception e) {
            System.err.println("Error retrieving author with ID " + id + ": " + e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Retrieves all Authors from the database.
     *
     * @return A List of all Author objects.
     */
    @Override
    public List<Author> findAll() {
        // SQL query to select all authors
        String sql = "SELECT id, name, email, birthdate FROM author";
        return jdbcTemplate.query(sql, new AuthorRowMapper());
    }

    /**
     * Deletes an Author by its UUID.
     *
     * @param id The UUID of the author to delete.
     * @return true if the author was successfully deleted, false otherwise.
     */
    @Override
    public boolean deleteById(UUID id) {
        // SQL query to delete an author by its ID
        String sql = "DELETE FROM author WHERE id = ?";
        int rowsAffected = jdbcTemplate.update(sql, id);
        if (rowsAffected > 0) {
            System.out.println("Author with ID: " + id + " deleted successfully.");
            return true;
        } else {
            System.out.println("No author found with ID: " + id + " to delete.");
            return false;
        }
    }

    /**
     * Finds all authors associated with a given course ID.
     * This uses a JOIN with the `course_author` (join) table.
     *
     * @param courseId The UUID of the course.
     * @return A list of Author objects.
     */
    @Override
    public List<Author> findAuthorsByCourseId(UUID courseId) {
        String sql = "SELECT a.id, a.name, a.email, a.birthdate " +
                "FROM author a " +
                "JOIN course_author ca ON a.id = ca.author_id " + // Using `course_author`
                "WHERE ca.course_id = ?";
        System.out.println("Fetching authors for course ID: " + courseId);
        return jdbcTemplate.query(sql, new AuthorRowMapper(), courseId);
    }
}
