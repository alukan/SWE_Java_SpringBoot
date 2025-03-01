package com.emailcollector.landing;

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
    
    private String source = "landing";
    
    public Date getFormattedDate() {
        return new Date(creationDate);
    }
}