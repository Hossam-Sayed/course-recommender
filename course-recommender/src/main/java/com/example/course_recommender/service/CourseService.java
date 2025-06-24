package com.example.course_recommender.service;

import com.example.course_recommender_bean.dto.CourseDto;
import com.example.course_recommender.mapper.CourseMapper;
import com.example.course_recommender.model.Author;
import com.example.course_recommender.model.Course;
import com.example.course_recommender.repository.AuthorJpaRepository;
import com.example.course_recommender.repository.CourseJpaRepository;
import com.example.course_recommender_bean.service.CourseRecommender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service layer for managing Course-related business logic.
 */
@Service
public class CourseService {

    private final CourseJpaRepository courseJpaRepository;
    private final AuthorJpaRepository authorJpaRepository;
    private final CourseRecommender courseRecommender;
    private final CourseMapper courseMapper;

    /**
     * Constructor for CourseService. Spring injects JPA repositories and mappers.
     *
     * @param courseJpaRepository The CourseJpaRepository instance.
     * @param authorJpaRepository The AuthorJpaRepository instance.
     * @param courseRecommender   The CourseRecommender instance
     * @param courseMapper        The CourseMapper instance.
     */
    @Autowired
    public CourseService(CourseJpaRepository courseJpaRepository, AuthorJpaRepository authorJpaRepository, CourseRecommender courseRecommender, CourseMapper courseMapper) {
        this.courseJpaRepository = courseJpaRepository;
        this.authorJpaRepository = authorJpaRepository;
        this.courseRecommender = courseRecommender;
        this.courseMapper = courseMapper;
    }

    /**
     * Adds a new course to the system and links it to specified authors.
     * This operation is transactional to ensure both course creation and author linking
     * succeed or fail together.
     *
     * @param courseDto The CourseDto object to be added. Its ID will be generated.
     * @param authorIds A list of UUIDs of authors to link to this course.
     * @return The saved CourseDto object including its generated ID and associated authors.
     * @throws IllegalArgumentException if no author IDs are provided or if an author ID is invalid.
     */
    @Transactional
    public CourseDto addCourse(CourseDto courseDto, List<UUID> authorIds) {
        if (authorIds == null || authorIds.isEmpty()) {
            throw new IllegalArgumentException("A course must be linked to at least one author.");
        }

        Course course = courseMapper.toEntity(courseDto);

        // Fetch Author entities and set them on the Course entity
        List<Author> authors = authorIds.stream()
                .map(authorJpaRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        if (authors.isEmpty()) {
            throw new IllegalArgumentException("None of the provided author IDs were valid.");
        }

        course.setAuthors(authors);

        // Save the Course entity. JPA will automatically persist the relationship in the join table.
        System.out.println("Attempting to add course: " + courseDto.getName());
        Course savedCourse = courseJpaRepository.save(course);

        // Convert the saved Entity back to DTO
        return courseMapper.toDto(savedCourse);
    }

    /**
     * Updates an existing course and its author associations.
     * This operation is transactional.
     *
     * @param id           The UUID of the course to update.
     * @param courseDto    The CourseDto object with updated details.
     * @param newAuthorIds A list of UUIDs of authors to link to this course. Existing links
     *                     will be removed and new ones established if newAuthorIds is not null/empty.
     * @return An Optional containing the updated CourseDto (with new authors) if found, otherwise Optional.empty().
     */
    @Transactional
    public Optional<CourseDto> updateCourse(UUID id, CourseDto courseDto, List<UUID> newAuthorIds) {
        System.out.println("Attempting to update course with ID: " + id);
        Optional<Course> existingCourseOpt = courseJpaRepository.findById(id);

        if (existingCourseOpt.isPresent()) {
            Course existingCourse = existingCourseOpt.get();

            // MapStruct updates basic fields of the existing entity from the DTO
            courseMapper.updateEntityFromDto(courseDto, existingCourse);

            // Update author associations if newAuthorIds are provided
            if (newAuthorIds != null && !newAuthorIds.isEmpty()) {
                // Clear existing authors from the collection
                existingCourse.getAuthors().clear(); // JPA will handle removal from join table

                // Fetch new Author entities and add them to the collection
                List<Author> authorsToAdd = newAuthorIds.stream()
                        .map(authorJpaRepository::findById)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList());

                if (authorsToAdd.isEmpty()) {
                    throw new IllegalArgumentException("None of the provided new author IDs were valid.");
                }

                existingCourse.getAuthors().addAll(authorsToAdd); // JPA will handle adding to join table
            }

            // Save the updated Course entity. JPA detects changes to entity and its collections.
            Course updatedCourse = courseJpaRepository.save(existingCourse);

            System.out.println("Course and authors updated successfully: " + updatedCourse.getName());
            return Optional.of(courseMapper.toDto(updatedCourse)); // Convert updated Entity to DTO
        }
        System.out.println("No course found with ID: " + id + " to update.");
        return Optional.empty();
    }

    /**
     * Retrieves a course by its unique identifier, including its associated authors.
     *
     * @param id The UUID of the course to retrieve.
     * @return An Optional containing the CourseDto if found, otherwise Optional.empty().
     */
    @Transactional(readOnly = true)
    public Optional<CourseDto> viewCourse(UUID id) {
        System.out.println("Attempting to view course with ID: " + id);
        return courseJpaRepository.findById(id)
                .map(courseMapper::toDto);
    }

    /**
     * Retrieves all available courses with pagination support.
     *
     * @param pageable Pagination information (page number, page size, sort order).
     * @return A Page of CourseDto objects.
     */
    @Transactional(readOnly = true)
    public Page<CourseDto> getAllCourses(Pageable pageable) {
        System.out.println("Attempting to retrieve all courses with pagination: Page " + pageable.getPageNumber() + ", Size " + pageable.getPageSize());
        Page<Course> coursePage = courseJpaRepository.findAll(pageable);
        return coursePage.map(courseMapper::toDto); // Map the Page<Course> to Page<CourseDto>
    }

    /**
     * Retrieves recommended courses
     *
     * @param pageable Pagination information (page number, page size, sort order).
     * @return A Page of recommended CourseDto objects.
     */
    public Page<CourseDto> getRecommendedCourses(Pageable pageable) {
        System.out.println("Attempting to retrieve all courses with pagination: Page " + pageable.getPageNumber() + ", Size " + pageable.getPageSize());
        return courseRecommender.recommendedCourses(pageable);
    }

    /**
     * Deletes a course by its unique identifier.
     * JPA's cascade and orphan removal settings in entities will handle related data.
     *
     * @param id The UUID of the course to delete.
     * @return true if the course was successfully deleted, false otherwise.
     */
    @Transactional
    public boolean deleteCourse(UUID id) {
        System.out.println("Attempting to delete course with ID: " + id);
        if (courseJpaRepository.existsById(id)) {
            courseJpaRepository.deleteById(id);
            System.out.println("Course with ID: " + id + " deleted successfully.");
            return true;
        } else {
            System.out.println("No course found with ID: " + id + " to delete.");
            return false;
        }
    }
}
