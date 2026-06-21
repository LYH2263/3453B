package com.club.dto;

import jakarta.validation.constraints.*;

public class CourseDTOs {

    public static class CourseCreateRequest {
        @NotNull(message = "社团ID不能为空")
        private Integer clubId;

        @NotBlank(message = "课程标题不能为空")
        private String title;

        private String description;

        private String cover;

        public Integer getClubId() { return clubId; }
        public void setClubId(Integer clubId) { this.clubId = clubId; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getCover() { return cover; }
        public void setCover(String cover) { this.cover = cover; }
    }

    public static class CourseUpdateRequest {
        private String title;
        private String description;
        private String cover;

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getCover() { return cover; }
        public void setCover(String cover) { this.cover = cover; }
    }

    public static class ChapterCreateRequest {
        @NotNull(message = "课程ID不能为空")
        private Integer courseId;

        @NotNull(message = "章节序号不能为空")
        private Integer sortOrder;

        @NotBlank(message = "章节标题不能为空")
        private String title;

        private String content;

        public Integer getCourseId() { return courseId; }
        public void setCourseId(Integer courseId) { this.courseId = courseId; }

        public Integer getSortOrder() { return sortOrder; }
        public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }

    public static class ChapterUpdateRequest {
        private Integer sortOrder;
        private String title;
        private String content;

        public Integer getSortOrder() { return sortOrder; }
        public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }

    public static class CourseQueryRequest {
        private Integer clubId;
        private String keyword;
        private Integer pageNum = 1;
        private Integer pageSize = 10;

        public Integer getClubId() { return clubId; }
        public void setClubId(Integer clubId) { this.clubId = clubId; }

        public String getKeyword() { return keyword; }
        public void setKeyword(String keyword) { this.keyword = keyword; }

        public Integer getPageNum() { return pageNum; }
        public void setPageNum(Integer pageNum) { this.pageNum = pageNum; }

        public Integer getPageSize() { return pageSize; }
        public void setPageSize(Integer pageSize) { this.pageSize = pageSize; }
    }

    public static class MarkCompleteRequest {
        @NotNull(message = "章节ID不能为空")
        private Integer chapterId;

        public Integer getChapterId() { return chapterId; }
        public void setChapterId(Integer chapterId) { this.chapterId = chapterId; }
    }
}
