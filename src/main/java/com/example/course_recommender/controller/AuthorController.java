package com.example.course_recommender.controller;

import com.example.course_recommender.dto.AuthorDto;
import com.example.course_recommender.service.AuthorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/authors")
public class AuthorController {

    private final AuthorService authorService;

    @Autowired
    public AuthorController(AuthorService authorService) {
        this.authorService = authorService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuthorDto> viewAuthor(@PathVariable UUID id) {
        Optional<AuthorDto> authorDto = authorService.viewAuthor(id);
        return authorDto.map(dto -> new ResponseEntity<>(dto, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping
    public ResponseEntity<Page<AuthorDto>> getAllAuthors(Pageable pageable) {
        Page<AuthorDto> authors = authorService.getAllAuthors(pageable);
        return new ResponseEntity<>(authors, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<AuthorDto> addAuthor(@RequestBody AuthorDto authorDto) {
        try {
            AuthorDto savedAuthor = authorService.addAuthor(authorDto);
            return new ResponseEntity<>(savedAuthor, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            System.err.println("Error adding author: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            System.err.println("Unexpected error adding author: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<AuthorDto> updateAuthor(@PathVariable UUID id, @RequestBody AuthorDto authorDto) {
        Optional<AuthorDto> updated = authorService.updateAuthor(id, authorDto);
        return updated.map(dto -> new ResponseEntity<>(dto, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAuthor(@PathVariable UUID id) {
        boolean deleted = authorService.deleteAuthor(id);
        return deleted ? new ResponseEntity<>(HttpStatus.NO_CONTENT)
                : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping("/email")
    public ResponseEntity<AuthorDto> getAuthorByEmail(@RequestParam String email) {
        Optional<AuthorDto> authorDto = authorService.getAuthorByEmail(email);
        return authorDto.map(dto -> new ResponseEntity<>(dto, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}
