package com.banking.account.kafka;

import com.banking.account.dto.FundTransferEvent;
import com.banking.account.entity.Account;
import com.banking.account.enums.AccountStatus;
import com.banking.account.repository.AccountRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountTransferListener {

    private final AccountRepository accountRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "fund-transfer-requests", groupId = "account-service-group")
    @Transactional
    public void handleFundTransferRequest(String message) {
        FundTransferEvent event = null;
        try {
            event = objectMapper.readValue(message, FundTransferEvent.class);
            log.info("Processing FundTransferRequest for Ref: {}", event.getReferenceNumber());

            Account fromAccount = accountRepository.findByAccountNumber(event.getFromAccountNumber())
                    .orElse(null);
            Account toAccount = accountRepository.findByAccountNumber(event.getToAccountNumber())
                    .orElse(null);

            if (fromAccount == null || toAccount == null) {
                failTransfer(event, "One or both accounts not found");
                return;
            }

            if (fromAccount.getStatus() != AccountStatus.ACTIVE || toAccount.getStatus() != AccountStatus.ACTIVE) {
                failTransfer(event, "One or both accounts are not ACTIVE");
                return;
            }

            if (fromAccount.getBalance().compareTo(event.getAmount()) < 0) {
                failTransfer(event, "Insufficient balance");
                return;
            }

            // Deduct and add
            fromAccount.setBalance(fromAccount.getBalance().subtract(event.getAmount()));
            toAccount.setBalance(toAccount.getBalance().add(event.getAmount()));

            accountRepository.save(fromAccount);
            accountRepository.save(toAccount);

            // Publish Success
            event.setStatus("SUCCESS");
            publishResponse(event);

            log.info("Transfer processed successfully for Ref: {}", event.getReferenceNumber());

        } catch (Exception e) {
            log.error("Error processing fund-transfer-request", e);
            if (event != null) {
                failTransfer(event, "Internal error: " + e.getMessage());
            }
        }
    }

    private void failTransfer(FundTransferEvent event, String reason) {
        log.warn("Transfer failed for Ref: {}. Reason: {}", event.getReferenceNumber(), reason);
        event.setStatus("FAILED");
        event.setReason(reason);
        publishResponse(event);
    }

    private void publishResponse(FundTransferEvent event) {
        try {
            String eventJson = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("fund-transfer-responses", event.getReferenceNumber(), eventJson);
        } catch (Exception e) {
            log.error("Failed to publish transfer response for Ref: {}", event.getReferenceNumber(), e);
        }
    }
}
