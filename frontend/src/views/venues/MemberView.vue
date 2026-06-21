<template>
  <div class="venue-page">
    <el-row :gutter="20">
      <el-col :span="24">
        <div class="glass-card page-header">
          <div class="header-left">
            <h2>场地预约 - 预约日历</h2>
            <p class="subtitle">查看各场地使用情况</p>
          </div>
          <div class="header-right">
            <el-select v-model="selectedVenueId" placeholder="选择场地" style="width: 180px" clearable @change="loadWeeklyBookings">
              <el-option v-for="venue in venues" :key="venue.id" :label="venue.name" :value="venue.id" />
            </el-select>
            <el-button-group style="margin-left: 12px">
              <el-button @click="prevWeek">
                <el-icon><ArrowLeft /></el-icon>
              </el-button>
              <el-button @click="today">今天</el-button>
              <el-button @click="nextWeek">
                <el-icon><ArrowRight /></el-icon>
              </el-button>
            </el-button-group>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="mt-20">
      <el-col :span="24">
        <div class="glass-card">
          <div class="card-header">
            <h3>{{ weekRangeText }}</h3>
            <div class="legend">
              <span class="legend-item"><span class="legend-dot approved"></span>已预约</span>
              <span class="legend-item"><span class="legend-dot pending"></span>待审批</span>
              <span class="legend-item"><span class="legend-dot rejected"></span>已驳回</span>
            </div>
          </div>
          <div class="calendar-container" v-loading="loading">
            <div class="calendar-header">
              <div class="time-header">时间</div>
              <div class="day-header" v-for="day in weekDays" :key="day.dateKey">
                <div class="day-name">{{ day.name }}</div>
                <div class="day-date" :class="{ today: day.isToday }">{{ day.dateText }}</div>
              </div>
            </div>
            <div class="calendar-body">
              <div class="time-column">
                <div class="time-slot" v-for="hour in timeSlots" :key="hour">{{ hour }}:00</div>
              </div>
              <div class="day-column" v-for="day in weekDays" :key="day.dateKey">
                <div class="day-slot" v-for="hour in timeSlots" :key="hour" :class="{ weekend: day.isWeekend }">
                  <div
                    class="booking-block"
                    v-for="booking in getBookingsAt(day.date, hour)"
                    :key="booking.id"
                    :class="getStatusClass(booking.status)"
                    :style="getBookingStyle(booking)"
                    @click="viewBookingDetail(booking)"
                  >
                    <div class="booking-title">{{ booking.venueName }}</div>
                    <div class="booking-time">{{ formatTime(booking.startTime) }} - {{ formatTime(booking.endTime) }}</div>
                    <div class="booking-club">{{ booking.clubName }}</div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-dialog v-model="detailDialogVisible" title="预约详情" width="480px">
      <el-descriptions :column="1" border v-if="currentBooking">
        <el-descriptions-item label="场地">{{ currentBooking.venueName }}</el-descriptions-item>
        <el-descriptions-item label="社团">{{ currentBooking.clubName }}</el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ formatDateTime(currentBooking.startTime) }}</el-descriptions-item>
        <el-descriptions-item label="结束时间">{{ formatDateTime(currentBooking.endTime) }}</el-descriptions-item>
        <el-descriptions-item label="使用目的">{{ currentBooking.purpose }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentBooking.status)">
            {{ getStatusText(currentBooking.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="申请人">{{ currentBooking.applicantName }}</el-descriptions-item>
        <el-descriptions-item label="审批人" v-if="currentBooking.auditorName">
          {{ currentBooking.auditorName }}
        </el-descriptions-item>
        <el-descriptions-item label="驳回原因" v-if="currentBooking.rejectReason">
          <span style="color: #f56c6c">{{ currentBooking.rejectReason }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="提交时间">{{ formatDateTime(currentBooking.createTime) }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ArrowLeft, ArrowRight } from '@element-plus/icons-vue'
import venueApi, { type Venue, type VenueBooking } from '../../api/venues'

const loading = ref(false)
const venues = ref<Venue[]>([])
const weeklyBookings = ref<VenueBooking[]>([])
const selectedVenueId = ref<number | undefined>(undefined)
const currentWeekStart = ref(new Date())
const detailDialogVisible = ref(false)
const currentBooking = ref<VenueBooking | null>(null)

const timeSlots = [8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22]

const weekDays = computed(() => {
  const startOfWeek = getStartOfWeek(currentWeekStart.value)
  const days = []
  const dayNames = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  const today = new Date()

  for (let i = 0; i < 7; i++) {
    const date = new Date(startOfWeek)
    date.setDate(startOfWeek.getDate() + i)
    days.push({
      date,
      dateKey: formatDate(date),
      name: dayNames[i],
      dateText: `${date.getMonth() + 1}/${date.getDate()}`,
      isToday: isSameDay(date, today),
      isWeekend: i === 0 || i === 6
    })
  }
  return days
})

const weekRangeText = computed(() => {
  const start = weekDays.value[0].date
  const end = weekDays.value[6].date
  return `${start.getFullYear()}年${start.getMonth() + 1}月${start.getDate()}日 - ${end.getMonth() + 1}月${end.getDate()}日`
})

const getStartOfWeek = (date: Date) => {
  const d = new Date(date)
  const day = d.getDay()
  d.setDate(d.getDate() - day)
  d.setHours(0, 0, 0, 0)
  return d
}

const isSameDay = (d1: Date, d2: Date) => {
  return d1.getFullYear() === d2.getFullYear() &&
    d1.getMonth() === d2.getMonth() &&
    d1.getDate() === d2.getDate()
}

const formatDate = (date: Date) => {
  return date.toISOString().split('T')[0]
}

const formatDateTimeForApi = (date: Date) => {
  const pad = (n: number) => n.toString().padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

const prevWeek = () => {
  const newDate = new Date(currentWeekStart.value)
  newDate.setDate(newDate.getDate() - 7)
  currentWeekStart.value = newDate
  loadWeeklyBookings()
}

const nextWeek = () => {
  const newDate = new Date(currentWeekStart.value)
  newDate.setDate(newDate.getDate() + 7)
  currentWeekStart.value = newDate
  loadWeeklyBookings()
}

const today = () => {
  currentWeekStart.value = new Date()
  loadWeeklyBookings()
}

const loadVenues = async () => {
  try {
    const data = await venueApi.getVenues()
    venues.value = data as unknown as Venue[]
  } catch (err) {
    console.error('Failed to load venues:', err)
  }
}

const loadWeeklyBookings = async () => {
  loading.value = true
  try {
    const weekStart = getStartOfWeek(currentWeekStart.value)
    const weekEnd = new Date(weekStart)
    weekEnd.setDate(weekStart.getDate() + 7)

    const data = await venueApi.getWeeklyBookings(
      formatDateTimeForApi(weekStart),
      formatDateTimeForApi(weekEnd),
      selectedVenueId.value
    )
    weeklyBookings.value = data as unknown as VenueBooking[]
  } catch (err) {
    console.error('Failed to load weekly bookings:', err)
  } finally {
    loading.value = false
  }
}

const getBookingsAt = (dayDate: Date, hour: number) => {
  const dayStr = formatDate(dayDate)
  return weeklyBookings.value.filter((booking: VenueBooking) => {
    const bookingStart = new Date(booking.startTime)
    const bookingEnd = new Date(booking.endTime)
    const bookingStartHour = bookingStart.getHours()
    const bookingEndHour = bookingEnd.getHours()
    return formatDate(bookingStart) === dayStr &&
      hour >= bookingStartHour && hour < bookingEndHour
  })
}

const getBookingStyle = (booking: VenueBooking) => {
  const startHour = new Date(booking.startTime).getHours()
  const startMin = new Date(booking.startTime).getMinutes()
  const endHour = new Date(booking.endTime).getHours()
  const endMin = new Date(booking.endTime).getMinutes()

  const duration = (endHour - startHour) + (endMin - startMin) / 60
  const top = (startMin / 60) * 100

  return {
    height: `calc(${duration * 100}% - 2px)`,
    top: `${top}%`
  }
}

const getStatusClass = (status: string) => {
  const map: Record<string, string> = {
    APPROVED: 'status-approved',
    PENDING: 'status-pending',
    REJECTED: 'status-rejected'
  }
  return map[status] || ''
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
    PENDING: '待审批',
    REJECTED: '已驳回'
  }
  return map[status] || status
}

const formatTime = (dateStr: string) => {
  const d = new Date(dateStr)
  const pad = (n: number) => n.toString().padStart(2, '0')
  return `${pad(d.getHours())}:${pad(d.getMinutes())}`
}

const formatDateTime = (dateStr: string) => {
  return dateStr ? new Date(dateStr).toLocaleString('zh-CN') : ''
}

const viewBookingDetail = (booking: VenueBooking) => {
  currentBooking.value = booking
  detailDialogVisible.value = true
}

onMounted(() => {
  loadVenues()
  loadWeeklyBookings()
})
</script>

<style scoped>
.venue-page {
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
.header-right {
  display: flex;
  align-items: center;
}
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
.legend {
  display: flex;
  gap: 20px;
}
.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #606266;
}
.legend-dot {
  width: 12px;
  height: 12px;
  border-radius: 3px;
}
.legend-dot.approved { background: #67c23a; }
.legend-dot.pending { background: #e6a23c; }
.legend-dot.rejected { background: #f56c6c; }

.calendar-container {
  padding: 20px;
}
.calendar-header {
  display: flex;
  border-bottom: 2px solid #ebeef5;
}
.time-header {
  width: 70px;
  padding: 10px;
  text-align: center;
  font-weight: 600;
  color: #606266;
  border-right: 1px solid #ebeef5;
}
.day-header {
  flex: 1;
  padding: 10px;
  text-align: center;
  border-right: 1px solid #ebeef5;
}
.day-header:last-child {
  border-right: none;
}
.day-name {
  font-size: 13px;
  color: #606266;
}
.day-date {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin-top: 4px;
}
.day-date.today {
  color: #409eff;
}
.calendar-body {
  display: flex;
  height: 600px;
  overflow-y: auto;
}
.time-column {
  width: 70px;
  flex-shrink: 0;
}
.time-slot {
  height: 40px;
  padding: 8px;
  text-align: right;
  font-size: 12px;
  color: #909399;
  border-right: 1px solid #ebeef5;
  border-bottom: 1px solid #f0f2f5;
}
.day-column {
  flex: 1;
  display: flex;
  flex-direction: column;
  border-right: 1px solid #ebeef5;
}
.day-column:last-child {
  border-right: none;
}
.day-slot {
  flex: 1;
  min-height: 40px;
  border-bottom: 1px solid #f0f2f5;
  position: relative;
  padding: 1px;
}
.day-slot.weekend {
  background: rgba(0,0,0,0.02);
}
.booking-block {
  position: absolute;
  left: 2px;
  right: 2px;
  border-radius: 4px;
  padding: 4px 6px;
  font-size: 11px;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.2s;
  z-index: 1;
}
.booking-block:hover {
  transform: scale(1.02);
  z-index: 10;
}
.booking-block.status-approved {
  background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
  color: white;
}
.booking-block.status-pending {
  background: linear-gradient(135deg, #e6a23c 0%, #f0c37b 100%);
  color: white;
}
.booking-block.status-rejected {
  background: linear-gradient(135deg, #f56c6c 0%, #f89898 100%);
  color: white;
}
.booking-title {
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.booking-time {
  font-size: 10px;
  opacity: 0.9;
  margin-top: 2px;
}
.booking-club {
  font-size: 10px;
  opacity: 0.8;
  margin-top: 1px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
