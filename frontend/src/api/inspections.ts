import request from '../utils/request'

export interface Device {
  id: number
  clubId: number
  clubName?: string
  name: string
  deviceNo: string
  location: string
  createTime?: string
  updateTime?: string
}

export interface InspectionPlan {
  id: number
  deviceId: number
  deviceName?: string
  deviceNo?: string
  deviceLocation?: string
  clubId?: number
  clubName?: string
  cycleDays?: number
  cronExpr?: string
  ownerId: number
  ownerName?: string
  inspectorId: number
  inspectorName?: string
  lastInspectTime?: string
  nextInspectDate?: string
  status: 'ACTIVE' | 'INACTIVE'
  isOverdue?: boolean
  createTime?: string
}

export interface InspectionRecord {
  id: number
  planId: number
  deviceName?: string
  deviceNo?: string
  deviceLocation?: string
  clubId?: number
  result: 'NORMAL' | 'ABNORMAL'
  remark?: string
  inspectTime: string
  inspectorId: number
  inspectorName?: string
  createTime?: string
}

export interface InspectionStats {
  deviceCount: number
  activePlanCount: number
  overdueCount: number
  todayCount: number
  abnormalCount: number
  totalRecordCount: number
}

export interface PlanCreateRequest {
  deviceId: number
  cycleDays?: number
  cronExpr?: string
  inspectorId: number
  nextInspectDate?: string
}

export interface PlanUpdateRequest {
  id: number
  cycleDays?: number
  cronExpr?: string
  inspectorId?: number
  nextInspectDate?: string
  status?: 'ACTIVE' | 'INACTIVE'
}

export interface RecordSubmitRequest {
  planId: number
  result: 'NORMAL' | 'ABNORMAL'
  remark?: string
  inspectTime: string
}

const inspectionApi = {
  getStats: () =>
    request.get<InspectionStats>('/inspections/stats'),

  getDevices: () =>
    request.get<Device[]>('/inspections/devices'),

  getDeviceById: (id: number) =>
    request.get<Device>(`/inspections/devices/${id}`),

  createDevice: (data: Omit<Device, 'id' | 'createTime' | 'updateTime' | 'clubName'>) =>
    request.post('/inspections/devices', data),

  updateDevice: (data: Device) =>
    request.put('/inspections/devices', data),

  deleteDevice: (id: number) =>
    request.delete(`/inspections/devices/${id}`),

  getPlans: () =>
    request.get<InspectionPlan[]>('/inspections/plans'),

  getPlanById: (id: number) =>
    request.get<InspectionPlan>(`/inspections/plans/${id}`),

  createPlan: (data: PlanCreateRequest) =>
    request.post('/inspections/plans', data),

  updatePlan: (data: PlanUpdateRequest) =>
    request.put('/inspections/plans', data),

  deletePlan: (id: number) =>
    request.delete(`/inspections/plans/${id}`),

  getMyTodoPlans: () =>
    request.get<InspectionPlan[]>('/inspections/plans/my-todo'),

  submitRecord: (data: RecordSubmitRequest) =>
    request.post('/inspections/records', data),

  getMyRecords: () =>
    request.get<InspectionRecord[]>('/inspections/records/my'),

  getRecordsByPlan: (planId: number) =>
    request.get<InspectionRecord[]>(`/inspections/plans/${planId}/records`)
}

export default inspectionApi
