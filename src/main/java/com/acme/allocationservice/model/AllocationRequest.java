package com.acme.allocationservice.model;

import com.acme.allocationservice.exception.AllocationServiceException;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.Accessors;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "allocation_requests")
@ToString
@Getter
@Setter(value = AccessLevel.PROTECTED)
@Accessors(chain = true)
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class AllocationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AllocationRequestState state = AllocationRequestState.pending;

    @Column(name = "failure_reason", length = 100)
    private String failureReason;

    @OneToMany(mappedBy = "allocationRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private List<AllocationPolicyItem> policyItems = new ArrayList<>();

    @ManyToMany
    @JoinTable(
        name = "allocation_equipments",
        joinColumns = @JoinColumn(name = "allocation_request_id"),
        inverseJoinColumns = @JoinColumn(name = "equipment_id")
    )
    @Builder.Default
    @ToString.Exclude
    private Set<Equipment> equipments = new HashSet<>();

    @Version
    private int version;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    @Setter(lombok.AccessLevel.NONE)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    @Setter(lombok.AccessLevel.NONE)
    private Instant updatedAt;

    @Column(name = "allocated_at")
    private Instant allocatedAt;

    @Column(name = "confirmed_at")
    private Instant confirmedAt;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;


    public AllocationPolicyItem createPolicyItem(@NotNull EquipmentType equipmentType, @DecimalMin("0.0") @DecimalMax("1.0") Double minConditionScore, String preferredBrand) {
        ensureNotFinished();
        AllocationPolicyItem item = AllocationPolicyItem.builder()
                .equipmentType(equipmentType)
                .minConditionScore(minConditionScore)
                .preferredBrand(preferredBrand)
                .allocationRequest(this)
                .build();
        policyItems.add(item);
        return item;
    }

    public void completeAllocation(Collection<Equipment> allocatedEquipments) {
        ensureNotFinished();
        for (Equipment equipment : allocatedEquipments) {
            equipment.makeReservation();
            this.equipments.add(equipment);
        }
        this.failureReason = null;
        this.state = AllocationRequestState.allocated;
        this.allocatedAt = Instant.now();
    }

    public void failAllocation(String failureReason) {
        ensureNotFinished();
        this.failureReason = failureReason;
        this.state = AllocationRequestState.failed;
    }

    public void confirmAllocation() {
        ensureAllocated();
        this.state = AllocationRequestState.confirmed;
        this.confirmedAt = Instant.now();
        for(Equipment e : this.equipments) {
            e.confirmAllocation();
        }
    }

    public void cancelAllocation() {
        ensureAllocated();
        this.state = AllocationRequestState.cancelled;
        this.cancelledAt = Instant.now();
        for(Equipment e : this.equipments) {
            e.cancelAllocation();
        }
    }

    private void ensureNotFinished() {
        if (state == AllocationRequestState.allocated || state == AllocationRequestState.failed) {
            throw new AllocationServiceException("Allocation request is already completed");
        }
    }
    private void ensureAllocated() {
        if (state != AllocationRequestState.allocated) {
            throw new AllocationServiceException("Allocation request must be in 'allocated' state");
        }
    }
}
