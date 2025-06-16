package com.example.course_recommender.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Represents an Assessment for a Course (one-to-one relationship).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Assessment {
    private UUID id;
    private String content;
    private UUID courseId; // Foreign key to Course table
}
