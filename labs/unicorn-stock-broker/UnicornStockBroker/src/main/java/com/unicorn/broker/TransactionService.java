package com.unicorn.broker;

import com.unicorn.broker.data.TransactionRepository;
import com.unicorn.broker.data.BlockedStockFetcher;
import com.unicorn.broker.exceptions.InvalidStockException;
import com.unicorn.broker.model.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.UUID;

public class TransactionService {

    private String BROKER_ID = UUID.randomUUID().toString();
    private final BlockedStockFetcher blockedStockFetcher;
    private final TransactionRepository transactionRepository;
    private final Logger logger = LoggerFactory.getLogger(TransactionService.class);

    public TransactionService(final BlockedStockFetcher validStockFetcher, final TransactionRepository transactionRepository) {
        this.blockedStockFetcher = validStockFetcher;
        this.transactionRepository = transactionRepository;
    }

    public Optional<Transaction> writeTransaction(Transaction transaction) {
        if(blockedStockFetcher.getBlockedStocks().contains(transaction.stockId)) {
            throw new InvalidStockException(transaction.stockId + " is not a valid stock.\n");
        }

        transaction.transactionId = UUID.randomUUID().toString();
        transaction.brokerId = BROKER_ID;

        return transactionRepository.writeTransaction(transaction);
    }

}
