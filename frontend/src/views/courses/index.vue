<template>
  <div class="courses-page">
    <div class="page-header glass-card">
      <div class="header-left">
        <h2><el-icon><Reading /></el-icon> 培训课程中心</h2>
        <p class="subtitle">系统化学习，提升专业技能</p>
      </div>
      <div class="header-right">
        <el-input
          v-model="keyword"
          placeholder="搜索课程标题或简介..."
          style="width: 280px"
          clearable
          @clear="loadCourses"
          @keyup.enter="loadCourses"
        >
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-button type="primary" @click="loadCourses">
          <el-icon><Search /></el-icon> 搜索
        </el-button>
        <el-button v-if="canManage" type="success" @click="openCourseDialog()">
          <el-icon><Plus /></el-icon> 新建课程
        </el-button>
      </div>
    </div>

    <div v-loading="loading" class="course-grid">
      <el-card
        v-for="course in courses"
        :key="course.id"
        class="course-card glass-card hover-lift"
        shadow="never"
      >
        <div class="card-cover" @click="goDetail(course.id)">
          <el-image
            :src="course.cover || defaultCover"
            :preview-src-list="[course.cover || defaultCover]"
            fit="cover"
            style="width: 100%; height: 160px; border-radius: 8px"
          >
            <template #error>
              <div class="image-placeholder">
                <el-icon :size="48"><Picture /></el-icon>
              </div>
            </template>
          </el-image>
        </div>
        <div class="card-body">
          <div class="card-title" @click="goDetail(course.id)">{{ course.title }}</div>
          <div class="card-meta">
            <el-tag size="small" type="info">
              <el-icon><OfficeBuilding /></el-icon> {{ course.clubName }}
            </el-tag>
            <span class="chapter-count">
              <el-icon><List /></el-icon> {{ course.totalChapters || 0 }} 章节
            </span>
          </div>
          <div class="card-desc">{{ course.description || '暂无简介' }}</div>
          <div class="progress-section">
            <div class="progress-label">
              <span>学习进度</span>
              <span class="progress-text">
                {{ course.completedChapters || 0 }} / {{ course.totalChapters || 0 }}
                ({{ course.progressPercent?.toFixed(1) || 0 }}%)
              </span>
            </div>
            <el-progress
              :percentage="Math.round(course.progressPercent || 0)"
              :stroke-width="8"
              :color="progressColor(course.progressPercent || 0)"
            />
          </div>
          <div class="card-actions">
            <el-button type="primary" @click="goDetail(course.id)">
              {{ (course.progressPercent || 0) > 0 ? '继续学习' : '开始学习' }}
              <el-icon><ArrowRight /></el-icon>
            </el-button>
            <template v-if="canManage">
              <el-button @click="openCourseDialog(course)">
                <el-icon><Edit /></el-icon>
              </el-button>
              <el-button type="danger" @click="deleteCourse(course)">
                <el-icon><Delete /></el-icon>
              </el-button>
            </template>
          </div>
        </div>
      </el-card>

      <el-empty v-if="courses.length === 0 && !loading" description="暂无课程数据" />
    </div>

    <div v-if="total > pageSize" class="pagination glass-card">
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :page-sizes="[6, 9, 12, 24]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadCourses"
        @current-change="loadCourses"
      />
    </div>

    <el-dialog
      v-model="courseDialogVisible"
      :title="editingCourse ? '编辑课程' : '新建课程'"
      width="560px"
      destroy-on-close
    >
      <el-form :model="courseForm" :rules="courseRules" ref="courseFormRef" label-width="90px">
        <el-form-item label="课程标题" prop="title">
          <el-input v-model="courseForm.title" placeholder="请输入课程标题" maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item label="所属社团" prop="clubId">
          <el-select v-model="courseForm.clubId" placeholder="请选择社团" style="width: 100%" :disabled="isClubLeader">
            <el-option v-for="club in clubList" :key="club.id" :label="club.name" :value="club.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="封面图片">
          <el-input v-model="courseForm.cover" placeholder="封面图片URL（可选）" />
        </el-form-item>
        <el-form-item label="课程简介">
          <el-input
            v-model="courseForm.description"
            type="textarea"
            :rows="4"
            placeholder="请输入课程简介（可选）"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="courseDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitCourse">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Reading, Search, Plus, ArrowRight, Edit, Delete, OfficeBuilding, List, Picture } from '@element-plus/icons-vue'
import courseApi, { type Course } from '../../api/courses'
import { useUserStore } from '../../store/user'
import request from '../../utils/request'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const courses = ref<Course[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(6)
const keyword = ref('')

const canManage = computed(() =>
  ['ADMIN', 'UNION_ADMIN', 'CLUB_LEADER'].includes(userStore.role ?? '')
)
const isClubLeader = computed(() => userStore.role === 'CLUB_LEADER')
const defaultCover = '/mock_images/media__1772178768576.png'

const courseDialogVisible = ref(false)
const editingCourse = ref<Course | null>(null)
const submitting = ref(false)
const courseFormRef = ref<FormInstance>()
const clubList = ref<any[]>([])

const courseForm = reactive({
  title: '',
  clubId: userStore.userInfo?.clubId as number | undefined,
  cover: '',
  description: ''
})

const courseRules: FormRules = {
  title: [{ required: true, message: '请输入课程标题', trigger: 'blur' }],
  clubId: [{ required: true, message: '请选择所属社团', trigger: 'change' }]
}

async function loadCourses() {
  loading.value = true
  try {
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: keyword.value || undefined
    }
    let res
    if (canManage.value) {
      res = await courseApi.listCourses(params)
    } else {
      res = await courseApi.listMyCourses(params)
    }
    courses.value = res.list || []
    total.value = res.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

async function loadClubList() {
  try {
    const res: any = await request.get('/clubs')
    if (Array.isArray(res)) {
      clubList.value = res
    } else if (res && res.list) {
      clubList.value = res.list
    }
  } catch (e) {
    console.error(e)
  }
}

function progressColor(percent: number) {
  if (percent >= 100) return '#67c23a'
  if (percent >= 60) return '#409eff'
  if (percent > 0) return '#e6a23c'
  return '#909399'
}

function goDetail(id: number) {
  router.push(`/courses/${id}`)
}

function openCourseDialog(course?: Course) {
  editingCourse.value = course || null
  if (course) {
    courseForm.title = course.title
    courseForm.clubId = course.clubId
    courseForm.cover = course.cover || ''
    courseForm.description = course.description || ''
  } else {
    courseForm.title = ''
    courseForm.clubId = isClubLeader.value ? userStore.userInfo?.clubId : undefined
    courseForm.cover = ''
    courseForm.description = ''
  }
  courseDialogVisible.value = true
  if (!course) {
    loadClubList()
  }
}

async function submitCourse() {
  try {
    await courseFormRef.value?.validate()
  } catch {
    return
  }
  submitting.value = true
  try {
    if (editingCourse.value) {
      await courseApi.updateCourse(editingCourse.value.id, {
        title: courseForm.title,
        cover: courseForm.cover,
        description: courseForm.description
      })
      ElMessage.success('课程更新成功')
    } else {
      await courseApi.createCourse({
        title: courseForm.title,
        clubId: courseForm.clubId!,
        cover: courseForm.cover,
        description: courseForm.description
      })
      ElMessage.success('课程创建成功')
    }
    courseDialogVisible.value = false
    loadCourses()
  } finally {
    submitting.value = false
  }
}

async function deleteCourse(course: Course) {
  try {
    await ElMessageBox.confirm(
      `确定删除课程「${course.title}」？将同时删除所有章节和学习进度，此操作不可恢复。`,
      '删除确认',
      { type: 'warning' }
    )
    await courseApi.deleteCourse(course.id)
    ElMessage.success('删除成功')
    loadCourses()
  } catch (e: any) {
    if (e !== 'cancel') console.error(e)
  }
}

onMounted(() => {
  loadCourses()
})
</script>

<style scoped>
.courses-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.page-header {
  padding: 20px 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
}

.header-left h2 {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 4px 0;
  color: #303133;
}

.subtitle {
  margin: 0;
  color: #909399;
  font-size: 13px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.course-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
}

.course-card {
  padding: 0 !important;
  display: flex;
  flex-direction: column;
}

.card-cover {
  cursor: pointer;
  padding: 14px 14px 0 14px;
}

.image-placeholder {
  width: 100%;
  height: 160px;
  background: #f5f7fa;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #c0c4cc;
}

.card-body {
  padding: 14px 18px 18px;
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.card-title {
  font-size: 17px;
  font-weight: 600;
  color: #303133;
  cursor: pointer;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.card-title:hover {
  color: #409eff;
}

.card-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 12px;
}

.chapter-count {
  color: #909399;
  display: flex;
  align-items: center;
  gap: 4px;
}

.card-desc {
  color: #606266;
  font-size: 13px;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.progress-section {
  margin-top: auto;
  padding-top: 6px;
}

.progress-label {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #909399;
  margin-bottom: 6px;
}

.progress-text {
  color: #409eff;
  font-weight: 600;
}

.card-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  padding-top: 4px;
}
.card-actions .el-button:first-child {
  flex: 1;
}

.pagination {
  padding: 16px;
  display: flex;
  justify-content: center;
}
</style>
