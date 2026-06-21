package com.club.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.club.common.Result;
import com.club.entity.Venue;
import com.club.mapper.VenueMapper;
import com.club.service.VenueService;
import org.springframework.stereotype.Service;

@Service
public class VenueServiceImpl extends ServiceImpl<VenueMapper, Venue> implements VenueService {

    @Override
    public Result<?> createVenue(Venue venue) {
        venue.setStatus("AVAILABLE");
        boolean saved = this.save(venue);
        if (!saved) {
            return Result.error("场地创建失败");
        }
        return Result.success(null);
    }

    @Override
    public Result<?> updateVenue(Venue venue) {
        Venue existing = this.getById(venue.getId());
        if (existing == null) {
            return Result.error("场地不存在");
        }
        boolean updated = this.updateById(venue);
        if (!updated) {
            return Result.error("场地更新失败");
        }
        return Result.success(null);
    }

    @Override
    public Result<?> deleteVenue(Integer id) {
        Venue existing = this.getById(id);
        if (existing == null) {
            return Result.error("场地不存在");
        }
        boolean deleted = this.removeById(id);
        if (!deleted) {
            return Result.error("场地删除失败");
        }
        return Result.success(null);
    }

    @Override
    public Result<?> listVenues() {
        return Result.success(this.list(new LambdaQueryWrapper<Venue>().orderByAsc(Venue::getId)));
    }

    @Override
    public Result<?> getVenueById(Integer id) {
        Venue venue = this.getById(id);
        if (venue == null) {
            return Result.error("场地不存在");
        }
        return Result.success(venue);
    }
}
