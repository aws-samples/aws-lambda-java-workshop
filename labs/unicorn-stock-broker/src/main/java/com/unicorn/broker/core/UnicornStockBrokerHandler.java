package com.unicorn.broker.core;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.jr.ob.JSON;
import com.unicorn.broker.model.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.function.Function;

@Component
public class UnicornStockBrokerHandler implements Function<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final Logger logger = LoggerFactory.getLogger(UnicornStockBrokerHandler.class);
    private final TransactionService transactionService;

    public UnicornStockBrokerHandler(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    public APIGatewayProxyResponseEvent apply(final APIGatewayProxyRequestEvent input) {
        try {
            var unicornStock= JSON.std.beanFrom(Transaction.class, input.getBody());
            var brokerResponse = transactionService.writeTransaction(unicornStock).orElseThrow();
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(200)
                    .withBody(generateJSONResponse(brokerResponse));
        } catch (IOException e ) {
            logger.error("Could not transform request body to UnicornStack object", e);
            return createErrorResponse(400,
                    "Invalid request body. Please provide a valid stock object request");
        }
    }

    private APIGatewayProxyResponseEvent createErrorResponse(Integer statusCode, String errorMessage){
        return new APIGatewayProxyResponseEvent()
                .withStatusCode(statusCode)
                .withBody(errorMessage);
    }

    private String generateJSONResponse(Transaction transaction) {
        return String.format("Broker %s successfully created transaction %s%n", transaction.brokerId.substring(0,8), transaction.transactionId);
    }

}
