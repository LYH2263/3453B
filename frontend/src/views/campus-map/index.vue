<template>
  <div class="campus-map-page">
    <el-row :gutter="20">
      <el-col :span="24">
        <div class="glass-card page-header">
          <div class="header-left">
            <h2>校园据点地图</h2>
            <p class="subtitle">查看各社团线下据点分布与详情</p>
          </div>
          <div class="header-right" v-if="canManage">
            <el-button type="primary" @click="openCreateDialog">
              <el-icon><Plus /></el-icon>
              <span>新增据点</span>
            </el-button>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="mt-20">
      <el-col :span="8">
        <div class="glass-card sidebar-panel">
          <div class="search-section">
            <el-input
              v-model="searchKeyword"
              placeholder="按社团名称搜索..."
              clearable
              @input="debouncedSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </div>

          <div class="filter-section">
            <div class="filter-label">按楼栋筛选</div>
            <div class="building-tags">
              <el-tag
                v-for="b in buildingOptions"
                :key="b"
                :type="selectedBuilding === b ? 'primary' : 'info'"
                :effect="selectedBuilding === b ? 'dark' : 'plain'"
                class="building-tag"
                @click="selectBuilding(b)"
              >
                {{ b }}
              </el-tag>
              <el-tag
                v-if="selectedBuilding"
                type="danger"
                effect="plain"
                class="building-tag"
                @click="selectBuilding('')"
              >
                清除筛选
              </el-tag>
            </div>
          </div>

          <div class="location-list">
            <div class="list-header">
              <span>据点列表</span>
              <span class="count">{{ filteredLocations.length }} 个</span>
            </div>
            <el-scrollbar height="calc(100vh - 420px)">
              <div
                v-for="loc in filteredLocations"
                :key="loc.id"
                class="location-item"
                :class="{ active: selectedLocation?.id === loc.id }"
                @click="selectLocation(loc)"
              >
                <div class="location-item-left">
                  <div class="building-badge" :class="getBuildingClass(loc.building)">
                    {{ loc.building.substring(0, 2) }}
                  </div>
                </div>
                <div class="location-item-content">
                  <div class="location-title">
                    <el-icon><LocationFilled /></el-icon>
                    <span>{{ loc.building }}{{ loc.floor || '' }}{{ loc.room || '' }}</span>
                  </div>
                  <div class="location-club" v-if="loc.club">
                    <el-avatar :size="20" :src="loc.club.logo" style="margin-right:6px" />
                    <span>{{ loc.club.name }}</span>
                  </div>
                  <div class="location-hours" v-if="loc.openHours">
                    <el-icon><Clock /></el-icon>
                    <span>{{ loc.openHours }}</span>
                  </div>
                </div>
                <div class="location-item-right">
                  <el-icon><ArrowRight /></el-icon>
                </div>
              </div>
              <el-empty v-if="filteredLocations.length === 0" description="暂无符合条件的据点" />
            </el-scrollbar>
          </div>
        </div>
      </el-col>

      <el-col :span="16">
        <div class="glass-card map-panel">
          <div class="card-header">
            <h3>
              <el-icon><MapLocation /></el-icon>
              <span>校园平面图</span>
            </h3>
            <div class="building-legend">
              <span
                v-for="b in buildingOptions"
                :key="b"
                class="legend-item"
                @click="selectBuilding(b)"
              >
                <span class="legend-dot" :class="getBuildingClass(b)"></span>
                <span>{{ b }}</span>
              </span>
            </div>
          </div>

          <div class="campus-map">
            <div class="map-title">校园平面示意图</div>
            <div class="map-container">
              <div class="building-area building-tech" :class="{ highlighted: selectedBuilding === '科技楼' || selectedBuilding === '' }" @click="selectBuilding('科技楼')">
                <div class="building-label">科技楼</div>
                <div class="floor-markers">
                  <div class="marker pos-1" :class="{ active: isLocationInArea(1, 1) }" @click.stop="showLocationByArea(1, 1)">102</div>
                  <div class="marker pos-2" :class="{ active: isLocationInArea(1, 3) }" @click.stop="showLocationByArea(1, 3)">305</div>
                </div>
              </div>
              <div class="building-area building-art" :class="{ highlighted: selectedBuilding === '艺术楼' || selectedBuilding === '' }" @click="selectBuilding('艺术楼')">
                <div class="building-label">艺术楼</div>
                <div class="floor-markers">
                  <div class="marker pos-3" :class="{ active: isLocationInArea(2, 1) }" @click.stop="showLocationByArea(2, 1)">102</div>
                  <div class="marker pos-4" :class="{ active: isLocationInArea(2, 2) }" @click.stop="showLocationByArea(2, 2)">201</div>
                  <div class="marker pos-5" :class="{ active: isLocationInArea(2, 3) }" @click.stop="showLocationByArea(2, 3)">302</div>
                </div>
              </div>
              <div class="campus-road-h"></div>
              <div class="campus-road-v"></div>
              <div class="garden-area">
                <el-icon><Reading /></el-icon>
                <span>图书馆</span>
              </div>
              <div class="garden-area" style="right: 0; background: rgba(67, 160, 71, 0.1);">
                <el-icon><Food /></el-icon>
                <span>食堂</span>
              </div>
              <div class="compass">
                <div class="compass-n">N</div>
                <div class="compass-ring"></div>
              </div>
            </div>
          </div>
        </div>

        <div class="glass-card detail-panel mt-20" v-if="selectedLocation">
          <div class="card-header detail-header">
            <div>
              <h3>
                <el-icon><Document /></el-icon>
                <span>据点详情</span>
              </h3>
            </div>
            <div v-if="canManage" class="detail-actions">
              <el-button size="small" type="primary" @click="openEditDialog(selectedLocation)">
                <el-icon><Edit /></el-icon>
                <span>编辑</span>
              </el-button>
              <el-button size="small" type="danger" @click="handleDelete(selectedLocation.id)">
                <el-icon><Delete /></el-icon>
                <span>删除</span>
              </el-button>
            </div>
          </div>

          <div class="detail-content">
            <div class="detail-row">
              <div class="detail-label">
                <el-icon><LocationFilled /></el-icon>
                <span>位置信息</span>
              </div>
              <div class="detail-value">
                <el-tag type="primary" effect="dark" size="large" class="building-tag-lg">
                  {{ selectedLocation.building }}
                </el-tag>
                <span v-if="selectedLocation.floor" class="detail-sub"> · {{ selectedLocation.floor }}</span>
                <span v-if="selectedLocation.room" class="detail-sub"> · {{ selectedLocation.room }}</span>
              </div>
            </div>

            <div class="detail-row" v-if="selectedLocation.club">
              <div class="detail-label">
                <el-icon><Collection /></el-icon>
                <span>所属社团</span>
              </div>
              <div class="detail-value">
                <div class="club-info">
                  <el-avatar :size="48" :src="selectedLocation.club.logo" />
                  <div class="club-text">
                    <div class="club-name">{{ selectedLocation.club.name }}</div>
                    <div class="club-desc">{{ selectedLocation.club.description }}</div>
                  </div>
                </div>
              </div>
            </div>

            <div class="detail-row" v-if="selectedLocation.openHours">
              <div class="detail-label">
                <el-icon><Clock /></el-icon>
                <span>开放时间</span>
              </div>
              <div class="detail-value">
                <el-tag type="success" effect="light">{{ selectedLocation.openHours }}</el-tag>
              </div>
            </div>

            <div class="detail-row" v-if="selectedLocation.description">
              <div class="detail-label">
                <el-icon><InfoFilled /></el-icon>
                <span>据点描述</span>
              </div>
              <div class="detail-value desc-text">
                {{ selectedLocation.description }}
              </div>
            </div>

            <div class="detail-row" v-if="selectedLocation.longitude && selectedLocation.latitude">
              <div class="detail-label">
                <el-icon><Aim /></el-icon>
                <span>坐标信息</span>
              </div>
              <div class="detail-value">
                <span class="coord">经度: {{ selectedLocation.longitude }}</span>
                <span class="coord" style="margin-left:20px">纬度: {{ selectedLocation.latitude }}</span>
              </div>
            </div>
          </div>
        </div>

        <div class="glass-card detail-panel mt-20" v-else>
          <el-empty description="请从左侧列表或平面图中选择据点查看详情">
            <template #image>
              <el-icon :size="80" color="#c0c4cc"><MapLocation /></el-icon>
            </template>
          </el-empty>
        </div>
      </el-col>
    </el-row>

    <el-dialog
      v-model="formDialogVisible"
      :title="isEditing ? '编辑据点' : '新增据点'"
      width="600px"
      @close="resetForm"
    >
      <el-form :model="formData" :rules="formRules" ref="formRef" label-width="100px">
        <el-form-item label="所属社团" prop="clubId">
          <el-select v-model="formData.clubId" placeholder="选择社团" style="width: 100%">
            <el-option
              v-for="c in clubList"
              :key="c.id"
              :label="c.name"
              :value="c.id"
            >
              <el-avatar :size="20" :src="c.logo" style="margin-right:8px" />
              {{ c.name }}
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="楼栋名称" prop="building">
          <el-select v-model="formData.building" placeholder="选择或输入楼栋" allow-create filterable style="width: 100%">
            <el-option v-for="b in buildingOptions" :key="b" :label="b" :value="b" />
          </el-select>
        </el-form-item>
        <el-form-item label="楼层">
          <el-input v-model="formData.floor" placeholder="如：1层、B1层" />
        </el-form-item>
        <el-form-item label="房间号">
          <el-input v-model="formData.room" placeholder="如：102室、会议室A" />
        </el-form-item>
        <el-form-item label="开放时间">
          <el-input v-model="formData.openHours" placeholder="如：周一至周五 09:00-18:00" />
        </el-form-item>
        <el-form-item label="据点描述">
          <el-input v-model="formData.description" type="textarea" :rows="3" placeholder="描述据点设施、用途等" />
        </el-form-item>
        <el-form-item label="经度">
          <el-input v-model.number="formData.longitude" type="number" placeholder="可选，如：116.407395" />
        </el-form-item>
        <el-form-item label="纬度">
          <el-input v-model.number="formData.latitude" type="number" placeholder="可选，如：39.904211" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="formSubmitting" @click="submitForm">确认提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  Search, Plus, LocationFilled, Clock, ArrowRight, MapLocation,
  Document, Edit, Delete, Collection, InfoFilled, Aim,
  Reading, Food
} from '@element-plus/icons-vue'
import clubLocationApi, { type ClubLocation } from '../../api/clubLocations'
import request from '../../utils/request'
import { useUserStore } from '../../store/user'

interface Club {
  id: number
  name: string
  logo?: string
  description?: string
}

const userStore = useUserStore()
const canManage = computed(() =>
  ['ADMIN', 'UNION_ADMIN', 'CLUB_LEADER'].includes(userStore.role || '')
)

const loading = ref(false)
const locations = ref<ClubLocation[]>([])
const buildingOptions = ref<string[]>([])
const clubList = ref<Club[]>([])
const searchKeyword = ref('')
const selectedBuilding = ref('')
const selectedLocation = ref<ClubLocation | null>(null)
let searchTimer: number | null = null

const formDialogVisible = ref(false)
const isEditing = ref(false)
const formSubmitting = ref(false)
const formRef = ref<FormInstance>()
const formData = ref<Partial<ClubLocation>>({
  clubId: undefined,
  building: '',
  floor: '',
  room: '',
  description: '',
  longitude: undefined,
  latitude: undefined,
  openHours: ''
})

const formRules: FormRules = {
  clubId: [{ required: true, message: '请选择所属社团', trigger: 'change' }],
  building: [{ required: true, message: '请选择或输入楼栋名称', trigger: 'change' }]
}

const filteredLocations = computed(() => {
  let result = locations.value
  if (selectedBuilding.value) {
    result = result.filter(loc => loc.building === selectedBuilding.value)
  }
  if (searchKeyword.value.trim()) {
    const keyword = searchKeyword.value.trim().toLowerCase()
    result = result.filter(loc =>
      loc.club?.name?.toLowerCase().includes(keyword) ||
      loc.building.toLowerCase().includes(keyword) ||
      loc.room?.toLowerCase().includes(keyword)
    )
  }
  return result
})

const getBuildingClass = (building: string) => {
  const map: Record<string, string> = {
    '科技楼': 'bld-tech',
    '艺术楼': 'bld-art'
  }
  return map[building] || 'bld-default'
}

const buildingToAreaId = (building: string) => {
  if (building === '科技楼') return 1
  if (building === '艺术楼') return 2
  return 0
}

const floorToLevel = (floor?: string) => {
  if (!floor) return 0
  const match = floor.match(/\d+/)
  return match ? parseInt(match[0]) : 0
}

const isLocationInArea = (buildingId: number, floorLevel: number) => {
  return filteredLocations.value.some(loc =>
    buildingToAreaId(loc.building) === buildingId &&
    floorToLevel(loc.floor) === floorLevel
  )
}

const showLocationByArea = (buildingId: number, floorLevel: number) => {
  const loc = filteredLocations.value.find(l =>
    buildingToAreaId(l.building) === buildingId &&
    floorToLevel(l.floor) === floorLevel
  )
  if (loc) selectLocation(loc)
}

const selectBuilding = (building: string) => {
  selectedBuilding.value = building
  if (building) {
    const loc = filteredLocations.value.find(l => l.building === building)
    if (loc) selectLocation(loc)
  }
}

const selectLocation = (loc: ClubLocation) => {
  selectedLocation.value = loc
}

const debouncedSearch = () => {
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = window.setTimeout(loadLocations, 300)
}

const loadLocations = async () => {
  loading.value = true
  try {
    const params: { clubName?: string; building?: string } = {}
    if (searchKeyword.value.trim()) params.clubName = searchKeyword.value.trim()
    if (selectedBuilding.value) params.building = selectedBuilding.value
    const data = await clubLocationApi.getLocations(params)
    locations.value = data as unknown as ClubLocation[]
  } catch (err) {
    console.error('Failed to load locations:', err)
  } finally {
    loading.value = false
  }
}

const loadBuildings = async () => {
  try {
    const data = await clubLocationApi.getBuildings()
    buildingOptions.value = data as unknown as string[]
  } catch (err) {
    console.error('Failed to load buildings:', err)
  }
}

const loadClubs = async () => {
  try {
    const res = await request.get<any>('/clubs', { params: { pageNum: 1, pageSize: 100 } })
    const result = res as any
    clubList.value = (result?.records || result?.data?.records || []).map((r: any) => ({
      id: r.id,
      name: r.name,
      logo: r.logo,
      description: r.description
    }))
  } catch (err) {
    console.error('Failed to load clubs:', err)
  }
}

const openCreateDialog = () => {
  isEditing.value = false
  formData.value = {
    clubId: undefined,
    building: buildingOptions.value[0] || '',
    floor: '',
    room: '',
    description: '',
    longitude: undefined,
    latitude: undefined,
    openHours: ''
  }
  formDialogVisible.value = true
}

const openEditDialog = (loc: ClubLocation) => {
  isEditing.value = true
  formData.value = { ...loc }
  formDialogVisible.value = true
}

const resetForm = () => {
  formRef.value?.resetFields()
}

const submitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    formSubmitting.value = true
    try {
      if (isEditing.value) {
        await clubLocationApi.updateLocation(formData.value as any)
        ElMessage.success('据点更新成功')
      } else {
        await clubLocationApi.createLocation(formData.value as any)
        ElMessage.success('据点创建成功')
      }
      formDialogVisible.value = false
      loadLocations()
      loadBuildings()
    } catch (err: any) {
      ElMessage.error(err?.message || '操作失败')
    } finally {
      formSubmitting.value = false
    }
  })
}

const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm('确认要删除该据点吗？', '删除确认', {
      type: 'warning',
      confirmButtonText: '确认删除',
      cancelButtonText: '取消'
    })
    await clubLocationApi.deleteLocation(id)
    ElMessage.success('删除成功')
    if (selectedLocation.value?.id === id) {
      selectedLocation.value = null
    }
    loadLocations()
    loadBuildings()
  } catch {
    /* cancel */
  }
}

onMounted(() => {
  loadBuildings()
  loadLocations()
  loadClubs()
})
</script>

<style scoped>
.campus-map-page { padding: 0; }
.page-header {
  padding: 24px 30px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.page-header h2 { margin: 0; font-size: 22px; color: #303133; }
.subtitle { margin: 6px 0 0 0; color: #909399; font-size: 14px; }
.mt-20 { margin-top: 20px; }

.sidebar-panel { padding: 20px; height: calc(100vh - 180px); display: flex; flex-direction: column; }
.search-section { margin-bottom: 16px; }

.filter-section { margin-bottom: 16px; padding-bottom: 16px; border-bottom: 1px solid rgba(0,0,0,0.05); }
.filter-label { font-size: 13px; color: #909399; margin-bottom: 10px; }
.building-tags { display: flex; flex-wrap: wrap; gap: 8px; }
.building-tag { cursor: pointer; transition: all 0.2s; }

.location-list { flex: 1; display: flex; flex-direction: column; overflow: hidden; }
.list-header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 10px 4px; font-weight: 600; color: #303133;
  margin-bottom: 8px;
}
.list-header .count { font-size: 12px; color: #909399; font-weight: normal; }

.location-item {
  display: flex; align-items: center; padding: 12px;
  border-radius: 8px; cursor: pointer; transition: all 0.2s;
  margin-bottom: 8px; border: 1px solid transparent;
}
.location-item:hover { background: rgba(64, 158, 255, 0.05); }
.location-item.active {
  background: linear-gradient(135deg, rgba(64,158,255,0.1), rgba(103,194,58,0.08));
  border-color: rgba(64,158,255,0.3);
}
.location-item-left { margin-right: 12px; }
.building-badge {
  width: 44px; height: 44px; border-radius: 10px;
  display: flex; align-items: center; justify-content: center;
  font-size: 13px; font-weight: 700; color: white;
}
.building-badge.bld-tech { background: linear-gradient(135deg, #409eff, #5dade2); }
.building-badge.bld-art { background: linear-gradient(135deg, #e74c3c, #f39c12); }
.building-badge.bld-default { background: linear-gradient(135deg, #909399, #c0c4cc); }

.location-item-content { flex: 1; min-width: 0; }
.location-title {
  display: flex; align-items: center; gap: 4px;
  font-weight: 600; color: #303133; font-size: 14px;
  margin-bottom: 4px;
}
.location-club {
  display: flex; align-items: center;
  font-size: 13px; color: #606266; margin-bottom: 3px;
}
.location-hours {
  display: flex; align-items: center; gap: 4px;
  font-size: 12px; color: #909399;
}
.location-item-right { color: #c0c4cc; }

.map-panel { padding: 0; }
.card-header {
  padding: 20px 24px;
  border-bottom: 1px solid rgba(0,0,0,0.05);
  display: flex; justify-content: space-between; align-items: center;
}
.card-header h3 {
  margin: 0; font-size: 16px; color: #303133;
  display: flex; align-items: center; gap: 6px;
}
.building-legend { display: flex; gap: 16px; }
.legend-item {
  display: flex; align-items: center; gap: 6px;
  font-size: 13px; color: #606266; cursor: pointer;
  transition: color 0.2s;
}
.legend-item:hover { color: #409eff; }
.legend-dot {
  width: 10px; height: 10px; border-radius: 50%;
}
.legend-dot.bld-tech { background: #409eff; }
.legend-dot.bld-art { background: #e74c3c; }

.campus-map { padding: 24px; }
.map-title {
  text-align: center; font-size: 14px; color: #909399;
  margin-bottom: 16px; letter-spacing: 2px;
}
.map-container {
  position: relative;
  background: linear-gradient(135deg, rgba(232, 245, 233, 0.4), rgba(227, 242, 253, 0.4));
  border: 2px dashed rgba(0,0,0,0.08);
  border-radius: 12px;
  height: 480px;
  overflow: hidden;
}

.building-area {
  position: absolute;
  border-radius: 10px;
  display: flex; flex-direction: column;
  justify-content: space-between;
  cursor: pointer;
  transition: all 0.3s;
  overflow: hidden;
}
.building-area.highlighted { opacity: 1; }
.building-area:not(.highlighted) { opacity: 0.35; filter: grayscale(40%); }
.building-area:hover { transform: translateY(-2px); box-shadow: 0 8px 24px rgba(0,0,0,0.1); }

.building-area.building-tech {
  left: 8%; top: 15%; width: 38%; height: 60%;
  background: linear-gradient(135deg, rgba(64, 158, 255, 0.15), rgba(64, 158, 255, 0.25));
  border: 2px solid rgba(64, 158, 255, 0.4);
}
.building-area.building-art {
  right: 8%; top: 15%; width: 38%; height: 60%;
  background: linear-gradient(135deg, rgba(231, 76, 60, 0.15), rgba(243, 156, 18, 0.15));
  border: 2px solid rgba(231, 76, 60, 0.4);
}

.building-label {
  padding: 12px 16px;
  font-size: 16px; font-weight: 700;
  letter-spacing: 4px;
  color: rgba(0,0,0,0.7);
  text-align: center;
  background: rgba(255,255,255,0.3);
  border-bottom: 1px dashed rgba(0,0,0,0.1);
}
.building-tech .building-label { color: rgba(64, 158, 255, 0.9); }
.building-art .building-label { color: rgba(231, 76, 60, 0.9); }

.floor-markers {
  position: relative; flex: 1;
  padding: 12px;
}
.marker {
  position: absolute;
  padding: 6px 10px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
  background: rgba(255,255,255,0.7);
  color: #606266;
  cursor: pointer;
  border: 1px solid rgba(0,0,0,0.08);
  transition: all 0.2s;
}
.marker:hover { transform: scale(1.05); }
.marker.active {
  background: linear-gradient(135deg, #67c23a, #85ce61);
  color: white;
  border-color: #67c23a;
  box-shadow: 0 4px 12px rgba(103, 194, 58, 0.4);
  animation: pulse-glow 2s infinite;
}
@keyframes pulse-glow {
  0%, 100% { box-shadow: 0 4px 12px rgba(103, 194, 58, 0.4); }
  50% { box-shadow: 0 4px 20px rgba(103, 194, 58, 0.6); }
}
.pos-1 { left: 25%; top: 15%; }
.pos-2 { left: 55%; top: 65%; }
.pos-3 { left: 15%; top: 70%; }
.pos-4 { left: 45%; top: 40%; }
.pos-5 { right: 20%; top: 15%; }

.campus-road-h {
  position: absolute;
  left: 0; right: 0; top: 50%;
  height: 18px; margin-top: -9px;
  background: linear-gradient(90deg, transparent, #a8aab0 20%, #a8aab0 80%, transparent);
  opacity: 0.35;
}
.campus-road-v {
  position: absolute;
  top: 0; bottom: 0; left: 50%;
  width: 18px; margin-left: -9px;
  background: linear-gradient(180deg, transparent, #a8aab0 20%, #a8aab0 80%, transparent);
  opacity: 0.35;
}

.garden-area {
  position: absolute;
  bottom: 6%; left: 6%;
  padding: 16px 24px;
  border-radius: 50% 50% 40% 60%;
  background: rgba(103, 194, 58, 0.12);
  color: rgba(103, 194, 58, 0.8);
  font-size: 12px;
  display: flex; align-items: center; gap: 6px;
}

.compass {
  position: absolute;
  top: 16px; right: 16px;
  width: 46px; height: 46px;
  display: flex; align-items: center; justify-content: center;
}
.compass-ring {
  position: absolute; inset: 0;
  border: 2px solid rgba(0,0,0,0.1);
  border-radius: 50%;
  border-top-color: #f56c6c;
}
.compass-n {
  position: relative; z-index: 1;
  font-weight: 700; font-size: 13px; color: #f56c6c;
}

.detail-panel { padding: 0; }
.detail-header { padding: 20px 24px; }
.detail-actions { display: flex; gap: 8px; }

.detail-content { padding: 20px 24px; }
.detail-row {
  display: flex; padding: 14px 0;
  border-bottom: 1px solid rgba(0,0,0,0.04);
}
.detail-row:last-child { border-bottom: none; }
.detail-label {
  width: 120px; flex-shrink: 0;
  display: flex; align-items: center; gap: 6px;
  font-weight: 500; color: #606266;
}
.detail-value { flex: 1; color: #303133; }
.building-tag-lg { margin-right: 8px; }
.detail-sub { color: #606266; font-size: 14px; }

.club-info {
  display: flex; align-items: center; gap: 14px;
}
.club-text .club-name {
  font-weight: 600; font-size: 15px; color: #303133;
  margin-bottom: 4px;
}
.club-text .club-desc {
  font-size: 13px; color: #909399;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.desc-text {
  padding: 12px 14px;
  background: rgba(0,0,0,0.02);
  border-radius: 8px;
  line-height: 1.7;
  color: #606266;
}

.coord {
  font-family: 'Consolas', monospace;
  font-size: 13px;
  color: #606266;
  padding: 4px 10px;
  background: rgba(0,0,0,0.03);
  border-radius: 4px;
}
</style>
