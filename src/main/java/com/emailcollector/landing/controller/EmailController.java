package com.emailcollector.landing.controller;

import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;

import com.emailcollector.landing.service.EmailService;
import com.emailcollector.landing.model.EmailSubmission;

import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class EmailController {

    private final EmailService emailService;

    @Autowired
    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @GetMapping("/")
    public String showLandingPage(Model model) {
        model.addAttribute("emailSubmission", new EmailSubmission());
        return "index";
    }

    @PostMapping("/submit")
    public String submitEmail(@Valid EmailSubmission emailSubmission, BindingResult bindingResult, 
                             HttpServletRequest request, Model model) {
        if (bindingResult.hasErrors()) {
            return "index";
        }
        
        if (emailService.emailExists(emailSubmission.getEmail())) {
            model.addAttribute("duplicateError", "This email is already registered");
            return "index";
        }

        emailService.processSubmission(emailSubmission, request, 
                                     EmailSubmission.SubmissionSource.LANDING_PAGE);
        return "success";
    }
    
    @GetMapping("/emails")
    public String listAllEmails(Model model) {
        model.addAttribute("emails", emailService.getAllEmails());
        model.addAttribute("totalCount", emailService.getSubmissionCount());
        return "emails";
    }
}