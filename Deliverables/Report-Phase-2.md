# Project - Phase 2

| Name | Student Number |
| --- | ---: |
| Diogo Martins | 1221223 |
| Francisco Osorio | 1220846 |
| Joao Pinto | 1220663 |
| Francisco Reis | 1201373 |
| Marco Marques | 1250685 |

# Introduction

In this report, its presentend the phase 2 of the DESOFS project, which consists in documenting operations related to the development process, CI/CD pipeline and security practices.

The main topics covered in this report include the development process and conventions, such as commit message strategy, pull request guidelines, code review process, branch naming strategy and the CI/CD pipeline.

# Development Process & Conventions

To maintain high code quality, improve collaboration, and ensure consistency across the team, we have established a comprehensive set of development conventions and processes. These guidelines cover how we structure our commits, manage pull requests, conduct code reviews, and organize our branching strategy. By adhering to these practices, we ensure that our codebase remains clean, maintainable, and easy to navigate for all team members.

## Commit Message Strategy

Our team follows the conventional commits format to maintain clear and consistent commit history. The structure of our commit messages is:

```
<type>: <description>
```

Where `<type>` can be:
- **feat**: A new feature or functionality
- **fix**: A bug fix
- **docs**: Documentation changes
- **refactor**: Code refactoring without changing functionality
- **test**: Adding or updating tests
- **chore**: Maintenance tasks and dependencies

**Example:**
```
feat: implement user authentication system
fix: resolve login validation bug
docs: update API documentation
```

<img src="./images/phase-2/commit-message.png" alt="Commit Message Example" width="800">

This convention ensures that the commit history is readable, searchable, and can be automated for changelog generation.

## Pull Request Guidelines

Each pull request should follow these guidelines to ensure quality and clarity:

1. **Title**: A clear and descriptive title that summarizes the changes
   - Should be concise and match the commit convention when possible
   - Example: "feat: implement user authentication" or "fix: resolve database connection issue"

2. **Description**: A comprehensive description of what was developed
   - Main points and changes implemented
   - Motivation or reason for the changes
   - Any relevant context for reviewers

3. **Assignment**: 
   - Assign the PR to the developer who created it
   - This ensures clear responsibility and accountability

4. **Reviewers**: 
   - Add team members who should review the code
   - Include Copilot for automated code analysis when relevant

5. **Labels**: Use appropriate labels (e.g., bug, enhancement, documentation) to categorize the PR

<img src="./images/phase-2/pull-request.png" alt="Pull Request Example" width="800">

## Code Review Process

Our code review process ensures code quality and knowledge sharing across the team:

1. **Review Requirements**:
   - A minimum of **one approval** is required before merging a PR
   - All conversations and suggested changes must be addressed

2. **Reviewer Responsibilities**:
   - Examine code for correctness, style, and best practices
   - Verify that the changes align with project requirements
   - Check for potential bugs, performance issues, and security vulnerabilities
   - Provide constructive feedback and suggestions for improvement

3. **Automated Reviews**:
   - **GitHub Copilot** is integrated into the review process
   - Copilot provides automated code analysis and suggestions
   - Helps identify issues and improves code quality
   - Complements manual reviews by developers

4. **Approval & Merge**:
   - Once the PR is approved and all checks pass, the PR can be merged
   - The author or reviewer can perform the merge

## Branch Merging Strategy

Our team follows a controlled merging strategy to ensure stability and quality across branches:

1. **Development Flow**:
   - Feature branches (`feat/`, `fix/`, etc.) are created from the `dev` branch
   - PRs from feature branches are merged into the `dev` branch after approval

2. **Dev Branch**:
   - Acts as an integration branch for all features and fixes
   - All changes are tested and validated in the dev environment
   - Must be in a working, stable state at all times

3. **Main Branch Deployment**:
   - Once all changes are working correctly on the `dev` branch
   - A final PR is created to merge `dev` into `main`
   - This ensures `main` always contains production-ready code
   - Deployments to production are made from the `main` branch

4. **Branch Hierarchy**:
   ```
   feature/something --> dev --> main
           (PR #1)     (PR #2)
   ```

## Branch Naming Strategy

Branch names follow a structured format similar to commits to maintain consistency and clarity:

```
<type>/<description>
```

Where `<type>` matches the commit type:
- **feat/**: Feature branches (e.g., `feat/user-authentication`)
- **fix/**: Bug fix branches (e.g., `fix/login-validation`)
- **docs/**: Documentation branches (e.g., `docs/api-guide`)
- **refactor/**: Refactoring branches (e.g., `refactor/database-layer`)
- **test/**: Test branches (e.g., `test/integration-tests`)
- **chore/**: Maintenance branches (e.g., `chore/update-dependencies`)

**Examples:**
- `feat/payment-integration`
- `fix/session-timeout-bug`
- `docs/setup-instructions`

<img src="./images/phase-2/branch-naming.png" alt="Branch Naming Example" width="800">

This naming convention makes it easy to identify the purpose of a branch and correlate it with related commits and PRs.

# CI/CD Pipeline

## Pipeline Overview

Our CI/CD pipeline follows a structured approach with different workflows triggered at different stages of the development process:

### Main Pipeline

### Dev Pipeline

### Feature Pipeline

## Build & Test

## Security Secrets Management

## Dependency Scanning

Dependency scanning is a critical security practice that automatically checks all project dependencies for known vulnerabilities. Our team uses **Snyk** to scan Maven dependencies and identify potential security risks in the codebase. The scanning process is triggered on every workflow call and analyzes the `pom.xml` file to detect vulnerable packages. Results are automatically uploaded to GitHub's security dashboard in SARIF format, providing visibility to the entire development team. We have configured the scan to only block the pipeline on critical severity vulnerabilities, allowing us to prioritize and fix the most important issues first.

The Snyk integration is seamlessly integrated into our CI/CD pipeline through a dedicated workflow that executes the security scan and reports findings. The workflow runs the Snyk CLI against our Maven dependencies, sets a critical severity threshold, and uploads the results to GitHub's native security features. This allows our team to track vulnerabilities over time, receive notifications when new issues are discovered, and maintain a dashboard view of our security posture.

### Workflow Implementation

```yaml
- name: Snyk Dependency Scan
  run: |
    snyk test \
      --org=2324a6a0-65da-44fb-922e-340f88ffea53 \
      --severity-threshold=critical \
      --file=backend/pom.xml \
      --sarif-file-output=snyk.sarif
  env:
    SNYK_TOKEN: ${{ secrets.SNYK_TOKEN }}

- name: Upload Dependency Results
  if: always()
  uses: github/codeql-action/upload-sarif@v3
  with:
    sarif_file: snyk.sarif
```

<img src="./images/phase-2/critical-vulnerability-snyk.png" alt="Snyk Vulnerability Example" width="800">

<img src="./images/phase-2/snyk-interface.png" alt="Snyk Dashboard" width="800">

## SAST

## DAST

## Container Security

## Docker Image Publishing

Docker image publishing is a critical part of our deployment pipeline that automates the building and pushing of Docker images to DockerHub. This ensures that every release and main branch deployment has a corresponding container image available for deployment. Our workflow is triggered automatically on git version tags (e.g., `v1.0.0`) and can also be manually invoked. The Docker build process uses the `backend/` directory and `backend/Dockerfile` as the build context, and we build for multiple architectures including both `linux/amd64` and `linux/arm64` to support various deployment environments.

The tagging strategy is intelligent and automated, allowing us to tag images in multiple ways depending on the trigger context. Every build receives a commit SHA tag (`sha-{commit-hash}`), release tags are applied when pushing version tags, and the special `latest` tag is applied to images built from the main branch. Our workflow leverages Docker Buildx for multi-platform builds and GitHub Actions cache for improved build performance. All credentials are securely stored in GitHub Secrets, and the workflow is reusable, allowing it to be called from other workflows in our pipeline.

### Workflow Implementation

```yaml
- name: Set up Docker Buildx
  uses: docker/setup-buildx-action@v3

- name: Login to DockerHub
  uses: docker/login-action@v3
  with:
    username: ${{ secrets.DOCKERHUB_USERNAME }}
    password: ${{ secrets.DOCKERHUB_TOKEN }}

- name: Build and push Docker image
  uses: docker/build-push-action@v5
  with:
    context: backend
    file: backend/Dockerfile
    platforms: linux/amd64,linux/arm64
    push: true
    tags: ${{ steps.meta.outputs.tags }}
    cache-from: type=gha
    cache-to: type=gha,mode=max
```

<img src="./images/phase-2/docker-hub.png" alt="DockerHub Repository" width="800">

## Deployment

Deployment to production occurs when changes are ready on the main branch. The deployment process is fully automated through a secure SSH connection to the production server, where it pulls the latest Docker image and starts containers with proper configuration. Our deployment workflow handles all the necessary steps including system cleanup to remove unused Docker images and containers, authentication to DockerHub, and network setup to ensure the backend container runs in an isolated network. The process creates a `techstore-net` Docker network if it doesn't already exist, maintaining security through network isolation while ensuring seamless communication between services.

The deployment process carefully removes old containers before starting new ones. The backend container is configured with the `restart unless-stopped` policy to ensure it automatically recovers from unexpected crashes, and it runs with environment configuration loaded from `/opt/techstore/backend.env`. All sensitive credentials including SSH keys, DockerHub tokens, and server details are securely stored in GitHub Secrets, ensuring that production credentials are never exposed in the workflow code.

### Workflow Implementation

```yaml
- name: Deploy via SSH
  uses: appleboy/ssh-action@v1.0.3
  with:
    host: ${{ secrets.SSH_HOST }}
    port: 22
    username: ${{ secrets.SSH_USER }}
    key: ${{ secrets.SSH_KEY }}
    script: |
      set -e
      
      sudo docker system prune -af
      echo "${{ secrets.DOCKERHUB_TOKEN }}" | sudo docker login -u "${{ secrets.DOCKERHUB_USERNAME }}" --password-stdin
      sudo docker network inspect techstore-net >/dev/null 2>&1 || sudo docker network create techstore-net
      
      BACKEND_IMAGE="${{ secrets.DOCKERHUB_USERNAME }}/techstore:latest"
      sudo docker pull "$BACKEND_IMAGE"

- name: Start containers
  uses: appleboy/ssh-action@v1.0.3
  with:
    host: ${{ secrets.SSH_HOST }}
    port: 22
    username: ${{ secrets.SSH_USER }}
    key: ${{ secrets.SSH_KEY }}
    script: |
      set -e
      
      BACKEND_IMAGE="${{ secrets.DOCKERHUB_USERNAME }}/techstore:latest"
      
      sudo docker rm -f techstore || true
      sudo docker run -d \
        --name techstore \
        --restart unless-stopped \
        --network techstore-net \
        -p 8080:8081 \
        --env-file /opt/techstore/backend.env \
        "$BACKEND_IMAGE"
      
      sudo docker image prune -f
```

<img src="./images/phase-2/health-endpoint.png" alt="Health Endpoint Response" width="800">

## Release Please
