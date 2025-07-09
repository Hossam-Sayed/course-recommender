package com.example.course_recommender.mapper;

import com.example.course_recommender.dto.RecommendedCourseDto;
import com.example.course_recommender.soap.generated.CourseWithAdditionalFieldType;
import com.example.course_recommender.soap.generated.AuthorType;
import com.example.course_recommender_bean.dto.AuthorDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.UUID;

/**
 * MapStruct mapper for converting SOAP-generated CourseWithAdditionalFieldType
 * objects to RecommendedCourseDto objects.
 */
@Mapper(componentModel = "spring")
public interface SoapCourseMapper {

    // Map the list of AuthorType to AuthorDto
    @Mapping(source = "id", target = "id", qualifiedByName = "stringToUuid")
    @Mapping(source = "authors.author", target = "authors")
    @Mapping(source = "recommendationScore", target = "recommendationScore")
    @Mapping(source = "recommendedBy", target = "recommendedBy")
    RecommendedCourseDto toDto(CourseWithAdditionalFieldType soapCourse);

    @Mapping(source = "id", target = "id", qualifiedByName = "stringToUuid")
    AuthorDto toAuthorDto(AuthorType soapAuthor);

    List<AuthorDto> toAuthorDtoList(List<AuthorType> soapAuthors);

    // Custom mapping method for UUID conversion
    @Named("stringToUuid")
    default UUID stringToUuid(String uuidString) {
        return uuidString != null ? UUID.fromString(uuidString) : null;
    }
}
