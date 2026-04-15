package com.acme.allocationservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class CreateAllocationRequest {

    @NotNull
    private UUID employeeId;

    @Size(min=1, max=10)
    @Valid
    private List<PolicyItemDto> policy;
}