package com.deroyque.activation.repository;

import com.deroyque.activation.entity.Activation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ActivationRepository extends JpaRepository<Activation, UUID> {
    Optional<Activation> findByExternalId(String externalId);
}
