package com.saas.app.service;

import com.saas.app.exception.GitHubApiException;
import com.saas.app.model.GitHubActivity;
import org.kohsuke.github.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class GitHubService {
    private static final Logger logger = LoggerFactory.getLogger(GitHubService.class);

    @Value("${GITHUB_TOKEN:#{null}}")
    private String githubToken;

    private GitHub connectToGitHub() {
        try {
            if (githubToken != null && !githubToken.isEmpty()) {
                logger.debug("Connecting to GitHub with authentication token");
                return new GitHubBuilder().withOAuthToken(githubToken).build();
            } else {
                logger.warn("Connecting to GitHub anonymously - rate limits will be lower");
                return GitHub.connectAnonymously();
            }
        } catch (IOException e) {
            logger.error("Failed to connect to GitHub API", e);
            throw new GitHubApiException("Failed to connect to GitHub API", e);
        }
    }

    private GHRepository getRepository(String owner, String repo) {
        try {
            GitHub github = connectToGitHub();
            return github.getRepository(owner + "/" + repo);
        } catch (IOException e) {
            logger.error("Failed to fetch repository {}/{}", owner, repo, e);
            throw new GitHubApiException("Failed to fetch repository: " + e.getMessage(), e);
        }
    }

    public List<GitHubActivity> getCommits(String owner, String repo, int limit) {
        validateParams(owner, repo, limit);
        GHRepository repository = getRepository(owner, repo);

        return repository.listCommits()
                .withPageSize(limit)
                .iterator()
                .nextPage().stream()
                .map(commit -> {
                    try {
                        return new GitHubActivity(
                                "commit",
                                repository.getName(),
                                commit.getAuthor() != null ? commit.getAuthor().getLogin() : "Unknown",
                                commit.getCommitShortInfo().getMessage(),
                                commit.getHtmlUrl().toString(),
                                commit.getCommitDate().toInstant().atZone(ZoneId.systemDefault()));
                    } catch (IOException e) {
                        logger.warn("Error retrieving commit details", e);
                        return null;
                    }
                })
                .filter(a -> a != null)
                .collect(Collectors.toList());
    }

    public List<GitHubActivity> getPullRequests(String owner, String repo, int limit) {
        validateParams(owner, repo, limit);
        GHRepository repository = getRepository(owner, repo);

        try {
            return repository.getPullRequests(GHIssueState.ALL)
                    .stream()
                    .limit(limit)
                    .map(pr -> {
                        try {
                            return new GitHubActivity(
                                    "pull_request",
                                    repository.getName(),
                                    pr.getUser().getLogin(),
                                    pr.getTitle(),
                                    pr.getHtmlUrl().toString(),
                                    pr.getCreatedAt().toInstant().atZone(ZoneId.systemDefault()));
                        } catch (IOException e) {
                            logger.warn("Error retrieving PR details", e);
                            return null;
                        }
                    })
                    .filter(a -> a != null)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            logger.error("Failed to fetch pull requests for {}/{}", owner, repo, e);
            throw new GitHubApiException("Failed to fetch pull requests", e);
        }
    }

    public List<GitHubActivity> getIssues(String owner, String repo, int limit) {
        validateParams(owner, repo, limit);
        GHRepository repository = getRepository(owner, repo);

        try {
            return repository.getIssues(GHIssueState.ALL)
                    .stream()
                    .filter(issue -> !issue.isPullRequest())
                    .limit(limit)
                    .map(issue -> {
                        try {
                            return new GitHubActivity(
                                    "issue",
                                    repository.getName(),
                                    issue.getUser().getLogin(),
                                    issue.getTitle(),
                                    issue.getHtmlUrl().toString(),
                                    issue.getCreatedAt().toInstant().atZone(ZoneId.systemDefault()));
                        } catch (IOException e) {
                            logger.warn("Error retrieving issue details", e);
                            return null;
                        }
                    })
                    .filter(a -> a != null)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            logger.error("Failed to fetch issues for {}/{}", owner, repo, e);
            throw new GitHubApiException("Failed to fetch issues", e);
        }
    }

    public List<GitHubActivity> getReleases(String owner, String repo, int limit) {
        validateParams(owner, repo, limit);
        GHRepository repository = getRepository(owner, repo);

        try {
            return repository.listReleases()
                    .toList()
                    .stream()
                    .limit(limit)
                    .map(release -> {
                        try {
                            String htmlUrl = release.getHtmlUrl().toString();
                            String name = release.getName() != null ? release.getName() : "Unnamed release";

                            return new GitHubActivity(
                                    "release",
                                    repository.getName(),
                                    repository.getOwnerName(),
                                    name,
                                    htmlUrl,
                                    release.getPublished_at() != null
                                            ? release.getPublished_at().toInstant().atZone(ZoneId.systemDefault())
                                            : java.time.ZonedDateTime.now());
                        } catch (Exception e) {
                            logger.warn("Unexpected error processing release: {}", e.getMessage());
                            return null;
                        }
                    })
                    .filter(a -> a != null)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            logger.error("Failed to fetch releases for {}/{}", owner, repo, e);
            throw new GitHubApiException("Failed to fetch releases", e);
        }
    }

    public List<GitHubActivity> getRepositoryActivities(String owner, String repo, int limit) {
        validateParams(owner, repo, limit);

        List<GitHubActivity> commits = getCommits(owner, repo, limit);
        List<GitHubActivity> prs = getPullRequests(owner, repo, limit);
        List<GitHubActivity> issues = getIssues(owner, repo, limit);
        List<GitHubActivity> releases = getReleases(owner, repo, limit);

        // Combine and sort by date (newest first)
        return Stream.of(commits, prs, issues, releases)
                .flatMap(List::stream)
                .sorted(Comparator.comparing(GitHubActivity::getCreatedAt).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    private void validateParams(String owner, String repo, int limit) {
        if (owner == null || owner.trim().isEmpty()) {
            throw new IllegalArgumentException("Repository owner cannot be empty");
        }
        if (repo == null || repo.trim().isEmpty()) {
            throw new IllegalArgumentException("Repository name cannot be empty");
        }
        if (limit <= 0 || limit > 100) {
            throw new IllegalArgumentException("Limit must be between 1 and 100");
        }
    }

    /**
     * Validates if a repository exists and is accessible
     */
    public boolean validateRepository(String owner, String repo) {
        validateParams(owner, repo, 1);  // Use minimum limit just for validation
        try {
            getRepository(owner, repo);
            return true;
        } catch (Exception e) {
            logger.warn("Repository validation failed for {}/{}: {}", owner, repo, e.getMessage());
            return false;
        }
    }
}