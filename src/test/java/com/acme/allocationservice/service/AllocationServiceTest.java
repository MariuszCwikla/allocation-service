package com.acme.allocationservice.service;

import com.acme.allocationservice.exception.AllocationServiceException;
import com.acme.allocationservice.fixture.EquipmentFixtures;
import com.acme.allocationservice.fixture.PolicyItemRequestFixtures;
import com.acme.allocationservice.model.*;
import com.acme.allocationservice.repository.AllocationRequestRepository;
import com.acme.allocationservice.repository.EquipmentRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class AllocationServiceTest {

    @Autowired
    private AllocationService allocationService;

    @Autowired
    private AllocationRequestRepository allocationRequestRepository;

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;


    private TransactionTemplate transactionTemplate;

    @BeforeEach
    void setUp() {
        allocationRequestRepository.deleteAll();
        equipmentRepository.deleteAll();
        transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Test
    @Transactional
    void testCreateAllocationRequest() {
        UUID employeeId = UUID.randomUUID();

        AllocationRequest request = allocationService.createAllocationRequest(employeeId, List.of(
            PolicyItemRequestFixtures.policyItem(EquipmentType.main_computer, 0.8, "Apple"),
            PolicyItemRequestFixtures.policyItem(EquipmentType.mouse, null, null)
        ));

        AllocationRequest saved = allocationRequestRepository.findById(request.getId()).orElseThrow();
        assertEquals(employeeId, saved.getEmployeeId());
        assertEquals(AllocationRequestState.pending, saved.getState());
        assertEquals(2, saved.getPolicyItems().size());
    }

    @Test
    void testProcessAllocationRequestSolutionNotFound() throws Exception {
        AllocationRequest request = allocationService.createAllocationRequest(
                UUID.randomUUID(),
                List.of(PolicyItemRequestFixtures.policyItem(EquipmentType.main_computer))
        );

        allocationService.processAllocationRequestSync(request.getId());

        AllocationRequest saved = allocationRequestRepository.findById(request.getId()).orElseThrow();
        assertEquals(AllocationRequestState.failed, saved.getState());
        assertEquals("UNSOLVABLE", saved.getFailureReason());
    }

    @Test
    void testProcessAllocationRequestOneLaptop() throws Exception {
        Equipment laptop = equipmentRepository.save(EquipmentFixtures.availableLaptop());
        AllocationRequest request = allocationService.createAllocationRequest(
                UUID.randomUUID(),
                List.of(PolicyItemRequestFixtures.policyItem(EquipmentType.main_computer))
        );

        allocationService.processAllocationRequestAsync(request.getId()).get();
        transactionTemplate.executeWithoutResult(status -> {
            AllocationRequest saved = allocationRequestRepository.findById(request.getId()).orElseThrow();
            assertEquals(AllocationRequestState.allocated, saved.getState());
            assertNull(saved.getFailureReason());
            assertEquals(1, saved.getEquipments().size());
            assertTrue(saved.getEquipments().stream().anyMatch(e -> e.getId().equals(laptop.getId())));
            assertEquals(EquipmentState.reserved, equipmentRepository.findById(laptop.getId()).orElseThrow().getState());
        });
    }

    @Test
    void testConfirmAllocation_happyPath() throws Exception {
        Equipment laptop = equipmentRepository.save(EquipmentFixtures.availableLaptop());
        AllocationRequest request = allocationService.createAllocationRequest(
                UUID.randomUUID(),
                List.of(PolicyItemRequestFixtures.policyItem(EquipmentType.main_computer))
        );
        allocationService.processAllocationRequestAsync(request.getId()).get();

        allocationService.confirmAllocation(request.getId());

        transactionTemplate.executeWithoutResult(status -> {
            AllocationRequest saved = allocationRequestRepository.findById(request.getId()).orElseThrow();
            assertEquals(AllocationRequestState.confirmed, saved.getState());
            assertEquals(EquipmentState.assigned, equipmentRepository.findById(laptop.getId()).orElseThrow().getState());
        });
    }

    @Test
    void testConfirmAllocation_notAllocated_throws() {
        AllocationRequest request = allocationService.createAllocationRequest(
                UUID.randomUUID(),
                List.of(PolicyItemRequestFixtures.policyItem(EquipmentType.main_computer))
        );
        // request is still in 'pending' state — confirm must be rejected
        assertThrows(AllocationServiceException.class, () -> allocationService.confirmAllocation(request.getId()));
    }

    @Test
    void testCancelAllocation_happyPath() throws Exception {
        Equipment laptop = equipmentRepository.save(EquipmentFixtures.availableLaptop());
        AllocationRequest request = allocationService.createAllocationRequest(
                UUID.randomUUID(),
                List.of(PolicyItemRequestFixtures.policyItem(EquipmentType.main_computer))
        );
        allocationService.processAllocationRequestAsync(request.getId()).get();

        allocationService.cancelAllocation(request.getId());

        transactionTemplate.executeWithoutResult(status -> {
            AllocationRequest saved = allocationRequestRepository.findById(request.getId()).orElseThrow();
            assertEquals(AllocationRequestState.cancelled, saved.getState());
            assertEquals(EquipmentState.available, equipmentRepository.findById(laptop.getId()).orElseThrow().getState());
        });
    }

    @Test
    void testCancelAllocation_notAllocated_throws() {
        AllocationRequest request = allocationService.createAllocationRequest(
                UUID.randomUUID(),
                List.of(PolicyItemRequestFixtures.policyItem(EquipmentType.main_computer))
        );
        // request is still in 'pending' state — cancel must be rejected
        assertThrows(AllocationServiceException.class, () -> allocationService.cancelAllocation(request.getId()));
    }
}