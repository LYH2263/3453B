package com.club.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.club.common.Result;
import com.club.dto.MentorshipDTOs;
import com.club.entity.ClubMentor;

public interface ClubMentorService extends IService<ClubMentor> {
    Result<?> createMentor(ClubMentor mentor);
    Result<?> updateMentor(ClubMentor mentor);
    Result<?> deleteMentor(Integer id);
    Result<?> listMentors(Integer clubId);
    Result<?> getMentorById(Integer id);
}
