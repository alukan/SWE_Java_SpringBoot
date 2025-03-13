package com.saas.app.model;

import java.time.ZonedDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GitHubActivity {
    private String type;
    private String repositoryName;
    private String actor;
    private String title;
    private String url;
    private ZonedDateTime createdAt;
}