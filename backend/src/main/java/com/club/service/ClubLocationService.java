package com.club.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.club.common.Result;
import com.club.entity.ClubLocation;

public interface ClubLocationService extends IService<ClubLocation> {
    Result<?> createLocation(ClubLocation location);
    Result<?> updateLocation(ClubLocation location);
    Result<?> deleteLocation(Integer id);
    Result<?> listLocations(String clubName, String building);
    Result<?> getLocationById(Integer id);
    Result<?> listBuildings();
    Result<?> deleteByClubId(Integer clubId);
}
