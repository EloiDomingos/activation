package com.deroyque.activation.service;

import com.deroyque.activation.client.ProviderClient;
import com.deroyque.activation.dto.ActivationRequestDTO;
import com.deroyque.activation.dto.ActivationResponseDTO;
import com.deroyque.activation.entity.Activation;
import com.deroyque.activation.entity.ActivationStatus;
import com.deroyque.activation.exception.DuplicateRequestException;
import com.deroyque.activation.repository.ActivationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActivationServiceTest {

    @Mock
    private ActivationRepository repository;

    @Mock
    private ProviderClient providerClient;

    @InjectMocks
    private ActivationService service;

    private ActivationRequestDTO validDto;
    private Activation request;

    @BeforeEach
    void setUp() {
        validDto = ActivationRequestDTO.builder()
                .externalId("EXT-1")
                .customerId("CUST-1")
                .productCode("PROD-1")
                .build();

        request = Activation.builder()
                .id(UUID.randomUUID())
                .externalId("EXT-1")
                .customerId("CUST-1")
                .productCode("PROD-1")
                .status(ActivationStatus.PENDING)
                .build();
    }

    @Test
    void createRequest_NewRequest_Success() {
        when(repository.findByExternalId(validDto.getExternalId())).thenReturn(Optional.empty());
        when(repository.save(any(Activation.class))).thenReturn(request);

        ActivationResponseDTO response = service.createRequest(validDto);

        assertNotNull(response);
        assertEquals(ActivationStatus.COMPLETED, request.getStatus());
        verify(providerClient).activate(any(), any(), any());
        verify(repository, atLeast(2)).save(any());
    }

    @Test
    void createRequest_DuplicateSameContent_ReturnExisting() {
        when(repository.findByExternalId(validDto.getExternalId())).thenReturn(Optional.of(request));

        ActivationResponseDTO response = service.createRequest(validDto);

        assertNotNull(response);
        verify(providerClient, never()).activate(any(), any(), any());
    }

    @Test
    void createRequest_DuplicateDifferentContent_ThrowException() {
        ActivationRequestDTO differentDto = ActivationRequestDTO.builder()
                .externalId("EXT-1")
                .customerId("CUST-2")
                .productCode("PROD-2")
                .build();

        when(repository.findByExternalId(differentDto.getExternalId())).thenReturn(Optional.of(request));

        assertThrows(DuplicateRequestException.class, () -> service.createRequest(differentDto));
    }

    @Test
    void createRequest_IntegrationFailure_UpdateStatusToFailed() {
        when(repository.findByExternalId(validDto.getExternalId())).thenReturn(Optional.empty());
        when(repository.save(any(Activation.class))).thenReturn(request);
        doThrow(new RuntimeException("API Error")).when(providerClient).activate(any(), any(), any());

        assertThrows(RuntimeException.class, () -> service.createRequest(validDto));
        assertEquals(ActivationStatus.FAILED, request.getStatus());
        verify(repository, atLeast(2)).save(any());
    }
}
