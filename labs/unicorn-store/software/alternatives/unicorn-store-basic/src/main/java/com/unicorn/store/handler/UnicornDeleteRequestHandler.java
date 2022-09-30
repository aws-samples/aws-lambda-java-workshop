package com.unicorn.store.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unicorn.store.data.UnicornPublisher;
import com.unicorn.store.data.UnicornRepository;
import com.unicorn.store.exceptions.MissingParameterException;
import com.unicorn.store.exceptions.ResourceNotFoundException;
import com.unicorn.store.service.UnicornService;
import com.unicorn.store.utils.UnicornUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UnicornDeleteRequestHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static UnicornService unicornService = new UnicornService(new UnicornRepository(), new UnicornPublisher());
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(UnicornDeleteRequestHandler.class);

    public UnicornDeleteRequestHandler(){
    }

    public UnicornDeleteRequestHandler(UnicornRepository unicornRepository, UnicornPublisher unicornPublisher){
        unicornService = new UnicornService(unicornRepository, unicornPublisher);
    }

    /**
     * Handle DELETE requests to delete a unicorn.
     * Expects {id} as a path parameter
     * @param APIGatewayProxyRequestEvent
     * @param Context
     * @return API Gateway response with confirmation of deleted unicorn
     */
    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, final Context context)  {
        try {
			var unicornId = UnicornUtils.getUnicornParameter(event);
			unicornService.deleteUnicorn(unicornId);

			var response = objectMapper.writeValueAsString("Unicorn " + unicornId + " was deleted");
            return new APIGatewayProxyResponseEvent().withStatusCode(200).withBody(response);
        } catch (MissingParameterException e) {
        	logger.error("MissingParameterException: no unicorn ID provided via Path Parameters", e);
        	return new APIGatewayProxyResponseEvent().withStatusCode(400).withBody("Error: please provide a Unicorn ID");
        } catch (ResourceNotFoundException e) {
        	logger.error("ResourceNotFoundException: ", e);
            return new APIGatewayProxyResponseEvent().withStatusCode(404).withBody("Error: the provided unicorn does not exist");
        } catch (Exception e) {
            logger.error("Error handling request: ", e);
            return new APIGatewayProxyResponseEvent().withStatusCode(500).withBody("Error while handling the Request");
        }
    }
    
}
