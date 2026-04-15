package com.acme.allocationservice.repository;

import com.acme.allocationservice.model.AllocationRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AllocationRequestRepository extends JpaRepository<AllocationRequest, UUID> {
}