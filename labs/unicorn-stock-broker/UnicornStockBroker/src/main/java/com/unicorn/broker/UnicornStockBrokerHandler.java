package com.unicorn.broker;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

public class UnicornStockBrokerHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final UnicornTransactionRepository unicornTransactionRepository = new UnicornTransactionRepository();

    public APIGatewayProxyResponseEvent handleRequest(final APIGatewayProxyRequestEvent input, final Context context) {
        UnicornStock unicornStock = new UnicornStock();
        unicornStock.stockId = "test";
        unicornStock.quantity = 2;
        unicornTransactionRepository.writeTransaction(unicornStock);
        return new APIGatewayProxyResponseEvent()
                .withStatusCode(200)
                .withBody("This is supposed to be the Unicorn Location API at some point!");
    }

    public APIGatewayProxyResponseEvent handleReadRequest(final APIGatewayProxyRequestEvent input, final Context context) {
        var transactions = unicornTransactionRepository.readTransactions();
        return new APIGatewayProxyResponseEvent()
                .withStatusCode(200)
                .withBody(transactions);
    }

}
