package com.example.course_recommender.mapper;

import com.example.course_recommender.model.Author;
import com.example.course_recommender_bean.dto.AuthorDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

/**
 * MapStruct Mapper for converting between Author entity and AuthorDto.
 */
@Mapper(componentModel = "spring")
public interface AuthorMapper {

    // Map an Author entity to an AuthorDto
    AuthorDto toDto(Author author);

    // Map an AuthorDto to an Author entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "courses", ignore = true)
    Author toEntity(AuthorDto authorDto);

    // Update an existing Author entity from an AuthorDto
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "courses", ignore = true)
    void updateEntityFromDto(AuthorDto authorDto, @MappingTarget Author author);

    // Map a list of Author entities to a list of AuthorDtos
    List<AuthorDto> toDtoList(List<Author> authors);

    // Map a list of AuthorDtos to a list of Author entities
    List<Author> toEntityList(List<AuthorDto> authorDtos);
}
