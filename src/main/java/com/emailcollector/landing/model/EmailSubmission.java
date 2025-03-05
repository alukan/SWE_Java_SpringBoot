package com.emailcollector.landing.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
public class EmailSubmission {

    @NotEmpty(message = "Email cannot be empty")
    @Email(message = "Please provide a valid email address")
    private String email;
    
    private long creationDate;
    
    private String ipAddress;
    
    private SubmissionSource source = SubmissionSource.LANDING_PAGE;
    
    public Date getFormattedDate() {
        return new Date(creationDate);
    }
    
    public enum SubmissionSource {
        LANDING_PAGE,
        API
    }
}