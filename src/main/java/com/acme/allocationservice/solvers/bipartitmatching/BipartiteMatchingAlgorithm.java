package com.acme.allocationservice.solvers.bipartitmatching;

import com.acme.allocationservice.solvers.SolverOutcome;
import lombok.RequiredArgsConstructor;

import java.util.*;

@RequiredArgsConstructor
public class BipartiteMatchingAlgorithm {
    private final List<VNode> vNodes;

    public SolverOutcome solve() {
        // Sort each VNode's adjacency list so FULL_MATCH (hard constraints satisfied)
        // edges are tried before PARTIAL_MATCH (soft constraints) edges.
        for (VNode v : vNodes) {
            v.sortNeighborsByMatch();
        }

        Map<VNode, UNode> matchV = new HashMap<>();
        Map<UNode, VNode> matchU = new HashMap<>();

        for (VNode v : vNodes) {
            dfs(v, matchV, matchU, new HashSet<>());
        }

        if (matchV.size() < vNodes.size()) {
            return SolverOutcome.failure("UNSOLVABLE");
        }

        List<SolverOutcome.Matching> matchings = new ArrayList<>();
        for (Map.Entry<VNode, UNode> entry : matchV.entrySet()) {
            matchings.add(SolverOutcome.Matching.builder()
                .policy(entry.getKey().getPolicy())
                .equipment(entry.getValue().getEquipment())
                .build());
        }
        return SolverOutcome.success(matchings);
    }

    /**
     * Augmenting-path DFS. Tries to find a free UNode reachable from {@code v}
     * by alternating between unmatched and matched edges.
     *
     * @param v        the VNode to augment from
     * @param matchV   current matching: VNode → UNode
     * @param matchU   current matching: UNode → VNode
     * @param visited  UNodes already visited in this DFS to avoid cycles
     * @return true if an augmenting path was found and the matching was updated
     */
    private boolean dfs(VNode v, Map<VNode, UNode> matchV, Map<UNode, VNode> matchU, Set<UNode> visited) {
        for (UNode u : v.getAdjacencyList()) {
            if (visited.add(u)) {
                VNode currentMatch = matchU.get(u);
                if (currentMatch == null || dfs(currentMatch, matchV, matchU, visited)) {
                    matchV.put(v, u);
                    matchU.put(u, v);
                    return true;
                }
            }
        }
        return false;
    }
}
