import request from '../utils/request'

export interface ClubMentor {
  id: number
  clubId: number
  clubName: string
  name: string
  staffNo: string
  researchArea: string
  intro: string
  userId: number | null
  linkedUsername: string | null
  createTime: string
}

export interface MentorSlot {
  id: number
  mentorId: number
  mentorName: string
  startTime: string
  endTime: string
  status: 'AVAILABLE' | 'BOOKED' | 'CANCELLED'
  createTime: string
}

export interface MentorAppointment {
  id: number
  slotId: number
  studentId: number
  studentName: string
  mentorId: number
  mentorName: string
  startTime: string
  endTime: string
  status: 'PENDING' | 'CONFIRMED' | 'REJECTED'
  rejectReason: string | null
  createTime: string
}

export interface MentorCreateRequest {
  clubId: number
  name: string
  staffNo?: string
  researchArea?: string
  intro?: string
  userId?: number | null
}

export interface SlotCreateRequest {
  mentorId: number
  startTime: string
  endTime: string
}

export interface AppointmentCreateRequest {
  slotId: number
}

export interface AppointmentActionRequest {
  status: 'CONFIRMED' | 'REJECTED'
  rejectReason?: string
}

const mentorshipApi = {
  getMentors: (clubId?: number) =>
    request.get<ClubMentor[]>('/mentorship/mentors', { params: { clubId } }),

  getMentorById: (id: number) =>
    request.get<ClubMentor>(`/mentorship/mentors/${id}`),

  createMentor: (data: MentorCreateRequest) =>
    request.post('/mentorship/mentors', data),

  updateMentor: (data: ClubMentor) =>
    request.put('/mentorship/mentors', data),

  deleteMentor: (id: number) =>
    request.delete(`/mentorship/mentors/${id}`),

  getSlotsByMentor: (mentorId: number) =>
    request.get<MentorSlot[]>('/mentorship/slots', { params: { mentorId } }),

  getAvailableSlots: (clubId?: number) =>
    request.get<MentorSlot[]>('/mentorship/slots/available', { params: { clubId } }),

  createSlot: (data: SlotCreateRequest) =>
    request.post('/mentorship/slots', data),

  cancelSlot: (id: number) =>
    request.post(`/mentorship/slots/${id}/cancel`),

  createAppointment: (data: AppointmentCreateRequest) =>
    request.post('/mentorship/appointments', data),

  handleAppointment: (id: number, data: AppointmentActionRequest) =>
    request.post(`/mentorship/appointments/${id}/handle`, data),

  cancelAppointment: (id: number) =>
    request.post(`/mentorship/appointments/${id}/cancel`),

  getMyAppointments: () =>
    request.get<MentorAppointment[]>('/mentorship/appointments/my'),

  getPendingByMentor: (mentorId: number) =>
    request.get<MentorAppointment[]>('/mentorship/appointments/pending', { params: { mentorId } })
}

export default mentorshipApi
