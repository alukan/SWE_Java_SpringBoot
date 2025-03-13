package com.saas.app.util;

import com.saas.app.exception.GitHubApiException;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.function.Supplier;


public class GitHubErrorHandler {

    /**
     * Executes the given operation and handles any exceptions in a standardized way.
     *
     * @param operation    The operation to execute
     * @param resourceType The type of GitHub resource being accessed (e.g., "commits", "issues")
     * @param logger       The logger to use for logging errors
     * @param <T>          The return type of the operation
     * @return A ResponseEntity with either the operation result or an error response
     */
    public static <T> ResponseEntity<?> executeWithErrorHandling(
            Supplier<T> operation,
            String resourceType,
            Logger logger) {

        try {
            T result = operation.get();
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid request parameters: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (GitHubApiException e) {
            logger.error("GitHub API error when fetching {}: {}", resourceType, e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of(
                            "error", "Failed to fetch GitHub " + resourceType,
                            "message", e.getMessage()
                    ));
        } catch (Exception e) {
            logger.error("Unexpected error fetching GitHub {}: {}", resourceType, e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred"));
        }
    }
}