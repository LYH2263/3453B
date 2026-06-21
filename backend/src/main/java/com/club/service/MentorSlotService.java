package com.club.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.club.common.Result;
import com.club.dto.MentorshipDTOs;
import com.club.entity.MentorSlot;

public interface MentorSlotService extends IService<MentorSlot> {
    Result<?> createSlot(MentorshipDTOs.SlotCreateRequest request);
    Result<?> cancelSlot(Integer id);
    Result<?> listSlotsByMentor(Integer mentorId);
    Result<?> listAvailableSlots(Integer clubId);
}
