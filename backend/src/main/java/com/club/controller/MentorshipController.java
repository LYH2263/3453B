package com.club.controller;

import com.club.common.Result;
import com.club.dto.MentorshipDTOs;
import com.club.entity.ClubMentor;
import com.club.service.ClubMentorService;
import com.club.service.MentorAppointmentService;
import com.club.service.MentorSlotService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mentorship")
public class MentorshipController {

    @Autowired
    private ClubMentorService clubMentorService;

    @Autowired
    private MentorSlotService mentorSlotService;

    @Autowired
    private MentorAppointmentService mentorAppointmentService;

    @GetMapping("/mentors")
    public Result<?> listMentors(@RequestParam(required = false) Integer clubId) {
        return clubMentorService.listMentors(clubId);
    }

    @GetMapping("/mentors/{id}")
    public Result<?> getMentorById(@PathVariable Integer id) {
        return clubMentorService.getMentorById(id);
    }

    @PostMapping("/mentors")
    public Result<?> createMentor(@Valid @RequestBody ClubMentor mentor) {
        return clubMentorService.createMentor(mentor);
    }

    @PutMapping("/mentors")
    public Result<?> updateMentor(@Valid @RequestBody ClubMentor mentor) {
        return clubMentorService.updateMentor(mentor);
    }

    @DeleteMapping("/mentors/{id}")
    public Result<?> deleteMentor(@PathVariable Integer id) {
        return clubMentorService.deleteMentor(id);
    }

    @GetMapping("/slots")
    public Result<?> listSlotsByMentor(@RequestParam Integer mentorId) {
        return mentorSlotService.listSlotsByMentor(mentorId);
    }

    @GetMapping("/slots/available")
    public Result<?> listAvailableSlots(@RequestParam(required = false) Integer clubId) {
        return mentorSlotService.listAvailableSlots(clubId);
    }

    @PostMapping("/slots")
    public Result<?> createSlot(@Valid @RequestBody MentorshipDTOs.SlotCreateRequest request) {
        return mentorSlotService.createSlot(request);
    }

    @PostMapping("/slots/{id}/cancel")
    public Result<?> cancelSlot(@PathVariable Integer id) {
        return mentorSlotService.cancelSlot(id);
    }

    @PostMapping("/appointments")
    public Result<?> createAppointment(@Valid @RequestBody MentorshipDTOs.AppointmentCreateRequest request) {
        return mentorAppointmentService.createAppointment(request);
    }

    @PostMapping("/appointments/{id}/handle")
    public Result<?> handleAppointment(@PathVariable Integer id, @Valid @RequestBody MentorshipDTOs.AppointmentActionRequest request) {
        return mentorAppointmentService.handleAppointment(id, request);
    }

    @PostMapping("/appointments/{id}/cancel")
    public Result<?> cancelAppointment(@PathVariable Integer id) {
        return mentorAppointmentService.cancelAppointment(id);
    }

    @GetMapping("/appointments/my")
    public Result<?> getMyAppointments() {
        return mentorAppointmentService.getMyAppointments();
    }

    @GetMapping("/appointments/pending")
    public Result<?> getPendingByMentor(@RequestParam Integer mentorId) {
        return mentorAppointmentService.getPendingByMentor(mentorId);
    }
}
