<template>
  <div class="points-leader-page">
    <div class="page-header">
      <h2>积分商城管理</h2>
      <el-button type="primary" @click="openCreateDialog">
        <el-icon><Plus /></el-icon>
        新增商品
      </el-button>
    </div>

    <el-tabs v-model="activeTab" class="glass-card content-tabs">
      <el-tab-pane label="商品管理" name="items">
        <div class="items-section">
          <div class="items-toolbar">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索商品..."
              style="width: 250px"
              clearable
              @input="loadItems"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-select
              v-model="filterStatus"
              placeholder="全部状态"
              style="width: 140px"
              @change="loadItems"
            >
              <el-option label="全部状态" value="" />
              <el-option label="上架中" value="ON_SALE" />
              <el-option label="已下架" value="OFF_SALE" />
            </el-select>
          </div>

          <el-table :data="items" v-loading="loadingItems" stripe>
            <el-table-column prop="id" label="ID" width="70" />
            <el-table-column label="商品" min-width="200">
              <template #default="{ row }">
                <div class="item-cell">
                  <div class="item-thumb">
                    <img v-if="row.imageUrl" :src="row.imageUrl" />
                    <el-icon v-else><Goods /></el-icon>
                  </div>
                  <span class="item-name">{{ row.name }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="costPoints" label="所需积分" width="120">
              <template #default="{ row }">
                <span class="text-points">
                  <el-icon><Coin /></el-icon>
                  {{ row.costPoints }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="stock" label="库存" width="80" />
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === 'ON_SALE' ? 'success' : 'info'">
                  {{ row.status === 'ON_SALE' ? '上架中' : '已下架' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="sortOrder" label="排序" width="80" />
            <el-table-column label="操作" width="240" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link size="small" @click="openEditDialog(row)">
                  编辑
                </el-button>
                <el-button
                  :type="row.status === 'ON_SALE' ? 'warning' : 'success'"
                  link
                  size="small"
                  @click="toggleStatus(row)"
                >
                  {{ row.status === 'ON_SALE' ? '下架' : '上架' }}
                </el-button>
                <el-button type="danger" link size="small" @click="handleDelete(row)">
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <el-pagination
            v-if="items.length > 0"
            class="pagination"
            v-model:current-page="pageNum"
            v-model:page-size="pageSize"
            :total="total"
            layout="total, prev, pager, next"
            @current-change="loadItems"
          />
        </div>
      </el-tab-pane>

      <el-tab-pane label="兑换订单" name="orders">
        <div class="orders-section">
          <div class="orders-toolbar">
            <el-select
              v-model="orderStatus"
              placeholder="全部状态"
              style="width: 140px"
              @change="loadOrders"
            >
              <el-option label="全部状态" value="" />
              <el-option label="待处理" value="PENDING" />
              <el-option label="已完成" value="COMPLETED" />
              <el-option label="已取消" value="CANCELLED" />
            </el-select>
          </div>

          <el-table :data="orders" v-loading="loadingOrders" stripe>
            <el-table-column prop="orderNo" label="订单号" width="200" />
            <el-table-column prop="itemName" label="商品" min-width="180" />
            <el-table-column prop="userName" label="兑换人" width="120" />
            <el-table-column prop="quantity" label="数量" width="80" />
            <el-table-column prop="costPoints" label="消耗积分" width="120">
              <template #default="{ row }">
                <span class="text-points">-{{ row.costPoints }}</span>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)">
                  {{ getStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="兑换时间" width="180" />
          </el-table>

          <el-pagination
            v-if="orders.length > 0"
            class="pagination"
            v-model:current-page="orderPageNum"
            v-model:page-size="orderPageSize"
            :total="orderTotal"
            layout="total, prev, pager, next"
            @current-change="loadOrders"
          />
        </div>
      </el-tab-pane>
    </el-tabs>

    <el-dialog
      v-model="itemDialogVisible"
      :title="isEdit ? '编辑商品' : '新增商品'"
      width="600px"
    >
      <el-form :model="itemForm" :rules="itemRules" ref="itemFormRef" label-width="100px">
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="itemForm.name" placeholder="请输入商品名称" />
        </el-form-item>
        <el-form-item label="商品描述" prop="description">
          <el-input
            v-model="itemForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入商品描述"
          />
        </el-form-item>
        <el-form-item label="图片URL" prop="imageUrl">
          <el-input v-model="itemForm.imageUrl" placeholder="请输入商品图片URL" />
        </el-form-item>
        <el-form-item label="所需积分" prop="costPoints">
          <el-input-number v-model="itemForm.costPoints" :min="1" style="width: 200px" />
        </el-form-item>
        <el-form-item label="库存" prop="stock">
          <el-input-number v-model="itemForm.stock" :min="0" style="width: 200px" />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="itemForm.sortOrder" :min="0" style="width: 200px" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="itemDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitItem" :loading="submitting">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus, Search, Goods, Coin } from '@element-plus/icons-vue'
import { useUserStore } from '../../store/user'
import pointApi, { type ShopItem, type RedeemOrder } from '../../api/points'

const userStore = useUserStore()

const activeTab = ref('items')
const searchKeyword = ref('')
const filterStatus = ref('')

const items = ref<ShopItem[]>([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const loadingItems = ref(false)

const orders = ref<RedeemOrder[]>([])
const orderPageNum = ref(1)
const orderPageSize = ref(10)
const orderTotal = ref(0)
const loadingOrders = ref(false)
const orderStatus = ref('')

const itemDialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref<number | null>(null)
const submitting = ref(false)
const itemFormRef = ref<FormInstance>()

const itemForm = ref({
  name: '',
  description: '',
  imageUrl: '',
  costPoints: 1,
  stock: 0,
  sortOrder: 0
})

const itemRules: FormRules = {
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  costPoints: [{ required: true, message: '请输入所需积分', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入库存', trigger: 'blur' }]
}

const loadItems = async () => {
  loadingItems.value = true
  try {
    const res = await pointApi.getMyClubItems({
      keyword: searchKeyword.value || undefined,
      status: filterStatus.value || undefined,
      pageNum: pageNum.value,
      pageSize: pageSize.value
    })
    const data = res as any
    items.value = data.records
    total.value = data.total
  } catch (e) {
    console.error(e)
  } finally {
    loadingItems.value = false
  }
}

const loadOrders = async () => {
  loadingOrders.value = true
  try {
    const res = await pointApi.getAllOrders({
      status: orderStatus.value || undefined,
      pageNum: orderPageNum.value,
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

const openCreateDialog = () => {
  isEdit.value = false
  editId.value = null
  itemForm.value = {
    name: '',
    description: '',
    imageUrl: '',
    costPoints: 1,
    stock: 0,
    sortOrder: 0
  }
  itemDialogVisible.value = true
}

const openEditDialog = (item: ShopItem) => {
  isEdit.value = true
  editId.value = item.id
  itemForm.value = {
    name: item.name,
    description: item.description || '',
    imageUrl: item.imageUrl || '',
    costPoints: item.costPoints,
    stock: item.stock,
    sortOrder: item.sortOrder || 0
  }
  itemDialogVisible.value = true
}

const submitItem = async () => {
  if (!itemFormRef.value) return

  try {
    await itemFormRef.value.validate()
  } catch (e) {
    return
  }

  submitting.value = true
  try {
    const clubId = userStore.userInfo?.clubId
    if (isEdit.value && editId.value) {
      await pointApi.updateItem(editId.value, {
        ...itemForm.value,
        clubId: clubId
      })
      ElMessage.success('编辑成功')
    } else {
      await pointApi.createItem({
        ...itemForm.value,
        clubId: clubId
      })
      ElMessage.success('创建成功')
    }
    itemDialogVisible.value = false
    loadItems()
  } catch (e) {
    console.error(e)
  } finally {
    submitting.value = false
  }
}

const toggleStatus = async (item: ShopItem) => {
  try {
    const newStatus = item.status === 'ON_SALE' ? 'OFF_SALE' : 'ON_SALE'
    await pointApi.updateItemStatus(item.id, newStatus)
    ElMessage.success(newStatus === 'ON_SALE' ? '已上架' : '已下架')
    loadItems()
  } catch (e) {
    console.error(e)
  }
}

const handleDelete = async (item: ShopItem) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除商品「${item.name}」吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    await pointApi.deleteItem(item.id)
    ElMessage.success('删除成功')
    loadItems()
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

onMounted(() => {
  loadItems()
  loadOrders()
})
</script>

<style scoped>
.points-leader-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
  color: #303133;
}

.glass-card {
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.3);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  padding: 20px;
}

.items-toolbar,
.orders-toolbar {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.item-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.item-thumb {
  width: 40px;
  height: 40px;
  border-radius: 6px;
  overflow: hidden;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ccc;
  flex-shrink: 0;
}

.item-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.item-name {
  font-weight: 500;
  color: #303133;
}

.text-points {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #e6a23c;
  font-weight: 600;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>
