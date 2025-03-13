package com.saas.app.service;

import com.saas.app.model.GitHubActivity;
import com.saas.app.model.GitHubRepository;
import com.saas.app.repository.RepositoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class RepoService {
    
    private static final Logger logger = LoggerFactory.getLogger(RepoService.class);
    
    @Autowired
    private RepositoryRepository repositoryRepository;
    
    @Autowired
    private GitHubService gitHubService;
    
    /**
     * Gets or creates a repository by owner and name
     * 
     * @param owner Repository owner
     * @param name Repository name
     * @return The repository entity
     */
    @Transactional
    public GitHubRepository getOrCreateRepository(String owner, String name) {
        Optional<GitHubRepository> existingRepo = repositoryRepository.findByOwnerAndName(owner, name);
        
        if (existingRepo.isPresent()) {
            return existingRepo.get();
        }
        
        // Validate repository exists on GitHub before creating
        if (!gitHubService.validateRepository(owner, name)) {
            throw new IllegalArgumentException("Repository " + owner + "/" + name + " does not exist or is not accessible");
        }
        
        GitHubRepository repository = new GitHubRepository(owner, name);
        return repositoryRepository.save(repository);
    }
    
    /**
     * Checks for new activity in a repository
     * 
     * @param repository The repository to check
     * @param limit Maximum number of activities to fetch
     * @return true if new activity was found
     */
    @Transactional
    public boolean checkForNewActivity(GitHubRepository repository, int limit) {
        repository.markAsChecked();
        
        List<GitHubActivity> activities = gitHubService.getRepositoryActivities(
                repository.getOwner(), repository.getName(), limit);
        
        if (!activities.isEmpty()) {
            ZonedDateTime latestActivity = activities.get(0).getCreatedAt();
            
            // If this is first check or we found newer activity
            if (repository.getLastActivityAt() == null || 
                latestActivity.isAfter(repository.getLastActivityAt())) {
                
                repository.setLastActivityAt(latestActivity);
                repository.markActivity();
                repositoryRepository.save(repository);
                
                logger.info("New activity found in repository {}/{}", 
                        repository.getOwner(), repository.getName());
                return true;
            }
        }
        
        repositoryRepository.save(repository);
        return false;
    }
    
    /**
     * Gets repositories that need to be checked for updates
     * 
     * @param minutesSinceLastCheck Minutes since repositories were last checked
     * @return List of repositories to check
     */
    public List<GitHubRepository> getRepositoriesToCheck(int minutesSinceLastCheck) {
        ZonedDateTime checkBefore = ZonedDateTime.now().minusMinutes(minutesSinceLastCheck);
        return repositoryRepository.findByLastCheckedAtBefore(checkBefore);
    }
}