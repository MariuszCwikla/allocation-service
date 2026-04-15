package com.acme.allocationservice.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class CreateEquipmentResponse {
    private UUID id;
}