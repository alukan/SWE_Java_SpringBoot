package com.saas.app.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;

@Entity
@Table(name = "repo_notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RepoNotification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String email;
    
    @ManyToOne
    @JoinColumn(name = "repository_id", nullable = false)
    private GitHubRepository repository;
    
    @Column(nullable = false)
    private String message;
    
    @Column(nullable = false)
    private boolean read = false;
    
    @Column(nullable = false)
    private ZonedDateTime createdAt;
    
    public RepoNotification(String email, GitHubRepository repository, String message) {
        this.email = email;
        this.repository = repository;
        this.message = message;
        this.read = false;
        this.createdAt = ZonedDateTime.now();
    }
}