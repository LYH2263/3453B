package com.club.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("club_locations")
public class ClubLocation {
    @TableId(type = IdType.AUTO)
    private Integer id;

    @NotNull(message = "所属社团ID不能为空")
    private Integer clubId;

    @NotBlank(message = "楼栋名称不能为空")
    private String building;

    private String floor;
    private String room;
    private String description;

    private BigDecimal longitude;
    private BigDecimal latitude;

    private String openHours;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;

    @TableField(exist = false)
    private Club club;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getClubId() { return clubId; }
    public void setClubId(Integer clubId) { this.clubId = clubId; }

    public String getBuilding() { return building; }
    public void setBuilding(String building) { this.building = building; }

    public String getFloor() { return floor; }
    public void setFloor(String floor) { this.floor = floor; }

    public String getRoom() { return room; }
    public void setRoom(String room) { this.room = room; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }

    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }

    public String getOpenHours() { return openHours; }
    public void setOpenHours(String openHours) { this.openHours = openHours; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }

    public Integer getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Integer isDeleted) { this.isDeleted = isDeleted; }

    public Club getClub() { return club; }
    public void setClub(Club club) { this.club = club; }
}
