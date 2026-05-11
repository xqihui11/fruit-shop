<template>
  <div class="orders-page">
    <el-card class="orders-card" shadow="never">
      <template #header>
        <div class="card-header">
          <div class="card-title-wrap">
            <div class="card-title">订单管理</div>
            <div class="card-subtitle">统一管理订单状态、履约进度与售后风险</div>
          </div>
          <el-input v-model="keyword" class="search-input" placeholder="搜索订单号 / 用户ID / 商家ID" clearable>
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
        </div>
      </template>

      <div class="filters">
        <el-date-picker
          v-model="timeRange"
          type="daterange"
          range-separator="至"
          start-placeholder="下单开始日期"
          end-placeholder="下单结束日期"
          value-format="YYYY-MM-DD"
        />
        <el-select
          v-model="merchantFilter"
          placeholder="按商家筛选（可输入商家名）"
          clearable
          filterable
          class="merchant-filter"
        >
          <el-option
            v-for="item in merchantOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
        <el-switch v-model="groupByMerchant" inline-prompt active-text="按商家分组" inactive-text="普通排序" />
        <el-button @click="resetFilters">重置筛选</el-button>
      </div>

      <el-tabs v-model="activeTab" @tab-change="loadOrders" class="status-tabs">
        <el-tab-pane label="全部" name="" />
        <el-tab-pane label="待支付" name="0" />
        <el-tab-pane label="待发货" name="1" />
        <el-tab-pane label="待收货" name="2" />
        <el-tab-pane label="已完成" name="4" />
        <el-tab-pane label="已取消" name="5" />
      </el-tabs>

      <div class="toolbar">
        <div class="toolbar-left">已选中 <strong>{{ selectedOrders.length }}</strong> 个订单</div>
        <div class="toolbar-right">
          <el-button :disabled="!filteredOrders.length" @click="exportCsv">导出</el-button>
        </div>
      </div>

      <el-table
        :data="pagedOrders"
        stripe
        class="orders-table"
        :row-class-name="getRowClassName"
        @selection-change="handleSelectionChange"
        @sort-change="handleSortChange"
      >
        <el-table-column type="selection" width="48" />

        <el-table-column label="商品" min-width="260">
          <template #default="{ row }">
            <div class="product-cell">
              <div class="thumb-wrap">
                <img v-if="row.firstProductImage" :src="getImageUrl(row.firstProductImage)" class="thumb" />
                <div v-else class="thumb-empty">暂无</div>
              </div>
              <div class="product-meta">
                <div class="product-name">{{ row.firstProductName || '未知商品' }}</div>
                <div class="product-sub">共 {{ row.totalQuantity ?? 0 }} 件</div>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="orderNo" label="订单号" min-width="220" />
        <el-table-column prop="userId" label="用户ID" width="90" />
        <el-table-column label="商家" min-width="150">
          <template #default="{ row }">
            <span class="merchant-badge" :style="getMerchantBadgeStyle(row)">
              {{ getMerchantDisplayName(row) }}
            </span>
          </template>
        </el-table-column>

        <el-table-column prop="payAmount" label="金额" width="120" sortable="custom" align="right">
          <template #default="{ row }"><span class="amount">¥{{ formatMoney(row.payAmount) }}</span></template>
        </el-table-column>

        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <span class="status-pill" :class="getStatusClass(row.orderStatus)">{{ getStatusText(row.orderStatus) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="订单标签" min-width="180">
          <template #default="{ row }">
            <el-space wrap size="4">
              <el-tag v-for="tag in getOrderTags(row)" :key="tag" size="small" effect="plain">{{ tag }}</el-tag>
            </el-space>
          </template>
        </el-table-column>

        <el-table-column prop="createTime" label="下单时间" width="170" sortable="custom">
          <template #default="{ row }">
            <el-tooltip :content="formatTime(row.createTime)" placement="top">
              <span>{{ formatRelativeTime(row.createTime) }}</span>
            </el-tooltip>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewDetail(row)">ℹ️ 详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <div class="pagination-wrap">
      <el-pagination
        background
        layout="prev, pager, next"
        :page-size="pageSize"
        :current-page="currentPage"
        :total="sortedOrders.length"
        @current-change="(p) => currentPage = p"
      />
    </div>

    <el-drawer v-model="detailVisible" title="订单详情" size="560px" direction="rtl">
      <div v-if="currentOrder" class="detail-wrap">
        <div class="detail-row"><span>订单号</span><span>{{ currentOrder.orderNo }}</span></div>
        <div class="detail-row"><span>订单状态</span><span>{{ getStatusText(currentOrder.orderStatus) }}</span></div>
        <div class="detail-row"><span>订单金额</span><span>¥{{ formatMoney(currentOrder.totalAmount) }}</span></div>
        <div class="detail-row"><span>实付金额</span><span>¥{{ formatMoney(currentOrder.payAmount) }}</span></div>
        <div class="detail-row"><span>下单时间</span><span>{{ formatTime(currentOrder.createTime) || '—' }}</span></div>
        <div class="detail-row"><span>发货时间</span><span>{{ formatTime(currentOrder.shipTime) || '—' }}</span></div>
        <div class="detail-row"><span>收货人</span><span>{{ currentOrder.receiverName || '未获取' }}</span></div>
        <div class="detail-row"><span>联系电话</span><span>{{ currentOrder.receiverPhone || '未获取' }}</span></div>
        <div class="detail-row"><span>用户信息</span><span>{{ currentOrder.userName || `用户#${currentOrder.userId}` }}</span></div>
        <div class="detail-row"><span>商家信息</span><span>{{ getMerchantDisplayName(currentOrder) }}</span></div>
        <div class="detail-row"><span>物流公司</span><span>{{ currentOrder.logisticsCompany || '—' }}</span></div>
        <div class="detail-row"><span>物流单号</span><span>{{ currentOrder.logisticsNo || '—' }}</span></div>
        <div class="detail-row"><span>售后状态</span><span>{{ currentOrder.afterSaleInProgress ? '售后中' : '无售后' }}</span></div>

        <el-divider />
        <div class="detail-title">商品信息</div>
        <div v-for="item in currentOrderItems" :key="item.id" class="detail-item">
          <img v-if="item.productImage" :src="getImageUrl(item.productImage)" class="detail-item-img" />
          <div v-else class="detail-item-img empty">暂无</div>
          <div class="detail-item-meta">
            <div>{{ item.productName }}</div>
            <div class="sub">数量：{{ item.quantity }} / 单价：¥{{ formatMoney(item.price) }}</div>
          </div>
        </div>

        <el-divider />
        <div class="detail-title">操作日志</div>
        <div class="log-line">创建：{{ formatTime(currentOrder.createTime) || '—' }}</div>
        <div class="log-line">支付：{{ formatTime(currentOrder.paymentTime) || '—' }}</div>
        <div class="log-line">发货：{{ formatTime(currentOrder.shipTime) || '—' }}</div>
        <div class="log-line">完成：{{ formatTime(currentOrder.completeTime) || '—' }}</div>
        <div class="log-line">取消：{{ formatTime(currentOrder.cancelTime) || '—' }}</div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { Search } from '@element-plus/icons-vue'
import api from '@/utils/api'

const orders = ref([])
const merchantNameMap = ref({})
const activeTab = ref('')
const keyword = ref('')
const timeRange = ref([])
const merchantFilter = ref(null)
const groupByMerchant = ref(true)
const selectedOrders = ref([])
const sortState = ref({ prop: '', order: '' })

const detailVisible = ref(false)
const currentOrder = ref(null)
const currentOrderItems = ref([])

const getStatusText = (status) => ({ 0: '待支付', 1: '待发货', 2: '待收货', 3: '待评价', 4: '已完成', 5: '已取消' }[status] || '未知')
const getStatusClass = (status) => ({ 0: 'status-pay', 1: 'status-ship', 2: 'status-receive', 3: 'status-receive', 4: 'status-done', 5: 'status-cancel' }[status] || 'status-pay')

const formatTime = (time) => (time ? new Date(time).toLocaleString('zh-CN') : '')
const formatMoney = (value) => (value == null ? '0.00' : Number(value).toFixed(2))
const formatRelativeTime = (time) => {
  if (!time) return '—'
  const diff = Date.now() - new Date(time).getTime()
  const minute = 60 * 1000
  const hour = 60 * minute
  const day = 24 * hour
  if (diff < minute) return '刚刚'
  if (diff < hour) return `${Math.floor(diff / minute)} 分钟前`
  if (diff < day) return `${Math.floor(diff / hour)} 小时前`
  if (diff < 7 * day) return `${Math.floor(diff / day)} 天前`
  return new Date(time).toLocaleDateString('zh-CN')
}
const maskPhone = (phone) => (!phone ? '—' : phone.length < 7 ? phone : `${phone.slice(0, 3)}****${phone.slice(-4)}`)
const getImageUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http://') || url.startsWith('https://')) return url
  if (url.startsWith('/uploads/')) return '/api' + url
  if (url.startsWith('/api/uploads/')) return url
  return url
}

const getOrderTags = (o) => {
  const tags = []
  if (Number(o.payAmount || 0) >= 200) tags.push('大额订单')
  if ((o.remark || '').includes('加急')) tags.push('加急订单')
  if (o.afterSaleInProgress) tags.push('售后中')
  return tags
}

const getMerchantDisplayName = (entity) => {
  if (!entity) return '未知商家'
  if (entity.merchantName && String(entity.merchantName).trim()) return entity.merchantName
  const id = entity.merchantId
  if (id != null && merchantNameMap.value[id]) return merchantNameMap.value[id]
  return id != null ? `商家#${id}` : '未知商家'
}

const merchantOptions = computed(() => {
  const map = new Map()
  for (const o of orders.value) {
    const id = o.merchantId
    if (id == null) continue
    if (!map.has(id)) {
      map.set(id, getMerchantDisplayName(o))
    }
  }
  return Array.from(map.entries()).map(([value, label]) => ({ value, label }))
})

const filteredOrders = computed(() => {
  let list = [...orders.value]
  if (keyword.value) {
    const k = keyword.value.toLowerCase()
    list = list.filter((o) => String(o.orderNo || '').toLowerCase().includes(k) || String(o.userId || '').includes(k) || String(o.merchantId || '').includes(k))
  }
  if (timeRange.value && timeRange.value.length === 2) {
    const start = new Date(`${timeRange.value[0]} 00:00:00`).getTime()
    const end = new Date(`${timeRange.value[1]} 23:59:59`).getTime()
    list = list.filter((o) => {
      const t = o.createTime ? new Date(o.createTime).getTime() : 0
      return t >= start && t <= end
    })
  }
  if (merchantFilter.value != null) {
    list = list.filter((o) => o.merchantId === merchantFilter.value)
  }
  return list
})

const sortedOrders = computed(() => {
  const list = [...filteredOrders.value]
  const { prop, order } = sortState.value
  if (!prop || !order) {
    if (groupByMerchant.value) {
      return list.sort((a, b) => {
        const ma = String(getMerchantDisplayName(a))
        const mb = String(getMerchantDisplayName(b))
        if (ma !== mb) return ma.localeCompare(mb)
        return new Date(b.createTime || 0).getTime() - new Date(a.createTime || 0).getTime()
      })
    }
    return list
  }
  const factor = order === 'ascending' ? 1 : -1
  return list.sort((a, b) => {
    const va = a[prop]
    const vb = b[prop]
    if (va == null && vb == null) return 0
    if (va == null) return -1 * factor
    if (vb == null) return 1 * factor
    if (prop.toLowerCase().includes('time')) return (new Date(va).getTime() - new Date(vb).getTime()) * factor
    if (!Number.isNaN(Number(va)) && !Number.isNaN(Number(vb))) return (Number(va) - Number(vb)) * factor
    return String(va).localeCompare(String(vb)) * factor
  })
})

const currentPage = ref(1)
const pageSize = ref(10)

const pagedOrders = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return sortedOrders.value.slice(start, start + pageSize.value)
})

const loadOrders = async () => {
  try {
    const params = activeTab.value ? { status: parseInt(activeTab.value, 10) } : {}
    const res = await api.get('/admin/order/list', { params })
    if (res.code === 200) {
      const list = res.data || []
      // 前端兜底：如果后端未补齐商品字段，则补查一次订单项
      await Promise.all(list.map(async (order) => {
        if (order.firstProductName && order.totalQuantity != null) return
        try {
          const itemsRes = await api.get(`/order/items/${order.id}`)
          if (itemsRes.code === 200 && itemsRes.data && itemsRes.data.length > 0) {
            const items = itemsRes.data
            const first = items[0]
            if (!order.firstProductName) order.firstProductName = first.productName
            if (!order.firstProductImage) order.firstProductImage = first.productImage
            if (order.totalQuantity == null) {
              order.totalQuantity = items.reduce((sum, it) => sum + Number(it.quantity || 0), 0)
            }
          }
        } catch (e) {
          // ignore fallback error
        }
      }))
      orders.value = list
    }
  } catch (error) {
    console.error('加载订单失败', error)
  }
}

const loadMerchantNames = async () => {
  try {
    const res = await api.get('/admin/merchant/list')
    if (res.code === 200) {
      const map = {}
      for (const m of res.data || []) {
        map[m.id] = m.shopName || m.username || `商家#${m.id}`
      }
      merchantNameMap.value = map
    }
  } catch (e) {
    // ignore
  }
}

const resetFilters = () => { keyword.value = ''; timeRange.value = []; merchantFilter.value = null }
const handleSelectionChange = (rows) => { selectedOrders.value = rows }
const handleSortChange = ({ prop, order }) => { sortState.value = { prop, order } }

const getMerchantBadgeStyle = (row) => {
  const palette = [
    ['#E8F5E9', '#1B5E20'],
    ['#E3F2FD', '#0D47A1'],
    ['#FFF3E0', '#E65100'],
    ['#F3E5F5', '#4A148C'],
    ['#E0F2F1', '#004D40']
  ]
  const idx = Number(row.merchantId || 0) % palette.length
  return { background: palette[idx][0], color: palette[idx][1] }
}

const getRowClassName = ({ rowIndex }) => {
  if (!groupByMerchant.value || rowIndex === 0) return ''
  const prev = sortedOrders.value[rowIndex - 1]
  const cur = sortedOrders.value[rowIndex]
  return prev && cur && prev.merchantId !== cur.merchantId ? 'merchant-group-start' : ''
}

const viewDetail = async (order) => {
  currentOrder.value = order
  detailVisible.value = true
  try {
    const res = await api.get(`/order/items/${order.id}`)
    currentOrderItems.value = res.code === 200 ? (res.data || []) : []
  } catch (error) {
    currentOrderItems.value = []
  }
}

const exportCsv = () => {
  const headers = ['订单号', '商品名称', '用户ID', '商家ID', '收货人', '收货电话', '金额', '状态', '下单时间']
  const rows = filteredOrders.value.map((o) => [
    o.orderNo,
    o.firstProductName || '',
    o.userId,
    o.merchantId,
    o.receiverName || '',
    o.receiverPhone || '',
    o.payAmount ?? 0,
    getStatusText(o.orderStatus),
    formatTime(o.createTime)
  ])
  const csv = [headers, ...rows].map((r) => r.map((cell) => `"${String(cell ?? '').replace(/"/g, '""')}"`).join(',')).join('\n')
  const blob = new Blob(['\uFEFF' + csv], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `订单导出_${new Date().toISOString().slice(0, 10)}.csv`
  a.click()
  URL.revokeObjectURL(url)
}

onMounted(async () => {
  await loadMerchantNames()
  await loadOrders()
})
</script>

<style scoped>
.orders-page { padding: 24px; background: #f5f7fa; min-height: calc(100vh - 60px); }
.orders-card { border: none; border-radius: 8px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.card-title { font-size: 18px; font-weight: 600; color: #2c3e50; }
.card-subtitle { margin-top: 4px; color: #7f8c8d; font-size: 13px; }
.search-input { width: 320px; }
.filters { display: flex; gap: 8px; align-items: center; margin-bottom: 10px; }
.merchant-filter { width: 180px; }
.status-tabs { margin-bottom: 8px; }
:deep(.status-tabs .el-tabs__item) { color: #7f8c8d; }
:deep(.status-tabs .el-tabs__item.is-active) { color: #2ecc71; font-weight: 600; }
:deep(.status-tabs .el-tabs__active-bar) { background: #2ecc71; }
.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.toolbar-left { color: #7f8c8d; }
.toolbar-right { display: flex; gap: 8px; flex-wrap: wrap; }
.btn-cancel-link { color: #e74c3c; }
.product-cell { display: flex; align-items: center; }
.thumb-wrap { width: 40px; height: 40px; border-radius: 4px; overflow: hidden; background: #f8f9fa; margin-right: 10px; display: flex; align-items: center; justify-content: center; }
.thumb { width: 100%; height: 100%; object-fit: cover; }
.thumb-empty { font-size: 12px; color: #bdc3c7; }
.product-name { color: #2c3e50; font-weight: 550; font-size: 14px; }
.product-sub { color: #7f8c8d; font-size: 12px; margin-top: 2px; }
.merchant-badge { display: inline-flex; align-items: center; padding: 4px 10px; border-radius: 12px; font-size: 12px; font-weight: 600; }
.amount { font-weight: 600; color: #2c3e50; font-size: 14px; }
.status-pill { border-radius: 16px; padding: 4px 12px; font-size: 12px; display: inline-flex; }
.status-done { background: #d4edda; color: #155724; }
.status-pay { background: #fff3e0; color: #f57c00; }
.status-ship { background: #e3f2fd; color: #0288d1; }
.status-receive { background: #f3e5f5; color: #8e24aa; }
.status-cancel { background: #f8d7da; color: #721c24; }
.orders-page :deep(.el-table__cell) { padding-top: 10px; padding-bottom: 10px; font-size: 13px; }
.detail-wrap { padding: 6px 0; }
.detail-row { display: flex; justify-content: space-between; margin: 7px 0; color: #2c3e50; gap: 8px; }
.detail-title { font-weight: 600; color: #2c3e50; margin-bottom: 8px; }
.detail-item { display: flex; align-items: center; margin-bottom: 10px; }
.detail-item-img { width: 44px; height: 44px; border-radius: 4px; object-fit: cover; margin-right: 10px; background: #f8f9fa; }
.detail-item-img.empty { display: flex; align-items: center; justify-content: center; color: #bdc3c7; font-size: 12px; }
.detail-item-meta .sub { color: #7f8c8d; font-size: 12px; }
.log-line { color: #7f8c8d; font-size: 13px; margin: 4px 0; }
:deep(.el-table__header-wrapper th) { background: #eaecef; color: #2c3e50; font-weight: 600; }
:deep(.el-table__row:hover) { background-color: #ecf0f1 !important; }
:deep(.merchant-group-start td) { border-top: 2px solid #b7e4c7 !important; }
@media (max-width: 768px) {
  .card-header, .toolbar, .filters { flex-direction: column; align-items: flex-start; }
  .search-input { width: 100%; }
}
</style>

