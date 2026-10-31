package com.deroyque.activation.controller;

import com.deroyque.activation.dto.ActivationRequestDTO;
import com.deroyque.activation.dto.ActivationResponseDTO;
import com.deroyque.activation.entity.ActivationStatus;
import com.deroyque.activation.exception.DuplicateRequestException;
import com.deroyque.activation.exception.ResourceNotFoundException;
import com.deroyque.activation.service.ActivationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ActivationController.class)
class ActivationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ActivationService service;

    @Autowired
    private ObjectMapper objectMapper;

    private ActivationRequestDTO validDto;
    private ActivationResponseDTO responseDto;
    private UUID id;

    @BeforeEach
    void setUp() {
        id = UUID.randomUUID();
        validDto = ActivationRequestDTO.builder()
                .externalId("EXT-123")
                .customerId("CUST-123")
                .productCode("PROD-123")
                .build();

        responseDto = ActivationResponseDTO.builder()
                .id(id)
                .externalId("EXT-123")
                .customerId("CUST-123")
                .productCode("PROD-123")
                .status(ActivationStatus.COMPLETED)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Test
    void create_ValidData_ReturnsCreated() throws Exception {
        when(service.createRequest(any(ActivationRequestDTO.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/v1/requests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void create_InvalidData_ReturnsBadRequest() throws Exception {
        ActivationRequestDTO invalidDto = new ActivationRequestDTO("", "", "");

        mockMvc.perform(post("/api/v1/requests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_DuplicateDifferentContent_ReturnsConflict() throws Exception {
        when(service.createRequest(any(ActivationRequestDTO.class)))
                .thenThrow(new DuplicateRequestException("ExternalId já associado a um conteúdo diferente"));

        mockMvc.perform(post("/api/v1/requests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isConflict());
    }

    @Test
    void getById_ExistingId_ReturnsOk() throws Exception {
        when(service.getRequest(id)).thenReturn(responseDto);

        mockMvc.perform(get("/api/v1/requests/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    void getById_NonExistingId_ReturnsNotFound() throws Exception {
        when(service.getRequest(id)).thenThrow(new ResourceNotFoundException("Not found"));

        mockMvc.perform(get("/api/v1/requests/{id}", id))
                .andExpect(status().isNotFound());
    }
}
