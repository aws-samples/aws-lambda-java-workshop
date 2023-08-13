package com.unicorn.store.handler;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.unicorn.store.service.UnicornService;
import io.micronaut.function.aws.MicronautRequestHandler;
import io.micronaut.json.JsonMapper;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//@Introspected
public class UnicornPostRequestHandler extends MicronautRequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    @Inject
    private UnicornService unicornService;

    @Inject
    private JsonMapper objectMapper;

    private static final Logger logger = LoggerFactory.getLogger(UnicornPostRequestHandler.class);


    @Override
    public APIGatewayProxyResponseEvent execute(APIGatewayProxyRequestEvent input) {
        logger.info("Message!!!");
        try {
            logger.info(input.getBody());
           // var unicorn = objectMapper.readValue(input.getBody(), Unicorn.class);
          //  var savedUnicorn = unicornService.createUnicorn(unicorn);
            return new APIGatewayProxyResponseEvent().withStatusCode(200).withBody(objectMapper.writeValueAsString("Done"));
        } catch (Exception e) {
            logger.error("Error creating unicorn", e);
            return new APIGatewayProxyResponseEvent().withStatusCode(500).withBody("Error creating unicorn");
        }
    }
}