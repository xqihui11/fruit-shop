<template>
  <div class="orders-page">
    <el-card class="orders-card" shadow="always">
      <template #header>
        <div class="card-header">
          <div>
            <div class="card-title">订单管理</div>
            <div class="card-subtitle">统一查看并处理店铺订单，支持快速发货与筛选</div>
          </div>
        </div>
      </template>

      <!-- 顶部状态切换 -->
      <el-tabs v-model="activeTab" @tab-change="handleTabChange" class="orders-tabs">
        <el-tab-pane label="全部" name=""></el-tab-pane>
        <el-tab-pane label="待发货" name="1"></el-tab-pane>
        <el-tab-pane label="待收货" name="2"></el-tab-pane>
        <el-tab-pane label="已完成" name="4"></el-tab-pane>
      </el-tabs>

      <!-- 筛选区域 -->
      <el-form :inline="true" :model="filters" class="filter-form" @submit.prevent>
        <el-form-item label="订单号">
          <el-input
            v-model="filters.keyword"
            placeholder="输入订单号搜索"
            clearable
            style="width: 220px"
          />
        </el-form-item>
        <el-form-item label="下单时间">
          <el-date-picker
            v-model="filters.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 280px"
            clearable
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">筛选</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 表格区域 -->
      <div class="table-wrapper">
        <el-skeleton v-if="loading" :rows="6" animated />

        <template v-else>
          <el-empty
            v-if="pagedOrders.length === 0"
            description="暂无订单记录，可尝试调整筛选条件"
            :image-size="120"
          />
          <el-table
            v-else
            :data="pagedOrders"
            :header-cell-style="headerCellStyle"
            :cell-style="cellStyle"
            class="orders-table"
            highlight-current-row
          >
            <el-table-column label="商品" width="80">
              <template #default="{ row }">
                <img
                  v-if="row.firstProductImage"
                  :src="getImageUrl(row.firstProductImage)"
                  class="product-thumb"
                />
                <span v-else>暂无</span>
              </template>
            </el-table-column>
            <el-table-column prop="orderNo" label="订单号" min-width="220">
              <template #default="{ row }">
                <div class="order-no-cell">
                  <span class="order-no-text" @click="copyOrderNo(row.orderNo)">
                    {{ row.orderNo }}
                  </span>
                  <el-tag size="small" class="copy-tag">复制</el-tag>
                </div>
              </template>
            </el-table-column>

            <el-table-column prop="payAmount" label="金额" min-width="120" align="right">
              <template #default="{ row }">¥{{ Number(row.payAmount || 0).toFixed(2) }}</template>
            </el-table-column>

            <el-table-column prop="orderStatus" label="状态" min-width="120">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.orderStatus, row)" effect="light">
                  {{ getStatusText(row.orderStatus, row) }}
                </el-tag>
              </template>
            </el-table-column>

            <el-table-column prop="createTime" label="下单时间" min-width="180">
              <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
            </el-table-column>

            <el-table-column label="操作" min-width="180" fixed="right">
              <template #default="{ row }">
                <div class="action-buttons">
                  <el-button
                    v-if="row.orderStatus === 1"
                    type="primary"
                    size="small"
                    @click="showShipDialog(row)"
                  >
                    发货
                  </el-button>
                  <el-button size="small" @click="viewDetail(row)">详情</el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>

          <div v-if="filteredOrders.length > 0" class="pagination-wrapper">
            <el-pagination
              background
              layout="total, prev, pager, next, jumper"
              :page-size="pageSize"
              :current-page="currentPage"
              :total="filteredOrders.length"
              @current-change="handlePageChange"
            />
          </div>
        </template>
      </div>
    </el-card>

    <!-- 发货对话框 -->
    <el-dialog v-model="shipDialogVisible" title="订单发货" width="400px">
      <el-form :model="shipForm" :rules="shipRules" ref="shipFormRef" label-width="100px">
        <el-form-item label="物流公司" prop="logisticsCompany">
          <el-select v-model="shipForm.logisticsCompany" placeholder="请选择物流公司" style="width: 100%">
            <el-option label="顺丰速运" value="顺丰速运" />
            <el-option label="中通快递" value="中通快递" />
            <el-option label="圆通速递" value="圆通速递" />
            <el-option label="韵达快递" value="韵达快递" />
            <el-option label="申通快递" value="申通快递" />
          </el-select>
        </el-form-item>
        <el-form-item label="物流单号" prop="logisticsNo">
          <el-input v-model="shipForm.logisticsNo" placeholder="请输入物流单号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="shipDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmShip" :loading="shipping">确认发货</el-button>
      </template>
    </el-dialog>

    <!-- 订单详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="订单详情" width="800px">
      <div v-if="orderDetail" class="order-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单号">{{ orderDetail.order.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="订单状态">
            <el-tag :type="getStatusType(orderDetail.order.orderStatus)">
              {{ getStatusText(orderDetail.order.orderStatus) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="订单金额">¥{{ orderDetail.order.payAmount }}</el-descriptions-item>
          <el-descriptions-item label="下单时间">{{ formatTime(orderDetail.order.createTime) }}</el-descriptions-item>
          <el-descriptions-item v-if="orderDetail.order.logisticsCompany" label="物流公司">
            {{ orderDetail.order.logisticsCompany }}
          </el-descriptions-item>
          <el-descriptions-item v-if="orderDetail.order.logisticsNo" label="物流单号">
            {{ orderDetail.order.logisticsNo }}
          </el-descriptions-item>
          <el-descriptions-item v-if="orderDetail.order.remark" label="订单备注" :span="2">
            {{ orderDetail.order.remark }}
          </el-descriptions-item>
        </el-descriptions>

        <div class="order-items-section">
          <h3>订单商品</h3>
          <el-table :data="orderDetail.items" stripe>
            <el-table-column label="商品图片" width="100">
              <template #default="{ row }">
                <img
                  v-if="row.productImage"
                  :src="getImageUrl(row.productImage)"
                  class="product-thumb"
                />
                <span v-else>暂无图片</span>
              </template>
            </el-table-column>
            <el-table-column prop="productName" label="商品名称" />
            <el-table-column label="规格" width="120">
              <template #default="{ row }">
                {{ row.specName || '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="price" label="单价" width="100">
              <template #default="{ row }">¥{{ row.price }}</template>
            </el-table-column>
            <el-table-column prop="quantity" label="数量" width="80" />
            <el-table-column prop="subtotal" label="小计" width="100">
              <template #default="{ row }">¥{{ row.subtotal }}</template>
            </el-table-column>
          </el-table>
        </div>
      </div>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import api from '@/utils/api'

const orders = ref([])
const activeTab = ref('')
const loading = ref(false)
const shipDialogVisible = ref(false)
const currentOrder = ref(null)
const shipFormRef = ref(null)
const shipping = ref(false)
const detailDialogVisible = ref(false)
const orderDetail = ref(null)
const loadingDetail = ref(false)

const filters = ref({
  keyword: '',
  dateRange: []
})

const pageSize = ref(10)
const currentPage = ref(1)

const shipForm = ref({
  logisticsCompany: '',
  logisticsNo: ''
})

const shipRules = {
  logisticsCompany: [{ required: true, message: '请选择物流公司', trigger: 'change' }],
  logisticsNo: [{ required: true, message: '请输入物流单号', trigger: 'blur' }]
}

const getStatusText = (status, order) => {
  // 若订单正处于售后中，则在商家侧优先展示“售后中”以便快速感知风险
  if (order && order.afterSaleInProgress) {
    return '售后中'
  }
  const map = {
    0: '待支付',
    1: '待发货',
    2: '待收货',
    3: '待评价',
    4: '已完成',
    5: '已取消'
  }
  return map[status] || '未知'
}

const getStatusType = (status, order) => {
  // 售后中高亮为橙色，其他状态与用户端保持一致风格
  if (order && order.afterSaleInProgress) {
    return 'warning'
  }
  const map = {
    0: 'info', // 待支付
    1: 'warning', // 待发货
    2: 'info', // 待收货
    3: 'success', // 待评价
    4: 'success', // 已完成
    5: 'danger' // 已取消
  }
  return map[status] || 'info'
}

const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN')
}

const loadOrders = async () => {
  try {
    loading.value = true
    const params = activeTab.value ? { status: parseInt(activeTab.value) } : {}
    const res = await api.get('/merchant/order/list', { params })
    if (res.code === 200) {
      orders.value = res.data || []
      // 为列表每条订单加载一张商品图
      for (const order of orders.value) {
        try {
          const itemsRes = await api.get(`/order/items/${order.id}`)
          if (itemsRes.code === 200 && itemsRes.data && itemsRes.data.length > 0) {
            order.firstProductImage = itemsRes.data[0].productImage
          } else {
            order.firstProductImage = ''
          }
        } catch (e) {
          console.error(`加载订单 ${order.id} 商品失败`, e)
          order.firstProductImage = ''
        }
      }
    } else {
      ElMessage.error(res.message || '加载订单失败')
      orders.value = []
    }
  } catch (error) {
    const errorMsg = error.response?.data?.message || error.message || '加载订单失败'
    ElMessage.error(errorMsg)
    console.error('加载订单失败', error)
    orders.value = []
  } finally {
    loading.value = false
  }
}

const handleTabChange = () => {
  currentPage.value = 1
  loadOrders()
}

const handleSearch = () => {
  currentPage.value = 1
}

const resetFilters = () => {
  filters.value = {
    keyword: '',
    dateRange: []
  }
  currentPage.value = 1
}

const handlePageChange = (page) => {
  currentPage.value = page
}

// 订单过滤与分页
const filteredOrders = computed(() => {
  let list = orders.value || []

  if (filters.value.keyword) {
    const kw = filters.value.keyword.trim()
    list = list.filter(item => item.orderNo && item.orderNo.includes(kw))
  }

  if (filters.value.dateRange && filters.value.dateRange.length === 2) {
    const [start, end] = filters.value.dateRange
    const startTime = new Date(start + ' 00:00:00').getTime()
    const endTime = new Date(end + ' 23:59:59').getTime()
    list = list.filter(item => {
      if (!item.createTime) return false
      const t = new Date(item.createTime).getTime()
      return t >= startTime && t <= endTime
    })
  }

  return list
})

const pagedOrders = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredOrders.value.slice(start, end)
})

// 表格样式
const headerCellStyle = () => ({
  background: '#fafafa',
  fontWeight: 600,
  color: '#303133'
})

const cellStyle = () => ({
  padding: '12px 8px'
})

// 点击复制订单号（简单实现，避免额外依赖）
const copyOrderNo = async (orderNo) => {
  try {
    await navigator.clipboard.writeText(orderNo)
    ElMessage.success('订单号已复制')
  } catch (e) {
    ElMessage.error('复制失败，请手动选择复制')
  }
}

const showShipDialog = (order) => {
  currentOrder.value = order
  shipForm.value = { logisticsCompany: '', logisticsNo: '' }
  shipDialogVisible.value = true
}

const confirmShip = async () => {
  try {
    await shipFormRef.value.validate()
    shipping.value = true
    const res = await api.post(`/merchant/order/ship/${currentOrder.value.id}`, shipForm.value)
    if (res.code === 200) {
      ElMessage.success('发货成功')
      shipDialogVisible.value = false
      loadOrders()
    } else {
      ElMessage.error(res.message || '发货失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('发货失败')
    }
  } finally {
    shipping.value = false
  }
}

const viewDetail = async (order) => {
  try {
    loadingDetail.value = true
    detailDialogVisible.value = true
    const res = await api.get(`/merchant/order/detail/${order.id}`)
    if (res.code === 200) {
      orderDetail.value = res.data
    } else {
      ElMessage.error(res.message || '加载订单详情失败')
      detailDialogVisible.value = false
    }
  } catch (error) {
    const errorMsg = error.response?.data?.message || error.message || '加载订单详情失败'
    ElMessage.error(errorMsg)
    detailDialogVisible.value = false
    console.error('加载订单详情失败:', error)
  } finally {
    loadingDetail.value = false
  }
}

const getImageUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http://') || url.startsWith('https://')) {
    return url
  }
  if (url.startsWith('/uploads/')) {
    return '/api' + url
  }
  if (url.startsWith('/api/uploads/')) {
    return url
  }
  return url
}

onMounted(() => {
  loadOrders()
})
</script>

<style scoped>
.orders-page {
  padding: 24px;
}

.orders-card {
  background: #ffffff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.04);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.card-subtitle {
  margin-top: 4px;
  font-size: 13px;
  color: #909399;
}

.orders-tabs {
  margin-bottom: 12px;
}

.filter-form {
  margin-bottom: 16px;
}

.table-wrapper {
  background: #fff;
}

.orders-table :deep(.el-table__row:hover) {
  background-color: #f5f7fa;
}

.order-no-cell {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.order-no-text {
  color: #409eff;
}

.copy-tag {
  cursor: pointer;
}

.action-buttons {
  display: flex;
  gap: 8px;
}

.pagination-wrapper {
  margin-top: 16px;
  display: flex;
  justify-content: center;
}

.product-thumb {
  width: 40px;
  height: 40px;
  border-radius: 4px;
  object-fit: cover;
}

.orders-page :deep(.el-table__cell) {
  padding-top: 10px;
  padding-bottom: 10px;
}
</style>

