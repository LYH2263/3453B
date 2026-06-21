package com.club.controller;

import com.club.common.Result;
import com.club.dto.VenueDTOs;
import com.club.entity.Venue;
import com.club.service.VenueBookingService;
import com.club.service.VenueService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/venues")
public class VenueController {

    @Autowired
    private VenueService venueService;

    @Autowired
    private VenueBookingService venueBookingService;

    @GetMapping
    public Result<?> listVenues() {
        return venueService.listVenues();
    }

    @GetMapping("/{id}")
    public Result<?> getVenueById(@PathVariable Integer id) {
        return venueService.getVenueById(id);
    }

    @PostMapping
    public Result<?> createVenue(@Valid @RequestBody Venue venue) {
        return venueService.createVenue(venue);
    }

    @PutMapping
    public Result<?> updateVenue(@Valid @RequestBody Venue venue) {
        return venueService.updateVenue(venue);
    }

    @DeleteMapping("/{id}")
    public Result<?> deleteVenue(@PathVariable Integer id) {
        return venueService.deleteVenue(id);
    }

    @PostMapping("/bookings")
    public Result<?> submitBooking(@Valid @RequestBody VenueDTOs.BookingSubmitRequest request) {
        return venueBookingService.submitBooking(request);
    }

    @PostMapping("/bookings/{id}/audit")
    public Result<?> auditBooking(@PathVariable Integer id, @Valid @RequestBody VenueDTOs.BookingAuditRequest request) {
        return venueBookingService.auditBooking(id, request);
    }

    @GetMapping("/bookings/my")
    public Result<?> getMyBookings() {
        return venueBookingService.getMyBookings();
    }

    @GetMapping("/bookings/pending")
    public Result<?> getPendingBookings() {
        return venueBookingService.getPendingBookings();
    }

    @GetMapping("/bookings/all")
    public Result<?> getAllBookings() {
        return venueBookingService.getAllBookings();
    }

    @GetMapping("/bookings/weekly")
    public Result<?> getWeeklyBookings(
            @RequestParam LocalDateTime weekStart,
            @RequestParam LocalDateTime weekEnd,
            @RequestParam(required = false) Integer venueId) {
        VenueDTOs.WeeklyQueryRequest request = new VenueDTOs.WeeklyQueryRequest();
        request.setWeekStart(weekStart);
        request.setWeekEnd(weekEnd);
        request.setVenueId(venueId);
        return venueBookingService.getWeeklyBookings(request);
    }

    @GetMapping("/bookings/check-overlap")
    public Result<?> checkOverlap(
            @RequestParam Integer venueId,
            @RequestParam LocalDateTime startTime,
            @RequestParam LocalDateTime endTime,
            @RequestParam(required = false) Integer excludeBookingId) {
        return venueBookingService.checkOverlap(venueId, startTime, endTime, excludeBookingId);
    }
}
