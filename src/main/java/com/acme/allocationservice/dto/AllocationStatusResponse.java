package com.acme.allocationservice.dto;

import com.acme.allocationservice.model.AllocationRequestState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AllocationStatusResponse {
    private UUID allocationId;
    private AllocationRequestState state;
    private String failureReason;
    private List<EquipmentDto> equipments;
}
