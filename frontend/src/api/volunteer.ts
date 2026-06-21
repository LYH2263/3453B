import request from '../utils/request'

export interface VolunteerRecord {
  id: number
  studentId: number
  activityName: string
  serviceDate: string
  hours: number
  proofUrl: string
  status: 'PENDING' | 'APPROVED' | 'REJECTED'
  clubId: number
  rejectReason?: string
  createTime: string
  studentName: string
  studentNo: string
  clubName: string
  auditorName?: string
}

export interface VolunteerSubmitRequest {
  activityName: string
  serviceDate: string
  hours: number
  proofUrl?: string
  clubId?: number
}

export interface VolunteerAuditRequest {
  status: 'APPROVED' | 'REJECTED'
  rejectReason?: string
}

export interface VolunteerStat {
  userId: number
  userName: string
  studentId: string
  totalHours: number
  approvedCount: number
  pendingCount: number
  rejectedCount: number
  lastServiceDate?: string
}

export interface VolunteerStatsSummary {
  totalRecords: number
  approvedRecords: number
  pendingRecords: number
  rejectedRecords: number
  totalHours: number
  participantCount: number
  clubStats: Array<{
    clubId: number
    clubName: string
    totalHours: number
    recordCount: number
  }>
  trend: Array<{
    date: string
    hours: number
  }>
}

export interface QueryParams {
  clubId?: number
  startDate?: string
  endDate?: string
  status?: string
  userId?: number
}

const volunteerApi = {
  submitRecord: (data: VolunteerSubmitRequest) =>
    request.post('/volunteer', data),

  getMyRecords: () =>
    request.get<VolunteerRecord[]>('/volunteer/my'),

  getPendingAudits: () =>
    request.get<VolunteerRecord[]>('/volunteer/pending'),

  auditRecord: (id: number, data: VolunteerAuditRequest) =>
    request.post(`/volunteer/${id}/audit`, data),

  getAllRecords: (params?: QueryParams) =>
    request.get<VolunteerRecord[]>('/volunteer/records', { params }),

  getStatsSummary: (params?: QueryParams) =>
    request.get<VolunteerStatsSummary>('/volunteer/stats/summary', { params }),

  getMyStats: () =>
    request.get<VolunteerStat>('/volunteer/stats/my'),

  getUserStats: (userId: number) =>
    request.get<VolunteerStat>(`/volunteer/stats/user/${userId}`),

  getRanking: () =>
    request.get<VolunteerStat[]>('/volunteer/stats/ranking'),

  exportRecords: (params?: QueryParams) => {
    const query = params ? new URLSearchParams(params as any).toString() : ''
    window.open(`/api/volunteer/export/records${query ? '?' + query : ''}`, '_blank')
  },

  exportStats: () => {
    window.open('/api/volunteer/export/stats', '_blank')
  }
}

export default volunteerApi
