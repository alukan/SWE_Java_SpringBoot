package com.emailcollector.landing.service;

import com.emailcollector.landing.model.EmailSubmission;
import com.emailcollector.landing.repository.EmailRepository;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EmailService {
    
    private final EmailRepository emailRepository;
    
    public void saveEmail(EmailSubmission emailSubmission) {
        emailRepository.save(emailSubmission);
    }
    
    public List<EmailSubmission> getAllEmails() {
        return emailRepository.findAll();
    }
    
    public boolean emailExists(String email) {
        return emailRepository.findByEmail(email).isPresent();
    }
    
    public int getSubmissionCount() {
        return (int) emailRepository.count();
    }
    
    public Optional<EmailSubmission> findByEmail(String email) {
        return emailRepository.findByEmail(email);
    }
    
    public EmailSubmission processSubmission(EmailSubmission emailSubmission, HttpServletRequest request, 
                                            EmailSubmission.SubmissionSource source) {
        String ipAddress = request.getRemoteAddr();
        emailSubmission.setIpAddress(ipAddress);
        emailSubmission.setCreationDate(System.currentTimeMillis());
        emailSubmission.setSource(source);
        
        emailRepository.save(emailSubmission);
        return emailSubmission;
    }
}