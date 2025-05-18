# Reusable GitHub Action Workflows

This directory contains reusable workflows for common tasks in the CI/CD pipeline.

## Available Workflows

### 1. Gradle Setup (`gradle-setup.yml`)

Sets up the Gradle environment, validates and fixes the Gradle wrapper if needed.

**Usage Example:**
```yaml
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
        with:
          fetch-depth: 0
          lfs: true

      - name: Set up Gradle
        uses: ./.github/workflows/reusable/gradle-setup.yml
        with:
          working-directory: ./backend/autotrader-backend
          java-version: '17'
          gradle-version: '8.5'
          cache: true
```

**Inputs:**
- `working-directory` (required): Path to the directory containing the Gradle wrapper
- `java-version` (optional, default: '17'): Java version to use
- `distribution` (optional, default: 'temurin'): Java distribution to use
- `gradle-version` (optional, default: '8.5'): Gradle version to use if wrapper needs to be fixed
- `cache` (optional, default: true): Whether to cache Gradle dependencies
- `cache-read-only` (optional, default: false): Whether the Gradle cache should be read-only

### 2. Docker Services Setup (`docker-services-setup.yml`)

Sets up Docker services using docker-compose and waits for them to be ready.

**Usage Example:**
```yaml
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      
      - name: Set up Docker Services
        uses: ./.github/workflows/reusable/docker-services-setup.yml
        with:
          docker-compose-file: './backend/autotrader-backend/docker-compose.dev.yml'
          services: 'db minio redis'
          wait-time: 60
```

**Inputs:**
- `docker-compose-file` (optional, default: './backend/autotrader-backend/docker-compose.dev.yml'): Path to the docker-compose file
- `down-flags` (optional, default: "--volumes"): Flags to pass to docker-compose down
- `services` (optional, default: "db minio createbuckets redis"): Space-separated list of services to start
- `wait-time` (optional, default: 60): Time in seconds to wait for services to initialize
- `db-container-name` (optional, default: "db"): Name of the database container
- `db-user` (optional, default: "autotrader"): Database user
- `minio-health-check` (optional, default: true): Whether to check MinIO health
- `minio-health-url` (optional, default: "http://localhost:9000/minio/health/live"): URL to check MinIO health

### 3. Spring Boot Build and Start (`spring-boot-setup.yml`)

Builds and starts a Spring Boot application, waiting for it to be ready.

**Usage Example:**
```yaml
jobs:
  api-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      
      - name: Set up Docker Services
        uses: ./.github/workflows/reusable/docker-services-setup.yml
      
      - name: Set up Gradle
        uses: ./.github/workflows/reusable/gradle-setup.yml
        with:
          working-directory: ./backend/autotrader-backend
      
      - name: Build and Start Spring Boot
        uses: ./.github/workflows/reusable/spring-boot-setup.yml
        with:
          working-directory: ./backend/autotrader-backend
          spring-profiles: 'dev'
          debug-mode: true
```

**Inputs:**
- `working-directory` (required): Path to the Spring Boot application directory
- `build-args` (optional, default: "build -x test"): Gradle build arguments
- `spring-profiles` (optional, default: "dev"): Spring profiles to activate
- `debug-mode` (optional, default: true): Whether to enable debug logging
- `wait-retries` (optional, default: 45): Number of retries for health check
- `wait-time` (optional, default: 10): Time in seconds between retries
- `health-check-path` (optional, default: "/actuator/health"): Path for health check
- `skip-build` (optional, default: false): Whether to skip build step

### 4. Postman Tests (`postman-tests.yml`)

Runs Postman API tests using Newman.

**Usage Example:**
```yaml
jobs:
  api-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      
      - name: Set up Docker Services
        uses: ./.github/workflows/reusable/docker-services-setup.yml
      
      - name: Set up Gradle
        uses: ./.github/workflows/reusable/gradle-setup.yml
        with:
          working-directory: ./backend/autotrader-backend
      
      - name: Build and Start Spring Boot
        uses: ./.github/workflows/reusable/spring-boot-setup.yml
        with:
          working-directory: ./backend/autotrader-backend
      
      - name: Run Postman Tests
        uses: ./.github/workflows/reusable/postman-tests.yml
        with:
          environment-file: './postman/test_environment.json'
          results-directory: 'results'
```

**Inputs:**
- `collection-path` (optional): Path to the Postman collection file (if empty, will auto-detect)
- `environment-file` (optional, default: './postman/test_environment.json'): Path to the Postman environment file
- `results-directory` (optional, default: 'results'): Directory to store test results
- `reporters` (optional, default: 'cli,junit,htmlextra'): Newman reporters to use (comma-separated)
- `working-directory` (optional, default: '.'): Working directory for running the tests
- `auto-detect-collection` (optional, default: true): Whether to auto-detect the collection file if not specified

## Implementation Note

To use these reusable workflows in your existing workflows, you need to replace the inline steps with calls to these reusable workflows. The existing workflows (`ci-cd.yml`, `integration-tests.yml`, `postman-tests.yml`) should be updated to use these reusable components.
