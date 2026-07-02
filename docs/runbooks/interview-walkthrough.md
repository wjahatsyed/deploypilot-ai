# Interview Walkthrough: DeployPilot AI

This is a 7-minute walkthrough script for explaining DeployPilot AI in an interview.

## 0:00-0:45 - Problem

DeployPilot AI addresses a common enterprise operations problem: teams receive messy inbound requests, but they need consistent classification, field extraction, recommended actions, approval routing, and quality measurement.

The example customer is EuroLogix Operations, a logistics team handling shipment delays, compliance escalations, and customer complaints. The goal is not to replace the operator. The goal is to make the workflow faster, more consistent, and safer to deploy.

## 0:45-1:45 - Architecture

The system has three main services.

The backend is Java 21 and Spring Boot. It owns customers, workflows, workflow runs, deployment configs, approvals, audit-oriented entities, and eval data. It uses PostgreSQL through Spring Data JPA and exposes REST APIs.

The AI service is Python FastAPI. It provides classification, extraction, generation, and evaluation endpoints with Pydantic request and response models. It can run in mock mode without OpenAI credentials, which keeps local demos reliable.

The frontend is a Next.js TypeScript dashboard. It uses `NEXT_PUBLIC_BACKEND_URL` to connect to the backend and gives a simple operator interface for the core flow.

## 1:45-2:30 - Customer Onboarding

I start by creating a customer, such as EuroLogix Operations. The customer record captures name, industry, region, and contact email. That gives the platform a tenant-like anchor for workflows, deployment config, evals, and future usage reporting.

Then I create a deployment config for `PROD`. In v1, this controls whether the LLM path is enabled, whether approval is required, the confidence threshold, and run limits. That models how enterprise teams roll out AI gradually instead of turning everything on at once.

## 2:30-3:30 - Workflow Run

Next I create a workflow called `SLA Breach Refund Assistant`. I run it with a sample input: a Berlin shipment delay refund request.

The backend creates a workflow run, checks risk, calls the AI service, stores the detected intent, extracted fields, recommended action, and final run status. If policy requires review, the run moves to `WAITING_FOR_APPROVAL`.

This is the main product loop: customer input becomes structured operational decision support.

## 3:30-4:20 - AI Service

The AI service is intentionally isolated behind HTTP contracts. It has endpoints for `/classify`, `/extract`, `/generate`, and `/evaluate`. That separation keeps the backend focused on business state and lets the AI layer evolve independently.

Mock mode is important. It means demos, tests, and local development do not require an OpenAI key. In a production version, this is where I would add provider retries, prompt versioning, model selection, and stronger response validation.

## 4:20-5:05 - Evals

DeployPilot AI includes eval datasets, eval cases, eval runs, and eval summaries. The demo data includes Berlin, Dublin, and Amsterdam examples with expected intent, expected fields, and action keywords.

This matters because AI quality needs to be measured continuously. If I change a prompt, model, workflow, or extraction strategy, evals give me a way to detect regressions before customers feel them.

## 5:05-5:45 - Approval

Approval is the safety layer. When a workflow run requires human review, the backend creates a pending approval. An operator can approve or reject it.

This reflects how many enterprise AI deployments actually work: AI assists and accelerates, but high-impact actions still need accountable human control, especially early in rollout.

## 5:45-6:25 - Production Deployment

The project includes Dockerfiles, Docker Compose, health endpoints, GitHub Actions CI, smoke-test scripts, and runbooks. After `docker compose up --build`, the smoke test checks backend health, AI service health, customer creation, deployment config, workflow creation, workflow run, optional approval, and eval summary.

That makes the project demo-ready and gives reviewers a repeatable way to verify the system.

## 6:25-7:00 - What I Would Improve Next

The next improvements are authentication, tenant isolation, database migrations, production OpenAI integration, model and prompt versioning, richer observability, and eval-gated deployment controls.

I would also expand the frontend into a fuller operations console with run timelines, approval latency, eval trend charts, and customer-specific configuration.

The key point is that v1 is not trying to be everything. It is a credible production foundation: customer-aware, eval-aware, approval-aware, deployable, and documented.
