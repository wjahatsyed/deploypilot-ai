$ErrorActionPreference = "Stop"

$BackendUrl = if ($env:BACKEND_URL) { $env:BACKEND_URL } else { "http://localhost:8080" }
$AiServiceUrl = if ($env:AI_SERVICE_URL) { $env:AI_SERVICE_URL } else { "http://localhost:8000" }
$RunIdSuffix = Get-Date -Format "yyyyMMddHHmmss"
$PassCount = 0

function Write-Step {
    param(
        [string] $Number,
        [string] $Message
    )

    Write-Host ""
    Write-Host "[$Number] $Message"
}

function Pass {
    param([string] $Message)

    $script:PassCount++
    Write-Host "  PASS: $Message"
}

function Fail {
    param([string] $Message)

    Write-Host "  FAIL: $Message" -ForegroundColor Red
    exit 1
}

function Get-Json {
    param([string] $Url)

    Invoke-RestMethod -Method Get -Uri $Url
}

function Post-Json {
    param(
        [string] $Url,
        [hashtable] $Body
    )

    Invoke-RestMethod -Method Post -Uri $Url -ContentType "application/json" -Body ($Body | ConvertTo-Json -Depth 10)
}

try {
    Write-Step "1/8" "Backend health: GET $BackendUrl/api/health"
    $backendHealth = Get-Json "$BackendUrl/api/health"
    if ($backendHealth.status -ne "UP") {
        Fail "Backend status was '$($backendHealth.status)'"
    }
    Pass "Backend is UP"

    Write-Step "2/8" "AI service health: GET $AiServiceUrl/health"
    $aiHealth = Get-Json "$AiServiceUrl/health"
    if ($aiHealth.status -ne "UP") {
        Fail "AI service status was '$($aiHealth.status)'"
    }
    Pass "AI service is UP"

    Write-Step "3/8" "Create customer: POST $BackendUrl/api/customers"
    $customer = Post-Json "$BackendUrl/api/customers" @{
        name = "Smoke Test Logistics"
        industry = "Logistics"
        region = "EMEA"
        contactEmail = "smoke-$RunIdSuffix@deploypilot.example"
    }
    if (-not $customer.id) {
        Fail "Customer response did not include id"
    }
    Pass "Created customer $($customer.id)"

    Write-Step "4/8" "Create deployment config: POST $BackendUrl/api/customers/$($customer.id)/deployment-configs"
    $deploymentConfig = Post-Json "$BackendUrl/api/customers/$($customer.id)/deployment-configs" @{
        environment = "PROD"
        modelName = "deploypilot-smoke-model"
        llmEnabled = $false
        approvalRequired = $true
        confidenceThreshold = 0.85
        maxMonthlyRuns = 100
    }
    if (-not $deploymentConfig.id) {
        Fail "Deployment config response did not include id"
    }
    Pass "Created PROD deployment config $($deploymentConfig.id)"

    Write-Step "5/8" "Create workflow: POST $BackendUrl/api/workflows"
    $workflow = Post-Json "$BackendUrl/api/workflows" @{
        customerId = $customer.id
        name = "Smoke SLA Refund Assistant"
        description = "End-to-end smoke workflow for DeployPilot AI."
    }
    if (-not $workflow.id) {
        Fail "Workflow response did not include id"
    }
    Pass "Created workflow $($workflow.id)"

    Write-Step "6/8" "Run workflow: POST $BackendUrl/api/workflows/$($workflow.id)/runs"
    $workflowRun = Post-Json "$BackendUrl/api/workflows/$($workflow.id)/runs" @{
        inputSource = "e2e-smoke-test"
        inputContent = "Berlin shipment delay refund request: customer reports shipment BER-4482 arrived 72 hours late and asks for a refund under the premium SLA."
    }
    if (-not $workflowRun.id) {
        Fail "Workflow run response did not include id"
    }
    if (-not $workflowRun.status -and -not $workflowRun.detectedIntent) {
        Fail "Workflow run response did not include status or detectedIntent"
    }
    Pass "Workflow run $($workflowRun.id) returned status=$($workflowRun.status), detectedIntent=$($workflowRun.detectedIntent)"

    if ($workflowRun.status -eq "WAITING_FOR_APPROVAL") {
        Write-Step "7/8" "Approve workflow run: POST $BackendUrl/api/runs/$($workflowRun.id)/approve"
        $approval = Post-Json "$BackendUrl/api/runs/$($workflowRun.id)/approve" @{
            email = "smoke.approver@deploypilot.example"
            comment = "Approved by smoke test."
        }
        if ($approval.status -ne "APPROVED") {
            Fail "Approval status was '$($approval.status)'"
        }
        Pass "Approved workflow run $($workflowRun.id)"
    } else {
        Write-Step "7/8" "Approval not required"
        Pass "Workflow run status is $($workflowRun.status)"
    }

    Write-Step "8/8" "Eval summary: GET $BackendUrl/api/evals/summary"
    $evalSummary = Get-Json "$BackendUrl/api/evals/summary"
    Write-Host "  Summary: $($evalSummary | ConvertTo-Json -Compress)"
    Pass "Eval summary returned"

    Write-Host ""
    Write-Host "PASS: DeployPilot AI smoke test completed ($PassCount checks)." -ForegroundColor Green
} catch {
    Fail $_.Exception.Message
}
