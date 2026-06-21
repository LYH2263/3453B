package com.club.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.club.common.Result;
import com.club.dto.VenueDTOs;
import com.club.entity.VenueBooking;

public interface VenueBookingService extends IService<VenueBooking> {
    Result<?> submitBooking(VenueDTOs.BookingSubmitRequest request);
    Result<?> auditBooking(Integer id, VenueDTOs.BookingAuditRequest request);
    Result<?> getMyBookings();
    Result<?> getPendingBookings();
    Result<?> getAllBookings();
    Result<?> getWeeklyBookings(VenueDTOs.WeeklyQueryRequest request);
    Result<?> checkOverlap(Integer venueId, java.time.LocalDateTime startTime, java.time.LocalDateTime endTime, Integer excludeBookingId);
}
