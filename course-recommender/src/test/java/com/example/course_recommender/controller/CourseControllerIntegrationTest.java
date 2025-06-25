package com.example.course_recommender.controller;

import com.example.course_recommender.CourseRecommenderApplication;
import com.example.course_recommender.model.Author;
import com.example.course_recommender.model.Course;
import com.example.course_recommender.repository.AuthorJpaRepository;
import com.example.course_recommender.repository.CourseJpaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the CourseController.
 */
@SpringBootTest(classes = CourseRecommenderApplication.class) // Loads the full Spring Boot application context
@AutoConfigureMockMvc // Configures MockMvc for web layer testing
@ActiveProfiles("test") // Activates the "test" profile and loads application-test.properties
@Transactional // Ensures each test runs in a transaction and rolls back changes
class CourseControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc; // Used to perform HTTP requests against the running application context

    @Autowired
    private ObjectMapper objectMapper; // Used for JSON serialization/deserialization

    @Autowired
    private CourseJpaRepository courseJpaRepository; // Inject real repositories for setup/verification
    @Autowired
    private AuthorJpaRepository authorJpaRepository;

    private Author savedAuthor1;
    private Author savedAuthor2;
    private Course savedCourse;

    /**
     * Set up common test data before each test method.
     * This method cleans up the database and pre-populates it for each test.
     *
     * @Transactional annotation on the test class ensures rollback after each test.
     */
    @BeforeEach
    void setUp() {
        // Clean up any existing data (@Transactional should handle most of this)
        courseJpaRepository.deleteAll();
        authorJpaRepository.deleteAll();

        // Create and save authors for linking courses
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

        // Create and save a course linked to authors for initial data
        Course course = new Course(null, "Existing Integration Course", "Description", 3);
        course.setAuthors(new ArrayList<>(Arrays.asList(savedAuthor1, savedAuthor2))); // Link to saved authors
        savedCourse = courseJpaRepository.save(course);
    }

    /**
     * Test case for retrieving a course by ID in an integration scenario.
     * Verifies full stack operation.
     */
    @Test
    @DisplayName("GET /api/courses/{id} - Should return 200 OK and CourseDto for existing course")
    void viewCourse_ExistingCourse_Returns200AndCourseDto() throws Exception {
        mockMvc.perform(get("/api/courses/{id}", savedCourse.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(savedCourse.getId().toString()))
                .andExpect(jsonPath("$.name").value(savedCourse.getName()))
                .andExpect(jsonPath("$.authors", hasSize(2))) // Verify authors are loaded
                .andExpect(jsonPath("$.authors[0].name").value(savedAuthor1.getName())); // Assuming ordering by name
    }

    /**
     * Test case for adding a new course via the API.
     * Verifies that the course is persisted correctly in the H2 database.
     */
    @Test
    @DisplayName("POST /api/courses - Should return 201 Created and persist the new course")
    void addCourse_ValidInput_Returns201AndPersistsCourse() throws Exception {
        // Create a CourseInputDto with author IDs
        CourseController.CourseInputDto inputDto = new CourseController.CourseInputDto();
        inputDto.setName("New Course From API");
        inputDto.setDescription("Description for new course.");
        inputDto.setCredit(4);
        inputDto.setAuthorIds(Collections.singletonList(savedAuthor1.getId()));

        mockMvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("New Course From API"))
                .andExpect(jsonPath("$.authors", hasSize(1))); // Verify author is linked

        // Verify persistence by fetching from the repository
        List<Course> courses = courseJpaRepository.findAll();
        Course createdCourse = courses.stream()
                .filter(c -> c.getName().equals("New Course From API"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("New course not found in database"));

        // Ensure authors are correctly linked in the persisted entity
        assertThat(createdCourse.getAuthors(), hasSize(1));
        assertThat(createdCourse.getAuthors().getFirst().getName(), is(savedAuthor1.getName()));
    }

    /**
     * Test case for updating an existing course via the API.
     */
    @Test
    @DisplayName("PUT /api/courses/{id} - Should return 200 OK and update the course")
    void updateCourse_ExistingCourse_Returns200AndUpdates() throws Exception {
        // Create an update DTO
        CourseController.CourseInputDto updateDto = new CourseController.CourseInputDto();
        updateDto.setName("Updated Course Name");
        updateDto.setDescription("Updated Description");
        updateDto.setCredit(5);
        updateDto.setAuthorIds(Collections.singletonList(savedAuthor2.getId())); // Change authors

        mockMvc.perform(put("/api/courses/{id}", savedCourse.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedCourse.getId().toString()))
                .andExpect(jsonPath("$.name").value("Updated Course Name"))
                .andExpect(jsonPath("$.authors", hasSize(1)))
                .andExpect(jsonPath("$.authors[0].name").value(savedAuthor2.getName()));

        // Verify persistence by fetching from the repository
        Course updatedCourse = courseJpaRepository.findById(savedCourse.getId())
                .orElseThrow(() -> new AssertionError("Course not found after update"));

        assertThat(updatedCourse.getName(), is("Updated Course Name"));
        assertThat(updatedCourse.getCredit(), is(5));
        assertThat(updatedCourse.getAuthors(), hasSize(1));
        assertThat(updatedCourse.getAuthors().getFirst().getName(), is(savedAuthor2.getName()));
    }

    /**
     * Test case for deleting an existing course via the API.
     */
    @Test
    @DisplayName("DELETE /api/courses/{id} - Should return 204 No Content and delete the course")
    void deleteCourse_ExistingCourse_Returns204AndDeletes() throws Exception {
        mockMvc.perform(delete("/api/courses/{id}", savedCourse.getId()))
                .andExpect(status().isNoContent());

        // Verify deletion by attempting to fetch from the repository
        assertTrue(courseJpaRepository.findById(savedCourse.getId()).isEmpty());
    }

    /**
     * Test case for retrieving all courses with pagination.
     */
    @Test
    @DisplayName("GET /api/courses - Should return 200 OK and a page of courses")
    void getAllCourses_Returns200AndPage() throws Exception {
        mockMvc.perform(get("/api/courses?page=0&size=10&sort=name,asc")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name").value(savedCourse.getName()))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    /**
     * Test case for retrieving recommended courses.
     */
    @Test
    @DisplayName("GET /api/courses/recommendations - Should return 200 OK and a page of all courses from DB")
    void discoverAllCourses_ReturnsRecommendedCoursesFromDB() throws Exception {
        mockMvc.perform(get("/api/courses/recommendations?page=0&size=10&sort=name,asc")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name").value(savedCourse.getName()))
                .andExpect(jsonPath("$.totalElements").value(1));
    }
}
    