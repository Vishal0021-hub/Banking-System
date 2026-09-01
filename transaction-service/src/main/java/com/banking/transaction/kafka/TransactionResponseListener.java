package com.banking.transaction.kafka;

import com.banking.transaction.dto.FundTransferEvent;
import com.banking.transaction.entity.Transaction;
import com.banking.transaction.enums.TransactionStatus;
import com.banking.transaction.repository.TransactionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionResponseListener {

    private final TransactionRepository transactionRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "fund-transfer-responses", groupId = "transaction-service-group")
    @Transactional
    public void handleFundTransferResponse(String message) {
        try {
            FundTransferEvent event = objectMapper.readValue(message, FundTransferEvent.class);
            log.info("Received FundTransferEvent response for Ref: {} with status: {}", event.getReferenceNumber(), event.getStatus());

            Transaction txn = transactionRepository.findByReferenceNumber(event.getReferenceNumber())
                    .orElse(null);

            if (txn != null) {
                if ("SUCCESS".equals(event.getStatus())) {
                    txn.setStatus(TransactionStatus.SUCCESS);
                    txn.setCompletedAt(java.time.LocalDateTime.now());
                } else if ("FAILED".equals(event.getStatus())) {
                    txn.setStatus(TransactionStatus.FAILED);
                    txn.setCompletedAt(java.time.LocalDateTime.now());
                    txn.setDescription(txn.getDescription() + " - Failed: " + event.getReason());
                }
                transactionRepository.save(txn);
                log.info("Updated Transaction {} to {}", txn.getReferenceNumber(), event.getStatus());
            } else {
                log.warn("Transaction not found for Ref: {}", event.getReferenceNumber());
            }

        } catch (Exception e) {
            log.error("Error processing fund-transfer-response", e);
        }
    }
}
