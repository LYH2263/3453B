package com.club.service;

import com.club.common.Result;
import com.club.entity.ActivityLeaveRequest;
import java.util.List;

public interface LeaveRequestService {
    Result<?> submitLeave(ActivityLeaveRequest leaveRequest);
    Result<?> approveLeave(Integer id, Integer approverId);
    Result<?> rejectLeave(Integer id, Integer approverId, String rejectReason);
    Result<List<ActivityLeaveRequest>> getMyLeaveRequests(Integer userId);
    Result<List<ActivityLeaveRequest>> getPendingRequests(Integer clubId);
    Result<List<ActivityLeaveRequest>> getAllRequests(Integer clubId);
    boolean isUserExempted(Integer activityId, Integer userId);
}
