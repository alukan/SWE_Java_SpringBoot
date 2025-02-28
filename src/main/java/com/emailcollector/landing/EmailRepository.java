package com.emailcollector.landing;

import org.springframework.stereotype.Repository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class EmailRepository {

    private final List<EmailSubmission> submissions = new ArrayList<>();

    public void save(EmailSubmission submission) {
        submissions.add(submission);
    }

    public List<EmailSubmission> findAll() {
        return submissions;
    }
    
    public Optional<EmailSubmission> findByEmail(String email) {
        return submissions.stream()
                .filter(submission -> submission.getEmail().equals(email))
                .findFirst();
    }
    
    public int count() {
        return submissions.size();
    }
}