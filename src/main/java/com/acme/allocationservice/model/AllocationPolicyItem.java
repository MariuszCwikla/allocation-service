package com.acme.allocationservice.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;

import java.util.UUID;

@Entity
@Table(name = "allocation_policy_items")
@ToString
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class AllocationPolicyItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "allocation_request_id", nullable = false)
    @ToString.Exclude
    private AllocationRequest allocationRequest;

    @Enumerated(EnumType.STRING)
    @Column(name = "equipment_type", nullable = false)
    private EquipmentType equipmentType;

    @Column(name = "min_condition_score")
    private Double minConditionScore;

    @Column(name = "preferred_brand", length = 100)
    private String preferredBrand;

    public MatchingResult getMatch(Equipment e) {
        if (e.getType().equals(equipmentType) &&
            (minConditionScore == null || e.getConditionScore() >= minConditionScore)) {
            boolean brandMatch = preferredBrand != null && preferredBrand.equals(e.getBrand());
            return brandMatch ? MatchingResult.FULL_MATCH : MatchingResult.PARTIAL_MATCH;
        }
        return MatchingResult.NO_MATCH;
    }

    public enum MatchingResult {
        NO_MATCH,
        PARTIAL_MATCH,
        FULL_MATCH
    }
}