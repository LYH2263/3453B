package com.club.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public class VolunteerDTOs {

    public static class SubmitRequest {
        @NotBlank(message = "活动名称不能为空")
        private String activityName;

        @NotNull(message = "服务日期不能为空")
        private LocalDate serviceDate;

        @NotNull(message = "时长不能为空")
        @DecimalMin(value = "0.5", message = "时长最少0.5小时")
        @DecimalMax(value = "24", message = "时长不能超过24小时")
        private BigDecimal hours;

        private String proofUrl;

        private Integer clubId;

        public String getActivityName() { return activityName; }
        public void setActivityName(String activityName) { this.activityName = activityName; }

        public LocalDate getServiceDate() { return serviceDate; }
        public void setServiceDate(LocalDate serviceDate) { this.serviceDate = serviceDate; }

        public BigDecimal getHours() { return hours; }
        public void setHours(BigDecimal hours) { this.hours = hours; }

        public String getProofUrl() { return proofUrl; }
        public void setProofUrl(String proofUrl) { this.proofUrl = proofUrl; }

        public Integer getClubId() { return clubId; }
        public void setClubId(Integer clubId) { this.clubId = clubId; }
    }

    public static class AuditRequest {
        @NotBlank(message = "状态不能为空")
        @Pattern(regexp = "^(APPROVED|REJECTED)$", message = "状态只能是APPROVED或REJECTED")
        private String status;

        private String rejectReason;

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getRejectReason() { return rejectReason; }
        public void setRejectReason(String rejectReason) { this.rejectReason = rejectReason; }
    }

    public static class QueryRequest {
        private Integer clubId;
        private LocalDate startDate;
        private LocalDate endDate;
        private String status;
        private Integer userId;

        public Integer getClubId() { return clubId; }
        public void setClubId(Integer clubId) { this.clubId = clubId; }

        public LocalDate getStartDate() { return startDate; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

        public LocalDate getEndDate() { return endDate; }
        public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public Integer getUserId() { return userId; }
        public void setUserId(Integer userId) { this.userId = userId; }
    }
}
