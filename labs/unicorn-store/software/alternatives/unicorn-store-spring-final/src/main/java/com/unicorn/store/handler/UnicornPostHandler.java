package com.unicorn.store.handler;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unicorn.store.model.Unicorn;
import com.unicorn.store.service.UnicornService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class UnicornPostHandler implements Function<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final ObjectMapper objectMapper;
    private final UnicornService unicornService;
    private final Logger logger = LoggerFactory.getLogger(UnicornPostHandler.class);

    public UnicornPostHandler(UnicornService unicornService, ObjectMapper objetMapper) {
        this.unicornService = unicornService;
        this.objectMapper = objetMapper;
    }

    @Override
    public APIGatewayProxyResponseEvent apply(APIGatewayProxyRequestEvent input) {
        try {
            var unicorn = objectMapper.readValue(input.getBody(), Unicorn.class);
            var savedUnicorn = unicornService.createUnicorn(unicorn);
            return new APIGatewayProxyResponseEvent().withStatusCode(200).withBody(objectMapper.writeValueAsString(savedUnicorn));
        } catch (Exception e) {
            logger.error("Error creating unicorn", e);
            return new APIGatewayProxyResponseEvent().withStatusCode(500).withBody("Error creating unicorn");
        }
    }

}
