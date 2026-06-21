package com.club.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.club.common.Result;
import com.club.dto.InspectionDTOs;
import com.club.entity.InspectionPlan;
import com.club.entity.InspectionRecord;

public interface InspectionService extends IService<InspectionPlan> {
    Result<?> createPlan(InspectionDTOs.PlanCreateRequest request);
    Result<?> updatePlan(InspectionDTOs.PlanUpdateRequest request);
    Result<?> deletePlan(Integer id);
    Result<?> listPlans();
    Result<?> getPlanById(Integer id);
    Result<?> getMyTodoPlans();
    Result<?> submitRecord(InspectionDTOs.RecordSubmitRequest request);
    Result<?> listRecordsByPlan(Integer planId);
    Result<?> getStatistics();
    Result<?> listMyRecords();
}
