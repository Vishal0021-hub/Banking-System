package com.banking.transaction.kafka;

import com.banking.transaction.entity.Transaction;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionEventProducer {

    @Getter
    private final KafkaTemplate<String, String> kafkaTemplate;
    
    private static final String TOPIC = "transaction-events";

    public void publishTransactionEvent(Transaction txn) {
        try {
            String event = String.format(
                "{\"event\":\"TRANSACTION_COMPLETED\"," +
                "\"referenceNumber\":\"%s\"," +
                "\"userId\":%d," +
                "\"amount\":%.2f," +
                "\"type\":\"%s\"," +
                "\"status\":\"%s\"," +
                "\"fromAccount\":\"%s\"," +
                "\"toAccount\":\"%s\"," +
                "\"fraudFlagged\":%b}",
                txn.getReferenceNumber(),
                txn.getUserId(),
                txn.getAmount(),
                txn.getTransactionType(),
                txn.getStatus(),
                txn.getFromAccountNumber() != null ? txn.getFromAccountNumber() : "",
                txn.getToAccountNumber() != null ? txn.getToAccountNumber() : "",
                txn.isFraudFlagged()
            );
            kafkaTemplate.send(TOPIC, txn.getReferenceNumber(), event);
            log.debug("Transaction event published: {}", txn.getReferenceNumber());
        } catch (Exception e) {
            log.warn("Failed to publish transaction event: {}", e.getMessage());
        }
    }
}
