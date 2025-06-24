package com.example.course_recommender.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents an Author entity in the database.
 * Mapped to the 'author' table.
 */
@Entity
@Table(name = "author")
@Data
@NoArgsConstructor
@ToString(exclude = "courses")
public class Author {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "birthdate")
    private LocalDate birthdate;

    @ManyToMany(mappedBy = "authors")
    private List<Course> courses = new ArrayList<>();
}
