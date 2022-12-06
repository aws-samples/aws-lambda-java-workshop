package com.unicorn.broker;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

public class UnicornStockBrokerHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final UnicornTransactionWriter unicornTransactionWriter = new UnicornTransactionWriter();

    public APIGatewayProxyResponseEvent handleRequest(final APIGatewayProxyRequestEvent input, final Context context) {
        UnicornStock unicornStock = new UnicornStock();
        unicornStock.stockId = "test";
        unicornStock.quantity = 2;
        unicornTransactionWriter.writeTransaction(unicornStock);
        return new APIGatewayProxyResponseEvent()
                .withStatusCode(200)
                .withBody("This is supposed to be the Unicorn Location API at some point!");
    }
}
