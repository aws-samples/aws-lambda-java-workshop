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

public class BlockedStockFetcher {

    private List<String> blockedStocks;
    private static final String BUCKET_NAME = System.getenv("BUCKET_NAME");
    private final Logger logger = LoggerFactory.getLogger(BlockedStockFetcher.class);
    private final S3AsyncClient s3Client = S3AsyncClient.builder()
            .region(Region.of(System.getenv(SdkSystemSetting.AWS_REGION.environmentVariable())))
            .httpClientBuilder(AwsCrtAsyncHttpClient.builder())
            .build();

    public BlockedStockFetcher() {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(BUCKET_NAME)
                .key("blocked-stock-list.txt")
                .build();

       s3Client.getObject(getObjectRequest, AsyncResponseTransformer.toBytes())
               .thenApply(ResponseBytes::asUtf8String)
               .whenComplete((stringContent, e) -> {
                   if (stringContent != null) {
                       blockedStocks = stringContent.lines().collect(Collectors.toList());
                   } else {
                       logger.error("Could not retrieve validation file" , e);
                   }
               });

    }

    public List<String> getBlockedStocks(){
        return Collections.unmodifiableList(blockedStocks);
    }


}
