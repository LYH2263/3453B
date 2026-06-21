package com.club.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.club.common.Result;
import com.club.entity.Club;
import com.club.entity.ClubLocation;
import com.club.mapper.ClubLocationMapper;
import com.club.mapper.ClubMapper;
import com.club.service.ClubLocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClubLocationServiceImpl extends ServiceImpl<ClubLocationMapper, ClubLocation> implements ClubLocationService {

    @Autowired
    private ClubMapper clubMapper;

    @Override
    public Result<?> createLocation(ClubLocation location) {
        if (location.getClubId() != null) {
            Club club = clubMapper.selectById(location.getClubId());
            if (club == null) {
                return Result.error("所属社团不存在");
            }
        }
        boolean saved = this.save(location);
        if (!saved) {
            return Result.error("据点创建失败");
        }
        return Result.success(null);
    }

    @Override
    public Result<?> updateLocation(ClubLocation location) {
        ClubLocation existing = this.getById(location.getId());
        if (existing == null) {
            return Result.error("据点不存在");
        }
        if (location.getClubId() != null) {
            Club club = clubMapper.selectById(location.getClubId());
            if (club == null) {
                return Result.error("所属社团不存在");
            }
        }
        boolean updated = this.updateById(location);
        if (!updated) {
            return Result.error("据点更新失败");
        }
        return Result.success(null);
    }

    @Override
    public Result<?> deleteLocation(Integer id) {
        ClubLocation existing = this.getById(id);
        if (existing == null) {
            return Result.error("据点不存在");
        }
        boolean deleted = this.removeById(id);
        if (!deleted) {
            return Result.error("据点删除失败");
        }
        return Result.success(null);
    }

    @Override
    public Result<?> listLocations(String clubName, String building) {
        LambdaQueryWrapper<ClubLocation> wrapper = new LambdaQueryWrapper<>();

        if (building != null && !building.trim().isEmpty()) {
            wrapper.eq(ClubLocation::getBuilding, building);
        }
        wrapper.orderByAsc(ClubLocation::getBuilding, ClubLocation::getFloor);

        List<ClubLocation> locations = this.list(wrapper);

        if (clubName != null && !clubName.trim().isEmpty()) {
            List<Integer> matchedClubIds = clubMapper.selectList(
                new LambdaQueryWrapper<Club>().like(Club::getName, clubName)
            ).stream().map(Club::getId).collect(Collectors.toList());

            locations = locations.stream()
                .filter(loc -> loc.getClubId() != null && matchedClubIds.contains(loc.getClubId()))
                .collect(Collectors.toList());
        }

        for (ClubLocation loc : locations) {
            if (loc.getClubId() != null) {
                Club club = clubMapper.selectById(loc.getClubId());
                if (club != null) {
                    Club clubSimple = new Club();
                    clubSimple.setId(club.getId());
                    clubSimple.setName(club.getName());
                    clubSimple.setLogo(club.getLogo());
                    clubSimple.setDescription(club.getDescription());
                    loc.setClub(clubSimple);
                }
            }
        }

        return Result.success(locations);
    }

    @Override
    public Result<?> getLocationById(Integer id) {
        ClubLocation location = this.getById(id);
        if (location == null) {
            return Result.error("据点不存在");
        }
        if (location.getClubId() != null) {
            Club club = clubMapper.selectById(location.getClubId());
            if (club != null) {
                Club clubSimple = new Club();
                clubSimple.setId(club.getId());
                clubSimple.setName(club.getName());
                clubSimple.setLogo(club.getLogo());
                clubSimple.setDescription(club.getDescription());
                location.setClub(clubSimple);
            }
        }
        return Result.success(location);
    }

    @Override
    public Result<?> listBuildings() {
        List<String> buildings = this.list(new LambdaQueryWrapper<ClubLocation>()
                .select(ClubLocation::getBuilding)
                .groupBy(ClubLocation::getBuilding)
                .orderByAsc(ClubLocation::getBuilding))
            .stream()
            .map(ClubLocation::getBuilding)
            .distinct()
            .collect(Collectors.toList());
        return Result.success(buildings);
    }

    @Override
    public Result<?> deleteByClubId(Integer clubId) {
        LambdaQueryWrapper<ClubLocation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ClubLocation::getClubId, clubId);
        this.remove(wrapper);
        return Result.success(null);
    }
}
