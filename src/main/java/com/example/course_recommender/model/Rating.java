package com.example.course_recommender.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Represents a Rating for a Course (one-to-many relationship).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Rating {
    private UUID id;
    private int number; // The rating value
    private UUID courseId; // Foreign key to Course table
}
