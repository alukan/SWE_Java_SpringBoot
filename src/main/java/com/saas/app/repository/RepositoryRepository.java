package com.saas.app.repository;

import com.saas.app.model.GitHubRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RepositoryRepository extends JpaRepository<GitHubRepository, Long> {
    
    Optional<GitHubRepository> findByOwnerAndName(String owner, String name);
    
    boolean existsByOwnerAndName(String owner, String name);
    
    List<GitHubRepository> findByLastCheckedAtBefore(ZonedDateTime time);
}