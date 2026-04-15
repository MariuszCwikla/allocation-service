package com.acme.allocationservice.solvers.bipartitmatching;

import com.acme.allocationservice.model.Equipment;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class UNode {
    private final Equipment equipment;
    private final List<VNode> adjacencyList = new ArrayList<>();

    /**
     * NOTE: BipartiteMatchingAlgorithm uses this as key in a HashMap!
     */
    @EqualsAndHashCode.Include
    private UUID equipmentId() { return equipment.getId(); }
}
