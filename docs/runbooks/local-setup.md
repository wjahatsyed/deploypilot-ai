# Local Setup Guide

## Requirements
- **Java**: JDK 21
- **Database**: PostgreSQL (can be run via Docker Compose in `infra/`)
- **Build**: Maven or IntelliJ IDEA's bundled Maven

## Backend Setup
1. Ensure you have JDK 21 installed and `JAVA_HOME` set.
2. Start PostgreSQL from the project root:
   ```bash
   docker compose -f infra/docker-compose.yml up -d postgres
   ```
3. Run the backend tests:
   ```bash
   mvn test
   ```
4. Run the backend API:
   ```bash
   mvn spring-boot:run -pl backend
   ```

## Infrastructure
Run the following to start the database:
```bash
docker compose -f infra/docker-compose.yml up -d postgres
```
