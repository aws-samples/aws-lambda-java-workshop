package com.unicorn.broker.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Transaction {
    public String stockId;
    public Integer quantity;
    public String brokerId;
    public String transactionId;

    public Boolean isValid() {
        return (stockId != null && !stockId.isBlank() && quantity != null && quantity > 0);
    }
}
