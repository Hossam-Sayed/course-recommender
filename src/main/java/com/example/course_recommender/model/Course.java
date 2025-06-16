package com.example.course_recommender.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Represents a Course in the system.
 */
@Data // Generates getters, setters, toString, equals, hashCode
@NoArgsConstructor // Generates a no-argument constructor
@AllArgsConstructor // Generates a constructor with all fields as arguments
public class Course {
    private UUID id;
    private String name;
    private String description;
    private int credit;
}
