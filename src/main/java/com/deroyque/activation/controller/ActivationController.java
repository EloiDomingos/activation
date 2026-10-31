package com.deroyque.activation.controller;

import com.deroyque.activation.dto.ActivationRequestDTO;
import com.deroyque.activation.dto.ActivationResponseDTO;
import com.deroyque.activation.service.ActivationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/requests")
public class ActivationController {

    private final ActivationService service;

    public ActivationController(ActivationService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ActivationResponseDTO> create(@Valid @RequestBody ActivationRequestDTO dto) {
        ActivationResponseDTO response = service.createRequest(dto);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getId())
                .toUri();
        return ResponseEntity.created(uri).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ActivationResponseDTO> getById(@PathVariable UUID id) {
        ActivationResponseDTO response = service.getRequest(id);
        return ResponseEntity.ok(response);
    }
}
