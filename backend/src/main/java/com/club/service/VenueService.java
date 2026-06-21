package com.club.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.club.common.Result;
import com.club.entity.Venue;

public interface VenueService extends IService<Venue> {
    Result<?> createVenue(Venue venue);
    Result<?> updateVenue(Venue venue);
    Result<?> deleteVenue(Integer id);
    Result<?> listVenues();
    Result<?> getVenueById(Integer id);
}
