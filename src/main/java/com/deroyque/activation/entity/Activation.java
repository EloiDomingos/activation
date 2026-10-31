package com.deroyque.activation.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "activations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Activation {

    @Id
    private UUID id;

    @Column(name = "external_id", unique = true, nullable = false)
    private String externalId;

    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @Column(name = "product_code", nullable = false)
    private String productCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActivationStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
