package com.deploypilot.customer;

import com.deploypilot.common.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor
public class Customer extends AuditableEntity {

    @Column(nullable = false)
    private String name;

    private String industry;

    private String region;

    @Column(unique = true)
    private String contactEmail;
}
