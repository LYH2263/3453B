<template>
  <div class="volunteer-page">
    <el-row :gutter="20">
      <el-col :span="24">
        <div class="glass-card page-header">
          <div class="header-left">
            <h2>志愿服务 - 我的申请</h2>
            <p class="subtitle">记录您的志愿服务历程</p>
          </div>
          <div class="header-right">
            <el-button type="primary" @click="openSubmitDialog">
              <el-icon><Plus /></el-icon>
              提交服务记录
            </el-button>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <div class="stat-card glass-card hover-lift">
          <div class="stat-label">累计服务时长</div>
          <div class="stat-value text-primary">{{ myStats?.totalHours || 0 }}<span class="stat-unit">小时</span></div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card glass-card hover-lift">
          <div class="stat-label">已通过记录</div>
          <div class="stat-value text-success">{{ myStats?.approvedCount || 0 }}<span class="stat-unit">次</span></div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card glass-card hover-lift">
          <div class="stat-label">待审核</div>
          <div class="stat-value text-warning">{{ myStats?.pendingCount || 0 }}<span class="stat-unit">次</span></div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card glass-card hover-lift">
          <div class="stat-label">已驳回</div>
          <div class="stat-value text-danger">{{ myStats?.rejectedCount || 0 }}<span class="stat-unit">次</span></div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="mt-20">
      <el-col :span="24">
        <div class="glass-card">
          <div class="card-header">
            <h3>我的服务记录</h3>
            <div class="filters">
              <el-select v-model="filterStatus" placeholder="状态筛选" style="width: 140px" clearable>
                <el-option label="待审核" value="PENDING" />
                <el-option label="已通过" value="APPROVED" />
                <el-option label="已驳回" value="REJECTED" />
              </el-select>
            </div>
          </div>
          <el-table :data="filteredRecords" v-loading="loading" style="width: 100%">
            <el-table-column prop="activityName" label="活动名称" min-width="180" />
            <el-table-column prop="serviceDate" label="服务日期" width="130">
              <template #default="{ row }">
                {{ formatDate(row.serviceDate) }}
              </template>
            </el-table-column>
            <el-table-column prop="hours" label="时长(小时)" width="110" align="center" />
            <el-table-column prop="clubName" label="所属社团" width="140" />
            <el-table-column prop="status" label="状态" width="110" align="center">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)">
                  {{ getStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120" align="center">
              <template #default="{ row }">
                <el-button type="primary" link @click="viewDetail(row)">查看</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="filteredRecords.length === 0 && !loading" description="暂无服务记录" />
        </div>
      </el-col>
    </el-row>

    <el-dialog v-model="submitDialogVisible" title="提交志愿服务记录" width="520px">
      <el-form :model="submitForm" :rules="submitRules" ref="submitFormRef" label-width="90px">
        <el-form-item label="活动名称" prop="activityName">
          <el-input v-model="submitForm.activityName" placeholder="请输入活动名称" />
        </el-form-item>
        <el-form-item label="服务日期" prop="serviceDate">
          <el-date-picker
            v-model="submitForm.serviceDate"
            type="date"
            placeholder="选择服务日期"
            style="width: 100%"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="服务时长" prop="hours">
          <el-input-number
            v-model="submitForm.hours"
            :min="0.5"
            :max="24"
            :step="0.5"
            :precision="1"
            style="width: 100%"
            placeholder="请输入服务时长(小时)"
          />
        </el-form-item>
        <el-form-item label="所属社团">
          <el-select v-model="submitForm.clubId" placeholder="选择所属社团(可选)" style="width: 100%" clearable>
            <el-option v-for="club in clubs" :key="club.id" :label="club.name" :value="club.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="证明链接">
          <el-input v-model="submitForm.proofUrl" placeholder="上传证明材料链接(可选)" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="submitDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">提交</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailDialogVisible" title="记录详情" width="480px">
      <el-descriptions :column="1" border v-if="currentRecord">
        <el-descriptions-item label="活动名称">{{ currentRecord.activityName }}</el-descriptions-item>
        <el-descriptions-item label="服务日期">{{ formatDate(currentRecord.serviceDate) }}</el-descriptions-item>
        <el-descriptions-item label="服务时长">{{ currentRecord.hours }} 小时</el-descriptions-item>
        <el-descriptions-item label="所属社团">{{ currentRecord.clubName }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentRecord.status)">
            {{ getStatusText(currentRecord.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="提交时间">{{ formatDateTime(currentRecord.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="审核人" v-if="currentRecord.auditorName">
          {{ currentRecord.auditorName }}
        </el-descriptions-item>
        <el-descriptions-item label="驳回原因" v-if="currentRecord.rejectReason">
          <span style="color: #f56c6c">{{ currentRecord.rejectReason }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="证明链接" v-if="currentRecord.proofUrl">
          <el-link :href="currentRecord.proofUrl" target="_blank" type="primary">
            查看证明 <el-icon><Link /></el-icon>
          </el-link>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElForm } from 'element-plus'
import { Plus, Link } from '@element-plus/icons-vue'
import volunteerApi, {
  type VolunteerRecord,
  type VolunteerStat,
  type VolunteerSubmitRequest
} from '../../api/volunteer'
import request from '../../utils/request'

const loading = ref(false)
const myRecords = ref<VolunteerRecord[]>([])
const myStats = ref<VolunteerStat | null>(null)
const clubs = ref<Array<{ id: number; name: string }>>([])
const filterStatus = ref('')

const submitDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const currentRecord = ref<VolunteerRecord | null>(null)
const submitFormRef = ref<InstanceType<typeof ElForm>>()

const submitForm = reactive<VolunteerSubmitRequest>({
  activityName: '',
  serviceDate: '',
  hours: 1,
  proofUrl: '',
  clubId: undefined
})

const submitRules = {
  activityName: [{ required: true, message: '请输入活动名称', trigger: 'blur' }],
  serviceDate: [{ required: true, message: '请选择服务日期', trigger: 'change' }],
  hours: [{ required: true, message: '请输入服务时长', trigger: 'blur' }]
}

const filteredRecords = computed(() => {
  if (!filterStatus.value) return myRecords.value
  return myRecords.value.filter(r => r.status === filterStatus.value)
})

const loadData = async () => {
  loading.value = true
  try {
    const [records, stats] = await Promise.all([
      volunteerApi.getMyRecords(),
      volunteerApi.getMyStats()
    ])
    myRecords.value = records as unknown as VolunteerRecord[]
    myStats.value = stats as unknown as VolunteerStat
  } catch (err) {
    console.error('Failed to load data:', err)
  } finally {
    loading.value = false
  }
}

const loadClubs = async () => {
  try {
    const res = await request.get('/clubs/dashboard')
    if (res && (res as any).clubs) {
      clubs.value = (res as any).clubs
    }
  } catch (err) {
    console.error('Failed to load clubs:', err)
  }
}

const openSubmitDialog = () => {
  submitForm.activityName = ''
  submitForm.serviceDate = ''
  submitForm.hours = 1
  submitForm.proofUrl = ''
  submitForm.clubId = undefined
  submitDialogVisible.value = true
}

const handleSubmit = async () => {
  if (!submitFormRef.value) return
  try {
    await submitFormRef.value.validate()
    await volunteerApi.submitRecord(submitForm)
    ElMessage.success('提交成功，等待审核')
    submitDialogVisible.value = false
    loadData()
  } catch (err) {
    console.error('Failed to submit:', err)
  }
}

const viewDetail = (row: VolunteerRecord) => {
  currentRecord.value = row
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
    PENDING: '待审核',
    REJECTED: '已驳回'
  }
  return map[status] || status
}

const formatDate = (date: string) => {
  return date ? new Date(date).toLocaleDateString('zh-CN') : ''
}

const formatDateTime = (date: string) => {
  return date ? new Date(date).toLocaleString('zh-CN') : ''
}

onMounted(() => {
  loadData()
  loadClubs()
})
</script>

<style scoped>
.volunteer-page {
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
