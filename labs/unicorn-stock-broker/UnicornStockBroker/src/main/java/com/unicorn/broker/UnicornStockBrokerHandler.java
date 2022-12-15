package com.unicorn.broker;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.jr.ob.JSON;
import com.unicorn.broker.data.TransactionRepository;
import com.unicorn.broker.data.ValidStockFetcher;
import com.unicorn.broker.exceptions.InvalidStockException;
import com.unicorn.broker.model.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class UnicornStockBrokerHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final Logger logger = LoggerFactory.getLogger(UnicornStockBrokerHandler.class);
    private static final TransactionService transactionService = new TransactionService(new ValidStockFetcher(), new TransactionRepository());

    public APIGatewayProxyResponseEvent handleRequest(final APIGatewayProxyRequestEvent input, final Context context) {
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
        } catch (InvalidStockException invalidStockException) {
            return createErrorResponse(400, invalidStockException.getMessage());
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
