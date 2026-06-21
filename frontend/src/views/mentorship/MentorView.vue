<template>
  <div class="mentorship-page">
    <el-row :gutter="20">
      <el-col :span="24">
        <div class="glass-card page-header">
          <div class="header-left">
            <h2>导师答疑 - 待办处理</h2>
            <p class="subtitle">查看并处理学生的答疑预约</p>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="stats-row">
      <el-col :span="8">
        <div class="stat-card glass-card hover-lift">
          <div class="stat-label">待处理</div>
          <div class="stat-value text-warning">{{ pendingAppointments.length }}<span class="stat-unit">条</span></div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="stat-card glass-card hover-lift">
          <div class="stat-label">已确认</div>
          <div class="stat-value text-success">{{ confirmedCount }}<span class="stat-unit">条</span></div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="stat-card glass-card hover-lift">
          <div class="stat-label">已拒绝</div>
          <div class="stat-value text-danger">{{ rejectedCount }}<span class="stat-unit">条</span></div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="mt-20">
      <el-col :span="24">
        <el-tabs v-model="activeTab" type="border-card">
          <el-tab-pane label="待处理预约" name="pending">
            <div class="glass-card">
              <div class="card-header">
                <h3>待处理预约</h3>
              </div>
              <el-table :data="pendingAppointments" v-loading="loading" style="width: 100%">
                <el-table-column prop="studentName" label="学生" width="120" />
                <el-table-column prop="startTime" label="开始时间" width="170">
                  <template #default="{ row }">{{ formatDateTime(row.startTime) }}</template>
                </el-table-column>
                <el-table-column prop="endTime" label="结束时间" width="170">
                  <template #default="{ row }">{{ formatDateTime(row.endTime) }}</template>
                </el-table-column>
                <el-table-column prop="status" label="状态" width="100" align="center">
                  <template #default>
                    <el-tag type="warning">待处理</el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="180" align="center" fixed="right">
                  <template #default="{ row }">
                    <el-button type="success" link @click="handleAppointment(row, 'CONFIRMED')">
                      <el-icon><Check /></el-icon>确认
                    </el-button>
                    <el-button type="danger" link @click="handleAppointment(row, 'REJECTED')">
                      <el-icon><Close /></el-icon>拒绝
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
              <el-empty v-if="pendingAppointments.length === 0 && !loading" description="暂无待处理预约" />
            </div>
          </el-tab-pane>

          <el-tab-pane label="已处理记录" name="handled">
            <div class="glass-card">
              <div class="card-header">
                <h3>已处理记录</h3>
              </div>
              <el-table :data="handledAppointments" style="width: 100%">
                <el-table-column prop="studentName" label="学生" width="120" />
                <el-table-column prop="startTime" label="开始时间" width="170">
                  <template #default="{ row }">{{ formatDateTime(row.startTime) }}</template>
                </el-table-column>
                <el-table-column prop="endTime" label="结束时间" width="170">
                  <template #default="{ row }">{{ formatDateTime(row.endTime) }}</template>
                </el-table-column>
                <el-table-column prop="status" label="状态" width="100" align="center">
                  <template #default="{ row }">
                    <el-tag :type="getAppointmentStatusType(row.status)">
                      {{ getAppointmentStatusText(row.status) }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="rejectReason" label="拒绝原因" min-width="150" show-overflow-tooltip>
                  <template #default="{ row }">
                    <span v-if="row.rejectReason" style="color: #f56c6c">{{ row.rejectReason }}</span>
                    <span v-else style="color: #c0c4cc">—</span>
                  </template>
                </el-table-column>
              </el-table>
              <el-empty v-if="handledAppointments.length === 0" description="暂无已处理记录" />
            </div>
          </el-tab-pane>
        </el-tabs>
      </el-col>
    </el-row>

    <el-dialog v-model="rejectDialogVisible" title="拒绝预约" width="480px">
      <el-form :model="rejectForm" :rules="rejectRules" ref="rejectFormRef" label-width="80px">
        <el-form-item label="拒绝原因" prop="rejectReason">
          <el-input v-model="rejectForm.rejectReason" type="textarea" :rows="3" placeholder="请输入拒绝原因" maxlength="500" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmReject">确认拒绝</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElForm } from 'element-plus'
import { Check, Close } from '@element-plus/icons-vue'
import { useUserStore } from '../../store/user'
import mentorshipApi, { type ClubMentor, type MentorAppointment } from '../../api/mentorship'

const userStore = useUserStore()

const activeTab = ref('pending')
const loading = ref(false)
const pendingAppointments = ref<MentorAppointment[]>([])
const allAppointments = ref<MentorAppointment[]>([])
const myMentorId = ref<number | null>(null)

const rejectDialogVisible = ref(false)
const rejectFormRef = ref<InstanceType<typeof ElForm>>()
const currentRejectAppointmentId = ref<number | null>(null)

const rejectForm = reactive({
  rejectReason: ''
})

const rejectRules = {
  rejectReason: [{ required: true, message: '请输入拒绝原因', trigger: 'blur' }]
}

const confirmedCount = computed(() => allAppointments.value.filter((a: MentorAppointment) => a.status === 'CONFIRMED').length)
const rejectedCount = computed(() => allAppointments.value.filter((a: MentorAppointment) => a.status === 'REJECTED').length)

const handledAppointments = computed(() =>
  allAppointments.value.filter((a: MentorAppointment) => a.status !== 'PENDING')
)

const loadMyMentor = async () => {
  try {
    const data = await mentorshipApi.getMentors()
    const mentors = data as unknown as ClubMentor[]
    const myMentor = mentors.find((m: ClubMentor) => m.userId === userStore.userInfo?.id)
    if (myMentor) {
      myMentorId.value = myMentor.id
      loadAppointments()
    }
  } catch (err) {
    console.error('Failed to load mentor info:', err)
  }
}

const loadAppointments = async () => {
  if (!myMentorId.value) return
  loading.value = true
  try {
    const data = await mentorshipApi.getPendingByMentor(myMentorId.value)
    pendingAppointments.value = data as unknown as MentorAppointment[]
    allAppointments.value = pendingAppointments.value
  } catch (err) {
    console.error('Failed to load appointments:', err)
  } finally {
    loading.value = false
  }
}

const handleAppointment = async (row: MentorAppointment, status: 'CONFIRMED' | 'REJECTED') => {
  if (status === 'REJECTED') {
    currentRejectAppointmentId.value = row.id
    rejectForm.rejectReason = ''
    rejectDialogVisible.value = true
    return
  }
  try {
    await mentorshipApi.handleAppointment(row.id, { status: 'CONFIRMED' })
    ElMessage.success('已确认预约')
    loadAppointments()
  } catch (err) {
    console.error('Failed to confirm appointment:', err)
  }
}

const confirmReject = async () => {
  if (!rejectFormRef.value || !currentRejectAppointmentId.value) return
  try {
    await rejectFormRef.value.validate()
    await mentorshipApi.handleAppointment(currentRejectAppointmentId.value, {
      status: 'REJECTED',
      rejectReason: rejectForm.rejectReason
    })
    ElMessage.success('已拒绝预约')
    rejectDialogVisible.value = false
    loadAppointments()
  } catch (err) {
    console.error('Failed to reject appointment:', err)
  }
}

const getAppointmentStatusType = (status: string) => {
  const map: Record<string, 'success' | 'warning' | 'danger' | 'info'> = {
    CONFIRMED: 'success',
    PENDING: 'warning',
    REJECTED: 'danger'
  }
  return map[status] || 'info'
}

const getAppointmentStatusText = (status: string) => {
  const map: Record<string, string> = {
    CONFIRMED: '已确认',
    PENDING: '待处理',
    REJECTED: '已拒绝'
  }
  return map[status] || status
}

const formatDateTime = (dateStr: string) => {
  return dateStr ? new Date(dateStr).toLocaleString('zh-CN') : ''
}

onMounted(() => {
  loadMyMentor()
})
</script>

<style scoped>
.mentorship-page {
  padding: 0;
}
.page-header {
  padding: 24px 30px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.page-header h2 {
  margin: 0;
  font-size: 22px;
  color: #303133;
}
.subtitle {
  margin: 6px 0 0 0;
  color: #909399;
  font-size: 14px;
}
.stats-row {
  margin-top: 20px;
}
.stat-card {
  padding: 24px;
  text-align: center;
}
.stat-label {
  font-size: 14px;
  color: #606266;
  margin-bottom: 12px;
}
.stat-value {
  font-size: 32px;
  font-weight: bold;
}
.stat-unit {
  font-size: 14px;
  font-weight: normal;
  margin-left: 4px;
  color: #909399;
}
.text-primary { color: #409eff; }
.text-success { color: #67c23a; }
.text-warning { color: #e6a23c; }
.text-danger { color: #f56c6c; }
.mt-20 { margin-top: 20px; }
.card-header {
  padding: 20px 24px;
  border-bottom: 1px solid rgba(0,0,0,0.05);
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.card-header h3 {
  margin: 0;
  font-size: 16px;
  color: #303133;
}
:deep(.el-table) {
  --el-table-bg-color: transparent;
  --el-table-tr-bg-color: transparent;
  --el-table-header-bg-color: rgba(0,0,0,0.02);
}
:deep(.el-tabs--border-card) {
  background: transparent;
  border: none;
}
:deep(.el-tabs--border-card > .el-tabs__header) {
  background: rgba(255, 255, 255, 0.8);
  border: none;
  border-radius: 12px 12px 0 0;
  margin-bottom: 0;
}
</style>
