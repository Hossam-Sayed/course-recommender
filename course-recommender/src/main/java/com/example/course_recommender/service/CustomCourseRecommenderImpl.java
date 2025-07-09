package com.example.course_recommender.service;

import com.example.course_recommender.mapper.SoapCourseMapper;
import com.example.course_recommender.soap.client.CourseRecommendationSoapClient;
import com.example.course_recommender.soap.generated.GetRecommendedCoursesResponse;
import com.example.course_recommender_bean.dto.CourseDto;
import com.example.course_recommender_bean.service.CourseRecommenderImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Custom implementation of the CourseRecommender interface that
 * retrieves recommended courses from an external SOAP service.
 */
@Slf4j
@Service
public class CustomCourseRecommenderImpl extends CourseRecommenderImpl {

    private final CourseRecommendationSoapClient soapClient;
    private final SoapCourseMapper soapCourseMapper;

    @Autowired
    public CustomCourseRecommenderImpl(
            CourseRecommendationSoapClient soapClient,
            SoapCourseMapper soapCourseMapper) {
        this.soapClient = soapClient;
        this.soapCourseMapper = soapCourseMapper;
    }

    /**
     * Provides a paginated list of recommended courses by calling an external SOAP service.
     *
     * @param pageable Pagination information.
     * @return A Page of CourseDto objects.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<CourseDto> recommendedCourses(Pageable pageable) {
        log.info("Executing recommendedCourses: Calling external SOAP service for recommendations.");

        try {
            // Call the SOAP client
            GetRecommendedCoursesResponse soapResponse = soapClient.getRecommendedCourses(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    pageable.getSort().toString() // Convert Spring Data Sort to String
            );

            // Check if response contains courses
            if (soapResponse != null && soapResponse.getCourses() != null && soapResponse.getCourses().getCourse() != null) {
                List<CourseDto> recommendedCourses = soapResponse.getCourses().getCourse().stream()
                        .map(soapCourseMapper::toDto) // Map generated SOAP type to CourseDto
                        .collect(Collectors.toList());

                // Create a PageImpl from the list and pagination info
                return new PageImpl<>(
                        recommendedCourses,
                        pageable,
                        soapResponse.getTotalElements() // Use total elements from SOAP response
                );
            } else {
                log.warn("SOAP service returned no recommended courses or an empty response.");
                return new PageImpl<>(Collections.emptyList(), pageable, 0);
            }
        } catch (Exception e) {
            log.error("Error calling SOAP recommendation service: {}", e.getMessage(), e);
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }
    }
}
