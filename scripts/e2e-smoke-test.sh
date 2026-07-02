#!/usr/bin/env bash
set -u

BACKEND_URL="${BACKEND_URL:-http://localhost:8080}"
AI_SERVICE_URL="${AI_SERVICE_URL:-http://localhost:8000}"
RUN_ID_SUFFIX="$(date +%Y%m%d%H%M%S)"

PASS_COUNT=0

log_step() {
  printf '\n[%s] %s\n' "$1" "$2"
}

pass() {
  PASS_COUNT=$((PASS_COUNT + 1))
  printf '  PASS: %s\n' "$1"
}

fail() {
  printf '  FAIL: %s\n' "$1" >&2
  exit 1
}

extract_json_string() {
  printf '%s' "$1" | sed -n "s/.*\"$2\"[[:space:]]*:[[:space:]]*\"\([^\"]*\)\".*/\1/p"
}

call_get() {
  local url="$1"
  curl -fsS "$url"
}

call_post() {
  local url="$1"
  local payload="$2"
  curl -fsS -X POST "$url" \
    -H "Content-Type: application/json" \
    -d "$payload"
}

log_step "1/8" "Backend health: GET $BACKEND_URL/api/health"
backend_health="$(call_get "$BACKEND_URL/api/health")" || fail "Backend health check failed"
backend_status="$(extract_json_string "$backend_health" "status")"
[ "$backend_status" = "UP" ] || fail "Backend status was '$backend_status'"
pass "Backend is UP"

log_step "2/8" "AI service health: GET $AI_SERVICE_URL/health"
ai_health="$(call_get "$AI_SERVICE_URL/health")" || fail "AI service health check failed"
ai_status="$(extract_json_string "$ai_health" "status")"
[ "$ai_status" = "UP" ] || fail "AI service status was '$ai_status'"
pass "AI service is UP"

log_step "3/8" "Create customer: POST $BACKEND_URL/api/customers"
customer_payload='{
  "name": "Smoke Test Logistics",
  "industry": "Logistics",
  "region": "EMEA",
  "contactEmail": "smoke-'"$RUN_ID_SUFFIX"'@deploypilot.example"
}'
customer_response="$(call_post "$BACKEND_URL/api/customers" "$customer_payload")" || fail "Customer creation failed"
customer_id="$(extract_json_string "$customer_response" "id")"
[ -n "$customer_id" ] || fail "Customer response did not include id"
pass "Created customer $customer_id"

log_step "4/8" "Create deployment config: POST $BACKEND_URL/api/customers/$customer_id/deployment-configs"
deployment_payload='{
  "environment": "PROD",
  "modelName": "deploypilot-smoke-model",
  "llmEnabled": false,
  "approvalRequired": true,
  "confidenceThreshold": 0.85,
  "maxMonthlyRuns": 100
}'
deployment_response="$(call_post "$BACKEND_URL/api/customers/$customer_id/deployment-configs" "$deployment_payload")" || fail "Deployment config creation failed"
deployment_id="$(extract_json_string "$deployment_response" "id")"
[ -n "$deployment_id" ] || fail "Deployment config response did not include id"
pass "Created PROD deployment config $deployment_id"

log_step "5/8" "Create workflow: POST $BACKEND_URL/api/workflows"
workflow_payload='{
  "customerId": "'"$customer_id"'",
  "name": "Smoke SLA Refund Assistant",
  "description": "End-to-end smoke workflow for DeployPilot AI."
}'
workflow_response="$(call_post "$BACKEND_URL/api/workflows" "$workflow_payload")" || fail "Workflow creation failed"
workflow_id="$(extract_json_string "$workflow_response" "id")"
[ -n "$workflow_id" ] || fail "Workflow response did not include id"
pass "Created workflow $workflow_id"

log_step "6/8" "Run workflow: POST $BACKEND_URL/api/workflows/$workflow_id/runs"
run_payload='{
  "inputSource": "e2e-smoke-test",
  "inputContent": "Berlin shipment delay refund request: customer reports shipment BER-4482 arrived 72 hours late and asks for a refund under the premium SLA."
}'
run_response="$(call_post "$BACKEND_URL/api/workflows/$workflow_id/runs" "$run_payload")" || fail "Workflow run failed"
run_id="$(extract_json_string "$run_response" "id")"
run_status="$(extract_json_string "$run_response" "status")"
detected_intent="$(extract_json_string "$run_response" "detectedIntent")"
[ -n "$run_id" ] || fail "Workflow run response did not include id"
[ -n "$run_status" ] || [ -n "$detected_intent" ] || fail "Workflow run response did not include status or detectedIntent"
pass "Workflow run $run_id returned status=${run_status:-n/a}, detectedIntent=${detected_intent:-n/a}"

if [ "$run_status" = "WAITING_FOR_APPROVAL" ]; then
  log_step "7/8" "Approve workflow run: POST $BACKEND_URL/api/runs/$run_id/approve"
  approval_payload='{
    "email": "smoke.approver@deploypilot.example",
    "comment": "Approved by smoke test."
  }'
  approval_response="$(call_post "$BACKEND_URL/api/runs/$run_id/approve" "$approval_payload")" || fail "Workflow approval failed"
  approval_status="$(extract_json_string "$approval_response" "status")"
  [ "$approval_status" = "APPROVED" ] || fail "Approval status was '$approval_status'"
  pass "Approved workflow run $run_id"
else
  log_step "7/8" "Approval not required"
  pass "Workflow run status is $run_status"
fi

log_step "8/8" "Eval summary: GET $BACKEND_URL/api/evals/summary"
eval_summary="$(call_get "$BACKEND_URL/api/evals/summary")" || fail "Eval summary request failed"
printf '  Summary: %s\n' "$eval_summary"
pass "Eval summary returned"

printf '\nPASS: DeployPilot AI smoke test completed (%s checks).\n' "$PASS_COUNT"
