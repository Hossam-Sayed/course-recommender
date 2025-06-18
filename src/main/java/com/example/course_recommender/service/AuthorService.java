package com.example.course_recommender.service;

import com.example.course_recommender.dto.AuthorDto;
import com.example.course_recommender.mapper.AuthorMapper;
import com.example.course_recommender.model.Author;
import com.example.course_recommender.repository.AuthorJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Service layer for managing Author-related business logic.
 * This class orchestrates data operations by interacting with the AuthorRepository.
 */
@Service // Marks this class as a Spring service component
public class AuthorService {

    private final AuthorJpaRepository authorJpaRepository;
    private final AuthorMapper authorMapper;

    @Autowired
    public AuthorService(AuthorJpaRepository authorJpaRepository, AuthorMapper authorMapper) {
        this.authorJpaRepository = authorJpaRepository;
        this.authorMapper = authorMapper;
    }

    /**
     * Adds a new author to the system.
     *
     * @param authorDto The AuthorDto object to be added.
     * @return The added AuthorDto object with its generated ID.
     */
    @Transactional
    public AuthorDto addAuthor(AuthorDto authorDto) {
        System.out.println("Attempting to add author: " + authorDto.getName());
        if (authorJpaRepository.findByEmail(authorDto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Author with email " + authorDto.getEmail() + " already exists");
        }
        Author author = authorMapper.toEntity(authorDto);
        Author savedAuthor = authorJpaRepository.save(author);
        return authorMapper.toDto(savedAuthor);
    }

    /**
     * Updates an existing author.
     *
     * @param id        The UUID of the author to update.
     * @param authorDto The AuthorDto object with updated details.
     * @return An Optional containing the updated AuthorDto if found, otherwise Optional.empty().
     */
    public Optional<AuthorDto> updateAuthor(UUID id, AuthorDto authorDto) {
        System.out.println("Attempting to update author with ID: " + authorDto.getId());
        Optional<Author> existingAuthorOpt = authorJpaRepository.findById(id);

        if (existingAuthorOpt.isPresent()) {
            Author existingAuthor = existingAuthorOpt.get();

            if (authorDto.getEmail() != null && !authorDto.getEmail().equalsIgnoreCase(existingAuthor.getEmail())) {
                if (authorJpaRepository.findByEmail(authorDto.getEmail()).isPresent()) {
                    throw new IllegalArgumentException("Cannot update: Another author with email " + authorDto.getEmail() + " already exists.");
                }
            }

            authorMapper.updateEntityFromDto(authorDto, existingAuthor);
            Author updatedAuthor = authorJpaRepository.save(existingAuthor);
            return Optional.of(authorMapper.toDto(updatedAuthor));
        }
        System.out.println("No author found with ID: " + id + " to update.");
        return Optional.empty();
    }

    /**
     * Retrieves an author by its unique identifier.
     *
     * @param id The UUID of the author to retrieve.
     * @return An Optional containing the AuthorDto if found, otherwise Optional.empty().
     */
    @Transactional(readOnly = true)
    public Optional<AuthorDto> viewAuthor(UUID id) {
        System.out.println("Attempting to view author with ID: " + id);
        return authorJpaRepository.findById(id)
                .map(authorMapper::toDto);
    }

    /**
     * Retrieves all available authors.
     *
     * @return A list of all AuthorDto objects.
     */
    @Transactional(readOnly = true)
    public Page<AuthorDto> getAllAuthors(Pageable pageable) {
        Page<Author> authorPage = authorJpaRepository.findAll(pageable);
        return authorPage.map(authorMapper::toDto);
    }

    /**
     * Deletes an author by its unique identifier.
     *
     * @param id The UUID of the author to delete.
     * @return true if the author was successfully deleted, false otherwise.
     */
    @Transactional
    public boolean deleteAuthor(UUID id) {
        System.out.println("Attempting to delete author with ID: " + id);
        if (authorJpaRepository.existsById(id)) {
            authorJpaRepository.deleteById(id);
            System.out.println("Author with ID: " + id + " deleted successfully.");
            return true;
        } else {
            System.out.println("No author found with ID: " + id + " to delete.");
            return false;
        }
    }

    /**
     * Retrieves an author by their email.
     * This method directly uses the custom query method in AuthorJpaRepository.
     *
     * @param email The email of the author to retrieve.
     * @return An Optional containing the AuthorDto if found, otherwise Optional.empty().
     */
    @Transactional(readOnly = true)
    public Optional<AuthorDto> getAuthorByEmail(String email) {
        System.out.println("Attempting to find author by email: " + email);
        return authorJpaRepository.findByEmail(email)
                .map(authorMapper::toDto);
    }
}
