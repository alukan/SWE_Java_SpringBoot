# GitHub Activity and Subscription Service

This service provides endpoints to get activity of a single repository, subscribe to a repository, manage notifications, and list subscribed repositories. It also includes a scheduled task to poll updates and notify clients of new updates.

## Endpoints

### 1. Get Repository Activity

Endpoints are available to fetch issues, releases, commits, and pull requests for a given repository.

#### Example:
```bash
curl -X GET "http://localhost:8080/api/github/issues/octocat/Hello-World?limit=10"
```

### 2. Manage Repository Subscriptions

#### Subscribe to a Repository
```bash
curl -X POST "http://localhost:8080/api/subscription/repository/octocat/Hello-World?email=user@example.com"
```

#### Unsubscribe from a Repository
```bash
curl -X DELETE "http://localhost:8080/api/subscription/repository/octocat/Hello-World?email=user@example.com"
```

#### Enable Notifications
```bash
curl -X PATCH "http://localhost:8080/api/subscription/repository/octocat/Hello-World/notifications/enable?email=user@example.com"
```

#### Disable Notifications
```bash
curl -X PATCH "http://localhost:8080/api/subscription/repository/octocat/Hello-World/notifications/disable?email=user@example.com"
```

### 3. List Subscribed Repositories

#### Get User Subscriptions
```bash
curl -X GET "http://localhost:8080/api/subscription/repository?email=user@example.com"
```

#### Get Repository Subscriptions
```bash
curl -X GET "http://localhost:8080/api/subscription/repository/octocat/Hello-World"
```

### 4. Manage Notifications

#### Get Notifications
```bash
curl -X GET "http://localhost:8080/api/notifications?email=user@example.com"
```

#### Clear Notifications
```bash
curl -X POST "http://localhost:8080/api/notifications/clear?email=user@example.com"
```

## Scheduled Task

A scheduled task runs at a configured interval to poll updates from subscribed repositories. If a new update is detected (newer than the last saved update), a notification is saved. Clients need to poll the notification endpoint to get the latest notifications.

## Configuration

The repository check interval can be configured in the `application.properties` file:
```properties
# Repository check interval in minutes
app.schedule.repository-check-minutes=1
```

This configuration sets the interval at which the scheduled task will run to check for updates in the subscribed repositories.
