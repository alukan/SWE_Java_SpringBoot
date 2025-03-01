package com.emailcollector.landing.Controller;

import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;

import com.emailcollector.landing.EmailRepository;
import com.emailcollector.landing.EmailSubmission;

import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class EmailController {

    @Autowired
    private EmailRepository emailRepository;

    @GetMapping("/")
    public String showLandingPage(Model model) {
        model.addAttribute("emailSubmission", new EmailSubmission());
        return "index";
    }

    @PostMapping("/submit")
    public String submitEmail(@Valid EmailSubmission emailSubmission, BindingResult bindingResult, HttpServletRequest request, Model model) {
        if (bindingResult.hasErrors()) {
            return "index";
        }
        
        if (emailRepository.findByEmail(emailSubmission.getEmail()).isPresent()) {
            model.addAttribute("duplicateError", "This email is already registered");
            return "index";
        }

        String ipAddress = request.getRemoteAddr();
        emailSubmission.setIpAddress(ipAddress);
        emailSubmission.setCreationDate(System.currentTimeMillis());

        emailRepository.save(emailSubmission);
        return "success";
    }
    
    @GetMapping("/emails")
    public String listAllEmails(Model model) {
        model.addAttribute("emails", emailRepository.findAll());
        model.addAttribute("totalCount", emailRepository.count());
        return "emails";
    }
}