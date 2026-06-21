package com.club.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.club.common.Result;
import com.club.common.RoleConstants;
import com.club.dto.InspectionDTOs;
import com.club.entity.*;
import com.club.mapper.*;
import com.club.service.InspectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class InspectionServiceImpl extends ServiceImpl<InspectionPlanMapper, InspectionPlan> implements InspectionService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ClubMapper clubMapper;

    @Autowired
    private DeviceMapper deviceMapper;

    @Autowired
    private InspectionRecordMapper inspectionRecordMapper;

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) return null;
        return userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, auth.getName()));
    }

    private boolean canManageClub(User user, Integer clubId) {
        if (user == null) return false;
        if (RoleConstants.ADMIN.equals(user.getRole()) || RoleConstants.UNION_ADMIN.equals(user.getRole())) return true;
        if (RoleConstants.CLUB_LEADER.equals(user.getRole()) && user.getClubId() != null && user.getClubId().equals(clubId)) return true;
        return false;
    }

    private void enrichPlan(InspectionPlan plan) {
        if (plan.getDeviceId() != null) {
            Device device = deviceMapper.selectById(plan.getDeviceId());
            if (device != null) {
                plan.setDeviceName(device.getName());
                plan.setDeviceNo(device.getDeviceNo());
                plan.setDeviceLocation(device.getLocation());
                plan.setClubId(device.getClubId());
                if (device.getClubId() != null) {
                    Club club = clubMapper.selectById(device.getClubId());
                    if (club != null) {
                        plan.setClubName(club.getName());
                    }
                }
            }
        }
        if (plan.getOwnerId() != null) {
            User owner = userMapper.selectById(plan.getOwnerId());
            if (owner != null) {
                plan.setOwnerName(owner.getRealName());
            }
        }
        if (plan.getInspectorId() != null) {
            User inspector = userMapper.selectById(plan.getInspectorId());
            if (inspector != null) {
                plan.setInspectorName(inspector.getRealName());
            }
        }
        if (plan.getNextInspectDate() != null) {
            plan.setIsOverdue(plan.getNextInspectDate().isBefore(LocalDate.now()) && "ACTIVE".equals(plan.getStatus()));
        } else {
            plan.setIsOverdue(false);
        }
    }

    private void enrichRecord(InspectionRecord record) {
        if (record.getPlanId() != null) {
            InspectionPlan plan = this.getById(record.getPlanId());
            if (plan != null && plan.getDeviceId() != null) {
                Device device = deviceMapper.selectById(plan.getDeviceId());
                if (device != null) {
                    record.setDeviceName(device.getName());
                    record.setDeviceNo(device.getDeviceNo());
                    record.setDeviceLocation(device.getLocation());
                    record.setClubId(device.getClubId());
                }
            }
        }
        if (record.getInspectorId() != null) {
            User inspector = userMapper.selectById(record.getInspectorId());
            if (inspector != null) {
                record.setInspectorName(inspector.getRealName());
            }
        }
    }

    @Override
    @Transactional
    public Result<?> createPlan(InspectionDTOs.PlanCreateRequest request) {
        User user = getCurrentUser();
        if (user == null) return Result.error("未认证");

        if (request.getCycleDays() == null && (request.getCronExpr() == null || request.getCronExpr().isBlank())) {
            return Result.error("必须指定巡检周期天数或Cron表达式");
        }

        Device device = deviceMapper.selectById(request.getDeviceId());
        if (device == null) {
            return Result.error("设备不存在");
        }

        if (!canManageClub(user, device.getClubId())) {
            return Result.error("无权限为此设备创建巡检计划");
        }

        User inspector = userMapper.selectById(request.getInspectorId());
        if (inspector == null) {
            return Result.error("巡检人不存在");
        }
        if (inspector.getClubId() != null && !inspector.getClubId().equals(device.getClubId())) {
            if (!RoleConstants.ADMIN.equals(inspector.getRole()) && !RoleConstants.UNION_ADMIN.equals(inspector.getRole())) {
                return Result.error("巡检人必须属于该社团或为管理员");
            }
        }

        InspectionPlan plan = new InspectionPlan();
        plan.setDeviceId(request.getDeviceId());
        plan.setCycleDays(request.getCycleDays());
        plan.setCronExpr(request.getCronExpr());
        plan.setOwnerId(user.getId());
        plan.setInspectorId(request.getInspectorId());
        plan.setStatus("ACTIVE");

        if (request.getNextInspectDate() != null) {
            plan.setNextInspectDate(request.getNextInspectDate());
        } else if (request.getCycleDays() != null) {
            plan.setNextInspectDate(LocalDate.now().plusDays(request.getCycleDays()));
        } else {
            plan.setNextInspectDate(LocalDate.now());
        }

        boolean saved = this.save(plan);
        if (!saved) {
            return Result.error("巡检计划创建失败");
        }
        return Result.success(plan);
    }

    @Override
    @Transactional
    public Result<?> updatePlan(InspectionDTOs.PlanUpdateRequest request) {
        User user = getCurrentUser();
        if (user == null) return Result.error("未认证");

        InspectionPlan existing = this.getById(request.getId());
        if (existing == null) {
            return Result.error("巡检计划不存在");
        }

        Device device = deviceMapper.selectById(existing.getDeviceId());
        if (device == null || !canManageClub(user, device.getClubId())) {
            return Result.error("无权限修改此巡检计划");
        }

        if (request.getInspectorId() != null) {
            User inspector = userMapper.selectById(request.getInspectorId());
            if (inspector == null) {
                return Result.error("巡检人不存在");
            }
            existing.setInspectorId(request.getInspectorId());
        }

        if (request.getCycleDays() != null) existing.setCycleDays(request.getCycleDays());
        if (request.getCronExpr() != null) existing.setCronExpr(request.getCronExpr());
        if (request.getNextInspectDate() != null) existing.setNextInspectDate(request.getNextInspectDate());
        if (request.getStatus() != null) existing.setStatus(request.getStatus());

        boolean updated = this.updateById(existing);
        if (!updated) {
            return Result.error("巡检计划更新失败");
        }
        return Result.success(null);
    }

    @Override
    @Transactional
    public Result<?> deletePlan(Integer id) {
        User user = getCurrentUser();
        if (user == null) return Result.error("未认证");

        InspectionPlan existing = this.getById(id);
        if (existing == null) {
            return Result.error("巡检计划不存在");
        }

        Device device = deviceMapper.selectById(existing.getDeviceId());
        if (device == null || !canManageClub(user, device.getClubId())) {
            return Result.error("无权限删除此巡检计划");
        }

        boolean deleted = this.removeById(id);
        if (!deleted) {
            return Result.error("巡检计划删除失败");
        }
        return Result.success(null);
    }

    @Override
    public Result<?> listPlans() {
        User user = getCurrentUser();
        if (user == null) return Result.error("未认证");

        LambdaQueryWrapper<InspectionPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(InspectionPlan::getNextInspectDate);

        List<InspectionPlan> allPlans = this.list(wrapper);
        List<InspectionPlan> filteredPlans = new ArrayList<>();

        for (InspectionPlan plan : allPlans) {
            enrichPlan(plan);
            if (RoleConstants.ADMIN.equals(user.getRole()) || RoleConstants.UNION_ADMIN.equals(user.getRole())) {
                filteredPlans.add(plan);
            } else if (user.getClubId() != null && plan.getClubId() != null && user.getClubId().equals(plan.getClubId())) {
                filteredPlans.add(plan);
            }
        }

        filteredPlans.sort((a, b) -> {
            if (a.getNextInspectDate() == null) return 1;
            if (b.getNextInspectDate() == null) return -1;
            return a.getNextInspectDate().compareTo(b.getNextInspectDate());
        });

        return Result.success(filteredPlans);
    }

    @Override
    public Result<?> getPlanById(Integer id) {
        User user = getCurrentUser();
        if (user == null) return Result.error("未认证");

        InspectionPlan plan = this.getById(id);
        if (plan == null) {
            return Result.error("巡检计划不存在");
        }
        enrichPlan(plan);

        if (RoleConstants.ADMIN.equals(user.getRole()) || RoleConstants.UNION_ADMIN.equals(user.getRole())) {
            return Result.success(plan);
        }
        if (user.getClubId() != null && plan.getClubId() != null && user.getClubId().equals(plan.getClubId())) {
            return Result.success(plan);
        }
        if (user.getId().equals(plan.getInspectorId())) {
            return Result.success(plan);
        }
        return Result.error("无权限查看此巡检计划");
    }

    @Override
    public Result<?> getMyTodoPlans() {
        User user = getCurrentUser();
        if (user == null) return Result.error("未认证");

        LambdaQueryWrapper<InspectionPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InspectionPlan::getInspectorId, user.getId())
                .eq(InspectionPlan::getStatus, "ACTIVE")
                .orderByAsc(InspectionPlan::getNextInspectDate);

        List<InspectionPlan> plans = this.list(wrapper);
        for (InspectionPlan plan : plans) {
            enrichPlan(plan);
        }
        return Result.success(plans);
    }

    @Override
    @Transactional
    public Result<?> submitRecord(InspectionDTOs.RecordSubmitRequest request) {
        User user = getCurrentUser();
        if (user == null) return Result.error("未认证");

        InspectionPlan plan = this.getById(request.getPlanId());
        if (plan == null) {
            return Result.error("巡检计划不存在");
        }

        if (!"ACTIVE".equals(plan.getStatus())) {
            return Result.error("该巡检计划未启用");
        }

        if (!plan.getInspectorId().equals(user.getId())) {
            Device device = deviceMapper.selectById(plan.getDeviceId());
            if (device == null || !canManageClub(user, device.getClubId())) {
                return Result.error("无权限为此计划提交巡检记录");
            }
        }

        InspectionRecord record = new InspectionRecord();
        record.setPlanId(request.getPlanId());
        record.setResult(request.getResult());
        record.setRemark(request.getRemark());
        record.setInspectTime(request.getInspectTime());
        record.setInspectorId(user.getId());

        inspectionRecordMapper.insert(record);

        plan.setLastInspectTime(request.getInspectTime());
        if (plan.getCycleDays() != null) {
            plan.setNextInspectDate(LocalDate.now().plusDays(plan.getCycleDays()));
        }
        this.updateById(plan);

        return Result.success(record);
    }

    @Override
    public Result<?> listRecordsByPlan(Integer planId) {
        User user = getCurrentUser();
        if (user == null) return Result.error("未认证");

        InspectionPlan plan = this.getById(planId);
        if (plan == null) {
            return Result.error("巡检计划不存在");
        }
        Device device = deviceMapper.selectById(plan.getDeviceId());
        if (device == null) {
            return Result.error("设备不存在");
        }

        if (RoleConstants.ADMIN.equals(user.getRole()) || RoleConstants.UNION_ADMIN.equals(user.getRole())) {
        } else if (user.getClubId() != null && user.getClubId().equals(device.getClubId())) {
        } else if (user.getId().equals(plan.getInspectorId())) {
        } else {
            return Result.error("无权限查看此计划的巡检记录");
        }

        List<InspectionRecord> records = inspectionRecordMapper.selectList(
                new LambdaQueryWrapper<InspectionRecord>()
                        .eq(InspectionRecord::getPlanId, planId)
                        .orderByDesc(InspectionRecord::getInspectTime)
        );
        for (InspectionRecord record : records) {
            enrichRecord(record);
        }
        return Result.success(records);
    }

    @Override
    public Result<?> getStatistics() {
        User user = getCurrentUser();
        if (user == null) return Result.error("未认证");

        List<InspectionPlan> allPlans = this.list(new LambdaQueryWrapper<>());
        List<InspectionPlan> filteredPlans = new ArrayList<>();
        Set<Integer> accessibleClubIds = new HashSet<>();

        for (InspectionPlan plan : allPlans) {
            enrichPlan(plan);
            if (RoleConstants.ADMIN.equals(user.getRole()) || RoleConstants.UNION_ADMIN.equals(user.getRole())) {
                filteredPlans.add(plan);
                if (plan.getClubId() != null) accessibleClubIds.add(plan.getClubId());
            } else if (user.getClubId() != null && plan.getClubId() != null && user.getClubId().equals(plan.getClubId())) {
                filteredPlans.add(plan);
                accessibleClubIds.add(plan.getClubId());
            } else if (user.getId().equals(plan.getInspectorId())) {
                filteredPlans.add(plan);
                if (plan.getClubId() != null) accessibleClubIds.add(plan.getClubId());
            }
        }

        LambdaQueryWrapper<Device> deviceWrapper = new LambdaQueryWrapper<>();
        if (!(RoleConstants.ADMIN.equals(user.getRole()) || RoleConstants.UNION_ADMIN.equals(user.getRole()))) {
            if (user.getClubId() != null) {
                deviceWrapper.eq(Device::getClubId, user.getClubId());
            } else {
                deviceWrapper.eq(Device::getClubId, -1);
            }
        }
        long deviceCount = deviceMapper.selectCount(deviceWrapper);

        long activePlanCount = filteredPlans.stream().filter(p -> "ACTIVE".equals(p.getStatus())).count();
        long overdueCount = filteredPlans.stream().filter(p -> Boolean.TRUE.equals(p.getIsOverdue())).count();
        long todayCount = filteredPlans.stream()
                .filter(p -> p.getNextInspectDate() != null && p.getNextInspectDate().equals(LocalDate.now()) && "ACTIVE".equals(p.getStatus()))
                .count();

        LambdaQueryWrapper<InspectionRecord> recordWrapper = new LambdaQueryWrapper<>();
        List<InspectionRecord> allRecords = inspectionRecordMapper.selectList(recordWrapper);
        List<InspectionRecord> filteredRecords = new ArrayList<>();
        for (InspectionRecord record : allRecords) {
            enrichRecord(record);
            if (record.getClubId() != null && accessibleClubIds.contains(record.getClubId())) {
                filteredRecords.add(record);
            }
        }
        long abnormalCount = filteredRecords.stream().filter(r -> "ABNORMAL".equals(r.getResult())).count();
        long totalRecordCount = filteredRecords.size();

        Map<String, Object> stats = new HashMap<>();
        stats.put("deviceCount", deviceCount);
        stats.put("activePlanCount", activePlanCount);
        stats.put("overdueCount", overdueCount);
        stats.put("todayCount", todayCount);
        stats.put("abnormalCount", abnormalCount);
        stats.put("totalRecordCount", totalRecordCount);

        return Result.success(stats);
    }

    @Override
    public Result<?> listMyRecords() {
        User user = getCurrentUser();
        if (user == null) return Result.error("未认证");

        List<InspectionRecord> records = inspectionRecordMapper.selectList(
                new LambdaQueryWrapper<InspectionRecord>()
                        .eq(InspectionRecord::getInspectorId, user.getId())
                        .orderByDesc(InspectionRecord::getInspectTime)
        );
        for (InspectionRecord record : records) {
            enrichRecord(record);
        }
        return Result.success(records);
    }
}
