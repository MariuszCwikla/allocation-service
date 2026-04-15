package com.acme.allocationservice;

import com.acme.allocationservice.fixture.EquipmentFixtures;
import com.acme.allocationservice.model.AllocationPolicyItem;
import com.acme.allocationservice.model.Equipment;
import com.acme.allocationservice.model.EquipmentType;
import com.acme.allocationservice.repository.AllocationRequestRepository;
import com.acme.allocationservice.repository.EquipmentRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
public class EquipmentRepositoryTest {

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Autowired
    private AllocationRequestRepository allocationRequestRepository;

    @BeforeEach
    void setUp() {
        allocationRequestRepository.deleteAll();
        equipmentRepository.deleteAll();
        Equipment apple1 = EquipmentFixtures.laptop("Apple", 0.9);
        Equipment apple2 = EquipmentFixtures.laptop("Apple", 0.8);
        Equipment apple3 = EquipmentFixtures.laptop("Apple", 0.85);
        Equipment dell = EquipmentFixtures.laptop("Dell", 0.9);
        equipmentRepository.save(apple1);
        equipmentRepository.save(apple2);
        equipmentRepository.save(apple3);
        equipmentRepository.save(dell);
    }

    @Test
    void testFindBySoftPolicy() {
        AllocationPolicyItem policy = AllocationPolicyItem.builder()
            .equipmentType(EquipmentType.main_computer)
            .minConditionScore(0.89)
            .preferredBrand("Apple")
            .build();
        List<Equipment> found = equipmentRepository.findByPolicy(policy).toList();
        assertEquals(1, found.stream().filter(e -> e.getBrand().equals("Apple")).count());
        assertEquals(1, found.stream().filter(e -> e.getBrand().equals("Dell")).count());
        assertEquals(2, found.size());
    }

    @Test
    void testFindByPolicyWithPreferredBrand() {
        // preferredBrand is always a soft constraint — both matching brands are returned,
        // preferred brand first
        AllocationPolicyItem policy = AllocationPolicyItem.builder()
            .equipmentType(EquipmentType.main_computer)
            .minConditionScore(0.89)
            .preferredBrand("Apple")
            .build();
        List<Equipment> found = equipmentRepository.findByPolicy(policy).toList();
        assertEquals(2, found.size());
        assertEquals("Apple", found.get(0).getBrand());
    }
}