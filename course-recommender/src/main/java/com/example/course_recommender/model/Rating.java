package com.example.course_recommender.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.UUID;

/**
 * Represents a Rating entity in the database.
 * Mapped to the 'rating' table, with a many-to-one relationship to Course.
 */
@Entity
@Table(name = "rating")
@Data
@NoArgsConstructor
@ToString(exclude = "course")
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "number", nullable = false)
    private int number; // Rating value

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;
}
