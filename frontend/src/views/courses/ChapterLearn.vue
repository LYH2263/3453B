<template>
  <div class="chapter-learn">
    <div class="nav-bar glass-card">
      <div class="nav-left">
        <el-button @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
          {{ courseTitle || '返回课程' }}
        </el-button>
      </div>
      <div class="nav-center">
        <el-breadcrumb separator=">">
          <el-breadcrumb-item @click="router.push('/courses')">培训课程</el-breadcrumb-item>
          <el-breadcrumb-item @click="router.push(`/courses/${courseId}`)">{{ courseTitle }}</el-breadcrumb-item>
          <el-breadcrumb-item>{{ chapterTitle }}</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      <div class="nav-right">
        <el-button
          v-if="prevChapterId"
          @click="goChapter(prevChapterId!)"
        >
          <el-icon><DArrowLeft /></el-icon> 上一章
        </el-button>
        <el-button
          v-if="nextChapterId"
          type="primary"
          @click="goChapter(nextChapterId!)"
        >
          下一章 <el-icon><DArrowRight /></el-icon>
        </el-button>
        <el-button v-else-if="isAllCompleted" type="success" disabled>
          <el-icon><CircleCheck /></el-icon> 已全部完成
        </el-button>
      </div>
    </div>

    <div v-loading="loading" class="learn-grid">
      <div class="chapter-panel glass-card">
        <div class="chapter-info">
          <el-tag v-if="isCompleted" type="success" effect="dark" size="large" round>
            <el-icon><CircleCheck /></el-icon> 已完成
          </el-tag>
          <el-tag v-else-if="canLearn" type="primary" size="large" round>
            <el-icon><Reading /></el-icon> 学习中
          </el-tag>
          <el-tag v-else type="warning" size="large" round>
            <el-icon><Lock /></el-icon> 需先完成前置章节
          </el-tag>
          <div class="chapter-time" v-if="completeTime">
            完成时间：{{ formatTime(completeTime) }}
          </div>
        </div>
        <h1 class="chapter-title">{{ chapterTitle }}</h1>

        <div class="chapter-content glass-card-inner">
          <template v-if="!canLearn && !isCompleted && !canManage">
            <el-empty description="请按顺序学习，先完成上一章节后再解锁本章节">
              <el-button type="primary" @click="goBack">返回课程目录</el-button>
            </el-empty>
          </template>
          <div v-else-if="chapterContent" v-html="renderContent(chapterContent)"></div>
          <el-empty v-else description="本章节暂无内容" />
        </div>

        <div v-if="(canLearn || canManage) && !isCompleted" class="complete-section">
          <el-button
            type="success"
            size="large"
            :loading="marking"
            @click="markComplete"
          >
            <el-icon><Check /></el-icon>
            标记本章已完成
          </el-button>
          <p class="complete-tip">
            完成后将自动解锁下一章节（如有）
          </p>
        </div>
        <div v-else-if="isCompleted" class="complete-done">
          <el-alert
            type="success"
            show-icon
            :closable="false"
            title="恭喜！本章节已完成学习"
            description="您可以继续学习下一章，或返回课程目录查看其他课程。"
          />
        </div>
      </div>

      <div class="sider-panel glass-card">
        <div class="sider-title">
          <el-icon><List /></el-icon> 章节目录
        </div>
        <div class="sider-list">
          <div
            v-for="(ch, idx) in allChapters"
            :key="ch.id"
            :class="[
              'sider-item',
              ch.id === chapterId ? 'active' : '',
              ch.completed ? 'completed' : '',
              !ch.canLearn && !ch.completed ? 'locked' : ''
            ]"
            @click="onSideChapter(ch)"
          >
            <span class="sider-idx">
              <el-icon v-if="ch.completed" color="#67c23a"><CircleCheckFilled /></el-icon>
              <el-icon v-else-if="!ch.canLearn" color="#909399"><Lock /></el-icon>
              <span v-else class="idx-num">{{ idx + 1 }}</span>
            </span>
            <span class="sider-text">{{ ch.title }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  ArrowLeft, DArrowLeft, DArrowRight, CircleCheck, Reading, Lock,
  Check, List, CircleCheckFilled
} from '@element-plus/icons-vue'
import courseApi, { type CourseChapter } from '../../api/courses'
import { useUserStore } from '../../store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const courseId = computed(() => Number(route.params.courseId))
const chapterId = computed(() => Number(route.params.chapterId))

const canManage = computed(() =>
  ['ADMIN', 'UNION_ADMIN', 'CLUB_LEADER'].includes(userStore.role ?? '')
)

const loading = ref(false)
const marking = ref(false)
const allChapters = ref<CourseChapter[]>([])
const courseTitle = ref('')
const chapterTitle = ref('')
const chapterContent = ref('')
const isCompleted = ref(false)
const canLearn = ref(false)
const completeTime = ref('')

const currentIndex = computed(() =>
  allChapters.value.findIndex((c) => c.id === chapterId.value)
)
const prevChapterId = computed(() => {
  const idx = currentIndex.value
  if (idx <= 0) return null
  return allChapters.value[idx - 1]?.id
})
const nextChapterId = computed(() => {
  const idx = currentIndex.value
  if (idx < 0 || idx >= allChapters.value.length - 1) return null
  const next = allChapters.value[idx + 1]
  if (!next) return null
  return next.canLearn || next.completed || canManage.value ? next.id : null
})
const isAllCompleted = computed(() =>
  allChapters.value.length > 0 && allChapters.value.every((c) => c.completed)
)

function renderContent(raw: string) {
  if (!raw) return ''
  let html = raw
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
  html = html.replace(/```(\w*)\n([\s\S]*?)```/g, (_m, lang, code) => {
    return `<pre class="code-block"><code class="language-${lang || 'text'}">${code.trim()}</code></pre>`
  })
  html = html.replace(/`([^`]+)`/g, '<code class="inline-code">$1</code>')
  html = html.replace(/^###### (.*)$/gm, '<h6>$1</h6>')
  html = html.replace(/^##### (.*)$/gm, '<h5>$1</h5>')
  html = html.replace(/^#### (.*)$/gm, '<h4>$1</h4>')
  html = html.replace(/^### (.*)$/gm, '<h3>$1</h3>')
  html = html.replace(/^## (.*)$/gm, '<h2>$1</h2>')
  html = html.replace(/^# (.*)$/gm, '<h1>$1</h1>')
  html = html.replace(/^\*\*\*(.*)\*\*\*$/gm, '<hr/>')
  html = html.replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
  html = html.replace(/\*(.+?)\*/g, '<em>$1</em>')
  html = html.replace(/\[(.+?)\]\((.+?)\)/g, '<a href="$2" target="_blank" rel="noopener">$1</a>')
  html = html.replace(/^\s*[-*+]\s+(.*)$/gm, '<li>$1</li>')
  html = html.replace(/(?:<li>.*<\/li>\n?)+/g, (m) => `<ul>${m}</ul>`)
  html = html.replace(/^\s*\d+\.\s+(.*)$/gm, '<li>$1</li>')
  html = html.replace(/(?:<li>.*<\/li>\n?)+/g, (m) => {
    return m.includes('<ul>') ? m : `<ol>${m}</ol>`
  })
  html = html.replace(/\n{2,}/g, '</p><p>')
  html = html.replace(/\n/g, '<br/>')
  html = `<p>${html}</p>`
  return html
}

async function loadAll() {
  loading.value = true
  try {
    const [courseInfo, chapters, detail] = await Promise.all([
      courseApi.getCourseById(courseId.value),
      courseApi.getChaptersByCourse(courseId.value),
      courseApi.getChapterDetail(chapterId.value)
    ])
    courseTitle.value = courseInfo?.title || ''
    allChapters.value = chapters || []
    chapterTitle.value = detail?.chapter?.title || ''
    chapterContent.value = detail?.chapter?.content || ''
    isCompleted.value = !!detail?.chapter?.completed
    canLearn.value = canManage.value || isCompleted.value
    if (isCompleted.value) {
      canLearn.value = true
    } else if (!canManage.value) {
      canLearn.value = allChapters.value.find((c) => c.id === chapterId.value)?.canLearn || false
    }
    completeTime.value = detail?.chapter?.completeTime || ''
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

async function markComplete() {
  marking.value = true
  try {
    await courseApi.markChapterComplete(chapterId.value)
    ElMessage.success('标记完成成功！')
    isCompleted.value = true
    completeTime.value = new Date().toISOString().replace('T', ' ').slice(0, 16)
    await loadAll()
  } catch (e) {
    console.error(e)
  } finally {
    marking.value = false
  }
}

function onSideChapter(ch: CourseChapter) {
  if (ch.id === chapterId.value) return
  if (canManage.value || ch.canLearn || ch.completed) {
    goChapter(ch.id)
  } else {
    ElMessage.warning('请先完成前置章节')
  }
}

function goChapter(id: number) {
  router.push(`/courses/${courseId.value}/chapter/${id}`)
}

function goBack() {
  router.push(`/courses/${courseId.value}`)
}

function formatTime(t?: string) {
  if (!t) return ''
  return t.replace('T', ' ').slice(0, 16)
}

watch(
  () => [route.params.courseId, route.params.chapterId],
  () => {
    loadAll()
  }
)

onMounted(() => {
  loadAll()
})
</script>

<style scoped>
.chapter-learn {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.nav-bar {
  padding: 14px 20px;
  display: grid;
  grid-template-columns: 1fr auto 1fr;
  align-items: center;
  gap: 16px;
}

.nav-center {
  text-align: center;
  font-size: 13px;
}

.nav-right {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.learn-grid {
  display: grid;
  grid-template-columns: 1fr 300px;
  gap: 20px;
  align-items: start;
}

@media (max-width: 1100px) {
  .learn-grid {
    grid-template-columns: 1fr;
  }
}

.chapter-panel, .sider-panel {
  padding: 24px;
}

.chapter-info {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
  margin-bottom: 10px;
}

.chapter-time {
  color: #909399;
  font-size: 12px;
  margin-left: auto;
}

.chapter-title {
  margin: 4px 0 20px 0;
  font-size: 24px;
  font-weight: 700;
  color: #303133;
}

.glass-card-inner {
  background: rgba(255, 255, 255, 0.6);
  padding: 24px;
  border-radius: 12px;
  border: 1px solid rgba(0, 0, 0, 0.04);
}

.chapter-content {
  min-height: 320px;
  line-height: 1.85;
  color: #303133;
  font-size: 15px;
  word-break: break-word;
}

.chapter-content :deep(h1) {
  font-size: 22px;
  margin: 24px 0 14px;
  font-weight: 700;
  padding-bottom: 8px;
  border-bottom: 2px solid #409eff22;
  color: #1f2937;
}
.chapter-content :deep(h2) {
  font-size: 19px;
  margin: 20px 0 12px;
  font-weight: 700;
  color: #1f2937;
}
.chapter-content :deep(h3) {
  font-size: 17px;
  margin: 18px 0 10px;
  font-weight: 600;
  color: #1f2937;
}
.chapter-content :deep(h4),
.chapter-content :deep(h5),
.chapter-content :deep(h6) {
  margin: 14px 0 8px;
  font-weight: 600;
  color: #374151;
}
.chapter-content :deep(p) {
  margin: 10px 0;
}
.chapter-content :deep(ul),
.chapter-content :deep(ol) {
  margin: 10px 0;
  padding-left: 24px;
}
.chapter-content :deep(li) {
  margin: 6px 0;
}
.chapter-content :deep(strong) {
  color: #1d4ed8;
  font-weight: 600;
}
.chapter-content :deep(a) {
  color: #409eff;
  text-decoration: none;
}
.chapter-content :deep(a:hover) {
  text-decoration: underline;
}
.chapter-content :deep(.code-block) {
  background: #1e293b;
  color: #e2e8f0;
  padding: 14px 18px;
  border-radius: 8px;
  overflow-x: auto;
  font-family: 'JetBrains Mono', Consolas, monospace;
  font-size: 13px;
  line-height: 1.6;
  margin: 14px 0;
}
.chapter-content :deep(.inline-code) {
  background: #f1f5f9;
  color: #be123c;
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'JetBrains Mono', Consolas, monospace;
  font-size: 13px;
}
.chapter-content :deep(hr) {
  margin: 24px 0;
  border: none;
  border-top: 1px solid #e2e8f0;
}

.complete-section {
  margin-top: 28px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 24px;
  background: rgba(103, 194, 58, 0.08);
  border-radius: 12px;
  border: 1px dashed rgba(103, 194, 58, 0.4);
}

.complete-tip {
  margin: 0;
  font-size: 12px;
  color: #909399;
}

.complete-done {
  margin-top: 20px;
}

.sider-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 14px;
  padding-bottom: 12px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
}

.sider-list {
  max-height: 640px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding-right: 4px;
}

.sider-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.15s;
  font-size: 13px;
}
.sider-item:hover {
  background: rgba(64, 158, 255, 0.08);
}
.sider-item.active {
  background: rgba(64, 158, 255, 0.15);
  color: #409eff;
  font-weight: 600;
}
.sider-item.completed {
  color: #67c23a;
}
.sider-item.locked {
  opacity: 0.55;
  cursor: not-allowed;
}

.sider-idx {
  flex-shrink: 0;
  width: 22px;
  height: 22px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.idx-num {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background: #e4e7ed;
  color: #606266;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
}

.sider-item.active .idx-num {
  background: #409eff;
  color: #fff;
}

.sider-text {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
