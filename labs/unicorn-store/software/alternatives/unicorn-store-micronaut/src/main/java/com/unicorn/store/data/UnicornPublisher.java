package com.unicorn.store.data;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unicorn.store.exceptions.PublisherException;
import com.unicorn.store.model.Unicorn;
import com.unicorn.store.model.UnicornEventType;
import jakarta.inject.Singleton;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.core.SdkSystemSetting;
import software.amazon.awssdk.http.crt.AwsCrtAsyncHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.eventbridge.EventBridgeAsyncClient;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequest;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequestEntry;

@Singleton
public class UnicornPublisher {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final EventBridgeAsyncClient eventBridgeClient = EventBridgeAsyncClient
            .builder()
            .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
            .region(Region.of(System.getenv(SdkSystemSetting.AWS_REGION.environmentVariable())))
            .httpClient(AwsCrtAsyncHttpClient.create())
            .build();

    public void publish(Unicorn unicorn, UnicornEventType unicornEventType) {
        try {
            var unicornJson = objectMapper.writeValueAsString(unicorn);
            var eventsRequest = createEventRequestEntry(unicornEventType, unicornJson);

            eventBridgeClient.putEvents(eventsRequest).get();
        } catch (JsonProcessingException e) {
            throw new PublisherException("Error while serializing the Unicorn", e);
        } catch (Exception e) {
            throw new PublisherException("Error while publishing the event", e);
        }
    }

    private PutEventsRequest createEventRequestEntry(UnicornEventType unicornEventType, String unicornJson) {
        var entry = PutEventsRequestEntry.builder()
                .source("com.unicorn.store")
                .eventBusName("unicorns")
                .detailType(unicornEventType.name())
                .detail(unicornJson)
                .build();

        return PutEventsRequest.builder().entries(entry).build();
    }

}
