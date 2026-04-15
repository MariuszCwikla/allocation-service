package com.acme.allocationservice.controller;

import com.acme.allocationservice.dto.CreateEquipmentRequest;
import com.acme.allocationservice.dto.CreateEquipmentResponse;
import com.acme.allocationservice.dto.EquipmentDto;
import com.acme.allocationservice.model.Equipment;
import com.acme.allocationservice.model.EquipmentState;
import com.acme.allocationservice.model.EquipmentType;
import com.acme.allocationservice.repository.EquipmentRepository;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class EquipmentControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired EquipmentRepository equipmentRepository;

    @Test
    void testCreateEquipment() throws Exception {
        CreateEquipmentRequest request = new CreateEquipmentRequest();
        request.setType(EquipmentType.main_computer);
        request.setBrand("Apple");
        request.setModel("MacBook Pro 14");
        request.setConditionScore(0.95);
        request.setPurchaseDate(LocalDate.of(2023, 6, 1));

        MvcResult result = mockMvc.perform(post("/equipments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNotEmpty())
            .andReturn();

        CreateEquipmentResponse response = objectMapper.readValue(result.getResponse().getContentAsString(), CreateEquipmentResponse.class);

        Equipment saved = equipmentRepository.findById(response.getId()).orElseThrow();
        assertEquals(request.getType(), saved.getType());
        assertEquals(request.getBrand(), saved.getBrand());
        assertEquals(request.getModel(), saved.getModel());
        assertEquals(request.getConditionScore(), saved.getConditionScore());
        assertEquals(request.getPurchaseDate(), saved.getPurchaseDate());
        assertEquals(EquipmentState.available, saved.getState());
    }

    @Test
    void testGetEquipments() throws Exception {
        MvcResult result = mockMvc.perform(get("/equipments"))
            .andExpect(status().isOk())
            .andReturn();

        RestPage<EquipmentDto> page = objectMapper.readValue(
            result.getResponse().getContentAsString(),
            new TypeReference<RestPage<EquipmentDto>>() {});

        assertNotNull(page.getContent());
    }

    static class RestPage<T> extends PageImpl<T> {
        @JsonCreator
        public RestPage(
            @JsonProperty("content") List<T> content,
            @JsonProperty("number") int number,
            @JsonProperty("size") int size,
            @JsonProperty("totalElements") long totalElements
        ) {
            super(content, PageRequest.of(number, size), totalElements);
        }
    }
}