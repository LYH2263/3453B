package com.club.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.ExcelIgnore;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@TableName("volunteer_stats")
public class VolunteerStat {
    @TableId(type = IdType.AUTO)
    @ExcelIgnore
    private Integer id;

    @ExcelProperty("用户ID")
    private Integer userId;

    @ExcelProperty("累计服务时长(小时)")
    private BigDecimal totalHours;

    @ExcelProperty("已通过记录数")
    private Integer approvedCount;

    @ExcelProperty("待审核记录数")
    private Integer pendingCount;

    @ExcelProperty("已驳回记录数")
    private Integer rejectedCount;

    @ExcelProperty("最近服务日期")
    private LocalDate lastServiceDate;

    @TableField(fill = FieldFill.INSERT)
    @ExcelIgnore
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @ExcelIgnore
    private LocalDateTime updateTime;

    @TableField(exist = false)
    @ExcelProperty("学生姓名")
    private String studentName;

    @TableField(exist = false)
    @ExcelProperty("学号")
    private String studentNo;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public BigDecimal getTotalHours() { return totalHours; }
    public void setTotalHours(BigDecimal totalHours) { this.totalHours = totalHours; }

    public Integer getApprovedCount() { return approvedCount; }
    public void setApprovedCount(Integer approvedCount) { this.approvedCount = approvedCount; }

    public Integer getPendingCount() { return pendingCount; }
    public void setPendingCount(Integer pendingCount) { this.pendingCount = pendingCount; }

    public Integer getRejectedCount() { return rejectedCount; }
    public void setRejectedCount(Integer rejectedCount) { this.rejectedCount = rejectedCount; }

    public LocalDate getLastServiceDate() { return lastServiceDate; }
    public void setLastServiceDate(LocalDate lastServiceDate) { this.lastServiceDate = lastServiceDate; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getStudentNo() { return studentNo; }
    public void setStudentNo(String studentNo) { this.studentNo = studentNo; }
}
