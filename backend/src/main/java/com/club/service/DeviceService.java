package com.club.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.club.common.Result;
import com.club.entity.Device;

public interface DeviceService extends IService<Device> {
    Result<?> createDevice(Device device);
    Result<?> updateDevice(Device device);
    Result<?> deleteDevice(Integer id);
    Result<?> listDevices();
    Result<?> getDeviceById(Integer id);
}
