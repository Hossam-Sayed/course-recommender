package com.example.course_recommender.service;

import com.example.course_recommender.mapper.CourseMapper;
import com.example.course_recommender.model.Author;
import com.example.course_recommender.model.Course;
import com.example.course_recommender.repository.AuthorJpaRepository;
import com.example.course_recommender.repository.CourseJpaRepository;
import com.example.course_recommender_bean.dto.CourseDto;
import com.example.course_recommender_bean.service.CourseRecommender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the CourseService class.
 * This class uses Mockito to mock dependencies and test CourseService methods in isolation.
 */
@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock
    private CourseJpaRepository courseJpaRepository;

    @Mock
    private AuthorJpaRepository authorJpaRepository;

    @Mock
    private CourseRecommender courseRecommender;

    @Mock
    private CourseMapper courseMapper;

    @InjectMocks
    private CourseService courseService;

    private UUID courseId;
    private UUID authorId1;
    private UUID authorId2;
    private CourseDto courseDto;
    private Course course; // Represents the Course entity before saving or after fetching
    private Author author1;
    private Author author2;

    /**
     * Set up common test data before each test method runs.
     */
    @BeforeEach
    void setUp() {
        // Initialize UUIDs
        courseId = UUID.randomUUID();
        authorId1 = UUID.randomUUID();
        authorId2 = UUID.randomUUID();

        // Create CourseDto for input.
        courseDto = new CourseDto(null, "Test Course", "Description", 3, Collections.emptyList());

        // Create Author entities.
        author1 = new Author();
        author1.setId(authorId1);
        author1.setName("Author One");
        author1.setEmail("author1@example.com");

        author2 = new Author();
        author2.setId(authorId2);
        author2.setName("Author Two");
        author2.setEmail("author2@example.com");

        // Create Course entity for repository interaction.
        course = new Course(courseId, "Test Course", "Description", 3);
    }

    /**
     * Test case for successful addition of a course with valid authors.
     */
    @Test
    @DisplayName("Should successfully add a course with valid authors")
    void addCourse_Success() {
        // Given
        List<UUID> authorIds = Arrays.asList(authorId1, authorId2);

        // Course entity that will be returned by courseMapper.toEntity
        Course courseToSave = new Course(null, "Test Course", "Description", 3); // ID is null before saving

        // Course entity that will be returned by courseJpaRepository.save (with generated ID and authors)
        Course savedCourse = new Course(courseId, "Test Course", "Description", 3);
        savedCourse.setAuthors(Arrays.asList(author1, author2)); // Set authors on the saved entity

        // CourseDto that will be returned by courseMapper.toDto
        CourseDto expectedCourseDto = new CourseDto(courseId, "Test Course", "Description", 3, Collections.emptyList());

        // When
        // Mock behavior for the dependencies
        // When mapper converts DTO to entity, return 'courseToSave' (entity without ID/authors initially)
        when(courseMapper.toEntity(any(CourseDto.class))).thenReturn(courseToSave);
        // When author repo finds author1, return it
        when(authorJpaRepository.findById(authorId1)).thenReturn(Optional.of(author1));
        // When author repo finds author2, return it
        when(authorJpaRepository.findById(authorId2)).thenReturn(Optional.of(author2));
        // When course repo saves the course entity, return 'savedCourse' (entity with ID and authors)
        when(courseJpaRepository.save(any(Course.class))).thenReturn(savedCourse);
        // When mapper converts the saved entity back to DTO, return 'expectedCourseDto'
        when(courseMapper.toDto(any(Course.class))).thenReturn(expectedCourseDto);

        // Call the service method
        CourseDto result = courseService.addCourse(courseDto, authorIds);

        // Then
        // Verify that the correct methods were called on the mocks
        assertNotNull(result);
        assertEquals(expectedCourseDto.getId(), result.getId());
        assertEquals(expectedCourseDto.getName(), result.getName());
        verify(courseMapper, times(1)).toEntity(courseDto); // Verify toEntity was called once with courseDto
        verify(authorJpaRepository, times(1)).findById(authorId1); // Verify findById for author1 was called once
        verify(authorJpaRepository, times(1)).findById(authorId2); // Verify findById for author2 was called once
        // Verify that the course entity passed to save method has the correct authors set
        verify(courseJpaRepository, times(1)).save(argThat(c ->
                c.getAuthors() != null &&
                        c.getAuthors().contains(author1) &&
                        c.getAuthors().contains(author2) &&
                        c.getAuthors().size() == 2 // Ensure only these authors are present
        ));
        verify(courseMapper, times(1)).toDto(savedCourse); // Verify toDto was called once with the saved course
    }

    /**
     * Test case for adding a course with no author IDs, expecting an IllegalArgumentException.
     */
    @Test
    @DisplayName("Should throw IllegalArgumentException when adding course with no author IDs")
    void addCourse_NoAuthorIds_ThrowsException() {
        // Given
        List<UUID> authorIds = Collections.emptyList(); // Empty list of author IDs

        // When/Then
        // Assert that calling addCourse with empty authorIds throws IllegalArgumentException
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () ->
                courseService.addCourse(courseDto, authorIds)
        );

        // Verify exception message
        assertEquals("A course must be linked to at least one author.", thrown.getMessage());
        // No interactions with mocks
        verifyNoInteractions(courseMapper, courseJpaRepository, authorJpaRepository);
    }

    /**
     * Test case for adding a course with invalid author IDs, expecting an IllegalArgumentException.
     */
    @Test
    @DisplayName("Should throw IllegalArgumentException when adding course with invalid author IDs")
    void addCourse_InvalidAuthorIds_ThrowsException() {
        // Given
        List<UUID> authorIds = Collections.singletonList(UUID.randomUUID()); // List with one non-existent author ID

        // Course entity that will be returned by courseMapper.toEntity
        Course courseToSave = new Course(null, "Test Course", "Description", 3);

        // When
        when(courseMapper.toEntity(any(CourseDto.class))).thenReturn(courseToSave); // Mock mapper
        when(authorJpaRepository.findById(any(UUID.class))).thenReturn(Optional.empty()); // Mock author repo to return empty for any ID

        // Then
        // Assert that calling addCourse throws IllegalArgumentException
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () ->
                courseService.addCourse(courseDto, authorIds)
        );
        assertEquals("None of the provided author IDs were valid.", thrown.getMessage()); // Verify exception message
        verify(courseMapper, times(1)).toEntity(courseDto); // Verify toEntity was called
        verify(authorJpaRepository, times(1)).findById(authorIds.getFirst()); // Verify findById was called for the invalid ID
        verify(courseJpaRepository, never()).save(any(Course.class)); // Verify save was NOT called
    }

    /**
     * Test case for successful update of an existing course with new authors.
     */
    @Test
    @DisplayName("Should successfully update an existing course with new authors")
    void updateCourse_Success_WithNewAuthors() {
        // Given
        List<UUID> newAuthorIds = Collections.singletonList(authorId2); // New author to link
        CourseDto updatedCourseDto = new CourseDto(courseId, "Updated Course", "Updated Description", 4, Collections.emptyList());

        // Existing course with author1 linked
        Course existingCourse = new Course(courseId, "Old Course", "Old Description", 2);
        existingCourse.setAuthors(new ArrayList<>(Collections.singletonList(author1))); // Add author1 to existing course

        // The final state of the course after update and saving
        Course finalUpdatedCourse = new Course(courseId, "Updated Course", "Updated Description", 4);
        finalUpdatedCourse.setAuthors(new ArrayList<>(Collections.singletonList(author2))); // Should contain only author2

        // When
        // Mock existing course lookup
        when(courseJpaRepository.findById(courseId)).thenReturn(Optional.of(existingCourse));
        // Mock author lookup for new author
        when(authorJpaRepository.findById(authorId2)).thenReturn(Optional.of(author2));
        // Mock updateEntityFromDto - simulate MapStruct updating the basic fields of the entity
        doNothing().when(courseMapper).updateEntityFromDto(any(CourseDto.class), any(Course.class));
        // Mock saving the updated course
        when(courseJpaRepository.save(any(Course.class))).thenReturn(finalUpdatedCourse);
        // Mock mapping back to DTO
        when(courseMapper.toDto(any(Course.class))).thenReturn(updatedCourseDto);

        // Call the service method
        Optional<CourseDto> result = courseService.updateCourse(courseId, updatedCourseDto, newAuthorIds);

        // Then
        assertTrue(result.isPresent());
        assertEquals(updatedCourseDto.getName(), result.get().getName());
        assertEquals(updatedCourseDto.getDescription(), result.get().getDescription());
        assertEquals(updatedCourseDto.getCredit(), result.get().getCredit());

        verify(courseJpaRepository, times(1)).findById(courseId); // Verify findById was called
        verify(courseMapper, times(1)).updateEntityFromDto(updatedCourseDto, existingCourse); // Verify updateEntityFromDto was called
        verify(authorJpaRepository, times(1)).findById(authorId2); // Verify findById for new author was called
        // Verify that the course entity passed to save method has the correct authors set after update
        verify(courseJpaRepository, times(1)).save(argThat(c ->
                c.getAuthors() != null &&
                        c.getAuthors().contains(author2) &&
                        c.getAuthors().size() == 1 // Should only contain the new author
        ));
        verify(courseMapper, times(1)).toDto(finalUpdatedCourse); // Verify toDto was called
    }

    /**
     * Test case for updating an existing course without changing authors.
     */
    @Test
    @DisplayName("Should successfully update an existing course without changing authors")
    void updateCourse_Success_NoNewAuthors() {
        // Given
        CourseDto updatedCourseDto = new CourseDto(courseId, "Updated Course", "Updated Description", 4, Collections.emptyList());

        // Existing course with author1 linked
        Course existingCourse = new Course(courseId, "Old Course", "Old Description", 2);
        existingCourse.setAuthors(new ArrayList<>(Collections.singletonList(author1)));

        // The final state of the course after update (authors remain the same)
        Course finalUpdatedCourse = new Course(courseId, "Updated Course", "Updated Description", 4);
        finalUpdatedCourse.setAuthors(new ArrayList<>(Collections.singletonList(author1)));

        // When
        when(courseJpaRepository.findById(courseId)).thenReturn(Optional.of(existingCourse));
        doNothing().when(courseMapper).updateEntityFromDto(any(CourseDto.class), any(Course.class));
        when(courseJpaRepository.save(any(Course.class))).thenReturn(finalUpdatedCourse);
        when(courseMapper.toDto(any(Course.class))).thenReturn(updatedCourseDto);

        Optional<CourseDto> result = courseService.updateCourse(courseId, updatedCourseDto, null); // No new authors provided

        // Then
        assertTrue(result.isPresent());
        assertEquals(updatedCourseDto.getName(), result.get().getName());
        verify(courseJpaRepository, times(1)).findById(courseId);
        verify(courseMapper, times(1)).updateEntityFromDto(updatedCourseDto, existingCourse);
        verify(authorJpaRepository, never()).findById(any(UUID.class)); // Verify no interaction with author repo
        // Verify that the course entity passed to save method still contains author1
        verify(courseJpaRepository, times(1)).save(argThat(c ->
                c.getAuthors() != null &&
                        c.getAuthors().contains(author1) &&
                        c.getAuthors().size() == 1
        ));
        verify(courseMapper, times(1)).toDto(finalUpdatedCourse);
    }

    /**
     * Test case for updating a non-existent course, expecting Optional.empty().
     */
    @Test
    @DisplayName("Should return Optional.empty when updating a non-existent course")
    void updateCourse_NotFound() {
        // Given
        CourseDto updatedCourseDto = new CourseDto(courseId, "Updated Course", "Updated Description", 4, Collections.emptyList());

        // When
        when(courseJpaRepository.findById(courseId)).thenReturn(Optional.empty()); // Mock course not found

        Optional<CourseDto> result = courseService.updateCourse(courseId, updatedCourseDto, null);

        // Then
        assertFalse(result.isPresent()); // Assert Optional is empty
        verify(courseJpaRepository, times(1)).findById(courseId); // Verify findById was called
        verifyNoMoreInteractions(courseMapper, authorJpaRepository, courseJpaRepository); // No further interactions
    }

    /**
     * Test case for viewing an existing course successfully.
     */
    @Test
    @DisplayName("Should return CourseDto when viewing an existing course")
    void viewCourse_Found() {
        // Given
        // Create a Course entity with some authors to simulate a real scenario
        Course courseWithAuthors = new Course(courseId, "Test Course", "Description", 3);
        courseWithAuthors.setAuthors(Arrays.asList(author1, author2));

        // CourseDto that will be returned after mapping
        CourseDto expectedCourseDto = new CourseDto(courseId, "Test Course", "Description", 3, Collections.emptyList());

        // When
        when(courseJpaRepository.findById(courseId)).thenReturn(Optional.of(courseWithAuthors)); // Mock course found
        when(courseMapper.toDto(courseWithAuthors)).thenReturn(expectedCourseDto); // Mock mapping to DTO

        Optional<CourseDto> result = courseService.viewCourse(courseId);

        // Then
        assertTrue(result.isPresent()); // Assert Optional is present
        assertEquals(expectedCourseDto, result.get()); // Assert returned DTO matches expected
        verify(courseJpaRepository, times(1)).findById(courseId); // Verify findById was called
        verify(courseMapper, times(1)).toDto(courseWithAuthors); // Verify toDto was called
    }

    /**
     * Test case for viewing a non-existent course, expecting Optional.empty().
     */
    @Test
    @DisplayName("Should return Optional.empty when viewing a non-existent course")
    void viewCourse_NotFound() {
        // Given / When
        when(courseJpaRepository.findById(courseId)).thenReturn(Optional.empty()); // Mock course not found

        Optional<CourseDto> result = courseService.viewCourse(courseId);

        // Then
        assertFalse(result.isPresent()); // Assert Optional is empty
        verify(courseJpaRepository, times(1)).findById(courseId); // Verify findById was called
        verifyNoInteractions(courseMapper); // No interaction with mapper
    }

    /**
     * Test case for successfully retrieving all courses with pagination.
     */
    @Test
    @DisplayName("Should return a Page of CourseDto when getting all courses")
    void getAllCourses_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<Course> courses = Arrays.asList(
                new Course(courseId, "Course 1", "Desc 1", 3),
                new Course(UUID.randomUUID(), "Course 2", "Desc 2", 4)
        );
        Page<Course> coursePage = new PageImpl<>(courses, pageable, courses.size());

        CourseDto dto1 = new CourseDto(courseId, "Course 1 DTO", "Desc 1 DTO", 3, Collections.emptyList());
        CourseDto dto2 = new CourseDto(UUID.randomUUID(), "Course 2 DTO", "Desc 2 DTO", 4, Collections.emptyList());

        // When
        when(courseJpaRepository.findAll(pageable)).thenReturn(coursePage); // Mock repository to return page of courses
        when(courseMapper.toDto(courses.get(0))).thenReturn(dto1); // Mock mapper for first course
        when(courseMapper.toDto(courses.get(1))).thenReturn(dto2); // Mock mapper for second course

        Page<CourseDto> result = courseService.getAllCourses(pageable);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getTotalElements()); // Assert total elements
        assertEquals(dto1, result.getContent().get(0)); // Assert content matches
        assertEquals(dto2, result.getContent().get(1));
        verify(courseJpaRepository, times(1)).findAll(pageable); // Verify findAll was called
        verify(courseMapper, times(courses.size())).toDto(any(Course.class)); // Verify toDto was called for each course
    }

    /**
     * Test case for successfully retrieving recommended courses with pagination.
     */
    @Test
    @DisplayName("Should return a Page of CourseDto when getting recommended courses")
    void getRecommendedCourses_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<CourseDto> recommendedCourseDtos = Arrays.asList(
                new CourseDto(courseId, "Recommended Course 1", "Recommended Desc 1", 5, Collections.emptyList()),
                new CourseDto(UUID.randomUUID(), "Recommended Course 2", "Recommended Desc 2", 4, Collections.emptyList())
        );
        Page<CourseDto> expectedPage = new PageImpl<>(recommendedCourseDtos, pageable, recommendedCourseDtos.size());

        // When
        when(courseRecommender.recommendedCourses(pageable)).thenReturn(expectedPage); // Mock recommender service

        Page<CourseDto> result = courseService.getRecommendedCourses(pageable);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(recommendedCourseDtos.getFirst().getName(), result.getContent().getFirst().getName());
        verify(courseRecommender, times(1)).recommendedCourses(pageable); // Verify recommendedCourses was called
    }


    /**
     * Test case for successful deletion of an existing course.
     */
    @Test
    @DisplayName("Should return true when deleting an existing course")
    void deleteCourse_Found() {
        // Given / When
        when(courseJpaRepository.existsById(courseId)).thenReturn(true); // Mock course exists
        doNothing().when(courseJpaRepository).deleteById(courseId); // Mock delete operation

        boolean deleted = courseService.deleteCourse(courseId);

        // Then
        assertTrue(deleted); // Assert deletion was successful
        verify(courseJpaRepository, times(1)).existsById(courseId); // Verify existsById was called
        verify(courseJpaRepository, times(1)).deleteById(courseId); // Verify deleteById was called
    }

    /**
     * Test case for attempting to delete a non-existent course.
     */
    @Test
    @DisplayName("Should return false when deleting a non-existent course")
    void deleteCourse_NotFound() {
        // Given / When
        when(courseJpaRepository.existsById(courseId)).thenReturn(false); // Mock course does not exist

        boolean deleted = courseService.deleteCourse(courseId);

        // Then
        assertFalse(deleted); // Assert deletion was unsuccessful
        verify(courseJpaRepository, times(1)).existsById(courseId); // Verify existsById was called
        verify(courseJpaRepository, never()).deleteById(courseId); // Verify deleteById was NOT called
    }
}
