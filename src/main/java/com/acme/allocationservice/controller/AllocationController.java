package com.acme.allocationservice.controller;

import com.acme.allocationservice.dto.AllocationStatusResponse;
import com.acme.allocationservice.dto.CreateAllocationRequest;
import com.acme.allocationservice.dto.CreateAllocationResponse;
import com.acme.allocationservice.dto.EquipmentDto;
import com.acme.allocationservice.model.AllocationRequest;
import com.acme.allocationservice.service.AllocationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/allocations")
public class AllocationController {

    @Autowired
    private AllocationService allocationService;

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public CreateAllocationResponse create(@RequestBody @Valid CreateAllocationRequest request) {
        AllocationRequest allocation = allocationService.createAllocationRequest(request.getEmployeeId(), request.getPolicy());
        allocationService.processAllocationRequestAsync(allocation.getId());
        return new CreateAllocationResponse(allocation.getId());
    }

    @GetMapping("/{id}")
    public AllocationStatusResponse get(@PathVariable UUID id) {
        var allocation = allocationService.getById(id);
        List<EquipmentDto> equipments = allocation.getEquipments().stream()
            .map(e -> EquipmentDto.builder()
                .id(e.getId())
                .type(e.getType())
                .brand(e.getBrand())
                .model(e.getModel())
                .conditionScore(e.getConditionScore())
                .purchaseDate(e.getPurchaseDate())
                .state(e.getState())
                .createdAt(e.getCreatedAt())
                .build())
            .toList();
        return AllocationStatusResponse.builder()
            .allocationId(allocation.getId())
            .state(allocation.getState())
            .failureReason(allocation.getFailureReason())
            .equipments(equipments)
            .build();
    }

    @PostMapping("/{id}/confirm")
    public void confirm(@PathVariable UUID id) {
        allocationService.confirmAllocation(id);
    }

    @PostMapping("/{id}/cancel")
    public void cancel(@PathVariable UUID id) {
        allocationService.cancelAllocation(id);
    }
}
