package com.unicorn.store.model;

import io.micronaut.core.annotation.Introspected;

@Introspected
public enum UnicornEventType {
    UNICORN_CREATED, UNICORN_UPDATED, UNICORN_DELETED
}
