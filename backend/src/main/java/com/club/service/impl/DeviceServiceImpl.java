package com.club.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.club.common.Result;
import com.club.common.RoleConstants;
import com.club.entity.Club;
import com.club.entity.Device;
import com.club.entity.User;
import com.club.mapper.ClubMapper;
import com.club.mapper.DeviceMapper;
import com.club.mapper.UserMapper;
import com.club.service.DeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DeviceServiceImpl extends ServiceImpl<DeviceMapper, Device> implements DeviceService {

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
        if (RoleConstants.ADMIN.equals(user.getRole()) || RoleConstants.UNION_ADMIN.equals(user.getRole())) return true;
        if (RoleConstants.CLUB_LEADER.equals(user.getRole()) && user.getClubId() != null && user.getClubId().equals(clubId)) return true;
        return false;
    }

    @Override
    public Result<?> createDevice(Device device) {
        User user = getCurrentUser();
        if (user == null) return Result.error("未认证");

        if (device.getClubId() == null) {
            if (user.getClubId() != null) {
                device.setClubId(user.getClubId());
            } else {
                return Result.error("必须指定所属社团");
            }
        }

        if (!canManageClub(user, device.getClubId())) {
            return Result.error("无权限在此社团创建设备");
        }

        long count = this.count(new LambdaQueryWrapper<Device>()
                .eq(Device::getClubId, device.getClubId())
                .eq(Device::getDeviceNo, device.getDeviceNo()));
        if (count > 0) {
            return Result.error("该社团下设备编号已存在");
        }

        boolean saved = this.save(device);
        if (!saved) {
            return Result.error("设备创建失败");
        }
        return Result.success(device);
    }

    @Override
    public Result<?> updateDevice(Device device) {
        User user = getCurrentUser();
        if (user == null) return Result.error("未认证");

        Device existing = this.getById(device.getId());
        if (existing == null) {
            return Result.error("设备不存在");
        }

        if (!canManageClub(user, existing.getClubId())) {
            return Result.error("无权限修改此设备");
        }

        if (device.getClubId() != null && !device.getClubId().equals(existing.getClubId())) {
            if (!canManageClub(user, device.getClubId())) {
                return Result.error("无权限将设备转移到此社团");
            }
        } else {
            device.setClubId(existing.getClubId());
        }

        if (device.getDeviceNo() != null && !device.getDeviceNo().equals(existing.getDeviceNo())) {
            long count = this.count(new LambdaQueryWrapper<Device>()
                    .eq(Device::getClubId, device.getClubId())
                    .eq(Device::getDeviceNo, device.getDeviceNo())
                    .ne(Device::getId, device.getId()));
            if (count > 0) {
                return Result.error("该社团下设备编号已存在");
            }
        }

        boolean updated = this.updateById(device);
        if (!updated) {
            return Result.error("设备更新失败");
        }
        return Result.success(null);
    }

    @Override
    public Result<?> deleteDevice(Integer id) {
        User user = getCurrentUser();
        if (user == null) return Result.error("未认证");

        Device existing = this.getById(id);
        if (existing == null) {
            return Result.error("设备不存在");
        }

        if (!canManageClub(user, existing.getClubId())) {
            return Result.error("无权限删除此设备");
        }

        boolean deleted = this.removeById(id);
        if (!deleted) {
            return Result.error("设备删除失败");
        }
        return Result.success(null);
    }

    @Override
    public Result<?> listDevices() {
        User user = getCurrentUser();
        if (user == null) return Result.error("未认证");

        LambdaQueryWrapper<Device> wrapper = new LambdaQueryWrapper<>();
        if (RoleConstants.ADMIN.equals(user.getRole()) || RoleConstants.UNION_ADMIN.equals(user.getRole())) {
        } else if (user.getClubId() != null) {
            wrapper.eq(Device::getClubId, user.getClubId());
        } else {
            return Result.success(List.of());
        }
        wrapper.orderByDesc(Device::getId);

        List<Device> devices = this.list(wrapper);
        for (Device d : devices) {
            if (d.getClubId() != null) {
                Club club = clubMapper.selectById(d.getClubId());
                if (club != null) {
                    d.setClubName(club.getName());
                }
            }
        }
        return Result.success(devices);
    }

    @Override
    public Result<?> getDeviceById(Integer id) {
        User user = getCurrentUser();
        if (user == null) return Result.error("未认证");

        Device device = this.getById(id);
        if (device == null) {
            return Result.error("设备不存在");
        }

        if (RoleConstants.ADMIN.equals(user.getRole()) || RoleConstants.UNION_ADMIN.equals(user.getRole())) {
        } else if (user.getClubId() != null && user.getClubId().equals(device.getClubId())) {
        } else {
            return Result.error("无权限查看此设备");
        }

        if (device.getClubId() != null) {
            Club club = clubMapper.selectById(device.getClubId());
            if (club != null) {
                device.setClubName(club.getName());
            }
        }
        return Result.success(device);
    }
}
