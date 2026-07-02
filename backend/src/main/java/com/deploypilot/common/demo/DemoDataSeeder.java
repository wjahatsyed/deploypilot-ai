package com.deploypilot.common.demo;

import com.deploypilot.customer.Customer;
import com.deploypilot.customer.CustomerRepository;
import com.deploypilot.deployment.DeploymentConfig;
import com.deploypilot.deployment.DeploymentConfigRepository;
import com.deploypilot.deployment.DeploymentConfigStatus;
import com.deploypilot.deployment.DeploymentEnvironment;
import com.deploypilot.evaluation.EvaluationCase;
import com.deploypilot.evaluation.EvaluationCaseRepository;
import com.deploypilot.evaluation.EvaluationDataset;
import com.deploypilot.evaluation.EvaluationDatasetRepository;
import com.deploypilot.workflow.Workflow;
import com.deploypilot.workflow.WorkflowRepository;
import com.deploypilot.workflow.WorkflowStatus;
import java.util.List;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("demo")
public class DemoDataSeeder implements ApplicationRunner {

    public static final String DEMO_CUSTOMER_EMAIL = "ops@eurologix.example";
    public static final String DEMO_CUSTOMER_NAME = "EuroLogix Operations";
    public static final String DEMO_WORKFLOW_NAME = "SLA Breach Refund Assistant";
    public static final String DEMO_DATASET_NAME = "EuroLogix SLA Demo Eval Set";

    private final CustomerRepository customerRepository;
    private final DeploymentConfigRepository deploymentConfigRepository;
    private final WorkflowRepository workflowRepository;
    private final EvaluationDatasetRepository evaluationDatasetRepository;
    private final EvaluationCaseRepository evaluationCaseRepository;

    public DemoDataSeeder(
            CustomerRepository customerRepository,
            DeploymentConfigRepository deploymentConfigRepository,
            WorkflowRepository workflowRepository,
            EvaluationDatasetRepository evaluationDatasetRepository,
            EvaluationCaseRepository evaluationCaseRepository
    ) {
        this.customerRepository = customerRepository;
        this.deploymentConfigRepository = deploymentConfigRepository;
        this.workflowRepository = workflowRepository;
        this.evaluationDatasetRepository = evaluationDatasetRepository;
        this.evaluationCaseRepository = evaluationCaseRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Customer customer = seedCustomer();
        seedProdDeploymentConfig(customer);
        seedWorkflow(customer);
        seedEvaluationCases(customer);
    }

    private Customer seedCustomer() {
        return customerRepository.findByContactEmail(DEMO_CUSTOMER_EMAIL)
                .orElseGet(() -> {
                    Customer customer = new Customer();
                    customer.setName(DEMO_CUSTOMER_NAME);
                    customer.setIndustry("Logistics");
                    customer.setRegion("EMEA");
                    customer.setContactEmail(DEMO_CUSTOMER_EMAIL);
                    return customerRepository.save(customer);
                });
    }

    private void seedProdDeploymentConfig(Customer customer) {
        deploymentConfigRepository.findByCustomerIdAndEnvironment(customer.getId(), DeploymentEnvironment.PROD)
                .orElseGet(() -> {
                    DeploymentConfig config = new DeploymentConfig();
                    config.setCustomerId(customer.getId());
                    config.setEnvironment(DeploymentEnvironment.PROD);
                    config.setModelName("deploypilot-demo-model");
                    config.setLlmEnabled(false);
                    config.setApprovalRequired(true);
                    config.setConfidenceThreshold(0.85);
                    config.setMaxMonthlyRuns(1_000);
                    config.setStatus(DeploymentConfigStatus.ACTIVE);
                    return deploymentConfigRepository.save(config);
                });
    }

    private void seedWorkflow(Customer customer) {
        workflowRepository.findByCustomerIdAndName(customer.getId(), DEMO_WORKFLOW_NAME)
                .orElseGet(() -> {
                    Workflow workflow = new Workflow();
                    workflow.setCustomer(customer);
                    workflow.setName(DEMO_WORKFLOW_NAME);
                    workflow.setDescription("Assists operations teams with SLA breach refund triage and approval routing.");
                    workflow.setStatus(WorkflowStatus.ACTIVE);
                    return workflowRepository.save(workflow);
                });
    }

    private void seedEvaluationCases(Customer customer) {
        EvaluationDataset dataset = evaluationDatasetRepository.findByCustomerIdAndName(customer.getId(), DEMO_DATASET_NAME)
                .orElseGet(() -> {
                    EvaluationDataset newDataset = new EvaluationDataset();
                    newDataset.setCustomerId(customer.getId());
                    newDataset.setName(DEMO_DATASET_NAME);
                    newDataset.setDescription("Demo cases for EuroLogix SLA breach, compliance, and complaint workflows.");
                    return evaluationDatasetRepository.save(newDataset);
                });

        List<DemoEvalCase> demoCases = List.of(
                new DemoEvalCase(
                        "Berlin shipment delay refund request: customer reports shipment BER-4482 arrived 72 hours late and asks for a refund under the premium SLA.",
                        "sla_breach_refund",
                        "{\"city\":\"Berlin\",\"shipmentId\":\"BER-4482\",\"delayHours\":72,\"requestedOutcome\":\"refund\"}",
                        "refund,SLA,delay,approval"
                ),
                new DemoEvalCase(
                        "Dublin compliance escalation: customs paperwork for shipment DUB-1180 is missing required EU export declarations and needs compliance review.",
                        "compliance_escalation",
                        "{\"city\":\"Dublin\",\"shipmentId\":\"DUB-1180\",\"issue\":\"missing_export_declarations\"}",
                        "compliance,escalate,review"
                ),
                new DemoEvalCase(
                        "Amsterdam customer complaint: recipient reports damaged packaging and repeated missed delivery windows for shipment AMS-7741.",
                        "customer_complaint",
                        "{\"city\":\"Amsterdam\",\"shipmentId\":\"AMS-7741\",\"issue\":\"damaged_packaging_and_missed_windows\"}",
                        "complaint,investigate,customer-care"
                )
        );

        List<String> existingInputs = evaluationCaseRepository.findByDatasetId(dataset.getId()).stream()
                .map(EvaluationCase::getInputContent)
                .toList();

        for (DemoEvalCase demoCase : demoCases) {
            if (!existingInputs.contains(demoCase.inputContent())) {
                EvaluationCase evaluationCase = new EvaluationCase();
                evaluationCase.setDatasetId(dataset.getId());
                evaluationCase.setInputContent(demoCase.inputContent());
                evaluationCase.setExpectedIntent(demoCase.expectedIntent());
                evaluationCase.setExpectedFieldsJson(demoCase.expectedFieldsJson());
                evaluationCase.setExpectedActionKeywords(demoCase.expectedActionKeywords());
                evaluationCaseRepository.save(evaluationCase);
            }
        }
    }

    private record DemoEvalCase(
            String inputContent,
            String expectedIntent,
            String expectedFieldsJson,
            String expectedActionKeywords
    ) {
    }
}
