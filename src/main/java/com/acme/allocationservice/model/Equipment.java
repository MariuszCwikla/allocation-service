package com.acme.allocationservice.model;

import com.acme.allocationservice.exception.AllocationServiceException;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "equipments")
@ToString
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class Equipment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EquipmentType type;

    @Column(nullable = false, length = 100)
    private String brand;

    @Column(nullable = false, length = 200)
    private String model;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EquipmentState state = EquipmentState.available;

    @Column(name = "condition_score", nullable = false)
    private double conditionScore;

    @Column(name = "purchase_date", nullable = false)
    private LocalDate purchaseDate;

    @Column(name = "retired_at")
    private Instant retiredAt;

    @Column(name = "retirement_reason")
    private String retirementReason;

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

    @Column(name = "reserved_at")
    private Instant reservedAt;

    @Column(name = "confirmed_at")
    private Instant confirmedAt;

    public void makeReservation() {
        if (this.state == EquipmentState.available) {
            this.state = EquipmentState.reserved;
            this.reservedAt = Instant.now();
        } else {
            throw new AllocationServiceException("Unable to reserve equipment in state: " + this.state);
        }
    }

    public void confirmAllocation() {
        if (this.state == EquipmentState.reserved) {
            this.state = EquipmentState.assigned;
            this.confirmedAt = Instant.now();
        } else {
            throw new AllocationServiceException("Unable to confirm allocation in state: " + this.state);
        }
    }

    public void cancelAllocation() {
        if (this.state == EquipmentState.reserved) {
            this.state = EquipmentState.available;
            this.reservedAt = null;
        } else {
            throw new AllocationServiceException("Unable to cancel allocation in state: " + this.state);
        }
    }
}
