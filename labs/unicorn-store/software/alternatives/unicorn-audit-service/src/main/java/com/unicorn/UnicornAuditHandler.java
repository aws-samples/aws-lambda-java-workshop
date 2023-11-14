package com.unicorn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class UnicornAuditHandler implements Function<String, String> {

    private final UnicornWriter unicornWriter;
    private final Logger logger = LoggerFactory.getLogger(UnicornAuditHandler.class);

    public UnicornAuditHandler(UnicornWriter unicornWriter) {
        this.unicornWriter = unicornWriter;
    }
    @Override
    public String apply(String eventBridgePayload) {
        try {
            return unicornWriter.save(eventBridgePayload);
        } catch (Exception e) {
            logger.error("Error creating unicorn audit record", e);
            throw new RuntimeException("Unable to process unicorn item", e);
        }
    }
}
