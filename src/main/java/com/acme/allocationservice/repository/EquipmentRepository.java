package com.acme.allocationservice.repository;

import com.acme.allocationservice.model.AllocationPolicyItem;
import com.acme.allocationservice.model.Equipment;
import com.acme.allocationservice.model.EquipmentType;
import org.hibernate.annotations.BatchSize;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;
import java.util.stream.Stream;

public interface EquipmentRepository extends JpaRepository<Equipment, UUID>, JpaSpecificationExecutor<Equipment> {

    @Query("""
        SELECT e FROM Equipment e
        WHERE e.state = EquipmentState.available
          AND e.type = :type
          AND e.conditionScore >= :minScore
        ORDER BY
          CASE
            WHEN :brand IS NOT NULL AND e.brand = :brand THEN 0
            ELSE 1
          END,
          e.conditionScore DESC
    """)
    @BatchSize(size = 10)
    Stream<Equipment> findByPolicyOrdered(
            @Param("type") EquipmentType type,
            @Param("minScore") double minScore,
            @Param("brand") String brand
    );

    default Stream<Equipment> findByPolicy(AllocationPolicyItem item) {
        double minScore = item.getMinConditionScore() != null
                ? item.getMinConditionScore()
                : 0.0;

        return findByPolicyOrdered(
                item.getEquipmentType(),
                minScore,
                item.getPreferredBrand()
        );
    }

}