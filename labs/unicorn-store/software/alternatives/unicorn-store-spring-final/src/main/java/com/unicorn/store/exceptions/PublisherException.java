package com.unicorn.store.exceptions;

public class PublisherException extends RuntimeException{
    public PublisherException(String errorMessage, Throwable err) {
        super(errorMessage, err);
    }
}
