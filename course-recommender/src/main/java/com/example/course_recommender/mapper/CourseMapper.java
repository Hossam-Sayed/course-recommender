package com.example.course_recommender.mapper;

import com.example.course_recommender.model.Course;
import com.example.course_recommender_bean.dto.CourseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

/**
 * MapStruct Mapper for converting between Course entity and CourseDto.
 */
@Mapper(componentModel = "spring", uses = {AuthorMapper.class})
public interface CourseMapper {

    // Map a Course entity to a CourseDto
    CourseDto toDto(Course course);

    // Map a CourseDto to a Course entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "authors", ignore = true)
    @Mapping(target = "assessment", ignore = true)
    @Mapping(target = "ratings", ignore = true)
    Course toEntity(CourseDto courseDto);

    // Update an existing Course entity from a CourseDto
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "authors", ignore = true)
    @Mapping(target = "assessment", ignore = true)
    @Mapping(target = "ratings", ignore = true)
    void updateEntityFromDto(CourseDto courseDto, @MappingTarget Course course);

    // Map a list of Course entities to a list of CourseDtos
    List<CourseDto> toDtoList(List<Course> courses);

    // Map a list of CourseDtos to a list of Course entities
    List<Course> toEntityList(List<CourseDto> courseDtos);
}
