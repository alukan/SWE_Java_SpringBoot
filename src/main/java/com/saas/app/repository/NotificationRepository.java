package com.saas.app.repository;

import com.saas.app.model.RepoNotification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<RepoNotification, Long> {
    
    Page<RepoNotification> findByEmail(String email, Pageable pageable);
    
    List<RepoNotification> findByEmailAndReadFalseOrderByCreatedAtDesc(String email);
    
    Optional<RepoNotification> findByIdAndEmail(Long id, String email);
    
    int countByEmailAndReadFalse(String email);
    
    void deleteByEmail(String email);
}