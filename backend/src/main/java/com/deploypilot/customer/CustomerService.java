package com.deploypilot.customer;

import com.deploypilot.common.exception.ResourceNotFoundException;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public List<CustomerResponse> findAll() {
        return customerRepository.findAll().stream()
                .map(CustomerService::toResponse)
                .toList();
    }

    public CustomerResponse findById(UUID id) {
        return customerRepository.findById(id)
                .map(CustomerService::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + id));
    }

    @Transactional
    public CustomerResponse create(CreateCustomerRequest request) {
        Customer customer = new Customer();
        customer.setName(request.name());
        customer.setIndustry(request.industry());
        customer.setRegion(request.region());
        customer.setContactEmail(request.contactEmail());

        return toResponse(customerRepository.save(customer));
    }

    public Customer getEntityById(UUID id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + id));
    }

    static CustomerResponse toResponse(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getName(),
                customer.getIndustry(),
                customer.getRegion(),
                customer.getContactEmail(),
                customer.getCreatedAt(),
                customer.getUpdatedAt()
        );
    }
}
