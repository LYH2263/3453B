package com.club.controller;

import com.club.common.Result;
import com.club.entity.ClubLocation;
import com.club.service.ClubLocationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/club-locations")
public class ClubLocationController {

    @Autowired
    private ClubLocationService clubLocationService;

    @GetMapping
    public Result<?> listLocations(
            @RequestParam(required = false) String clubName,
            @RequestParam(required = false) String building) {
        return clubLocationService.listLocations(clubName, building);
    }

    @GetMapping("/{id}")
    public Result<?> getLocationById(@PathVariable Integer id) {
        return clubLocationService.getLocationById(id);
    }

    @GetMapping("/buildings")
    public Result<?> listBuildings() {
        return clubLocationService.listBuildings();
    }

    @PostMapping
    public Result<?> createLocation(@Valid @RequestBody ClubLocation location) {
        return clubLocationService.createLocation(location);
    }

    @PutMapping
    public Result<?> updateLocation(@Valid @RequestBody ClubLocation location) {
        return clubLocationService.updateLocation(location);
    }

    @DeleteMapping("/{id}")
    public Result<?> deleteLocation(@PathVariable Integer id) {
        return clubLocationService.deleteLocation(id);
    }
}
