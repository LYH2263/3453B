<template>
  <div class="mentorship-page">
    <el-row :gutter="20">
      <el-col :span="24">
        <div class="glass-card page-header">
          <div class="header-left">
            <h2>导师制管理</h2>
            <p class="subtitle">管理社团导师与开放时段，处理预约待办</p>
          </div>
          <div class="header-right">
            <el-button type="primary" @click="openMentorDialog">
              <el-icon><Plus /></el-icon>
              添加导师
            </el-button>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <div class="stat-card glass-card hover-lift">
          <div class="stat-label">导师人数</div>
          <div class="stat-value text-primary">{{ mentors.length }}<span class="stat-unit">人</span></div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card glass-card hover-lift">
          <div class="stat-label">可预约时段</div>
          <div class="stat-value text-success">{{ availableSlotCount }}<span class="stat-unit">个</span></div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card glass-card hover-lift">
          <div class="stat-label">待处理预约</div>
          <div class="stat-value text-warning">{{ totalPendingCount }}<span class="stat-unit">条</span></div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card glass-card hover-lift">
          <div class="stat-label">已确认预约</div>
          <div class="stat-value text-info">{{ totalConfirmedCount }}<span class="stat-unit">条</span></div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="mt-20">
      <el-col :span="24">
        <el-tabs v-model="activeTab" type="border-card">
          <el-tab-pane label="导师管理" name="mentors">
            <div class="glass-card">
              <div class="card-header">
                <h3>导师列表</h3>
              </div>
              <el-table :data="mentors" v-loading="mentorLoading" style="width: 100%">
                <el-table-column prop="name" label="姓名" width="120" />
                <el-table-column prop="staffNo" label="工号" width="130" />
                <el-table-column prop="researchArea" label="研究方向" min-width="160" show-overflow-tooltip />
                <el-table-column prop="intro" label="简介" min-width="200" show-overflow-tooltip />
                <el-table-column prop="linkedUsername" label="关联账号" width="120">
                  <template #default="{ row }">
                    {{ row.linkedUsername || '未关联' }}
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="220" align="center" fixed="right">
                  <template #default="{ row }">
                    <el-button type="primary" link @click="openSlotDialog(row)">开放时段</el-button>
                    <el-button type="warning" link @click="editMentor(row)">编辑</el-button>
                    <el-button type="danger" link @click="deleteMentor(row)">删除</el-button>
                  </template>
                </el-table-column>
              </el-table>
              <el-empty v-if="mentors.length === 0 && !mentorLoading" description="暂无导师" />
            </div>
          </el-tab-pane>

          <el-tab-pane label="时段管理" name="slots">
            <div class="glass-card">
              <div class="card-header">
                <h3>导师时段</h3>
                <div class="filters">
                  <el-select v-model="selectedMentorId" placeholder="选择导师" style="width: 180px" clearable @change="loadSlots">
                    <el-option v-for="m in mentors" :key="m.id" :label="m.name" :value="m.id" />
                  </el-select>
                </div>
              </div>
              <el-table :data="slots" v-loading="slotLoading" style="width: 100%">
                <el-table-column prop="mentorName" label="导师" width="120" />
                <el-table-column prop="startTime" label="开始时间" width="170">
                  <template #default="{ row }">{{ formatDateTime(row.startTime) }}</template>
                </el-table-column>
                <el-table-column prop="endTime" label="结束时间" width="170">
                  <template #default="{ row }">{{ formatDateTime(row.endTime) }}</template>
                </el-table-column>
                <el-table-column prop="status" label="状态" width="110" align="center">
                  <template #default="{ row }">
                    <el-tag :type="getSlotStatusType(row.status)">{{ getSlotStatusText(row.status) }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="120" align="center">
                  <template #default="{ row }">
                    <el-button v-if="row.status === 'AVAILABLE'" type="danger" link @click="cancelSlot(row)">取消</el-button>
                    <span v-else style="color: #909399; font-size: 13px">—</span>
                  </template>
                </el-table-column>
              </el-table>
              <el-empty v-if="slots.length === 0 && !slotLoading" description="暂无时段数据" />
            </div>
          </el-tab-pane>

          <el-tab-pane label="预约待办" name="pending">
            <div class="glass-card">
              <div class="card-header">
                <h3>待处理预约</h3>
                <div class="filters">
                  <el-select v-model="pendingMentorId" placeholder="选择导师" style="width: 180px" clearable @change="loadPendingAppointments">
                    <el-option v-for="m in mentors" :key="m.id" :label="m.name" :value="m.id" />
                  </el-select>
                </div>
              </div>
              <el-table :data="pendingAppointments" v-loading="pendingLoading" style="width: 100%">
                <el-table-column prop="mentorName" label="导师" width="120" />
                <el-table-column prop="studentName" label="学生" width="100" />
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
              <el-empty v-if="pendingAppointments.length === 0 && !pendingLoading" description="暂无待处理预约" />
            </div>
          </el-tab-pane>
        </el-tabs>
      </el-col>
    </el-row>

    <el-dialog v-model="mentorDialogVisible" :title="isEditMentor ? '编辑导师' : '添加导师'" width="560px">
      <el-form :model="mentorForm" :rules="mentorRules" ref="mentorFormRef" label-width="100px">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="mentorForm.name" placeholder="请输入导师姓名" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="工号" prop="staffNo">
          <el-input v-model="mentorForm.staffNo" placeholder="请输入工号" maxlength="50" />
        </el-form-item>
        <el-form-item label="研究方向" prop="researchArea">
          <el-input v-model="mentorForm.researchArea" placeholder="请输入研究方向" maxlength="200" />
        </el-form-item>
        <el-form-item label="简介" prop="intro">
          <el-input v-model="mentorForm.intro" type="textarea" :rows="3" placeholder="请输入导师简介" maxlength="500" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="mentorDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveMentor">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="slotDialogVisible" title="开放导师时段" width="560px">
      <div style="margin-bottom: 16px; color: #606266">
        为导师 <strong>{{ currentMentorName }}</strong> 开放答疑时段
      </div>
      <el-form :model="slotForm" :rules="slotRules" ref="slotFormRef" label-width="100px">
        <el-form-item label="时段" prop="timeRange">
          <el-date-picker
            v-model="timeRange"
            type="datetimerange"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            style="width: 100%"
            value-format="YYYY-MM-DD HH:mm:ss"
            :disabled-date="disabledDate"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="slotDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreateSlot">创建时段</el-button>
      </template>
    </el-dialog>

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
import { ElMessage, ElForm, ElMessageBox } from 'element-plus'
import { Plus, Check, Close } from '@element-plus/icons-vue'
import { useUserStore } from '../../store/user'
import mentorshipApi, { type ClubMentor, type MentorSlot, type MentorAppointment } from '../../api/mentorship'

const userStore = useUserStore()

const activeTab = ref('mentors')
const mentorLoading = ref(false)
const slotLoading = ref(false)
const pendingLoading = ref(false)

const mentors = ref<ClubMentor[]>([])
const slots = ref<MentorSlot[]>([])
const pendingAppointments = ref<MentorAppointment[]>([])

const selectedMentorId = ref<number | undefined>(undefined)
const pendingMentorId = ref<number | undefined>(undefined)

const mentorDialogVisible = ref(false)
const isEditMentor = ref(false)
const currentEditMentorId = ref<number | null>(null)
const mentorFormRef = ref<InstanceType<typeof ElForm>>()

const slotDialogVisible = ref(false)
const currentMentorId = ref<number | null>(null)
const currentMentorName = ref('')
const slotFormRef = ref<InstanceType<typeof ElForm>>()
const timeRange = ref<string[]>([])

const rejectDialogVisible = ref(false)
const rejectFormRef = ref<InstanceType<typeof ElForm>>()
const currentRejectAppointmentId = ref<number | null>(null)

const mentorForm = reactive({
  name: '',
  staffNo: '',
  researchArea: '',
  intro: ''
})

const mentorRules = {
  name: [{ required: true, message: '请输入导师姓名', trigger: 'blur' }]
}

const slotForm = reactive({
  timeRange: [] as string[]
})

const slotRules = {
  timeRange: [{ required: true, message: '请选择时段', trigger: 'change' }]
}

const rejectForm = reactive({
  rejectReason: ''
})

const rejectRules = {
  rejectReason: [{ required: true, message: '请输入拒绝原因', trigger: 'blur' }]
}

const availableSlotCount = computed(() => {
  return slots.value.filter((s: MentorSlot) => s.status === 'AVAILABLE').length
})

const totalPendingCount = computed(() => pendingAppointments.value.length)

const totalConfirmedCount = computed(() => mentors.value.length)

const loadMentors = async () => {
  mentorLoading.value = true
  try {
    const clubId = userStore.role === 'CLUB_LEADER' ? userStore.userInfo?.clubId : undefined
    const data = await mentorshipApi.getMentors(clubId)
    mentors.value = data as unknown as ClubMentor[]
  } catch (err) {
    console.error('Failed to load mentors:', err)
  } finally {
    mentorLoading.value = false
  }
}

const loadSlots = async () => {
  if (!selectedMentorId.value) {
    slots.value = []
    return
  }
  slotLoading.value = true
  try {
    const data = await mentorshipApi.getSlotsByMentor(selectedMentorId.value)
    slots.value = data as unknown as MentorSlot[]
  } catch (err) {
    console.error('Failed to load slots:', err)
  } finally {
    slotLoading.value = false
  }
}

const loadPendingAppointments = async () => {
  if (!pendingMentorId.value) {
    pendingAppointments.value = []
    return
  }
  pendingLoading.value = true
  try {
    const data = await mentorshipApi.getPendingByMentor(pendingMentorId.value)
    pendingAppointments.value = data as unknown as MentorAppointment[]
  } catch (err) {
    console.error('Failed to load pending appointments:', err)
  } finally {
    pendingLoading.value = false
  }
}

const openMentorDialog = () => {
  isEditMentor.value = false
  currentEditMentorId.value = null
  mentorForm.name = ''
  mentorForm.staffNo = ''
  mentorForm.researchArea = ''
  mentorForm.intro = ''
  mentorDialogVisible.value = true
}

const editMentor = (row: ClubMentor) => {
  isEditMentor.value = true
  currentEditMentorId.value = row.id
  mentorForm.name = row.name
  mentorForm.staffNo = row.staffNo || ''
  mentorForm.researchArea = row.researchArea || ''
  mentorForm.intro = row.intro || ''
  mentorDialogVisible.value = true
}

const handleSaveMentor = async () => {
  if (!mentorFormRef.value) return
  try {
    await mentorFormRef.value.validate()
    const clubId = userStore.userInfo?.clubId || 1
    if (isEditMentor.value && currentEditMentorId.value) {
      await mentorshipApi.updateMentor({
        id: currentEditMentorId.value,
        clubId,
        name: mentorForm.name,
        staffNo: mentorForm.staffNo,
        researchArea: mentorForm.researchArea,
        intro: mentorForm.intro,
        userId: null,
        clubName: '',
        linkedUsername: null,
        createTime: ''
      })
      ElMessage.success('导师更新成功')
    } else {
      await mentorshipApi.createMentor({
        clubId,
        name: mentorForm.name,
        staffNo: mentorForm.staffNo,
        researchArea: mentorForm.researchArea,
        intro: mentorForm.intro
      })
      ElMessage.success('导师添加成功')
    }
    mentorDialogVisible.value = false
    loadMentors()
  } catch (err) {
    console.error('Failed to save mentor:', err)
  }
}

const deleteMentor = async (row: ClubMentor) => {
  try {
    await ElMessageBox.confirm(`确定要删除导师"${row.name}"吗？`, '确认删除', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    await mentorshipApi.deleteMentor(row.id)
    ElMessage.success('删除成功')
    loadMentors()
  } catch (err) {
    if (err !== 'cancel') {
      console.error('Failed to delete mentor:', err)
    }
  }
}

const openSlotDialog = (row: ClubMentor) => {
  currentMentorId.value = row.id
  currentMentorName.value = row.name
  timeRange.value = []
  slotDialogVisible.value = true
}

const handleCreateSlot = async () => {
  if (!slotFormRef.value || !currentMentorId.value) return
  try {
    await slotFormRef.value.validate()
    if (!timeRange.value || timeRange.value.length !== 2) {
      ElMessage.warning('请选择完整时段')
      return
    }
    await mentorshipApi.createSlot({
      mentorId: currentMentorId.value,
      startTime: timeRange.value[0],
      endTime: timeRange.value[1]
    })
    ElMessage.success('时段创建成功')
    slotDialogVisible.value = false
    if (selectedMentorId.value === currentMentorId.value) {
      loadSlots()
    }
  } catch (err) {
    console.error('Failed to create slot:', err)
  }
}

const cancelSlot = async (row: MentorSlot) => {
  try {
    await ElMessageBox.confirm('确定要取消此时段吗？', '确认取消', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    await mentorshipApi.cancelSlot(row.id)
    ElMessage.success('时段已取消')
    loadSlots()
  } catch (err) {
    if (err !== 'cancel') {
      console.error('Failed to cancel slot:', err)
    }
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
    loadPendingAppointments()
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
    loadPendingAppointments()
  } catch (err) {
    console.error('Failed to reject appointment:', err)
  }
}

const disabledDate = (time: Date) => {
  return time.getTime() < Date.now() - 8.64e7
}

const getSlotStatusType = (status: string) => {
  const map: Record<string, 'success' | 'warning' | 'info' | 'danger'> = {
    AVAILABLE: 'success',
    BOOKED: 'warning',
    CANCELLED: 'info'
  }
  return map[status] || 'info'
}

const getSlotStatusText = (status: string) => {
  const map: Record<string, string> = {
    AVAILABLE: '可预约',
    BOOKED: '已预约',
    CANCELLED: '已取消'
  }
  return map[status] || status
}

const formatDateTime = (dateStr: string) => {
  return dateStr ? new Date(dateStr).toLocaleString('zh-CN') : ''
}

onMounted(() => {
  loadMentors()
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
