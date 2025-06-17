package com.example.course_recommender.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * Represents a Course in the system.
 * Uses Lombok annotations for boilerplate code (getters, setters, constructors, toString).
 */
@Data // Generates getters, setters, toString, equals, hashCode
@NoArgsConstructor
public class Course {
    private UUID id;
    private String name;
    private String description;
    private int credit;
    private List<Author> authors;

    // Manual constructor for common use case (without authors initially)
    public Course(UUID id, String name, String description, int credit) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.credit = credit;
        // Authors will be set separately or fetched later
    }
}
