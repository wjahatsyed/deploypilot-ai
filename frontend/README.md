# DeployPilot AI Frontend

Minimal demo dashboard for DeployPilot AI.

## Stack

- Next.js
- TypeScript
- Plain CSS

## Setup

```bash
npm install
```

Create a local environment file:

```bash
copy .env.example .env.local
```

Set the backend URL:

```text
NEXT_PUBLIC_BACKEND_URL=http://localhost:8080
```

## Run

```bash
npm run dev
```

Open:

```text
http://localhost:3000
```

## Build

```bash
npm run build
npm run start
```

## Notes

- The dashboard uses real backend endpoints for customers, workflows, workflow runs, approvals, and eval summary.
- If an endpoint is unavailable, the UI shows an explicit error or unavailable state.
- Workflow run AI fields may be empty until backend AI execution is fully wired; the UI displays that clearly instead of silently inventing values.
