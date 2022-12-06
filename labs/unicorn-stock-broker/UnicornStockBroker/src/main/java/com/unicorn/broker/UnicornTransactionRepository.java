package com.unicorn.broker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.SdkSystemSetting;
import software.amazon.awssdk.http.crt.AwsCrtAsyncHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class UnicornTransactionRepository {

    private static final String BROKER_ID = UUID.randomUUID().toString();
    private static final String TABLE_NAME = System.getenv("TABLE_NAME");
    private final Logger logger = LoggerFactory.getLogger(UnicornTransactionRepository.class);
    private final DynamoDbAsyncClient dynamoDbClient = DynamoDbAsyncClient.builder()
            .region(Region.of(System.getenv(SdkSystemSetting.AWS_REGION.environmentVariable())))
            .httpClientBuilder(AwsCrtAsyncHttpClient.builder())
            .build();

    public void writeTransaction(UnicornStock unicornStock) {
        try {
            dynamoDbClient.putItem(PutItemRequest.builder()
                    .tableName(TABLE_NAME)
                    .item(createTransactionDBItem(unicornStock))
                    .build())
                    .get();
        } catch (InterruptedException | ExecutionException e) {
           logger.error("Error while writing to DynamoDB", e);
        }
    }

    public String readTransactions() {
        try {
            var items = dynamoDbClient.scan(ScanRequest.builder()
                    .tableName(TABLE_NAME)
                    .limit(5)
                    .build())
                    .get();

            return items.toString();
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error while writing to DynamoDB", e);
            return "";
        }
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
