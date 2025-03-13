package com.saas.app.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;

@Entity
@Table(name = "repositories", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"owner", "name"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GitHubRepository {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String owner;
    
    @Column(nullable = false)
    private String name;
    
    @Column(name = "last_checked_at")
    private ZonedDateTime lastCheckedAt;
    
    @Column(name = "last_activity_at")
    private ZonedDateTime lastActivityAt;
    
    @Column(name = "activity_count")
    private Integer activityCount = 0;
    
    public GitHubRepository(String owner, String name) {
        this.owner = owner;
        this.name = name;
        this.lastCheckedAt = ZonedDateTime.now();
    }
    
    /**
     * Updates the last checked time to now
     */
    public void markAsChecked() {
        this.lastCheckedAt = ZonedDateTime.now();
    }
    
    /**
     * Updates the last activity time to now
     */
    public void markActivity() {
        this.lastActivityAt = ZonedDateTime.now();
        this.activityCount++;
    }
    
    /**
     * Determines if there has been new activity since a given time
     * 
     * @param since The time to check against
     * @return true if activity has occurred since the given time
     */
    public boolean hasActivitySince(ZonedDateTime since) {
        return lastActivityAt != null && 
               (since == null || lastActivityAt.isAfter(since));
    }
}