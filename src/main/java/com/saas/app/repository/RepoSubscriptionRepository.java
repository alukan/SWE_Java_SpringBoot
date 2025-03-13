package com.saas.app.repository;

import com.saas.app.model.RepoSubscription;
import com.saas.app.model.GitHubRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepoSubscriptionRepository extends JpaRepository<RepoSubscription, Long> {
    
    List<RepoSubscription> findByEmail(String email);
    
    Optional<RepoSubscription> findByEmailAndRepository(String email, GitHubRepository repository);
    
    boolean existsByEmailAndRepository(String email, GitHubRepository repository);
    
    void deleteByEmailAndRepository(String email, GitHubRepository repository);
    
    List<RepoSubscription> findByRepository(GitHubRepository repository);
    
    List<RepoSubscription> findByNotificationsEnabledTrue();
}