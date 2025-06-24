package com.example.course_recommender_bean.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Data Transfer Object (DTO) for Author entity.
 * Used for sending author data to the client via REST API.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthorDto {
    private UUID id;
    private String name;
    private String email;
    private LocalDate birthdate;
}
