package com.club.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.club.common.Result;
import com.club.dto.MentorshipDTOs;
import com.club.entity.MentorAppointment;

public interface MentorAppointmentService extends IService<MentorAppointment> {
    Result<?> createAppointment(MentorshipDTOs.AppointmentCreateRequest request);
    Result<?> handleAppointment(Integer id, MentorshipDTOs.AppointmentActionRequest request);
    Result<?> cancelAppointment(Integer id);
    Result<?> getMyAppointments();
    Result<?> getPendingByMentor(Integer mentorId);
}
