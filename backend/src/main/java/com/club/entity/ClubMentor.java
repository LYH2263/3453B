package com.club.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

@TableName("club_mentors")
public class ClubMentor {
    @TableId(type = IdType.AUTO)
    private Integer id;
    @NotNull(message = "社团不能为空")
    private Integer clubId;
    @NotBlank(message = "导师姓名不能为空")
    private String name;
    private String staffNo;
    private String researchArea;
    private String intro;
    private Integer userId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer isDeleted;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getClubId() { return clubId; }
    public void setClubId(Integer clubId) { this.clubId = clubId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getStaffNo() { return staffNo; }
    public void setStaffNo(String staffNo) { this.staffNo = staffNo; }

    public String getResearchArea() { return researchArea; }
    public void setResearchArea(String researchArea) { this.researchArea = researchArea; }

    public String getIntro() { return intro; }
    public void setIntro(String intro) { this.intro = intro; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }

    public Integer getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Integer isDeleted) { this.isDeleted = isDeleted; }
}
