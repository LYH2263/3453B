<template>
  <div class="volunteer-page">
    <el-row :gutter="20">
      <el-col :span="24">
        <div class="glass-card page-header">
          <div class="header-left">
            <h2>志愿服务 - 全校统计</h2>
            <p class="subtitle">查看全校志愿服务数据统计</p>
          </div>
          <div class="header-right">
            <el-button @click="exportRecords">
              <el-icon><Download /></el-icon>
              导出记录
            </el-button>
            <el-button type="primary" @click="exportStats">
              <el-icon><Document /></el-icon>
              导出统计
            </el-button>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="mt-20">
      <el-col :span="24">
        <div class="glass-card filter-card">
          <el-form :inline="true" :model="queryParams" @submit.prevent>
            <el-form-item label="时间范围">
              <el-date-picker
                v-model="dateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
            <el-form-item label="社团">
              <el-select v-model="queryParams.clubId" placeholder="全部社团" style="width: 160px" clearable>
                <el-option v-for="club in clubs" :key="club.id" :label="club.name" :value="club.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="queryParams.status" placeholder="全部状态" style="width: 140px" clearable>
                <el-option label="待审核" value="PENDING" />
                <el-option label="已通过" value="APPROVED" />
                <el-option label="已驳回" value="REJECTED" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadData">
                <el-icon><Search /></el-icon>
                查询
              </el-button>
              <el-button @click="resetFilters">重置</el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <div class="stat-card glass-card hover-lift">
          <div class="stat-icon primary">
            <el-icon><DocumentCopy /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-label">总记录数</div>
            <div class="stat-value">{{ summary?.totalRecords || 0 }}</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card glass-card hover-lift">
          <div class="stat-icon success">
            <el-icon><CircleCheck /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-label">已通过</div>
            <div class="stat-value text-success">{{ summary?.approvedRecords || 0 }}</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card glass-card hover-lift">
          <div class="stat-icon primary">
            <el-icon><Clock /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-label">总服务时长</div>
            <div class="stat-value text-primary">{{ summary?.totalHours || 0 }}<span class="stat-unit">小时</span></div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card glass-card hover-lift">
          <div class="stat-icon warning">
            <el-icon><User /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-label">参与人数</div>
            <div class="stat-value text-warning">{{ summary?.participantCount || 0 }}<span class="stat-unit">人</span></div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="mt-20">
      <el-col :span="14">
        <div class="glass-card">
          <div class="card-header">
            <h3>服务时长趋势</h3>
          </div>
          <div ref="trendChartRef" style="height: 320px;"></div>
        </div>
      </el-col>
      <el-col :span="10">
        <div class="glass-card">
          <div class="card-header">
            <h3>各社团服务时长排行</h3>
          </div>
          <el-table :data="summary?.clubStats || []" style="width: 100%">
            <el-table-column type="index" label="排名" width="60" align="center">
              <template #default="{ $index }">
                <el-tag v-if="$index === 0" type="warning" effect="dark">🥇</el-tag>
                <el-tag v-else-if="$index === 1" type="info" effect="dark">🥈</el-tag>
                <el-tag v-else-if="$index === 2" type="danger" effect="dark">🥉</el-tag>
                <span v-else>{{ $index + 1 }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="clubName" label="社团名称" min-width="120" />
            <el-table-column prop="totalHours" label="时长(小时)" width="100" align="center" />
            <el-table-column prop="recordCount" label="记录数" width="80" align="center" />
          </el-table>
          <el-empty v-if="!summary?.clubStats?.length" description="暂无数据" />
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="mt-20">
      <el-col :span="24">
        <div class="glass-card">
          <div class="card-header">
            <h3>服务时长排行榜</h3>
          </div>
          <el-table :data="rankingList" v-loading="rankingLoading" style="width: 100%">
            <el-table-column type="index" label="排名" width="60" align="center">
              <template #default="{ $index }">
                <el-tag v-if="$index === 0" type="warning" effect="dark">🥇</el-tag>
                <el-tag v-else-if="$index === 1" type="info" effect="dark">🥈</el-tag>
                <el-tag v-else-if="$index === 2" type="danger" effect="dark">🥉</el-tag>
                <span v-else>{{ $index + 1 }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="studentId" label="学号" width="130" />
            <el-table-column prop="userName" label="姓名" width="110" />
            <el-table-column prop="totalHours" label="累计时长(小时)" width="130" align="center">
              <template #default="{ row }">
                <span style="font-weight: bold; color: #409eff">{{ row.totalHours }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="approvedCount" label="通过记录" width="100" align="center" />
            <el-table-column prop="pendingCount" label="待审核" width="90" align="center" />
            <el-table-column prop="lastServiceDate" label="最近服务" width="130">
              <template #default="{ row }">
                {{ row.lastServiceDate ? formatDate(row.lastServiceDate) : '-' }}
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="mt-20">
      <el-col :span="24">
        <div class="glass-card">
          <div class="card-header">
            <h3>所有服务记录</h3>
          </div>
          <el-table :data="allRecords" v-loading="recordsLoading" style="width: 100%">
            <el-table-column prop="studentNo" label="学号" width="130" />
            <el-table-column prop="studentName" label="姓名" width="100" />
            <el-table-column prop="activityName" label="活动名称" min-width="180" />
            <el-table-column prop="serviceDate" label="服务日期" width="130">
              <template #default="{ row }">
                {{ formatDate(row.serviceDate) }}
              </template>
            </el-table-column>
            <el-table-column prop="hours" label="时长(小时)" width="100" align="center" />
            <el-table-column prop="clubName" label="所属社团" width="130" />
            <el-table-column prop="status" label="状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)">
                  {{ getStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="auditorName" label="审核人" width="100" />
          </el-table>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, watch } from 'vue'
import * as echarts from 'echarts'
import { Download, Document, Search, DocumentCopy, CircleCheck, Clock, User } from '@element-plus/icons-vue'
import volunteerApi, {
  type VolunteerStatsSummary,
  type VolunteerStat,
  type VolunteerRecord,
  type QueryParams
} from '../../api/volunteer'
import request from '../../utils/request'

const loading = ref(false)
const recordsLoading = ref(false)
const rankingLoading = ref(false)
const summary = ref<VolunteerStatsSummary | null>(null)
const allRecords = ref<VolunteerRecord[]>([])
const rankingList = ref<VolunteerStat[]>([])
const clubs = ref<Array<{ id: number; name: string }>>([])

const dateRange = ref<string[]>([])
const queryParams = reactive<QueryParams>({
  clubId: undefined,
  status: '',
  startDate: undefined,
  endDate: undefined
})

const trendChartRef = ref<HTMLElement | null>(null)
let trendChart: echarts.ECharts | null = null

const loadData = async () => {
  loading.value = true
  try {
    const params: QueryParams = { ...queryParams }
    if (dateRange.value?.length === 2) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }
    const data = await volunteerApi.getStatsSummary(params)
    summary.value = data as unknown as VolunteerStatsSummary
    renderTrendChart()
  } catch (err) {
    console.error('Failed to load summary:', err)
  } finally {
    loading.value = false
  }
}

const loadRecords = async () => {
  recordsLoading.value = true
  try {
    const params: QueryParams = { ...queryParams }
    if (dateRange.value?.length === 2) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }
    const data = await volunteerApi.getAllRecords(params)
    allRecords.value = data as unknown as VolunteerRecord[]
  } catch (err) {
    console.error('Failed to load records:', err)
  } finally {
    recordsLoading.value = false
  }
}

const loadRanking = async () => {
  rankingLoading.value = true
  try {
    const data = await volunteerApi.getRanking()
    rankingList.value = data as unknown as VolunteerStat[]
  } catch (err) {
    console.error('Failed to load ranking:', err)
  } finally {
    rankingLoading.value = false
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

const resetFilters = () => {
  dateRange.value = []
  queryParams.clubId = undefined
  queryParams.status = ''
  queryParams.startDate = undefined
  queryParams.endDate = undefined
  loadData()
  loadRecords()
}

const exportRecords = () => {
  const params: QueryParams = { ...queryParams }
  if (dateRange.value?.length === 2) {
    params.startDate = dateRange.value[0]
    params.endDate = dateRange.value[1]
  }
  volunteerApi.exportRecords(params)
}

const exportStats = () => {
  volunteerApi.exportStats()
}

const renderTrendChart = () => {
  if (!trendChartRef.value || !summary.value) return

  if (!trendChart) {
    trendChart = echarts.init(trendChartRef.value)
  }

  const trend = summary.value.trend || []
  trendChart.setOption({
    tooltip: {
      trigger: 'axis',
      formatter: '{b}<br/>服务时长: {c} 小时'
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: trend.map(t => t.date.slice(5)),
      axisLabel: {
        rotate: 45,
        fontSize: 11
      }
    },
    yAxis: {
      type: 'value',
      name: '小时'
    },
    series: [{
      name: '服务时长',
      type: 'line',
      smooth: true,
      symbol: 'circle',
      symbolSize: 8,
      itemStyle: {
        color: '#409eff'
      },
      lineStyle: {
        width: 3,
        color: '#409eff'
      },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
          { offset: 1, color: 'rgba(64, 158, 255, 0.05)' }
        ])
      },
      data: trend.map(t => t.hours)
    }]
  })
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

const handleResize = () => {
  trendChart?.resize()
}

watch(dateRange, () => {
  if (dateRange.value?.length === 2) {
    queryParams.startDate = dateRange.value[0]
    queryParams.endDate = dateRange.value[1]
  }
})

onMounted(() => {
  loadData()
  loadRecords()
  loadRanking()
  loadClubs()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  trendChart?.dispose()
  window.removeEventListener('resize', handleResize)
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
.filter-card {
  padding: 16px 24px;
}
.stats-row {
  margin-top: 20px;
}
.stat-card {
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
}
.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: #fff;
}
.stat-icon.primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.stat-icon.success {
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
}
.stat-icon.warning {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}
.stat-label {
  font-size: 14px;
  color: #606266;
  margin-bottom: 6px;
}
.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
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
.card-header h3 {
  margin: 0;
  font-size: 16px;
  color: #303133;
}
:deep(.el-table) {
  --el-table-bg-color: transparent;
  --el-table-tr-bg-color: transparent;
  --el-table-header-bg-color: rgba(0,0,0,0.02);
}
</style>
