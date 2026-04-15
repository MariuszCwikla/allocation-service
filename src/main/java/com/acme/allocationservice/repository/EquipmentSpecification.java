package com.acme.allocationservice.repository;

import com.acme.allocationservice.model.Equipment;
import com.acme.allocationservice.model.EquipmentState;
import org.springframework.data.jpa.domain.Specification;

public class EquipmentSpecification {
    public static Specification<Equipment> state(EquipmentState state) {
        return ((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("state"), state));
    }
}
