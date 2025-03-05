package com.emailcollector.landing.repository;

import com.emailcollector.landing.model.EmailSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmailRepository extends JpaRepository<EmailSubmission, Long> {
    Optional<EmailSubmission> findByEmail(String email);
}
