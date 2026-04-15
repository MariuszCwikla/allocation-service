package com.acme.allocationservice.fixture;

import com.acme.allocationservice.model.Equipment;
import com.acme.allocationservice.model.EquipmentState;
import com.acme.allocationservice.model.EquipmentType;

import java.time.LocalDate;

public class EquipmentFixtures {

    public static Equipment availableLaptop() {
        Equipment e = new Equipment();
        e.setType(EquipmentType.main_computer);
        e.setBrand("Dell");
        e.setModel("XPS 15 9530");
        e.setState(EquipmentState.available);
        e.setConditionScore(0.95);
        e.setPurchaseDate(LocalDate.of(2023, 6, 1));
        return e;
    }

    public static Equipment assignedMonitor() {
        Equipment e = new Equipment();
        e.setType(EquipmentType.monitor);
        e.setBrand("LG");
        e.setModel("27UK850-W");
        e.setState(EquipmentState.assigned);
        e.setConditionScore(0.88);
        e.setPurchaseDate(LocalDate.of(2022, 3, 15));
        return e;
    }

    public static Equipment reservedKeyboard() {
        Equipment e = new Equipment();
        e.setType(EquipmentType.keyboard);
        e.setBrand("Logitech");
        e.setModel("MX Keys");
        e.setState(EquipmentState.reserved);
        e.setConditionScore(0.75);
        e.setPurchaseDate(LocalDate.of(2021, 11, 20));
        return e;
    }

    public static Equipment retiredMouse() {
        Equipment e = new Equipment();
        e.setType(EquipmentType.mouse);
        e.setBrand("Logitech");
        e.setModel("MX Master 3");
        e.setState(EquipmentState.retired);
        e.setConditionScore(0.30);
        e.setPurchaseDate(LocalDate.of(2019, 5, 10));
        e.setRetirementReason("Hardware failure");
        return e;
    }

    public static Equipment availableMouse() {
        Equipment e = new Equipment();
        e.setType(EquipmentType.mouse);
        e.setBrand("Apple");
        e.setModel("Magic Mouse");
        e.setState(EquipmentState.available);
        e.setConditionScore(0.9f);
        e.setPurchaseDate(LocalDate.of(2023, 1, 5));
        return e;
    }

    public static Equipment laptop(String brand) {
        return availableLaptop().setBrand(brand);
    }

    public static Equipment laptop(String brand, double score) {
        return availableLaptop().setBrand(brand).setConditionScore(score);
    }
}
