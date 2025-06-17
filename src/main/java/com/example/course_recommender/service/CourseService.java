package com.example.course_recommender.service;

import com.example.course_recommender.model.Author;
import com.example.course_recommender.model.Course;
import com.example.course_recommender.repository.CourseRepository;
import com.example.course_recommender.repository.AuthorRepository; // Import AuthorRepository
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Import for transactional support

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service layer for managing Course-related business logic, including
 * handling the many-to-many relationship with Author.
 */
@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final AuthorRepository authorRepository; // Inject AuthorRepository

    /**
     * Constructor for CourseService.
     *
     * @param courseRepository The CourseRepository instance.
     * @param authorRepository The AuthorRepository instance.
     */
    @Autowired
    public CourseService(CourseRepository courseRepository, AuthorRepository authorRepository) {
        this.courseRepository = courseRepository;
        this.authorRepository = authorRepository;
    }

    /**
     * Adds a new course to the system and links it to specified authors.
     * This operation is transactional to ensure both course creation and author linking
     * succeed or fail together.
     *
     * @param course    The Course object to be added. Its ID will be generated.
     * @param authorIds A list of UUIDs of authors to link to this course.
     * @return The saved Course object including its generated ID and associated authors.
     * @throws IllegalArgumentException if no author IDs are provided or if an author ID is invalid.
     */
    @Transactional // Ensures atomicity: all operations within this method succeed or rollback
    public Course addCourse(Course course, List<UUID> authorIds) {
        if (authorIds == null || authorIds.isEmpty()) {
            throw new IllegalArgumentException("A course must be linked to at least one author.");
        }

        // 1. Save the Course itself
        System.out.println("Attempting to add course: " + course.getName());
        Course savedCourse = courseRepository.save(course);

        // 2. Link authors to the newly created course
        for (UUID authorId : authorIds) {
            // TODO: add a check here to ensure the author actually exists
            courseRepository.addCourseAuthor(savedCourse.getId(), authorId);
        }

        // 3. Populate the 'authors' list in the returned Course object for convenience
        List<Author> linkedAuthors = authorRepository.findAuthorsByCourseId(savedCourse.getId());
        savedCourse.setAuthors(linkedAuthors);

        System.out.println("Course and authors added successfully: " + savedCourse.getName());
        return savedCourse;
    }

    /**
     * Updates an existing course and its author associations.
     * This operation is transactional.
     *
     * @param course       The Course object with updated details. The ID must be present.
     * @param newAuthorIds A list of UUIDs of authors to link to this course. Existing links
     *                     will be removed and new ones established.
     * @return An Optional containing the updated Course (with new authors) if found, otherwise Optional.empty().
     */
    @Transactional
    public Optional<Course> updateCourse(Course course, List<UUID> newAuthorIds) {
        if (course.getId() == null) {
            System.err.println("Error: Cannot update course with a null ID.");
            return Optional.empty();
        }

        // 1. Update the Course itself
        System.out.println("Attempting to update course with ID: " + course.getId());
        Optional<Course> updatedCourseOpt = courseRepository.update(course);

        if (updatedCourseOpt.isPresent()) {
            Course updatedCourse = updatedCourseOpt.get();

            // Add new links if provided
            if (newAuthorIds != null && !newAuthorIds.isEmpty()) {
                // 2. Update author associations
                // Remove all existing links for this course
                courseRepository.removeAllAuthorsForCourse(updatedCourse.getId());

                for (UUID authorId : newAuthorIds) {
                    courseRepository.addCourseAuthor(updatedCourse.getId(), authorId);
                }

                // 3. Re-populate the 'authors' list in the returned Course object
                List<Author> linkedAuthors = authorRepository.findAuthorsByCourseId(updatedCourse.getId());
                updatedCourse.setAuthors(linkedAuthors);

                System.out.println("Course and authors updated successfully: " + updatedCourse.getName());
            }
            return Optional.of(updatedCourse);
        }
        return Optional.empty();
    }

    /**
     * Retrieves a course by its unique identifier, including its associated authors.
     * Delegates to the repository for data retrieval and then enriches the Course object.
     *
     * @param id The UUID of the course to retrieve.
     * @return An Optional containing the Course (with authors) if found, otherwise Optional.empty().
     */
    public Optional<Course> viewCourse(UUID id) {
        System.out.println("Attempting to view course with ID: " + id);
        Optional<Course> courseOpt = courseRepository.findById(id);

        courseOpt.ifPresent(course -> {
            // Fetch authors for the found course and set them
            List<Author> authors = authorRepository.findAuthorsByCourseId(course.getId());
            course.setAuthors(authors);
        });

        return courseOpt;
    }

    /**
     * Retrieves all available courses, including their associated authors.
     * Delegates to the repository for data retrieval and then enriches each Course object.
     *
     * @return A list of all Course objects (each with their authors populated).
     */
    public List<Course> getAllCourses() {
        System.out.println("Attempting to retrieve all courses with authors.");
        List<Course> courses = courseRepository.findAll();
        // For each course, fetch its associated authors
        courses.forEach(course -> {
            List<Author> authors = authorRepository.findAuthorsByCourseId(course.getId());
            course.setAuthors(authors);
        });
        return courses;
    }

    /**
     * Deletes a course by its unique identifier.
     * `ON DELETE CASCADE` in schema handles the `course_authors` entries.
     *
     * @param id The UUID of the course to delete.
     * @return true if the course was successfully deleted, false otherwise.
     */
    @Transactional // Ensure deletion of course and any related data (e.g., in join table via cascade) is atomic
    public boolean deleteCourse(UUID id) {
        System.out.println("Attempting to delete course with ID: " + id);
        // The PostgreSQL `ON DELETE CASCADE` constraint on the `course_authors` table
        // will automatically handle the deletion of associated entries when the course is deleted.
        return courseRepository.deleteById(id);
    }
}
