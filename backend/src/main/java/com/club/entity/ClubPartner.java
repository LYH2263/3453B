package com.club.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.ExcelIgnore;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@TableName("club_partners")
public class ClubPartner {
    @TableId(type = IdType.AUTO)
    @ExcelProperty("ID")
    private Integer id;

    @NotNull(message = "社团ID不能为空")
    @ExcelProperty("社团ID")
    private Integer clubId;

    @NotBlank(message = "合作伙伴名称不能为空")
    @ExcelProperty("合作伙伴名称")
    private String partnerName;

    @NotBlank(message = "类型不能为空")
    @Pattern(regexp = "^(ENTERPRISE|SCHOOL_ORG|OTHER)$", message = "类型只能是ENTERPRISE、SCHOOL_ORG或OTHER")
    @ExcelProperty("类型")
    private String type;

    @ExcelProperty("联系人")
    private String contactName;

    @ExcelProperty("联系电话")
    private String contactPhone;

    @ExcelProperty("合同开始日期")
    private LocalDate contractStart;

    @ExcelProperty("合同结束日期")
    private LocalDate contractEnd;

    @ExcelProperty("合同附件URL")
    private String contractUrl;

    @ExcelProperty("备注")
    private String remark;

    @ExcelProperty("状态")
    private String status;

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
    @ExcelProperty("社团名称")
    private String clubName;

    @TableField(exist = false)
    @ExcelProperty("距离到期天数")
    private Integer daysToExpire;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getClubId() { return clubId; }
    public void setClubId(Integer clubId) { this.clubId = clubId; }

    public String getPartnerName() { return partnerName; }
    public void setPartnerName(String partnerName) { this.partnerName = partnerName; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }

    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }

    public LocalDate getContractStart() { return contractStart; }
    public void setContractStart(LocalDate contractStart) { this.contractStart = contractStart; }

    public LocalDate getContractEnd() { return contractEnd; }
    public void setContractEnd(LocalDate contractEnd) { this.contractEnd = contractEnd; }

    public String getContractUrl() { return contractUrl; }
    public void setContractUrl(String contractUrl) { this.contractUrl = contractUrl; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }

    public Integer getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Integer isDeleted) { this.isDeleted = isDeleted; }

    public String getClubName() { return clubName; }
    public void setClubName(String clubName) { this.clubName = clubName; }

    public Integer getDaysToExpire() { return daysToExpire; }
    public void setDaysToExpire(Integer daysToExpire) { this.daysToExpire = daysToExpire; }
}
