package com.saas.app.service;

import com.saas.app.exception.SubscriptionException;
import com.saas.app.model.RepoSubscription;
import com.saas.app.model.GitHubRepository;
import com.saas.app.repository.RepoSubscriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class RepoSubscriptionService {
    
    private static final Logger logger = LoggerFactory.getLogger(RepoSubscriptionService.class);
    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    
    @Autowired
    private RepoSubscriptionRepository subscriptionRepository;
    
    @Autowired
    private RepoService repoService;
    
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
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Invalid email address");
        }
        
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
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Invalid email address");
        }
        
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