package com.club.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class InspectionDTOs {

    public static class PlanCreateRequest {
        @NotNull(message = "设备ID不能为空")
        private Integer deviceId;

        private Integer cycleDays;

        private String cronExpr;

        @NotNull(message = "巡检人ID不能为空")
        private Integer inspectorId;

        private LocalDate nextInspectDate;

        public Integer getDeviceId() { return deviceId; }
        public void setDeviceId(Integer deviceId) { this.deviceId = deviceId; }

        public Integer getCycleDays() { return cycleDays; }
        public void setCycleDays(Integer cycleDays) { this.cycleDays = cycleDays; }

        public String getCronExpr() { return cronExpr; }
        public void setCronExpr(String cronExpr) { this.cronExpr = cronExpr; }

        public Integer getInspectorId() { return inspectorId; }
        public void setInspectorId(Integer inspectorId) { this.inspectorId = inspectorId; }

        public LocalDate getNextInspectDate() { return nextInspectDate; }
        public void setNextInspectDate(LocalDate nextInspectDate) { this.nextInspectDate = nextInspectDate; }
    }

    public static class PlanUpdateRequest {
        @NotNull(message = "计划ID不能为空")
        private Integer id;

        private Integer cycleDays;

        private String cronExpr;

        private Integer inspectorId;

        private LocalDate nextInspectDate;

        private String status;

        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }

        public Integer getCycleDays() { return cycleDays; }
        public void setCycleDays(Integer cycleDays) { this.cycleDays = cycleDays; }

        public String getCronExpr() { return cronExpr; }
        public void setCronExpr(String cronExpr) { this.cronExpr = cronExpr; }

        public Integer getInspectorId() { return inspectorId; }
        public void setInspectorId(Integer inspectorId) { this.inspectorId = inspectorId; }

        public LocalDate getNextInspectDate() { return nextInspectDate; }
        public void setNextInspectDate(LocalDate nextInspectDate) { this.nextInspectDate = nextInspectDate; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    public static class RecordSubmitRequest {
        @NotNull(message = "计划ID不能为空")
        private Integer planId;

        @NotBlank(message = "巡检结果不能为空")
        @Pattern(regexp = "^(NORMAL|ABNORMAL)$", message = "结果只能是NORMAL或ABNORMAL")
        private String result;

        private String remark;

        @NotNull(message = "巡检时间不能为空")
        private LocalDateTime inspectTime;

        public Integer getPlanId() { return planId; }
        public void setPlanId(Integer planId) { this.planId = planId; }

        public String getResult() { return result; }
        public void setResult(String result) { this.result = result; }

        public String getRemark() { return remark; }
        public void setRemark(String remark) { this.remark = remark; }

        public LocalDateTime getInspectTime() { return inspectTime; }
        public void setInspectTime(LocalDateTime inspectTime) { this.inspectTime = inspectTime; }
    }
}
