# DeployPilot AI Demo Data

This runbook prepares a predictable demo flow for local and recruiter demos. Demo data is created only when the Spring `demo` profile is active.

## Seeded Data

When the backend starts with `demo`, it creates the following records if they do not already exist:

- Customer: `EuroLogix Operations`
- Customer email: `ops@eurologix.example`
- Region: `EMEA`
- Industry: `Logistics`
- PROD deployment config:
  - `environment`: `PROD`
  - `modelName`: `deploypilot-demo-model`
  - `llmEnabled`: `false`
  - `approvalRequired`: `true`
  - `confidenceThreshold`: `0.85`
  - `maxMonthlyRuns`: `1000`
- Workflow: `SLA Breach Refund Assistant`
- Eval dataset: `EuroLogix SLA Demo Eval Set`
- 3 eval cases:
  - Berlin shipment delay refund request
  - Dublin compliance escalation
  - Amsterdam customer complaint

Because `llmEnabled` is `false`, workflow runs use the backend mock AI path and do not require the Python AI service or OpenAI.

## Run With Demo Profile

From the repository root:

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=demo
```

Or run a packaged jar:

```bash
java -jar target/deploypilot-backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=demo
```

Normal startup is unchanged when the `demo` profile is not active.

## Sample Workflow Inputs

```text
Berlin shipment delay refund request: customer reports shipment BER-4482 arrived 72 hours late and asks for a refund under the premium SLA.
```

```text
Dublin compliance escalation: customs paperwork for shipment DUB-1180 is missing required EU export declarations and needs compliance review.
```

```text
Amsterdam customer complaint: recipient reports damaged packaging and repeated missed delivery windows for shipment AMS-7741.
```

## Curl Demo Flow

Set the API base URL:

```bash
API_URL=http://localhost:8080
```

Create a customer manually:

```bash
curl -X POST "$API_URL/api/customers" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "EuroLogix Operations",
    "industry": "Logistics",
    "region": "EMEA",
    "contactEmail": "ops-manual@eurologix.example"
  }'
```

Create a PROD deployment config for a customer:

```bash
CUSTOMER_ID=<customer-id>

curl -X POST "$API_URL/api/customers/$CUSTOMER_ID/deployment-configs" \
  -H "Content-Type: application/json" \
  -d '{
    "environment": "PROD",
    "modelName": "deploypilot-demo-model",
    "llmEnabled": false,
    "approvalRequired": true,
    "confidenceThreshold": 0.85,
    "maxMonthlyRuns": 1000
  }'
```

Create a workflow:

```bash
curl -X POST "$API_URL/api/workflows" \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "'"$CUSTOMER_ID"'",
    "name": "SLA Breach Refund Assistant",
    "description": "Assists operations teams with SLA breach refund triage and approval routing."
  }'
```

Run a workflow:

```bash
WORKFLOW_ID=<workflow-id>

curl -X POST "$API_URL/api/workflows/$WORKFLOW_ID/runs" \
  -H "Content-Type: application/json" \
  -d '{
    "inputSource": "demo-runbook",
    "inputContent": "Berlin shipment delay refund request: customer reports shipment BER-4482 arrived 72 hours late and asks for a refund under the premium SLA."
  }'
```

Approve a workflow run:

```bash
RUN_ID=<run-id>

curl -X POST "$API_URL/api/runs/$RUN_ID/approve" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "ops.lead@eurologix.example",
    "comment": "Approved for demo refund processing."
  }'
```

Create an eval dataset:

```bash
curl -X POST "$API_URL/api/eval-datasets" \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "'"$CUSTOMER_ID"'",
    "name": "Manual Demo Eval Set",
    "description": "Manual eval cases for demo validation."
  }'
```

Create an eval case:

```bash
DATASET_ID=<dataset-id>

curl -X POST "$API_URL/api/eval-datasets/$DATASET_ID/cases" \
  -H "Content-Type: application/json" \
  -d '{
    "inputContent": "Dublin compliance escalation: customs paperwork for shipment DUB-1180 is missing required EU export declarations and needs compliance review.",
    "expectedIntent": "compliance_escalation",
    "expectedFieldsJson": "{\"city\":\"Dublin\",\"shipmentId\":\"DUB-1180\",\"issue\":\"missing_export_declarations\"}",
    "expectedActionKeywords": "compliance,escalate,review"
  }'
```

Run evals:

```bash
curl -X POST "$API_URL/api/eval-datasets/$DATASET_ID/run"
```

Get the current eval summary:

```bash
curl "$API_URL/api/evals/summary"
```
