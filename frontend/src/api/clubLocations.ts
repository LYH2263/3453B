import request from '../utils/request'

export interface Club {
  id: number
  name: string
  description: string
  logo?: string
}

export interface ClubLocation {
  id: number
  clubId: number
  building: string
  floor?: string
  room?: string
  description?: string
  longitude?: number
  latitude?: number
  openHours?: string
  createTime: string
  updateTime: string
  isDeleted?: number
  club?: Club
}

const clubLocationApi = {
  getLocations: (params?: { clubName?: string; building?: string }) =>
    request.get<ClubLocation[]>('/club-locations', { params }),

  getLocationById: (id: number) =>
    request.get<ClubLocation>(`/club-locations/${id}`),

  getBuildings: () =>
    request.get<string[]>('/club-locations/buildings'),

  createLocation: (data: Omit<ClubLocation, 'id' | 'createTime' | 'updateTime' | 'club'>) =>
    request.post('/club-locations', data),

  updateLocation: (data: Omit<ClubLocation, 'createTime' | 'updateTime' | 'club'>) =>
    request.put('/club-locations', data),

  deleteLocation: (id: number) =>
    request.delete(`/club-locations/${id}`)
}

export default clubLocationApi
