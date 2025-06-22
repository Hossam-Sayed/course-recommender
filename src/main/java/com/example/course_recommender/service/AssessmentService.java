package com.example.course_recommender.service;

import com.example.course_recommender.dto.AssessmentDto;
import com.example.course_recommender.mapper.AssessmentMapper;
import com.example.course_recommender.model.Assessment;
import com.example.course_recommender.model.Course;
import com.example.course_recommender.repository.AssessmentJpaRepository;
import com.example.course_recommender.repository.CourseJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Service layer for managing Assessment-related business logic.
 * Uses Spring Data JPA repositories, MapStruct for DTO mapping.
 * Handles the one-to-one relationship with Course.
 */
@Service
public class AssessmentService {

    private final AssessmentJpaRepository assessmentJpaRepository;
    private final CourseJpaRepository courseJpaRepository; // To find the associated Course
    private final AssessmentMapper assessmentMapper;

    /**
     * Constructor for AssessmentService. Spring injects JPA repositories and mapper.
     *
     * @param assessmentJpaRepository The AssessmentJpaRepository instance.
     * @param courseJpaRepository     The CourseJpaRepository instance.
     * @param assessmentMapper        The AssessmentMapper instance.
     */
    @Autowired
    public AssessmentService(AssessmentJpaRepository assessmentJpaRepository, CourseJpaRepository courseJpaRepository, AssessmentMapper assessmentMapper) {
        this.assessmentJpaRepository = assessmentJpaRepository;
        this.courseJpaRepository = courseJpaRepository;
        this.assessmentMapper = assessmentMapper;
    }

    /**
     * Adds a new assessment to the system and links it to a specific course.
     * Due to the one-to-one relationship, a course can only have one assessment.
     *
     * @param assessmentDto The AssessmentDto object to be added, must contain courseId.
     * @return The added AssessmentDto object with its generated ID.
     * @throws IllegalArgumentException if courseId is null, the associated course does not exist,
     *                                  or the course already has an assessment.
     */
    @Transactional
    public AssessmentDto addAssessment(AssessmentDto assessmentDto) {
        if (assessmentDto.getCourseId() == null) {
            throw new IllegalArgumentException("Assessment must be associated with a course.");
        }

        // 1. Find the Course entity by ID
        Optional<Course> courseOpt = courseJpaRepository.findById(assessmentDto.getCourseId());
        if (courseOpt.isEmpty()) {
            throw new IllegalArgumentException("Course with ID " + assessmentDto.getCourseId() + " not found for assessment.");
        }
        Course course = courseOpt.get();

        // 2. Check if the course already has an assessment (due to 1-to-1 relationship)
        if (course.getAssessment() != null) {
            throw new IllegalArgumentException("Course with ID " + course.getId() + " already has an assessment. Use PUT to update.");
        }

        // 3. Convert AssessmentDto to Assessment entity
        Assessment assessment = assessmentMapper.toEntity(assessmentDto);
        // Set the associated Course entity on the Assessment
        assessment.setCourse(course); // Set the owning side of the relationship

        // Also set the inverse side (Course's assessment field) for consistency.
        course.setAssessment(assessment);

        // 4. Save the Assessment entity.
        System.out.println("Attempting to add assessment for course: " + course.getName());
        Assessment savedAssessment = assessmentJpaRepository.save(assessment);

        // 5. Return the mapped DTO of the saved entity
        return assessmentMapper.toDto(savedAssessment);
    }

    /**
     * Updates an existing assessment.
     * The association with the course is typically not changed via this method for a 1-to-1.
     *
     * @param id            The UUID of the assessment to update.
     * @param assessmentDto The AssessmentDto object with updated details.
     * @return An Optional containing the updated AssessmentDto if found, otherwise Optional.empty().
     */
    @Transactional
    public Optional<AssessmentDto> updateAssessment(UUID id, AssessmentDto assessmentDto) {
        System.out.println("Attempting to update assessment with ID: " + id);
        Optional<Assessment> existingAssessmentOpt = assessmentJpaRepository.findById(id);

        if (existingAssessmentOpt.isPresent()) {
            Assessment existingAssessment = existingAssessmentOpt.get();

            // Check if courseId is provided and different (meaning a re-assignment attempt)
            if (assessmentDto.getCourseId() != null && !assessmentDto.getCourseId().equals(existingAssessment.getCourse().getId())) {
                throw new IllegalArgumentException("Cannot change course association for an existing assessment. Delete and re-add if needed.");
            }

            // MapStruct updates basic fields of the existing entity from the DTO
            assessmentMapper.updateEntityFromDto(assessmentDto, existingAssessment);

            // Save the updated Assessment entity
            Assessment updatedAssessment = assessmentJpaRepository.save(existingAssessment);
            return Optional.of(assessmentMapper.toDto(updatedAssessment));
        }
        System.out.println("No assessment found with ID: " + id + " to update.");
        return Optional.empty();
    }

    /**
     * Retrieves an assessment by its unique identifier.
     *
     * @param id The UUID of the assessment to retrieve.
     * @return An Optional containing the AssessmentDto if found, otherwise Optional.empty().
     */
    @Transactional(readOnly = true)
    public Optional<AssessmentDto> viewAssessment(UUID id) {
        System.out.println("Attempting to view assessment with ID: " + id);
        return assessmentJpaRepository.findById(id)
                .map(assessmentMapper::toDto);
    }

    /**
     * Retrieves all available assessments with pagination support.
     *
     * @param pageable Pagination information (page number, page size, sort order).
     * @return A Page of AssessmentDto objects.
     */
    @Transactional(readOnly = true)
    public Page<AssessmentDto> getAllAssessments(Pageable pageable) {
        System.out.println("Attempting to retrieve all assessments with pagination: Page " + pageable.getPageNumber() + ", Size " + pageable.getPageSize());
        Page<Assessment> assessmentPage = assessmentJpaRepository.findAll(pageable);
        return assessmentPage.map(assessmentMapper::toDto);
    }

    /**
     * Deletes an assessment by its unique identifier.
     *
     * @param id The UUID of the assessment to delete.
     * @return true if the assessment was successfully deleted, false otherwise.
     */
    @Transactional
    public boolean deleteAssessment(UUID id) {
        System.out.println("Attempting to delete assessment with ID: " + id);
        if (assessmentJpaRepository.existsById(id)) {
            Optional<Assessment> assessmentOpt = assessmentJpaRepository.findById(id);
            if (assessmentOpt.isPresent()) {
                Course course = assessmentOpt.get().getCourse();
                if (course != null) {
                    course.setAssessment(null);
                    courseJpaRepository.save(course);
                }
            }
            assessmentJpaRepository.deleteById(id);
            System.out.println("Assessment with ID: " + id + " deleted successfully.");
            return true;
        } else {
            System.out.println("No assessment found with ID: " + id + " to delete.");
            return false;
        }
    }

    /**
     * Retrieves an assessment for a specific course.
     *
     * @param courseId The UUID of the course whose assessment is to be retrieved.
     * @return An Optional containing the AssessmentDto for the given course, otherwise Optional.empty().
     */
    @Transactional(readOnly = true)
    public Optional<AssessmentDto> getAssessmentByCourseId(UUID courseId) {
        System.out.println("Attempting to get assessment for course " + courseId + ".");
        return assessmentJpaRepository.findByCourseId(courseId)
                .map(assessmentMapper::toDto);
    }
}
