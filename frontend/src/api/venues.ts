import request from '../utils/request'

export interface Venue {
  id: number
  name: string
  capacity: number
  location: string
  status: 'AVAILABLE' | 'UNAVAILABLE'
  createTime: string
  isDeleted?: number
}

export interface VenueBooking {
  id: number
  venueId: number
  venueName: string
  clubId: number
  clubName: string
  startTime: string
  endTime: string
  purpose: string
  status: 'PENDING' | 'APPROVED' | 'REJECTED'
  applicantId: number
  applicantName: string
  auditorId?: number
  auditorName?: string
  rejectReason?: string
  createTime: string
}

export interface BookingSubmitRequest {
  venueId: number
  startTime: string
  endTime: string
  purpose: string
}

export interface BookingAuditRequest {
  status: 'APPROVED' | 'REJECTED'
  rejectReason?: string
}

const venueApi = {
  getVenues: () =>
    request.get<Venue[]>('/venues'),

  getVenueById: (id: number) =>
    request.get<Venue>(`/venues/${id}`),

  createVenue: (data: Omit<Venue, 'id' | 'createTime'>) =>
    request.post('/venues', data),

  updateVenue: (data: Venue) =>
    request.put('/venues', data),

  deleteVenue: (id: number) =>
    request.delete(`/venues/${id}`),

  submitBooking: (data: BookingSubmitRequest) =>
    request.post('/venues/bookings', data),

  auditBooking: (id: number, data: BookingAuditRequest) =>
    request.post(`/venues/bookings/${id}/audit`, data),

  getMyBookings: () =>
    request.get<VenueBooking[]>('/venues/bookings/my'),

  getPendingBookings: () =>
    request.get<VenueBooking[]>('/venues/bookings/pending'),

  getAllBookings: () =>
    request.get<VenueBooking[]>('/venues/bookings/all'),

  getWeeklyBookings: (weekStart: string, weekEnd: string, venueId?: number) =>
    request.get<VenueBooking[]>('/venues/bookings/weekly', {
      params: { weekStart, weekEnd, venueId }
    }),

  checkOverlap: (venueId: number, startTime: string, endTime: string, excludeBookingId?: number) =>
    request.get<boolean>('/venues/bookings/check-overlap', {
      params: { venueId, startTime, endTime, excludeBookingId }
    })
}

export default venueApi
