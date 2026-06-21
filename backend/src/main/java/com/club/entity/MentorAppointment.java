package com.club.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

@TableName("mentor_appointments")
public class MentorAppointment {
    @TableId(type = IdType.AUTO)
    private Integer id;
    @NotNull(message = "时段不能为空")
    private Integer slotId;
    @NotNull(message = "学生不能为空")
    private Integer studentId;
    private String status;
    private String rejectReason;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getSlotId() { return slotId; }
    public void setSlotId(Integer slotId) { this.slotId = slotId; }

    public Integer getStudentId() { return studentId; }
    public void setStudentId(Integer studentId) { this.studentId = studentId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRejectReason() { return rejectReason; }
    public void setRejectReason(String rejectReason) { this.rejectReason = rejectReason; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
