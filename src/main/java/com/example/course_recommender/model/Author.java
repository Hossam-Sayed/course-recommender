package com.example.course_recommender.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Represents an Author in the system.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Author {
    private UUID id;
    private String name;
    private String email;
    private LocalDate birthdate;
}
