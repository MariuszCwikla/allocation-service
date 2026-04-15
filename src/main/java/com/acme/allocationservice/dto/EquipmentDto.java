package com.acme.allocationservice.dto;

import com.acme.allocationservice.model.EquipmentState;
import com.acme.allocationservice.model.EquipmentType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentDto {
    @NotNull
    private UUID id;

    @NotNull
    private EquipmentType type;

    @NotBlank
    private String brand;

    @NotBlank
    private String model;

    @NotNull
    @DecimalMin("0.0")
    @DecimalMax("1.0")
    private double conditionScore;

    @NotNull
    private LocalDate purchaseDate;

    @NotNull
    private EquipmentState state;

    @NotNull
    private Instant createdAt;
}
