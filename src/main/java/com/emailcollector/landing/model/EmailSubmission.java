package com.emailcollector.landing.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@Entity
@Table(name = "email_submissions")
public class EmailSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty(message = "Email cannot be empty")
    @Email(message = "Please provide a valid email address")
    @Column(unique = true, nullable = false)
    private String email;

    private long creationDate;

    private String ipAddress;

    @Enumerated(EnumType.STRING)
    private SubmissionSource source = SubmissionSource.LANDING_PAGE;
    
    public Date getFormattedDate() {
        return new Date(creationDate);
    }
    
    public enum SubmissionSource {
        LANDING_PAGE,
        API
    }
}
