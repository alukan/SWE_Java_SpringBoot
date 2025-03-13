package com.saas.app.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.ZonedDateTime;

@Entity
@Table(name = "repo_subscriptions", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"email", "owner", "repo_name"}))
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
    
    @Column(nullable = false)
    private String owner;
    
    @Column(name = "repo_name", nullable = false)
    private String repoName;
    
    @Column(name = "subscribed_at", nullable = false)
    private ZonedDateTime subscribedAt;
    
    @Column(name = "notifications_enabled", nullable = false)
    private boolean notificationsEnabled = false;
    
    @Column(name = "last_notification_at")
    private ZonedDateTime lastNotificationAt;
    
    public RepoSubscription(String email, String owner, String repoName) {
        this.email = email;
        this.owner = owner;
        this.repoName = repoName;
        this.subscribedAt = ZonedDateTime.now();
        this.notificationsEnabled = true;
    }
    
    public RepoSubscription(String email, String owner, String repoName, boolean notificationsEnabled) {
        this.email = email;
        this.owner = owner;
        this.repoName = repoName;
        this.subscribedAt = ZonedDateTime.now();
        this.notificationsEnabled = notificationsEnabled;
    }
}