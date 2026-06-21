<template>
  <div class="inspection-page">
    <el-row :gutter="20">
      <el-col :span="24">
        <div class="glass-card page-header">
          <div class="header-left">
            <h2>设备巡检管理</h2>
            <p class="subtitle">管理社团设备、创建巡检计划、查看巡检记录</p>
          </div>
          <div class="header-right">
            <el-button type="primary" @click="openDeviceDialog">
              <el-icon><Plus /></el-icon>
              新增设备
            </el-button>
            <el-button type="success" @click="openPlanDialog" style="margin-left: 10px">
              <el-icon><Calendar /></el-icon>
              创建计划
            </el-button>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="stats-row">
      <el-col :span="4">
        <div class="stat-card glass-card hover-lift">
          <div class="stat-label">设备总数</div>
          <div class="stat-value text-primary">{{ stats.deviceCount }}<span class="stat-unit">台</span></div>
        </div>
      </el-col>
      <el-col :span="4">
        <div class="stat-card glass-card hover-lift">
          <div class="stat-label">活跃计划</div>
          <div class="stat-value text-info">{{ stats.activePlanCount }}<span class="stat-unit">个</span></div>
        </div>
      </el-col>
      <el-col :span="4">
        <div class="stat-card glass-card hover-lift">
          <div class="stat-label">今日待检</div>
          <div class="stat-value text-warning">{{ stats.todayCount }}<span class="stat-unit">项</span></div>
        </div>
      </el-col>
      <el-col :span="4">
        <div class="stat-card glass-card hover-lift">
          <div class="stat-label">逾期计划</div>
          <div class="stat-value text-danger">{{ stats.overdueCount }}<span class="stat-unit">项</span></div>
        </div>
      </el-col>
      <el-col :span="4">
        <div class="stat-card glass-card hover-lift">
          <div class="stat-label">异常记录</div>
          <div class="stat-value text-danger">{{ stats.abnormalCount }}<span class="stat-unit">条</span></div>
        </div>
      </el-col>
      <el-col :span="4">
        <div class="stat-card glass-card hover-lift">
          <div class="stat-label">巡检总记录</div>
          <div class="stat-value text-success">{{ stats.totalRecordCount }}<span class="stat-unit">条</span></div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="mt-20">
      <el-col :span="24">
        <div class="glass-card">
          <el-tabs v-model="activeTab">
            <el-tab-pane label="设备列表" name="devices">
              <div class="card-header">
                <div class="filters">
                  <el-input v-model="deviceSearch" placeholder="搜索设备名称/编号" style="width: 220px" clearable />
                </div>
              </div>
              <el-table :data="filteredDevices" v-loading="loading" style="width: 100%">
                <el-table-column prop="name" label="设备名称" min-width="140" />
                <el-table-column prop="deviceNo" label="设备编号" min-width="140" />
                <el-table-column prop="location" label="存放位置" min-width="180" />
                <el-table-column prop="clubName" label="所属社团" min-width="120" v-if="isAdmin" />
                <el-table-column label="操作" width="180" align="center">
                  <template #default="{ row }">
                    <el-button type="primary" link @click="editDevice(row)">编辑</el-button>
                    <el-button type="danger" link @click="deleteDevice(row)">删除</el-button>
                  </template>
                </el-table-column>
              </el-table>
              <el-empty v-if="filteredDevices.length === 0 && !loading" description="暂无设备" />
            </el-tab-pane>

            <el-tab-pane label="巡检计划" name="plans">
              <div class="card-header">
                <div class="filters">
                  <el-select v-model="planStatusFilter" placeholder="状态筛选" style="width: 140px" clearable>
                    <el-option label="启用中" value="ACTIVE" />
                    <el-option label="已停用" value="INACTIVE" />
                  </el-select>
                </div>
              </div>
              <el-table :data="filteredPlans" v-loading="loading" style="width: 100%" :row-class-name="planRowClass">
                <el-table-column prop="deviceName" label="设备名称" min-width="140" />
                <el-table-column prop="deviceNo" label="设备编号" min-width="120" />
                <el-table-column prop="deviceLocation" label="位置" min-width="160" />
                <el-table-column prop="cycleDays" label="周期(天)" width="90" align="center" />
                <el-table-column prop="inspectorName" label="巡检人" min-width="100" />
                <el-table-column prop="nextInspectDate" label="下次应检" width="130" align="center">
                  <template #default="{ row }">
                    <span :class="{ 'text-danger': row.isOverdue }">{{ row.nextInspectDate || '-' }}</span>
                  </template>
                </el-table-column>
                <el-table-column prop="lastInspectTime" label="上次巡检" width="160" align="center">
                  <template #default="{ row }">
                    {{ row.lastInspectTime ? formatDateTime(row.lastInspectTime) : '-' }}
                  </template>
                </el-table-column>
                <el-table-column prop="status" label="状态" width="90" align="center">
                  <template #default="{ row }">
                    <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'">
                      {{ row.status === 'ACTIVE' ? '启用' : '停用' }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="逾期" width="70" align="center">
                  <template #default="{ row }">
                    <el-tag v-if="row.isOverdue" type="danger" size="small">逾期</el-tag>
                    <span v-else>-</span>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="200" align="center">
                  <template #default="{ row }">
                    <el-button type="primary" link @click="viewRecords(row)">记录</el-button>
                    <el-button type="success" link @click="editPlan(row)">编辑</el-button>
                    <el-button type="danger" link @click="deletePlan(row)">删除</el-button>
                  </template>
                </el-table-column>
              </el-table>
              <el-empty v-if="filteredPlans.length === 0 && !loading" description="暂无巡检计划" />
            </el-tab-pane>

            <el-tab-pane label="异常记录" name="abnormal">
              <el-table :data="abnormalRecords" v-loading="loading" style="width: 100%">
                <el-table-column prop="deviceName" label="设备名称" min-width="140" />
                <el-table-column prop="deviceLocation" label="位置" min-width="160" />
                <el-table-column prop="result" label="结果" width="90" align="center">
                  <template #default>
                    <el-tag type="danger">异常</el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="remark" label="异常描述" min-width="200" show-overflow-tooltip />
                <el-table-column prop="inspectorName" label="巡检人" min-width="100" />
                <el-table-column prop="inspectTime" label="巡检时间" width="170" align="center">
                  <template #default="{ row }">
                    {{ formatDateTime(row.inspectTime) }}
                  </template>
                </el-table-column>
              </el-table>
              <el-empty v-if="abnormalRecords.length === 0 && !loading" description="暂无异常记录" />
            </el-tab-pane>
          </el-tabs>
        </div>
      </el-col>
    </el-row>

    <el-dialog v-model="deviceDialogVisible" :title="isEditDevice ? '编辑设备' : '新增设备'" width="520px" @close="onDeviceDialogClose">
      <el-form :model="deviceForm" :rules="deviceRules" ref="deviceFormRef" label-width="100px">
        <el-form-item label="设备名称" prop="name">
          <el-input v-model="deviceForm.name" placeholder="请输入设备名称" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="设备编号" prop="deviceNo">
          <el-input v-model="deviceForm.deviceNo" placeholder="请输入设备编号" maxlength="50" />
        </el-form-item>
        <el-form-item label="存放位置" prop="location">
          <el-input v-model="deviceForm.location" placeholder="请输入存放位置" maxlength="255" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="deviceDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitDevice">确认</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="planDialogVisible" :title="isEditPlan ? '编辑巡检计划' : '创建巡检计划'" width="560px" @close="onPlanDialogClose">
      <el-form :model="planForm" :rules="planRules" ref="planFormRef" label-width="110px">
        <el-form-item label="选择设备" prop="deviceId">
          <el-select v-model="planForm.deviceId" placeholder="请选择设备" style="width: 100%" :disabled="isEditPlan">
            <el-option
              v-for="d in devices"
              :key="d.id"
              :label="`${d.name} (${d.deviceNo})`"
              :value="d.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="巡检周期" prop="cycleDays">
          <el-input-number v-model="planForm.cycleDays" :min="1" :max="365" style="width: 160px" />
          <span style="margin-left: 10px; color: #909399">天</span>
        </el-form-item>
        <el-form-item label="巡检人" prop="inspectorId">
          <el-input v-model="planForm.inspectorId" placeholder="请输入巡检人用户ID" style="width: 100%" />
          <div style="color: #909399; font-size: 12px; margin-top: 4px">提示：请输入巡检人的用户ID</div>
        </el-form-item>
        <el-form-item label="首次应检日期" prop="nextInspectDate">
          <el-date-picker
            v-model="planForm.nextInspectDate"
            type="date"
            placeholder="选择日期"
            style="width: 100%"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status" v-if="isEditPlan">
          <el-select v-model="planForm.status" placeholder="选择状态" style="width: 100%">
            <el-option label="启用" value="ACTIVE" />
            <el-option label="停用" value="INACTIVE" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="planDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitPlan">确认</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="recordsDialogVisible" title="巡检记录" width="700px">
      <el-table :data="currentRecords" v-loading="recordsLoading" style="width: 100%">
        <el-table-column prop="result" label="结果" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.result === 'NORMAL' ? 'success' : 'danger'">
              {{ row.result === 'NORMAL' ? '正常' : '异常' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="200" show-overflow-tooltip />
        <el-table-column prop="inspectorName" label="巡检人" width="100" />
        <el-table-column prop="inspectTime" label="巡检时间" width="170" align="center">
          <template #default="{ row }">
            {{ formatDateTime(row.inspectTime) }}
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="currentRecords.length === 0 && !recordsLoading" description="暂无巡检记录" />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, ElForm } from 'element-plus'
import { Plus, Calendar } from '@element-plus/icons-vue'
import { useUserStore } from '../../store/user'
import inspectionApi, {
  type Device,
  type InspectionPlan,
  type InspectionRecord,
  type InspectionStats
} from '../../api/inspections'

const userStore = useUserStore()
const isAdmin = computed(() =>
  ['ADMIN', 'UNION_ADMIN'].includes(userStore.role ?? '')
)

const loading = ref(false)
const recordsLoading = ref(false)
const activeTab = ref('devices')
const deviceSearch = ref('')
const planStatusFilter = ref('')

const stats = ref<InspectionStats>({
  deviceCount: 0,
  activePlanCount: 0,
  overdueCount: 0,
  todayCount: 0,
  abnormalCount: 0,
  totalRecordCount: 0
})

const devices = ref<Device[]>([])
const plans = ref<InspectionPlan[]>([])
const allRecords = ref<InspectionRecord[]>([])

const deviceDialogVisible = ref(false)
const isEditDevice = ref(false)
const deviceFormRef = ref<InstanceType<typeof ElForm>>()
const deviceForm = reactive({
  id: 0,
  clubId: 0,
  name: '',
  deviceNo: '',
  location: ''
})
const deviceRules = {
  name: [{ required: true, message: '请输入设备名称', trigger: 'blur' }],
  deviceNo: [{ required: true, message: '请输入设备编号', trigger: 'blur' }],
  location: [{ required: true, message: '请输入存放位置', trigger: 'blur' }]
}

const planDialogVisible = ref(false)
const isEditPlan = ref(false)
const planFormRef = ref<InstanceType<typeof ElForm>>()
const planForm = reactive({
  id: 0,
  deviceId: 0,
  cycleDays: 7,
  cronExpr: '',
  inspectorId: 0,
  nextInspectDate: '',
  status: 'ACTIVE' as 'ACTIVE' | 'INACTIVE'
})
const planRules = {
  deviceId: [{ required: true, message: '请选择设备', trigger: 'change' }],
  cycleDays: [{ required: true, message: '请输入巡检周期', trigger: 'blur' }],
  inspectorId: [{ required: true, message: '请输入巡检人ID', trigger: 'blur' }]
}

const recordsDialogVisible = ref(false)
const currentRecords = ref<InspectionRecord[]>([])

const filteredDevices = computed(() => {
  if (!deviceSearch.value) return devices.value
  const keyword = deviceSearch.value.toLowerCase()
  return devices.value.filter(
    (d: Device) =>
      d.name.toLowerCase().includes(keyword) ||
      d.deviceNo.toLowerCase().includes(keyword)
  )
})

const filteredPlans = computed(() => {
  if (!planStatusFilter.value) return plans.value
  return plans.value.filter((p: InspectionPlan) => p.status === planStatusFilter.value)
})

const abnormalRecords = computed(() => {
  return allRecords.value.filter((r: InspectionRecord) => r.result === 'ABNORMAL')
})

const loadStats = async () => {
  try {
    const data = await inspectionApi.getStats()
    stats.value = data as unknown as InspectionStats
  } catch (err) {
    console.error('Failed to load stats:', err)
  }
}

const loadDevices = async () => {
  try {
    const data = await inspectionApi.getDevices()
    devices.value = data as unknown as Device[]
  } catch (err) {
    console.error('Failed to load devices:', err)
  }
}

const loadPlans = async () => {
  loading.value = true
  try {
    const data = await inspectionApi.getPlans()
    plans.value = data as unknown as InspectionPlan[]
  } catch (err) {
    console.error('Failed to load plans:', err)
  } finally {
    loading.value = false
  }
}

const loadAllRecords = async () => {
  const all: InspectionRecord[] = []
  for (const plan of plans.value) {
    try {
      const data = await inspectionApi.getRecordsByPlan(plan.id)
      all.push(...(data as unknown as InspectionRecord[]))
    } catch (err) {
      console.error(`Failed to load records for plan ${plan.id}:`, err)
    }
  }
  allRecords.value = all
}

const planRowClass = ({ row }: { row: InspectionPlan }) => {
  if (row.isOverdue) return 'row-overdue'
  return ''
}

const openDeviceDialog = () => {
  isEditDevice.value = false
  deviceForm.id = 0
  deviceForm.clubId = userStore.userInfo?.clubId || 0
  deviceForm.name = ''
  deviceForm.deviceNo = ''
  deviceForm.location = ''
  deviceDialogVisible.value = true
}

const editDevice = (row: Device) => {
  isEditDevice.value = true
  deviceForm.id = row.id
  deviceForm.clubId = row.clubId
  deviceForm.name = row.name
  deviceForm.deviceNo = row.deviceNo
  deviceForm.location = row.location
  deviceDialogVisible.value = true
}

const onDeviceDialogClose = () => {
  deviceFormRef.value?.resetFields()
}

const submitDevice = async () => {
  if (!deviceFormRef.value) return
  try {
    await deviceFormRef.value.validate()
    if (isEditDevice.value) {
      const updateData = {
        id: deviceForm.id,
        clubId: deviceForm.clubId,
        name: deviceForm.name,
        deviceNo: deviceForm.deviceNo,
        location: deviceForm.location
      }
      await inspectionApi.updateDevice(updateData as unknown as Device)
      ElMessage.success('设备更新成功')
    } else {
      const createData = {
        clubId: deviceForm.clubId,
        name: deviceForm.name,
        deviceNo: deviceForm.deviceNo,
        location: deviceForm.location
      }
      await inspectionApi.createDevice(createData)
      ElMessage.success('设备创建成功')
    }
    deviceDialogVisible.value = false
    loadDevices()
    loadStats()
  } catch (err) {
    console.error('Failed to submit device:', err)
  }
}

const deleteDevice = async (row: Device) => {
  try {
    await ElMessageBox.confirm(`确定要删除设备"${row.name}"吗？`, '确认删除', {
      type: 'warning'
    })
    await inspectionApi.deleteDevice(row.id)
    ElMessage.success('删除成功')
    loadDevices()
    loadStats()
  } catch (err) {
    if (err !== 'cancel') {
      console.error('Failed to delete device:', err)
    }
  }
}

const openPlanDialog = () => {
  isEditPlan.value = false
  planForm.id = 0
  planForm.deviceId = 0
  planForm.cycleDays = 7
  planForm.cronExpr = ''
  planForm.inspectorId = 0
  planForm.nextInspectDate = ''
  planForm.status = 'ACTIVE'
  planDialogVisible.value = true
}

const editPlan = (row: InspectionPlan) => {
  isEditPlan.value = true
  planForm.id = row.id
  planForm.deviceId = row.deviceId
  planForm.cycleDays = row.cycleDays ?? 7
  planForm.cronExpr = row.cronExpr ?? ''
  planForm.inspectorId = row.inspectorId
  planForm.nextInspectDate = row.nextInspectDate ?? ''
  planForm.status = (row.status || 'ACTIVE') as 'ACTIVE' | 'INACTIVE'
  planDialogVisible.value = true
}

const onPlanDialogClose = () => {
  planFormRef.value?.resetFields()
}

const submitPlan = async () => {
  if (!planFormRef.value) return
  try {
    await planFormRef.value.validate()
    if (isEditPlan.value) {
      const updateData = {
        id: planForm.id,
        cycleDays: planForm.cycleDays,
        cronExpr: planForm.cronExpr,
        inspectorId: planForm.inspectorId,
        nextInspectDate: planForm.nextInspectDate,
        status: planForm.status
      }
      await inspectionApi.updatePlan(updateData)
      ElMessage.success('计划更新成功')
    } else {
      const createData = {
        deviceId: planForm.deviceId,
        cycleDays: planForm.cycleDays,
        cronExpr: planForm.cronExpr,
        inspectorId: planForm.inspectorId,
        nextInspectDate: planForm.nextInspectDate
      }
      await inspectionApi.createPlan(createData)
      ElMessage.success('计划创建成功')
    }
    planDialogVisible.value = false
    loadPlans()
    loadStats()
  } catch (err) {
    console.error('Failed to submit plan:', err)
  }
}

const deletePlan = async (row: InspectionPlan) => {
  try {
    await ElMessageBox.confirm(`确定要删除该巡检计划吗？`, '确认删除', {
      type: 'warning'
    })
    await inspectionApi.deletePlan(row.id)
    ElMessage.success('删除成功')
    loadPlans()
    loadStats()
  } catch (err) {
    if (err !== 'cancel') {
      console.error('Failed to delete plan:', err)
    }
  }
}

const viewRecords = async (row: InspectionPlan) => {
  recordsLoading.value = true
  recordsDialogVisible.value = true
  currentRecords.value = []
  try {
    const data = await inspectionApi.getRecordsByPlan(row.id)
    currentRecords.value = data as unknown as InspectionRecord[]
  } catch (err) {
    console.error('Failed to load records:', err)
  } finally {
    recordsLoading.value = false
  }
}

const formatDateTime = (dateStr: string) => {
  return dateStr ? new Date(dateStr).toLocaleString('zh-CN') : ''
}

onMounted(async () => {
  await loadDevices()
  await loadPlans()
  await loadStats()
  loadAllRecords()
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
.text-info { color: #909399; }
.mt-20 { margin-top: 20px; }
.card-header {
  padding: 0 0 16px 0;
  display: flex;
  justify-content: flex-end;
  align-items: center;
}
.filters { display: flex; gap: 12px; }
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
