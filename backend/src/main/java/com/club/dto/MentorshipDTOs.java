package com.club.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

public class MentorshipDTOs {

    public static class MentorCreateRequest {
        @NotNull(message = "社团不能为空")
        private Integer clubId;
        @NotBlank(message = "导师姓名不能为空")
        private String name;
        private String staffNo;
        private String researchArea;
        private String intro;
        private Integer userId;

        public Integer getClubId() { return clubId; }
        public void setClubId(Integer clubId) { this.clubId = clubId; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getStaffNo() { return staffNo; }
        public void setStaffNo(String staffNo) { this.staffNo = staffNo; }

        public String getResearchArea() { return researchArea; }
        public void setResearchArea(String researchArea) { this.researchArea = researchArea; }

        public String getIntro() { return intro; }
        public void setIntro(String intro) { this.intro = intro; }

        public Integer getUserId() { return userId; }
        public void setUserId(Integer userId) { this.userId = userId; }
    }

    public static class SlotCreateRequest {
        @NotNull(message = "导师不能为空")
        private Integer mentorId;
        @NotNull(message = "开始时间不能为空")
        private LocalDateTime startTime;
        @NotNull(message = "结束时间不能为空")
        private LocalDateTime endTime;

        public Integer getMentorId() { return mentorId; }
        public void setMentorId(Integer mentorId) { this.mentorId = mentorId; }

        public LocalDateTime getStartTime() { return startTime; }
        public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

        public LocalDateTime getEndTime() { return endTime; }
        public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    }

    public static class AppointmentCreateRequest {
        @NotNull(message = "时段不能为空")
        private Integer slotId;

        public Integer getSlotId() { return slotId; }
        public void setSlotId(Integer slotId) { this.slotId = slotId; }
    }

    public static class AppointmentActionRequest {
        @NotBlank(message = "状态不能为空")
        @Pattern(regexp = "^(CONFIRMED|REJECTED)$", message = "状态只能是CONFIRMED或REJECTED")
        private String status;
        private String rejectReason;

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getRejectReason() { return rejectReason; }
        public void setRejectReason(String rejectReason) { this.rejectReason = rejectReason; }
    }

    public static class MentorVO {
        private Integer id;
        private Integer clubId;
        private String clubName;
        private String name;
        private String staffNo;
        private String researchArea;
        private String intro;
        private Integer userId;
        private String linkedUsername;
        private LocalDateTime createTime;

        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }

        public Integer getClubId() { return clubId; }
        public void setClubId(Integer clubId) { this.clubId = clubId; }

        public String getClubName() { return clubName; }
        public void setClubName(String clubName) { this.clubName = clubName; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getStaffNo() { return staffNo; }
        public void setStaffNo(String staffNo) { this.staffNo = staffNo; }

        public String getResearchArea() { return researchArea; }
        public void setResearchArea(String researchArea) { this.researchArea = researchArea; }

        public String getIntro() { return intro; }
        public void setIntro(String intro) { this.intro = intro; }

        public Integer getUserId() { return userId; }
        public void setUserId(Integer userId) { this.userId = userId; }

        public String getLinkedUsername() { return linkedUsername; }
        public void setLinkedUsername(String linkedUsername) { this.linkedUsername = linkedUsername; }

        public LocalDateTime getCreateTime() { return createTime; }
        public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    }

    public static class SlotVO {
        private Integer id;
        private Integer mentorId;
        private String mentorName;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private String status;
        private LocalDateTime createTime;

        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }

        public Integer getMentorId() { return mentorId; }
        public void setMentorId(Integer mentorId) { this.mentorId = mentorId; }

        public String getMentorName() { return mentorName; }
        public void setMentorName(String mentorName) { this.mentorName = mentorName; }

        public LocalDateTime getStartTime() { return startTime; }
        public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

        public LocalDateTime getEndTime() { return endTime; }
        public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public LocalDateTime getCreateTime() { return createTime; }
        public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    }

    public static class AppointmentVO {
        private Integer id;
        private Integer slotId;
        private Integer studentId;
        private String studentName;
        private Integer mentorId;
        private String mentorName;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private String status;
        private String rejectReason;
        private LocalDateTime createTime;

        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }

        public Integer getSlotId() { return slotId; }
        public void setSlotId(Integer slotId) { this.slotId = slotId; }

        public Integer getStudentId() { return studentId; }
        public void setStudentId(Integer studentId) { this.studentId = studentId; }

        public String getStudentName() { return studentName; }
        public void setStudentName(String studentName) { this.studentName = studentName; }

        public Integer getMentorId() { return mentorId; }
        public void setMentorId(Integer mentorId) { this.mentorId = mentorId; }

        public String getMentorName() { return mentorName; }
        public void setMentorName(String mentorName) { this.mentorName = mentorName; }

        public LocalDateTime getStartTime() { return startTime; }
        public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

        public LocalDateTime getEndTime() { return endTime; }
        public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getRejectReason() { return rejectReason; }
        public void setRejectReason(String rejectReason) { this.rejectReason = rejectReason; }

        public LocalDateTime getCreateTime() { return createTime; }
        public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    }
}
