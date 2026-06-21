<template>
  <div class="course-detail">
    <div class="back-bar">
      <el-button @click="router.back()">
        <el-icon><ArrowLeft /></el-icon> 返回课程列表
      </el-button>
    </div>

    <div v-loading="loading" class="detail-grid">
      <div class="left-panel">
        <div class="course-header glass-card">
          <el-image
            :src="course?.cover || defaultCover"
            fit="cover"
            style="width: 100%; height: 220px; border-radius: 10px"
          >
            <template #error>
              <div class="image-placeholder">
                <el-icon :size="56"><Picture /></el-icon>
              </div>
            </template>
          </el-image>
          <h1 class="course-title">{{ course?.title }}</h1>
          <div class="course-tags">
            <el-tag size="small" type="info">
              <el-icon><OfficeBuilding /></el-icon> {{ course?.clubName }}
            </el-tag>
            <el-tag size="small" type="success">
              {{ chapters?.length || 0 }} 章节
            </el-tag>
          </div>
          <p class="course-desc">{{ course?.description || '暂无简介' }}</p>

          <div class="progress-header">
            <div class="progress-title">
              <el-icon><DataLine /></el-icon>
              <span>学习进度</span>
              <strong class="progress-percent">{{ course?.progressPercent?.toFixed(1) || 0 }}%</strong>
            </div>
            <div class="progress-sub">
              已完成 {{ course?.completedChapters || 0 }} / {{ course?.totalChapters || 0 }} 章
            </div>
          </div>
          <el-progress
            :percentage="Math.round(course?.progressPercent || 0)"
            :stroke-width="14"
            :color="progressColor"
          />
          <div class="learn-entry">
            <el-button
              v-if="nextChapterId"
              type="primary"
              size="large"
              @click="goLearn(nextChapterId!)"
            >
              <el-icon><VideoPlay /></el-icon>
              {{ (course?.completedChapters || 0) > 0 ? '继续学习' : '开始学习' }}
            </el-button>
            <el-button v-else size="large" type="success" disabled>
              <el-icon><CircleCheck /></el-icon> 课程已全部完成
            </el-button>
          </div>
        </div>
      </div>

      <div class="right-panel glass-card">
        <div class="chapter-header">
          <h3>
            <el-icon><List /></el-icon> 章节目录
          </h3>
          <el-button
            v-if="canManage"
            type="success"
            size="small"
            @click="openChapterDialog()"
          >
            <el-icon><Plus /></el-icon> 添加章节
          </el-button>
        </div>

        <div class="chapter-list" v-if="chapters.length">
          <div
            v-for="(ch, idx) in chapters"
            :key="ch.id"
            :class="[
              'chapter-item',
              ch.completed ? 'completed' : '',
              !ch.canLearn && !ch.completed ? 'locked' : '',
            ]"
          >
            <div class="chapter-index">
              <el-tag
                v-if="ch.completed"
                type="success"
                effect="dark"
                size="small"
                round
              >
                <el-icon><CircleCheck /></el-icon>
              </el-tag>
              <el-tag
                v-else-if="!ch.canLearn"
                type="info"
                size="small"
                round
              >
                <el-icon><Lock /></el-icon>
              </el-tag>
              <el-tag v-else type="primary" size="small" round>
                {{ idx + 1 }}
              </el-tag>
            </div>
            <div
              class="chapter-content"
              :class="{ clickable: ch.canLearn || ch.completed || canManage }"
              @click="onChapterClick(ch)"
            >
              <div class="chapter-title">{{ ch.title }}</div>
              <div class="chapter-sub">
                <span v-if="ch.completed" class="tag-done">
                  已完成 · {{ formatTime(ch.completeTime) }}
                </span>
                <span v-else-if="!ch.canLearn && !canManage" class="tag-lock">
                  请先完成上一章节
                </span>
                <span v-else-if="!canManage" class="tag-start">点击开始学习</span>
                <span v-else class="tag-manage">
                  序号 {{ ch.sortOrder }}
                </span>
              </div>
            </div>
            <div v-if="canManage" class="chapter-actions">
              <el-button
                type="primary"
                link
                @click.stop="goLearn(ch.id)"
              >
                <el-icon><View /></el-icon>
              </el-button>
              <el-button
                type="warning"
                link
                @click.stop="openChapterDialog(ch)"
              >
                <el-icon><Edit /></el-icon>
              </el-button>
              <el-button
                type="danger"
                link
                @click.stop="deleteChapter(ch)"
              >
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无章节，请负责人添加" />
      </div>
    </div>

    <el-dialog
      v-model="chapterDialogVisible"
      :title="editingChapter ? '编辑章节' : '添加章节'"
      width="640px"
      destroy-on-close
    >
      <el-form :model="chapterForm" :rules="chapterRules" ref="chapterFormRef" label-width="90px">
        <el-form-item label="章节标题" prop="title">
          <el-input v-model="chapterForm.title" placeholder="请输入章节标题" maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item label="章节序号" prop="sortOrder">
          <el-input-number
            v-model="chapterForm.sortOrder"
            :min="1"
            :max="999"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="章节内容">
          <el-input
            v-model="chapterForm.content"
            type="textarea"
            :rows="12"
            placeholder="支持 Markdown / HTML 格式"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="chapterDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitChapter">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, reactive, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  ArrowLeft, ArrowRight, Picture, OfficeBuilding, DataLine, VideoPlay,
  CircleCheck, List, Plus, Lock, View, Edit, Delete
} from '@element-plus/icons-vue'
import courseApi, { type Course, type CourseChapter } from '../../api/courses'
import { useUserStore } from '../../store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const courseId = computed(() => Number(route.params.id))
const loading = ref(false)
const course = ref<Course | null>(null)
const chapters = ref<CourseChapter[]>([])

const canManage = computed(() =>
  ['ADMIN', 'UNION_ADMIN', 'CLUB_LEADER'].includes(userStore.role ?? '')
)
const defaultCover = '/mock_images/media__1772178768576.png'

const progressColor = computed(() => {
  const p = course.value?.progressPercent || 0
  if (p >= 100) return '#67c23a'
  if (p >= 60) return '#409eff'
  if (p > 0) return '#e6a23c'
  return '#909399'
})

const nextChapterId = computed(() => {
  for (const ch of chapters.value) {
    if (!ch.completed) return ch.canLearn ? ch.id : null
  }
  return null
})

const chapterDialogVisible = ref(false)
const editingChapter = ref<CourseChapter | null>(null)
const submitting = ref(false)
const chapterFormRef = ref<FormInstance>()

const chapterForm = reactive({
  title: '',
  sortOrder: 1,
  content: ''
})

const chapterRules: FormRules = {
  title: [{ required: true, message: '请输入章节标题', trigger: 'blur' }],
  sortOrder: [{ required: true, message: '请输入章节序号', trigger: 'blur' }]
}

async function loadDetail() {
  loading.value = true
  try {
    const [c, chs] = await Promise.all([
      courseApi.getCourseById(courseId.value),
      courseApi.getChaptersByCourse(courseId.value)
    ])
    course.value = c
    chapters.value = chs || []
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

function onChapterClick(ch: CourseChapter) {
  if (canManage.value || ch.canLearn || ch.completed) {
    goLearn(ch.id)
  }
}

function goLearn(chapterId: number) {
  router.push(`/courses/${courseId.value}/chapter/${chapterId}`)
}

function formatTime(t?: string) {
  if (!t) return ''
  return t.replace('T', ' ').slice(0, 16)
}

function openChapterDialog(ch?: CourseChapter) {
  editingChapter.value = ch || null
  if (ch) {
    chapterForm.title = ch.title
    chapterForm.sortOrder = ch.sortOrder
    chapterForm.content = ch.content || ''
  } else {
    chapterForm.title = ''
    chapterForm.sortOrder = (chapters.value.length || 0) + 1
    chapterForm.content = ''
  }
  chapterDialogVisible.value = true
}

async function submitChapter() {
  try {
    await chapterFormRef.value?.validate()
  } catch {
    return
  }
  submitting.value = true
  try {
    if (editingChapter.value) {
      await courseApi.updateChapter(editingChapter.value.id, {
        title: chapterForm.title,
        sortOrder: chapterForm.sortOrder,
        content: chapterForm.content
      })
      ElMessage.success('章节更新成功')
    } else {
      await courseApi.createChapter({
        courseId: courseId.value,
        title: chapterForm.title,
        sortOrder: chapterForm.sortOrder,
        content: chapterForm.content
      })
      ElMessage.success('章节添加成功')
    }
    chapterDialogVisible.value = false
    loadDetail()
  } finally {
    submitting.value = false
  }
}

async function deleteChapter(ch: CourseChapter) {
  try {
    await ElMessageBox.confirm(
      `确定删除章节「${ch.title}」？该章节下所有学习进度将一并清除。`,
      '删除确认',
      { type: 'warning' }
    )
    await courseApi.deleteChapter(ch.id)
    ElMessage.success('删除成功')
    loadDetail()
  } catch (e: any) {
    if (e !== 'cancel') console.error(e)
  }
}

watch(() => route.params.id, () => {
  loadDetail()
})

onMounted(() => {
  loadDetail()
})
</script>

<style scoped>
.course-detail {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.back-bar {
  display: flex;
  align-items: center;
}

.detail-grid {
  display: grid;
  grid-template-columns: 360px 1fr;
  gap: 20px;
}

@media (max-width: 1024px) {
  .detail-grid {
    grid-template-columns: 1fr;
  }
}

.left-panel, .right-panel {
  padding: 20px;
}

.course-header {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.image-placeholder {
  width: 100%;
  height: 220px;
  background: #f5f7fa;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #c0c4cc;
}

.course-title {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: #303133;
}

.course-tags {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.course-desc {
  margin: 0;
  color: #606266;
  font-size: 13px;
  line-height: 1.7;
}

.progress-header {
  margin-top: 4px;
}

.progress-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #303133;
}

.progress-percent {
  margin-left: auto;
  font-size: 18px;
  color: #409eff;
}

.progress-sub {
  color: #909399;
  font-size: 12px;
  margin-bottom: 8px;
}

.learn-entry {
  margin-top: 4px;
}

.learn-entry .el-button {
  width: 100%;
}

.chapter-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 14px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
}

.chapter-header h3 {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0;
  font-size: 16px;
  color: #303133;
}

.chapter-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  max-height: 600px;
  overflow-y: auto;
  padding-right: 4px;
}

.chapter-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 16px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.5);
  border: 1px solid rgba(0, 0, 0, 0.04);
  transition: all 0.2s;
}
.chapter-item:hover {
  background: rgba(64, 158, 255, 0.06);
}
.chapter-item.completed {
  background: rgba(103, 194, 58, 0.08);
  border-color: rgba(103, 194, 58, 0.15);
}
.chapter-item.locked {
  opacity: 0.6;
}

.chapter-index {
  flex-shrink: 0;
}

.chapter-content {
  flex: 1;
  min-width: 0;
}
.chapter-content.clickable {
  cursor: pointer;
}

.chapter-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.chapter-sub {
  font-size: 12px;
  color: #909399;
  display: flex;
  align-items: center;
  gap: 4px;
}

.tag-done { color: #67c23a; }
.tag-lock { color: #e6a23c; }
.tag-start { color: #409eff; }
.tag-manage { color: #606266; }

.chapter-actions {
  display: flex;
  gap: 6px;
  flex-shrink: 0;
}
</style>
