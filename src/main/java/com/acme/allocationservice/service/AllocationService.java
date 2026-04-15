package com.acme.allocationservice.service;

import com.acme.allocationservice.dto.PolicyItemDto;
import com.acme.allocationservice.exception.NotFoundException;
import com.acme.allocationservice.model.AllocationRequest;
import com.acme.allocationservice.repository.AllocationRequestRepository;
import com.acme.allocationservice.solvers.Solver;
import com.acme.allocationservice.solvers.SolverOutcome;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

@Slf4j
@Service
public class AllocationService {

    @Lazy
    @Autowired
    private AllocationService self;

    @Autowired
    private AllocationRequestRepository allocationRequestRepository;

    @Autowired
    private Solver solver;

    @Transactional(readOnly = true)
    public AllocationRequest getById(UUID id) {
        return allocationRequestRepository.findById(id)
            .orElseThrow(NotFoundException::new);
    }

    @Transactional
    public AllocationRequest createAllocationRequest(@NotNull UUID employeeId, @NotEmpty @Valid List<PolicyItemDto> policies) {
        AllocationRequest allocationRequest = AllocationRequest.builder()
                .employeeId(employeeId)
                .build();
        policies.forEach(policy -> {
                    allocationRequest.createPolicyItem(policy.getEquipmentType(), policy.getMinConditionScore(), policy.getPreferredBrand());
                });
        allocationRequestRepository.save(allocationRequest);
        return allocationRequest;
    }

    @Async  // Simple async event bus to be replaced with kafka in version 2.0 :)
    public Future<Void> processAllocationRequestAsync(UUID allocationRequestId) {
        try {
            self.processAllocationRequestSync(allocationRequestId);
        } catch (Exception e) {
            self.failAllocationRequest(allocationRequestId, "PROCESSING_FAILED");
            log.error("Error processing allocation request {}", allocationRequestId, e);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Retryable(
        includes = ObjectOptimisticLockingFailureException.class,
        maxRetries = 3,
        delay = 100,
        multiplier = 2.0
    )
    @Transactional
    public void processAllocationRequestSync(UUID allocationRequestId) {
        allocationRequestRepository.findById(allocationRequestId).ifPresentOrElse(allocationRequest -> {
                log.debug("Processing allocation request {} using solver {}", allocationRequestId, solver.getClass().getSimpleName());
                SolverOutcome outcome = solver.solve(allocationRequest);
                if (outcome.isSuccess()) {
                    log.debug("Found solution for request {}", allocationRequestId);
                    allocationRequest.completeAllocation(outcome.getMatches().stream()
                        .map(SolverOutcome.Matching::getEquipment)
                        .toList());
                    log.debug("Allocation request {} successfully processed", allocationRequestId);
                } else {
                    log.error("Unable to find solution for request {}. Reason={}", allocationRequestId, outcome.getFailureReason());
                    allocationRequest.failAllocation(outcome.getFailureReason());
                }
            },
            () -> log.error("Unable to find allocation request {}", allocationRequestId)
        );
    }

    @Transactional
    public void failAllocationRequest(UUID allocationRequestId, String failureReason) {
        AllocationRequest request = allocationRequestRepository.findById(allocationRequestId).orElseThrow(NotFoundException::new);
        request.failAllocation(failureReason);
        log.error("Allocation request {} failed", allocationRequestId);
    }

    @Transactional
    public void confirmAllocation(UUID allocationRequestId) {
        AllocationRequest request = allocationRequestRepository.findById(allocationRequestId).orElseThrow(NotFoundException::new);
        request.confirmAllocation();
        log.debug("Allocation request {} confirmed", allocationRequestId);
    }

    @Transactional
    public void cancelAllocation(UUID allocationRequestId) {
        AllocationRequest request = allocationRequestRepository.findById(allocationRequestId).orElseThrow(NotFoundException::new);
        request.cancelAllocation();
        log.debug("Allocation request {} cancelled", allocationRequestId);
    }
}

