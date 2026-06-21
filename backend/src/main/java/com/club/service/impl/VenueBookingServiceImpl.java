package com.club.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.club.common.Result;
import com.club.common.RoleConstants;
import com.club.dto.VenueDTOs;
import com.club.entity.*;
import com.club.mapper.*;
import com.club.service.VenueBookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VenueBookingServiceImpl extends ServiceImpl<VenueBookingMapper, VenueBooking> implements VenueBookingService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private VenueMapper venueMapper;
    @Autowired
    private ClubMapper clubMapper;

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getName() != null) {
            return userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, auth.getName()));
        }
        return null;
    }

    private boolean hasOverlap(Integer venueId, LocalDateTime startTime, LocalDateTime endTime, Integer excludeBookingId) {
        LambdaQueryWrapper<VenueBooking> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VenueBooking::getVenueId, venueId)
               .eq(VenueBooking::getStatus, "APPROVED")
               .and(w -> w.lt(VenueBooking::getStartTime, endTime)
                          .gt(VenueBooking::getEndTime, startTime));
        
        if (excludeBookingId != null) {
            wrapper.ne(VenueBooking::getId, excludeBookingId);
        }
        
        return this.count(wrapper) > 0;
    }

    private VenueDTOs.VenueBookingVO toVO(VenueBooking booking) {
        VenueDTOs.VenueBookingVO vo = new VenueDTOs.VenueBookingVO();
        vo.setId(booking.getId());
        vo.setVenueId(booking.getVenueId());
        vo.setClubId(booking.getClubId());
        vo.setStartTime(booking.getStartTime());
        vo.setEndTime(booking.getEndTime());
        vo.setPurpose(booking.getPurpose());
        vo.setStatus(booking.getStatus());
        vo.setApplicantId(booking.getApplicantId());
        vo.setAuditorId(booking.getAuditorId());
        vo.setRejectReason(booking.getRejectReason());
        vo.setCreateTime(booking.getCreateTime());

        Venue venue = venueMapper.selectById(booking.getVenueId());
        if (venue != null) {
            vo.setVenueName(venue.getName());
        }

        Club club = clubMapper.selectById(booking.getClubId());
        if (club != null) {
            vo.setClubName(club.getName());
        }

        User applicant = userMapper.selectById(booking.getApplicantId());
        if (applicant != null) {
            vo.setApplicantName(applicant.getRealName());
        }

        if (booking.getAuditorId() != null) {
            User auditor = userMapper.selectById(booking.getAuditorId());
            if (auditor != null) {
                vo.setAuditorName(auditor.getRealName());
            }
        }

        return vo;
    }

    @Override
    @Transactional
    public Result<?> submitBooking(VenueDTOs.BookingSubmitRequest request) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return Result.error("尚未登录");
        }

        if (!RoleConstants.CLUB_LEADER.equals(currentUser.getRole())) {
            return Result.error("仅社团负责人可提交预约");
        }

        if (currentUser.getClubId() == null) {
            return Result.error("您尚未绑定任何社团");
        }

        if (request.getEndTime().isBefore(request.getStartTime())) {
            return Result.error("结束时间不能早于开始时间");
        }

        if (request.getStartTime().isBefore(LocalDateTime.now())) {
            return Result.error("预约时间不能早于当前时间");
        }

        Venue venue = venueMapper.selectById(request.getVenueId());
        if (venue == null) {
            return Result.error("场地不存在");
        }

        if (!"AVAILABLE".equals(venue.getStatus())) {
            return Result.error("该场地当前不可用");
        }

        if (hasOverlap(request.getVenueId(), request.getStartTime(), request.getEndTime(), null)) {
            return Result.error("该时段已被其他预约占用，请选择其他时段");
        }

        VenueBooking booking = new VenueBooking();
        booking.setVenueId(request.getVenueId());
        booking.setClubId(currentUser.getClubId());
        booking.setStartTime(request.getStartTime());
        booking.setEndTime(request.getEndTime());
        booking.setPurpose(request.getPurpose());
        booking.setStatus("PENDING");
        booking.setApplicantId(currentUser.getId());

        boolean saved = this.save(booking);
        if (!saved) {
            return Result.error("预约提交失败");
        }

        return Result.success(null);
    }

    @Override
    @Transactional
    public Result<?> auditBooking(Integer id, VenueDTOs.BookingAuditRequest request) {
        VenueBooking booking = this.getById(id);
        if (booking == null) {
            return Result.error("预约记录不存在");
        }

        if (!"PENDING".equals(booking.getStatus())) {
            return Result.error("该预约已处理，无法重复审批");
        }

        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return Result.error("尚未登录");
        }

        boolean isAdmin = RoleConstants.ADMIN.equals(currentUser.getRole());
        boolean isUnionAdmin = RoleConstants.UNION_ADMIN.equals(currentUser.getRole());
        if (!isAdmin && !isUnionAdmin) {
            return Result.error("无权限审批");
        }

        if ("APPROVED".equals(request.getStatus())) {
            if (hasOverlap(booking.getVenueId(), booking.getStartTime(), booking.getEndTime(), booking.getId())) {
                return Result.error("该时段已被其他预约占用，无法通过审批");
            }
            booking.setStatus("APPROVED");
        } else if ("REJECTED".equals(request.getStatus())) {
            if (request.getRejectReason() == null || request.getRejectReason().trim().isEmpty()) {
                return Result.error("驳回时必须填写驳回原因");
            }
            booking.setStatus("REJECTED");
            booking.setRejectReason(request.getRejectReason());
        }

        booking.setAuditorId(currentUser.getId());
        boolean updated = this.updateById(booking);
        if (!updated) {
            return Result.error("审批失败");
        }

        return Result.success(null);
    }

    @Override
    public Result<?> getMyBookings() {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return Result.error("尚未登录");
        }

        LambdaQueryWrapper<VenueBooking> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VenueBooking::getApplicantId, currentUser.getId())
               .orderByDesc(VenueBooking::getCreateTime);

        List<VenueBooking> bookings = this.list(wrapper);
        List<VenueDTOs.VenueBookingVO> voList = bookings.stream()
                .map(this::toVO)
                .collect(Collectors.toList());

        return Result.success(voList);
    }

    @Override
    public Result<?> getPendingBookings() {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return Result.error("尚未登录");
        }

        boolean isAdmin = RoleConstants.ADMIN.equals(currentUser.getRole());
        boolean isUnionAdmin = RoleConstants.UNION_ADMIN.equals(currentUser.getRole());
        if (!isAdmin && !isUnionAdmin) {
            return Result.error("无权限查看");
        }

        LambdaQueryWrapper<VenueBooking> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VenueBooking::getStatus, "PENDING")
               .orderByAsc(VenueBooking::getCreateTime);

        List<VenueBooking> bookings = this.list(wrapper);
        List<VenueDTOs.VenueBookingVO> voList = bookings.stream()
                .map(this::toVO)
                .collect(Collectors.toList());

        return Result.success(voList);
    }

    @Override
    public Result<?> getAllBookings() {
        LambdaQueryWrapper<VenueBooking> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(VenueBooking::getCreateTime);

        List<VenueBooking> bookings = this.list(wrapper);
        List<VenueDTOs.VenueBookingVO> voList = bookings.stream()
                .map(this::toVO)
                .collect(Collectors.toList());

        return Result.success(voList);
    }

    @Override
    public Result<?> getWeeklyBookings(VenueDTOs.WeeklyQueryRequest request) {
        if (request.getWeekEnd().isBefore(request.getWeekStart())) {
            return Result.error("结束日期不能早于开始日期");
        }

        LambdaQueryWrapper<VenueBooking> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VenueBooking::getStatus, "APPROVED")
               .and(w -> w.lt(VenueBooking::getStartTime, request.getWeekEnd())
                          .gt(VenueBooking::getEndTime, request.getWeekStart()));

        if (request.getVenueId() != null) {
            wrapper.eq(VenueBooking::getVenueId, request.getVenueId());
        }

        wrapper.orderByAsc(VenueBooking::getStartTime);

        List<VenueBooking> bookings = this.list(wrapper);
        List<VenueDTOs.VenueBookingVO> voList = bookings.stream()
                .map(this::toVO)
                .collect(Collectors.toList());

        return Result.success(voList);
    }

    @Override
    public Result<?> checkOverlap(Integer venueId, LocalDateTime startTime, LocalDateTime endTime, Integer excludeBookingId) {
        boolean overlap = hasOverlap(venueId, startTime, endTime, excludeBookingId);
        return Result.success(overlap);
    }
}
