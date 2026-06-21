import request from '../utils/request'

export interface Partner {
  id: number
  clubId: number
  clubName?: string
  partnerName: string
  type: 'ENTERPRISE' | 'SCHOOL_ORG' | 'OTHER'
  contactName?: string
  contactPhone?: string
  contractStart?: string
  contractEnd?: string
  contractUrl?: string
  remark?: string
  status: 'ACTIVE' | 'EXPIRED'
  createTime?: string
  daysToExpire?: number
}

export interface PartnerCreateRequest {
  clubId: number
  partnerName: string
  type: 'ENTERPRISE' | 'SCHOOL_ORG' | 'OTHER'
  contactName?: string
  contactPhone?: string
  contractStart?: string
  contractEnd?: string
  contractUrl?: string
  remark?: string
}

export interface PartnerUpdateRequest {
  clubId?: number
  partnerName?: string
  type?: 'ENTERPRISE' | 'SCHOOL_ORG' | 'OTHER'
  contactName?: string
  contactPhone?: string
  contractStart?: string
  contractEnd?: string
  contractUrl?: string
  remark?: string
  status?: 'ACTIVE' | 'EXPIRED'
}

export interface PartnerQueryParams {
  clubId?: number
  type?: string
  status?: string
  keyword?: string
  expiringSoon?: boolean
  contractStartFrom?: string
  contractStartTo?: string
  contractEndFrom?: string
  contractEndTo?: string
}

export interface PartnerStats {
  total: number
  activeCount: number
  expiredCount: number
  expiringIn30Days: number
  expiringIn7Days: number
  typeStats: Record<string, number>
  clubStats: Array<{
    clubId: number
    clubName: string
    count: number
  }>
}

const partnerApi = {
  list: (params?: PartnerQueryParams) =>
    request.get<Partner[]>('/partners', { params }),

  getById: (id: number) =>
    request.get<Partner>(`/partners/${id}`),

  create: (data: PartnerCreateRequest) =>
    request.post('/partners', data),

  update: (id: number, data: PartnerUpdateRequest) =>
    request.put(`/partners/${id}`, data),

  delete: (id: number) =>
    request.delete(`/partners/${id}`),

  getStats: () =>
    request.get<PartnerStats>('/partners/stats/summary'),

  export: (params?: PartnerQueryParams) => {
    const queryParams: Record<string, any> = { ...params }
    const query = new URLSearchParams(queryParams as any).toString()
    window.open(`/api/partners/export${query ? '?' + query : ''}`, '_blank')
  }
}

export default partnerApi
