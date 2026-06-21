package com.club.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.club.common.Result;
import com.club.common.RoleConstants;
import com.club.dto.VolunteerDTOs;
import com.club.entity.Club;
import com.club.entity.User;
import com.club.entity.VolunteerRecord;
import com.club.entity.VolunteerStat;
import com.club.mapper.ClubMapper;
import com.club.mapper.UserMapper;
import com.club.mapper.VolunteerRecordMapper;
import com.club.mapper.VolunteerStatMapper;
import com.club.service.VolunteerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class VolunteerServiceImpl extends ServiceImpl<VolunteerRecordMapper, VolunteerRecord> implements VolunteerService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ClubMapper clubMapper;

    @Autowired
    private VolunteerStatMapper volunteerStatMapper;

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) return null;
        return userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, auth.getName()));
    }

    @Override
    @Transactional
    public Result<?> submitRecord(VolunteerDTOs.SubmitRequest request) {
        User user = getCurrentUser();
        if (user == null) return Result.error("未认证");

        VolunteerRecord record = new VolunteerRecord();
        record.setStudentId(user.getId());
        record.setActivityName(request.getActivityName());
        record.setServiceDate(request.getServiceDate());
        record.setHours(request.getHours());
        record.setProofUrl(request.getProofUrl());
        record.setClubId(request.getClubId() != null ? request.getClubId() : user.getClubId());
        record.setStatus("PENDING");

        this.save(record);
        updateUserStats(user.getId());

        return Result.success(record);
    }

    @Override
    public Result<?> getMyRecords() {
        User user = getCurrentUser();
        if (user == null) return Result.error("未认证");

        List<VolunteerRecord> records = this.list(new LambdaQueryWrapper<VolunteerRecord>()
                .eq(VolunteerRecord::getStudentId, user.getId())
                .orderByDesc(VolunteerRecord::getServiceDate));

        List<Map<String, Object>> result = enrichRecords(records);
        return Result.success(result);
    }

    @Override
    @Transactional
    public Result<?> auditRecord(Integer id, VolunteerDTOs.AuditRequest request) {
        User user = getCurrentUser();
        if (user == null) return Result.error("未认证");

        VolunteerRecord record = this.getById(id);
        if (record == null) return Result.error("记录不存在");

        if (!canAudit(user, record)) {
            return Result.error("无权限审核此记录");
        }

        if (!"PENDING".equals(record.getStatus())) {
            return Result.error("该记录已审核，不可重复审核");
        }

        record.setStatus(request.getStatus());
        record.setAuditorId(user.getId());
        if ("REJECTED".equals(request.getStatus())) {
            record.setRejectReason(request.getRejectReason());
        }

        this.updateById(record);
        updateUserStats(record.getStudentId());

        return Result.success(record);
    }

    private boolean canAudit(User user, VolunteerRecord record) {
        if (RoleConstants.ADMIN.equals(user.getRole()) || RoleConstants.UNION_ADMIN.equals(user.getRole())) {
            return true;
        }
        if (RoleConstants.CLUB_LEADER.equals(user.getRole())) {
            return record.getClubId() != null && record.getClubId().equals(user.getClubId());
        }
        return false;
    }

    @Override
    public Result<?> getPendingAudits() {
        User user = getCurrentUser();
        if (user == null) return Result.error("未认证");

        LambdaQueryWrapper<VolunteerRecord> wrapper = new LambdaQueryWrapper<VolunteerRecord>()
                .eq(VolunteerRecord::getStatus, "PENDING")
                .orderByDesc(VolunteerRecord::getCreateTime);

        if (RoleConstants.CLUB_LEADER.equals(user.getRole())) {
            wrapper.eq(VolunteerRecord::getClubId, user.getClubId());
        }

        List<VolunteerRecord> records = this.list(wrapper);
        List<Map<String, Object>> result = enrichRecords(records);
        return Result.success(result);
    }

    @Override
    public Result<?> getAllRecords(VolunteerDTOs.QueryRequest request) {
        User user = getCurrentUser();
        if (user == null) return Result.error("未认证");

        LambdaQueryWrapper<VolunteerRecord> wrapper = new LambdaQueryWrapper<>();

        if (request.getUserId() != null) {
            wrapper.eq(VolunteerRecord::getStudentId, request.getUserId());
        }
        if (request.getClubId() != null) {
            wrapper.eq(VolunteerRecord::getClubId, request.getClubId());
        } else if (RoleConstants.CLUB_LEADER.equals(user.getRole())) {
            wrapper.eq(VolunteerRecord::getClubId, user.getClubId());
        }
        if (request.getStartDate() != null) {
            wrapper.ge(VolunteerRecord::getServiceDate, request.getStartDate());
        }
        if (request.getEndDate() != null) {
            wrapper.le(VolunteerRecord::getServiceDate, request.getEndDate());
        }
        if (request.getStatus() != null && !request.getStatus().isEmpty()) {
            wrapper.eq(VolunteerRecord::getStatus, request.getStatus());
        }

        wrapper.orderByDesc(VolunteerRecord::getServiceDate);

        List<VolunteerRecord> records = this.list(wrapper);
        List<Map<String, Object>> result = enrichRecords(records);
        return Result.success(result);
    }

    @Override
    public Result<?> getStatsSummary(VolunteerDTOs.QueryRequest request) {
        User user = getCurrentUser();
        if (user == null) return Result.error("未认证");

        LambdaQueryWrapper<VolunteerRecord> wrapper = new LambdaQueryWrapper<>();

        if (request.getClubId() != null) {
            wrapper.eq(VolunteerRecord::getClubId, request.getClubId());
        } else if (RoleConstants.CLUB_LEADER.equals(user.getRole())) {
            wrapper.eq(VolunteerRecord::getClubId, user.getClubId());
        }
        if (request.getStartDate() != null) {
            wrapper.ge(VolunteerRecord::getServiceDate, request.getStartDate());
        }
        if (request.getEndDate() != null) {
            wrapper.le(VolunteerRecord::getServiceDate, request.getEndDate());
        }

        List<VolunteerRecord> records = this.list(wrapper);

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalRecords", records.size());
        summary.put("approvedRecords", records.stream().filter(r -> "APPROVED".equals(r.getStatus())).count());
        summary.put("pendingRecords", records.stream().filter(r -> "PENDING".equals(r.getStatus())).count());
        summary.put("rejectedRecords", records.stream().filter(r -> "REJECTED".equals(r.getStatus())).count());

        BigDecimal totalHours = records.stream()
                .filter(r -> "APPROVED".equals(r.getStatus()))
                .map(VolunteerRecord::getHours)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        summary.put("totalHours", totalHours);

        Set<Integer> participantIds = records.stream()
                .map(VolunteerRecord::getStudentId)
                .collect(Collectors.toSet());
        summary.put("participantCount", participantIds.size());

        Map<Integer, BigDecimal> clubHours = new HashMap<>();
        Map<Integer, Integer> clubRecords = new HashMap<>();
        for (VolunteerRecord r : records) {
            if ("APPROVED".equals(r.getStatus()) && r.getClubId() != null) {
                clubHours.merge(r.getClubId(), r.getHours(), BigDecimal::add);
                clubRecords.merge(r.getClubId(), 1, Integer::sum);
            }
        }

        List<Map<String, Object>> clubStats = new ArrayList<>();
        for (Map.Entry<Integer, BigDecimal> entry : clubHours.entrySet()) {
            Club club = clubMapper.selectById(entry.getKey());
            Map<String, Object> stat = new HashMap<>();
            stat.put("clubId", entry.getKey());
            stat.put("clubName", club != null ? club.getName() : "未知社团");
            stat.put("totalHours", entry.getValue());
            stat.put("recordCount", clubRecords.getOrDefault(entry.getKey(), 0));
            clubStats.add(stat);
        }
        clubStats.sort((a, b) -> ((BigDecimal) b.get("totalHours")).compareTo((BigDecimal) a.get("totalHours")));
        summary.put("clubStats", clubStats);

        Map<LocalDate, BigDecimal> dailyHours = new LinkedHashMap<>();
        LocalDate start = request.getStartDate() != null ? request.getStartDate() : LocalDate.now().minusMonths(1);
        LocalDate end = request.getEndDate() != null ? request.getEndDate() : LocalDate.now();
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            dailyHours.put(date, BigDecimal.ZERO);
        }
        for (VolunteerRecord r : records) {
            if ("APPROVED".equals(r.getStatus()) && dailyHours.containsKey(r.getServiceDate())) {
                dailyHours.merge(r.getServiceDate(), r.getHours(), BigDecimal::add);
            }
        }

        List<Map<String, Object>> trend = new ArrayList<>();
        for (Map.Entry<LocalDate, BigDecimal> entry : dailyHours.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("date", entry.getKey().toString());
            item.put("hours", entry.getValue());
            trend.add(item);
        }
        summary.put("trend", trend);

        return Result.success(summary);
    }

    @Override
    public Result<?> getUserStats(Integer userId) {
        User user = getCurrentUser();
        if (user == null) return Result.error("未认证");

        int targetUserId = userId != null ? userId : user.getId();

        VolunteerStat stat = volunteerStatMapper.selectOne(
                new LambdaQueryWrapper<VolunteerStat>().eq(VolunteerStat::getUserId, targetUserId));

        Map<String, Object> result = new HashMap<>();
        if (stat != null) {
            User targetUser = userMapper.selectById(targetUserId);
            result.put("userId", targetUserId);
            result.put("userName", targetUser != null ? targetUser.getRealName() : "未知用户");
            result.put("studentId", targetUser != null ? targetUser.getStudentId() : "未知");
            result.put("totalHours", stat.getTotalHours());
            result.put("approvedCount", stat.getApprovedCount());
            result.put("pendingCount", stat.getPendingCount());
            result.put("rejectedCount", stat.getRejectedCount());
            result.put("lastServiceDate", stat.getLastServiceDate());
        } else {
            result.put("userId", targetUserId);
            result.put("totalHours", BigDecimal.ZERO);
            result.put("approvedCount", 0);
            result.put("pendingCount", 0);
            result.put("rejectedCount", 0);
            result.put("lastServiceDate", null);
        }

        return Result.success(result);
    }

    @Override
    public Result<?> getClubStats() {
        User user = getCurrentUser();
        if (user == null) return Result.error("未认证");

        List<VolunteerStat> stats = volunteerStatMapper.selectList(null);
        List<Map<String, Object>> result = new ArrayList<>();

        for (VolunteerStat stat : stats) {
            User u = userMapper.selectById(stat.getUserId());
            if (u != null) {
                Map<String, Object> map = new HashMap<>();
                map.put("userId", stat.getUserId());
                map.put("userName", u.getRealName());
                map.put("studentId", u.getStudentId());
                map.put("totalHours", stat.getTotalHours());
                map.put("approvedCount", stat.getApprovedCount());
                map.put("pendingCount", stat.getPendingCount());
                map.put("rejectedCount", stat.getRejectedCount());
                map.put("lastServiceDate", stat.getLastServiceDate());
                result.add(map);
            }
        }

        result.sort((a, b) -> ((BigDecimal) b.get("totalHours")).compareTo((BigDecimal) a.get("totalHours")));
        return Result.success(result);
    }

    private List<Map<String, Object>> enrichRecords(List<VolunteerRecord> records) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (VolunteerRecord r : records) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", r.getId());
            map.put("studentId", r.getStudentId());
            map.put("activityName", r.getActivityName());
            map.put("serviceDate", r.getServiceDate());
            map.put("hours", r.getHours());
            map.put("proofUrl", r.getProofUrl());
            map.put("status", r.getStatus());
            map.put("clubId", r.getClubId());
            map.put("rejectReason", r.getRejectReason());
            map.put("createTime", r.getCreateTime());

            User student = userMapper.selectById(r.getStudentId());
            map.put("studentName", student != null ? student.getRealName() : "未知");
            map.put("studentNo", student != null ? student.getStudentId() : "未知");

            Club club = clubMapper.selectById(r.getClubId());
            map.put("clubName", club != null ? club.getName() : "全校");

            if (r.getAuditorId() != null) {
                User auditor = userMapper.selectById(r.getAuditorId());
                map.put("auditorName", auditor != null ? auditor.getRealName() : "未知");
            }

            result.add(map);
        }
        return result;
    }

    private List<VolunteerRecord> enrichRecordsForExport(List<VolunteerRecord> records) {
        for (VolunteerRecord r : records) {
            User student = userMapper.selectById(r.getStudentId());
            r.setStudentName(student != null ? student.getRealName() : "未知");
            r.setStudentNo(student != null ? student.getStudentId() : "未知");

            Club club = clubMapper.selectById(r.getClubId());
            r.setClubName(club != null ? club.getName() : "全校");

            if (r.getAuditorId() != null) {
                User auditor = userMapper.selectById(r.getAuditorId());
                r.setAuditorName(auditor != null ? auditor.getRealName() : "未知");
            }
        }
        return records;
    }

    @Transactional
    public void updateUserStats(Integer userId) {
        List<VolunteerRecord> records = this.list(new LambdaQueryWrapper<VolunteerRecord>()
                .eq(VolunteerRecord::getStudentId, userId));

        BigDecimal totalHours = records.stream()
                .filter(r -> "APPROVED".equals(r.getStatus()))
                .map(VolunteerRecord::getHours)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long approvedCount = records.stream().filter(r -> "APPROVED".equals(r.getStatus())).count();
        long pendingCount = records.stream().filter(r -> "PENDING".equals(r.getStatus())).count();
        long rejectedCount = records.stream().filter(r -> "REJECTED".equals(r.getStatus())).count();

        LocalDate lastServiceDate = records.stream()
                .filter(r -> "APPROVED".equals(r.getStatus()))
                .map(VolunteerRecord::getServiceDate)
                .max(LocalDate::compareTo)
                .orElse(null);

        VolunteerStat stat = volunteerStatMapper.selectOne(
                new LambdaQueryWrapper<VolunteerStat>().eq(VolunteerStat::getUserId, userId));

        if (stat == null) {
            stat = new VolunteerStat();
            stat.setUserId(userId);
            stat.setTotalHours(totalHours);
            stat.setApprovedCount((int) approvedCount);
            stat.setPendingCount((int) pendingCount);
            stat.setRejectedCount((int) rejectedCount);
            stat.setLastServiceDate(lastServiceDate);
            volunteerStatMapper.insert(stat);
        } else {
            stat.setTotalHours(totalHours);
            stat.setApprovedCount((int) approvedCount);
            stat.setPendingCount((int) pendingCount);
            stat.setRejectedCount((int) rejectedCount);
            stat.setLastServiceDate(lastServiceDate);
            volunteerStatMapper.updateById(stat);
        }
    }

    @Override
    public void exportRecords(VolunteerDTOs.QueryRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("志愿服务记录", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        LambdaQueryWrapper<VolunteerRecord> wrapper = new LambdaQueryWrapper<>();
        if (request.getClubId() != null) {
            wrapper.eq(VolunteerRecord::getClubId, request.getClubId());
        }
        if (request.getStartDate() != null) {
            wrapper.ge(VolunteerRecord::getServiceDate, request.getStartDate());
        }
        if (request.getEndDate() != null) {
            wrapper.le(VolunteerRecord::getServiceDate, request.getEndDate());
        }
        if (request.getStatus() != null && !request.getStatus().isEmpty()) {
            wrapper.eq(VolunteerRecord::getStatus, request.getStatus());
        }
        wrapper.orderByDesc(VolunteerRecord::getServiceDate);

        List<VolunteerRecord> list = this.list(wrapper);
        List<VolunteerRecord> exportList = enrichRecordsForExport(list);

        EasyExcel.write(response.getOutputStream(), VolunteerRecord.class)
                .sheet("志愿服务记录")
                .doWrite(exportList);
    }

    @Override
    public void exportStats(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("志愿服务统计", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        List<VolunteerStat> list = volunteerStatMapper.selectList(null);
        List<VolunteerStat> exportList = new ArrayList<>();
        for (VolunteerStat stat : list) {
            User u = userMapper.selectById(stat.getUserId());
            if (u != null) {
                stat.setStudentName(u.getRealName());
                stat.setStudentNo(u.getStudentId());
                exportList.add(stat);
            }
        }

        EasyExcel.write(response.getOutputStream(), VolunteerStat.class)
                .sheet("志愿服务统计")
                .doWrite(exportList);
    }
}
