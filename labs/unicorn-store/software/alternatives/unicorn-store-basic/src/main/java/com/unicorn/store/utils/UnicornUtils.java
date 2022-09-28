package com.unicorn.store.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.unicorn.store.exceptions.MissingParameterException;
import com.unicorn.store.model.Unicorn;

public class UnicornUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();
	
	public static String getUnicornParameter(APIGatewayProxyRequestEvent event) {
    	try {
    		return (String) event.getPathParameters().get("id");
    		
    	} catch(NullPointerException e) {
    		throw new MissingParameterException();
    	}
    }
	
    public static Unicorn getBodyUnicorn(APIGatewayProxyRequestEvent event) throws JsonProcessingException {
        return objectMapper.readValue(event.getBody(), Unicorn.class);
    }
}
