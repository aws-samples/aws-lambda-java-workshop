package com.unicorn.broker;

import software.amazon.awssdk.core.SdkSystemSetting;
import software.amazon.awssdk.http.crt.AwsCrtAsyncHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UnicornTransactionWriter {

    private static final String BROKER_ID = UUID.randomUUID().toString();
    private static final String TABLE_NAME = System.getenv("TABLE_NAME");
    private final DynamoDbAsyncClient dynamoDbClient = DynamoDbAsyncClient.builder()
            .region(Region.of(System.getenv(SdkSystemSetting.AWS_REGION.environmentVariable())))
            .httpClientBuilder(AwsCrtAsyncHttpClient.builder())
            .build();

    public void writeTransaction(UnicornStock unicornStock) {
        dynamoDbClient.putItem(PutItemRequest.builder()
                .tableName(TABLE_NAME)
                .item(createTransactionDBItem(unicornStock))
                .build());
    }

    private static Map<String, AttributeValue> createTransactionDBItem(UnicornStock stock) {
        Map<String, AttributeValue> item = new HashMap<>();

        item.put("transactionId", AttributeValue.builder().s(UUID.randomUUID().toString()).build());
        item.put("stock", AttributeValue.builder().s(stock.stockId).build());
        item.put("quantity", AttributeValue.builder().n(stock.quantity.toString()).build());
        item.put("broker_id", AttributeValue.builder().s(BROKER_ID).build());

        return item;
    }
}
