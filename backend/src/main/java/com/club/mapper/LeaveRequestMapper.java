package com.club.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.club.entity.ActivityLeaveRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface LeaveRequestMapper extends BaseMapper<ActivityLeaveRequest> {

    @Select("SELECT lr.*, a.title AS activity_title, u.real_name AS user_name, " +
            "au.real_name AS approver_name " +
            "FROM activity_leave_requests lr " +
            "LEFT JOIN activities a ON lr.activity_id = a.id " +
            "LEFT JOIN users u ON lr.user_id = u.id " +
            "LEFT JOIN users au ON lr.approver_id = au.id " +
            "WHERE lr.user_id = #{userId} " +
            "ORDER BY lr.create_time DESC")
    List<ActivityLeaveRequest> selectByUserId(Integer userId);

    @Select("SELECT lr.*, a.title AS activity_title, u.real_name AS user_name, " +
            "au.real_name AS approver_name " +
            "FROM activity_leave_requests lr " +
            "LEFT JOIN activities a ON lr.activity_id = a.id " +
            "LEFT JOIN users u ON lr.user_id = u.id " +
            "LEFT JOIN users au ON lr.approver_id = au.id " +
            "WHERE lr.club_id = #{clubId} AND lr.status = 'PENDING' " +
            "ORDER BY lr.create_time DESC")
    List<ActivityLeaveRequest> selectPendingByClubId(Integer clubId);

    @Select("SELECT lr.*, a.title AS activity_title, u.real_name AS user_name, " +
            "au.real_name AS approver_name " +
            "FROM activity_leave_requests lr " +
            "LEFT JOIN activities a ON lr.activity_id = a.id " +
            "LEFT JOIN users u ON lr.user_id = u.id " +
            "LEFT JOIN users au ON lr.approver_id = au.id " +
            "WHERE lr.club_id = #{clubId} " +
            "ORDER BY lr.create_time DESC")
    List<ActivityLeaveRequest> selectByClubId(Integer clubId);
}
