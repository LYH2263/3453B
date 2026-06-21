import request from '../utils/request'

export interface Course {
  id: number
  clubId: number
  title: string
  description: string
  cover?: string
  createTime?: string
  updateTime?: string
  clubName?: string
  totalChapters?: number
  completedChapters?: number
  progressPercent?: number
}

export interface CourseChapter {
  id: number
  courseId: number
  sortOrder: number
  title: string
  content?: string
  createTime?: string
  completed?: boolean
  completeTime?: string
  canLearn?: boolean
}

export interface CourseCreateRequest {
  clubId: number
  title: string
  description?: string
  cover?: string
}

export interface CourseUpdateRequest {
  title?: string
  description?: string
  cover?: string
}

export interface ChapterCreateRequest {
  courseId: number
  sortOrder: number
  title: string
  content?: string
}

export interface ChapterUpdateRequest {
  sortOrder?: number
  title?: string
  content?: string
}

export interface CourseQueryParams {
  clubId?: number
  keyword?: string
  pageNum?: number
  pageSize?: number
}

export interface PageResult<T> {
  list: T[]
  total: number
  pageNum: number
  pageSize: number
}

export interface ChapterDetailResponse {
  chapter: CourseChapter
  course: Course
}

const courseApi = {
  createCourse: (data: CourseCreateRequest) =>
    request.post<Course>('/courses', data),

  updateCourse: (id: number, data: CourseUpdateRequest) =>
    request.put<Course>(`/courses/${id}`, data),

  deleteCourse: (id: number) =>
    request.delete(`/courses/${id}`),

  getCourseById: (id: number) =>
    request.get<Course>(`/courses/${id}`),

  listCourses: (params?: CourseQueryParams) =>
    request.get<PageResult<Course>>('/courses', { params }),

  listMyCourses: (params?: CourseQueryParams) =>
    request.get<PageResult<Course>>('/courses/my', { params }),

  createChapter: (data: ChapterCreateRequest) =>
    request.post<CourseChapter>('/courses/chapters', data),

  updateChapter: (id: number, data: ChapterUpdateRequest) =>
    request.put<CourseChapter>(`/courses/chapters/${id}`, data),

  deleteChapter: (id: number) =>
    request.delete(`/courses/chapters/${id}`),

  getChaptersByCourse: (courseId: number) =>
    request.get<CourseChapter[]>(`/courses/${courseId}/chapters`),

  getChapterDetail: (id: number) =>
    request.get<ChapterDetailResponse>(`/courses/chapters/${id}`),

  markChapterComplete: (chapterId: number) =>
    request.post('/courses/chapters/complete', { chapterId })
}

export default courseApi
