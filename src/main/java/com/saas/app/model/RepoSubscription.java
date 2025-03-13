package com.saas.app.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.ZonedDateTime;

@Entity
@Table(name = "repo_subscriptions", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"email", "repository_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RepoSubscription {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String email;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "repository_id", nullable = false)
    private GitHubRepository repository;
    
    @Column(name = "subscribed_at", nullable = false)
    private ZonedDateTime subscribedAt;
    
    @Column(name = "notifications_enabled", nullable = false)
    private boolean notificationsEnabled = true;
    
    @Column(name = "last_notification_at")
    private ZonedDateTime lastNotificationAt;
    
    /**
     * Creates a new subscription with notifications disabled by default
     */
    public RepoSubscription(String email, GitHubRepository repository) {
        this.email = email;
        this.repository = repository;
        this.subscribedAt = ZonedDateTime.now();
        this.notificationsEnabled = false;
    }
    
    /**
     * Creates a new subscription with specified notification preference
     */
    public RepoSubscription(String email, GitHubRepository repository, boolean notificationsEnabled) {
        this.email = email;
        this.repository = repository;
        this.subscribedAt = ZonedDateTime.now();
        this.notificationsEnabled = notificationsEnabled;
    }
    
    /**
     * Checks if this subscription needs notification based on repository activity
     */
    public boolean needsNotification() {
        if (!notificationsEnabled) {
            return false;
        }
        
        return repository.hasActivitySince(lastNotificationAt);
    }
    
    /**
     * Marks that notification has been sent
     */
    public void markNotified() {
        this.lastNotificationAt = ZonedDateTime.now();
    }
}