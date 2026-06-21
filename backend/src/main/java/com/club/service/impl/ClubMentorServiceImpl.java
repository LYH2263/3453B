package com.club.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.club.common.Result;
import com.club.common.RoleConstants;
import com.club.dto.MentorshipDTOs;
import com.club.entity.Club;
import com.club.entity.ClubMentor;
import com.club.entity.User;
import com.club.mapper.ClubMapper;
import com.club.mapper.ClubMentorMapper;
import com.club.mapper.UserMapper;
import com.club.service.ClubMentorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClubMentorServiceImpl extends ServiceImpl<ClubMentorMapper, ClubMentor> implements ClubMentorService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ClubMapper clubMapper;

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getName() != null) {
            return userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, auth.getName()));
        }
        return null;
    }

    private MentorshipDTOs.MentorVO toVO(ClubMentor mentor) {
        MentorshipDTOs.MentorVO vo = new MentorshipDTOs.MentorVO();
        vo.setId(mentor.getId());
        vo.setClubId(mentor.getClubId());
        vo.setName(mentor.getName());
        vo.setStaffNo(mentor.getStaffNo());
        vo.setResearchArea(mentor.getResearchArea());
        vo.setIntro(mentor.getIntro());
        vo.setUserId(mentor.getUserId());
        vo.setCreateTime(mentor.getCreateTime());

        Club club = clubMapper.selectById(mentor.getClubId());
        if (club != null) {
            vo.setClubName(club.getName());
        }

        if (mentor.getUserId() != null) {
            User linkedUser = userMapper.selectById(mentor.getUserId());
            if (linkedUser != null) {
                vo.setLinkedUsername(linkedUser.getRealName());
            }
        }

        return vo;
    }

    @Override
    public Result<?> createMentor(ClubMentor mentor) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return Result.error("尚未登录");
        }

        boolean isAdmin = RoleConstants.ADMIN.equals(currentUser.getRole())
                || RoleConstants.UNION_ADMIN.equals(currentUser.getRole());
        boolean isLeader = RoleConstants.CLUB_LEADER.equals(currentUser.getRole());

        if (!isAdmin && !isLeader) {
            return Result.error("仅社团负责人或管理员可添加导师");
        }

        if (isLeader) {
            if (currentUser.getClubId() == null) {
                return Result.error("您尚未绑定任何社团");
            }
            mentor.setClubId(currentUser.getClubId());
        }

        if (mentor.getUserId() != null) {
            User linkedUser = userMapper.selectById(mentor.getUserId());
            if (linkedUser == null) {
                return Result.error("关联用户不存在");
            }
        }

        boolean saved = this.save(mentor);
        if (!saved) {
            return Result.error("导师添加失败");
        }
        return Result.success(null);
    }

    @Override
    public Result<?> updateMentor(ClubMentor mentor) {
        ClubMentor existing = this.getById(mentor.getId());
        if (existing == null) {
            return Result.error("导师不存在");
        }

        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return Result.error("尚未登录");
        }

        boolean isAdmin = RoleConstants.ADMIN.equals(currentUser.getRole())
                || RoleConstants.UNION_ADMIN.equals(currentUser.getRole());
        boolean isLeader = RoleConstants.CLUB_LEADER.equals(currentUser.getRole())
                && currentUser.getClubId() != null
                && currentUser.getClubId().equals(existing.getClubId());

        if (!isAdmin && !isLeader) {
            return Result.error("无权限修改该导师");
        }

        if (mentor.getUserId() != null) {
            User linkedUser = userMapper.selectById(mentor.getUserId());
            if (linkedUser == null) {
                return Result.error("关联用户不存在");
            }
        }

        boolean updated = this.updateById(mentor);
        if (!updated) {
            return Result.error("导师更新失败");
        }
        return Result.success(null);
    }

    @Override
    public Result<?> deleteMentor(Integer id) {
        ClubMentor existing = this.getById(id);
        if (existing == null) {
            return Result.error("导师不存在");
        }

        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return Result.error("尚未登录");
        }

        boolean isAdmin = RoleConstants.ADMIN.equals(currentUser.getRole())
                || RoleConstants.UNION_ADMIN.equals(currentUser.getRole());
        boolean isLeader = RoleConstants.CLUB_LEADER.equals(currentUser.getRole())
                && currentUser.getClubId() != null
                && currentUser.getClubId().equals(existing.getClubId());

        if (!isAdmin && !isLeader) {
            return Result.error("无权限删除该导师");
        }

        boolean deleted = this.removeById(id);
        if (!deleted) {
            return Result.error("导师删除失败");
        }
        return Result.success(null);
    }

    @Override
    public Result<?> listMentors(Integer clubId) {
        LambdaQueryWrapper<ClubMentor> wrapper = new LambdaQueryWrapper<>();
        if (clubId != null) {
            wrapper.eq(ClubMentor::getClubId, clubId);
        }
        wrapper.orderByDesc(ClubMentor::getCreateTime);

        List<ClubMentor> mentors = this.list(wrapper);
        List<MentorshipDTOs.MentorVO> voList = mentors.stream()
                .map(this::toVO)
                .collect(Collectors.toList());

        return Result.success(voList);
    }

    @Override
    public Result<?> getMentorById(Integer id) {
        ClubMentor mentor = this.getById(id);
        if (mentor == null) {
            return Result.error("导师不存在");
        }
        return Result.success(toVO(mentor));
    }
}
