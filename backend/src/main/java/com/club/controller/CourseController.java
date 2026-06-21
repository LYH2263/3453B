package com.club.controller;

import com.club.common.Result;
import com.club.common.annotation.Log;
import com.club.dto.CourseDTOs;
import com.club.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "课程学习管理", description = "课程与章节的CRUD、学习进度管理")
@RestController
@RequestMapping("/api/courses")
public class CourseController {

    @Autowired
    private CourseService courseService;

    @Operation(summary = "创建课程（负责人/管理员）")
    @Log("创建课程")
    @PostMapping
    @PreAuthorize("hasAnyRole('CLUB_LEADER', 'UNION_ADMIN', 'ADMIN')")
    public Result<?> createCourse(@Valid @RequestBody CourseDTOs.CourseCreateRequest request) {
        return courseService.createCourse(request);
    }

    @Operation(summary = "修改课程（负责人/管理员）")
    @Log("修改课程")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLUB_LEADER', 'UNION_ADMIN', 'ADMIN')")
    public Result<?> updateCourse(
            @PathVariable Integer id,
            @RequestBody CourseDTOs.CourseUpdateRequest request) {
        return courseService.updateCourse(id, request);
    }

    @Operation(summary = "删除课程（负责人/管理员，级联删除章节与进度）")
    @Log("删除课程")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLUB_LEADER', 'UNION_ADMIN', 'ADMIN')")
    public Result<?> deleteCourse(@PathVariable Integer id) {
        return courseService.deleteCourse(id);
    }

    @Operation(summary = "获取课程详情")
    @GetMapping("/{id}")
    public Result<?> getCourseById(@PathVariable Integer id) {
        return courseService.getCourseById(id);
    }

    @Operation(summary = "分页查询课程列表（负责人/管理员）")
    @GetMapping
    @PreAuthorize("hasAnyRole('CLUB_LEADER', 'UNION_ADMIN', 'ADMIN')")
    public Result<?> listCourses(@ModelAttribute CourseDTOs.CourseQueryRequest request) {
        return courseService.listCourses(request);
    }

    @Operation(summary = "获取我所在社团的课程列表（成员学习入口）")
    @GetMapping("/my")
    public Result<?> listMyCourses(@ModelAttribute CourseDTOs.CourseQueryRequest request) {
        return courseService.listMyCourses(request);
    }

    @Operation(summary = "添加章节（负责人/管理员）")
    @Log("添加章节")
    @PostMapping("/chapters")
    @PreAuthorize("hasAnyRole('CLUB_LEADER', 'UNION_ADMIN', 'ADMIN')")
    public Result<?> createChapter(@Valid @RequestBody CourseDTOs.ChapterCreateRequest request) {
        return courseService.createChapter(request);
    }

    @Operation(summary = "修改章节（负责人/管理员）")
    @Log("修改章节")
    @PutMapping("/chapters/{id}")
    @PreAuthorize("hasAnyRole('CLUB_LEADER', 'UNION_ADMIN', 'ADMIN')")
    public Result<?> updateChapter(
            @PathVariable Integer id,
            @RequestBody CourseDTOs.ChapterUpdateRequest request) {
        return courseService.updateChapter(id, request);
    }

    @Operation(summary = "删除章节（负责人/管理员，级联删除进度）")
    @Log("删除章节")
    @DeleteMapping("/chapters/{id}")
    @PreAuthorize("hasAnyRole('CLUB_LEADER', 'UNION_ADMIN', 'ADMIN')")
    public Result<?> deleteChapter(@PathVariable Integer id) {
        return courseService.deleteChapter(id);
    }

    @Operation(summary = "获取某课程的所有章节")
    @GetMapping("/{courseId}/chapters")
    public Result<?> getChaptersByCourse(@PathVariable Integer courseId) {
        return courseService.getChaptersByCourse(courseId);
    }

    @Operation(summary = "获取章节详情（带内容与学习权限校验）")
    @GetMapping("/chapters/{id}")
    public Result<?> getChapterDetail(@PathVariable Integer id) {
        return courseService.getChapterDetail(id);
    }

    @Operation(summary = "标记章节完成（按序学习）")
    @Log("标记章节完成")
    @PostMapping("/chapters/complete")
    public Result<?> markChapterComplete(@Valid @RequestBody CourseDTOs.MarkCompleteRequest request) {
        return courseService.markChapterComplete(request.getChapterId());
    }
}
