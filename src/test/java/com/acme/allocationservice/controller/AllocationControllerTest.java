package com.acme.allocationservice.controller;

import com.acme.allocationservice.dto.CreateAllocationRequest;
import com.acme.allocationservice.dto.CreateAllocationResponse;
import com.acme.allocationservice.fixture.EquipmentFixtures;
import com.acme.allocationservice.fixture.PolicyItemRequestFixtures;
import com.acme.allocationservice.model.AllocationRequestState;
import com.acme.allocationservice.model.EquipmentType;
import com.acme.allocationservice.repository.AllocationRequestRepository;
import com.acme.allocationservice.repository.EquipmentRepository;
import com.acme.allocationservice.service.AllocationService;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AllocationControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired EquipmentRepository equipmentRepository;
    @Autowired AllocationService allocationService;
    @Autowired AllocationRequestRepository allocationRequestRepository;

    @Test
    void createAllocation_returns202WithId() throws Exception {
        CreateAllocationRequest request = new CreateAllocationRequest();
        request.setEmployeeId(UUID.randomUUID());
        request.setPolicy(List.of(PolicyItemRequestFixtures.policyItem(EquipmentType.main_computer)));

        MvcResult result = mockMvc.perform(post("/allocations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isAccepted())
            .andExpect(jsonPath("$.allocationId").isNotEmpty())
            .andReturn();

        UUID allocationId = objectMapper.readValue(
            result.getResponse().getContentAsString(), CreateAllocationResponse.class).getAllocationId();

        Awaitility.await()
            .atMost(Duration.ofSeconds(10))
            .until(() -> allocationRequestRepository.findById(allocationId)
                .map(a -> a.getState() != AllocationRequestState.pending)
                .orElse(false));
    }

    @Test
    void getAllocation_returnsState() throws Exception {
        var allocation = allocationService.createAllocationRequest(UUID.randomUUID(),
            List.of(PolicyItemRequestFixtures.policyItem(EquipmentType.main_computer)));

        mockMvc.perform(get("/allocations/{id}", allocation.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.allocationId").value(allocation.getId().toString()))
            .andExpect(jsonPath("$.state").value(AllocationRequestState.pending.name()));
    }

    @Test
    void getAllocation_notFound_returns404() throws Exception {
        mockMvc.perform(get("/allocations/{id}", UUID.randomUUID()))
            .andExpect(status().isNotFound());
    }

    @Test
    void confirmAllocation_happyPath_returns200() throws Exception {
        equipmentRepository.save(EquipmentFixtures.availableLaptop());
        var allocation = allocationService.createAllocationRequest(UUID.randomUUID(),
            List.of(PolicyItemRequestFixtures.policyItem(EquipmentType.main_computer)));
        allocationService.processAllocationRequestAsync(allocation.getId()).get();

        mockMvc.perform(post("/allocations/{id}/confirm",  allocation.getId()))
            .andExpect(status().isOk());
    }

    @Test
    void cancelAllocation_happyPath_returns200() throws Exception {
        equipmentRepository.save(EquipmentFixtures.availableLaptop());
        var allocation = allocationService.createAllocationRequest(UUID.randomUUID(),
            List.of(PolicyItemRequestFixtures.policyItem(EquipmentType.main_computer)));
        allocationService.processAllocationRequestAsync(allocation.getId()).get();

        mockMvc.perform(post("/allocations/{id}/cancel", allocation.getId()))
            .andExpect(status().isOk());
    }

    @Test
    void confirmAllocation_invalidState_returns400() throws Exception {
        var allocation = allocationService.createAllocationRequest(UUID.randomUUID(),
            List.of(PolicyItemRequestFixtures.policyItem(EquipmentType.main_computer)));

        mockMvc.perform(post("/allocations/{id}/confirm", allocation.getId()))
            .andExpect(status().isBadRequest());
    }

    @Test
    void cancelAllocation_invalidState_returns400() throws Exception {
        var allocation = allocationService.createAllocationRequest(UUID.randomUUID(),
            List.of(PolicyItemRequestFixtures.policyItem(EquipmentType.main_computer)));

        mockMvc.perform(post("/allocations/{id}/cancel", allocation.getId()))
            .andExpect(status().isBadRequest());
    }
}