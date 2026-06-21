package com.club.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.club.common.Result;
import com.club.common.RoleConstants;
import com.club.dto.PartnerDTOs;
import com.club.entity.Club;
import com.club.entity.ClubPartner;
import com.club.entity.User;
import com.club.mapper.ClubMapper;
import com.club.mapper.ClubPartnerMapper;
import com.club.mapper.UserMapper;
import com.club.service.PartnerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PartnerServiceImpl extends ServiceImpl<ClubPartnerMapper, ClubPartner> implements PartnerService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ClubMapper clubMapper;

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) return null;
        return userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, auth.getName()));
    }

    private boolean canManageClub(User user, Integer clubId) {
        if (user == null) return false;
        if (RoleConstants.ADMIN.equals(user.getRole()) || RoleConstants.UNION_ADMIN.equals(user.getRole())) {
            return true;
        }
        if (RoleConstants.CLUB_LEADER.equals(user.getRole())) {
            return user.getClubId() != null && user.getClubId().equals(clubId);
        }
        return false;
    }

    private Integer calculateDaysToExpire(LocalDate contractEnd) {
        if (contractEnd == null) return null;
        return (int) ChronoUnit.DAYS.between(LocalDate.now(), contractEnd);
    }

    private String autoUpdateStatus(ClubPartner partner) {
        if (partner.getContractEnd() != null && !partner.getContractEnd().isAfter(LocalDate.now())) {
            if (!"EXPIRED".equals(partner.getStatus())) {
                partner.setStatus("EXPIRED");
                this.updateById(partner);
            }
            return "EXPIRED";
        }
        if (partner.getStatus() == null) {
            partner.setStatus("ACTIVE");
        }
        return partner.getStatus();
    }

    private Map<String, Object> enrichPartner(ClubPartner p) {
        autoUpdateStatus(p);
        Map<String, Object> map = new HashMap<>();
        map.put("id", p.getId());
        map.put("clubId", p.getClubId());
        map.put("partnerName", p.getPartnerName());
        map.put("type", p.getType());
        map.put("contactName", p.getContactName());
        map.put("contactPhone", p.getContactPhone());
        map.put("contractStart", p.getContractStart());
        map.put("contractEnd", p.getContractEnd());
        map.put("contractUrl", p.getContractUrl());
        map.put("remark", p.getRemark());
        map.put("status", p.getStatus());
        map.put("createTime", p.getCreateTime());
        map.put("daysToExpire", calculateDaysToExpire(p.getContractEnd()));

        Club club = clubMapper.selectById(p.getClubId());
        map.put("clubName", club != null ? club.getName() : "未知社团");

        return map;
    }

    private List<Map<String, Object>> enrichPartnerList(List<ClubPartner> list) {
        List<Integer> clubIds = list.stream().map(ClubPartner::getClubId).distinct().collect(Collectors.toList());
        Map<Integer, String> clubNameMap = new HashMap<>();
        if (!clubIds.isEmpty()) {
            List<Club> clubs = clubMapper.selectBatchIds(clubIds);
            for (Club c : clubs) {
                clubNameMap.put(c.getId(), c.getName());
            }
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (ClubPartner p : list) {
            autoUpdateStatus(p);
            Map<String, Object> map = new HashMap<>();
            map.put("id", p.getId());
            map.put("clubId", p.getClubId());
            map.put("partnerName", p.getPartnerName());
            map.put("type", p.getType());
            map.put("contactName", p.getContactName());
            map.put("contactPhone", p.getContactPhone());
            map.put("contractStart", p.getContractStart());
            map.put("contractEnd", p.getContractEnd());
            map.put("contractUrl", p.getContractUrl());
            map.put("remark", p.getRemark());
            map.put("status", p.getStatus());
            map.put("createTime", p.getCreateTime());
            map.put("daysToExpire", calculateDaysToExpire(p.getContractEnd()));
            map.put("clubName", clubNameMap.getOrDefault(p.getClubId(), "未知社团"));
            result.add(map);
        }
        return result;
    }

    private List<ClubPartner> enrichForExport(List<ClubPartner> list) {
        List<Integer> clubIds = list.stream().map(ClubPartner::getClubId).distinct().collect(Collectors.toList());
        Map<Integer, String> clubNameMap = new HashMap<>();
        if (!clubIds.isEmpty()) {
            List<Club> clubs = clubMapper.selectBatchIds(clubIds);
            for (Club c : clubs) {
                clubNameMap.put(c.getId(), c.getName());
            }
        }
        for (ClubPartner p : list) {
            p.setClubName(clubNameMap.getOrDefault(p.getClubId(), "未知社团"));
            p.setDaysToExpire(calculateDaysToExpire(p.getContractEnd()));
        }
        return list;
    }

    private void validateDateRange(LocalDate start, LocalDate end) {
        if (start != null && end != null && end.isBefore(start)) {
            throw new IllegalArgumentException("合同结束日期不能早于开始日期");
        }
    }

    @Override
    @Transactional
    public Result<?> createPartner(PartnerDTOs.CreateRequest request) {
        User user = getCurrentUser();
        if (user == null) return Result.error("未认证");

        Integer clubId = request.getClubId() != null ? request.getClubId() : user.getClubId();
        if (clubId == null) return Result.error("必须指定所属社团");

        if (!canManageClub(user, clubId)) {
            return Result.error("无权限管理该社团的合作伙伴");
        }

        validateDateRange(request.getContractStart(), request.getContractEnd());

        ClubPartner partner = new ClubPartner();
        partner.setClubId(clubId);
        partner.setPartnerName(request.getPartnerName());
        partner.setType(request.getType());
        partner.setContactName(request.getContactName());
        partner.setContactPhone(request.getContactPhone());
        partner.setContractStart(request.getContractStart());
        partner.setContractEnd(request.getContractEnd());
        partner.setContractUrl(request.getContractUrl());
        partner.setRemark(request.getRemark());
        partner.setStatus("ACTIVE");

        this.save(partner);
        return Result.success(partner);
    }

    @Override
    @Transactional
    public Result<?> updatePartner(Integer id, PartnerDTOs.UpdateRequest request) {
        User user = getCurrentUser();
        if (user == null) return Result.error("未认证");

        ClubPartner partner = this.getById(id);
        if (partner == null) return Result.error("合作伙伴不存在");

        if (!canManageClub(user, partner.getClubId())) {
            return Result.error("无权限修改该合作伙伴记录");
        }

        LocalDate newStart = request.getContractStart() != null ? request.getContractStart() : partner.getContractStart();
        LocalDate newEnd = request.getContractEnd() != null ? request.getContractEnd() : partner.getContractEnd();
        validateDateRange(newStart, newEnd);

        if (request.getClubId() != null) {
            if (!canManageClub(user, request.getClubId())) {
                return Result.error("无权限将记录转移到该社团");
            }
            partner.setClubId(request.getClubId());
        }
        if (request.getPartnerName() != null) partner.setPartnerName(request.getPartnerName());
        if (request.getType() != null) partner.setType(request.getType());
        if (request.getContactName() != null) partner.setContactName(request.getContactName());
        if (request.getContactPhone() != null) partner.setContactPhone(request.getContactPhone());
        if (request.getContractStart() != null) partner.setContractStart(request.getContractStart());
        if (request.getContractEnd() != null) partner.setContractEnd(request.getContractEnd());
        if (request.getContractUrl() != null) partner.setContractUrl(request.getContractUrl());
        if (request.getRemark() != null) partner.setRemark(request.getRemark());
        if (request.getStatus() != null) partner.setStatus(request.getStatus());

        this.updateById(partner);
        return Result.success(partner);
    }

    @Override
    @Transactional
    public Result<?> deletePartner(Integer id) {
        User user = getCurrentUser();
        if (user == null) return Result.error("未认证");

        ClubPartner partner = this.getById(id);
        if (partner == null) return Result.error("合作伙伴不存在");

        if (!canManageClub(user, partner.getClubId())) {
            return Result.error("无权限删除该合作伙伴记录");
        }

        this.removeById(id);
        return Result.success("删除成功");
    }

    @Override
    public Result<?> getPartnerById(Integer id) {
        User user = getCurrentUser();
        if (user == null) return Result.error("未认证");

        ClubPartner partner = this.getById(id);
        if (partner == null) return Result.error("合作伙伴不存在");

        if (!canManageClub(user, partner.getClubId())) {
            return Result.error("无权限查看该记录");
        }

        return Result.success(enrichPartner(partner));
    }

    @Override
    public Result<?> listPartners(PartnerDTOs.QueryRequest request) {
        User user = getCurrentUser();
        if (user == null) return Result.error("未认证");

        LambdaQueryWrapper<ClubPartner> wrapper = new LambdaQueryWrapper<>();

        if (RoleConstants.CLUB_LEADER.equals(user.getRole())) {
            wrapper.eq(ClubPartner::getClubId, user.getClubId());
        } else if (request.getClubId() != null) {
            wrapper.eq(ClubPartner::getClubId, request.getClubId());
        }

        if (request.getType() != null && !request.getType().isEmpty()) {
            wrapper.eq(ClubPartner::getType, request.getType());
        }
        if (request.getStatus() != null && !request.getStatus().isEmpty()) {
            wrapper.eq(ClubPartner::getStatus, request.getStatus());
        }
        if (request.getKeyword() != null && !request.getKeyword().isEmpty()) {
            wrapper.and(w -> w.like(ClubPartner::getPartnerName, request.getKeyword())
                    .or().like(ClubPartner::getContactName, request.getKeyword())
                    .or().like(ClubPartner::getContactPhone, request.getKeyword()));
        }
        if (request.getContractStartFrom() != null) {
            wrapper.ge(ClubPartner::getContractStart, request.getContractStartFrom());
        }
        if (request.getContractStartTo() != null) {
            wrapper.le(ClubPartner::getContractStart, request.getContractStartTo());
        }
        if (request.getContractEndFrom() != null) {
            wrapper.ge(ClubPartner::getContractEnd, request.getContractEndFrom());
        }
        if (request.getContractEndTo() != null) {
            wrapper.le(ClubPartner::getContractEnd, request.getContractEndTo());
        }
        if (Boolean.TRUE.equals(request.getExpiringSoon())) {
            LocalDate today = LocalDate.now();
            LocalDate threshold = today.plusDays(30);
            wrapper.and(w -> w.isNotNull(ClubPartner::getContractEnd)
                    .le(ClubPartner::getContractEnd, threshold)
                    .ge(ClubPartner::getContractEnd, today));
        }

        wrapper.orderByDesc(ClubPartner::getCreateTime);

        List<ClubPartner> list = this.list(wrapper);
        List<Map<String, Object>> result = enrichPartnerList(list);
        return Result.success(result);
    }

    @Override
    public Result<?> getPartnerStats() {
        User user = getCurrentUser();
        if (user == null) return Result.error("未认证");

        LambdaQueryWrapper<ClubPartner> wrapper = new LambdaQueryWrapper<>();
        if (RoleConstants.CLUB_LEADER.equals(user.getRole())) {
            wrapper.eq(ClubPartner::getClubId, user.getClubId());
        }

        List<ClubPartner> all = this.list(wrapper);
        LocalDate today = LocalDate.now();
        LocalDate threshold30 = today.plusDays(30);
        LocalDate threshold7 = today.plusDays(7);

        long total = all.size();
        long activeCount = all.stream().filter(p -> "ACTIVE".equals(autoUpdateStatus(p))).count();
        long expiredCount = total - activeCount;

        long expiring30 = all.stream()
                .filter(p -> p.getContractEnd() != null)
                .filter(p -> !p.getContractEnd().isBefore(today) && !p.getContractEnd().isAfter(threshold30))
                .filter(p -> "ACTIVE".equals(p.getStatus()))
                .count();

        long expiring7 = all.stream()
                .filter(p -> p.getContractEnd() != null)
                .filter(p -> !p.getContractEnd().isBefore(today) && !p.getContractEnd().isAfter(threshold7))
                .filter(p -> "ACTIVE".equals(p.getStatus()))
                .count();

        Map<String, Long> typeStats = all.stream()
                .collect(Collectors.groupingBy(ClubPartner::getType, Collectors.counting()));

        Map<Integer, Long> clubStats = all.stream()
                .filter(p -> !RoleConstants.CLUB_LEADER.equals(user.getRole()))
                .collect(Collectors.groupingBy(ClubPartner::getClubId, Collectors.counting()));

        Map<Integer, String> clubNameMap = new HashMap<>();
        if (!clubStats.isEmpty()) {
            List<Club> clubs = clubMapper.selectBatchIds(clubStats.keySet());
            for (Club c : clubs) {
                clubNameMap.put(c.getId(), c.getName());
            }
        }

        List<Map<String, Object>> clubStatsList = new ArrayList<>();
        for (Map.Entry<Integer, Long> entry : clubStats.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("clubId", entry.getKey());
            item.put("clubName", clubNameMap.getOrDefault(entry.getKey(), "未知社团"));
            item.put("count", entry.getValue());
            clubStatsList.add(item);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("activeCount", activeCount);
        result.put("expiredCount", expiredCount);
        result.put("expiringIn30Days", expiring30);
        result.put("expiringIn7Days", expiring7);
        result.put("typeStats", typeStats);
        result.put("clubStats", clubStatsList);

        return Result.success(result);
    }

    @Override
    public void exportPartners(PartnerDTOs.QueryRequest request, HttpServletResponse response) throws IOException {
        User user = getCurrentUser();
        if (user == null) return;

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("合作伙伴台账", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        LambdaQueryWrapper<ClubPartner> wrapper = new LambdaQueryWrapper<>();
        if (RoleConstants.CLUB_LEADER.equals(user.getRole())) {
            wrapper.eq(ClubPartner::getClubId, user.getClubId());
        } else if (request.getClubId() != null) {
            wrapper.eq(ClubPartner::getClubId, request.getClubId());
        }
        if (request.getType() != null && !request.getType().isEmpty()) {
            wrapper.eq(ClubPartner::getType, request.getType());
        }
        if (request.getStatus() != null && !request.getStatus().isEmpty()) {
            wrapper.eq(ClubPartner::getStatus, request.getStatus());
        }
        if (request.getKeyword() != null && !request.getKeyword().isEmpty()) {
            wrapper.and(w -> w.like(ClubPartner::getPartnerName, request.getKeyword())
                    .or().like(ClubPartner::getContactName, request.getKeyword()));
        }
        if (Boolean.TRUE.equals(request.getExpiringSoon())) {
            LocalDate today = LocalDate.now();
            LocalDate threshold = today.plusDays(30);
            wrapper.and(w -> w.isNotNull(ClubPartner::getContractEnd)
                    .le(ClubPartner::getContractEnd, threshold)
                    .ge(ClubPartner::getContractEnd, today));
        }
        wrapper.orderByDesc(ClubPartner::getCreateTime);

        List<ClubPartner> list = this.list(wrapper);
        List<ClubPartner> exportList = enrichForExport(list);

        EasyExcel.write(response.getOutputStream(), ClubPartner.class)
                .sheet("合作伙伴台账")
                .doWrite(exportList);
    }
}
