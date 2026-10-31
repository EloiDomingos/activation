package com.deroyque.activation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivationRequestDTO {

    @NotBlank(message = "externalId é obrigatório")
    @Size(max = 100, message = "externalId não deve exceder 100 caracteres")
    private String externalId;

    @NotBlank(message = "customerId é obrigatório")
    @Size(max = 100, message = "customerId não deve exceder 100 caracteres")
    private String customerId;

    @NotBlank(message = "productCode é obrigatório")
    @Size(max = 100, message = "productCode não deve exceder 100 caracteres")
    private String productCode;
}
