<template>
  <div class="mentorship-page">
    <el-row :gutter="20">
      <el-col :span="24">
        <div class="glass-card page-header">
          <div class="header-left">
            <h2>导师答疑预约</h2>
            <p class="subtitle">浏览导师信息，预约答疑时段</p>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <div class="stat-card glass-card hover-lift">
          <div class="stat-label">可预约时段</div>
          <div class="stat-value text-success">{{ availableSlots.length }}<span class="stat-unit">个</span></div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card glass-card hover-lift">
          <div class="stat-label">待确认</div>
          <div class="stat-value text-warning">{{ pendingCount }}<span class="stat-unit">条</span></div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card glass-card hover-lift">
          <div class="stat-label">已确认</div>
          <div class="stat-value text-primary">{{ confirmedCount }}<span class="stat-unit">条</span></div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card glass-card hover-lift">
          <div class="stat-label">已拒绝</div>
          <div class="stat-value text-danger">{{ rejectedCount }}<span class="stat-unit">条</span></div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="mt-20">
      <el-col :span="24">
        <el-tabs v-model="activeTab" type="border-card">
          <el-tab-pane label="预约导师" name="book">
            <div class="glass-card">
              <div class="card-header">
                <h3>可预约时段</h3>
                <div class="filters">
                  <el-select v-model="filterClubId" placeholder="选择社团" style="width: 180px" clearable @change="loadAvailableSlots">
                    <el-option v-for="c in clubs" :key="c.id" :label="c.name" :value="c.id" />
                  </el-select>
                </div>
              </div>
              <el-table :data="availableSlots" v-loading="slotLoading" style="width: 100%">
                <el-table-column prop="mentorName" label="导师" width="120" />
                <el-table-column prop="startTime" label="开始时间" width="170">
                  <template #default="{ row }">{{ formatDateTime(row.startTime) }}</template>
                </el-table-column>
                <el-table-column prop="endTime" label="结束时间" width="170">
                  <template #default="{ row }">{{ formatDateTime(row.endTime) }}</template>
                </el-table-column>
                <el-table-column label="操作" width="120" align="center">
                  <template #default="{ row }">
                    <el-button type="primary" link @click="bookSlot(row)">预约</el-button>
                  </template>
                </el-table-column>
              </el-table>
              <el-empty v-if="availableSlots.length === 0 && !slotLoading" description="暂无可预约时段" />
            </div>
          </el-tab-pane>

          <el-tab-pane label="我的预约" name="my">
            <div class="glass-card">
              <div class="card-header">
                <h3>我的预约记录</h3>
              </div>
              <el-table :data="myAppointments" v-loading="appointmentLoading" style="width: 100%">
                <el-table-column prop="mentorName" label="导师" width="120" />
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
                <el-table-column label="操作" width="100" align="center">
                  <template #default="{ row }">
                    <el-button v-if="row.status === 'PENDING' || row.status === 'CONFIRMED'" type="danger" link @click="cancelAppointment(row)">取消</el-button>
                    <span v-else style="color: #909399; font-size: 13px">—</span>
                  </template>
                </el-table-column>
              </el-table>
              <el-empty v-if="myAppointments.length === 0 && !appointmentLoading" description="暂无预约记录" />
            </div>
          </el-tab-pane>
        </el-tabs>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import mentorshipApi, { type MentorSlot, type MentorAppointment } from '../../api/mentorship'
import request from '../../utils/request'

const activeTab = ref('book')
const slotLoading = ref(false)
const appointmentLoading = ref(false)

const availableSlots = ref<MentorSlot[]>([])
const myAppointments = ref<MentorAppointment[]>([])
const filterClubId = ref<number | undefined>(undefined)

const clubs = ref<{ id: number; name: string }[]>([])

const pendingCount = computed(() => myAppointments.value.filter((a: MentorAppointment) => a.status === 'PENDING').length)
const confirmedCount = computed(() => myAppointments.value.filter((a: MentorAppointment) => a.status === 'CONFIRMED').length)
const rejectedCount = computed(() => myAppointments.value.filter((a: MentorAppointment) => a.status === 'REJECTED').length)

const loadClubs = async () => {
  try {
    const data = await request.get('/clubs')
    clubs.value = (data as any[]).map((c: any) => ({ id: c.id, name: c.name }))
  } catch (err) {
    console.error('Failed to load clubs:', err)
  }
}

const loadAvailableSlots = async () => {
  slotLoading.value = true
  try {
    const data = await mentorshipApi.getAvailableSlots(filterClubId.value)
    availableSlots.value = data as unknown as MentorSlot[]
  } catch (err) {
    console.error('Failed to load available slots:', err)
  } finally {
    slotLoading.value = false
  }
}

const loadMyAppointments = async () => {
  appointmentLoading.value = true
  try {
    const data = await mentorshipApi.getMyAppointments()
    myAppointments.value = data as unknown as MentorAppointment[]
  } catch (err) {
    console.error('Failed to load my appointments:', err)
  } finally {
    appointmentLoading.value = false
  }
}

const bookSlot = async (row: MentorSlot) => {
  try {
    await ElMessageBox.confirm(
      `确定要预约导师 ${row.mentorName} 的 ${formatDateTime(row.startTime)} - ${formatTime(row.endTime)} 时段吗？`,
      '确认预约',
      { type: 'info', confirmButtonText: '确定', cancelButtonText: '取消' }
    )
    await mentorshipApi.createAppointment({ slotId: row.id })
    ElMessage.success('预约提交成功，等待导师确认')
    loadAvailableSlots()
    loadMyAppointments()
  } catch (err) {
    if (err !== 'cancel') {
      console.error('Failed to book slot:', err)
    }
  }
}

const cancelAppointment = async (row: MentorAppointment) => {
  try {
    await ElMessageBox.confirm('确定要取消此预约吗？取消后时段将释放。', '确认取消', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    await mentorshipApi.cancelAppointment(row.id)
    ElMessage.success('预约已取消')
    loadMyAppointments()
    loadAvailableSlots()
  } catch (err) {
    if (err !== 'cancel') {
      console.error('Failed to cancel appointment:', err)
    }
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
    PENDING: '待确认',
    REJECTED: '已拒绝'
  }
  return map[status] || status
}

const formatDateTime = (dateStr: string) => {
  return dateStr ? new Date(dateStr).toLocaleString('zh-CN') : ''
}

const formatTime = (dateStr: string) => {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const pad = (n: number) => n.toString().padStart(2, '0')
  return `${pad(d.getHours())}:${pad(d.getMinutes())}`
}

onMounted(() => {
  loadClubs()
  loadAvailableSlots()
  loadMyAppointments()
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
.text-info { color: #909399; }
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
.filters {
  display: flex;
  gap: 12px;
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
