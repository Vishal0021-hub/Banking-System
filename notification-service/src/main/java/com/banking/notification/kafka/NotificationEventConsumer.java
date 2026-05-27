package com.banking.notification.kafka;

import com.banking.notification.entity.Notification;
import com.banking.notification.enums.NotificationType;
import com.banking.notification.repository.NotificationRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventConsumer {

    private final NotificationRepository notificationRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "transaction-events", groupId = "notification-group")
    public void handleTransactionEvent(String message) {
        try {
            JsonNode node = objectMapper.readTree(message);
            String event = node.get("event").asText();
            Long userId = node.get("userId").asLong();
            String refNumber = node.get("referenceNumber").asText();
            double amount = node.get("amount").asDouble();
            String type = node.get("type").asText();
            String status = node.get("status").asText();
            boolean fraudFlagged = node.has("fraudFlagged") && node.get("fraudFlagged").asBoolean();

            String title, body;
            NotificationType notifType;

            if (fraudFlagged) {
                title = "⚠️ Transaction Flagged";
                body = "Your transaction of ₹" + String.format("%.2f", amount) +
                       " (Ref: " + refNumber + ") was flagged for suspicious activity.";
                notifType = NotificationType.FRAUD_ALERT;
            } else {
                switch (type) {
                    case "DEPOSIT" -> {
                        title = "💰 Money Received";
                        body = "₹" + String.format("%.2f", amount) + " deposited. Ref: " + refNumber;
                        notifType = NotificationType.TRANSACTION;
                    }
                    case "WITHDRAWAL" -> {
                        title = "💸 Money Withdrawn";
                        body = "₹" + String.format("%.2f", amount) + " withdrawn. Ref: " + refNumber;
                        notifType = NotificationType.TRANSACTION;
                    }
                    case "TRANSFER" -> {
                        title = "🔄 Transfer " + status;
                        body = "₹" + String.format("%.2f", amount) + " transfer " + status.toLowerCase() + ". Ref: " + refNumber;
                        notifType = NotificationType.TRANSACTION;
                    }
                    default -> {
                        title = "Transaction Update";
                        body = "Transaction " + refNumber + " status: " + status;
                        notifType = NotificationType.INFO;
                    }
                }
            }

            Notification notification = Notification.builder()
                    .userId(userId)
                    .title(title)
                    .message(body)
                    .type(notifType)
                    .referenceId(refNumber)
                    .build();

            notificationRepository.save(notification);
            log.info("Notification saved for user {} - {}", userId, title);

        } catch (Exception e) {
            log.error("Failed to process transaction event: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "user-events", groupId = "notification-group")
    public void handleUserEvent(String message) {
        try {
            JsonNode node = objectMapper.readTree(message);
            String event = node.get("event").asText();
            Long userId = node.get("userId").asLong();
            String name = node.has("name") ? node.get("name").asText() : "User";

            if ("USER_REGISTERED".equals(event)) {
                Notification notification = Notification.builder()
                        .userId(userId)
                        .title("🎉 Welcome to Enterprise Bank!")
                        .message("Hi " + name + "! Your account has been created. Complete your KYC to unlock all features.")
                        .type(NotificationType.ACCOUNT)
                        .build();
                notificationRepository.save(notification);
            }
        } catch (Exception e) {
            log.error("Failed to process user event: {}", e.getMessage(), e);
        }
    }
}
