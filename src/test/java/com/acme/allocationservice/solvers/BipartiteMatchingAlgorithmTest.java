package com.acme.allocationservice.solvers;

import com.acme.allocationservice.model.AllocationPolicyItem;
import com.acme.allocationservice.model.Equipment;
import com.acme.allocationservice.model.EquipmentType;
import com.acme.allocationservice.solvers.bipartitmatching.BipartiteMatchingAlgorithm;
import com.acme.allocationservice.solvers.bipartitmatching.UNode;
import com.acme.allocationservice.solvers.bipartitmatching.VNode;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BipartiteMatchingAlgorithmTest {

    private AllocationPolicyItem policy(EquipmentType type) {
        return policy(type, null, null);
    }

    private AllocationPolicyItem policy(EquipmentType type, double minScore) {
        return policy(type, minScore, null);
    }

    private AllocationPolicyItem policy(EquipmentType type, String brand) {
        return policy(type, null, brand);
    }

    private AllocationPolicyItem policy(EquipmentType type, double minScore, String brand) {
        return policy(type, (Double) minScore, brand);
    }

    private AllocationPolicyItem policy(EquipmentType type, Double minScore, String brand) {
        return AllocationPolicyItem.builder()
            .id(UUID.randomUUID())
            .equipmentType(type)
            .minConditionScore(minScore)
            .preferredBrand(brand)
            .build();
    }

    private Equipment equipment(EquipmentType type, String brand, double conditionScore) {
        Equipment e = new Equipment();
        e.setId(UUID.randomUUID());
        e.setType(type);
        e.setBrand(brand);
        e.setModel("Model");
        e.setConditionScore(conditionScore);
        e.setPurchaseDate(LocalDate.of(2023, 1, 1));
        return e;
    }

    @Test
    void noNodes_returnsEmptySolution() {
        var algo = new BipartiteMatchingAlgorithm(List.of());

        SolverOutcome result = algo.solve();

        assertTrue(result.isSuccess());
        assertTrue(result.getMatches().isEmpty());
    }

    @Test
    void singlePolicy_singleCompatibleEquipment_returnsMatch() {
        AllocationPolicyItem p = policy(EquipmentType.main_computer, 0.8, "Apple");
        Equipment e = equipment(EquipmentType.main_computer, "Apple", 0.9);

        VNode v = new VNode(p, Collections.emptyIterator());
        UNode u = new UNode(e);
        v.wire(u);

        SolverOutcome result = new BipartiteMatchingAlgorithm(List.of(v)).solve();

        assertTrue(result.isSuccess());
        List<SolverOutcome.Matching> matches = result.getMatches();
        assertEquals(1, matches.size());
        assertEquals(p, matches.get(0).getPolicy());
        assertEquals(e, matches.get(0).getEquipment());
    }

    @Test
    void singlePolicy_noAdjacentEquipment_returnsEmpty() {
        AllocationPolicyItem p = policy(EquipmentType.main_computer, 0.8, "Apple");
        VNode v = new VNode(p, Collections.emptyIterator());
        // v.adjacencyList is empty — no candidate equipment

        SolverOutcome result = new BipartiteMatchingAlgorithm(List.of(v)).solve();

        assertFalse(result.isSuccess());
        assertEquals("UNSOLVABLE", result.getFailureReason());
    }

    /**
     * Tests the scenario where two independent policies (disjoint equipment requirements)
     * are both satisfied by their respective matching equipment. Validates that all matches
     * are correct and complete.
     *
     * This test sets up:
     * - Two policies: each requiring a distinct type of equipment with specific brand and condition score.
     * - Two pieces of equipment: each satisfying one of the policies based on type, brand, and condition score.
     * - The wiring of VNode and UNode objects to represent the bipartite graph.
     *
     * The test performs the following:
     * - Executes the bipartite matching algorithm.
     * - Asserts that a solution is present.
     * - Verifies that the number of matches in the solution corresponds to the total number of policies and equipment.
     */
    @Test
    void twoPolicies_disjointEquipment_allMatched() {
        AllocationPolicyItem p1 = policy(EquipmentType.main_computer, 0.8, "Apple");
        AllocationPolicyItem p2 = policy(EquipmentType.monitor, 0.7, "LG");
        Equipment e1 = equipment(EquipmentType.main_computer, "Apple", 0.9);
        Equipment e2 = equipment(EquipmentType.monitor, "LG", 0.85);

        VNode v1 = new VNode(p1, Collections.emptyIterator());
        VNode v2 = new VNode(p2, Collections.emptyIterator());
        UNode u1 = new UNode(e1);
        UNode u2 = new UNode(e2);
        v1.wire(u1);
        v2.wire(u2);

        SolverOutcome result = new BipartiteMatchingAlgorithm(List.of(v1, v2)).solve();

        assertTrue(result.isSuccess());
        assertEquals(2, result.getMatches().size());
    }

    @Test
    void twoPolicies_shareOneEquipment_returnsEmpty() {
        // Both policies can only use E1 — impossible to match both
        AllocationPolicyItem p1 = policy(EquipmentType.main_computer, 0.8, "Apple");
        AllocationPolicyItem p2 = policy(EquipmentType.main_computer, 0.8, "Apple");
        Equipment e1 = equipment(EquipmentType.main_computer, "Apple", 0.9);

        VNode v1 = new VNode(p1, Collections.emptyIterator());
        VNode v2 = new VNode(p2, Collections.emptyIterator());
        UNode u1 = new UNode(e1);
        v1.wire(u1);
        v2.wire(u1);

        SolverOutcome result = new BipartiteMatchingAlgorithm(List.of(v1, v2)).solve();

        assertFalse(result.isSuccess());
        assertEquals("UNSOLVABLE", result.getFailureReason());
    }

    @Test
    void augmentingPath_displacesPreviousMatch_allMatched() {
        // V1 → {U1, U2}
        // V2 → {U1} only
        //
        // DFS processes V1 first: V1 → U1 (greedy first pick)
        // DFS processes V2: U1 is taken; augmenting path finds V1 can move to U2
        // Final matching: V1→U2, V2→U1
        AllocationPolicyItem p1 = policy(EquipmentType.monitor, 0.7);
        AllocationPolicyItem p2 = policy(EquipmentType.monitor, 0.7);
        Equipment e1 = equipment(EquipmentType.monitor, "Apple", 0.9);
        Equipment e2 = equipment(EquipmentType.monitor, "Apple", 0.85);

        VNode v1 = new VNode(p1, Collections.emptyIterator());
        VNode v2 = new VNode(p2, Collections.emptyIterator());
        UNode u1 = new UNode(e1);
        UNode u2 = new UNode(e2);
        v1.wire(u1);
        v1.wire(u2);
        v2.wire(u1);

        SolverOutcome result = new BipartiteMatchingAlgorithm(List.of(v1, v2)).solve();

        assertTrue(result.isSuccess());
        assertEquals(2, result.getMatches().size());
    }

    @Test
    void preferredBrand_preferredOverNonPreferredBrand() {
        // Policy prefers Apple (soft constraint).
        // Adjacency list has Dell first (PARTIAL_MATCH), then Apple (FULL_MATCH).
        // The algorithm should sort FULL_MATCH edges first and assign Apple.
        AllocationPolicyItem p = policy(EquipmentType.main_computer, 0.8, "Apple");
        Equipment eDell = equipment(EquipmentType.main_computer, "Dell", 0.9);  // PARTIAL_MATCH
        Equipment eApple = equipment(EquipmentType.main_computer, "Apple", 0.9); // FULL_MATCH

        VNode v = new VNode(p, Collections.emptyIterator());
        UNode uDell = new UNode(eDell);
        UNode uApple = new UNode(eApple);
        v.wire(uDell);
        v.wire(uApple);

        SolverOutcome result = new BipartiteMatchingAlgorithm(List.of(v)).solve();

        assertTrue(result.isSuccess());
        assertEquals(eApple, result.getMatches().get(0).getEquipment());
    }

    @Test
    void codingTaskExample() {
        //literally taken from coding task description
        AllocationPolicyItem p1 = policy(EquipmentType.main_computer, "Apple");
        AllocationPolicyItem p2 = policy(EquipmentType.monitor);
        AllocationPolicyItem p3 = policy(EquipmentType.monitor);
        Equipment e1 = equipment(EquipmentType.main_computer, "Apple", 0.9);
        Equipment e2 = equipment(EquipmentType.monitor, "LG", 0.9);
        Equipment e3 = equipment(EquipmentType.monitor, "Dell", 0.9);

        VNode v1 = new VNode(p1, Collections.emptyIterator());
        VNode v2 = new VNode(p2, Collections.emptyIterator());
        VNode v3 = new VNode(p3, Collections.emptyIterator());
        UNode u1 = new UNode(e1);
        UNode u2 = new UNode(e2);
        UNode u3 = new UNode(e3);

        v1.wire(u1);
        v2.wire(u2); v2.wire(u3);
        v3.wire(u2); v3.wire(u3);

        SolverOutcome outcome = new BipartiteMatchingAlgorithm(List.of(v1, v2, v3)).solve();
        assertTrue(outcome.isSuccess());
        assertEquals(e1, getMatching(outcome, p1));
        // The algorithm matches oddly e2 and e3 to p2 and p3, respectively but still this is correct!
        assertEquals(e3, getMatching(outcome, p2));
        assertEquals(e2, getMatching(outcome, p3));
    }

    private Equipment getMatching(SolverOutcome outcome, AllocationPolicyItem policy) {
        // NOTE: we do not test equality here but reference! Because AllocationPolicyItem do not have identity in this version (yet)
        return outcome.getMatches().stream().filter(m -> m.getPolicy() == policy).findFirst().get().getEquipment();
    }

    @Test
    void codingTaskExampleEnhanced() {
        // pretty similar to example from coding task but little enhanced
        AllocationPolicyItem p1 = policy(EquipmentType.monitor, 0.9);
        AllocationPolicyItem p2 = policy(EquipmentType.monitor, 0.8);
        Equipment e1 = equipment(EquipmentType.monitor, "LG", 0.9);
        Equipment e2 = equipment(EquipmentType.monitor, "Dell", 0.85);

        // This is how actually solver works... First it will only assign LG to both policies and algorithm shall not be returning solution
        // but affter adding second equipment it shall find proper solution
        VNode v1 = new VNode(p1, Collections.emptyIterator());
        VNode v2 = new VNode(p2, Collections.emptyIterator());
        UNode u1 = new UNode(e1);

        v1.wire(u1);
        v2.wire(u1);
        assertFalse(new BipartiteMatchingAlgorithm(List.of(v1, v2)).solve().isSuccess());

        UNode u2 = new UNode(e2);
        v2.wire(u2);

        assertTrue(new BipartiteMatchingAlgorithm(List.of(v1, v2)).solve().isSuccess());
    }
}
