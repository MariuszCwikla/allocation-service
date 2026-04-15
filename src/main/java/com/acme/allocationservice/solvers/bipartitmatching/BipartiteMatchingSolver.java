package com.acme.allocationservice.solvers.bipartitmatching;

import com.acme.allocationservice.model.AllocationPolicyItem;
import com.acme.allocationservice.model.AllocationRequest;
import com.acme.allocationservice.model.Equipment;
import com.acme.allocationservice.repository.EquipmentRepository;
import com.acme.allocationservice.solvers.Solver;
import com.acme.allocationservice.solvers.SolverOutcome;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Stream;

@Slf4j
@Component
public class BipartiteMatchingSolver implements Solver {

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Override
    public SolverOutcome solve(AllocationRequest allocationRequest) {
        List<VNode> vNodes = new ArrayList<>();
        Map<UUID, UNode> uNodeMap = new LinkedHashMap<>();
        List<Stream<Equipment>> streams = new ArrayList<>();

        try {
            for (AllocationPolicyItem policy : allocationRequest.getPolicyItems()) {
                Stream<Equipment> stream = equipmentRepository.findByPolicy(policy);
                streams.add(stream);
                VNode vNode = new VNode(policy, stream.iterator());
                vNodes.add(vNode);
            }

            int maxNumOfEquipments = allocationRequest.getPolicyItems().size() * 20;             // See ADR 0002
            log.debug("Setting limit to {} equipments", maxNumOfEquipments);

            while (true) {
                boolean found = false;
                for (VNode vNode : vNodes) {
                    UNode uNode = createUNode(vNode, uNodeMap);
                    if (uNode != null) {
                        found = true;
                        if (uNodeMap.size() >= maxNumOfEquipments) {
                            log.debug("Number of equipments fetched is too big: {}", uNodeMap.size());
                            return SolverOutcome.failure("LIMIT_EXCEEDED");
                        }
                    }
                }
                if (!found) {
                    log.debug("All queries are exhausted.");
                    return SolverOutcome.failure("UNSOLVABLE");
                }
                BipartiteMatchingAlgorithm algorithm = new BipartiteMatchingAlgorithm(vNodes);
                SolverOutcome outcome = algorithm.solve();
                if (outcome.isSuccess()) {
                    return outcome;
                }
            }
        } finally {
            streams.forEach(Stream::close);
        }
    }

    private UNode createUNode(VNode vNode, Map<UUID, UNode> uNodeMap) {
        if (!vNode.hasNextEquipment()) {
            log.debug("No more equipments for {}", vNode.getPolicy());
            return null;
        }
        Equipment e = vNode.nextEquipment();
        log.debug("Fetched equipment: {} {} {} {}", e.getId(), e.getType(), e.getBrand(), e.getModel());
        UNode uNode = uNodeMap.get(e.getId());
        if (uNode == null) {
            uNode = new UNode(e);
            uNodeMap.put(e.getId(), uNode);
        }
        vNode.wire(uNode);
        return uNode;
    }

}
