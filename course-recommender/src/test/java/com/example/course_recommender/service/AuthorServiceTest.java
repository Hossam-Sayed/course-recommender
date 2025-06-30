package com.example.course_recommender.service;

import com.example.course_recommender.mapper.AuthorMapper;
import com.example.course_recommender.model.Author;
import com.example.course_recommender.repository.AuthorJpaRepository;
import com.example.course_recommender_bean.dto.AuthorDto;
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

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the AuthorService class.
 */
@ExtendWith(MockitoExtension.class)
class AuthorServiceTest {

    @Mock
    private AuthorJpaRepository authorJpaRepository;

    @Mock
    private AuthorMapper authorMapper;

    @InjectMocks
    private AuthorService authorService;

    private UUID authorId;
    private AuthorDto authorDto;
    private Author authorEntity;

    /**
     * Set up common test data before each test method runs.
     */
    @BeforeEach
    void setUp() {
        authorId = UUID.randomUUID();
        authorDto = new AuthorDto(authorId, "Test Author", "test@example.com", LocalDate.of(1990, 1, 1));
        authorEntity = new Author();
        authorEntity.setId(authorId);
        authorEntity.setName("Test Author");
        authorEntity.setEmail("test@example.com");
        authorEntity.setBirthdate(LocalDate.of(1990, 1, 1));
    }

    /**
     * Test case for successfully adding a new author.
     */
    @Test
    @DisplayName("Should successfully add a new author")
    void addAuthor_Success() {
        // Given: The authorDto and a mock behavior for findByEmail (author does not exist)
        when(authorJpaRepository.findByEmail(authorDto.getEmail())).thenReturn(Optional.empty());
        // When authorMapper.toEntity is called, return our pre-defined authorEntity
        when(authorMapper.toEntity(any(AuthorDto.class))).thenReturn(authorEntity);
        // When authorJpaRepository.save is called, return the same authorEntity
        when(authorJpaRepository.save(any(Author.class))).thenReturn(authorEntity);
        // When authorMapper.toDto is called, return our pre-defined authorDto
        when(authorMapper.toDto(any(Author.class))).thenReturn(authorDto);

        // When: Calling the addAuthor service method
        AuthorDto result = authorService.addAuthor(authorDto);

        // Then: Verify the result and mock interactions
        assertNotNull(result);
        assertEquals(authorDto.getId(), result.getId());
        assertEquals(authorDto.getEmail(), result.getEmail());

        verify(authorJpaRepository, times(1)).findByEmail(authorDto.getEmail()); // Verify email existence check
        verify(authorMapper, times(1)).toEntity(authorDto); // Verify DTO to Entity conversion
        verify(authorJpaRepository, times(1)).save(authorEntity); // Verify entity saving
        verify(authorMapper, times(1)).toDto(authorEntity); // Verify Entity to DTO conversion
    }

    /**
     * Test case for adding an author with an email that already exists.
     * Expects an IllegalArgumentException.
     */
    @Test
    @DisplayName("Should throw IllegalArgumentException when adding author with existing email")
    void addAuthor_ExistingEmail_ThrowsException() {
        // Given: An author with the same email already exists in the repository
        when(authorJpaRepository.findByEmail(authorDto.getEmail())).thenReturn(Optional.of(authorEntity));

        // When/Then: Assert that calling addAuthor throws IllegalArgumentException
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () ->
                authorService.addAuthor(authorDto)
        );

        // Verify the exception message
        assertEquals("Author with email " + authorDto.getEmail() + " already exists", thrown.getMessage());

        // Verify no further interactions with mapper or repository save method
        verify(authorJpaRepository, times(1)).findByEmail(authorDto.getEmail());
        verifyNoInteractions(authorMapper);
        verify(authorJpaRepository, never()).save(any(Author.class));
    }

    /**
     * Test case for successfully updating an existing author without changing email.
     */
    @Test
    @DisplayName("Should successfully update an existing author without changing email")
    void updateAuthor_Success_NoEmailChange() {
        // Given: An existing author and an update DTO with the same email
        AuthorDto updatedAuthorDto = new AuthorDto(authorId, "Updated Author", "test@example.com", LocalDate.of(1991, 2, 2));
        Author existingAuthor = new Author();
        existingAuthor.setId(authorId);
        existingAuthor.setName("Original Author");
        existingAuthor.setEmail("test@example.com"); // Same email
        existingAuthor.setBirthdate(LocalDate.of(1990, 1, 1));

        // When
        when(authorJpaRepository.findById(authorId)).thenReturn(Optional.of(existingAuthor));
        doNothing().when(authorMapper).updateEntityFromDto(any(AuthorDto.class), any(Author.class)); // Simulate MapStruct update
        when(authorJpaRepository.save(any(Author.class))).thenReturn(existingAuthor); // Return the (updated) existing entity
        when(authorMapper.toDto(any(Author.class))).thenReturn(updatedAuthorDto);

        Optional<AuthorDto> result = authorService.updateAuthor(authorId, updatedAuthorDto);

        // Then
        assertTrue(result.isPresent());
        assertEquals(updatedAuthorDto.getName(), result.get().getName());
        assertEquals(updatedAuthorDto.getEmail(), result.get().getEmail());
        assertEquals(updatedAuthorDto.getBirthdate(), result.get().getBirthdate());

        verify(authorJpaRepository, times(1)).findById(authorId);
        // Verify findByEmail for collision check was NOT called since email is same
        verify(authorJpaRepository, never()).findByEmail(anyString());
        verify(authorMapper, times(1)).updateEntityFromDto(updatedAuthorDto, existingAuthor);
        verify(authorJpaRepository, times(1)).save(existingAuthor);
        verify(authorMapper, times(1)).toDto(existingAuthor);
    }

    /**
     * Test case for successfully updating an existing author with a new, non-existent email.
     */
    @Test
    @DisplayName("Should successfully update an existing author with a new, non-existent email")
    void updateAuthor_Success_EmailChangeToNonExistent() {
        // Given: An existing author and an update DTO with a new email
        AuthorDto updatedAuthorDto = new AuthorDto(authorId, "Updated Author", "new@example.com", LocalDate.of(1991, 2, 2));
        Author existingAuthor = new Author();
        existingAuthor.setId(authorId);
        existingAuthor.setName("Original Author");
        existingAuthor.setEmail("old@example.com"); // Different email
        existingAuthor.setBirthdate(LocalDate.of(1990, 1, 1));

        // When
        when(authorJpaRepository.findById(authorId)).thenReturn(Optional.of(existingAuthor));
        // Mock findByEmail for the new email to return empty (meaning it's available)
        when(authorJpaRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        doNothing().when(authorMapper).updateEntityFromDto(any(AuthorDto.class), any(Author.class));
        when(authorJpaRepository.save(any(Author.class))).thenReturn(existingAuthor);
        when(authorMapper.toDto(any(Author.class))).thenReturn(updatedAuthorDto);

        Optional<AuthorDto> result = authorService.updateAuthor(authorId, updatedAuthorDto);

        // Then
        assertTrue(result.isPresent());
        assertEquals(updatedAuthorDto.getName(), result.get().getName());
        assertEquals(updatedAuthorDto.getEmail(), result.get().getEmail());

        verify(authorJpaRepository, times(1)).findById(authorId);
        verify(authorJpaRepository, times(1)).findByEmail("new@example.com"); // Verify email collision check for new email
        verify(authorMapper, times(1)).updateEntityFromDto(updatedAuthorDto, existingAuthor);
        verify(authorJpaRepository, times(1)).save(existingAuthor);
        verify(authorMapper, times(1)).toDto(existingAuthor);
    }

    /**
     * Test case for updating an existing author with an email that already belongs to another author.
     * Expects an IllegalArgumentException.
     */
    @Test
    @DisplayName("Should throw IllegalArgumentException when updating author email to an existing email")
    void updateAuthor_EmailChangeToExisting_ThrowsException() {
        // Given: An existing author to update, and another existing author with the target email
        AuthorDto updatedAuthorDto = new AuthorDto(authorId, "Updated Author", "another@example.com", LocalDate.of(1991, 2, 2));
        Author existingAuthor = new Author();
        existingAuthor.setId(authorId);
        existingAuthor.setName("Original Author");
        existingAuthor.setEmail("original@example.com");

        Author anotherAuthorWithSameEmail = new Author();
        anotherAuthorWithSameEmail.setId(UUID.randomUUID()); // Different ID
        anotherAuthorWithSameEmail.setEmail("another@example.com");

        // When
        when(authorJpaRepository.findById(authorId)).thenReturn(Optional.of(existingAuthor));
        // Mock findByEmail for the new email to return an existing author
        when(authorJpaRepository.findByEmail("another@example.com")).thenReturn(Optional.of(anotherAuthorWithSameEmail));

        // Then
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () ->
                authorService.updateAuthor(authorId, updatedAuthorDto)
        );

        assertEquals("Cannot update: Another author with email another@example.com already exists.", thrown.getMessage());

        verify(authorJpaRepository, times(1)).findById(authorId);
        verify(authorJpaRepository, times(1)).findByEmail("another@example.com");
        verify(authorMapper, never()).updateEntityFromDto(any(AuthorDto.class), any(Author.class));
        verify(authorJpaRepository, never()).save(any(Author.class));
        verify(authorMapper, never()).toDto(any(Author.class));
    }

    /**
     * Test case for updating a non-existent author.
     * Expects Optional.empty().
     */
    @Test
    @DisplayName("Should return Optional.empty when updating a non-existent author")
    void updateAuthor_NotFound() {
        // Given: An ID for a non-existent author
        AuthorDto updatedAuthorDto = new AuthorDto(UUID.randomUUID(), "Non Existent", "nonexistent@example.com", LocalDate.now());
        when(authorJpaRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        // When
        Optional<AuthorDto> result = authorService.updateAuthor(UUID.randomUUID(), updatedAuthorDto);

        // Then
        assertFalse(result.isPresent());
        verify(authorJpaRepository, times(1)).findById(any(UUID.class));
        verifyNoMoreInteractions(authorJpaRepository, authorMapper); // Ensure no other interactions
    }

    /**
     * Test case for retrieving an existing author by ID.
     */
    @Test
    @DisplayName("Should return AuthorDto when viewing an existing author")
    void viewAuthor_Found() {
        // Given
        when(authorJpaRepository.findById(authorId)).thenReturn(Optional.of(authorEntity));
        when(authorMapper.toDto(any(Author.class))).thenReturn(authorDto);

        // When
        Optional<AuthorDto> result = authorService.viewAuthor(authorId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(authorDto, result.get());
        verify(authorJpaRepository, times(1)).findById(authorId);
        verify(authorMapper, times(1)).toDto(authorEntity);
    }

    /**
     * Test case for retrieving a non-existent author by ID.
     */
    @Test
    @DisplayName("Should return Optional.empty when viewing a non-existent author")
    void viewAuthor_NotFound() {
        // Given
        when(authorJpaRepository.findById(authorId)).thenReturn(Optional.empty());

        // When
        Optional<AuthorDto> result = authorService.viewAuthor(authorId);

        // Then
        assertFalse(result.isPresent());
        verify(authorJpaRepository, times(1)).findById(authorId);
        verifyNoInteractions(authorMapper);
    }

    /**
     * Test case for retrieving all authors with pagination.
     */
    @Test
    @DisplayName("Should return a Page of AuthorDto when getting all authors")
    void getAllAuthors_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<Author> authors = Collections.singletonList(authorEntity);
        Page<Author> authorPage = new PageImpl<>(authors, pageable, authors.size());

        when(authorJpaRepository.findAll(pageable)).thenReturn(authorPage);
        when(authorMapper.toDto(any(Author.class))).thenReturn(authorDto);

        // When
        Page<AuthorDto> result = authorService.getAllAuthors(pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(authorDto, result.getContent().getFirst());
        verify(authorJpaRepository, times(1)).findAll(pageable);
        verify(authorMapper, times(authors.size())).toDto(any(Author.class));
    }

    /**
     * Test case for successfully deleting an existing author.
     */
    @Test
    @DisplayName("Should return true when deleting an existing author")
    void deleteAuthor_Found() {
        // Given
        when(authorJpaRepository.existsById(authorId)).thenReturn(true);
        doNothing().when(authorJpaRepository).deleteById(authorId);

        // When
        boolean deleted = authorService.deleteAuthor(authorId);

        // Then
        assertTrue(deleted);
        verify(authorJpaRepository, times(1)).existsById(authorId);
        verify(authorJpaRepository, times(1)).deleteById(authorId);
    }

    /**
     * Test case for attempting to delete a non-existent author.
     */
    @Test
    @DisplayName("Should return false when deleting a non-existent author")
    void deleteAuthor_NotFound() {
        // Given
        when(authorJpaRepository.existsById(authorId)).thenReturn(false);

        // When
        boolean deleted = authorService.deleteAuthor(authorId);

        // Then
        assertFalse(deleted);
        verify(authorJpaRepository, times(1)).existsById(authorId);
        verify(authorJpaRepository, never()).deleteById(any(UUID.class));
    }

    /**
     * Test case for retrieving an existing author by email.
     */
    @Test
    @DisplayName("Should return AuthorDto when finding author by existing email")
    void getAuthorByEmail_Found() {
        // Given
        when(authorJpaRepository.findByEmail(authorDto.getEmail())).thenReturn(Optional.of(authorEntity));
        when(authorMapper.toDto(any(Author.class))).thenReturn(authorDto);

        // When
        Optional<AuthorDto> result = authorService.getAuthorByEmail(authorDto.getEmail());

        // Then
        assertTrue(result.isPresent());
        assertEquals(authorDto, result.get());
        verify(authorJpaRepository, times(1)).findByEmail(authorDto.getEmail());
        verify(authorMapper, times(1)).toDto(authorEntity);
    }

    /**
     * Test case for attempting to retrieve a non-existent author by email.
     */
    @Test
    @DisplayName("Should return Optional.empty when finding author by non-existent email")
    void getAuthorByEmail_NotFound() {
        // Given
        when(authorJpaRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // When
        Optional<AuthorDto> result = authorService.getAuthorByEmail("nonexistent@example.com");

        // Then
        assertFalse(result.isPresent());
        verify(authorJpaRepository, times(1)).findByEmail("nonexistent@example.com");
        verifyNoInteractions(authorMapper);
    }
}
