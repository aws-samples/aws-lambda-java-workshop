package com.unicorn.broker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.SdkSystemSetting;
import software.amazon.awssdk.core.async.AsyncResponseTransformer;
import software.amazon.awssdk.http.crt.AwsCrtAsyncHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class UnicornValidStockFetcher {

    private static List<String> VALID_STOCKS = Collections.emptyList();
    private static final String BUCKET_NAME = System.getenv("BUCKET_NAME");
    private final Logger logger = LoggerFactory.getLogger(UnicornValidStockFetcher.class);
    private final S3AsyncClient s3Client = S3AsyncClient.builder()
            .region(Region.of(System.getenv(SdkSystemSetting.AWS_REGION.environmentVariable())))
            .httpClientBuilder(AwsCrtAsyncHttpClient.builder())
            .build();

    public UnicornValidStockFetcher() {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(BUCKET_NAME)
                .key("valid-stock-list")
                .build();

       s3Client.getObject(getObjectRequest, AsyncResponseTransformer.toBytes())
               .thenApply(ResponseBytes::asUtf8String)
               .whenComplete((stringContent, e) -> {
                   if (stringContent != null)
                       logger.info(stringContent);
                   else
                       logger.error("Could not retrieve validation file" , e);
               });

    }


}
