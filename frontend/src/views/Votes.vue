<template>
  <div class="vote-container">
    <el-card class="glass-card mb-20">
      <div class="toolbar" style="display:flex; justify-content:space-between; align-items:center;">
        <h2>社团投票</h2>
        <el-button v-if="isClubLeader" type="primary" @click="openCreateDialog">创建投票</el-button>
      </div>
    </el-card>

    <el-row :gutter="20">
      <el-col :span="8" v-for="v in votes" :key="v.id">
        <el-card class="vote-card mb-20 hover-lift" :class="{ 'vote-closed': v.status === 'CLOSED' }">
          <template #header>
            <div class="card-header">
              <span class="vote-title">{{ v.title }}</span>
              <el-tag :type="v.status === 'OPEN' ? 'success' : 'info'" size="small">
                {{ v.status === 'OPEN' ? '进行中' : '已截止' }}
              </el-tag>
            </div>
          </template>
          <div class="vote-meta">
            <span>类型: {{ v.type === 'SINGLE' ? '单选' : '多选' }}</span>
            <span>截止: {{ formatDate(v.deadline) }}</span>
          </div>
          <div class="vote-actions mt-20">
            <el-button v-if="v.status === 'OPEN' && !v.hasVoted" type="primary" size="small" @click="openVoteDialog(v)">投票</el-button>
            <el-button v-else-if="v.status === 'OPEN' && v.hasVoted" type="info" size="small" disabled>已投票</el-button>
            <el-button type="success" size="small" @click="viewResult(v)">查看结果</el-button>
            <el-button v-if="isClubLeader && v.status === 'OPEN'" type="warning" size="small" @click="closeVote(v)">关闭</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <el-empty v-if="!votes.length" description="暂无投票" />

    <!-- Create Vote Dialog -->
    <el-dialog v-model="showCreateDialog" title="创建投票" width="600px">
      <el-form :model="createForm" label-width="100px">
        <el-form-item label="投票标题" required>
          <el-input v-model="createForm.title" placeholder="请输入投票标题" />
        </el-form-item>
        <el-form-item label="投票类型" required>
          <el-radio-group v-model="createForm.type" @change="onTypeChange">
            <el-radio label="SINGLE">单选</el-radio>
            <el-radio label="MULTIPLE">多选</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="createForm.type === 'MULTIPLE'" label="最多可选">
          <el-input-number v-model="createForm.maxChoices" :min="2" :max="10" />
        </el-form-item>
        <el-form-item label="截止时间" required>
          <el-date-picker v-model="createForm.deadline" type="datetime" placeholder="选择截止时间" style="width:100%" />
        </el-form-item>
        <el-form-item label="投票选项" required>
          <div v-for="(_, idx) in createForm.options" :key="idx" style="display:flex; gap:10px; margin-bottom:8px;">
            <el-input v-model="createForm.options[idx]" :placeholder="`选项 ${idx + 1}`" />
            <el-button v-if="createForm.options.length > 2" type="danger" plain @click="removeOption(idx)">删除</el-button>
          </div>
          <el-button type="primary" plain @click="addOption">+ 添加选项</el-button>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="submitCreate">创建</el-button>
      </template>
    </el-dialog>

    <!-- Vote Dialog -->
    <el-dialog v-model="showVoteDialog" title="投票" width="500px">
      <div v-if="currentVote" class="vote-dialog-content">
        <h3>{{ currentVote.title }}</h3>
        <p class="vote-type-hint">
          {{ currentVote.type === 'SINGLE' ? '单选投票，请选择一个选项' : `多选投票，最多选择 ${currentVote.maxChoices} 个选项` }}
        </p>
        <el-checkbox-group v-if="currentVote.type === 'MULTIPLE'" v-model="selectedOptions" :max="currentVote.maxChoices">
          <div v-for="opt in currentVoteDetail?.options" :key="opt.id" class="option-item">
            <el-checkbox :label="opt.id">{{ opt.optionText }}</el-checkbox>
          </div>
        </el-checkbox-group>
        <el-radio-group v-else v-model="selectedRadio">
          <div v-for="opt in currentVoteDetail?.options" :key="opt.id" class="option-item">
            <el-radio :label="opt.id">{{ opt.optionText }}</el-radio>
          </div>
        </el-radio-group>
      </div>
      <template #footer>
        <el-button @click="showVoteDialog = false">取消</el-button>
        <el-button type="primary" @click="submitBallot">提交选票</el-button>
      </template>
    </el-dialog>

    <!-- Result Dialog -->
    <el-dialog v-model="showResultDialog" title="投票结果" width="650px">
      <div v-if="voteResult" class="result-content">
        <h3>{{ voteResult.title }}</h3>
        <p class="total-votes">总票数: {{ voteResult.totalVotes }}</p>
        <div ref="pieChartRef" style="width:100%; height:350px;"></div>
        <el-table :data="voteResult.options" class="mt-20" style="width:100%">
          <el-table-column prop="optionText" label="选项" />
          <el-table-column prop="voteCount" label="票数" width="100" />
          <el-table-column prop="percentage" label="占比" width="100">
            <template #default="{ row }">{{ row.percentage }}%</template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue'
import { useUserStore } from '../store/user'
import voteApi from '../api/votes'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as echarts from 'echarts'

const userStore = useUserStore()
const isClubLeader = ref(['ADMIN', 'UNION_ADMIN', 'CLUB_LEADER'].includes(userStore.role || ''))

const votes = ref<any[]>([])
const showCreateDialog = ref(false)
const createForm = reactive({
  title: '',
  type: 'SINGLE',
  maxChoices: 2,
  deadline: '',
  options: ['', '']
})

const showVoteDialog = ref(false)
const currentVote = ref<any>(null)
const currentVoteDetail = ref<any>(null)
const selectedOptions = ref<number[]>([])
const selectedRadio = ref<number | null>(null)

const showResultDialog = ref(false)
const voteResult = ref<any>(null)
const pieChartRef = ref<HTMLElement | null>(null)

const formatDate = (dt: string) => {
  if (!dt) return '-'
  return new Date(dt).toLocaleString('zh-CN', { hour12: false })
}

const loadVotes = async () => {
  try {
    votes.value = (await voteApi.getVotes()) as any
  } catch (err) {
    console.error('Failed to load votes:', err)
  }
}

const openCreateDialog = () => {
  createForm.title = ''
  createForm.type = 'SINGLE'
  createForm.maxChoices = 2
  createForm.deadline = ''
  createForm.options = ['', '']
  showCreateDialog.value = true
}

const onTypeChange = (val: string) => {
  if (val === 'SINGLE') {
    createForm.maxChoices = 1
  }
}

const addOption = () => {
  createForm.options.push('')
}

const removeOption = (idx: number) => {
  createForm.options.splice(idx, 1)
}

const submitCreate = async () => {
  if (!createForm.title) { ElMessage.warning('请输入标题'); return }
  if (!createForm.deadline) { ElMessage.warning('请选择截止时间'); return }
  const filtered = createForm.options.filter(o => o.trim())
  if (filtered.length < 2) { ElMessage.warning('至少需要2个有效选项'); return }

  try {
    const deadlineStr = new Date(createForm.deadline).toISOString().slice(0, 19)
    await voteApi.createVote({
      title: createForm.title,
      type: createForm.type,
      deadline: deadlineStr,
      maxChoices: createForm.type === 'MULTIPLE' ? createForm.maxChoices : undefined,
      options: filtered
    })
    ElMessage.success('投票创建成功')
    showCreateDialog.value = false
    loadVotes()
  } catch (err) {
    console.error('Create vote failed:', err)
  }
}

const openVoteDialog = async (v: any) => {
  try {
    const detail = (await voteApi.getVoteDetail(v.id)) as any
    currentVote.value = v
    currentVoteDetail.value = detail
    selectedOptions.value = []
    selectedRadio.value = null
    showVoteDialog.value = true
  } catch (err) {
    console.error('Load vote detail failed:', err)
  }
}

const submitBallot = async () => {
  if (!currentVote.value) return
  let optionIds: number[]
  if (currentVote.value.type === 'SINGLE') {
    if (selectedRadio.value === null) { ElMessage.warning('请选择选项'); return }
    optionIds = [selectedRadio.value]
  } else {
    if (selectedOptions.value.length === 0) { ElMessage.warning('请选择选项'); return }
    optionIds = selectedOptions.value
  }

  try {
    await voteApi.submitBallot(currentVote.value.id, optionIds)
    ElMessage.success('投票成功')
    showVoteDialog.value = false
    loadVotes()
  } catch (err) {
    console.error('Submit ballot failed:', err)
  }
}

const viewResult = async (v: any) => {
  try {
    const result = (await voteApi.getVoteResult(v.id)) as any
    voteResult.value = result
    showResultDialog.value = true
    await nextTick()
    renderPieChart()
  } catch (err) {
    console.error('Load vote result failed:', err)
  }
}

const renderPieChart = () => {
  if (!pieChartRef.value || !voteResult.value) return
  const chart = echarts.init(pieChartRef.value)
  const data = voteResult.value.options.map((opt: any) => ({
    name: opt.optionText,
    value: opt.voteCount
  }))
  chart.setOption({
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c}票 ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left',
      top: 'center'
    },
    series: [
      {
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['60%', '50%'],
        avoidLabelOverlap: true,
        itemStyle: {
          borderRadius: 6,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: true,
          formatter: '{b}\n{d}%'
        },
        data: data
      }
    ]
  })
  window.addEventListener('resize', () => chart.resize())
}

const closeVote = async (v: any) => {
  try {
    await ElMessageBox.confirm('确定要关闭该投票吗？', '提示', { type: 'warning' })
    await voteApi.closeVote(v.id)
    ElMessage.success('投票已关闭')
    loadVotes()
  } catch (err) {
    if (err !== 'cancel') {
      console.error('Close vote failed:', err)
    }
  }
}

onMounted(() => {
  loadVotes()
})
</script>

<style scoped>
.vote-container {
  padding: 10px;
}
.mb-20 { margin-bottom: 20px; }
.mt-20 { margin-top: 20px; }
.toolbar h2 { margin: 0; }

.vote-card {
  border-radius: 8px;
  transition: transform 0.2s;
}
.hover-lift:hover {
  transform: translateY(-3px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}
.vote-closed {
  opacity: 0.75;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.vote-title {
  font-weight: 600;
  font-size: 15px;
}
.vote-meta {
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 13px;
  color: #909399;
}
.vote-actions {
  display: flex;
  gap: 8px;
}

.vote-dialog-content h3 {
  margin: 0 0 10px;
}
.vote-type-hint {
  font-size: 13px;
  color: #909399;
  margin-bottom: 15px;
}
.option-item {
  margin-bottom: 10px;
  padding: 8px 12px;
  border-radius: 6px;
  background: #f5f7fa;
}

.result-content h3 { margin: 0 0 8px; }
.total-votes { font-size: 14px; color: #606266; margin-bottom: 15px; }
</style>
