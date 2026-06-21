package com.club.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.club.common.Result;
import com.club.common.RoleConstants;
import com.club.dto.MentorshipDTOs;
import com.club.entity.ClubMentor;
import com.club.entity.MentorSlot;
import com.club.entity.User;
import com.club.mapper.ClubMentorMapper;
import com.club.mapper.MentorSlotMapper;
import com.club.mapper.UserMapper;
import com.club.service.MentorSlotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MentorSlotServiceImpl extends ServiceImpl<MentorSlotMapper, MentorSlot> implements MentorSlotService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ClubMentorMapper clubMentorMapper;

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getName() != null) {
            return userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, auth.getName()));
        }
        return null;
    }

    private MentorshipDTOs.SlotVO toVO(MentorSlot slot) {
        MentorshipDTOs.SlotVO vo = new MentorshipDTOs.SlotVO();
        vo.setId(slot.getId());
        vo.setMentorId(slot.getMentorId());
        vo.setStartTime(slot.getStartTime());
        vo.setEndTime(slot.getEndTime());
        vo.setStatus(slot.getStatus());
        vo.setCreateTime(slot.getCreateTime());

        ClubMentor mentor = clubMentorMapper.selectById(slot.getMentorId());
        if (mentor != null) {
            vo.setMentorName(mentor.getName());
        }

        return vo;
    }

    private boolean hasOverlap(Integer mentorId, LocalDateTime startTime, LocalDateTime endTime, Integer excludeSlotId) {
        LambdaQueryWrapper<MentorSlot> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MentorSlot::getMentorId, mentorId)
               .ne(MentorSlot::getStatus, "CANCELLED")
               .and(w -> w.lt(MentorSlot::getStartTime, endTime)
                          .gt(MentorSlot::getEndTime, startTime));

        if (excludeSlotId != null) {
            wrapper.ne(MentorSlot::getId, excludeSlotId);
        }

        return this.count(wrapper) > 0;
    }

    @Override
    @Transactional
    public Result<?> createSlot(MentorshipDTOs.SlotCreateRequest request) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return Result.error("尚未登录");
        }

        ClubMentor mentor = clubMentorMapper.selectById(request.getMentorId());
        if (mentor == null) {
            return Result.error("导师不存在");
        }

        boolean isAdmin = RoleConstants.ADMIN.equals(currentUser.getRole())
                || RoleConstants.UNION_ADMIN.equals(currentUser.getRole());
        boolean isLeader = RoleConstants.CLUB_LEADER.equals(currentUser.getRole())
                && currentUser.getClubId() != null
                && currentUser.getClubId().equals(mentor.getClubId());
        boolean isMentorUser = mentor.getUserId() != null
                && mentor.getUserId().equals(currentUser.getId());

        if (!isAdmin && !isLeader && !isMentorUser) {
            return Result.error("仅社团负责人或导师本人可创建时段");
        }

        if (request.getEndTime().isBefore(request.getStartTime())) {
            return Result.error("结束时间不能早于开始时间");
        }

        if (request.getStartTime().isBefore(LocalDateTime.now())) {
            return Result.error("时段开始时间不能早于当前时间");
        }

        if (hasOverlap(request.getMentorId(), request.getStartTime(), request.getEndTime(), null)) {
            return Result.error("该导师此时段已有其他安排，存在时间冲突");
        }

        MentorSlot slot = new MentorSlot();
        slot.setMentorId(request.getMentorId());
        slot.setStartTime(request.getStartTime());
        slot.setEndTime(request.getEndTime());
        slot.setStatus("AVAILABLE");

        boolean saved = this.save(slot);
        if (!saved) {
            return Result.error("时段创建失败");
        }
        return Result.success(null);
    }

    @Override
    @Transactional
    public Result<?> cancelSlot(Integer id) {
        MentorSlot slot = this.getById(id);
        if (slot == null) {
            return Result.error("时段不存在");
        }

        if ("BOOKED".equals(slot.getStatus())) {
            return Result.error("已被预约的时段不可直接取消，请先处理预约");
        }

        if ("CANCELLED".equals(slot.getStatus())) {
            return Result.error("该时段已取消");
        }

        slot.setStatus("CANCELLED");
        boolean updated = this.updateById(slot);
        if (!updated) {
            return Result.error("取消失败");
        }
        return Result.success(null);
    }

    @Override
    public Result<?> listSlotsByMentor(Integer mentorId) {
        LambdaQueryWrapper<MentorSlot> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MentorSlot::getMentorId, mentorId)
               .orderByAsc(MentorSlot::getStartTime);

        List<MentorSlot> slots = this.list(wrapper);
        List<MentorshipDTOs.SlotVO> voList = slots.stream()
                .map(this::toVO)
                .collect(Collectors.toList());

        return Result.success(voList);
    }

    @Override
    public Result<?> listAvailableSlots(Integer clubId) {
        LambdaQueryWrapper<MentorSlot> slotWrapper = new LambdaQueryWrapper<>();
        slotWrapper.eq(MentorSlot::getStatus, "AVAILABLE");

        if (clubId != null) {
            LambdaQueryWrapper<ClubMentor> mentorWrapper = new LambdaQueryWrapper<>();
            mentorWrapper.eq(ClubMentor::getClubId, clubId);
            List<ClubMentor> mentors = clubMentorMapper.selectList(mentorWrapper);
            List<Integer> mentorIds = mentors.stream()
                    .map(ClubMentor::getId)
                    .collect(Collectors.toList());
            if (mentorIds.isEmpty()) {
                return Result.success(List.of());
            }
            slotWrapper.in(MentorSlot::getMentorId, mentorIds);
        }

        slotWrapper.orderByAsc(MentorSlot::getStartTime);

        List<MentorSlot> slots = this.list(slotWrapper);
        List<MentorshipDTOs.SlotVO> voList = slots.stream()
                .map(this::toVO)
                .collect(Collectors.toList());

        return Result.success(voList);
    }
}
