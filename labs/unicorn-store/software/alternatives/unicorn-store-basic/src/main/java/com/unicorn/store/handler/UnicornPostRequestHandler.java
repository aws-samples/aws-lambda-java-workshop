package com.unicorn.store.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unicorn.store.data.UnicornPublisher;
import com.unicorn.store.data.UnicornRepository;
import com.unicorn.store.service.UnicornService;
import com.unicorn.store.utils.UnicornUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UnicornPostRequestHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static UnicornService unicornService = new UnicornService(new UnicornRepository(), new UnicornPublisher());
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(UnicornPostRequestHandler.class);

    public UnicornPostRequestHandler(){
    }

    public UnicornPostRequestHandler(UnicornRepository unicornRepository, UnicornPublisher unicornPublisher){
        unicornService = new UnicornService(unicornRepository, unicornPublisher);
    }

    /**
     * Handle POST requests to create a unicorn
     * Test with POST body: {"name": "Big Unicorn", "age": "Quite old", "type": "Beautiful", "size": "Very big"}
     * @param APIGatewayProxyRequestEvent
     * @param Context
     * @return API Gateway response with the created unicorn data
     */
    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, final Context context)  {
        try {
			var unicorn = unicornService.createUnicorn(UnicornUtils.getBodyUnicorn(event));
        	var response = objectMapper.writeValueAsString(unicorn);

            return new APIGatewayProxyResponseEvent().withStatusCode(200).withBody(response);
        } catch (JsonProcessingException e) {
        	logger.error("JSONException on handling event body", e);
        	return new APIGatewayProxyResponseEvent().withStatusCode(400).withBody("Error: the provided Request JSON body is invalid or incomplete");
        } catch (Exception e) {
            logger.error("Error handling request", e);
            return new APIGatewayProxyResponseEvent().withStatusCode(500).withBody("Error while handling the Request");
        }
    }

}
