<template>
  <div class="inspection-page">
    <el-row :gutter="20">
      <el-col :span="24">
        <div class="glass-card page-header">
          <div class="header-left">
            <h2>我的巡检任务</h2>
            <p class="subtitle">查看待办巡检、提交巡检记录</p>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <div class="stat-card glass-card hover-lift">
          <div class="stat-label">今日待检</div>
          <div class="stat-value text-warning">{{ todayTodoCount }}<span class="stat-unit">项</span></div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card glass-card hover-lift">
          <div class="stat-label">逾期未检</div>
          <div class="stat-value text-danger">{{ overdueCount }}<span class="stat-unit">项</span></div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card glass-card hover-lift">
          <div class="stat-label">待检总数</div>
          <div class="stat-value text-primary">{{ todoPlans.length }}<span class="stat-unit">项</span></div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card glass-card hover-lift">
          <div class="stat-label">累计巡检</div>
          <div class="stat-value text-success">{{ myRecords.length }}<span class="stat-unit">次</span></div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="mt-20">
      <el-col :span="24">
        <div class="glass-card">
          <el-tabs v-model="activeTab">
            <el-tab-pane label="待办巡检" name="todo">
              <div class="card-header">
                <div class="filters">
                  <el-radio-group v-model="todoFilter">
                    <el-radio-button label="all">全部</el-radio-button>
                    <el-radio-button label="overdue">仅逾期</el-radio-button>
                    <el-radio-button label="today">仅今日</el-radio-button>
                  </el-radio-group>
                </div>
              </div>
              <el-table :data="filteredTodoPlans" v-loading="loading" style="width: 100%" :row-class-name="todoRowClass">
                <el-table-column prop="deviceName" label="设备名称" min-width="140" />
                <el-table-column prop="deviceNo" label="设备编号" min-width="120" />
                <el-table-column prop="deviceLocation" label="位置" min-width="160" />
                <el-table-column prop="cycleDays" label="周期(天)" width="90" align="center" />
                <el-table-column prop="nextInspectDate" label="应检日期" width="130" align="center">
                  <template #default="{ row }">
                    <span :class="{ 'text-danger': row.isOverdue }">{{ row.nextInspectDate || '-' }}</span>
                  </template>
                </el-table-column>
                <el-table-column prop="lastInspectTime" label="上次巡检" width="160" align="center">
                  <template #default="{ row }">
                    {{ row.lastInspectTime ? formatDateTime(row.lastInspectTime) : '从未巡检' }}
                  </template>
                </el-table-column>
                <el-table-column label="状态" width="90" align="center">
                  <template #default="{ row }">
                    <el-tag v-if="row.isOverdue" type="danger" size="small">逾期</el-tag>
                    <el-tag v-else type="warning" size="small">待检</el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="120" align="center">
                  <template #default="{ row }">
                    <el-button type="primary" link @click="openSubmitDialog(row)">提交巡检</el-button>
                  </template>
                </el-table-column>
              </el-table>
              <el-empty v-if="filteredTodoPlans.length === 0 && !loading" description="暂无待办巡检任务" />
            </el-tab-pane>

            <el-tab-pane label="我的巡检记录" name="records">
              <div class="card-header">
                <div class="filters">
                  <el-select v-model="recordResultFilter" placeholder="结果筛选" style="width: 140px" clearable>
                    <el-option label="正常" value="NORMAL" />
                    <el-option label="异常" value="ABNORMAL" />
                  </el-select>
                </div>
              </div>
              <el-table :data="filteredRecords" v-loading="recordsLoading" style="width: 100%">
                <el-table-column prop="deviceName" label="设备名称" min-width="140" />
                <el-table-column prop="deviceLocation" label="位置" min-width="160" />
                <el-table-column prop="result" label="结果" width="90" align="center">
                  <template #default="{ row }">
                    <el-tag :type="row.result === 'NORMAL' ? 'success' : 'danger'">
                      {{ row.result === 'NORMAL' ? '正常' : '异常' }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="remark" label="备注" min-width="200" show-overflow-tooltip />
                <el-table-column prop="inspectTime" label="巡检时间" width="170" align="center">
                  <template #default="{ row }">
                    {{ formatDateTime(row.inspectTime) }}
                  </template>
                </el-table-column>
              </el-table>
              <el-empty v-if="filteredRecords.length === 0 && !recordsLoading" description="暂无巡检记录" />
            </el-tab-pane>
          </el-tabs>
        </div>
      </el-col>
    </el-row>

    <el-dialog v-model="submitDialogVisible" title="提交巡检记录" width="520px" @close="onSubmitDialogClose">
      <div class="device-info" v-if="currentPlan">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="设备名称">{{ currentPlan.deviceName }}</el-descriptions-item>
          <el-descriptions-item label="设备编号">{{ currentPlan.deviceNo }}</el-descriptions-item>
          <el-descriptions-item label="存放位置" :span="2">{{ currentPlan.deviceLocation }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <el-form :model="submitForm" :rules="submitRules" ref="submitFormRef" label-width="100px" style="margin-top: 20px">
        <el-form-item label="巡检结果" prop="result">
          <el-radio-group v-model="submitForm.result">
            <el-radio value="NORMAL">正常</el-radio>
            <el-radio value="ABNORMAL">异常</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="巡检时间" prop="inspectTime">
          <el-date-picker
            v-model="submitForm.inspectTime"
            type="datetime"
            placeholder="选择巡检时间"
            style="width: 100%"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="submitForm.remark"
            type="textarea"
            :rows="4"
            placeholder="请输入巡检备注（异常必填"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="submitDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitRecord">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElForm } from 'element-plus'
import inspectionApi, {
  type InspectionPlan,
  type InspectionRecord
} from '../../api/inspections'

const loading = ref(false)
const recordsLoading = ref(false)
const activeTab = ref('todo')
const todoFilter = ref('all')
const recordResultFilter = ref('')

const todoPlans = ref<InspectionPlan[]>([])
const myRecords = ref<InspectionRecord[]>([])

const todayTodoCount = computed(() => {
  const today = new Date().toISOString().slice(0, 10)
  return todoPlans.value.filter(
    (p: InspectionPlan) => p.nextInspectDate === today
  ).length
})

const overdueCount = computed(() => {
  return todoPlans.value.filter(
    (p: InspectionPlan) => Boolean(p.isOverdue)
  ).length
})

const filteredTodoPlans = computed(() => {
  if (todoFilter.value === 'overdue') {
    return todoPlans.value.filter((p: InspectionPlan) => Boolean(p.isOverdue))
  } else if (todoFilter.value === 'today') {
    const today = new Date().toISOString().slice(0, 10)
    return todoPlans.value.filter((p: InspectionPlan) => p.nextInspectDate === today)
  }
  return todoPlans.value
})

const filteredRecords = computed(() => {
  if (!recordResultFilter.value) return myRecords.value
  return myRecords.value.filter((r: InspectionRecord) => r.result === recordResultFilter.value)
})

const submitDialogVisible = ref(false)
const currentPlan = ref<InspectionPlan | null>(null)
const submitFormRef = ref<InstanceType<typeof ElForm>>()
const submitForm = reactive({
  planId: 0,
  result: 'NORMAL' as 'NORMAL' | 'ABNORMAL',
  inspectTime: '',
  remark: ''
})
const submitRules = {
  result: [{ required: true, message: '请选择巡检结果', trigger: 'change' }],
  inspectTime: [{ required: true, message: '请选择巡检时间', trigger: 'change' }]
}

const todoRowClass = ({ row }: { row: InspectionPlan }) => {
  if (row.isOverdue) return 'row-overdue'
  return ''
}

const loadTodoPlans = async () => {
  loading.value = true
  try {
    const data = await inspectionApi.getMyTodoPlans()
    todoPlans.value = data as unknown as InspectionPlan[]
  } catch (err) {
    console.error('Failed to load todo plans:', err)
  } finally {
    loading.value = false
  }
}

const loadMyRecords = async () => {
  recordsLoading.value = true
  try {
    const data = await inspectionApi.getMyRecords()
    myRecords.value = data as unknown as InspectionRecord[]
  } catch (err) {
    console.error('Failed to load my records:', err)
  } finally {
    recordsLoading.value = false
  }
}

const openSubmitDialog = (row: InspectionPlan) => {
  currentPlan.value = row
  submitForm.planId = row.id
  submitForm.result = 'NORMAL'
  const now = new Date()
  submitForm.inspectTime = now.toISOString().slice(0, 19).replace('T', ' ')
  submitForm.remark = ''
  submitDialogVisible.value = true
}

const onSubmitDialogClose = () => {
  submitFormRef.value?.resetFields()
}

const handleSubmitRecord = async () => {
  if (!submitFormRef.value) return
  try {
    await submitFormRef.value.validate()
    await inspectionApi.submitRecord(submitForm)
    ElMessage.success('巡检记录提交成功')
    submitDialogVisible.value = false
    loadTodoPlans()
    loadMyRecords()
  } catch (err) {
    console.error('Failed to submit record:', err)
  }
}

const formatDateTime = (dateStr: string) => {
  return dateStr ? new Date(dateStr).toLocaleString('zh-CN') : ''
}

onMounted(() => {
  loadTodoPlans()
  loadMyRecords()
})
</script>

<style scoped>
.inspection-page { padding: 0; }
.page-header {
  padding: 24px 30px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.page-header h2 { margin: 0; font-size: 22px; color: #303133; }
.subtitle { margin: 6px 0 0 0; color: #909399; font-size: 14px; }
.stats-row { margin-top: 20px; }
.stat-card { padding: 24px; text-align: center; }
.stat-label { font-size: 14px; color: #606266; margin-bottom: 12px; }
.stat-value { font-size: 32px; font-weight: bold; }
.stat-unit { font-size: 14px; font-weight: normal; margin-left: 4px; color: #909399; }
.text-primary { color: #409eff; }
.text-success { color: #67c23a; }
.text-warning { color: #e6a23c; }
.text-danger { color: #f56c6c; }
.mt-20 { margin-top: 20px; }
.card-header {
  padding: 0 0 16px 0;
  display: flex;
  justify-content: flex-end;
  align-items: center;
}
.filters { display: flex; gap: 12px; }
.device-info { margin-bottom: 8px; }
:deep(.el-table) {
  --el-table-bg-color: transparent;
  --el-table-tr-bg-color: transparent;
  --el-table-header-bg-color: rgba(0,0,0,0.02);
}
:deep(.row-overdue) {
  --el-table-tr-bg-color: rgba(245, 108, 108, 0.08);
}
:deep(.row-overdue td) {
  background-color: rgba(245, 108, 108, 0.08) !important;
}
:deep(.el-tabs__content) { padding-top: 16px; }
</style>
