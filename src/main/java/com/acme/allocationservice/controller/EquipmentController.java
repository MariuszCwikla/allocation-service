package com.acme.allocationservice.controller;

import com.acme.allocationservice.dto.CreateEquipmentRequest;
import com.acme.allocationservice.dto.CreateEquipmentResponse;
import com.acme.allocationservice.dto.EquipmentDto;
import com.acme.allocationservice.model.Equipment;
import com.acme.allocationservice.model.EquipmentState;
import com.acme.allocationservice.repository.EquipmentRepository;
import com.acme.allocationservice.repository.EquipmentSpecification;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/equipments")
public class EquipmentController {

    @Autowired
    private EquipmentRepository equipmentRepository;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public CreateEquipmentResponse register(@RequestBody @Valid CreateEquipmentRequest request) {
        Equipment equipment = Equipment.builder()
                .type(request.getType())
                .brand(request.getBrand())
                .model(request.getModel())
                .conditionScore(request.getConditionScore())
                .purchaseDate(request.getPurchaseDate())
                .build();
        equipmentRepository.save(equipment);

        return CreateEquipmentResponse.builder()
                .id(equipment.getId())
                .build();
    }

    @GetMapping
    public Page<EquipmentDto> list(
            @RequestParam(required = false) EquipmentState state,
            @PageableDefault(size = 20) Pageable pageable) {

        Specification<Equipment> spec = Specification.unrestricted();
        if (state != null) {
            spec = EquipmentSpecification.state(state);
        }
        Page<Equipment> page = equipmentRepository.findAll(spec, pageable);
        return page.map(e -> EquipmentDto.builder()
                .id(e.getId())
                .type(e.getType())
                .brand(e.getBrand())
                .model(e.getModel())
                .state(e.getState())
                .conditionScore(e.getConditionScore())
                .purchaseDate(e.getPurchaseDate())
                .createdAt(e.getCreatedAt())
            .build());
    }
}