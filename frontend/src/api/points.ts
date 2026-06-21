import request from '../utils/request'

export interface PointInfo {
  userId: number
  userName: string
  balance: number
  totalEarned: number
  totalSpent: number
}

export interface ShopItem {
  id: number
  name: string
  description: string
  imageUrl: string
  costPoints: number
  stock: number
  clubId: number | null
  status: string
  sortOrder: number
  clubName: string
  creatorName: string
  createTime: string
}

export interface PointLedger {
  id: number
  userId: number
  delta: number
  reason: string
  refType: string
  refId: number
  balanceSnapshot: number
  createTime: string
  userName?: string
  studentNo?: string
}

export interface RedeemOrder {
  id: number
  orderNo: string
  userId: number
  itemId: number
  itemName: string
  itemImage: string
  costPoints: number
  quantity: number
  status: string
  remark: string
  createTime: string
  userName?: string
  studentNo?: string
}

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

export interface PointRule {
  id: number
  ruleCode: string
  ruleName: string
  ruleJson: string
  description: string
  status: string
  createTime: string
}

const pointApi = {
  getMyPoints: () =>
    request.get<PointInfo>('/points/my'),

  getMyLedger: (params?: {
    refType?: string
    startTime?: string
    endTime?: string
    pageNum?: number
    pageSize?: number
  }) =>
    request.get<PageResult<PointLedger>>('/points/my/ledger', { params }),

  getShopItems: (params?: {
    keyword?: string
    clubId?: number
    status?: string
    pageNum?: number
    pageSize?: number
  }) =>
    request.get<PageResult<ShopItem>>('/points/shop/items', { params }),

  getItemDetail: (id: number) =>
    request.get<ShopItem>(`/points/shop/items/${id}`),

  redeemItem: (data: { itemId: number; quantity: number }) =>
    request.post('/points/shop/redeem', data),

  getMyOrders: (params?: {
    itemId?: number
    status?: string
    startTime?: string
    endTime?: string
    pageNum?: number
    pageSize?: number
  }) =>
    request.get<PageResult<RedeemOrder>>('/points/my/orders', { params }),

  getOrderDetail: (id: number) =>
    request.get<RedeemOrder>(`/points/orders/${id}`),

  getPointRules: () =>
    request.get<PointRule[]>('/points/rules'),

  getMyClubItems: (params?: {
    keyword?: string
    status?: string
    pageNum?: number
    pageSize?: number
  }) =>
    request.get<PageResult<ShopItem>>('/points/admin/items/my-club', { params }),

  createItem: (data: {
    name: string
    description?: string
    imageUrl?: string
    costPoints: number
    stock: number
    clubId?: number
    sortOrder?: number
  }) =>
    request.post('/points/admin/items', data),

  updateItem: (id: number, data: {
    name: string
    description?: string
    imageUrl?: string
    costPoints: number
    stock: number
    clubId?: number
    sortOrder?: number
  }) =>
    request.put(`/points/admin/items/${id}`, data),

  deleteItem: (id: number) =>
    request.delete(`/points/admin/items/${id}`),

  updateItemStatus: (id: number, status: string) =>
    request.put(`/points/admin/items/${id}/status`, null, { params: { status } }),

  getAllOrders: (params?: {
    userId?: number
    itemId?: number
    status?: string
    startTime?: string
    endTime?: string
    pageNum?: number
    pageSize?: number
  }) =>
    request.get<PageResult<RedeemOrder>>('/points/admin/orders', { params }),

  updateOrderStatus: (id: number, status: string) =>
    request.put(`/points/admin/orders/${id}/status`, null, { params: { status } }),

  addPoints: (data: {
    userId: number
    points: number
    reason: string
    refType?: string
    refId?: number
  }) =>
    request.post('/points/admin/add-points', data),

  getUserPoints: (userId: number) =>
    request.get<PointInfo>(`/points/admin/user/${userId}`),

  getUserLedger: (params?: {
    userId?: number
    refType?: string
    startTime?: string
    endTime?: string
    pageNum?: number
    pageSize?: number
  }) =>
    request.get<PageResult<PointLedger>>('/points/admin/ledger', { params }),

  createPointRule: (data: {
    ruleCode: string
    ruleName: string
    ruleJson: string
    description?: string
  }) =>
    request.post('/points/rules', data),

  updatePointRule: (id: number, data: {
    ruleCode: string
    ruleName: string
    ruleJson: string
    description?: string
  }) =>
    request.put(`/points/rules/${id}`, data),

  updateRuleStatus: (id: number, status: string) =>
    request.put(`/points/rules/${id}/status`, null, { params: { status } })
}

export default pointApi
