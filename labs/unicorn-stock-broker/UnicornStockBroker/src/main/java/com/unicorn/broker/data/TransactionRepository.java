package com.unicorn.broker.data;

import com.unicorn.broker.model.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.SdkSystemSetting;
import software.amazon.awssdk.http.crt.AwsCrtAsyncHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

public class TransactionRepository {

    private static final String TABLE_NAME = System.getenv("TABLE_NAME");
    private final Logger logger = LoggerFactory.getLogger(TransactionRepository.class);
    private final DynamoDbAsyncClient dynamoDbClient = DynamoDbAsyncClient.builder()
            .region(Region.of(System.getenv(SdkSystemSetting.AWS_REGION.environmentVariable())))
            .httpClientBuilder(AwsCrtAsyncHttpClient.builder())
            .build();

    public Optional<Transaction> writeTransaction(Transaction transaction) {
        try {
            dynamoDbClient.putItem(PutItemRequest.builder()
                    .tableName(TABLE_NAME)
                    .item(createTransactionDBItem(transaction))
                    .build())
                    .get();

            return Optional.of(transaction);
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error while writing to DynamoDB", e);
            return Optional.empty();
        }
    }

    private static Map<String, AttributeValue> createTransactionDBItem(Transaction stock) {
        Map<String, AttributeValue> item = new HashMap<>();

        item.put("transactionId", AttributeValue.builder().s(stock.transactionId).build());
        item.put("stock", AttributeValue.builder().s(stock.stockId).build());
        item.put("quantity", AttributeValue.builder().n(stock.quantity.toString()).build());
        item.put("broker_id", AttributeValue.builder().s(stock.brokerId).build());

        return item;
    }
}
