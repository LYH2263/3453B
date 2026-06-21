package com.club.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.club.common.Result;
import com.club.dto.VolunteerDTOs;
import com.club.entity.VolunteerRecord;
import com.club.entity.VolunteerStat;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

public interface VolunteerService extends IService<VolunteerRecord> {
    Result<?> submitRecord(VolunteerDTOs.SubmitRequest request);
    Result<?> getMyRecords();
    Result<?> auditRecord(Integer id, VolunteerDTOs.AuditRequest request);
    Result<?> getPendingAudits();
    Result<?> getAllRecords(VolunteerDTOs.QueryRequest request);
    Result<?> getStatsSummary(VolunteerDTOs.QueryRequest request);
    Result<?> getUserStats(Integer userId);
    Result<?> getClubStats();
    void exportRecords(VolunteerDTOs.QueryRequest request, HttpServletResponse response) throws IOException;
    void exportStats(HttpServletResponse response) throws IOException;
}
