package com.acme.allocationservice.solvers;

import com.acme.allocationservice.model.AllocationRequest;

public interface Solver {

    SolverOutcome solve(AllocationRequest allocationRequest);
}
