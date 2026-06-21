package com.club.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.ExcelIgnore;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@TableName("volunteer_records")
public class VolunteerRecord {
    @TableId(type = IdType.AUTO)
    @ExcelProperty("记录ID")
    private Integer id;

    @NotNull(message = "学生ID不能为空")
    @ExcelProperty("学生ID")
    private Integer studentId;

    @NotBlank(message = "活动名称不能为空")
    @ExcelProperty("活动名称")
    private String activityName;

    @NotNull(message = "服务日期不能为空")
    @ExcelProperty("服务日期")
    private LocalDate serviceDate;

    @NotNull(message = "时长不能为空")
    @DecimalMin(value = "0.5", message = "时长最少0.5小时")
    @DecimalMax(value = "24", message = "时长不能超过24小时")
    @ExcelProperty("时长(小时)")
    private BigDecimal hours;

    @ExcelProperty("证明URL")
    private String proofUrl;

    @ExcelProperty("状态")
    private String status;

    @ExcelIgnore
    private Integer auditorId;

    @ExcelProperty("社团ID")
    private Integer clubId;

    @ExcelProperty("驳回原因")
    private String rejectReason;

    @TableField(fill = FieldFill.INSERT)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @ExcelIgnore
    private LocalDateTime updateTime;

    @TableLogic
    @ExcelIgnore
    private Integer isDeleted;

    @TableField(exist = false)
    @ExcelProperty("学生姓名")
    private String studentName;

    @TableField(exist = false)
    @ExcelProperty("学号")
    private String studentNo;

    @TableField(exist = false)
    @ExcelProperty("社团名称")
    private String clubName;

    @TableField(exist = false)
    @ExcelProperty("审核人")
    private String auditorName;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getStudentId() { return studentId; }
    public void setStudentId(Integer studentId) { this.studentId = studentId; }

    public String getActivityName() { return activityName; }
    public void setActivityName(String activityName) { this.activityName = activityName; }

    public LocalDate getServiceDate() { return serviceDate; }
    public void setServiceDate(LocalDate serviceDate) { this.serviceDate = serviceDate; }

    public BigDecimal getHours() { return hours; }
    public void setHours(BigDecimal hours) { this.hours = hours; }

    public String getProofUrl() { return proofUrl; }
    public void setProofUrl(String proofUrl) { this.proofUrl = proofUrl; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getAuditorId() { return auditorId; }
    public void setAuditorId(Integer auditorId) { this.auditorId = auditorId; }

    public Integer getClubId() { return clubId; }
    public void setClubId(Integer clubId) { this.clubId = clubId; }

    public String getRejectReason() { return rejectReason; }
    public void setRejectReason(String rejectReason) { this.rejectReason = rejectReason; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }

    public Integer getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Integer isDeleted) { this.isDeleted = isDeleted; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getStudentNo() { return studentNo; }
    public void setStudentNo(String studentNo) { this.studentNo = studentNo; }

    public String getClubName() { return clubName; }
    public void setClubName(String clubName) { this.clubName = clubName; }

    public String getAuditorName() { return auditorName; }
    public void setAuditorName(String auditorName) { this.auditorName = auditorName; }
}
