package com.club.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

public class VenueDTOs {

    public static class BookingSubmitRequest {
        @NotNull(message = "场地不能为空")
        private Integer venueId;

        @NotNull(message = "开始时间不能为空")
        private LocalDateTime startTime;

        @NotNull(message = "结束时间不能为空")
        private LocalDateTime endTime;

        @NotBlank(message = "使用目的不能为空")
        private String purpose;

        public Integer getVenueId() { return venueId; }
        public void setVenueId(Integer venueId) { this.venueId = venueId; }

        public LocalDateTime getStartTime() { return startTime; }
        public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

        public LocalDateTime getEndTime() { return endTime; }
        public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

        public String getPurpose() { return purpose; }
        public void setPurpose(String purpose) { this.purpose = purpose; }
    }

    public static class BookingAuditRequest {
        @NotBlank(message = "状态不能为空")
        @Pattern(regexp = "^(APPROVED|REJECTED)$", message = "状态只能是APPROVED或REJECTED")
        private String status;

        private String rejectReason;

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getRejectReason() { return rejectReason; }
        public void setRejectReason(String rejectReason) { this.rejectReason = rejectReason; }
    }

    public static class WeeklyQueryRequest {
        @NotNull(message = "开始日期不能为空")
        private LocalDateTime weekStart;

        @NotNull(message = "结束日期不能为空")
        private LocalDateTime weekEnd;

        private Integer venueId;

        public LocalDateTime getWeekStart() { return weekStart; }
        public void setWeekStart(LocalDateTime weekStart) { this.weekStart = weekStart; }

        public LocalDateTime getWeekEnd() { return weekEnd; }
        public void setWeekEnd(LocalDateTime weekEnd) { this.weekEnd = weekEnd; }

        public Integer getVenueId() { return venueId; }
        public void setVenueId(Integer venueId) { this.venueId = venueId; }
    }

    public static class VenueBookingVO {
        private Integer id;
        private Integer venueId;
        private String venueName;
        private Integer clubId;
        private String clubName;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private String purpose;
        private String status;
        private Integer applicantId;
        private String applicantName;
        private Integer auditorId;
        private String auditorName;
        private String rejectReason;
        private LocalDateTime createTime;

        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }

        public Integer getVenueId() { return venueId; }
        public void setVenueId(Integer venueId) { this.venueId = venueId; }

        public String getVenueName() { return venueName; }
        public void setVenueName(String venueName) { this.venueName = venueName; }

        public Integer getClubId() { return clubId; }
        public void setClubId(Integer clubId) { this.clubId = clubId; }

        public String getClubName() { return clubName; }
        public void setClubName(String clubName) { this.clubName = clubName; }

        public LocalDateTime getStartTime() { return startTime; }
        public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

        public LocalDateTime getEndTime() { return endTime; }
        public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

        public String getPurpose() { return purpose; }
        public void setPurpose(String purpose) { this.purpose = purpose; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public Integer getApplicantId() { return applicantId; }
        public void setApplicantId(Integer applicantId) { this.applicantId = applicantId; }

        public String getApplicantName() { return applicantName; }
        public void setApplicantName(String applicantName) { this.applicantName = applicantName; }

        public Integer getAuditorId() { return auditorId; }
        public void setAuditorId(Integer auditorId) { this.auditorId = auditorId; }

        public String getAuditorName() { return auditorName; }
        public void setAuditorName(String auditorName) { this.auditorName = auditorName; }

        public String getRejectReason() { return rejectReason; }
        public void setRejectReason(String rejectReason) { this.rejectReason = rejectReason; }

        public LocalDateTime getCreateTime() { return createTime; }
        public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    }
}
