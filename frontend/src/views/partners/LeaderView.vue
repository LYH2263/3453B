<template>
  <div class="partners-page">
    <el-row :gutter="20">
      <el-col :span="24">
        <div class="glass-card page-header">
          <div class="header-left">
            <h2>合作伙伴台账</h2>
            <p class="subtitle">管理本社团的合作伙伴信息</p>
          </div>
          <div class="header-right">
            <el-button @click="handleExport">
              <el-icon><Download /></el-icon>
              导出Excel
            </el-button>
            <el-button type="primary" @click="openCreateDialog">
              <el-icon><Plus /></el-icon>
              新增合作伙伴
            </el-button>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <div class="stat-card glass-card hover-lift">
          <div class="stat-label">合作总数</div>
          <div class="stat-value text-primary">{{ stats?.total || 0 }}<span class="stat-unit">家</span></div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card glass-card hover-lift">
          <div class="stat-label">合作中</div>
          <div class="stat-value text-success">{{ stats?.activeCount || 0 }}<span class="stat-unit">家</span></div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card glass-card hover-lift">
          <div class="stat-label">30天内到期</div>
          <div class="stat-value text-warning">{{ stats?.expiringIn30Days || 0 }}<span class="stat-unit">家</span></div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card glass-card hover-lift">
          <div class="stat-label">已过期</div>
          <div class="stat-value text-danger">{{ stats?.expiredCount || 0 }}<span class="stat-unit">家</span></div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="mt-20">
      <el-col :span="24">
        <div class="glass-card filter-card">
          <el-form :inline="true" :model="queryParams" @submit.prevent>
            <el-form-item label="类型">
              <el-select v-model="queryParams.type" placeholder="全部类型" style="width: 140px" clearable>
                <el-option label="企业" value="ENTERPRISE" />
                <el-option label="校组织" value="SCHOOL_ORG" />
                <el-option label="其他" value="OTHER" />
              </el-select>
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="queryParams.status" placeholder="全部状态" style="width: 140px" clearable>
                <el-option label="合作中" value="ACTIVE" />
                <el-option label="已过期" value="EXPIRED" />
              </el-select>
            </el-form-item>
            <el-form-item label="到期筛选">
              <el-checkbox v-model="queryParams.expiringSoon" label="30天内到期" />
            </el-form-item>
            <el-form-item label="关键词">
              <el-input v-model="queryParams.keyword" placeholder="名称/联系人/电话" style="width: 200px" clearable />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadList">
                <el-icon><Search /></el-icon>
                查询
              </el-button>
              <el-button @click="resetFilters">重置</el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="mt-20">
      <el-col :span="24">
        <div class="glass-card">
          <div class="card-header">
            <h3>合作伙伴列表</h3>
          </div>
          <el-table :data="list" v-loading="loading" style="width: 100%">
            <el-table-column prop="partnerName" label="合作伙伴名称" min-width="180" />
            <el-table-column prop="type" label="类型" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="getTypeTagType(row.type)">{{ getTypeText(row.type) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="contactName" label="联系人" width="100" />
            <el-table-column prop="contactPhone" label="联系电话" width="140" />
            <el-table-column prop="contractStart" label="合同开始" width="120">
              <template #default="{ row }">
                {{ formatDate(row.contractStart) }}
              </template>
            </el-table-column>
            <el-table-column prop="contractEnd" label="合同结束" width="120">
              <template #default="{ row }">
                <span :style="getExpireStyle(row)">
                  {{ formatDate(row.contractEnd) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="daysToExpire" label="距到期" width="100" align="center">
              <template #default="{ row }">
                <el-tag v-if="row.daysToExpire === null || row.daysToExpire === undefined" type="info" size="small">无</el-tag>
                <el-tag v-else-if="row.daysToExpire < 0" type="danger" size="small">已过期{{ -row.daysToExpire }}天</el-tag>
                <el-tag v-else-if="row.daysToExpire <= 30" type="warning" effect="dark" size="small">{{ row.daysToExpire }}天</el-tag>
                <el-tag v-else type="success" size="small">{{ row.daysToExpire }}天</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'">
                  {{ row.status === 'ACTIVE' ? '合作中' : '已过期' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="合同" width="80" align="center">
              <template #default="{ row }">
                <el-link v-if="row.contractUrl" :href="row.contractUrl" target="_blank" type="primary">
                  <el-icon><View /></el-icon>
                </el-link>
                <span v-else style="color: #c0c4cc">-</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="180" align="center" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link @click="openEditDialog(row)">
                  <el-icon><Edit /></el-icon>
                  编辑
                </el-button>
                <el-button type="danger" link @click="handleDelete(row)">
                  <el-icon><Delete /></el-icon>
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="list.length === 0 && !loading" description="暂无合作伙伴数据" />
        </div>
      </el-col>
    </el-row>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑合作伙伴' : '新增合作伙伴'" width="640px">
      <el-form
        :model="form"
        :rules="formRules"
        ref="formRef"
        label-width="100px"
      >
        <el-form-item label="合作伙伴名称" prop="partnerName">
          <el-input v-model="form.partnerName" placeholder="请输入合作伙伴名称" />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-radio-group v-model="form.type">
            <el-radio value="ENTERPRISE">企业</el-radio>
            <el-radio value="SCHOOL_ORG">校组织</el-radio>
            <el-radio value="OTHER">其他</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="联系人" prop="contactName">
              <el-input v-model="form.contactName" placeholder="请输入联系人姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="contactPhone">
              <el-input v-model="form.contactPhone" placeholder="请输入联系电话" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="合同开始" prop="contractStart">
              <el-date-picker
                v-model="form.contractStart"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="选择开始日期"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="合同结束" prop="contractEnd">
              <el-date-picker
                v-model="form.contractEnd"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="选择结束日期"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="合同附件URL" prop="contractUrl">
          <el-input v-model="form.contractUrl" placeholder="请输入合同附件URL（可选）" />
        </el-form-item>
        <el-form-item label="状态" prop="status" v-if="isEdit">
          <el-radio-group v-model="form.status">
            <el-radio value="ACTIVE">合作中</el-radio>
            <el-radio value="EXPIRED">已过期</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注信息" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确认提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, ElForm } from 'element-plus'
import {
  Download, Plus, Search, Edit, Delete, View
} from '@element-plus/icons-vue'
import partnerApi, {
  type Partner,
  type PartnerCreateRequest,
  type PartnerUpdateRequest,
  type PartnerQueryParams,
  type PartnerStats
} from '../api/partners'
import { useUserStore } from '../store/user'

const userStore = useUserStore()
const loading = ref(false)
const submitLoading = ref(false)
const list = ref<Partner[]>([])
const stats = ref<PartnerStats | null>(null)

const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref<number | null>(null)
const formRef = ref<InstanceType<typeof ElForm>>()

const queryParams = reactive<PartnerQueryParams>({
  type: '',
  status: '',
  expiringSoon: false,
  keyword: ''
})

const form = reactive<PartnerCreateRequest & PartnerUpdateRequest>({
  partnerName: '',
  type: 'ENTERPRISE',
  contactName: '',
  contactPhone: '',
  contractStart: undefined,
  contractEnd: undefined,
  contractUrl: '',
  remark: '',
  status: 'ACTIVE'
})

const formRules = {
  partnerName: [{ required: true, message: '请输入合作伙伴名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }]
}

const getTypeText = (type: string) => {
  const map: Record<string, string> = {
    ENTERPRISE: '企业',
    SCHOOL_ORG: '校组织',
    OTHER: '其他'
  }
  return map[type] || type
}

const getTypeTagType = (type: string) => {
  const map: Record<string, 'primary' | 'success' | 'info'> = {
    ENTERPRISE: 'primary',
    SCHOOL_ORG: 'success',
    OTHER: 'info'
  }
  return map[type] || 'info'
}

const getExpireStyle = (row: Partner) => {
  if (row.daysToExpire === null || row.daysToExpire === undefined) return {}
  if (row.daysToExpire < 0) return { color: '#f56c6c', fontWeight: 'bold' }
  if (row.daysToExpire <= 30) return { color: '#e6a23c', fontWeight: 'bold' }
  return {}
}

const formatDate = (date?: string) => {
  return date ? new Date(date).toLocaleDateString('zh-CN') : '-'
}

const loadStats = async () => {
  try {
    const data = await partnerApi.getStats()
    stats.value = data as unknown as PartnerStats
  } catch (err) {
    console.error('Failed to load stats:', err)
  }
}

const loadList = async () => {
  loading.value = true
  try {
    const params: PartnerQueryParams = { ...queryParams }
    if (!params.type) delete params.type
    if (!params.status) delete params.status
    if (!params.keyword) delete params.keyword
    if (!params.expiringSoon) delete params.expiringSoon
    const data = await partnerApi.list(params)
    list.value = data as unknown as Partner[]
  } catch (err) {
    console.error('Failed to load list:', err)
  } finally {
    loading.value = false
  }
}

const resetFilters = () => {
  queryParams.type = ''
  queryParams.status = ''
  queryParams.expiringSoon = false
  queryParams.keyword = ''
  loadList()
}

const openCreateDialog = () => {
  isEdit.value = false
  editId.value = null
  form.partnerName = ''
  form.type = 'ENTERPRISE'
  form.contactName = ''
  form.contactPhone = ''
  form.contractStart = undefined
  form.contractEnd = undefined
  form.contractUrl = ''
  form.remark = ''
  form.status = 'ACTIVE'
  form.clubId = userStore.userInfo?.clubId
  dialogVisible.value = true
}

const openEditDialog = (row: Partner) => {
  isEdit.value = true
  editId.value = row.id
  form.partnerName = row.partnerName
  form.type = row.type
  form.contactName = row.contactName || ''
  form.contactPhone = row.contactPhone || ''
  form.contractStart = row.contractStart
  form.contractEnd = row.contractEnd
  form.contractUrl = row.contractUrl || ''
  form.remark = row.remark || ''
  form.status = row.status
  form.clubId = row.clubId
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch {
    return
  }

  if (form.contractStart && form.contractEnd) {
    if (new Date(form.contractEnd) < new Date(form.contractStart)) {
      ElMessage.error('合同结束日期不能早于开始日期')
      return
    }
  }

  submitLoading.value = true
  try {
    if (isEdit.value && editId.value) {
      const payload: PartnerUpdateRequest = { ...form }
      await partnerApi.update(editId.value, payload)
      ElMessage.success('修改成功')
    } else {
      const payload: PartnerCreateRequest = {
        partnerName: form.partnerName,
        type: form.type,
        contactName: form.contactName,
        contactPhone: form.contactPhone,
        contractStart: form.contractStart,
        contractEnd: form.contractEnd,
        contractUrl: form.contractUrl,
        remark: form.remark,
        clubId: form.clubId || userStore.userInfo?.clubId!
      }
      await partnerApi.create(payload)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadList()
    loadStats()
  } catch (err: any) {
    ElMessage.error(err?.message || '操作失败')
  } finally {
    submitLoading.value = false
  }
}

const handleDelete = (row: Partner) => {
  ElMessageBox.confirm(
    `确定要删除合作伙伴「${row.partnerName}」吗？此操作不可撤销。`,
    '删除确认',
    {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await partnerApi.delete(row.id)
      ElMessage.success('删除成功')
      loadList()
      loadStats()
    } catch (err: any) {
      ElMessage.error(err?.message || '删除失败')
    }
  }).catch(() => {})
}

const handleExport = () => {
  const params: PartnerQueryParams = { ...queryParams }
  if (!params.type) delete params.type
  if (!params.status) delete params.status
  if (!params.keyword) delete params.keyword
  if (!params.expiringSoon) delete params.expiringSoon
  partnerApi.export(params)
}

onMounted(() => {
  loadStats()
  loadList()
})
</script>

<style scoped>
.partners-page { padding: 0; }
.page-header {
  padding: 24px 30px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.page-header h2 { margin: 0; font-size: 22px; color: #303133; }
.subtitle { margin: 6px 0 0 0; color: #909399; font-size: 14px; }
.filter-card { padding: 16px 24px; }
.stats-row { margin-top: 20px; }
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
}
.card-header h3 { margin: 0; font-size: 16px; color: #303133; }
:deep(.el-table) {
  --el-table-bg-color: transparent;
  --el-table-tr-bg-color: transparent;
  --el-table-header-bg-color: rgba(0,0,0,0.02);
}
</style>
