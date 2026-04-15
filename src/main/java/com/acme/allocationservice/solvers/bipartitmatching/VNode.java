package com.acme.allocationservice.solvers.bipartitmatching;

import com.acme.allocationservice.model.AllocationPolicyItem;
import com.acme.allocationservice.model.Equipment;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class VNode {
    @Getter
    private final AllocationPolicyItem policy;
    private final Iterator<Equipment> equipments;
    @Getter
    private final List<UNode> adjacencyList = new ArrayList<>();

    /**
     * NOTE: BipartiteMatchingAlgorithm uses this as key in a HashMap!
     */
    @EqualsAndHashCode.Include
    private UUID policyId() { return policy.getId(); }

    public void wire(UNode uNode) {
        this.adjacencyList.add(uNode);
        uNode.getAdjacencyList().add(this);
    }

    public boolean hasNextEquipment() {
        return equipments.hasNext();
    }

    public Equipment nextEquipment() {
        return equipments.next();
    }

    public void sortNeighborsByMatch() {
        adjacencyList.sort((u1, u2) ->
            policy.getMatch(u2.getEquipment()).compareTo(policy.getMatch(u1.getEquipment())));
    }
}