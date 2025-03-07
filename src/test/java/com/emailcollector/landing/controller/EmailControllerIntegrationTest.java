package com.emailcollector.landing.controller;

import com.emailcollector.landing.model.EmailSubmission;
import com.emailcollector.landing.service.EmailService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmailController.class)
public class EmailControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmailService emailService;

    @Test
    void testShowLandingPage() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("emailSubmission"));
    }

    @Test
    void testSubmitEmail_Success() throws Exception {
        String email = "test@example.com";
        when(emailService.emailExists(email)).thenReturn(false);
        when(emailService.processSubmission(any(EmailSubmission.class), any(), eq(EmailSubmission.SubmissionSource.LANDING_PAGE)))
                .thenReturn(new EmailSubmission());

        mockMvc.perform(post("/submit")
                .param("email", email))
                .andExpect(status().isOk())
                .andExpect(view().name("success"));
    }

    @Test
    void testSubmitEmail_DuplicateEmail() throws Exception {
        String email = "duplicate@example.com";
        when(emailService.emailExists(email)).thenReturn(true);

        mockMvc.perform(post("/submit")
                .param("email", email))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("duplicateError", "This email is already registered"));
    }

    @Test
    void testSubmitEmail_InvalidEmail() throws Exception {
        mockMvc.perform(post("/submit")
                .param("email", "invalid-email"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().hasErrors());
    }

    @Test
    void testListAllEmails() throws Exception {
        EmailSubmission email1 = new EmailSubmission();
        email1.setEmail("email1@example.com");
        
        EmailSubmission email2 = new EmailSubmission();
        email2.setEmail("email2@example.com");
        
        when(emailService.getAllEmails()).thenReturn(Arrays.asList(email1, email2));
        when(emailService.getSubmissionCount()).thenReturn(2);

        mockMvc.perform(get("/emails"))
                .andExpect(status().isOk())
                .andExpect(view().name("emails"))
                .andExpect(model().attribute("totalCount", 2))
                .andExpect(model().attributeExists("emails"));
    }
}