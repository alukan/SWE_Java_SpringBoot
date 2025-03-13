package com.saas.app.service;

import com.saas.app.model.GitHubRepository;
import com.saas.app.model.RepoSubscription;
import com.saas.app.repository.RepoSubscriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class ScheduledTaskService {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledTaskService.class);

    private final RepoSubscriptionRepository subscriptionRepository;
    private final RepoService repoService;
    private final NotificationService notificationService;

    @Autowired
    public ScheduledTaskService(
            RepoSubscriptionRepository subscriptionRepository,
            RepoService repoService,
            NotificationService notificationService) {
        this.subscriptionRepository = subscriptionRepository;
        this.repoService = repoService;
        this.notificationService = notificationService;
    }

    /**
     * Scheduled task that checks repositories with notification-enabled
     * subscriptions
     * Scheduled to run every N minutes from application.properties, default 30
     * minutes
     */
    @Scheduled(fixedRateString = "${app.schedule.repository-check-minutes:30}", timeUnit = TimeUnit.MINUTES)
    public void checkRepositoriesForActivity() {
        logger.info("Starting scheduled repository activity check");

        List<RepoSubscription> activeSubscriptions = subscriptionRepository.findByNotificationsEnabledTrue();

        if (activeSubscriptions.isEmpty()) {
            logger.info("No active subscriptions found, skipping repository check");
            return;
        }

        logger.info("Found {} subscriptions with notifications enabled", activeSubscriptions.size());

        // Get unique repositories to avoid checking the same repository multiple times
        Set<GitHubRepository> repositoriesToCheck = new HashSet<>();
        for (RepoSubscription subscription : activeSubscriptions) {
            repositoriesToCheck.add(subscription.getRepository());
        }

        logger.info("Checking {} unique repositories for new activity", repositoriesToCheck.size());

        // Check each repository for updates
        for (GitHubRepository repository : repositoriesToCheck) {
            try {
                boolean hasNewActivity = repoService.checkForNewActivity(repository, 10);

                if (hasNewActivity) {
                    logger.info("New activity detected in repository {}/{}",
                            repository.getOwner(), repository.getName());

                    // Get all subscriptions for this repository that have notifications enabled
                    List<RepoSubscription> subscriptionsToNotify = activeSubscriptions.stream()
                            .filter(s -> s.getRepository().getId().equals(repository.getId()))
                            .filter(RepoSubscription::needsNotification)
                            .collect(Collectors.toList());

                    logger.info("Found {} subscriptions to notify about repository {}/{}",
                            subscriptionsToNotify.size(), repository.getOwner(), repository.getName());

                    for (RepoSubscription subscription : subscriptionsToNotify) {
                        String message = String.format("New activity detected in %s/%s", 
                                repository.getOwner(), repository.getName());
                        notificationService.createNotification(subscription, message);
                    }
                } else {
                    logger.debug("No new activity in repository {}/{}",
                            repository.getOwner(), repository.getName());
                }
            } catch (Exception e) {
                logger.error("Error checking repository {}/{} for activity: {}",
                        repository.getOwner(), repository.getName(), e.getMessage());
            }
        }

        logger.info("Completed scheduled repository activity check");
    }
}