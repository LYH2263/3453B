package com.club.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

@TableName("point_ledger")
public class PointLedger {
    @TableId(type = IdType.AUTO)
    private Integer id;

    @NotNull(message = "用户ID不能为空")
    private Integer userId;

    @NotNull(message = "积分变动不能为空")
    private Integer delta;

    @NotBlank(message = "变动原因不能为空")
    private String reason;

    private String refType;

    private Integer refId;

    @NotNull(message = "余额快照不能为空")
    private Integer balanceSnapshot;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(exist = false)
    private String userName;

    @TableField(exist = false)
    private String studentNo;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public Integer getDelta() { return delta; }
    public void setDelta(Integer delta) { this.delta = delta; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getRefType() { return refType; }
    public void setRefType(String refType) { this.refType = refType; }

    public Integer getRefId() { return refId; }
    public void setRefId(Integer refId) { this.refId = refId; }

    public Integer getBalanceSnapshot() { return balanceSnapshot; }
    public void setBalanceSnapshot(Integer balanceSnapshot) { this.balanceSnapshot = balanceSnapshot; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getStudentNo() { return studentNo; }
    public void setStudentNo(String studentNo) { this.studentNo = studentNo; }
}
