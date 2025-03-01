package com.emailcollector.landing.Controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.emailcollector.landing.EmailRepository;
import com.emailcollector.landing.EmailSubmission;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class EmailRestController {

    @Autowired
    private EmailRepository emailRepository;
    
    @PostMapping("/email")
    public ResponseEntity<?> addEmail(@Valid @RequestBody EmailSubmission emailSubmission, 
                                     BindingResult bindingResult, 
                                     HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        if (bindingResult.hasErrors()) {
            response.put("success", false);
            response.put("error", "Invalid email format");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        
        if (emailRepository.findByEmail(emailSubmission.getEmail()).isPresent()) {
            response.put("success", false);
            response.put("error", "Email already registered");
            return new ResponseEntity<>(response, HttpStatus.CONFLICT);
        }
        
        String ipAddress = request.getRemoteAddr();
        emailSubmission.setIpAddress(ipAddress);
        emailSubmission.setCreationDate(System.currentTimeMillis());
        emailSubmission.setSource("API");
        
        emailRepository.save(emailSubmission);
        
        response.put("success", true);
        response.put("message", "Email registered successfully");
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}