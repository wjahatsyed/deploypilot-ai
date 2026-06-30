package com.deploypilot.customer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.deploypilot.common.AuditableEntity;
import java.lang.reflect.Field;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    @Test
    void createMapsRequestToResponse() {
        UUID customerId = UUID.randomUUID();
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> {
            Customer customer = invocation.getArgument(0);
            setAuditFields(customer, customerId);
            return customer;
        });

        CustomerResponse response = customerService.create(new CreateCustomerRequest(
                "Acme",
                "SaaS",
                "NA",
                "ops@acme.test"
        ));

        assertThat(response.id()).isEqualTo(customerId);
        assertThat(response.name()).isEqualTo("Acme");
        assertThat(response.industry()).isEqualTo("SaaS");
        assertThat(response.region()).isEqualTo("NA");
        assertThat(response.contactEmail()).isEqualTo("ops@acme.test");
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    void findByIdReturnsCustomerResponse() {
        UUID customerId = UUID.randomUUID();
        Customer customer = new Customer();
        customer.setName("Acme");
        customer.setIndustry("SaaS");
        customer.setRegion("NA");
        customer.setContactEmail("ops@acme.test");
        setAuditFields(customer, customerId);
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

        CustomerResponse response = customerService.findById(customerId);

        assertThat(response.id()).isEqualTo(customerId);
        assertThat(response.name()).isEqualTo("Acme");
    }

    private static void setAuditFields(AuditableEntity entity, UUID id) {
        try {
            setField(entity, "id", id);
            setField(entity, "createdAt", Instant.now());
            setField(entity, "updatedAt", Instant.now());
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static void setField(AuditableEntity entity, String fieldName, Object value)
            throws ReflectiveOperationException {
        Field field = AuditableEntity.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(entity, value);
    }
}
