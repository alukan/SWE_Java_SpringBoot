package com.saas.app.controller;

import com.saas.app.exception.GitHubApiException;
import com.saas.app.model.GitHubActivity;
import com.saas.app.service.GitHubService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/github")
public class GitHubActivityController {
    
    private static final Logger logger = LoggerFactory.getLogger(GitHubActivityController.class);
    
    @Autowired
    private GitHubService gitHubService;
    
    @GetMapping("/activities/{owner}/{repo}")
    public ResponseEntity<?> getRepositoryActivities(
            @PathVariable String owner,
            @PathVariable String repo,
            @RequestParam(defaultValue = "30") int limit) {
        
        try {
            logger.info("Fetching GitHub activities for {}/{} with limit {}", owner, repo, limit);
            List<GitHubActivity> activities = gitHubService.getRepositoryActivities(owner, repo, limit);
            return ResponseEntity.ok(activities);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid request parameters: {}", e.getMessage());
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        } catch (GitHubApiException e) {
            logger.error("GitHub API error", e);
            return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("error", "Failed to fetch GitHub activities", 
                             "message", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error fetching GitHub activities", e);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "An unexpected error occurred"));
        }
    }
}