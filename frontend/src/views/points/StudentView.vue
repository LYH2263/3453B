<template>
  <div class="points-page">
    <div class="glass-card stats-card">
      <div class="points-header">
        <div class="points-icon">
          <el-icon><Trophy /></el-icon>
        </div>
        <div class="points-info">
          <h2>我的积分</h2>
          <div class="points-balance">{{ myPoints.balance || 0 }}</div>
          <div class="points-detail">
            <span>累计获得 {{ myPoints.totalEarned || 0 }}</span>
            <span class="divider">|</span>
            <span>累计消费 {{ myPoints.totalSpent || 0 }}</span>
          </div>
        </div>
      </div>
    </div>

    <el-tabs v-model="activeTab" class="glass-card content-tabs">
      <el-tab-pane label="积分商城" name="shop">
        <div class="shop-section">
          <div class="shop-header">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索商品..."
              style="width: 250px"
              clearable
              @input="loadShopItems"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-select
              v-model="filterClub"
              placeholder="全部商品"
              style="width: 150px"
              @change="loadShopItems"
            >
              <el-option label="全部商品" :value="null" />
              <el-option label="全校商品" :value="0" />
              <el-option label="我的社团" :value="userStore.userInfo?.clubId" />
            </el-select>
          </div>

          <div v-loading="loadingShop" class="shop-grid">
            <div
              v-for="item in shopItems"
              :key="item.id"
              class="shop-item"
              @click="openItemDetail(item)"
            >
              <div class="item-image">
                <img v-if="item.imageUrl" :src="item.imageUrl" :alt="item.name" />
                <div v-else class="image-placeholder">
                  <el-icon><Goods /></el-icon>
                </div>
                <div class="item-tag" :class="item.clubId ? 'club-tag' : 'global-tag'">
                  {{ item.clubName }}
                </div>
              </div>
              <div class="item-info">
                <h3 class="item-name">{{ item.name }}</h3>
                <p class="item-desc">{{ item.description || '暂无描述' }}</p>
                <div class="item-footer">
                  <span class="item-points">
                    <el-icon><Coin /></el-icon>
                    {{ item.costPoints }}
                  </span>
                  <span class="item-stock">库存: {{ item.stock }}</span>
                </div>
              </div>
            </div>
          </div>

          <el-pagination
            v-if="shopItems.length > 0"
            class="pagination"
            v-model:current-page="shopPage"
            v-model:page-size="shopPageSize"
            :total="shopTotal"
            layout="prev, pager, next"
            @current-change="loadShopItems"
          />

          <el-empty v-if="!loadingShop && shopItems.length === 0" description="暂无商品" />
        </div>
      </el-tab-pane>

      <el-tab-pane label="我的订单" name="orders">
        <div class="orders-section">
          <div v-loading="loadingOrders" class="orders-list">
            <div v-for="order in orders" :key="order.id" class="order-item">
              <div class="order-image">
                <img v-if="order.itemImage" :src="order.itemImage" :alt="order.itemName" />
                <div v-else class="image-placeholder-sm">
                  <el-icon><Goods /></el-icon>
                </div>
              </div>
              <div class="order-info">
                <div class="order-header">
                  <h4>{{ order.itemName }}</h4>
                  <el-tag :type="getStatusType(order.status)">
                    {{ getStatusText(order.status) }}
                  </el-tag>
                </div>
                <div class="order-meta">
                  <span>订单号: {{ order.orderNo }}</span>
                  <span>数量: {{ order.quantity }}</span>
                </div>
                <div class="order-footer">
                  <span class="order-points">
                    -{{ order.costPoints }} 积分
                  </span>
                  <span class="order-time">{{ formatTime(order.createTime) }}</span>
                </div>
              </div>
            </div>
          </div>

          <el-pagination
            v-if="orders.length > 0"
            class="pagination"
            v-model:current-page="orderPage"
            v-model:page-size="orderPageSize"
            :total="orderTotal"
            layout="prev, pager, next"
            @current-change="loadOrders"
          />

          <el-empty v-if="!loadingOrders && orders.length === 0" description="暂无兑换记录" />
        </div>
      </el-tab-pane>

      <el-tab-pane label="积分流水" name="ledger">
        <div class="ledger-section">
          <div v-loading="loadingLedger" class="ledger-list">
            <div v-for="item in ledger" :key="item.id" class="ledger-item">
              <div class="ledger-icon" :class="item.delta > 0 ? 'income' : 'expense'">
                <el-icon>
                  <TrendCharts v-if="item.delta > 0" />
                  <TrendCharts v-else style="transform: rotate(180deg)" />
                </el-icon>
              </div>
              <div class="ledger-info">
                <div class="ledger-reason">{{ item.reason }}</div>
                <div class="ledger-time">{{ formatTime(item.createTime) }}</div>
              </div>
              <div class="ledger-amount" :class="item.delta > 0 ? 'income' : 'expense'">
                {{ item.delta > 0 ? '+' : '' }}{{ item.delta }}
              </div>
            </div>
          </div>

          <el-pagination
            v-if="ledger.length > 0"
            class="pagination"
            v-model:current-page="ledgerPage"
            v-model:page-size="ledgerPageSize"
            :total="ledgerTotal"
            layout="prev, pager, next"
            @current-change="loadLedger"
          />

          <el-empty v-if="!loadingLedger && ledger.length === 0" description="暂无积分记录" />
        </div>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="detailDialogVisible" title="商品详情" width="500px">
      <div v-if="selectedItem" class="item-detail">
        <div class="detail-image">
          <img v-if="selectedItem.imageUrl" :src="selectedItem.imageUrl" :alt="selectedItem.name" />
          <div v-else class="image-placeholder-lg">
            <el-icon><Goods /></el-icon>
          </div>
        </div>
        <div class="detail-info">
          <h3>{{ selectedItem.name }}</h3>
          <p class="detail-desc">{{ selectedItem.description || '暂无描述' }}</p>
          <div class="detail-meta">
            <el-tag :type="selectedItem.clubId ? 'info' : 'success'">
              {{ selectedItem.clubName }}
            </el-tag>
            <span class="detail-stock">库存: {{ selectedItem.stock }}</span>
          </div>
          <div class="detail-points">
            <span class="points-label">所需积分:</span>
            <span class="points-value">
              <el-icon><Coin /></el-icon>
              {{ selectedItem.costPoints }}
            </span>
          </div>
          <div class="detail-quantity">
            <span>兑换数量:</span>
            <el-input-number
              v-model="redeemQuantity"
              :min="1"
              :max="selectedItem.stock"
              size="small"
            />
          </div>
          <div class="detail-total">
            <span>共需积分:</span>
            <span class="total-points">{{ totalCost }}</span>
            <span v-if="myPoints.balance < totalCost" class="insufficient">
              (积分不足)
            </span>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="detailDialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          :disabled="!canRedeem"
          @click="confirmRedeem"
        >
          立即兑换
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Trophy, Search, Goods, Coin, TrendCharts } from '@element-plus/icons-vue'
import { useUserStore } from '../../store/user'
import pointApi, { type PointInfo, type ShopItem, type PointLedger, type RedeemOrder } from '../../api/points'

const userStore = useUserStore()

const myPoints = ref<PointInfo>({
  userId: 0,
  userName: '',
  balance: 0,
  totalEarned: 0,
  totalSpent: 0
})

const activeTab = ref('shop')
const searchKeyword = ref('')
const filterClub = ref<number | null>(null)

const shopItems = ref<ShopItem[]>([])
const shopPage = ref(1)
const shopPageSize = ref(8)
const shopTotal = ref(0)
const loadingShop = ref(false)

const orders = ref<RedeemOrder[]>([])
const orderPage = ref(1)
const orderPageSize = ref(10)
const orderTotal = ref(0)
const loadingOrders = ref(false)

const ledger = ref<PointLedger[]>([])
const ledgerPage = ref(1)
const ledgerPageSize = ref(10)
const ledgerTotal = ref(0)
const loadingLedger = ref(false)

const detailDialogVisible = ref(false)
const selectedItem = ref<ShopItem | null>(null)
const redeemQuantity = ref(1)

const totalCost = computed(() => {
  if (!selectedItem.value) return 0
  return selectedItem.value.costPoints * redeemQuantity.value
})

const canRedeem = computed(() => {
  if (!selectedItem.value) return false
  return myPoints.value.balance >= totalCost.value && selectedItem.value.stock >= redeemQuantity.value
})

const loadMyPoints = async () => {
  try {
    const res = await pointApi.getMyPoints()
    myPoints.value = res as any
  } catch (e) {
    console.error(e)
  }
}

const loadShopItems = async () => {
  loadingShop.value = true
  try {
    const params: any = {
      keyword: searchKeyword.value || undefined,
      pageNum: shopPage.value,
      pageSize: shopPageSize.value
    }
    if (filterClub.value === 0) {
    } else if (filterClub.value) {
      params.clubId = filterClub.value
    }
    const res = await pointApi.getShopItems(params)
    const data = res as any
    shopItems.value = data.records
    shopTotal.value = data.total
  } catch (e) {
    console.error(e)
  } finally {
    loadingShop.value = false
  }
}

const loadOrders = async () => {
  loadingOrders.value = true
  try {
    const res = await pointApi.getMyOrders({
      pageNum: orderPage.value,
      pageSize: orderPageSize.value
    })
    const data = res as any
    orders.value = data.records
    orderTotal.value = data.total
  } catch (e) {
    console.error(e)
  } finally {
    loadingOrders.value = false
  }
}

const loadLedger = async () => {
  loadingLedger.value = true
  try {
    const res = await pointApi.getMyLedger({
      pageNum: ledgerPage.value,
      pageSize: ledgerPageSize.value
    })
    const data = res as any
    ledger.value = data.records
    ledgerTotal.value = data.total
  } catch (e) {
    console.error(e)
  } finally {
    loadingLedger.value = false
  }
}

const openItemDetail = (item: ShopItem) => {
  selectedItem.value = item
  redeemQuantity.value = 1
  detailDialogVisible.value = true
}

const confirmRedeem = async () => {
  if (!selectedItem.value) return

  try {
    await ElMessageBox.confirm(
      `确定要兑换 ${selectedItem.value.name} x ${redeemQuantity.value} 吗？\n将消耗 ${totalCost.value} 积分`,
      '确认兑换',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await pointApi.redeemItem({
      itemId: selectedItem.value.id,
      quantity: redeemQuantity.value
    })

    ElMessage.success('兑换成功！')
    detailDialogVisible.value = false
    loadMyPoints()
    loadShopItems()
    loadOrders()
    loadLedger()
  } catch (e) {
    if (e !== 'cancel') {
      console.error(e)
    }
  }
}

const getStatusType = (status: string) => {
  const map: Record<string, string> = {
    PENDING: 'warning',
    COMPLETED: 'success',
    CANCELLED: 'info',
    FAILED: 'danger'
  }
  return map[status] || 'info'
}

const getStatusText = (status: string) => {
  const map: Record<string, string> = {
    PENDING: '待处理',
    COMPLETED: '已完成',
    CANCELLED: '已取消',
    FAILED: '失败'
  }
  return map[status] || status
}

const formatTime = (time: string) => {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN')
}

onMounted(() => {
  loadMyPoints()
  loadShopItems()
  loadOrders()
  loadLedger()
})
</script>

<style scoped>
.points-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.glass-card {
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.3);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.stats-card {
  padding: 30px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.points-header {
  display: flex;
  align-items: center;
  gap: 20px;
}

.points-icon {
  width: 80px;
  height: 80px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40px;
}

.points-info h2 {
  margin: 0 0 8px 0;
  font-size: 18px;
  font-weight: 500;
  opacity: 0.9;
}

.points-balance {
  font-size: 48px;
  font-weight: bold;
  line-height: 1;
  margin-bottom: 8px;
}

.points-detail {
  font-size: 14px;
  opacity: 0.85;
  display: flex;
  gap: 12px;
}

.points-detail .divider {
  opacity: 0.5;
}

.content-tabs {
  padding: 20px;
}

.shop-header {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.shop-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 16px;
}

.shop-item {
  background: white;
  border-radius: 10px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid rgba(0, 0, 0, 0.05);
}

.shop-item:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.12);
}

.item-image {
  position: relative;
  width: 100%;
  height: 140px;
  background: #f5f5f5;
  overflow: hidden;
}

.item-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-placeholder,
.image-placeholder-sm,
.image-placeholder-lg {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ccc;
  font-size: 48px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e8e8e8 100%);
}

.image-placeholder-sm {
  font-size: 32px;
}

.image-placeholder-lg {
  font-size: 64px;
}

.item-tag {
  position: absolute;
  top: 8px;
  left: 8px;
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.club-tag {
  background: rgba(64, 158, 255, 0.9);
  color: white;
}

.global-tag {
  background: rgba(103, 194, 58, 0.9);
  color: white;
}

.item-info {
  padding: 12px;
}

.item-name {
  margin: 0 0 6px 0;
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.item-desc {
  margin: 0 0 10px 0;
  font-size: 13px;
  color: #909399;
  height: 36px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.item-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.item-points {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #e6a23c;
  font-weight: 600;
  font-size: 16px;
}

.item-stock {
  font-size: 12px;
  color: #909399;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.orders-list,
.ledger-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.order-item {
  display: flex;
  gap: 16px;
  padding: 16px;
  background: white;
  border-radius: 10px;
  border: 1px solid rgba(0, 0, 0, 0.05);
}

.order-image {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  overflow: hidden;
  flex-shrink: 0;
}

.order-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.order-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.order-header h4 {
  margin: 0;
  font-size: 15px;
  color: #303133;
}

.order-meta {
  font-size: 12px;
  color: #909399;
  display: flex;
  gap: 16px;
}

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
}

.order-points {
  color: #e6a23c;
  font-weight: 600;
}

.order-time {
  color: #c0c4cc;
}

.ledger-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: white;
  border-radius: 10px;
  border: 1px solid rgba(0, 0, 0, 0.05);
}

.ledger-icon {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
}

.ledger-icon.income {
  background: rgba(103, 194, 58, 0.1);
  color: #67c23a;
}

.ledger-icon.expense {
  background: rgba(245, 108, 108, 0.1);
  color: #f56c6c;
}

.ledger-info {
  flex: 1;
}

.ledger-reason {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
  margin-bottom: 4px;
}

.ledger-time {
  font-size: 12px;
  color: #909399;
}

.ledger-amount {
  font-size: 18px;
  font-weight: bold;
}

.ledger-amount.income {
  color: #67c23a;
}

.ledger-amount.expense {
  color: #f56c6c;
}

.item-detail {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.detail-image {
  width: 100%;
  height: 200px;
  border-radius: 8px;
  overflow: hidden;
  background: #f5f5f5;
}

.detail-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.detail-info h3 {
  margin: 0 0 10px 0;
  font-size: 20px;
  color: #303133;
}

.detail-desc {
  color: #606266;
  line-height: 1.6;
  margin-bottom: 16px;
}

.detail-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
}

.detail-stock {
  font-size: 13px;
  color: #909399;
}

.detail-points {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
}

.points-label {
  color: #606266;
}

.points-value {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #e6a23c;
  font-size: 24px;
  font-weight: bold;
}

.detail-quantity {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  font-size: 14px;
  color: #606266;
}

.detail-total {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
  font-size: 14px;
  color: #606266;
}

.total-points {
  color: #e6a23c;
  font-size: 18px;
  font-weight: bold;
}

.insufficient {
  color: #f56c6c;
  font-size: 12px;
}
</style>
