package com.saas.app.service;

import com.saas.app.exception.SubscriptionException;
import com.saas.app.model.RepoSubscription;
import com.saas.app.repository.RepoSubscriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Pattern;

@Service
public class RepoSubscriptionService {
    
    private static final Logger logger = LoggerFactory.getLogger(RepoSubscriptionService.class);
    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    
    @Autowired
    private RepoSubscriptionRepository subscriptionRepository;
    
    @Autowired
    private GitHubService gitHubService;
    
    @Transactional
    public RepoSubscription subscribe(String email, String owner, String repoName) {
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Invalid email address");
        }
        
        try {
            gitHubService.validateRepository(owner, repoName);
        } catch (Exception e) {
            throw new SubscriptionException("Invalid repository: " + owner + "/" + repoName);
        }
    
        if (subscriptionRepository.existsByEmailAndOwnerAndRepoName(email, owner, repoName)) {
            throw new SubscriptionException("Already subscribed to " + owner + "/" + repoName);
        }

        RepoSubscription subscription = new RepoSubscription(email, owner, repoName);
        subscription = subscriptionRepository.save(subscription);
        
        logger.info("User {} subscribed to repository {}/{}", email, owner, repoName);
        return subscription;
    }
    
    @Transactional
    public void unsubscribe(String email, String owner, String repoName) {
        if (!subscriptionRepository.existsByEmailAndOwnerAndRepoName(email, owner, repoName)) {
            throw new SubscriptionException("Not subscribed to " + owner + "/" + repoName);
        }
        
        subscriptionRepository.deleteByEmailAndOwnerAndRepoName(email, owner, repoName);
        logger.info("User {} unsubscribed from repository {}/{}", email, owner, repoName);
    }
    
    public List<RepoSubscription> getUserSubscriptions(String email) {
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Invalid email address");
        }
        
        return subscriptionRepository.findByEmail(email);
    }
    
    public List<RepoSubscription> getRepositorySubscriptions(String owner, String repoName) {
        return subscriptionRepository.findByOwnerAndRepoName(owner, repoName);
    }
}