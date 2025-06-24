package com.example.course_recommender.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.UUID;

/**
 * Represents an Assessment entity in the database.
 * Mapped to the 'assessment' table, with a one-to-one relationship to Course.
 */
@Entity
@Table(name = "assessment")
@Data
@NoArgsConstructor
@ToString(exclude = "course")
public class Assessment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", unique = true, nullable = false)
    private Course course;
}
