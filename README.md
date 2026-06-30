# DeployPilot AI

## Prerequisites
- JDK 21
- Docker and Docker Compose
- Maven, or IntelliJ IDEA's bundled Maven

## Getting Started
### Database
Start PostgreSQL from the repository root:
```bash
docker compose -f infra/docker-compose.yml up -d postgres
```

### Build and Run
1. Build the entire project from the root:
   ```bash
   mvn clean install
   ```

2. Run the backend application:
   ```bash
   mvn spring-boot:run -pl backend
   ```
