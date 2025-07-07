package com.example.course_recommender.soap.client;

import com.example.course_recommender.soap.generated.GetRecommendedCoursesRequest;
import com.example.course_recommender.soap.generated.GetRecommendedCoursesResponse;
import com.example.course_recommender.soap.generated.ObjectFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.ws.client.core.WebServiceTemplate;

/**
 * SOAP client for interacting with the Course Recommendation service.
 * Uses WebServiceTemplate to send SOAP requests and receive responses.
 */
@Slf4j
@Component
public class CourseRecommendationSoapClient {

    private final WebServiceTemplate webServiceTemplate;
    private final ObjectFactory objectFactory;

    /**
     * Constructor for CourseRecommendationSoapClient.
     * Spring injects the WebServiceTemplate and creates an instance of ObjectFactory.
     *
     * @param webServiceTemplate The WebServiceTemplate bean.
     */
    @Autowired
    public CourseRecommendationSoapClient(WebServiceTemplate webServiceTemplate) {
        this.webServiceTemplate = webServiceTemplate;
        this.objectFactory = new ObjectFactory();
    }

    /**
     * Calls the SOAP service to get recommended courses.
     *
     * @param page The page number for pagination.
     * @param size The page size for pagination.
     * @param sort The sort string for pagination.
     * @return The GetRecommendedCoursesResponse object received from the SOAP service.
     */
    public GetRecommendedCoursesResponse getRecommendedCourses(Integer page, Integer size, String sort) {
        // Create the SOAP request object using the generated ObjectFactory
        GetRecommendedCoursesRequest request = objectFactory.createGetRecommendedCoursesRequest();
        request.setPage(page);
        request.setSize(size);
        request.setSort(sort);

        log.info("Sending SOAP request to get recommended courses. Page: {}, Size: {}, Sort: {}", page, size, sort);

        // Send the SOAP request and receive the response.
        // The marshaller/unmarshaller configured in SoapClientConfig will handle XML-Java conversion.
        GetRecommendedCoursesResponse response = (GetRecommendedCoursesResponse) webServiceTemplate.marshalSendAndReceive(request);

        log.info("Received SOAP response for recommended courses. Total elements: {}", response.getCourses() != null ? response.getCourses().getCourse().size() : 0);

        return response;
    }
}
