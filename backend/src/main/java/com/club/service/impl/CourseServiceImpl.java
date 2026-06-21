package com.club.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.club.common.Result;
import com.club.common.RoleConstants;
import com.club.dto.CourseDTOs;
import com.club.entity.*;
import com.club.mapper.*;
import com.club.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course> implements CourseService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ClubMapper clubMapper;

    @Autowired
    private CourseChapterMapper courseChapterMapper;

    @Autowired
    private ChapterProgressMapper chapterProgressMapper;

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) return null;
        return userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, auth.getName()));
    }

    private boolean canManageCourse(User user, Course course) {
        if (RoleConstants.ADMIN.equals(user.getRole()) || RoleConstants.UNION_ADMIN.equals(user.getRole())) {
            return true;
        }
        if (RoleConstants.CLUB_LEADER.equals(user.getRole())) {
            return course.getClubId() != null && course.getClubId().equals(user.getClubId());
        }
        return false;
    }

    private boolean canLearnCourse(User user, Course course) {
        if (RoleConstants.ADMIN.equals(user.getRole()) || RoleConstants.UNION_ADMIN.equals(user.getRole())) {
            return true;
        }
        if (RoleConstants.CLUB_LEADER.equals(user.getRole()) || RoleConstants.MEMBER.equals(user.getRole())) {
            return course.getClubId() != null && course.getClubId().equals(user.getClubId());
        }
        return false;
    }

    @Override
    @Transactional
    public Result<?> createCourse(CourseDTOs.CourseCreateRequest request) {
        User user = getCurrentUser();
        if (user == null) return Result.error("未认证");

        if (!(RoleConstants.ADMIN.equals(user.getRole()) || RoleConstants.UNION_ADMIN.equals(user.getRole())
                || RoleConstants.CLUB_LEADER.equals(user.getRole()))) {
            return Result.error("无权限创建课程");
        }

        if (RoleConstants.CLUB_LEADER.equals(user.getRole())) {
            if (request.getClubId() == null || !request.getClubId().equals(user.getClubId())) {
                return Result.error("仅可为本社团创建课程");
            }
        }

        Course course = new Course();
        course.setClubId(request.getClubId());
        course.setTitle(request.getTitle());
        course.setDescription(request.getDescription());
        course.setCover(request.getCover());
        this.save(course);

        return Result.success(course);
    }

    @Override
    @Transactional
    public Result<?> updateCourse(Integer id, CourseDTOs.CourseUpdateRequest request) {
        User user = getCurrentUser();
        if (user == null) return Result.error("未认证");

        Course course = this.getById(id);
        if (course == null) return Result.error("课程不存在");

        if (!canManageCourse(user, course)) {
            return Result.error("无权限修改此课程");
        }

        if (request.getTitle() != null) course.setTitle(request.getTitle());
        if (request.getDescription() != null) course.setDescription(request.getDescription());
        if (request.getCover() != null) course.setCover(request.getCover());
        this.updateById(course);

        return Result.success(course);
    }

    @Override
    @Transactional
    public Result<?> deleteCourse(Integer id) {
        User user = getCurrentUser();
        if (user == null) return Result.error("未认证");

        Course course = this.getById(id);
        if (course == null) return Result.error("课程不存在");

        if (!canManageCourse(user, course)) {
            return Result.error("无权限删除此课程");
        }

        List<CourseChapter> chapters = courseChapterMapper.selectList(
                new LambdaQueryWrapper<CourseChapter>().eq(CourseChapter::getCourseId, id));
        List<Integer> chapterIds = chapters.stream().map(CourseChapter::getId).collect(Collectors.toList());

        if (!chapterIds.isEmpty()) {
            chapterProgressMapper.delete(new LambdaQueryWrapper<ChapterProgress>()
                    .in(ChapterProgress::getChapterId, chapterIds));
        }

        courseChapterMapper.delete(new LambdaQueryWrapper<CourseChapter>()
                .eq(CourseChapter::getCourseId, id));

        this.removeById(id);

        return Result.success("删除成功");
    }

    @Override
    public Result<?> getCourseById(Integer id) {
        User user = getCurrentUser();
        if (user == null) return Result.error("未认证");

        Course course = this.getById(id);
        if (course == null) return Result.error("课程不存在");

        if (!canManageCourse(user, course) && !canLearnCourse(user, course)) {
            return Result.error("仅本社团成员可查看本社团课程");
        }

        Club club = clubMapper.selectById(course.getClubId());
        course.setClubName(club != null ? club.getName() : "未知社团");

        long totalChapters = courseChapterMapper.selectCount(
                new LambdaQueryWrapper<CourseChapter>().eq(CourseChapter::getCourseId, id));
        course.setTotalChapters((int) totalChapters);

        if (totalChapters > 0) {
            List<CourseChapter> chapters = courseChapterMapper.selectList(
                    new LambdaQueryWrapper<CourseChapter>().eq(CourseChapter::getCourseId, id)
                            .orderByAsc(CourseChapter::getSortOrder));
            List<Integer> chapterIds = chapters.stream().map(CourseChapter::getId).collect(Collectors.toList());

            long completed = chapterProgressMapper.selectCount(
                    new LambdaQueryWrapper<ChapterProgress>()
                            .eq(ChapterProgress::getUserId, user.getId())
                            .in(ChapterProgress::getChapterId, chapterIds)
                            .eq(ChapterProgress::getCompleted, 1));
            course.setCompletedChapters((int) completed);
            course.setProgressPercent(totalChapters > 0 ? Math.round(completed * 10000.0 / totalChapters) / 100.0 : 0.0);
        } else {
            course.setCompletedChapters(0);
            course.setProgressPercent(0.0);
        }

        return Result.success(course);
    }

    @Override
    public Result<?> listCourses(CourseDTOs.CourseQueryRequest request) {
        User user = getCurrentUser();
        if (user == null) return Result.error("未认证");

        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();

        if (request.getClubId() != null) {
            wrapper.eq(Course::getClubId, request.getClubId());
        } else if (RoleConstants.CLUB_LEADER.equals(user.getRole())) {
            wrapper.eq(Course::getClubId, user.getClubId());
        }

        if (request.getKeyword() != null && !request.getKeyword().isEmpty()) {
            wrapper.like(Course::getTitle, request.getKeyword())
                    .or().like(Course::getDescription, request.getKeyword());
        }

        wrapper.orderByDesc(Course::getCreateTime);

        Page<Course> page = this.page(
                new Page<>(request.getPageNum(), request.getPageSize()),
                wrapper);

        List<Course> records = page.getRecords();
        for (Course c : records) {
            Club club = clubMapper.selectById(c.getClubId());
            c.setClubName(club != null ? club.getName() : "未知社团");

            long totalChapters = courseChapterMapper.selectCount(
                    new LambdaQueryWrapper<CourseChapter>().eq(CourseChapter::getCourseId, c.getId()));
            c.setTotalChapters((int) totalChapters);

            if (totalChapters > 0) {
                List<CourseChapter> chapters = courseChapterMapper.selectList(
                        new LambdaQueryWrapper<CourseChapter>().eq(CourseChapter::getCourseId, c.getId()));
                List<Integer> chapterIds = chapters.stream().map(CourseChapter::getId).collect(Collectors.toList());

                long completed = chapterProgressMapper.selectCount(
                        new LambdaQueryWrapper<ChapterProgress>()
                                .eq(ChapterProgress::getUserId, user.getId())
                                .in(ChapterProgress::getChapterId, chapterIds)
                                .eq(ChapterProgress::getCompleted, 1));
                c.setCompletedChapters((int) completed);
                c.setProgressPercent(Math.round(completed * 10000.0 / totalChapters) / 100.0);
            } else {
                c.setCompletedChapters(0);
                c.setProgressPercent(0.0);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("list", records);
        result.put("total", page.getTotal());
        result.put("pageNum", page.getCurrent());
        result.put("pageSize", page.getSize());

        return Result.success(result);
    }

    @Override
    public Result<?> listMyCourses(CourseDTOs.CourseQueryRequest request) {
        User user = getCurrentUser();
        if (user == null) return Result.error("未认证");

        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();

        if (user.getClubId() != null) {
            wrapper.eq(Course::getClubId, user.getClubId());
        } else {
            wrapper.eq(Course::getClubId, -1);
        }

        if (request.getKeyword() != null && !request.getKeyword().isEmpty()) {
            wrapper.and(w -> w.like(Course::getTitle, request.getKeyword())
                    .or().like(Course::getDescription, request.getKeyword()));
        }

        wrapper.orderByDesc(Course::getCreateTime);

        Page<Course> page = this.page(
                new Page<>(request.getPageNum(), request.getPageSize()),
                wrapper);

        List<Course> records = page.getRecords();
        for (Course c : records) {
            Club club = clubMapper.selectById(c.getClubId());
            c.setClubName(club != null ? club.getName() : "未知社团");

            long totalChapters = courseChapterMapper.selectCount(
                    new LambdaQueryWrapper<CourseChapter>().eq(CourseChapter::getCourseId, c.getId()));
            c.setTotalChapters((int) totalChapters);

            if (totalChapters > 0) {
                List<CourseChapter> chapters = courseChapterMapper.selectList(
                        new LambdaQueryWrapper<CourseChapter>().eq(CourseChapter::getCourseId, c.getId()));
                List<Integer> chapterIds = chapters.stream().map(CourseChapter::getId).collect(Collectors.toList());

                long completed = chapterProgressMapper.selectCount(
                        new LambdaQueryWrapper<ChapterProgress>()
                                .eq(ChapterProgress::getUserId, user.getId())
                                .in(ChapterProgress::getChapterId, chapterIds)
                                .eq(ChapterProgress::getCompleted, 1));
                c.setCompletedChapters((int) completed);
                c.setProgressPercent(Math.round(completed * 10000.0 / totalChapters) / 100.0);
            } else {
                c.setCompletedChapters(0);
                c.setProgressPercent(0.0);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("list", records);
        result.put("total", page.getTotal());
        result.put("pageNum", page.getCurrent());
        result.put("pageSize", page.getSize());

        return Result.success(result);
    }

    @Override
    @Transactional
    public Result<?> createChapter(CourseDTOs.ChapterCreateRequest request) {
        User user = getCurrentUser();
        if (user == null) return Result.error("未认证");

        Course course = this.getById(request.getCourseId());
        if (course == null) return Result.error("课程不存在");

        if (!canManageCourse(user, course)) {
            return Result.error("无权限为此课程添加章节");
        }

        CourseChapter chapter = new CourseChapter();
        chapter.setCourseId(request.getCourseId());
        chapter.setSortOrder(request.getSortOrder());
        chapter.setTitle(request.getTitle());
        chapter.setContent(request.getContent());
        courseChapterMapper.insert(chapter);

        return Result.success(chapter);
    }

    @Override
    @Transactional
    public Result<?> updateChapter(Integer id, CourseDTOs.ChapterUpdateRequest request) {
        User user = getCurrentUser();
        if (user == null) return Result.error("未认证");

        CourseChapter chapter = courseChapterMapper.selectById(id);
        if (chapter == null) return Result.error("章节不存在");

        Course course = this.getById(chapter.getCourseId());
        if (course == null) return Result.error("所属课程不存在");

        if (!canManageCourse(user, course)) {
            return Result.error("无权限修改此章节");
        }

        if (request.getSortOrder() != null) chapter.setSortOrder(request.getSortOrder());
        if (request.getTitle() != null) chapter.setTitle(request.getTitle());
        if (request.getContent() != null) chapter.setContent(request.getContent());
        courseChapterMapper.updateById(chapter);

        return Result.success(chapter);
    }

    @Override
    @Transactional
    public Result<?> deleteChapter(Integer id) {
        User user = getCurrentUser();
        if (user == null) return Result.error("未认证");

        CourseChapter chapter = courseChapterMapper.selectById(id);
        if (chapter == null) return Result.error("章节不存在");

        Course course = this.getById(chapter.getCourseId());
        if (course == null) return Result.error("所属课程不存在");

        if (!canManageCourse(user, course)) {
            return Result.error("无权限删除此章节");
        }

        chapterProgressMapper.delete(new LambdaQueryWrapper<ChapterProgress>()
                .eq(ChapterProgress::getChapterId, id));

        courseChapterMapper.deleteById(id);

        return Result.success("删除成功");
    }

    @Override
    public Result<?> getChaptersByCourse(Integer courseId) {
        User user = getCurrentUser();
        if (user == null) return Result.error("未认证");

        Course course = this.getById(courseId);
        if (course == null) return Result.error("课程不存在");

        if (!canManageCourse(user, course) && !canLearnCourse(user, course)) {
            return Result.error("仅本社团成员可查看本社团课程");
        }

        List<CourseChapter> chapters = courseChapterMapper.selectList(
                new LambdaQueryWrapper<CourseChapter>()
                        .eq(CourseChapter::getCourseId, courseId)
                        .orderByAsc(CourseChapter::getSortOrder));

        List<Integer> chapterIds = chapters.stream().map(CourseChapter::getId).collect(Collectors.toList());
        Map<Integer, ChapterProgress> progressMap = new HashMap<>();
        if (!chapterIds.isEmpty()) {
            List<ChapterProgress> progresses = chapterProgressMapper.selectList(
                    new LambdaQueryWrapper<ChapterProgress>()
                            .eq(ChapterProgress::getUserId, user.getId())
                            .in(ChapterProgress::getChapterId, chapterIds));
            for (ChapterProgress p : progresses) {
                progressMap.put(p.getChapterId(), p);
            }
        }

        for (int i = 0; i < chapters.size(); i++) {
            CourseChapter ch = chapters.get(i);
            ChapterProgress progress = progressMap.get(ch.getId());
            boolean isCompleted = progress != null && progress.getCompleted() != null && progress.getCompleted() == 1;
            ch.setCompleted(isCompleted);
            if (isCompleted && progress != null) {
                ch.setCompleteTime(progress.getCompleteTime());
            }
            if (i == 0) {
                ch.setCanLearn(true);
            } else {
                CourseChapter prevChapter = chapters.get(i - 1);
                ChapterProgress prevProgress = progressMap.get(prevChapter.getId());
                boolean prevCompleted = prevProgress != null && prevProgress.getCompleted() != null && prevProgress.getCompleted() == 1;
                ch.setCanLearn(prevCompleted || isCompleted);
            }
        }

        return Result.success(chapters);
    }

    @Override
    public Result<?> getChapterDetail(Integer id) {
        User user = getCurrentUser();
        if (user == null) return Result.error("未认证");

        CourseChapter chapter = courseChapterMapper.selectById(id);
        if (chapter == null) return Result.error("章节不存在");

        Course course = this.getById(chapter.getCourseId());
        if (course == null) return Result.error("所属课程不存在");

        if (!canManageCourse(user, course) && !canLearnCourse(user, course)) {
            return Result.error("仅本社团成员可查看本社团课程");
        }

        List<CourseChapter> allChapters = courseChapterMapper.selectList(
                new LambdaQueryWrapper<CourseChapter>()
                        .eq(CourseChapter::getCourseId, chapter.getCourseId())
                        .orderByAsc(CourseChapter::getSortOrder));

        if (!canManageCourse(user, course)) {
            List<Integer> chapterIds = allChapters.stream().map(CourseChapter::getId).collect(Collectors.toList());
            Map<Integer, ChapterProgress> progressMap = new HashMap<>();
            if (!chapterIds.isEmpty()) {
                List<ChapterProgress> progresses = chapterProgressMapper.selectList(
                        new LambdaQueryWrapper<ChapterProgress>()
                                .eq(ChapterProgress::getUserId, user.getId())
                                .in(ChapterProgress::getChapterId, chapterIds));
                for (ChapterProgress p : progresses) {
                    progressMap.put(p.getChapterId(), p);
                }
            }

            int currentIndex = -1;
            for (int i = 0; i < allChapters.size(); i++) {
                if (allChapters.get(i).getId().equals(id)) {
                    currentIndex = i;
                    break;
                }
            }

            if (currentIndex > 0) {
                CourseChapter prevChapter = allChapters.get(currentIndex - 1);
                ChapterProgress prevProgress = progressMap.get(prevChapter.getId());
                boolean prevCompleted = prevProgress != null && prevProgress.getCompleted() != null && prevProgress.getCompleted() == 1;
                ChapterProgress currentProgress = progressMap.get(chapter.getId());
                boolean currentCompleted = currentProgress != null && currentProgress.getCompleted() != null && currentProgress.getCompleted() == 1;
                if (!prevCompleted && !currentCompleted) {
                    return Result.error("请按顺序学习，先完成上一章节");
                }
            }
        }

        ChapterProgress progress = chapterProgressMapper.selectOne(
                new LambdaQueryWrapper<ChapterProgress>()
                        .eq(ChapterProgress::getUserId, user.getId())
                        .eq(ChapterProgress::getChapterId, id));
        chapter.setCompleted(progress != null && progress.getCompleted() != null && progress.getCompleted() == 1);
        if (progress != null) {
            chapter.setCompleteTime(progress.getCompleteTime());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("chapter", chapter);
        result.put("course", course);

        Club club = clubMapper.selectById(course.getClubId());
        course.setClubName(club != null ? club.getName() : "未知社团");

        return Result.success(result);
    }

    @Override
    @Transactional
    public Result<?> markChapterComplete(Integer chapterId) {
        User user = getCurrentUser();
        if (user == null) return Result.error("未认证");

        CourseChapter chapter = courseChapterMapper.selectById(chapterId);
        if (chapter == null) return Result.error("章节不存在");

        Course course = this.getById(chapter.getCourseId());
        if (course == null) return Result.error("所属课程不存在");

        if (!canLearnCourse(user, course)) {
            return Result.error("仅本社团成员可学习本社团课程");
        }

        List<CourseChapter> allChapters = courseChapterMapper.selectList(
                new LambdaQueryWrapper<CourseChapter>()
                        .eq(CourseChapter::getCourseId, chapter.getCourseId())
                        .orderByAsc(CourseChapter::getSortOrder));

        int currentIndex = -1;
        for (int i = 0; i < allChapters.size(); i++) {
            if (allChapters.get(i).getId().equals(chapterId)) {
                currentIndex = i;
                break;
            }
        }

        if (currentIndex > 0) {
            CourseChapter prevChapter = allChapters.get(currentIndex - 1);
            ChapterProgress prevProgress = chapterProgressMapper.selectOne(
                    new LambdaQueryWrapper<ChapterProgress>()
                            .eq(ChapterProgress::getUserId, user.getId())
                            .eq(ChapterProgress::getChapterId, prevChapter.getId()));
            if (prevProgress == null || prevProgress.getCompleted() == null || prevProgress.getCompleted() != 1) {
                return Result.error("请按顺序学习，先完成上一章节：" + prevChapter.getTitle());
            }
        }

        ChapterProgress existing = chapterProgressMapper.selectOne(
                new LambdaQueryWrapper<ChapterProgress>()
                        .eq(ChapterProgress::getUserId, user.getId())
                        .eq(ChapterProgress::getChapterId, chapterId));

        if (existing != null && existing.getCompleted() != null && existing.getCompleted() == 1) {
            return Result.error("该章节已完成");
        }

        if (existing == null) {
            ChapterProgress progress = new ChapterProgress();
            progress.setUserId(user.getId());
            progress.setChapterId(chapterId);
            progress.setCompleted(1);
            progress.setCompleteTime(LocalDateTime.now());
            chapterProgressMapper.insert(progress);
        } else {
            existing.setCompleted(1);
            existing.setCompleteTime(LocalDateTime.now());
            chapterProgressMapper.updateById(existing);
        }

        return Result.success("标记完成成功");
    }
}
