package com.saas.app.controller;

import com.saas.app.service.GitHubService;
import com.saas.app.util.GitHubErrorHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        
        logger.info("Fetching GitHub activities for {}/{} with limit {}", owner, repo, limit);
        return GitHubErrorHandler.executeWithErrorHandling(
                () -> gitHubService.getRepositoryActivities(owner, repo, limit),
                "activities",
                logger
        );
    }

    @GetMapping("/commits/{owner}/{repo}")
    public ResponseEntity<?> getCommits(
            @PathVariable String owner,
            @PathVariable String repo,
            @RequestParam(defaultValue = "30") int limit) {
        
        logger.info("Fetching GitHub commits for {}/{} with limit {}", owner, repo, limit);
        return GitHubErrorHandler.executeWithErrorHandling(
                () -> gitHubService.getCommits(owner, repo, limit),
                "commits",
                logger
        );
    }

    @GetMapping("/pull-requests/{owner}/{repo}")
    public ResponseEntity<?> getPullRequests(
            @PathVariable String owner,
            @PathVariable String repo,
            @RequestParam(defaultValue = "30") int limit) {
        
        logger.info("Fetching GitHub pull requests for {}/{} with limit {}", owner, repo, limit);
        return GitHubErrorHandler.executeWithErrorHandling(
                () -> gitHubService.getPullRequests(owner, repo, limit),
                "pull requests",
                logger
        );
    }

    @GetMapping("/issues/{owner}/{repo}")
    public ResponseEntity<?> getIssues(
            @PathVariable String owner,
            @PathVariable String repo,
            @RequestParam(defaultValue = "30") int limit) {
        
        logger.info("Fetching GitHub issues for {}/{} with limit {}", owner, repo, limit);
        return GitHubErrorHandler.executeWithErrorHandling(
                () -> gitHubService.getIssues(owner, repo, limit),
                "issues",
                logger
        );
    }

    @GetMapping("/releases/{owner}/{repo}")
    public ResponseEntity<?> getReleases(
            @PathVariable String owner,
            @PathVariable String repo,
            @RequestParam(defaultValue = "30") int limit) {
        
        logger.info("Fetching GitHub releases for {}/{} with limit {}", owner, repo, limit);
        return GitHubErrorHandler.executeWithErrorHandling(
                () -> gitHubService.getReleases(owner, repo, limit),
                "releases",
                logger
        );
    }
}