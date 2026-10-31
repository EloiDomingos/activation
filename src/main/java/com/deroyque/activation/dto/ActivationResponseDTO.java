package com.deroyque.activation.dto;

import com.deroyque.activation.entity.ActivationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivationResponseDTO {
    private UUID id;
    private String externalId;
    private String customerId;
    private String productCode;
    private ActivationStatus status;
    private Instant createdAt;
    private Instant updatedAt;
}
