package com.saas.app.service;

import com.saas.app.exception.SubscriptionException;
import com.saas.app.model.RepoSubscription;
import com.saas.app.model.GitHubRepository;
import com.saas.app.repository.RepoSubscriptionRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RepoSubscriptionService {
    
    private static final Logger logger = LoggerFactory.getLogger(RepoSubscriptionService.class);
    
    private final RepoSubscriptionRepository subscriptionRepository;
    private final RepoService repoService;
    private final Validator validator;
    
    public RepoSubscriptionService(RepoSubscriptionRepository subscriptionRepository, 
                                 RepoService repoService,
                                 Validator validator) {
        this.subscriptionRepository = subscriptionRepository;
        this.repoService = repoService;
        this.validator = validator;
    }
    
    /**
     * Validates an email address using Jakarta validation
     * 
     * @param email Email address to validate
     * @throws IllegalArgumentException if the email is invalid
     */
    private void validateEmail(String email) {
        if (email == null) {
            throw new IllegalArgumentException("Email address cannot be null");
        }
        
        // Create an anonymous class to validate the email
        class EmailContainer {
            @Email(message = "Invalid email format")
            @NotBlank(message = "Email is required")
            String value;
            
            public EmailContainer(String email) {
                this.value = email;
            }
        }
        
        EmailContainer container = new EmailContainer(email);
        Set<ConstraintViolation<EmailContainer>> violations = validator.validate(container);
        
        if (!violations.isEmpty()) {
            String errorMessage = violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
            throw new IllegalArgumentException(errorMessage);
        }
    }
    
    /**
     * Subscribe a user to repository activity
     * 
     * @param email User's email address
     * @param owner Repository owner
     * @param repoName Repository name
     * @return The created subscription
     * @throws IllegalArgumentException if the email is invalid
     * @throws SubscriptionException if the repository is invalid or user is already subscribed
     */
    @Transactional
    public RepoSubscription subscribe(String email, String owner, String repoName) {
        validateEmail(email);
        
        try {
            GitHubRepository repository = repoService.getOrCreateRepository(owner, repoName);
            
            Optional<RepoSubscription> existingSubscription = 
                subscriptionRepository.findByEmailAndRepository(email, repository);
            
            if (existingSubscription.isPresent()) {
                throw new SubscriptionException("Already subscribed to " + owner + "/" + repoName);
            }
    
            RepoSubscription subscription = new RepoSubscription(email, repository);
            subscription = subscriptionRepository.save(subscription);
            
            logger.info("User {} subscribed to repository {}/{}", email, owner, repoName);
            return subscription;
            
        } catch (IllegalArgumentException e) {
            throw new SubscriptionException("Invalid repository: " + owner + "/" + repoName);
        }
    }
    
    /**
     * Unsubscribe a user from repository activity
     * 
     * @param email User's email address
     * @param owner Repository owner
     * @param repoName Repository name
     * @throws SubscriptionException if the user is not subscribed to the repository
     */
    @Transactional
    public void unsubscribe(String email, String owner, String repoName) {
        validateEmail(email);
        
        try {
            GitHubRepository repository = repoService.getOrCreateRepository(owner, repoName);
            
            if (!subscriptionRepository.existsByEmailAndRepository(email, repository)) {
                throw new SubscriptionException("Not subscribed to " + owner + "/" + repoName);
            }
            
            subscriptionRepository.deleteByEmailAndRepository(email, repository);
            logger.info("User {} unsubscribed from repository {}/{}", email, owner, repoName);
            
        } catch (IllegalArgumentException e) {
            throw new SubscriptionException("Invalid repository: " + owner + "/" + repoName);
        }
    }
    
    /**
     * Get all subscriptions for a user
     * 
     * @param email User's email address
     * @return List of repository subscriptions
     * @throws IllegalArgumentException if the email is invalid
     */
    public List<RepoSubscription> getUserSubscriptions(String email) {
        validateEmail(email);
        return subscriptionRepository.findByEmail(email);
    }
    
    /**
     * Get all subscriptions for a repository
     * 
     * @param owner Repository owner
     * @param repoName Repository name
     * @return List of user subscriptions
     */
    public List<RepoSubscription> getRepositorySubscriptions(String owner, String repoName) {
        try {
            GitHubRepository repository = repoService.getOrCreateRepository(owner, repoName);
            return subscriptionRepository.findByRepository(repository);
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to get subscriptions for invalid repository {}/{}", owner, repoName);
            return List.of();
        }
    }
    
    /**
     * Update notification status for a subscription
     * 
     * @param email User's email address
     * @param owner Repository owner
     * @param repoName Repository name
     * @param enabled Whether notifications should be enabled
     * @return The updated subscription
     * @throws SubscriptionException if the subscription doesn't exist
     */
    @Transactional
    public RepoSubscription updateNotificationStatus(String email, String owner, String repoName, boolean enabled) {
        validateEmail(email);
        
        try {
            GitHubRepository repository = repoService.getOrCreateRepository(owner, repoName);
            
            RepoSubscription subscription = subscriptionRepository
                .findByEmailAndRepository(email, repository)
                .orElseThrow(() -> new SubscriptionException("No subscription found for " + owner + "/" + repoName));
            
            subscription.setNotificationsEnabled(enabled);
            
            // If enabling notifications, reset the last notification time
            if (enabled) {
                subscription.setLastNotificationAt(null);
            }
            
            logger.info("Notifications {} for user {} on repository {}/{}",
                       enabled ? "enabled" : "disabled", email, owner, repoName);
            
            return subscriptionRepository.save(subscription);
            
        } catch (IllegalArgumentException e) {
            throw new SubscriptionException("Invalid repository: " + owner + "/" + repoName);
        }
    }
    
    /**
     * Get subscriptions that need notifications
     * 
     * @return List of subscriptions needing notification
     */
    public List<RepoSubscription> getSubscriptionsNeedingNotification() {
        return subscriptionRepository.findByNotificationsEnabledTrue()
                .stream()
                .filter(RepoSubscription::needsNotification)
                .collect(Collectors.toList());
    }
    
    /**
     * Mark a subscription as notified
     * 
     * @param subscription The subscription to mark
     */
    @Transactional
    public void markNotified(RepoSubscription subscription) {
        subscription.markNotified();
        subscriptionRepository.save(subscription);
    }
}