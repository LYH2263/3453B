<template>
  <div class="leave-requests-page">
    <el-tabs v-model="activeTab" class="mb-20">
      <el-tab-pane label="我的请假" name="my">
        <div class="toolbar glass-card mb-20">
          <el-button type="primary" @click="showSubmitDialog = true">申请请假</el-button>
        </div>

        <el-table :data="myRequests" class="glass-card" stripe>
          <el-table-column prop="activityTitle" label="活动名称" min-width="150" />
          <el-table-column prop="reason" label="请假原因" min-width="200" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="rejectReason" label="驳回原因" min-width="180">
            <template #default="{ row }">
              {{ row.rejectReason || '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="approverName" label="审批人" width="120">
            <template #default="{ row }">
              {{ row.approverName || '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="申请时间" width="170">
            <template #default="{ row }">
              {{ formatTime(row.createTime) }}
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane v-if="isClubLeader" label="审批管理" name="approval">
        <el-row :gutter="20" class="mb-20">
          <el-col :span="6">
            <el-select v-model="filterStatus" placeholder="筛选状态" @change="fetchApprovalRequests" clearable>
              <el-option label="待审批" value="PENDING" />
              <el-option label="全部" value="ALL" />
            </el-select>
          </el-col>
        </el-row>

        <el-table :data="approvalRequests" class="glass-card" stripe>
          <el-table-column prop="userName" label="申请人" width="120" />
          <el-table-column prop="activityTitle" label="活动名称" min-width="150" />
          <el-table-column prop="reason" label="请假原因" min-width="200" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="申请时间" width="170">
            <template #default="{ row }">
              {{ formatTime(row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <template v-if="row.status === 'PENDING'">
                <el-button type="success" size="small" @click="handleApprove(row)">批准</el-button>
                <el-button type="danger" size="small" @click="handleReject(row)">拒绝</el-button>
              </template>
              <span v-else class="text-muted">已处理</span>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="showSubmitDialog" title="申请请假" width="500px">
      <el-form :model="submitForm" ref="submitFormRef" label-width="80px">
        <el-form-item label="选择活动" required>
          <el-select v-model="submitForm.activityId" placeholder="请选择强制活动" filterable style="width: 100%">
            <el-option
              v-for="act in mandatoryActivities"
              :key="act.id"
              :label="act.title"
              :value="act.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="请假原因" required>
          <el-input v-model="submitForm.reason" type="textarea" :rows="4" placeholder="请详细说明请假原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showSubmitDialog = false">取消</el-button>
        <el-button type="primary" @click="submitLeave">提交申请</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showRejectDialog" title="拒绝请假" width="400px">
      <el-form :model="rejectForm" label-width="80px">
        <el-form-item label="拒绝原因" required>
          <el-input v-model="rejectForm.rejectReason" type="textarea" :rows="3" placeholder="拒绝请假须填写原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showRejectDialog = false">取消</el-button>
        <el-button type="danger" @click="confirmReject">确认拒绝</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../store/user'
import leaveRequestApi, { type LeaveRequest } from '../api/leaveRequests'
import request from '../utils/request'

const userStore = useUserStore()
const activeTab = ref('my')
const myRequests = ref<LeaveRequest[]>([])
const approvalRequests = ref<LeaveRequest[]>([])
const filterStatus = ref('PENDING')
const showSubmitDialog = ref(false)
const showRejectDialog = ref(false)
const currentRejectId = ref(0)
const rejectForm = ref({ rejectReason: '' })
const submitForm = ref({ activityId: null as number | null, reason: '' })

interface ActivityItem {
  id: number
  title: string
  isMandatory: boolean
  status: string
}

const allActivities = ref<ActivityItem[]>([])
const mandatoryActivities = computed(() =>
  allActivities.value.filter(a => a.isMandatory && a.status === 'APPROVED')
)

const isClubLeader = computed(() =>
  userStore.userInfo && ['ADMIN', 'UNION_ADMIN', 'CLUB_LEADER'].includes(userStore.userInfo.role)
)

const userClubId = computed(() => userStore.userInfo?.clubId)

const statusType = (s: string) => {
  if (s === 'PENDING') return 'warning'
  if (s === 'APPROVED') return 'success'
  if (s === 'REJECTED') return 'danger'
  return 'info'
}

const statusText = (s: string) => {
  const map: Record<string, string> = { PENDING: '待审批', APPROVED: '已批准', REJECTED: '已拒绝' }
  return map[s] || s
}

const formatTime = (t: string) => {
  if (!t) return '-'
  return t.replace('T', ' ').substring(0, 19)
}

const fetchMyRequests = async () => {
  try {
    const res = await leaveRequestApi.getMyLeaveRequests() as any
    myRequests.value = Array.isArray(res) ? res : []
  } catch (err) {
    console.error('Failed to fetch my leave requests:', err)
  }
}

const fetchApprovalRequests = async () => {
  if (!isClubLeader.value || !userClubId.value) return
  try {
    if (filterStatus.value === 'ALL') {
      const res = await leaveRequestApi.getAllRequests(userClubId.value) as any
      approvalRequests.value = Array.isArray(res) ? res : []
    } else {
      const res = await leaveRequestApi.getPendingRequests(userClubId.value) as any
      approvalRequests.value = Array.isArray(res) ? res : []
    }
  } catch (err) {
    console.error('Failed to fetch approval requests:', err)
  }
}

const fetchActivities = async () => {
  try {
    const res: any = await request.get('/activities')
    allActivities.value = Array.isArray(res) ? res : []
  } catch (err) {
    console.error('Failed to fetch activities:', err)
  }
}

const submitLeave = async () => {
  if (!submitForm.value.activityId) {
    ElMessage.warning('请选择活动')
    return
  }
  if (!submitForm.value.reason.trim()) {
    ElMessage.warning('请填写请假原因')
    return
  }
  try {
    await leaveRequestApi.submitLeave({
      activityId: submitForm.value.activityId,
      reason: submitForm.value.reason
    })
    ElMessage.success('请假申请已提交')
    showSubmitDialog.value = false
    submitForm.value = { activityId: null, reason: '' }
    fetchMyRequests()
  } catch (err) {
    console.error('Submit leave failed:', err)
  }
}

const handleApprove = async (row: LeaveRequest) => {
  try {
    await leaveRequestApi.approveLeave(row.id)
    ElMessage.success('已批准请假')
    fetchApprovalRequests()
  } catch (err) {
    console.error('Approve failed:', err)
  }
}

const handleReject = (row: LeaveRequest) => {
  currentRejectId.value = row.id
  rejectForm.value = { rejectReason: '' }
  showRejectDialog.value = true
}

const confirmReject = async () => {
  if (!rejectForm.value.rejectReason.trim()) {
    ElMessage.warning('拒绝请假须填写原因')
    return
  }
  try {
    await leaveRequestApi.rejectLeave(currentRejectId.value, rejectForm.value.rejectReason)
    ElMessage.success('已拒绝请假')
    showRejectDialog.value = false
    fetchApprovalRequests()
  } catch (err) {
    console.error('Reject failed:', err)
  }
}

onMounted(() => {
  fetchMyRequests()
  fetchActivities()
  if (isClubLeader.value && userClubId.value) {
    fetchApprovalRequests()
  }
})
</script>

<style scoped>
.leave-requests-page {
  padding: 0;
}
.toolbar {
  padding: 15px 20px;
}
.mb-20 {
  margin-bottom: 20px;
}
.text-muted {
  color: #999;
  font-size: 13px;
}
</style>
