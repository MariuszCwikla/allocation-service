package com.acme.allocationservice.dto;

import com.acme.allocationservice.model.EquipmentType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PolicyItemDto {

    @NotNull
    private EquipmentType equipmentType;

    @DecimalMin("0.0")
    @DecimalMax("1.0")
    private Double minConditionScore;

    private String preferredBrand;
}