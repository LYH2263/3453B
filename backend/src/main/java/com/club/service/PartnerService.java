package com.club.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.club.common.Result;
import com.club.dto.PartnerDTOs;
import com.club.entity.ClubPartner;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public interface PartnerService extends IService<ClubPartner> {
    Result<?> createPartner(PartnerDTOs.CreateRequest request);
    Result<?> updatePartner(Integer id, PartnerDTOs.UpdateRequest request);
    Result<?> deletePartner(Integer id);
    Result<?> getPartnerById(Integer id);
    Result<?> listPartners(PartnerDTOs.QueryRequest request);
    Result<?> getPartnerStats();
    void exportPartners(PartnerDTOs.QueryRequest request, HttpServletResponse response) throws IOException;
}
