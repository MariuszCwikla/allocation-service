package com.acme.allocationservice.dto;

import com.acme.allocationservice.model.EquipmentType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateEquipmentRequest {

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
    @PastOrPresent
    private LocalDate purchaseDate;
}