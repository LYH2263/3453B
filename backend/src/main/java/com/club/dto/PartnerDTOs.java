package com.club.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public class PartnerDTOs {

    public static class CreateRequest {
        @NotNull(message = "社团ID不能为空")
        private Integer clubId;

        @NotBlank(message = "合作伙伴名称不能为空")
        private String partnerName;

        @NotBlank(message = "类型不能为空")
        @Pattern(regexp = "^(ENTERPRISE|SCHOOL_ORG|OTHER)$", message = "类型只能是ENTERPRISE、SCHOOL_ORG或OTHER")
        private String type;

        private String contactName;

        private String contactPhone;

        private LocalDate contractStart;

        private LocalDate contractEnd;

        private String contractUrl;

        private String remark;

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
    }

    public static class UpdateRequest {
        private Integer clubId;

        private String partnerName;

        @Pattern(regexp = "^(ENTERPRISE|SCHOOL_ORG|OTHER)$", message = "类型只能是ENTERPRISE、SCHOOL_ORG或OTHER")
        private String type;

        private String contactName;

        private String contactPhone;

        private LocalDate contractStart;

        private LocalDate contractEnd;

        private String contractUrl;

        private String remark;

        @Pattern(regexp = "^(ACTIVE|EXPIRED)$", message = "状态只能是ACTIVE或EXPIRED")
        private String status;

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
    }

    public static class QueryRequest {
        private Integer clubId;
        private String type;
        private String status;
        private String keyword;
        private Boolean expiringSoon;
        private LocalDate contractStartFrom;
        private LocalDate contractStartTo;
        private LocalDate contractEndFrom;
        private LocalDate contractEndTo;

        public Integer getClubId() { return clubId; }
        public void setClubId(Integer clubId) { this.clubId = clubId; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getKeyword() { return keyword; }
        public void setKeyword(String keyword) { this.keyword = keyword; }

        public Boolean getExpiringSoon() { return expiringSoon; }
        public void setExpiringSoon(Boolean expiringSoon) { this.expiringSoon = expiringSoon; }

        public LocalDate getContractStartFrom() { return contractStartFrom; }
        public void setContractStartFrom(LocalDate contractStartFrom) { this.contractStartFrom = contractStartFrom; }

        public LocalDate getContractStartTo() { return contractStartTo; }
        public void setContractStartTo(LocalDate contractStartTo) { this.contractStartTo = contractStartTo; }

        public LocalDate getContractEndFrom() { return contractEndFrom; }
        public void setContractEndFrom(LocalDate contractEndFrom) { this.contractEndFrom = contractEndFrom; }

        public LocalDate getContractEndTo() { return contractEndTo; }
        public void setContractEndTo(LocalDate contractEndTo) { this.contractEndTo = contractEndTo; }
    }
}
