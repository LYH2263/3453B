package com.club.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.club.common.Result;
import com.club.entity.Activity;
import com.club.entity.ActivityLeaveRequest;
import com.club.entity.ActivityRegistration;
import com.club.mapper.ActivityMapper;
import com.club.mapper.LeaveRequestMapper;
import com.club.mapper.RegistrationMapper;
import com.club.service.LeaveRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LeaveRequestServiceImpl implements LeaveRequestService {

    @Autowired
    private LeaveRequestMapper leaveRequestMapper;

    @Autowired
    private ActivityMapper activityMapper;

    @Autowired
    private RegistrationMapper registrationMapper;

    @Override
    public Result<?> submitLeave(ActivityLeaveRequest leaveRequest) {
        Activity activity = activityMapper.selectById(leaveRequest.getActivityId());
        if (activity == null) {
            return Result.error("活动不存在");
        }
        if (!Boolean.TRUE.equals(activity.getIsMandatory())) {
            return Result.error("该活动非强制参加，无需请假");
        }

        long existingCount = leaveRequestMapper.selectCount(new LambdaQueryWrapper<ActivityLeaveRequest>()
                .eq(ActivityLeaveRequest::getActivityId, leaveRequest.getActivityId())
                .eq(ActivityLeaveRequest::getUserId, leaveRequest.getUserId()));
        if (existingCount > 0) {
            return Result.error("您已对该活动提交过请假申请");
        }

        leaveRequest.setStatus("PENDING");
        leaveRequest.setClubId(activity.getClubId());
        leaveRequestMapper.insert(leaveRequest);
        return Result.success(null);
    }

    @Override
    @Transactional
    public Result<?> approveLeave(Integer id, Integer approverId) {
        ActivityLeaveRequest leave = leaveRequestMapper.selectById(id);
        if (leave == null) {
            return Result.error("请假申请不存在");
        }
        if (!"PENDING".equals(leave.getStatus())) {
            return Result.error("该申请已处理");
        }

        leave.setStatus("APPROVED");
        leave.setApproverId(approverId);
        leaveRequestMapper.updateById(leave);

        ActivityRegistration existingReg = registrationMapper.selectOne(
                new LambdaQueryWrapper<ActivityRegistration>()
                        .eq(ActivityRegistration::getActivityId, leave.getActivityId())
                        .eq(ActivityRegistration::getUserId, leave.getUserId()));
        if (existingReg != null) {
            existingReg.setStatus("EXEMPTED");
            registrationMapper.updateById(existingReg);
        } else {
            ActivityRegistration newReg = new ActivityRegistration();
            newReg.setActivityId(leave.getActivityId());
            newReg.setUserId(leave.getUserId());
            newReg.setStatus("EXEMPTED");
            registrationMapper.insert(newReg);
        }

        return Result.success(null);
    }

    @Override
    public Result<?> rejectLeave(Integer id, Integer approverId, String rejectReason) {
        ActivityLeaveRequest leave = leaveRequestMapper.selectById(id);
        if (leave == null) {
            return Result.error("请假申请不存在");
        }
        if (!"PENDING".equals(leave.getStatus())) {
            return Result.error("该申请已处理");
        }
        if (rejectReason == null || rejectReason.trim().isEmpty()) {
            return Result.error("拒绝请假须填写原因");
        }

        leave.setStatus("REJECTED");
        leave.setApproverId(approverId);
        leave.setRejectReason(rejectReason);
        leaveRequestMapper.updateById(leave);
        return Result.success(null);
    }

    @Override
    public Result<List<ActivityLeaveRequest>> getMyLeaveRequests(Integer userId) {
        return Result.success(leaveRequestMapper.selectByUserId(userId));
    }

    @Override
    public Result<List<ActivityLeaveRequest>> getPendingRequests(Integer clubId) {
        return Result.success(leaveRequestMapper.selectPendingByClubId(clubId));
    }

    @Override
    public Result<List<ActivityLeaveRequest>> getAllRequests(Integer clubId) {
        return Result.success(leaveRequestMapper.selectByClubId(clubId));
    }

    @Override
    public boolean isUserExempted(Integer activityId, Integer userId) {
        long count = leaveRequestMapper.selectCount(new LambdaQueryWrapper<ActivityLeaveRequest>()
                .eq(ActivityLeaveRequest::getActivityId, activityId)
                .eq(ActivityLeaveRequest::getUserId, userId)
                .eq(ActivityLeaveRequest::getStatus, "APPROVED"));
        return count > 0;
    }
}
