package com.example.course_recommender.config;

import com.example.course_recommender.soap.generated.ObjectFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;

/**
 * Configuration class for the SOAP client.
 * Defines beans for JAXB marshalling/unmarshalling and the WebServiceTemplate.
 */
@Configuration
public class SoapClientConfig {

    // Inject the URL of the SOAP mock service from application.properties
    @Value("${soap.mock.service.url}")
    private String defaultUri;

    /**
     * Configures the JAXB2Marshaller.
     * This marshaller is responsible for converting Java objects to XML (marshalling)
     * and XML to Java objects (unmarshalling) based on the JAXB-generated classes.
     * The contextPath should point to the package where the JAXB classes were generated.
     *
     * @return A configured Jaxb2Marshaller instance.
     */
    @Bean
    public Jaxb2Marshaller marshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setContextPath("com.example.course_recommender.soap.generated");
        return marshaller;
    }

    /**
     * Configures the WebServiceTemplate.
     * This is the central class for client-side SOAP interactions in Spring-WS.
     * It uses the configured marshaller for XML-Java conversions and the default URI
     * to know where to send the SOAP requests.
     *
     * @param marshaller The Jaxb2Marshaller bean.
     * @return A configured WebServiceTemplate instance.
     */
    @Bean
    public WebServiceTemplate webServiceTemplate(Jaxb2Marshaller marshaller) {
        WebServiceTemplate webServiceTemplate = new WebServiceTemplate();
        webServiceTemplate.setMarshaller(marshaller);
        webServiceTemplate.setUnmarshaller(marshaller); // Use the same marshaller for unmarshalling
        webServiceTemplate.setDefaultUri(defaultUri); // Set the default URI for the SOAP service
        // For SOAP 1.2 to configure the SOAP message factory
        // webServiceTemplate.setMessageFactory(soapMessageFactory());
        return webServiceTemplate;
    }

    // A specific SOAP message factory (if needed)
    // @Bean
    // public SaajSoapMessageFactory soapMessageFactory() {
    //     return new SaajSoapMessageFactory();
    // }
}
