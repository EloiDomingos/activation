package com.deroyque.activation.service;

import com.deroyque.activation.client.ProviderClient;
import com.deroyque.activation.dto.ActivationRequestDTO;
import com.deroyque.activation.dto.ActivationResponseDTO;
import com.deroyque.activation.entity.Activation;
import com.deroyque.activation.entity.ActivationStatus;
import com.deroyque.activation.exception.DuplicateRequestException;
import com.deroyque.activation.exception.ResourceNotFoundException;
import com.deroyque.activation.repository.ActivationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class ActivationService {

    private final ActivationRepository repository;
    private final ProviderClient providerClient;

    public ActivationService(ActivationRepository repository, ProviderClient providerClient) {
        this.repository = repository;
        this.providerClient = providerClient;
    }

    @Transactional
    public ActivationResponseDTO createRequest(ActivationRequestDTO dto) {
        Optional<Activation> existing = repository.findByExternalId(dto.getExternalId());

        if (existing.isPresent()) {
            Activation activation = existing.get();
            if (isSameContent(activation, dto)) {
                return mapToDTO(activation);
            } else {
                throw new DuplicateRequestException("ExternalId já associado a um conteúdo diferente");
            }
        }

        Activation request = Activation.builder()
                .id(UUID.randomUUID())
                .externalId(dto.getExternalId())
                .customerId(dto.getCustomerId())
                .productCode(dto.getProductCode())
                .status(ActivationStatus.PENDING)
                .createdAt(java.time.Instant.now())
                .updatedAt(java.time.Instant.now())
                .build();

        request = repository.save(request);
        processActivation(request);
        return mapToDTO(request);
    }

    private void processActivation(Activation request) {
        try {
            request.setStatus(ActivationStatus.PROCESSING);
            repository.save(request);

            providerClient.activate(request.getExternalId(), request.getCustomerId(), request.getProductCode());

            request.setStatus(ActivationStatus.COMPLETED);
        } catch (Exception e) {
            request.setStatus(ActivationStatus.FAILED);
            throw e;
        } finally {
            repository.save(request);
        }
    }

    @Transactional(readOnly = true)
    public ActivationResponseDTO getRequest(UUID id) {
        return repository.findById(id)
                .map(this::mapToDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitação de ativação não encontrada com o id: " + id));
    }

    private boolean isSameContent(Activation entity, ActivationRequestDTO dto) {
        return entity.getCustomerId().equals(dto.getCustomerId()) &&
               entity.getProductCode().equals(dto.getProductCode());
    }

    private ActivationResponseDTO mapToDTO(Activation entity) {
        return ActivationResponseDTO.builder()
                .id(entity.getId())
                .externalId(entity.getExternalId())
                .customerId(entity.getCustomerId())
                .productCode(entity.getProductCode())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
