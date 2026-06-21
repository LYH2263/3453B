<template>
  <div class="volunteer-page">
    <el-row :gutter="20">
      <el-col :span="24">
        <div class="glass-card page-header">
          <div class="header-left">
            <h2>志愿服务 - 待审核</h2>
            <p class="subtitle">审核学生提交的志愿服务记录</p>
          </div>
          <div class="header-right">
            <el-button type="primary" @click="loadData">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="stats-row">
      <el-col :span="8">
        <div class="stat-card glass-card hover-lift">
          <div class="stat-label">待审核记录</div>
          <div class="stat-value text-warning">{{ pendingCount }}<span class="stat-unit">条</span></div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="stat-card glass-card hover-lift">
          <div class="stat-label">今日待审核</div>
          <div class="stat-value text-primary">{{ todayCount }}<span class="stat-unit">条</span></div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="stat-card glass-card hover-lift">
          <div class="stat-label">累计已审核</div>
          <div class="stat-value text-success">{{ totalReviewed }}<span class="stat-unit">条</span></div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="mt-20">
      <el-col :span="24">
        <div class="glass-card">
          <div class="card-header">
            <h3>待审核列表</h3>
            <div class="filters">
              <el-select v-model="filterClubId" placeholder="社团筛选" style="width: 160px" clearable v-if="isUnionAdmin">
                <el-option v-for="club in clubs" :key="club.id" :label="club.name" :value="club.id" />
              </el-select>
            </div>
          </div>
          <el-table :data="filteredRecords" v-loading="loading" style="width: 100%">
            <el-table-column prop="studentName" label="学生姓名" width="110" />
            <el-table-column prop="studentNo" label="学号" width="130" />
            <el-table-column prop="activityName" label="活动名称" min-width="180" />
            <el-table-column prop="serviceDate" label="服务日期" width="130">
              <template #default="{ row }">
                {{ formatDate(row.serviceDate) }}
              </template>
            </el-table-column>
            <el-table-column prop="hours" label="时长(小时)" width="100" align="center" />
            <el-table-column prop="clubName" label="所属社团" width="130" />
            <el-table-column prop="createTime" label="提交时间" width="170">
              <template #default="{ row }">
                {{ formatDateTime(row.createTime) }}
              </template>
            </el-table-column>
            <el-table-column label="证明" width="80" align="center">
              <template #default="{ row }">
                <el-link v-if="row.proofUrl" :href="row.proofUrl" target="_blank" type="primary">
                  <el-icon><View /></el-icon>
                </el-link>
                <span v-else style="color: #c0c4cc">-</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200" align="center" fixed="right">
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
          <el-empty v-if="filteredRecords.length === 0 && !loading" description="暂无待审核记录" />
        </div>
      </el-col>
    </el-row>

    <el-dialog v-model="auditDialogVisible" :title="auditTitle" width="480px">
      <div v-if="currentRecord" class="audit-preview">
        <el-descriptions :column="1" border size="small">
          <el-descriptions-item label="学生姓名">{{ currentRecord.studentName }}</el-descriptions-item>
          <el-descriptions-item label="学号">{{ currentRecord.studentNo }}</el-descriptions-item>
          <el-descriptions-item label="活动名称">{{ currentRecord.activityName }}</el-descriptions-item>
          <el-descriptions-item label="服务日期">{{ formatDate(currentRecord.serviceDate) }}</el-descriptions-item>
          <el-descriptions-item label="服务时长">{{ currentRecord.hours }} 小时</el-descriptions-item>
          <el-descriptions-item label="所属社团">{{ currentRecord.clubName }}</el-descriptions-item>
          <el-descriptions-item label="证明链接" v-if="currentRecord.proofUrl">
            <el-link :href="currentRecord.proofUrl" target="_blank" type="primary">查看证明</el-link>
          </el-descriptions-item>
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElForm } from 'element-plus'
import { Refresh, View, Check, Close } from '@element-plus/icons-vue'
import volunteerApi, { type VolunteerRecord, type VolunteerAuditRequest } from '../api/volunteer'
import { useUserStore } from '../store/user'
import request from '../utils/request'

const userStore = useUserStore()
const loading = ref(false)
const pendingRecords = ref<VolunteerRecord[]>([])
const clubs = ref<Array<{ id: number; name: string }>>([])
const filterClubId = ref<number | undefined>(undefined)

const auditDialogVisible = ref(false)
const currentRecord = ref<VolunteerRecord | null>(null)
const auditStatus = ref<'APPROVED' | 'REJECTED'>('APPROVED')
const auditFormRef = ref<InstanceType<typeof ElForm>>()

const auditForm = reactive<VolunteerAuditRequest>({
  status: 'APPROVED',
  rejectReason: ''
})

const auditRules = {
  rejectReason: [{ required: true, message: '请输入驳回原因', trigger: 'blur' }]
}

const isUnionAdmin = computed(() => userStore.isUnionAdmin)

const auditTitle = computed(() => {
  return auditStatus.value === 'APPROVED' ? '审核通过' : '审核驳回'
})

const filteredRecords = computed(() => {
  if (!filterClubId.value) return pendingRecords.value
  return pendingRecords.value.filter(r => r.clubId === filterClubId.value)
})

const pendingCount = computed(() => pendingRecords.value.length)

const todayCount = computed(() => {
  const today = new Date().toDateString()
  return pendingRecords.value.filter(r =>
    new Date(r.createTime).toDateString() === today
  ).length
})

const totalReviewed = ref(0)

const loadData = async () => {
  loading.value = true
  try {
    const data = await volunteerApi.getPendingAudits()
    pendingRecords.value = data as unknown as VolunteerRecord[]
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

const openAuditDialog = (row: VolunteerRecord, status: 'APPROVED' | 'REJECTED') => {
  currentRecord.value = row
  auditStatus.value = status
  auditForm.status = status
  auditForm.rejectReason = ''
  auditDialogVisible.value = true
}

const handleAudit = async () => {
  if (!currentRecord.value) return

  if (auditStatus.value === 'REJECTED') {
    if (!auditFormRef.value) return
    try {
      await auditFormRef.value.validate()
    } catch {
      return
    }
  }

  try {
    await volunteerApi.auditRecord(currentRecord.value.id, {
      status: auditStatus.value,
      rejectReason: auditStatus.value === 'REJECTED' ? auditForm.rejectReason : undefined
    })
    ElMessage.success(`审核${auditStatus.value === 'APPROVED' ? '通过' : '驳回'}成功`)
    auditDialogVisible.value = false
    loadData()
  } catch (err) {
    console.error('Failed to audit:', err)
  }
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
.audit-preview {
  margin-bottom: 10px;
}
:deep(.el-table) {
  --el-table-bg-color: transparent;
  --el-table-tr-bg-color: transparent;
  --el-table-header-bg-color: rgba(0,0,0,0.02);
}
</style>
