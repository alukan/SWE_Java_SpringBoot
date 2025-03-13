package com.saas.app.controller;

import com.saas.app.exception.SubscriptionException;
import com.saas.app.model.RepoSubscription;
import com.saas.app.service.RepoSubscriptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping("/api/subscription")
public class RepoSubscriptionController {

    private static final Logger logger = LoggerFactory.getLogger(RepoSubscriptionController.class);
    private final RepoSubscriptionService subscriptionService;

    @Autowired
    public RepoSubscriptionController(RepoSubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    /**
     * Subscribe to a repository's activity
     * Uses path variables for repository info and email as a request parameter
     */
    @PostMapping("/repository/{owner}/{repo}")
    public ResponseEntity<?> subscribeToRepository(
            @PathVariable @NotBlank(message = "Owner name is required") String owner,
            @PathVariable @NotBlank(message = "Repository name is required") String repo,
            @RequestParam @jakarta.validation.constraints.Email(message = "Invalid email format") @NotBlank(message = "Email is required") String email) {

        try {
            logger.info("Subscribing {} to repository {}/{}", email, owner, repo);
            RepoSubscription subscription = subscriptionService.subscribe(email, owner, repo);
            return ResponseEntity.ok(subscription);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid subscription parameters: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (SubscriptionException e) {
            logger.warn("Subscription error: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error during subscription", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred"));
        }
    }

    /**
     * Unsubscribe from a repository's activity
     * Uses path variables for repository info and email as a request parameter
     */
    @DeleteMapping("/repository/{owner}/{repo}")
    public ResponseEntity<?> unsubscribeFromRepository(
            @PathVariable String owner,
            @PathVariable String repo,
            @RequestParam @jakarta.validation.constraints.Email(message = "Invalid email format") @NotBlank(message = "Email is required") String email) {

        try {
            logger.info("Unsubscribing {} from repository {}/{}", email, owner, repo);
            subscriptionService.unsubscribe(email, owner, repo);
            return ResponseEntity.ok(Map.of("message", "Successfully unsubscribed from " + owner + "/" + repo));
        } catch (SubscriptionException e) {
            logger.warn("Unsubscription error: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error during unsubscription", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred"));
        }
    }

    @GetMapping("/repository")
    public ResponseEntity<?> getUserSubscriptions(
            @RequestParam @jakarta.validation.constraints.Email(message = "Invalid email format") @NotBlank(message = "Email is required") String email) {
        try {
            logger.info("Fetching subscriptions for user {}", email);
            List<RepoSubscription> subscriptions = subscriptionService.getUserSubscriptions(email);
            return ResponseEntity.ok(subscriptions);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid email address: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error fetching user subscriptions", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred"));
        }
    }

    @GetMapping("/repository/{owner}/{repo}")
    public ResponseEntity<?> getRepositorySubscriptions(
            @PathVariable String owner,
            @PathVariable String repo) {

        try {
            logger.info("Fetching subscriptions for repository {}/{}", owner, repo);
            List<RepoSubscription> subscriptions = subscriptionService.getRepositorySubscriptions(owner, repo);
            return ResponseEntity.ok(subscriptions);
        } catch (Exception e) {
            logger.error("Unexpected error fetching repository subscriptions", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred"));
        }
    }

    @PutMapping("/repository/{owner}/{repo}/notifications/enable")
    public ResponseEntity<?> enableNotifications(
            @PathVariable String owner,
            @PathVariable String repo,
            @RequestParam @jakarta.validation.constraints.Email(message = "Invalid email format") @NotBlank(message = "Email is required") String email) {

        try {
            logger.info("Enabling notifications for {} on repository {}/{}", email, owner, repo);
            RepoSubscription subscription = subscriptionService.updateNotificationStatus(email, owner, repo, true);
            return ResponseEntity.ok(subscription);
        } catch (SubscriptionException e) {
            logger.warn("Notification update error: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error updating notifications", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred"));
        }
    }

    @PutMapping("/repository/{owner}/{repo}/notifications/disable")
    public ResponseEntity<?> disableNotifications(
            @PathVariable String owner,
            @PathVariable String repo,
            @RequestParam @jakarta.validation.constraints.Email(message = "Invalid email format") @NotBlank(message = "Email is required") String email) {

        try {
            logger.info("Disabling notifications for {} on repository {}/{}", email, owner, repo);
            RepoSubscription subscription = subscriptionService.updateNotificationStatus(email, owner, repo, false);
            return ResponseEntity.ok(subscription);
        } catch (SubscriptionException e) {
            logger.warn("Notification update error: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error updating notifications", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred"));
        }
    }
}