package com.unicorn.broker;

import com.unicorn.broker.data.TransactionRepository;
import com.unicorn.broker.data.ValidStockFetcher;
import com.unicorn.broker.exceptions.InvalidStockException;
import com.unicorn.broker.model.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.UUID;

public class TransactionService {

    private String BROKER_ID = UUID.randomUUID().toString();
    private final ValidStockFetcher validStockFetcher;
    private final TransactionRepository transactionRepository;
    private final Logger logger = LoggerFactory.getLogger(TransactionService.class);

    public TransactionService(final ValidStockFetcher validStockFetcher, final TransactionRepository transactionRepository) {
        this.validStockFetcher = validStockFetcher;
        this.transactionRepository = transactionRepository;
    }

    public Optional<Transaction> writeTransaction(Transaction transaction) {
        if(!validStockFetcher.getValidStocks().contains(transaction.stockId)) {
            throw new InvalidStockException(transaction.stockId + " is not a valid stock.%n");
        }

        transaction.transactionId = UUID.randomUUID().toString();
        transaction.brokerId = BROKER_ID;

        return transactionRepository.writeTransaction(transaction);
    }

}
