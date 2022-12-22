package com.unicorn.broker.core;

import com.unicorn.broker.data.TransactionRepository;
import com.unicorn.broker.model.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class TransactionService {

    private final String BROKER_ID = UUID.randomUUID().toString();
    private final TransactionRepository transactionRepository;
    private final Logger logger = LoggerFactory.getLogger(TransactionService.class);

    public TransactionService(final TransactionRepository transactionRepository) {
        logger.info(String.format("[NEW BROKER] registered with ID: %s ", BROKER_ID));
        this.transactionRepository = transactionRepository;
    }

    public Optional<Transaction> writeTransaction(Transaction transaction) {
        transaction.transactionId = UUID.randomUUID().toString();
        transaction.brokerId = BROKER_ID;

        return transactionRepository.writeTransaction(transaction);
    }
}
