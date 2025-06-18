package com.example.course_recommender.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a Course entity in the database.
 * Mapped to the 'course' table.
 */
@Entity
@Table(name = "course")
@Data
@NoArgsConstructor
@ToString(exclude = {"authors", "assessment", "ratings"})
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "credit", nullable = false)
    private int credit;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "course_authors",
            joinColumns = @JoinColumn(name = "course_id"),
            inverseJoinColumns = @JoinColumn(name = "author_id")
    )
    @OrderBy("name ASC")
    private List<Author> authors = new ArrayList<>();

    @OneToOne(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Assessment assessment;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Rating> ratings = new ArrayList<>();

    public Course(UUID id, String name, String description, int credit) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.credit = credit;
    }

    // Helper methods to manage bidirectional relationships (important for OneToMany/OneToOne)
    public void addRating(Rating rating) {
        ratings.add(rating);
        rating.setCourse(this);
    }

    public void removeRating(Rating rating) {
        ratings.remove(rating);
        rating.setCourse(null);
    }

    public void setAssessment(Assessment assessment) {
        if (this.assessment != null) {
            this.assessment.setCourse(null);
        }
        this.assessment = assessment;
        if (assessment != null) {
            assessment.setCourse(this);
        }
    }
}
