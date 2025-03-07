package com.emailcollector.landing.controller;

import com.emailcollector.landing.model.EmailSubmission;
import com.emailcollector.landing.service.EmailService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmailRestControllerTest {

    @Mock
    private EmailService emailService;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private EmailRestController emailRestController;

    private EmailSubmission emailSubmission;

    @BeforeEach
    void setUp() {
        emailSubmission = new EmailSubmission();
        emailSubmission.setEmail("test@example.com");
    }

    @Test
    void testAddEmail_Success() {
        when(bindingResult.hasErrors()).thenReturn(false);
        when(emailService.emailExists(anyString())).thenReturn(false);
        when(emailService.processSubmission(any(EmailSubmission.class), any(HttpServletRequest.class), 
             eq(EmailSubmission.SubmissionSource.API))).thenReturn(emailSubmission);
        
        ResponseEntity<?> response = emailRestController.addEmail(emailSubmission, bindingResult, request);
        
        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        verify(emailService).emailExists("test@example.com");
        verify(emailService).processSubmission(eq(emailSubmission), eq(request), 
               eq(EmailSubmission.SubmissionSource.API));
    }
    
    @Test
    void testAddEmail_InvalidFormat() {
        when(bindingResult.hasErrors()).thenReturn(true);
        
        emailSubmission.setEmail("invalid-email");

        ResponseEntity<?> response = emailRestController.addEmail(emailSubmission, bindingResult, request);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        verify(emailService, never()).emailExists(anyString());
        verify(emailService, never()).processSubmission(any(), any(), any());
    }
    
    @Test
    void testAddEmail_DuplicateEmail() {
        when(bindingResult.hasErrors()).thenReturn(false);
        when(emailService.emailExists("test@example.com")).thenReturn(true);
        
        ResponseEntity<?> response = emailRestController.addEmail(emailSubmission, bindingResult, request);
        
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());

        verify(emailService).emailExists("test@example.com");
        verify(emailService, never()).processSubmission(any(), any(), any());
    }
}