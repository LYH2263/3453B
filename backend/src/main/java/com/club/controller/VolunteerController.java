package com.club.controller;

import com.club.common.Result;
import com.club.common.annotation.Log;
import com.club.dto.VolunteerDTOs;
import com.club.service.VolunteerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Tag(name = "志愿服务管理", description = "志愿服务记录的提交、审核、统计与导出")
@RestController
@RequestMapping("/api/volunteer")
public class VolunteerController {

    @Autowired
    private VolunteerService volunteerService;

    @Operation(summary = "学生提交志愿服务记录")
    @PostMapping
    @PreAuthorize("hasAnyRole('MEMBER', 'CLUB_LEADER', 'UNION_ADMIN', 'ADMIN')")
    public Result<?> submitRecord(@Valid @RequestBody VolunteerDTOs.SubmitRequest request) {
        return volunteerService.submitRecord(request);
    }

    @Operation(summary = "学生查看我的志愿服务记录")
    @GetMapping("/my")
    public Result<?> getMyRecords() {
        return volunteerService.getMyRecords();
    }

    @Operation(summary = "查看待审核记录（负责人/管理员）")
    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('CLUB_LEADER', 'UNION_ADMIN', 'ADMIN')")
    public Result<?> getPendingAudits() {
        return volunteerService.getPendingAudits();
    }

    @Operation(summary = "审核志愿服务记录（负责人/管理员）")
    @Log("审核志愿服务记录")
    @PostMapping("/{id}/audit")
    @PreAuthorize("hasAnyRole('CLUB_LEADER', 'UNION_ADMIN', 'ADMIN')")
    public Result<?> auditRecord(
            @PathVariable Integer id,
            @Valid @RequestBody VolunteerDTOs.AuditRequest request) {
        return volunteerService.auditRecord(id, request);
    }

    @Operation(summary = "获取所有志愿服务记录（管理员）")
    @GetMapping("/records")
    @PreAuthorize("hasAnyRole('CLUB_LEADER', 'UNION_ADMIN', 'ADMIN')")
    public Result<?> getAllRecords(@ModelAttribute VolunteerDTOs.QueryRequest request) {
        return volunteerService.getAllRecords(request);
    }

    @Operation(summary = "获取志愿服务统计汇总（管理员）")
    @GetMapping("/stats/summary")
    @PreAuthorize("hasAnyRole('CLUB_LEADER', 'UNION_ADMIN', 'ADMIN')")
    public Result<?> getStatsSummary(@ModelAttribute VolunteerDTOs.QueryRequest request) {
        return volunteerService.getStatsSummary(request);
    }

    @Operation(summary = "获取当前用户志愿服务统计")
    @GetMapping("/stats/my")
    public Result<?> getMyStats() {
        return volunteerService.getUserStats(null);
    }

    @Operation(summary = "获取指定用户志愿服务统计（管理员）")
    @GetMapping("/stats/user/{userId}")
    @PreAuthorize("hasAnyRole('UNION_ADMIN', 'ADMIN')")
    public Result<?> getUserStats(@PathVariable Integer userId) {
        return volunteerService.getUserStats(userId);
    }

    @Operation(summary = "获取全校志愿服务统计排行（管理员）")
    @GetMapping("/stats/ranking")
    @PreAuthorize("hasAnyRole('UNION_ADMIN', 'ADMIN')")
    public Result<?> getClubStats() {
        return volunteerService.getClubStats();
    }

    @Operation(summary = "导出志愿服务记录Excel")
    @Log("导出志愿服务记录")
    @GetMapping("/export/records")
    @PreAuthorize("hasAnyRole('CLUB_LEADER', 'UNION_ADMIN', 'ADMIN')")
    public void exportRecords(
            @ModelAttribute VolunteerDTOs.QueryRequest request,
            HttpServletResponse response) throws IOException {
        volunteerService.exportRecords(request, response);
    }

    @Operation(summary = "导出志愿服务统计Excel")
    @Log("导出志愿服务统计")
    @GetMapping("/export/stats")
    @PreAuthorize("hasAnyRole('UNION_ADMIN', 'ADMIN')")
    public void exportStats(HttpServletResponse response) throws IOException {
        volunteerService.exportStats(response);
    }
}
