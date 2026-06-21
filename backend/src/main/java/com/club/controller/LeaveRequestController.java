package com.club.controller;

import com.club.common.Result;
import com.club.entity.ActivityLeaveRequest;
import com.club.entity.User;
import com.club.mapper.UserMapper;
import com.club.service.LeaveRequestService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/leave-requests")
public class LeaveRequestController {

    @Autowired
    private LeaveRequestService leaveRequestService;

    @Autowired
    private UserMapper userMapper;

    @PostMapping
    public Result<?> submitLeave(@RequestBody ActivityLeaveRequest leaveRequest) {
        User currentUser = getCurrentUser();
        if (currentUser == null) return Result.error("未登录");
        leaveRequest.setUserId(currentUser.getId());
        return leaveRequestService.submitLeave(leaveRequest);
    }

    @GetMapping("/my")
    public Result<?> getMyLeaveRequests() {
        User currentUser = getCurrentUser();
        if (currentUser == null) return Result.error("未登录");
        return leaveRequestService.getMyLeaveRequests(currentUser.getId());
    }

    @GetMapping("/pending")
    public Result<?> getPendingRequests(@RequestParam Integer clubId) {
        return leaveRequestService.getPendingRequests(clubId);
    }

    @GetMapping("/all")
    public Result<?> getAllRequests(@RequestParam Integer clubId) {
        return leaveRequestService.getAllRequests(clubId);
    }

    @PostMapping("/{id}/approve")
    public Result<?> approveLeave(@PathVariable Integer id) {
        User currentUser = getCurrentUser();
        if (currentUser == null) return Result.error("未登录");
        return leaveRequestService.approveLeave(id, currentUser.getId());
    }

    @PostMapping("/{id}/reject")
    public Result<?> rejectLeave(@PathVariable Integer id, @RequestBody Map<String, String> params) {
        User currentUser = getCurrentUser();
        if (currentUser == null) return Result.error("未登录");
        return leaveRequestService.rejectLeave(id, currentUser.getId(), params.get("rejectReason"));
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) return null;
        return userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, auth.getName()));
    }
}
