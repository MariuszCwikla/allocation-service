package com.acme.allocationservice.solvers;

import com.acme.allocationservice.model.AllocationPolicyItem;
import com.acme.allocationservice.model.Equipment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SolverOutcome {
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Matching {
        private AllocationPolicyItem policy;
        private Equipment equipment;
    }

    private List<Matching> matches;
    private String failureReason;

    public static SolverOutcome success(List<Matching> matches) {
        return new SolverOutcome(matches, null);
    }

    public static SolverOutcome failure(String failureReason) {
        return new SolverOutcome(List.of(), failureReason);
    }

    public boolean isSuccess() {
        return failureReason == null;
    }
}
