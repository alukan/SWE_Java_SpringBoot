package com.saas.app.service;

import com.saas.app.model.GitHubRepository;
import com.saas.app.model.RepoNotification;
import com.saas.app.model.RepoSubscription;
import com.saas.app.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    
    private final NotificationRepository notificationRepository;
    private final RepoSubscriptionService subscriptionService;
    
    @Autowired
    public NotificationService(NotificationRepository notificationRepository, RepoSubscriptionService subscriptionService) {
        this.notificationRepository = notificationRepository;
        this.subscriptionService = subscriptionService;
    }
    
    @Transactional
    public RepoNotification createNotification(RepoSubscription subscription, String message) {
        GitHubRepository repository = subscription.getRepository();
        String email = subscription.getEmail();
        
        RepoNotification notification = new RepoNotification(email, repository, message);
        notification = notificationRepository.save(notification);
        
        // Mark the subscription as notified to prevent duplicate notifications
        subscriptionService.markNotified(subscription);
        
        logger.info("Created notification {} for user {} about repository {}/{}",
                notification.getId(), email, repository.getOwner(), repository.getName());
                
        return notification;
    }
    
    /**
     * Get all notifications for a user
     * 
     * @param email The user's email
     * @param pageable Pagination information
     * @return A page of notifications
     */
    public Page<RepoNotification> getUserNotifications(String email, Pageable pageable) {
        return notificationRepository.findByEmail(email, pageable);
    }
    
    public List<RepoNotification> getUnreadNotifications(String email) {
        return notificationRepository.findByEmailAndReadFalseOrderByCreatedAtDesc(email);
    }
    
    /**
     * Mark a notification as read
     * 
     * @param id The notification ID
     * @param email The user's email
     * @return true if the notification was found and updated, false otherwise
     */
    @Transactional
    public boolean markAsRead(Long id, String email) {
        Optional<RepoNotification> notification = notificationRepository.findByIdAndEmail(id, email);
        
        if (notification.isPresent()) {
            RepoNotification n = notification.get();
            n.setRead(true);
            notificationRepository.save(n);
            return true;
        }
        
        return false;
    }
    
    /**
     * Mark all notifications for a user as read
     * 
     * @param email The user's email
     * @return The number of notifications marked as read
     */
    @Transactional
    public int markAllAsRead(String email) {
        List<RepoNotification> unreadNotifications = notificationRepository.findByEmailAndReadFalseOrderByCreatedAtDesc(email);
        
        for (RepoNotification notification : unreadNotifications) {
            notification.setRead(true);
            notificationRepository.save(notification);
        }
        
        return unreadNotifications.size();
    }
    
    @Transactional
    public void clearAllNotifications(String email) {
        notificationRepository.deleteByEmail(email);
    }
}