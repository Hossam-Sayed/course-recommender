package com.example.course_recommender.controller;

import com.example.course_recommender.CourseRecommenderApplication;
import com.example.course_recommender.model.Author;
import com.example.course_recommender.model.Course;
import com.example.course_recommender.model.User;
import com.example.course_recommender.repository.AuthorJpaRepository;
import com.example.course_recommender.repository.CourseJpaRepository;
import com.example.course_recommender.repository.UserJpaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the CourseController.
 * This class uses @SpringBootTest to load the full application context
 * and a dedicated MySQL database instance for testing.
 * It also includes tests for Spring Security configuration and custom filter.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = CourseRecommenderApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CourseControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CourseJpaRepository courseJpaRepository;
    @Autowired
    private AuthorJpaRepository authorJpaRepository;
    @Autowired
    private UserJpaRepository userJpaRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private Author savedAuthor1;
    private Author savedAuthor2;
    private Course savedCourse1;
    private Course savedCourse2;
    private User testUser;

    /**
     * Sets up common test data before each test method runs.
     * This includes clearing databases, creating authors, courses, and a test user.
     */
    @BeforeEach
    void setUp() {
        courseJpaRepository.deleteAllInBatch();
        authorJpaRepository.deleteAllInBatch();
        userJpaRepository.deleteAllInBatch();

        // Create and save authors
        Author author1 = new Author();
        author1.setName("Integration Author One");
        author1.setEmail("integration.author1@example.com");
        author1.setBirthdate(LocalDate.of(1980, 1, 1));
        savedAuthor1 = authorJpaRepository.save(author1);

        Author author2 = new Author();
        author2.setName("Integration Author Two");
        author2.setEmail("integration.author2@example.com");
        author2.setBirthdate(LocalDate.of(1985, 5, 5));
        savedAuthor2 = authorJpaRepository.save(author2);

        // Create and save courses
        Course course1 = new Course(null, "Existing Integration Course One", "Description one", 3);
        course1.setAuthors(new ArrayList<>(Arrays.asList(savedAuthor1, savedAuthor2)));
        savedCourse1 = courseJpaRepository.save(course1);

        Course course2 = new Course(null, "Existing Integration Course Two", "Description two", 4);
        course2.setAuthors(new ArrayList<>(Collections.singletonList(savedAuthor1)));
        savedCourse2 = courseJpaRepository.save(course2);

        // Create and save a test user for authenticated requests
        testUser = new User("testuser", passwordEncoder.encode("testpass"), Set.of("ROLE_USER"));
        userJpaRepository.save(testUser);
    }

    /**
     * Tests retrieving a course by ID. This is a permitAll endpoint and should not require authentication or the custom header.
     * Expects HTTP 200 OK.
     */
    @Test
    @DisplayName("GET /api/courses/{id} - Should return 200 OK for permitAll endpoint")
    void viewCourse_ExistingCourse_Returns200AndCourseDto() throws Exception {
        mockMvc.perform(get("/api/courses/{id}", savedCourse1.getId())
                        .header("x-validation-report", "true")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(savedCourse1.getId().toString()))
                .andExpect(jsonPath("$.name").value(savedCourse1.getName()))
                .andExpect(jsonPath("$.authors", hasSize(2)))
                .andExpect(jsonPath("$.authors[0].name").value(savedAuthor1.getName()));
    }

    /**
     * Tests adding a new course with valid authentication (HTTP Basic) and the required 'x-validation-report' header.
     * Expects HTTP 201 Created.
     */
    @Test
    @DisplayName("POST /api/courses - Should return 201 Created with auth and header")
    void addCourse_ValidInput_Returns201AndPersistsCourse_Authenticated() throws Exception {
        CourseController.CourseInputDto inputDto = new CourseController.CourseInputDto();
        inputDto.setName("New Secured Course");
        inputDto.setDescription("Description for new secured course.");
        inputDto.setCredit(4);
        inputDto.setAuthorIds(Collections.singletonList(savedAuthor1.getId()));

        mockMvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto))
                        .header("x-validation-report", "true") // Required custom header
                        .with(httpBasic(testUser.getUsername(), "testpass"))) // HTTP Basic authentication
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("New Secured Course"))
                .andExpect(jsonPath("$.authors", hasSize(1)));

        List<Course> courses = courseJpaRepository.findAll();
        Course createdCourse = courses.stream()
                .filter(c -> c.getName().equals("New Secured Course"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("New course not found in database"));
        assertThat(createdCourse.getAuthors(), hasSize(1));
        assertThat(createdCourse.getAuthors().getFirst().getName(), is(savedAuthor1.getName()));
    }

    /**
     * Tests adding a new course without any authentication.
     * Expects HTTP 401 Unauthorized.
     */
    @Test
    @DisplayName("POST /api/courses - Should return 401 Unauthorized without authentication")
    void addCourse_NoAuth_Returns401() throws Exception {
        CourseController.CourseInputDto inputDto = new CourseController.CourseInputDto();
        inputDto.setName("Unauthorized Course");
        inputDto.setDescription("Description.");
        inputDto.setCredit(1);
        inputDto.setAuthorIds(Collections.singletonList(savedAuthor1.getId()));

        mockMvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto))
                        .header("x-validation-report", "true")) // Header is present, but no auth
                .andExpect(status().isUnauthorized());

        assertThat(courseJpaRepository.findAll(), hasSize(2)); // No new course should be added
    }

    /**
     * Tests adding a new course with invalid HTTP Basic authentication credentials.
     * Expects HTTP 401 Unauthorized.
     */
    @Test
    @DisplayName("POST /api/courses - Should return 401 Unauthorized with invalid authentication")
    void addCourse_InvalidAuth_Returns401() throws Exception {
        CourseController.CourseInputDto inputDto = new CourseController.CourseInputDto();
        inputDto.setName("Invalid Auth Course");
        inputDto.setDescription("Description.");
        inputDto.setCredit(1);
        inputDto.setAuthorIds(Collections.singletonList(savedAuthor1.getId()));

        mockMvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto))
                        .header("x-validation-report", "true")
                        .with(httpBasic(testUser.getUsername(), "wrongpass"))) // Invalid password
                .andExpect(status().isUnauthorized());

        assertThat(courseJpaRepository.findAll(), hasSize(2));
    }

    /**
     * Tests adding a new course with valid authentication but missing the custom 'x-validation-report' header.
     * Expects HTTP 400 Bad Request from the custom filter.
     */
    @Test
    @DisplayName("POST /api/courses - Should return 400 Bad Request without x-validation-report header")
    void addCourse_NoXValidationHeader_Returns400() throws Exception {
        CourseController.CourseInputDto inputDto = new CourseController.CourseInputDto();
        inputDto.setName("No Header Course");
        inputDto.setDescription("Description.");
        inputDto.setCredit(1);
        inputDto.setAuthorIds(Collections.singletonList(savedAuthor1.getId()));

        mockMvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto))
                        .with(httpBasic(testUser.getUsername(), "testpass"))) // Auth is present, but no header
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Missing or invalid 'x-validation-report' header. It must be 'true'."));

        assertThat(courseJpaRepository.findAll(), hasSize(2));
    }

    /**
     * Tests updating an existing course with valid authentication and the required custom header.
     * Expects HTTP 200 OK.
     */
    @Test
    @DisplayName("PUT /api/courses/{id} - Should return 200 OK with auth and header")
    void updateCourse_ExistingCourse_Returns200AndUpdates_Authenticated() throws Exception {
        CourseController.CourseInputDto updateDto = new CourseController.CourseInputDto();
        updateDto.setName("Updated Secured Course Name");
        updateDto.setDescription("Updated Description");
        updateDto.setCredit(5);
        updateDto.setAuthorIds(Collections.singletonList(savedAuthor2.getId()));

        mockMvc.perform(put("/api/courses/{id}", savedCourse1.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto))
                        .header("x-validation-report", "true")
                        .with(httpBasic(testUser.getUsername(), "testpass")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedCourse1.getId().toString()))
                .andExpect(jsonPath("$.name").value("Updated Secured Course Name"))
                .andExpect(jsonPath("$.authors", hasSize(1)))
                .andExpect(jsonPath("$.authors[0].name").value(savedAuthor2.getName()));

        Course updatedCourse = courseJpaRepository.findById(savedCourse1.getId())
                .orElseThrow(() -> new AssertionError("Course not found after update"));
        assertThat(updatedCourse.getName(), is("Updated Secured Course Name"));
    }

    /**
     * Tests deleting an existing course with valid authentication and the required custom header.
     * Expects HTTP 204 No Content.
     */
    @Test
    @DisplayName("DELETE /api/courses/{id} - Should return 204 No Content with auth and header")
    void deleteCourse_ExistingCourse_Returns204AndDeletes_Authenticated() throws Exception {
        mockMvc.perform(delete("/api/courses/{id}", savedCourse1.getId())
                        .header("x-validation-report", "true")
                        .with(httpBasic(testUser.getUsername(), "testpass")))
                .andExpect(status().isNoContent());

        assertThat(courseJpaRepository.findById(savedCourse1.getId()).isEmpty(), is(true));
    }

    /**
     * Tests retrieving all courses with pagination. This is a permitAll endpoint and should not require authentication or the custom header.
     * Expects HTTP 200 OK and a page of courses.
     */
    @Test
    @DisplayName("GET /api/courses - Should return 200 OK and a page of courses")
    void getAllCourses_Returns200AndPage() throws Exception {
        mockMvc.perform(get("/api/courses?page=0&size=10&sort=name,asc")
                        .header("x-validation-report", "true")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].name").value(savedCourse1.getName()))
                .andExpect(jsonPath("$.content[1].name").value(savedCourse2.getName()))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    /**
     * Tests retrieving recommended courses. This is a permitAll endpoint and should not require authentication or the custom header.
     * Expects HTTP 200 OK and a page of all courses from the database.
     */
    @Test
    @DisplayName("GET /api/courses/recommendations - Should return 200 OK and a page of all courses from DB")
    void discoverAllCourses_ReturnsAllFromDB() throws Exception {
        mockMvc.perform(get("/api/courses/recommendations?page=0&size=10&sort=name,asc")
                        .header("x-validation-report", "true")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].name").value("SOAP Basics for Beginners"))
                .andExpect(jsonPath("$.content[1].name").value("Advanced XML Schema Design"))
                .andExpect(jsonPath("$.totalElements").value(2));
    }
}