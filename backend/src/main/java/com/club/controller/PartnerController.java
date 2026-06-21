package com.club.controller;

import com.club.common.Result;
import com.club.common.annotation.Log;
import com.club.dto.PartnerDTOs;
import com.club.service.PartnerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Tag(name = "合作伙伴台账", description = "合作伙伴信息的增删改查、筛选与导出")
@RestController
@RequestMapping("/api/partners")
public class PartnerController {

    @Autowired
    private PartnerService partnerService;

    @Operation(summary = "新增合作伙伴")
    @Log("新增合作伙伴")
    @PostMapping
    @PreAuthorize("hasAnyRole('CLUB_LEADER', 'UNION_ADMIN', 'ADMIN')")
    public Result<?> createPartner(@Valid @RequestBody PartnerDTOs.CreateRequest request) {
        return partnerService.createPartner(request);
    }

    @Operation(summary = "修改合作伙伴")
    @Log("修改合作伙伴")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLUB_LEADER', 'UNION_ADMIN', 'ADMIN')")
    public Result<?> updatePartner(
            @PathVariable Integer id,
            @Valid @RequestBody PartnerDTOs.UpdateRequest request) {
        return partnerService.updatePartner(id, request);
    }

    @Operation(summary = "删除合作伙伴")
    @Log("删除合作伙伴")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLUB_LEADER', 'UNION_ADMIN', 'ADMIN')")
    public Result<?> deletePartner(@PathVariable Integer id) {
        return partnerService.deletePartner(id);
    }

    @Operation(summary = "获取合作伙伴详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLUB_LEADER', 'UNION_ADMIN', 'ADMIN')")
    public Result<?> getPartnerById(@PathVariable Integer id) {
        return partnerService.getPartnerById(id);
    }

    @Operation(summary = "获取合作伙伴列表（负责人看本社团，管理员看全校）")
    @GetMapping
    @PreAuthorize("hasAnyRole('CLUB_LEADER', 'UNION_ADMIN', 'ADMIN')")
    public Result<?> listPartners(@ModelAttribute PartnerDTOs.QueryRequest request) {
        return partnerService.listPartners(request);
    }

    @Operation(summary = "获取合作伙伴统计概览")
    @GetMapping("/stats/summary")
    @PreAuthorize("hasAnyRole('CLUB_LEADER', 'UNION_ADMIN', 'ADMIN')")
    public Result<?> getPartnerStats() {
        return partnerService.getPartnerStats();
    }

    @Operation(summary = "导出合作伙伴Excel")
    @Log("导出合作伙伴台账")
    @GetMapping("/export")
    @PreAuthorize("hasAnyRole('CLUB_LEADER', 'UNION_ADMIN', 'ADMIN')")
    public void exportPartners(
            @ModelAttribute PartnerDTOs.QueryRequest request,
            HttpServletResponse response) throws IOException {
        partnerService.exportPartners(request, response);
    }
}
