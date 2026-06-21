<template>
  <div class="venue-page">
    <el-row :gutter="20">
      <el-col :span="24">
        <div class="glass-card page-header">
          <div class="header-left">
            <h2>场地预约 - 管理员</h2>
            <p class="subtitle">管理场地信息，审批预约申请</p>
          </div>
          <div class="header-right">
            <el-button type="primary" @click="openVenueDialog">
              <el-icon><Plus /></el-icon>
              新增场地
            </el-button>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <div class="stat-card glass-card hover-lift">
          <div class="stat-label">可用场地</div>
          <div class="stat-value text-success">{{ availableCount }}<span class="stat-unit">个</span></div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card glass-card hover-lift">
          <div class="stat-label">待审批预约</div>
          <div class="stat-value text-warning">{{ pendingCount }}<span class="stat-unit">条</span></div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card glass-card hover-lift">
          <div class="stat-label">本周预约</div>
          <div class="stat-value text-primary">{{ thisWeekCount }}<span class="stat-unit">条</span></div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card glass-card hover-lift">
          <div class="stat-label">累计预约</div>
          <div class="stat-value text-info">{{ totalBookings }}<span class="stat-unit">条</span></div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="mt-20">
      <el-col :span="24">
        <el-tabs v-model="activeTab" type="border-card">
          <el-tab-pane label="场地管理" name="venues">
            <div class="glass-card">
              <div class="card-header">
                <h3>场地列表</h3>
              </div>
              <el-table :data="venues" v-loading="venueLoading" style="width: 100%">
                <el-table-column prop="name" label="场地名称" min-width="150" />
                <el-table-column prop="capacity" label="容量" width="100" align="center" />
                <el-table-column prop="location" label="位置" min-width="200" />
                <el-table-column prop="status" label="状态" width="100" align="center">
                  <template #default="{ row }">
                    <el-tag :type="row.status === 'AVAILABLE' ? 'success' : 'danger'">
                      {{ row.status === 'AVAILABLE' ? '可用' : '不可用' }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="180" align="center" fixed="right">
                  <template #default="{ row }">
                    <el-button type="primary" link @click="editVenue(row)">编辑</el-button>
                    <el-button type="danger" link @click="deleteVenue(row)">删除</el-button>
                  </template>
                </el-table-column>
              </el-table>
              <el-empty v-if="venues.length === 0 && !venueLoading" description="暂无场地" />
            </div>
          </el-tab-pane>

          <el-tab-pane label="预约审批" name="bookings">
            <div class="glass-card">
              <div class="card-header">
                <h3>待审批预约</h3>
              </div>
              <el-table :data="pendingBookings" v-loading="bookingLoading" style="width: 100%">
                <el-table-column prop="venueName" label="场地" min-width="120" />
                <el-table-column prop="clubName" label="社团" min-width="120" />
                <el-table-column prop="applicantName" label="申请人" width="100" />
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
                <el-table-column label="操作" width="180" align="center" fixed="right">
                  <template #default="{ row }">
                    <el-button type="success" link @click="openAuditDialog(row, 'APPROVED')">
                      <el-icon><Check /></el-icon>
                      通过
                    </el-button>
                    <el-button type="danger" link @click="openAuditDialog(row, 'REJECTED')">
                      <el-icon><Close /></el-icon>
                      驳回
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
              <el-empty v-if="pendingBookings.length === 0 && !bookingLoading" description="暂无待审批预约" />
            </div>
          </el-tab-pane>

          <el-tab-pane label="全部预约" name="allBookings">
            <div class="glass-card">
              <div class="card-header">
                <h3>全部预约记录</h3>
                <div class="filters">
                  <el-select v-model="filterStatus" placeholder="状态筛选" style="width: 140px" clearable>
                    <el-option label="待审批" value="PENDING" />
                    <el-option label="已通过" value="APPROVED" />
                    <el-option label="已驳回" value="REJECTED" />
                  </el-select>
                </div>
              </div>
              <el-table :data="filteredAllBookings" v-loading="allBookingLoading" style="width: 100%">
                <el-table-column prop="venueName" label="场地" min-width="120" />
                <el-table-column prop="clubName" label="社团" min-width="120" />
                <el-table-column prop="applicantName" label="申请人" width="100" />
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
                <el-table-column prop="auditorName" label="审批人" width="100" />
                <el-table-column label="操作" width="100" align="center">
                  <template #default="{ row }">
                    <el-button type="primary" link @click="viewBookingDetail(row)">查看</el-button>
                  </template>
                </el-table-column>
              </el-table>
              <el-empty v-if="filteredAllBookings.length === 0 && !allBookingLoading" description="暂无预约记录" />
            </div>
          </el-tab-pane>
        </el-tabs>
      </el-col>
    </el-row>

    <el-dialog v-model="venueDialogVisible" :title="isEditVenue ? '编辑场地' : '新增场地'" width="520px">
      <el-form :model="venueForm" :rules="venueRules" ref="venueFormRef" label-width="100px">
        <el-form-item label="场地名称" prop="name">
          <el-input v-model="venueForm.name" placeholder="请输入场地名称" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="容量" prop="capacity">
          <el-input-number v-model="venueForm.capacity" :min="1" :max="10000" style="width: 100%" placeholder="请输入容纳人数" />
        </el-form-item>
        <el-form-item label="位置" prop="location">
          <el-input v-model="venueForm.location" placeholder="请输入位置描述" maxlength="255" show-word-limit />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="venueForm.status">
            <el-radio value="AVAILABLE">可用</el-radio>
            <el-radio value="UNAVAILABLE">不可用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="venueDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveVenue">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="auditDialogVisible" :title="auditTitle" width="520px">
      <div v-if="currentBooking" class="audit-preview">
        <el-descriptions :column="1" border size="small">
          <el-descriptions-item label="场地">{{ currentBooking.venueName }}</el-descriptions-item>
          <el-descriptions-item label="社团">{{ currentBooking.clubName }}</el-descriptions-item>
          <el-descriptions-item label="申请人">{{ currentBooking.applicantName }}</el-descriptions-item>
          <el-descriptions-item label="开始时间">{{ formatDateTime(currentBooking.startTime) }}</el-descriptions-item>
          <el-descriptions-item label="结束时间">{{ formatDateTime(currentBooking.endTime) }}</el-descriptions-item>
          <el-descriptions-item label="使用目的">{{ currentBooking.purpose }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <el-form
        v-if="auditStatus === 'REJECTED'"
        :model="auditForm"
        :rules="auditRules"
        ref="auditFormRef"
        style="margin-top: 20px"
        label-width="80px"
      >
        <el-form-item label="驳回原因" prop="rejectReason">
          <el-input
            v-model="auditForm.rejectReason"
            type="textarea"
            :rows="3"
            placeholder="请输入驳回原因"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="auditDialogVisible = false">取消</el-button>
        <el-button :type="auditStatus === 'APPROVED' ? 'success' : 'danger'" @click="handleAudit">
          确认{{ auditStatus === 'APPROVED' ? '通过' : '驳回' }}
        </el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="bookingDetailVisible" title="预约详情" width="480px">
      <el-descriptions :column="1" border v-if="currentBooking">
        <el-descriptions-item label="场地">{{ currentBooking.venueName }}</el-descriptions-item>
        <el-descriptions-item label="社团">{{ currentBooking.clubName }}</el-descriptions-item>
        <el-descriptions-item label="申请人">{{ currentBooking.applicantName }}</el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ formatDateTime(currentBooking.startTime) }}</el-descriptions-item>
        <el-descriptions-item label="结束时间">{{ formatDateTime(currentBooking.endTime) }}</el-descriptions-item>
        <el-descriptions-item label="使用目的">{{ currentBooking.purpose }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentBooking.status)">
            {{ getStatusText(currentBooking.status) }}
          </el-tag>
        </el-descriptions-item>
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
import { ElMessage, ElForm, ElMessageBox } from 'element-plus'
import { Plus, Check, Close } from '@element-plus/icons-vue'
import venueApi, { type Venue, type VenueBooking, type BookingAuditRequest } from '../../api/venues'

const activeTab = ref('venues')
const filterStatus = ref('')

const venueLoading = ref(false)
const bookingLoading = ref(false)
const allBookingLoading = ref(false)

const venues = ref<Venue[]>([])
const pendingBookings = ref<VenueBooking[]>([])
const allBookings = ref<VenueBooking[]>([])

const venueDialogVisible = ref(false)
const isEditVenue = ref(false)
const currentEditVenueId = ref<number | null>(null)
const venueFormRef = ref<InstanceType<typeof ElForm>>()

const auditDialogVisible = ref(false)
const bookingDetailVisible = ref(false)
const currentBooking = ref<VenueBooking | null>(null)
const auditStatus = ref<'APPROVED' | 'REJECTED'>('APPROVED')
const auditFormRef = ref<InstanceType<typeof ElForm>>()

const venueForm = reactive<Venue>({
  id: 0,
  name: '',
  capacity: 50,
  location: '',
  status: 'AVAILABLE',
  createTime: '',
  isDeleted: 0
})

const venueRules = {
  name: [{ required: true, message: '请输入场地名称', trigger: 'blur' }],
  capacity: [{ required: true, message: '请输入容量', trigger: 'blur' }],
  location: [{ required: true, message: '请输入位置', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

const auditForm = reactive<BookingAuditRequest>({
  status: 'APPROVED',
  rejectReason: ''
})

const auditRules = {
  rejectReason: [{ required: true, message: '请输入驳回原因', trigger: 'blur' }]
}

const availableCount = computed(() => venues.value.filter((v: Venue) => v.status === 'AVAILABLE').length)
const pendingCount = computed(() => pendingBookings.value.length)
const totalBookings = computed(() => allBookings.value.length)
const thisWeekCount = computed(() => {
  const now = new Date()
  const weekStart = new Date(now)
  weekStart.setDate(now.getDate() - now.getDay())
  weekStart.setHours(0, 0, 0, 0)
  const weekEnd = new Date(weekStart)
  weekEnd.setDate(weekStart.getDate() + 7)

  return allBookings.value.filter((b: VenueBooking) => {
    const startTime = new Date(b.startTime)
    return startTime >= weekStart && startTime < weekEnd
  }).length
})

const auditTitle = computed(() => {
  return auditStatus.value === 'APPROVED' ? '审核通过' : '审核驳回'
})

const filteredAllBookings = computed(() => {
  if (!filterStatus.value) return allBookings.value
  return allBookings.value.filter((b: VenueBooking) => b.status === filterStatus.value)
})

const loadVenues = async () => {
  venueLoading.value = true
  try {
    const data = await venueApi.getVenues()
    venues.value = data as unknown as Venue[]
  } catch (err) {
    console.error('Failed to load venues:', err)
  } finally {
    venueLoading.value = false
  }
}

const loadPendingBookings = async () => {
  bookingLoading.value = true
  try {
    const data = await venueApi.getPendingBookings()
    pendingBookings.value = data as unknown as VenueBooking[]
  } catch (err) {
    console.error('Failed to load pending bookings:', err)
  } finally {
    bookingLoading.value = false
  }
}

const loadAllBookings = async () => {
  allBookingLoading.value = true
  try {
    const data = await venueApi.getAllBookings()
    allBookings.value = data as unknown as VenueBooking[]
  } catch (err) {
    console.error('Failed to load all bookings:', err)
  } finally {
    allBookingLoading.value = false
  }
}

const openVenueDialog = () => {
  isEditVenue.value = false
  currentEditVenueId.value = null
  venueForm.id = 0
  venueForm.name = ''
  venueForm.capacity = 50
  venueForm.location = ''
  venueForm.status = 'AVAILABLE'
  venueDialogVisible.value = true
}

const editVenue = (row: Venue) => {
  isEditVenue.value = true
  currentEditVenueId.value = row.id
  venueForm.id = row.id
  venueForm.name = row.name
  venueForm.capacity = row.capacity
  venueForm.location = row.location
  venueForm.status = row.status
  venueDialogVisible.value = true
}

const deleteVenue = async (row: Venue) => {
  try {
    await ElMessageBox.confirm(`确定要删除场地"${row.name}"吗？`, '确认删除', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    await venueApi.deleteVenue(row.id)
    ElMessage.success('删除成功')
    loadVenues()
  } catch (err) {
    if (err !== 'cancel') {
      console.error('Failed to delete venue:', err)
    }
  }
}

const handleSaveVenue = async () => {
  if (!venueFormRef.value) return
  try {
    await venueFormRef.value.validate()
    if (isEditVenue.value) {
      await venueApi.updateVenue(venueForm)
      ElMessage.success('更新成功')
    } else {
      await venueApi.createVenue(venueForm)
      ElMessage.success('创建成功')
    }
    venueDialogVisible.value = false
    loadVenues()
  } catch (err) {
    console.error('Failed to save venue:', err)
  }
}

const openAuditDialog = (row: VenueBooking, status: 'APPROVED' | 'REJECTED') => {
  currentBooking.value = row
  auditStatus.value = status
  auditForm.status = status
  auditForm.rejectReason = ''
  auditDialogVisible.value = true
}

const handleAudit = async () => {
  if (!currentBooking.value) return

  if (auditStatus.value === 'REJECTED') {
    if (!auditFormRef.value) return
    try {
      await auditFormRef.value.validate()
    } catch {
      return
    }
  }

  try {
    await venueApi.auditBooking(currentBooking.value.id, {
      status: auditStatus.value,
      rejectReason: auditStatus.value === 'REJECTED' ? auditForm.rejectReason : undefined
    })
    ElMessage.success(`审核${auditStatus.value === 'APPROVED' ? '通过' : '驳回'}成功`)
    auditDialogVisible.value = false
    loadPendingBookings()
    loadAllBookings()
  } catch (err) {
    console.error('Failed to audit:', err)
  }
}

const viewBookingDetail = (row: VenueBooking) => {
  currentBooking.value = row
  bookingDetailVisible.value = true
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
  loadPendingBookings()
  loadAllBookings()
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
.audit-preview {
  margin-bottom: 10px;
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
