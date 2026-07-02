package com.deploypilot;

import static org.assertj.core.api.Assertions.assertThat;

import com.deploypilot.common.demo.DemoDataSeeder;
import com.deploypilot.customer.CustomerRepository;
import com.deploypilot.deployment.DeploymentConfigRepository;
import com.deploypilot.deployment.DeploymentConfigStatus;
import com.deploypilot.deployment.DeploymentEnvironment;
import com.deploypilot.evaluation.EvaluationCaseRepository;
import com.deploypilot.evaluation.EvaluationDatasetRepository;
import com.deploypilot.workflow.WorkflowRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("demo")
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:deploypilot-demo-test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class DemoProfileApplicationTests {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private DeploymentConfigRepository deploymentConfigRepository;

    @Autowired
    private WorkflowRepository workflowRepository;

    @Autowired
    private EvaluationDatasetRepository evaluationDatasetRepository;

    @Autowired
    private EvaluationCaseRepository evaluationCaseRepository;

    @Test
    void demoProfileSeedsExpectedData() {
        var customer = customerRepository.findByContactEmail(DemoDataSeeder.DEMO_CUSTOMER_EMAIL);

        assertThat(customer).isPresent();
        assertThat(customer.get().getName()).isEqualTo(DemoDataSeeder.DEMO_CUSTOMER_NAME);

        assertThat(deploymentConfigRepository.findByCustomerIdAndEnvironmentAndStatus(
                customer.get().getId(), DeploymentEnvironment.PROD, DeploymentConfigStatus.ACTIVE
        )).isPresent();

        assertThat(workflowRepository.findByCustomerIdAndName(
                customer.get().getId(), DemoDataSeeder.DEMO_WORKFLOW_NAME
        )).isPresent();

        var dataset = evaluationDatasetRepository.findByCustomerIdAndName(
                customer.get().getId(), DemoDataSeeder.DEMO_DATASET_NAME
        );

        assertThat(dataset).isPresent();
        assertThat(evaluationCaseRepository.findByDatasetId(dataset.get().getId())).hasSize(3);
    }
}
