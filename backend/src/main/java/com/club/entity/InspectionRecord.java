package com.club.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

@TableName("inspection_records")
public class InspectionRecord {
    @TableId(type = IdType.AUTO)
    private Integer id;

    @NotNull(message = "计划ID不能为空")
    private Integer planId;

    @NotBlank(message = "巡检结果不能为空")
    private String result;

    private String remark;

    @NotNull(message = "巡检时间不能为空")
    private LocalDateTime inspectTime;

    @NotNull(message = "巡检人ID不能为空")
    private Integer inspectorId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

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
    private String inspectorName;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getPlanId() { return planId; }
    public void setPlanId(Integer planId) { this.planId = planId; }

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    public LocalDateTime getInspectTime() { return inspectTime; }
    public void setInspectTime(LocalDateTime inspectTime) { this.inspectTime = inspectTime; }

    public Integer getInspectorId() { return inspectorId; }
    public void setInspectorId(Integer inspectorId) { this.inspectorId = inspectorId; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

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

    public String getInspectorName() { return inspectorName; }
    public void setInspectorName(String inspectorName) { this.inspectorName = inspectorName; }
}
