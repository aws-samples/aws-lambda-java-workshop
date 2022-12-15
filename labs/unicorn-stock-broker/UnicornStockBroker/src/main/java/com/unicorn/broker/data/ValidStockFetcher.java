package com.unicorn.broker.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.SdkSystemSetting;
import software.amazon.awssdk.core.async.AsyncResponseTransformer;
import software.amazon.awssdk.http.crt.AwsCrtAsyncHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ValidStockFetcher {

    private List<String> validStocks;
    private static final String BUCKET_NAME = System.getenv("BUCKET_NAME");
    private final Logger logger = LoggerFactory.getLogger(ValidStockFetcher.class);
    private final S3AsyncClient s3Client = S3AsyncClient.builder()
            .region(Region.of(System.getenv(SdkSystemSetting.AWS_REGION.environmentVariable())))
            .httpClientBuilder(AwsCrtAsyncHttpClient.builder())
            .build();

    public ValidStockFetcher() {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(BUCKET_NAME)
                .key("valid-stock-list")
                .build();

       s3Client.getObject(getObjectRequest, AsyncResponseTransformer.toBytes())
               .thenApply(ResponseBytes::asUtf8String)
               .whenComplete((stringContent, e) -> {
                   if (stringContent != null) {
                       validStocks = stringContent.lines().collect(Collectors.toList());
                   } else {
                       logger.error("Could not retrieve validation file" , e);
                   }
               });

    }

    public List<String> getValidStocks(){
        return Collections.unmodifiableList(validStocks);
    }


}
