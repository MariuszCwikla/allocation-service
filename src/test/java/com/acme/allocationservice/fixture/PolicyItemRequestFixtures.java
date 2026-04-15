package com.acme.allocationservice.fixture;

import com.acme.allocationservice.dto.PolicyItemDto;
import com.acme.allocationservice.model.EquipmentType;

public class PolicyItemRequestFixtures {

    public static PolicyItemDto policyItem(EquipmentType type, Double minScore, String brand) {
        PolicyItemDto req = new PolicyItemDto();
        req.setEquipmentType(type);
        req.setMinConditionScore(minScore);
        req.setPreferredBrand(brand);
        return req;
    }

    public static PolicyItemDto policyItem(EquipmentType type) {
        return policyItem(type, null, null);
    }
}