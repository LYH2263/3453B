package com.club.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.club.common.Result;
import com.club.dto.CourseDTOs;
import com.club.entity.Course;

public interface CourseService extends IService<Course> {
    Result<?> createCourse(CourseDTOs.CourseCreateRequest request);
    Result<?> updateCourse(Integer id, CourseDTOs.CourseUpdateRequest request);
    Result<?> deleteCourse(Integer id);
    Result<?> getCourseById(Integer id);
    Result<?> listCourses(CourseDTOs.CourseQueryRequest request);
    Result<?> listMyCourses(CourseDTOs.CourseQueryRequest request);

    Result<?> createChapter(CourseDTOs.ChapterCreateRequest request);
    Result<?> updateChapter(Integer id, CourseDTOs.ChapterUpdateRequest request);
    Result<?> deleteChapter(Integer id);
    Result<?> getChaptersByCourse(Integer courseId);
    Result<?> getChapterDetail(Integer id);

    Result<?> markChapterComplete(Integer chapterId);
}
