package com.unicorn.broker;

import com.unicorn.broker.data.TransactionRepository;
import com.unicorn.broker.data.ValidStockFetcher;
import com.unicorn.broker.exceptions.InvalidStockException;
import com.unicorn.broker.model.Transaction;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.Optional;
import java.util.UUID;

public class TransactionService {

    private static final String BROKER_ID = RandomStringUtils.randomAlphanumeric(8);
    private final ValidStockFetcher validStockFetcher = new ValidStockFetcher();
    private final TransactionRepository transactionRepository = new TransactionRepository();

    public Optional<Transaction> writeTransaction(Transaction transaction) {
        if(!validStockFetcher.getValidStocks().contains(transaction.stockId)) {
            throw new InvalidStockException(transaction.stockId + " is not a valid stock.\n");
        }

        transaction.transactionId = UUID.randomUUID().toString();
        transaction.brokerId = BROKER_ID;

        return transactionRepository.writeTransaction(transaction);
    }
}
