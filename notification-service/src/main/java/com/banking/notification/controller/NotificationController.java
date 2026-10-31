package com.banking.notification.controller;

import com.banking.common.dto.ApiResponse;
import com.banking.notification.entity.Notification;
import com.banking.notification.repository.NotificationRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import com.banking.notification.service.SseService;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final SseService sseService;

    private Long extractUserId(HttpServletRequest request) {
        String userIdStr = request.getHeader("X-User-Id");
        if (userIdStr != null && !userIdStr.isBlank()) {
            try {
                return Long.parseLong(userIdStr);
            } catch (NumberFormatException ignored) {}
        }
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getDetails() instanceof Long) {
            return (Long) auth.getDetails();
        }
        throw new com.banking.common.exception.BadRequestException("Missing or invalid user identification header");
    }

    @GetMapping("/stream")
    public SseEmitter stream(HttpServletRequest req) {
        Long userId = extractUserId(req);
        return sseService.createEmitter(userId);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Notification>>> getAll(HttpServletRequest req) {
        Long userId = extractUserId(req);
        return ResponseEntity.ok(ApiResponse.success(
                notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)));
    }

    @GetMapping("/unread")
    public ResponseEntity<ApiResponse<List<Notification>>> getUnread(HttpServletRequest req) {
        Long userId = extractUserId(req);
        return ResponseEntity.ok(ApiResponse.success(
                notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(userId)));
    }

    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getUnreadCount(HttpServletRequest req) {
        Long userId = extractUserId(req);
        long count = notificationRepository.countByUserIdAndReadFalse(userId);
        return ResponseEntity.ok(ApiResponse.success(Map.of("unreadCount", count)));
    }

    @PatchMapping("/{id}/read")
    @Transactional
    public ResponseEntity<ApiResponse<Void>> markRead(
            HttpServletRequest req, @PathVariable Long id) {
        Long userId = extractUserId(req);
        notificationRepository.findById(id).ifPresent(n -> {
            if (n.getUserId().equals(userId)) {
                n.setRead(true);
                notificationRepository.save(n);
            }
        });
        return ResponseEntity.ok(ApiResponse.success(null, "Marked as read"));
    }

    @PatchMapping("/read-all")
    @Transactional
    public ResponseEntity<ApiResponse<Void>> markAllRead(HttpServletRequest req) {
        Long userId = extractUserId(req);
        notificationRepository.markAllReadByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(null, "All notifications marked as read"));
    }
}
