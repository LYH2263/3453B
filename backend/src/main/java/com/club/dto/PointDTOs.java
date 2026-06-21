package com.club.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

public class PointDTOs {

    public static class ShopItemRequest {
        @NotBlank(message = "商品名称不能为空")
        private String name;

        private String description;

        private String imageUrl;

        @NotNull(message = "所需积分不能为空")
        @Min(value = 1, message = "所需积分不能小于1")
        private Integer costPoints;

        @NotNull(message = "库存不能为空")
        @Min(value = 0, message = "库存不能小于0")
        private Integer stock;

        private Integer clubId;

        private Integer sortOrder;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

        public Integer getCostPoints() { return costPoints; }
        public void setCostPoints(Integer costPoints) { this.costPoints = costPoints; }

        public Integer getStock() { return stock; }
        public void setStock(Integer stock) { this.stock = stock; }

        public Integer getClubId() { return clubId; }
        public void setClubId(Integer clubId) { this.clubId = clubId; }

        public Integer getSortOrder() { return sortOrder; }
        public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    }

    public static class RedeemRequest {
        @NotNull(message = "商品ID不能为空")
        private Integer itemId;

        @NotNull(message = "兑换数量不能为空")
        @Min(value = 1, message = "兑换数量不能小于1")
        private Integer quantity;

        public Integer getItemId() { return itemId; }
        public void setItemId(Integer itemId) { this.itemId = itemId; }

        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
    }

    public static class PointRuleRequest {
        @NotBlank(message = "规则编码不能为空")
        private String ruleCode;

        @NotBlank(message = "规则名称不能为空")
        private String ruleName;

        @NotBlank(message = "规则详情不能为空")
        private String ruleJson;

        private String description;

        public String getRuleCode() { return ruleCode; }
        public void setRuleCode(String ruleCode) { this.ruleCode = ruleCode; }

        public String getRuleName() { return ruleName; }
        public void setRuleName(String ruleName) { this.ruleName = ruleName; }

        public String getRuleJson() { return ruleJson; }
        public void setRuleJson(String ruleJson) { this.ruleJson = ruleJson; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    public static class AddPointsRequest {
        @NotNull(message = "用户ID不能为空")
        private Integer userId;

        @NotNull(message = "积分数量不能为空")
        private Integer points;

        @NotBlank(message = "变动原因不能为空")
        private String reason;

        private String refType;

        private Integer refId;

        public Integer getUserId() { return userId; }
        public void setUserId(Integer userId) { this.userId = userId; }

        public Integer getPoints() { return points; }
        public void setPoints(Integer points) { this.points = points; }

        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }

        public String getRefType() { return refType; }
        public void setRefType(String refType) { this.refType = refType; }

        public Integer getRefId() { return refId; }
        public void setRefId(Integer refId) { this.refId = refId; }
    }

    public static class ItemQueryRequest {
        private String keyword;
        private Integer clubId;
        private String status;
        private Integer pageNum = 1;
        private Integer pageSize = 10;

        public String getKeyword() { return keyword; }
        public void setKeyword(String keyword) { this.keyword = keyword; }

        public Integer getClubId() { return clubId; }
        public void setClubId(Integer clubId) { this.clubId = clubId; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public Integer getPageNum() { return pageNum; }
        public void setPageNum(Integer pageNum) { this.pageNum = pageNum; }

        public Integer getPageSize() { return pageSize; }
        public void setPageSize(Integer pageSize) { this.pageSize = pageSize; }
    }

    public static class LedgerQueryRequest {
        private Integer userId;
        private String refType;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Integer pageNum = 1;
        private Integer pageSize = 10;

        public Integer getUserId() { return userId; }
        public void setUserId(Integer userId) { this.userId = userId; }

        public String getRefType() { return refType; }
        public void setRefType(String refType) { this.refType = refType; }

        public LocalDateTime getStartTime() { return startTime; }
        public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

        public LocalDateTime getEndTime() { return endTime; }
        public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

        public Integer getPageNum() { return pageNum; }
        public void setPageNum(Integer pageNum) { this.pageNum = pageNum; }

        public Integer getPageSize() { return pageSize; }
        public void setPageSize(Integer pageSize) { this.pageSize = pageSize; }
    }

    public static class OrderQueryRequest {
        private Integer userId;
        private Integer itemId;
        private String status;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Integer pageNum = 1;
        private Integer pageSize = 10;

        public Integer getUserId() { return userId; }
        public void setUserId(Integer userId) { this.userId = userId; }

        public Integer getItemId() { return itemId; }
        public void setItemId(Integer itemId) { this.itemId = itemId; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public LocalDateTime getStartTime() { return startTime; }
        public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

        public LocalDateTime getEndTime() { return endTime; }
        public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

        public Integer getPageNum() { return pageNum; }
        public void setPageNum(Integer pageNum) { this.pageNum = pageNum; }

        public Integer getPageSize() { return pageSize; }
        public void setPageSize(Integer pageSize) { this.pageSize = pageSize; }
    }
}
