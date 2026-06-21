<template>
  <div class="venue-page">
    <el-row :gutter="20">
      <el-col :span="24">
        <div class="glass-card page-header">
          <div class="header-left">
            <h2>场地预约 - 社团负责人</h2>
            <p class="subtitle">提交场地预约申请，查看我的预约</p>
          </div>
          <div class="header-right">
            <el-button type="primary" @click="openBookingDialog">
              <el-icon><Plus /></el-icon>
              提交预约
            </el-button>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <div class="stat-card glass-card hover-lift">
          <div class="stat-label">待审批</div>
          <div class="stat-value text-warning">{{ pendingCount }}<span class="stat-unit">条</span></div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card glass-card hover-lift">
          <div class="stat-label">已通过</div>
          <div class="stat-value text-success">{{ approvedCount }}<span class="stat-unit">条</span></div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card glass-card hover-lift">
          <div class="stat-label">已驳回</div>
          <div class="stat-value text-danger">{{ rejectedCount }}<span class="stat-unit">条</span></div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card glass-card hover-lift">
          <div class="stat-label">累计预约</div>
          <div class="stat-value text-primary">{{ myBookings.length }}<span class="stat-unit">条</span></div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="mt-20">
      <el-col :span="24">
        <div class="glass-card">
          <div class="card-header">
            <h3>我的预约</h3>
            <div class="filters">
              <el-select v-model="filterStatus" placeholder="状态筛选" style="width: 140px" clearable>
                <el-option label="待审批" value="PENDING" />
                <el-option label="已通过" value="APPROVED" />
                <el-option label="已驳回" value="REJECTED" />
              </el-select>
            </div>
          </div>
          <el-table :data="filteredBookings" v-loading="loading" style="width: 100%">
            <el-table-column prop="venueName" label="场地" min-width="120" />
            <el-table-column prop="startTime" label="开始时间" width="170">
              <template #default="{ row }">
                {{ formatDateTime(row.startTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="endTime" label="结束时间" width="170">
              <template #default="{ row }">
                {{ formatDateTime(row.endTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="purpose" label="使用目的" min-width="150" show-overflow-tooltip />
            <el-table-column prop="status" label="状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)">
                  {{ getStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100" align="center">
              <template #default="{ row }">
                <el-button type="primary" link @click="viewDetail(row)">查看</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="filteredBookings.length === 0 && !loading" description="暂无预约记录" />
        </div>
      </el-col>
    </el-row>

    <el-dialog v-model="bookingDialogVisible" title="提交场地预约" width="560px" @close="onDialogClose">
      <el-form :model="bookingForm" :rules="bookingRules" ref="bookingFormRef" label-width="100px">
        <el-form-item label="选择场地" prop="venueId">
          <el-select v-model="bookingForm.venueId" placeholder="请选择场地" style="width: 100%" @change="onVenueChange">
            <el-option
              v-for="venue in availableVenues"
              :key="venue.id"
              :label="`${venue.name} (${venue.location} - 容纳${venue.capacity}人)`"
              :value="venue.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="使用时间" prop="timeRange">
          <el-date-picker
            v-model="timeRange"
            type="datetimerange"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            style="width: 100%"
            value-format="YYYY-MM-DD HH:mm:ss"
            :disabled-date="disabledDate"
            :shortcuts="shortcuts"
            @change="onTimeChange"
          />
        </el-form-item>
        <el-alert
          v-if="hasConflictWarning"
          type="warning"
          :title="conflictWarning"
          show-icon
          style="margin-bottom: 20px"
        />
        <el-form-item label="使用目的" prop="purpose">
          <el-input
            v-model="bookingForm.purpose"
            type="textarea"
            :rows="3"
            placeholder="请简要描述使用目的"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="bookingDialogVisible = false">取消</el-button>
        <el-button type="primary" :disabled="hasConflict" @click="handleSubmit">提交预约</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailDialogVisible" title="预约详情" width="480px">
      <el-descriptions :column="1" border v-if="currentBooking">
        <el-descriptions-item label="场地">{{ currentBooking.venueName }}</el-descriptions-item>
        <el-descriptions-item label="社团">{{ currentBooking.clubName }}</el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ formatDateTime(currentBooking.startTime) }}</el-descriptions-item>
        <el-descriptions-item label="结束时间">{{ formatDateTime(currentBooking.endTime) }}</el-descriptions-item>
        <el-descriptions-item label="使用目的">{{ currentBooking.purpose }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentBooking.status)">
            {{ getStatusText(currentBooking.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="申请人">{{ currentBooking.applicantName }}</el-descriptions-item>
        <el-descriptions-item label="审批人" v-if="currentBooking.auditorName">
          {{ currentBooking.auditorName }}
        </el-descriptions-item>
        <el-descriptions-item label="驳回原因" v-if="currentBooking.rejectReason">
          <span style="color: #f56c6c">{{ currentBooking.rejectReason }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="提交时间">{{ formatDateTime(currentBooking.createTime) }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElForm } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import venueApi, { type Venue, type VenueBooking, type BookingSubmitRequest } from '../../api/venues'

const loading = ref(false)
const myBookings = ref<VenueBooking[]>([])
const venues = ref<Venue[]>([])
const filterStatus = ref('')

const bookingDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const currentBooking = ref<VenueBooking | null>(null)
const bookingFormRef = ref<InstanceType<typeof ElForm>>()
const timeRange = ref<string[]>([])
const hasConflict = ref(false)
const hasConflictWarning = ref(false)
const conflictWarning = ref('')

const bookingForm = reactive<BookingSubmitRequest>({
  venueId: 0,
  startTime: '',
  endTime: '',
  purpose: ''
})

const bookingRules = {
  venueId: [{ required: true, message: '请选择场地', trigger: 'change' }],
  timeRange: [{ required: true, message: '请选择使用时间', trigger: 'change' }],
  purpose: [{ required: true, message: '请填写使用目的', trigger: 'blur' }]
}

const shortcuts = [
  {
    text: '今天',
    value: () => {
      const start = new Date()
      start.setHours(8, 0, 0, 0)
      const end = new Date()
      end.setHours(22, 0, 0, 0)
      return [start, end]
    }
  },
  {
    text: '明天',
    value: () => {
      const start = new Date()
      start.setDate(start.getDate() + 1)
      start.setHours(8, 0, 0, 0)
      const end = new Date()
      end.setDate(end.getDate() + 1)
      end.setHours(22, 0, 0, 0)
      return [start, end]
    }
  }
]

const availableVenues = computed(() => {
  return venues.value.filter((v: Venue) => v.status === 'AVAILABLE')
})

const pendingCount = computed(() => myBookings.value.filter((b: VenueBooking) => b.status === 'PENDING').length)
const approvedCount = computed(() => myBookings.value.filter((b: VenueBooking) => b.status === 'APPROVED').length)
const rejectedCount = computed(() => myBookings.value.filter((b: VenueBooking) => b.status === 'REJECTED').length)

const filteredBookings = computed(() => {
  if (!filterStatus.value) return myBookings.value
  return myBookings.value.filter((b: VenueBooking) => b.status === filterStatus.value)
})

const disabledDate = (time: Date) => {
  return time.getTime() < Date.now() - 8.64e7
}

const loadVenues = async () => {
  try {
    const data = await venueApi.getVenues()
    venues.value = data as unknown as Venue[]
  } catch (err) {
    console.error('Failed to load venues:', err)
  }
}

const loadMyBookings = async () => {
  loading.value = true
  try {
    const data = await venueApi.getMyBookings()
    myBookings.value = data as unknown as VenueBooking[]
  } catch (err) {
    console.error('Failed to load my bookings:', err)
  } finally {
    loading.value = false
  }
}

const checkConflict = async () => {
  if (!bookingForm.venueId || !bookingForm.startTime || !bookingForm.endTime) {
    hasConflict.value = false
    hasConflictWarning.value = false
    return
  }
  try {
    const overlap = await venueApi.checkOverlap(
      bookingForm.venueId,
      bookingForm.startTime,
      bookingForm.endTime
    )
    hasConflict.value = overlap as unknown as boolean
    hasConflictWarning.value = overlap as unknown as boolean
    if (overlap) {
      conflictWarning.value = '该时段已被其他预约占用，请选择其他时段'
    } else {
      conflictWarning.value = ''
    }
  } catch (err) {
    console.error('Failed to check conflict:', err)
  }
}

const onVenueChange = () => {
  checkConflict()
}

const onTimeChange = (val: string[]) => {
  if (val && val.length === 2) {
    bookingForm.startTime = val[0]
    bookingForm.endTime = val[1]
    checkConflict()
  }
}

const openBookingDialog = () => {
  bookingForm.venueId = 0
  bookingForm.startTime = ''
  bookingForm.endTime = ''
  bookingForm.purpose = ''
  timeRange.value = []
  hasConflict.value = false
  hasConflictWarning.value = false
  bookingDialogVisible.value = true
}

const onDialogClose = () => {
  bookingFormRef.value?.resetFields()
}

const handleSubmit = async () => {
  if (!bookingFormRef.value) return
  try {
    await bookingFormRef.value.validate()
    await venueApi.submitBooking(bookingForm)
    ElMessage.success('预约提交成功，等待审批')
    bookingDialogVisible.value = false
    loadMyBookings()
  } catch (err) {
    console.error('Failed to submit booking:', err)
  }
}

const viewDetail = (row: VenueBooking) => {
  currentBooking.value = row
  detailDialogVisible.value = true
}

const getStatusType = (status: string) => {
  const map: Record<string, 'success' | 'warning' | 'danger' | 'info'> = {
    APPROVED: 'success',
    PENDING: 'warning',
    REJECTED: 'danger'
  }
  return map[status] || 'info'
}

const getStatusText = (status: string) => {
  const map: Record<string, string> = {
    APPROVED: '已通过',
    PENDING: '待审批',
    REJECTED: '已驳回'
  }
  return map[status] || status
}

const formatDateTime = (dateStr: string) => {
  return dateStr ? new Date(dateStr).toLocaleString('zh-CN') : ''
}

onMounted(() => {
  loadVenues()
  loadMyBookings()
})
</script>

<style scoped>
.venue-page {
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
.filters {
  display: flex;
  gap: 12px;
}
:deep(.el-table) {
  --el-table-bg-color: transparent;
  --el-table-tr-bg-color: transparent;
  --el-table-header-bg-color: rgba(0,0,0,0.02);
}
</style>
