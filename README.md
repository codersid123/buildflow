# BuildFlow

A distributed build orchestration platform — a mini CI/CD system like Jenkins or GitHub Actions.
Users submit build jobs through a REST API; jobs are queued in RabbitMQ and executed by
independent worker services that report status and output back to PostgreSQL.

## Architecture

```
Client ──JWT──▶ api-service ──▶ PostgreSQL
                    │
                    ▼ (job ID)
                RabbitMQ (build.exchange → job.queue)
                    │
                    ▼
              worker-service ──▶ runs command ──▶ updates PostgreSQL
```

## Tech stack

| Layer                | Technology                     |
| -------------------- | ------------------------------ |
| Language / framework | Java 21, Spring Boot 4         |
| Auth                 | Spring Security, JWT, BCrypt   |
| Database             | PostgreSQL 16, Spring Data JPA |
| Messaging            | RabbitMQ                       |
| Build                | Maven (multi-module)           |
| Testing              | JUnit 5                        |

## What works so far (MVP)

- Register / login, JWT-protected endpoints, role-based admin endpoint
- Users can create and view only their own jobs
- Job is published to RabbitMQ only after the database commit succeeds
- Worker claims jobs atomically (PENDING → RUNNING), so a job never runs twice
- Worker executes the command, captures output and exit code, marks SUCCESS / FAILED
- Configurable job timeout; output capped to the last 64 KB
- Jobs wait safely in the queue if no worker is running

## How to run

**Prerequisites:** JDK 21, Docker Desktop

```powershell
# 1. Start Postgres and RabbitMQ
docker run --name buildflow-pg -e POSTGRES_PASSWORD=pass -e POSTGRES_DB=buildflow -p 5433:5432 -d postgres:16
docker run -d --name buildflow-rabbit -p 5672:5672 -p 15672:15672 rabbitmq:3-management

# 2. Start the API (terminal 1)
.\mvnw.cmd -pl api-service spring-boot:run

# 3. Start a worker (terminal 2)
.\mvnw.cmd -pl worker-service spring-boot:run
```

## API

| Method | Endpoint         | Auth | Description                   |
| ------ | ---------------- | ---- | ----------------------------- |
| POST   | `/auth/register` | —    | Create account                |
| POST   | `/auth/login`    | —    | Returns JWT                   |
| POST   | `/api/jobs`      | JWT  | Submit a job                  |
| GET    | `/api/jobs`      | JWT  | List your jobs                |
| GET    | `/api/jobs/{id}` | JWT  | Job status, output, exit code |

**Example job:**

```json
{
  "repoUrl": "https://github.com/x/y",
  "branch": "main",
  "command": "echo hello"
}
```

## Roadmap

- [ ] Redis caching for job status
- [ ] Retries with backoff + dead-letter queue
- [ ] Worker heartbeats and crash recovery
- [ ] Docker Compose with multiple workers
- [ ] Swagger docs and Actuator metrics
