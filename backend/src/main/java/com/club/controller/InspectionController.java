package com.club.controller;

import com.club.common.Result;
import com.club.dto.InspectionDTOs;
import com.club.entity.Device;
import com.club.entity.InspectionPlan;
import com.club.service.DeviceService;
import com.club.service.InspectionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inspections")
public class InspectionController {

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private InspectionService inspectionService;

    @GetMapping("/stats")
    public Result<?> getStatistics() {
        return inspectionService.getStatistics();
    }

    @GetMapping("/devices")
    public Result<?> listDevices() {
        return deviceService.listDevices();
    }

    @GetMapping("/devices/{id}")
    public Result<?> getDeviceById(@PathVariable Integer id) {
        return deviceService.getDeviceById(id);
    }

    @PostMapping("/devices")
    public Result<?> createDevice(@Valid @RequestBody Device device) {
        return deviceService.createDevice(device);
    }

    @PutMapping("/devices")
    public Result<?> updateDevice(@Valid @RequestBody Device device) {
        return deviceService.updateDevice(device);
    }

    @DeleteMapping("/devices/{id}")
    public Result<?> deleteDevice(@PathVariable Integer id) {
        return deviceService.deleteDevice(id);
    }

    @GetMapping("/plans")
    public Result<?> listPlans() {
        return inspectionService.listPlans();
    }

    @GetMapping("/plans/{id}")
    public Result<?> getPlanById(@PathVariable Integer id) {
        return inspectionService.getPlanById(id);
    }

    @PostMapping("/plans")
    public Result<?> createPlan(@Valid @RequestBody InspectionDTOs.PlanCreateRequest request) {
        return inspectionService.createPlan(request);
    }

    @PutMapping("/plans")
    public Result<?> updatePlan(@Valid @RequestBody InspectionDTOs.PlanUpdateRequest request) {
        return inspectionService.updatePlan(request);
    }

    @DeleteMapping("/plans/{id}")
    public Result<?> deletePlan(@PathVariable Integer id) {
        return inspectionService.deletePlan(id);
    }

    @GetMapping("/plans/my-todo")
    public Result<?> getMyTodoPlans() {
        return inspectionService.getMyTodoPlans();
    }

    @PostMapping("/records")
    public Result<?> submitRecord(@Valid @RequestBody InspectionDTOs.RecordSubmitRequest request) {
        return inspectionService.submitRecord(request);
    }

    @GetMapping("/records/my")
    public Result<?> listMyRecords() {
        return inspectionService.listMyRecords();
    }

    @GetMapping("/plans/{planId}/records")
    public Result<?> listRecordsByPlan(@PathVariable Integer planId) {
        return inspectionService.listRecordsByPlan(planId);
    }
}
