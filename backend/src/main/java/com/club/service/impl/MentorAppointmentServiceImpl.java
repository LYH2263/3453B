package com.club.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.club.common.Result;
import com.club.common.RoleConstants;
import com.club.dto.MentorshipDTOs;
import com.club.entity.ClubMentor;
import com.club.entity.MentorAppointment;
import com.club.entity.MentorSlot;
import com.club.entity.User;
import com.club.mapper.ClubMentorMapper;
import com.club.mapper.MentorAppointmentMapper;
import com.club.mapper.MentorSlotMapper;
import com.club.mapper.UserMapper;
import com.club.service.MentorAppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MentorAppointmentServiceImpl extends ServiceImpl<MentorAppointmentMapper, MentorAppointment> implements MentorAppointmentService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private MentorSlotMapper mentorSlotMapper;
    @Autowired
    private ClubMentorMapper clubMentorMapper;

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getName() != null) {
            return userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, auth.getName()));
        }
        return null;
    }

    private MentorshipDTOs.AppointmentVO toVO(MentorAppointment appointment) {
        MentorshipDTOs.AppointmentVO vo = new MentorshipDTOs.AppointmentVO();
        vo.setId(appointment.getId());
        vo.setSlotId(appointment.getSlotId());
        vo.setStudentId(appointment.getStudentId());
        vo.setStatus(appointment.getStatus());
        vo.setRejectReason(appointment.getRejectReason());
        vo.setCreateTime(appointment.getCreateTime());

        User student = userMapper.selectById(appointment.getStudentId());
        if (student != null) {
            vo.setStudentName(student.getRealName());
        }

        MentorSlot slot = mentorSlotMapper.selectById(appointment.getSlotId());
        if (slot != null) {
            vo.setMentorId(slot.getMentorId());
            vo.setStartTime(slot.getStartTime());
            vo.setEndTime(slot.getEndTime());

            ClubMentor mentor = clubMentorMapper.selectById(slot.getMentorId());
            if (mentor != null) {
                vo.setMentorName(mentor.getName());
            }
        }

        return vo;
    }

    @Override
    @Transactional
    public Result<?> createAppointment(MentorshipDTOs.AppointmentCreateRequest request) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return Result.error("尚未登录");
        }

        MentorSlot slot = mentorSlotMapper.selectById(request.getSlotId());
        if (slot == null) {
            return Result.error("时段不存在");
        }

        if (!"AVAILABLE".equals(slot.getStatus())) {
            return Result.error("该时段不可预约");
        }

        MentorAppointment existingAppointment = this.getOne(
                new LambdaQueryWrapper<MentorAppointment>()
                        .eq(MentorAppointment::getSlotId, request.getSlotId()));
        if (existingAppointment != null) {
            return Result.error("该时段已被预约");
        }

        MentorAppointment appointment = new MentorAppointment();
        appointment.setSlotId(request.getSlotId());
        appointment.setStudentId(currentUser.getId());
        appointment.setStatus("PENDING");

        boolean saved = this.save(appointment);
        if (!saved) {
            return Result.error("预约提交失败");
        }

        slot.setStatus("BOOKED");
        mentorSlotMapper.updateById(slot);

        return Result.success(null);
    }

    @Override
    @Transactional
    public Result<?> handleAppointment(Integer id, MentorshipDTOs.AppointmentActionRequest request) {
        MentorAppointment appointment = this.getById(id);
        if (appointment == null) {
            return Result.error("预约记录不存在");
        }

        if (!"PENDING".equals(appointment.getStatus())) {
            return Result.error("该预约已处理，无法重复操作");
        }

        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return Result.error("尚未登录");
        }

        MentorSlot slot = mentorSlotMapper.selectById(appointment.getSlotId());
        if (slot == null) {
            return Result.error("关联时段不存在");
        }

        ClubMentor mentor = clubMentorMapper.selectById(slot.getMentorId());
        if (mentor == null) {
            return Result.error("关联导师不存在");
        }

        boolean isAdmin = RoleConstants.ADMIN.equals(currentUser.getRole())
                || RoleConstants.UNION_ADMIN.equals(currentUser.getRole());
        boolean isLeader = RoleConstants.CLUB_LEADER.equals(currentUser.getRole())
                && currentUser.getClubId() != null
                && currentUser.getClubId().equals(mentor.getClubId());
        boolean isMentorUser = mentor.getUserId() != null
                && mentor.getUserId().equals(currentUser.getId());

        if (!isAdmin && !isLeader && !isMentorUser) {
            return Result.error("仅导师本人或社团负责人可处理预约");
        }

        if ("CONFIRMED".equals(request.getStatus())) {
            appointment.setStatus("CONFIRMED");
        } else if ("REJECTED".equals(request.getStatus())) {
            if (request.getRejectReason() == null || request.getRejectReason().trim().isEmpty()) {
                return Result.error("拒绝预约时必须填写原因");
            }
            appointment.setStatus("REJECTED");
            appointment.setRejectReason(request.getRejectReason());

            slot.setStatus("AVAILABLE");
            mentorSlotMapper.updateById(slot);
        }

        boolean updated = this.updateById(appointment);
        if (!updated) {
            return Result.error("处理失败");
        }
        return Result.success(null);
    }

    @Override
    @Transactional
    public Result<?> cancelAppointment(Integer id) {
        MentorAppointment appointment = this.getById(id);
        if (appointment == null) {
            return Result.error("预约记录不存在");
        }

        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return Result.error("尚未登录");
        }

        boolean isStudent = appointment.getStudentId().equals(currentUser.getId());
        if (!isStudent) {
            boolean isAdmin = RoleConstants.ADMIN.equals(currentUser.getRole())
                    || RoleConstants.UNION_ADMIN.equals(currentUser.getRole());
            if (!isAdmin) {
                return Result.error("仅预约学生或管理员可取消预约");
            }
        }

        if ("REJECTED".equals(appointment.getStatus())) {
            return Result.error("该预约已被拒绝，无需取消");
        }

        MentorSlot slot = mentorSlotMapper.selectById(appointment.getSlotId());
        if (slot != null && "BOOKED".equals(slot.getStatus())) {
            slot.setStatus("AVAILABLE");
            mentorSlotMapper.updateById(slot);
        }

        appointment.setStatus("REJECTED");
        appointment.setRejectReason("学生取消预约");
        this.updateById(appointment);

        return Result.success(null);
    }

    @Override
    public Result<?> getMyAppointments() {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return Result.error("尚未登录");
        }

        LambdaQueryWrapper<MentorAppointment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MentorAppointment::getStudentId, currentUser.getId())
               .orderByDesc(MentorAppointment::getCreateTime);

        List<MentorAppointment> appointments = this.list(wrapper);
        List<MentorshipDTOs.AppointmentVO> voList = appointments.stream()
                .map(this::toVO)
                .collect(Collectors.toList());

        return Result.success(voList);
    }

    @Override
    public Result<?> getPendingByMentor(Integer mentorId) {
        ClubMentor mentor = clubMentorMapper.selectById(mentorId);
        if (mentor == null) {
            return Result.error("导师不存在");
        }

        LambdaQueryWrapper<MentorSlot> slotWrapper = new LambdaQueryWrapper<>();
        slotWrapper.eq(MentorSlot::getMentorId, mentorId);
        List<MentorSlot> slots = mentorSlotMapper.selectList(slotWrapper);
        List<Integer> slotIds = slots.stream()
                .map(MentorSlot::getId)
                .collect(Collectors.toList());

        if (slotIds.isEmpty()) {
            return Result.success(new ArrayList<>());
        }

        LambdaQueryWrapper<MentorAppointment> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(MentorAppointment::getSlotId, slotIds)
               .eq(MentorAppointment::getStatus, "PENDING")
               .orderByAsc(MentorAppointment::getCreateTime);

        List<MentorAppointment> appointments = this.list(wrapper);
        List<MentorshipDTOs.AppointmentVO> voList = appointments.stream()
                .map(this::toVO)
                .collect(Collectors.toList());

        return Result.success(voList);
    }
}
