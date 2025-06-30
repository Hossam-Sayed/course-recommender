package com.example.course_recommender.controller;

import com.example.course_recommender.service.AuthorService;
import com.example.course_recommender_bean.dto.AuthorDto;
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

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for the AuthorController class.
 * This class uses @WebMvcTest to test the controller layer in isolation,
 * mocking the AuthorService dependency.
 */
@WebMvcTest(AuthorController.class)
class AuthorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthorService authorService;

    private UUID authorId;
    private AuthorDto authorDto;

    /**
     * Set up common test data before each test method runs.
     */
    @BeforeEach
    void setUp() {
        authorId = UUID.randomUUID();
        // AuthorDto for expected service responses and request bodies
        authorDto = new AuthorDto(authorId, "Test Author", "test@example.com", LocalDate.of(1990, 1, 1));
    }

    /**
     * Test case for successfully viewing an author by ID.
     * Expects HTTP 200 OK and the AuthorDto.
     */
    @Test
    @DisplayName("GET /api/authors/{id} - Should return 200 OK and AuthorDto when author found")
    void viewAuthor_Found_Returns200AndAuthorDto() throws Exception {
        // Given
        when(authorService.viewAuthor(authorId)).thenReturn(Optional.of(authorDto));

        // When & Then
        mockMvc.perform(get("/api/authors/{id}", authorId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(authorId.toString()))
                .andExpect(jsonPath("$.name").value("Test Author"));

        verify(authorService, times(1)).viewAuthor(authorId); // Verify service method was called
    }

    /**
     * Test case for viewing a non-existent author by ID.
     * Expects HTTP 404 Not Found.
     */
    @Test
    @DisplayName("GET /api/authors/{id} - Should return 404 Not Found when author not found")
    void viewAuthor_NotFound_Returns404() throws Exception {
        // Given
        when(authorService.viewAuthor(authorId)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/authors/{id}", authorId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(authorService, times(1)).viewAuthor(authorId);
    }

    /**
     * Test case for successfully adding a new author.
     * Expects HTTP 201 Created and the created AuthorDto.
     */
    @Test
    @DisplayName("POST /api/authors - Should return 201 Created and AuthorDto when author added successfully")
    void addAuthor_Success_Returns201AndAuthorDto() throws Exception {
        // Given
        AuthorDto authorDtoToSave = new AuthorDto(null, "New Author", "new@example.com", LocalDate.of(1995, 3, 3));
        AuthorDto savedAuthorDto = new AuthorDto(UUID.randomUUID(), "New Author", "new@example.com", LocalDate.of(1995, 3, 3));
        when(authorService.addAuthor(any(AuthorDto.class))).thenReturn(savedAuthorDto);

        // When & Then
        mockMvc.perform(post("/api/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authorDtoToSave)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("New Author"));

        // Verify that addAuthor was called with an AuthorDto containing the correct details
        verify(authorService, times(1)).addAuthor(argThat(dto ->
                dto.getName().equals("New Author") &&
                        dto.getEmail().equals("new@example.com") &&
                        dto.getBirthdate().equals(LocalDate.of(1995, 3, 3))
        ));
    }

    /**
     * Test case for adding an author with an email that already exists.
     * Expects HTTP 400 Bad Request.
     */
    @Test
    @DisplayName("POST /api/authors - Should return 400 Bad Request when service throws IllegalArgumentException")
    void addAuthor_IllegalArgument_Returns400() throws Exception {
        // Given
        AuthorDto authorDtoToSave = new AuthorDto(null, "Existing Email User", "test@example.com", LocalDate.of(1980, 1, 1));
        when(authorService.addAuthor(any(AuthorDto.class)))
                .thenThrow(new IllegalArgumentException("Author with email test@example.com already exists"));

        // When & Then
        mockMvc.perform(post("/api/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authorDtoToSave)))
                .andExpect(status().isBadRequest());

        verify(authorService, times(1)).addAuthor(any(AuthorDto.class));
    }

    /**
     * Test case for an unexpected error when adding an author.
     * Expects HTTP 500 Internal Server Error.
     */
    @Test
    @DisplayName("POST /api/authors - Should return 500 Internal Server Error for unexpected exception")
    void addAuthor_UnexpectedError_Returns500() throws Exception {
        // Given
        AuthorDto authorDtoToSave = new AuthorDto(null, "Error User", "error@example.com", LocalDate.of(1980, 1, 1));
        when(authorService.addAuthor(any(AuthorDto.class)))
                .thenThrow(new RuntimeException("Database connection lost")); // Simulate an unexpected error

        // When & Then
        mockMvc.perform(post("/api/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authorDtoToSave)))
                .andExpect(status().isInternalServerError());

        verify(authorService, times(1)).addAuthor(any(AuthorDto.class));
    }


    /**
     * Test case for successfully updating an existing author.
     * Expects HTTP 200 OK and the updated AuthorDto.
     */
    @Test
    @DisplayName("PUT /api/authors/{id} - Should return 200 OK and AuthorDto when author updated successfully")
    void updateAuthor_Success_Returns200AndAuthorDto() throws Exception {
        // Given
        AuthorDto updatedAuthorDto = new AuthorDto(authorId, "Updated Name", "updated@example.com", LocalDate.of(1991, 2, 2));
        when(authorService.updateAuthor(eq(authorId), any(AuthorDto.class))).thenReturn(Optional.of(updatedAuthorDto));

        // When & Then
        mockMvc.perform(put("/api/authors/{id}", authorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedAuthorDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(authorId.toString()))
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.email").value("updated@example.com"));

        verify(authorService, times(1)).updateAuthor(
                eq(authorId), // Matches the exact UUID
                argThat(dto -> dto.getName().equals("Updated Name") &&
                        dto.getEmail().equals("updated@example.com") &&
                        dto.getBirthdate().equals(LocalDate.of(1991, 2, 2))
                )
        );
    }

    /**
     * Test case for updating a non-existent author.
     * Expects HTTP 404 Not Found.
     */
    @Test
    @DisplayName("PUT /api/authors/{id} - Should return 404 Not Found when updating non-existent author")
    void updateAuthor_NotFound_Returns404() throws Exception {
        // Given
        AuthorDto updatedAuthorDto = new AuthorDto(authorId, "Updated Name", "updated@example.com", LocalDate.of(1991, 2, 2));
        when(authorService.updateAuthor(eq(authorId), any(AuthorDto.class))).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(put("/api/authors/{id}", authorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedAuthorDto)))
                .andExpect(status().isNotFound());

        verify(authorService, times(1)).updateAuthor(eq(authorId), any(AuthorDto.class));
    }

    /**
     * Test case for updating an author when service throws IllegalArgumentException (e.g., email conflict).
     * Expects HTTP 400 Bad Request.
     */
    @Test
    @DisplayName("PUT /api/authors/{id} - Should return 400 Bad Request when service throws IllegalArgumentException")
    void updateAuthor_IllegalArgument_Returns400() throws Exception {
        // Given
        AuthorDto updatedAuthorDto = new AuthorDto(authorId, "Updated Name", "conflict@example.com", LocalDate.of(1991, 2, 2));
        when(authorService.updateAuthor(eq(authorId), any(AuthorDto.class)))
                .thenThrow(new IllegalArgumentException("Cannot update: Another author with email conflict@example.com already exists."));

        // When & Then
        mockMvc.perform(put("/api/authors/{id}", authorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedAuthorDto)))
                .andExpect(status().isBadRequest());

        verify(authorService, times(1)).updateAuthor(eq(authorId), any(AuthorDto.class));
    }


    /**
     * Test case for successfully deleting an author.
     * Expects HTTP 204 No Content.
     */
    @Test
    @DisplayName("DELETE /api/authors/{id} - Should return 204 No Content when author deleted successfully")
    void deleteAuthor_Success_Returns204() throws Exception {
        // Given
        when(authorService.deleteAuthor(authorId)).thenReturn(true);

        // When & Then
        mockMvc.perform(delete("/api/authors/{id}", authorId))
                .andExpect(status().isNoContent());

        verify(authorService, times(1)).deleteAuthor(authorId);
    }

    /**
     * Test case for deleting a non-existent author.
     * Expects HTTP 404 Not Found.
     */
    @Test
    @DisplayName("DELETE /api/authors/{id} - Should return 404 Not Found when deleting non-existent author")
    void deleteAuthor_NotFound_Returns404() throws Exception {
        // Given
        when(authorService.deleteAuthor(authorId)).thenReturn(false);

        // When & Then
        mockMvc.perform(delete("/api/authors/{id}", authorId))
                .andExpect(status().isNotFound());

        verify(authorService, times(1)).deleteAuthor(authorId);
    }

    /**
     * Test case for getting all authors with pagination.
     * Expects HTTP 200 OK and a Page of AuthorDto.
     */
    @Test
    @DisplayName("GET /api/authors - Should return 200 OK and a Page of AuthorDto")
    void getAllAuthors_Success_Returns200AndPage() throws Exception {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<AuthorDto> authorDtos = Collections.singletonList(authorDto);
        Page<AuthorDto> authorPage = new PageImpl<>(authorDtos, pageable, authorDtos.size());

        when(authorService.getAllAuthors(any(Pageable.class))).thenReturn(authorPage);

        // When & Then
        mockMvc.perform(get("/api/authors?page=0&size=10&sort=name,asc")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(authorId.toString()))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(authorService, times(1)).getAllAuthors(any(Pageable.class));
    }

    /**
     * Test case for retrieving an author by email.
     * Expects HTTP 200 OK and AuthorDto.
     */
    @Test
    @DisplayName("GET /api/authors/email?email=test@example.com - Should return 200 OK and AuthorDto when found by email")
    void getAuthorByEmail_Found_Returns200AndAuthorDto() throws Exception {
        // Given
        when(authorService.getAuthorByEmail(authorDto.getEmail())).thenReturn(Optional.of(authorDto));

        // When & Then
        mockMvc.perform(get("/api/authors/email")
                        .param("email", authorDto.getEmail()) // Use .param for query parameters
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(authorId.toString()))
                .andExpect(jsonPath("$.email").value(authorDto.getEmail()));

        verify(authorService, times(1)).getAuthorByEmail(authorDto.getEmail());
    }

    /**
     * Test case for retrieving a non-existent author by email.
     * Expects HTTP 404 Not Found.
     */
    @Test
    @DisplayName("GET /api/authors/email?email=nonexistent@example.com - Should return 404 Not Found when not found by email")
    void getAuthorByEmail_NotFound_Returns404() throws Exception {
        // Given
        String nonExistentEmail = "nonexistent@example.com";
        when(authorService.getAuthorByEmail(nonExistentEmail)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/authors/email")
                        .param("email", nonExistentEmail)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(authorService, times(1)).getAuthorByEmail(nonExistentEmail);
    }
}
