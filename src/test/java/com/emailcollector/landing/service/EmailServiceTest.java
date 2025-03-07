package com.emailcollector.landing.service;

import com.emailcollector.landing.model.EmailSubmission;
import com.emailcollector.landing.repository.EmailRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmailServiceTest {

    @Mock
    private EmailRepository emailRepository;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private EmailService emailService;

    private EmailSubmission emailSubmission;

    @BeforeEach
    void setUp() {
        emailSubmission = new EmailSubmission();
        emailSubmission.setEmail("test@example.com");
    }

    @Test
    void testGetAllEmails() {
        EmailSubmission submission1 = new EmailSubmission();
        submission1.setEmail("email1@test.com");

        EmailSubmission submission2 = new EmailSubmission();
        submission2.setEmail("email2@test.com");

        List<EmailSubmission> expectedEmails = Arrays.asList(submission1, submission2);
        when(emailRepository.findAll()).thenReturn(expectedEmails);

        List<EmailSubmission> actualEmails = emailService.getAllEmails();

        assertEquals(2, actualEmails.size());
        assertEquals("email1@test.com", actualEmails.get(0).getEmail());
        assertEquals("email2@test.com", actualEmails.get(1).getEmail());
        verify(emailRepository).findAll();
    }

    @Test
    void testEmailExists() {
        String existingEmail = "exists@example.com";
        when(emailRepository.findByEmail(existingEmail)).thenReturn(Optional.of(new EmailSubmission()));

        boolean exists = emailService.emailExists(existingEmail);
        assertTrue(exists);
        verify(emailRepository).findByEmail(existingEmail);

        String nonExistingEmail = "nonexistent@example.com";
        when(emailRepository.findByEmail(nonExistingEmail)).thenReturn(Optional.empty());

        boolean doesNotExist = emailService.emailExists(nonExistingEmail);
        assertFalse(doesNotExist);
        verify(emailRepository).findByEmail(nonExistingEmail);
    }

    @Test
    void testGetSubmissionCount() {
        when(emailRepository.count()).thenReturn(5L);

        int count = emailService.getSubmissionCount();

        assertEquals(5, count);
        verify(emailRepository).count();
    }

    @Test
    void testProcessSubmission() {
        when(httpServletRequest.getRemoteAddr()).thenReturn("192.168.1.1");
        when(emailRepository.save(any(EmailSubmission.class))).thenReturn(emailSubmission);

        EmailSubmission result = emailService.processSubmission(emailSubmission, httpServletRequest,
                EmailSubmission.SubmissionSource.LANDING_PAGE);

        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        assertEquals("192.168.1.1", result.getIpAddress());
        assertEquals(EmailSubmission.SubmissionSource.LANDING_PAGE, result.getSource());
        assertNotNull(result.getCreationDate());
        verify(emailRepository).save(emailSubmission);
        verify(httpServletRequest).getRemoteAddr();
    }
}