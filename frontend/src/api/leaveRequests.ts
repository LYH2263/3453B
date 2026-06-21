import request from '../utils/request'

export interface LeaveRequest {
  id: number
  activityId: number
  userId: number
  reason: string
  status: 'PENDING' | 'APPROVED' | 'REJECTED'
  approverId?: number
  clubId: number
  rejectReason?: string
  createTime: string
  updateTime: string
  activityTitle?: string
  userName?: string
  approverName?: string
}

export interface SubmitLeaveRequest {
  activityId: number
  reason: string
}

const leaveRequestApi = {
  submitLeave: (data: SubmitLeaveRequest) =>
    request.post('/leave-requests', data),

  getMyLeaveRequests: () =>
    request.get<LeaveRequest[]>('/leave-requests/my'),

  getPendingRequests: (clubId: number) =>
    request.get<LeaveRequest[]>('/leave-requests/pending', { params: { clubId } }),

  getAllRequests: (clubId: number) =>
    request.get<LeaveRequest[]>('/leave-requests/all', { params: { clubId } }),

  approveLeave: (id: number) =>
    request.post(`/leave-requests/${id}/approve`),

  rejectLeave: (id: number, rejectReason: string) =>
    request.post(`/leave-requests/${id}/reject`, { rejectReason })
}

export default leaveRequestApi
