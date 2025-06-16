package com.example.course_recommender_api.model;

public class Course {
    private String id;
    private String title;
    private String level;

    public Course(String id, String title, String level) {
        this.id = id;
        this.title = title;
        this.level = level;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getLevel() {
        return level;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    @Override
    public String toString() {
        return "Course{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", level='" + level + '\'' +
                '}';
    }
}