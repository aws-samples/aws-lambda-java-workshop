package com.unicorn.store;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.core.SdkSystemSetting;
import software.amazon.awssdk.http.crt.AwsCrtAsyncHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
public class UnicornWriter {

    private static final DynamoDbAsyncClient dynamoDbAsyncClient = DynamoDbAsyncClient
            .builder()
            .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
            .region(Region.of(System.getenv(SdkSystemSetting.AWS_REGION.environmentVariable())))
            .httpClient(AwsCrtAsyncHttpClient.create())
            .build();

    public String save(String payload) {
        try {
            var id = UUID.randomUUID().toString();
            var dynamoDBRequest = createPutItemRequest(id, payload);
            dynamoDbAsyncClient.putItem(dynamoDBRequest).get();
            return id;
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException("SDK client call not successful", e);
        }
    }

    private PutItemRequest createPutItemRequest(String id, String details) {
        return PutItemRequest.builder()
                .tableName("unicorn-audit")
                .item(Map.of(
                        "id", AttributeValue.builder().s(id).build(),
                        "details", AttributeValue.builder().s(details).build()
                ))
                .build();
    }
}
