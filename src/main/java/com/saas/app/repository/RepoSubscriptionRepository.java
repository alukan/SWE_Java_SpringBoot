package com.saas.app.repository;

import com.saas.app.model.RepoSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepoSubscriptionRepository extends JpaRepository<RepoSubscription, Long> {
    
    List<RepoSubscription> findByEmail(String email);
    
    Optional<RepoSubscription> findByEmailAndOwnerAndRepoName(String email, String owner, String repoName);
    
    boolean existsByEmailAndOwnerAndRepoName(String email, String owner, String repoName);
    
    void deleteByEmailAndOwnerAndRepoName(String email, String owner, String repoName);
    
    List<RepoSubscription> findByOwnerAndRepoName(String owner, String repoName);
}