package com.example.course_recommender.controller;

import com.example.course_recommender.service.CourseService;
import com.example.course_recommender_bean.dto.CourseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for the CourseController class.
 * This class uses @WebMvcTest to test the controller layer in isolation,
 * mocking the CourseService dependency.
 */
@WebMvcTest(CourseController.class)
class CourseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CourseService courseService;

    private UUID courseId;
    private UUID authorId1;
    private UUID authorId2;
    private CourseDto courseDto;
    private CourseController.CourseInputDto courseInputDto;

    /**
     * Set up common test data before each test method runs.
     */
    @BeforeEach
    void setUp() {
        courseId = UUID.randomUUID();
        authorId1 = UUID.randomUUID();
        authorId2 = UUID.randomUUID();

        // CourseDto for expected service responses
        courseDto = new CourseDto(courseId, "Test Course", "Description", 3, Collections.emptyList());

        // CourseInputDto for request bodies
        courseInputDto = new CourseController.CourseInputDto();
        courseInputDto.setName("New Course");
        courseInputDto.setDescription("New Description");
        courseInputDto.setCredit(4);
        courseInputDto.setAuthorIds(Arrays.asList(authorId1, authorId2));
    }

    /**
     * Test case for successfully viewing a course by ID.
     * Expects HTTP 200 OK and the CourseDto.
     */
    @Test
    @DisplayName("GET /api/courses/{id} - Should return 200 OK and CourseDto when course found")
    void viewCourse_Found_Returns200AndCourseDto() throws Exception {
        // Given
        when(courseService.viewCourse(courseId)).thenReturn(Optional.of(courseDto));

        // When & Then
        mockMvc.perform(get("/api/courses/{id}", courseId)
                        .accept(MediaType.APPLICATION_JSON)) // Specifies expected response type
                .andExpect(status().isOk()) // Asserts HTTP 200 OK
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)) // Asserts content type
                .andExpect(jsonPath("$.id").value(courseId.toString())) // Asserts specific JSON field values
                .andExpect(jsonPath("$.name").value("Test Course"));

        verify(courseService, times(1)).viewCourse(courseId); // Verify service method was called
    }

    /**
     * Test case for viewing a non-existent course by ID.
     * Expects HTTP 404 Not Found.
     */
    @Test
    @DisplayName("GET /api/courses/{id} - Should return 404 Not Found when course not found")
    void viewCourse_NotFound_Returns404() throws Exception {
        // Given
        when(courseService.viewCourse(courseId)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/courses/{id}", courseId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()); // Asserts HTTP 404 Not Found

        verify(courseService, times(1)).viewCourse(courseId);
    }

    /**
     * Test case for successfully adding a course.
     * Expects HTTP 201 Created and the created CourseDto.
     */
    @Test
    @DisplayName("POST /api/courses - Should return 201 Created and CourseDto when course added successfully")
    void addCourse_Success_Returns201AndCourseDto() throws Exception {
        // Given
        // Mock the addCourse method of the service to return the courseDto
        when(courseService.addCourse(any(CourseDto.class), anyList())).thenReturn(courseDto);

        // When & Then
        mockMvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON) // Specifies request content type
                        .content(objectMapper.writeValueAsString(courseInputDto))) // Converts DTO to JSON string
                .andExpect(status().isCreated()) // Asserts HTTP 201 Created
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(courseId.toString()))
                .andExpect(jsonPath("$.name").value("Test Course"));

        // Verify that addCourse was called with a CourseDto and the list of author IDs
        verify(courseService, times(1)).addCourse(
                argThat(dto -> dto.getName().equals(courseInputDto.getName()) &&
                        dto.getDescription().equals(courseInputDto.getDescription()) &&
                        dto.getCredit() == courseInputDto.getCredit()),
                eq(courseInputDto.getAuthorIds()) // eq() is used for precise argument matching for lists/collections
        );
    }

    /**
     * Test case for adding a course with missing author IDs.
     * Expects HTTP 400 Bad Request.
     */
    @Test
    @DisplayName("POST /api/courses - Should return 400 Bad Request when authorIds are missing")
    void addCourse_MissingAuthorIds_Returns400() throws Exception {
        // Given
        courseInputDto.setAuthorIds(Collections.emptyList()); // Set empty author IDs

        // When & Then
        mockMvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(courseInputDto)))
                .andExpect(status().isBadRequest()); // Asserts HTTP 400 Bad Request

        verifyNoInteractions(courseService); // No interaction with the service when input is invalid
    }

    /**
     * Test case for adding a course when service throws IllegalArgumentException.
     * Expects HTTP 400 Bad Request.
     */
    @Test
    @DisplayName("POST /api/courses - Should return 400 Bad Request when service throws IllegalArgumentException")
    void addCourse_IllegalArgument_Returns400() throws Exception {
        // Given
        when(courseService.addCourse(any(CourseDto.class), anyList()))
                .thenThrow(new IllegalArgumentException("Invalid author IDs provided"));

        // When & Then
        mockMvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(courseInputDto)))
                .andExpect(status().isBadRequest());

        verify(courseService, times(1)).addCourse(any(CourseDto.class), anyList());
    }

    /**
     * Test case for successfully updating a course.
     * Expects HTTP 200 OK and the updated CourseDto.
     */
    @Test
    @DisplayName("PUT /api/courses/{id} - Should return 200 OK and CourseDto when course updated successfully")
    void updateCourse_Success_Returns200AndCourseDto() throws Exception {
        // Given
        CourseDto updatedCourseDto = new CourseDto(courseId, "Updated Name", "Updated Desc", 5, Collections.emptyList());
        when(courseService.updateCourse(eq(courseId), any(CourseDto.class), anyList())).thenReturn(Optional.of(updatedCourseDto));

        // When & Then
        mockMvc.perform(put("/api/courses/{id}", courseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(courseInputDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(courseId.toString()))
                .andExpect(jsonPath("$.name").value("Updated Name"));

        verify(courseService, times(1)).updateCourse(
                eq(courseId), // Matches the exact UUID
                argThat(dto -> dto.getName().equals(courseInputDto.getName()) &&
                        dto.getDescription().equals(courseInputDto.getDescription()) &&
                        dto.getCredit() == courseInputDto.getCredit()),
                eq(courseInputDto.getAuthorIds())
        );
    }

    /**
     * Test case for updating a non-existent course.
     * Expects HTTP 404 Not Found.
     */
    @Test
    @DisplayName("PUT /api/courses/{id} - Should return 404 Not Found when updating non-existent course")
    void updateCourse_NotFound_Returns404() throws Exception {
        // Given
        when(courseService.updateCourse(eq(courseId), any(CourseDto.class), anyList())).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(put("/api/courses/{id}", courseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(courseInputDto)))
                .andExpect(status().isNotFound());

        verify(courseService, times(1)).updateCourse(eq(courseId), any(CourseDto.class), anyList());
    }

    /**
     * Test case for successfully deleting a course.
     * Expects HTTP 204 No Content.
     */
    @Test
    @DisplayName("DELETE /api/courses/{id} - Should return 204 No Content when course deleted successfully")
    void deleteCourse_Success_Returns204() throws Exception {
        // Given
        when(courseService.deleteCourse(courseId)).thenReturn(true);

        // When & Then
        mockMvc.perform(delete("/api/courses/{id}", courseId))
                .andExpect(status().isNoContent()); // Asserts HTTP 204 No Content

        verify(courseService, times(1)).deleteCourse(courseId);
    }

    /**
     * Test case for deleting a non-existent course.
     * Expects HTTP 404 Not Found.
     */
    @Test
    @DisplayName("DELETE /api/courses/{id} - Should return 404 Not Found when deleting non-existent course")
    void deleteCourse_NotFound_Returns404() throws Exception {
        // Given
        when(courseService.deleteCourse(courseId)).thenReturn(false);

        // When & Then
        mockMvc.perform(delete("/api/courses/{id}", courseId))
                .andExpect(status().isNotFound()); // Asserts HTTP 404 Not Found

        verify(courseService, times(1)).deleteCourse(courseId);
    }

    /**
     * Test case for getting all courses with pagination.
     * Expects HTTP 200 OK and a Page of CourseDto.
     */
    @Test
    @DisplayName("GET /api/courses - Should return 200 OK and a Page of CourseDto")
    void getAllCourses_Success_Returns200AndPage() throws Exception {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<CourseDto> courseDtos = Collections.singletonList(courseDto);
        Page<CourseDto> coursePage = new PageImpl<>(courseDtos, pageable, courseDtos.size());

        when(courseService.getAllCourses(any(Pageable.class))).thenReturn(coursePage);

        // When & Then
        mockMvc.perform(get("/api/courses?page=0&size=10&sort=name,asc")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(courseId.toString()))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(courseService, times(1)).getAllCourses(any(Pageable.class));
    }

    /**
     * Test case for getting recommended courses with pagination.
     * Expects HTTP 200 OK and a Page of CourseDto.
     */
    @Test
    @DisplayName("GET /api/courses/recommendations - Should return 200 OK and a Page of CourseDto")
    void discoverAllCourses_Success_Returns200AndPage() throws Exception {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<CourseDto> recommendedCourseDtos = Collections.singletonList(courseDto);
        Page<CourseDto> recommendedCoursePage = new PageImpl<>(recommendedCourseDtos, pageable, recommendedCourseDtos.size());

        when(courseService.getRecommendedCourses(any(Pageable.class))).thenReturn(recommendedCoursePage);

        // When & Then
        mockMvc.perform(get("/api/courses/recommendations?page=0&size=10&sort=name,asc")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(courseId.toString()))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(courseService, times(1)).getRecommendedCourses(any(Pageable.class));
    }
}
