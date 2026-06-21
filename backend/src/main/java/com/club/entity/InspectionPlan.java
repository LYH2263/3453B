package com.club.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@TableName("inspection_plans")
public class InspectionPlan {
    @TableId(type = IdType.AUTO)
    private Integer id;

    @NotNull(message = "设备ID不能为空")
    private Integer deviceId;

    private Integer cycleDays;

    private String cronExpr;

    @NotNull(message = "负责人ID不能为空")
    private Integer ownerId;

    @NotNull(message = "巡检人ID不能为空")
    private Integer inspectorId;

    private LocalDateTime lastInspectTime;

    private LocalDate nextInspectDate;

    private String status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;

    @TableField(exist = false)
    private String deviceName;

    @TableField(exist = false)
    private String deviceNo;

    @TableField(exist = false)
    private String deviceLocation;

    @TableField(exist = false)
    private Integer clubId;

    @TableField(exist = false)
    private String clubName;

    @TableField(exist = false)
    private String ownerName;

    @TableField(exist = false)
    private String inspectorName;

    @TableField(exist = false)
    private Boolean isOverdue;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getDeviceId() { return deviceId; }
    public void setDeviceId(Integer deviceId) { this.deviceId = deviceId; }

    public Integer getCycleDays() { return cycleDays; }
    public void setCycleDays(Integer cycleDays) { this.cycleDays = cycleDays; }

    public String getCronExpr() { return cronExpr; }
    public void setCronExpr(String cronExpr) { this.cronExpr = cronExpr; }

    public Integer getOwnerId() { return ownerId; }
    public void setOwnerId(Integer ownerId) { this.ownerId = ownerId; }

    public Integer getInspectorId() { return inspectorId; }
    public void setInspectorId(Integer inspectorId) { this.inspectorId = inspectorId; }

    public LocalDateTime getLastInspectTime() { return lastInspectTime; }
    public void setLastInspectTime(LocalDateTime lastInspectTime) { this.lastInspectTime = lastInspectTime; }

    public LocalDate getNextInspectDate() { return nextInspectDate; }
    public void setNextInspectDate(LocalDate nextInspectDate) { this.nextInspectDate = nextInspectDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }

    public Integer getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Integer isDeleted) { this.isDeleted = isDeleted; }

    public String getDeviceName() { return deviceName; }
    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }

    public String getDeviceNo() { return deviceNo; }
    public void setDeviceNo(String deviceNo) { this.deviceNo = deviceNo; }

    public String getDeviceLocation() { return deviceLocation; }
    public void setDeviceLocation(String deviceLocation) { this.deviceLocation = deviceLocation; }

    public Integer getClubId() { return clubId; }
    public void setClubId(Integer clubId) { this.clubId = clubId; }

    public String getClubName() { return clubName; }
    public void setClubName(String clubName) { this.clubName = clubName; }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }

    public String getInspectorName() { return inspectorName; }
    public void setInspectorName(String inspectorName) { this.inspectorName = inspectorName; }

    public Boolean getIsOverdue() { return isOverdue; }
    public void setIsOverdue(Boolean isOverdue) { this.isOverdue = isOverdue; }
}
