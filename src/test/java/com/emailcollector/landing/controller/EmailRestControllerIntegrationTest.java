package com.emailcollector.landing.controller;

import com.emailcollector.landing.model.EmailSubmission;
import com.emailcollector.landing.service.EmailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmailRestController.class)
public class EmailRestControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmailService emailService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testAddEmail_Success() throws Exception {
        EmailSubmission submission = new EmailSubmission();
        submission.setEmail("test@example.com");
        
        when(emailService.emailExists("test@example.com")).thenReturn(false);
        when(emailService.processSubmission(any(EmailSubmission.class), any(), eq(EmailSubmission.SubmissionSource.API)))
                .thenReturn(submission);

        mockMvc.perform(post("/api/email")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(submission)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Email registered successfully"));
                
        verify(emailService).emailExists("test@example.com");
        verify(emailService).processSubmission(any(EmailSubmission.class), any(), eq(EmailSubmission.SubmissionSource.API));
    }

    @Test
    void testAddEmail_DuplicateEmail() throws Exception {
        EmailSubmission submission = new EmailSubmission();
        submission.setEmail("duplicate@example.com");
        
        when(emailService.emailExists("duplicate@example.com")).thenReturn(true);

        mockMvc.perform(post("/api/email")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(submission)))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Email already registered"));
                
        verify(emailService).emailExists("duplicate@example.com");
        verify(emailService, never()).processSubmission(any(), any(), any());
    }

    @Test
    void testAddEmail_InvalidEmail() throws Exception {
        EmailSubmission submission = new EmailSubmission();
        submission.setEmail("invalid-email");

        mockMvc.perform(post("/api/email")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(submission)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetAllEmails() throws Exception {
        EmailSubmission email1 = new EmailSubmission();
        email1.setEmail("email1@example.com");
        
        EmailSubmission email2 = new EmailSubmission();
        email2.setEmail("email2@example.com");
        
        List<EmailSubmission> emailList = Arrays.asList(email1, email2);
        
        when(emailService.getAllEmails()).thenReturn(emailList);

        mockMvc.perform(get("/api/emails")
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("email1@example.com"))
                .andExpect(jsonPath("$[1].email").value("email2@example.com"))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
                
        verify(emailService).getAllEmails();
    }
}