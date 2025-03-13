package com.saas.app.controller;

import com.saas.app.model.RepoNotification;
import com.saas.app.service.NotificationService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@Validated
public class NotificationController {

    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping()
    public ResponseEntity<Page<RepoNotification>> getNotifications(
            @RequestParam @Email @NotBlank String email,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        logger.info("Getting notifications for user {} (page={}, size={})", email, page, size);
        Page<RepoNotification> notifications = notificationService.getUserNotifications(
                email, 
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/unread")
    public ResponseEntity<List<RepoNotification>> getUnreadNotifications(
            @RequestParam @Email @NotBlank String email) {
        
        logger.info("Getting unread notifications for user {}", email);
        List<RepoNotification> notifications = notificationService.getUnreadNotifications(email);
        
        return ResponseEntity.ok(notifications);
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(
            @PathVariable @Positive Long id,
            @RequestParam @Email @NotBlank String email) {
        
        logger.info("Marking notification {} as read for user {}", id, email);
        boolean success = notificationService.markAsRead(id, email);
        
        if (success) {
            return ResponseEntity.ok(Map.of("message", "Notification marked as read"));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Map<String, Integer>> markAllAsRead(
            @RequestParam @Email @NotBlank String email) {
        
        logger.info("Marking all notifications as read for user {}", email);
        int count = notificationService.markAllAsRead(email);
        
        return ResponseEntity.ok(Map.of("markedAsRead", count));
    }

    @DeleteMapping("/clear")
    public ResponseEntity<Map<String, String>> clearAllNotifications(
            @RequestParam @Email @NotBlank String email) {
        
        logger.info("Clearing all notifications for user {}", email);
        notificationService.clearAllNotifications(email);
        
        return ResponseEntity.ok(Map.of("message", "All notifications cleared"));
    }
}