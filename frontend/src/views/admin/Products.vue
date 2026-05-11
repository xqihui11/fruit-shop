<template>
  <div class="products-page">
    <el-card class="products-card" shadow="never">
      <template #header>
        <div class="card-header">
          <div class="card-title-wrap">
            <div class="card-title">商品管理</div>
            <div class="card-subtitle">审核与管理平台商品状态、库存及热度表现</div>
          </div>
          <div class="card-actions">
            <el-input
              v-model="keyword"
              class="search-input"
              placeholder="搜索商品名称"
              clearable
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </div>
        </div>
      </template>

      <div class="filters">
        <el-input-number v-model="minPrice" :min="0" :precision="2" placeholder="最低价" />
        <span class="filter-sep">-</span>
        <el-input-number v-model="maxPrice" :min="0" :precision="2" placeholder="最高价" />
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

      <el-tabs v-model="activeTab" @tab-change="handleTabChange" class="status-tabs">
        <el-tab-pane label="全部" name="" />
        <el-tab-pane label="待审核" name="0" />
        <el-tab-pane label="已上架" name="1" />
        <el-tab-pane label="已下架" name="2" />
        <el-tab-pane label="审核拒绝" name="3" />
      </el-tabs>

      <div class="toolbar">
        <div class="toolbar-left">已选中 <strong>{{ selectedProducts.length }}</strong> 个商品</div>
        <div class="toolbar-right">
          <el-button :disabled="!selectedProducts.length" @click="handleBatchApprove">✔️ 批量审核通过</el-button>
          <el-button :disabled="!selectedProducts.length" class="btn-danger" @click="handleBatchReject">❌ 批量审核拒绝</el-button>
          <el-button :disabled="!selectedProducts.length" class="btn-success" @click="handleBatchUp">✅ 批量上架</el-button>
          <el-button :disabled="!selectedProducts.length" class="btn-warning" @click="handleBatchDown">📤 批量下架</el-button>
          <el-button :disabled="!filteredProducts.length" @click="exportCsv">导出</el-button>
        </div>
      </div>

      <el-table
        :data="pagedProducts"
        stripe
        class="products-table"
        :row-class-name="getRowClassName"
        @selection-change="handleSelectionChange"
        @sort-change="handleSortChange"
      >
        <el-table-column type="selection" width="48" />
        <el-table-column prop="id" label="ID" width="80" />

        <el-table-column label="商品" min-width="300">
          <template #default="{ row }">
            <div class="product-cell">
              <div class="thumb-wrap">
                <img v-if="row.mainImage" :src="getImageUrl(row.mainImage)" class="thumb" />
                <div v-else class="thumb-empty">无图</div>
              </div>
              <div class="product-meta">
                <div class="product-name">{{ row.name }}</div>
                <div class="product-merchant">所属商家：{{ getMerchantDisplayName(row) }}</div>
                <div class="tag-row">
                  <el-tag v-for="tag in getProductTags(row)" :key="tag" size="small" effect="plain">{{ tag }}</el-tag>
                </div>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="price" label="价格" width="120" sortable="custom" align="right">
          <template #default="{ row }">
            <span class="price-cell">¥{{ formatMoney(row.price) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="所属商家" min-width="150">
          <template #default="{ row }">
            <span class="merchant-badge" :style="getMerchantBadgeStyle(row)">
              {{ getMerchantDisplayName(row) }}
            </span>
          </template>
        </el-table-column>

        <el-table-column prop="stock" label="库存" width="100" sortable="custom">
          <template #default="{ row }">
            <span :class="{ 'stock-low': Number(row.stock || 0) < 10 }">{{ row.stock ?? 0 }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="salesCount" label="销量" width="100" sortable="custom">
          <template #default="{ row }">{{ row.salesCount ?? 0 }}</template>
        </el-table-column>

        <el-table-column prop="reviewCount" label="评价数" width="100" sortable="custom">
          <template #default="{ row }">{{ row.reviewCount ?? 0 }}</template>
        </el-table-column>

        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <span class="status-pill" :class="getStatusClass(row.status)">{{ getStatusText(row.status) }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="createTime" label="创建时间" width="160" sortable="custom">
          <template #default="{ row }">
            <el-tooltip :content="formatTime(row.createTime)" placement="top">
              <span>{{ formatRelativeTime(row.createTime) }}</span>
            </el-tooltip>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="showDetail(row)">ℹ️ 详情</el-button>
            <template v-if="row.status === 0">
              <el-button link type="success" @click="auditProduct(row.id, true)">✔️ 通过</el-button>
              <el-button link type="danger" @click="auditProduct(row.id, false)">❌ 拒绝</el-button>
            </template>
            <template v-else-if="row.status === 1">
              <el-button link type="warning" @click="updateStatus(row.id, 2)">📤 下架</el-button>
            </template>
            <template v-else-if="row.status === 2 || row.status === 3">
              <el-button link type="success" @click="updateStatus(row.id, 1)">✅ 上架</el-button>
            </template>
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
        :total="sortedProducts.length"
        @current-change="(p) => currentPage = p"
      />
    </div>

    <el-drawer v-model="detailVisible" title="商品详情" size="520px" direction="rtl">
      <div v-if="currentProduct" class="detail-wrap">
        <div class="detail-top">
          <img v-if="currentProduct.mainImage" :src="getImageUrl(currentProduct.mainImage)" class="detail-img" />
          <div v-else class="detail-img-empty">暂无图片</div>
          <div class="detail-meta">
            <div class="detail-name">{{ currentProduct.name }}</div>
            <span class="status-pill" :class="getStatusClass(currentProduct.status)">{{ getStatusText(currentProduct.status) }}</span>
            <div class="detail-merchant">所属商家：{{ getMerchantDisplayName(currentProduct) }}</div>
          </div>
        </div>

        <el-divider />

        <div class="detail-row"><span>价格</span><span>¥{{ formatMoney(currentProduct.price) }}</span></div>
        <div class="detail-row"><span>库存</span><span>{{ currentProduct.stock ?? 0 }}</span></div>
        <div class="detail-row"><span>销量</span><span>{{ currentProduct.salesCount ?? 0 }}</span></div>
        <div class="detail-row"><span>评价数</span><span>{{ currentProduct.reviewCount ?? 0 }}</span></div>
        <div class="detail-row"><span>创建时间</span><span>{{ formatTime(currentProduct.createTime) || '—' }}</span></div>
        <div class="detail-row"><span>审核时间</span><span>{{ formatTime(currentProduct.auditTime) || '—' }}</span></div>
        <div class="detail-row"><span>审核备注</span><span>{{ currentProduct.auditRemark || '无' }}</span></div>

        <el-divider />
        <div class="detail-desc-title">商品描述</div>
        <div class="detail-desc">{{ currentProduct.description || '暂无描述' }}</div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import api from '@/utils/api'

const products = ref([])
const merchantNameMap = ref({})
const activeTab = ref('')
const keyword = ref('')
const minPrice = ref(null)
const maxPrice = ref(null)
const merchantFilter = ref(null)
const groupByMerchant = ref(true)
const selectedProducts = ref([])
const sortState = ref({ prop: '', order: '' })

const detailVisible = ref(false)
const currentProduct = ref(null)

const getStatusText = (status) => ({ 0: '待审核', 1: '已上架', 2: '已下架', 3: '审核拒绝' }[status] || '未知')
const getStatusClass = (status) => ({ 0: 'status-pending', 1: 'status-up', 2: 'status-down', 3: 'status-reject' }[status] || 'status-pending')

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

const getImageUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http://') || url.startsWith('https://')) return url
  if (url.startsWith('/uploads/')) return '/api' + url
  if (url.startsWith('/api/uploads/')) return url
  return url
}

const getProductTags = (p) => {
  const tags = []
  const stock = Number(p.stock || 0)
  const sales = Number(p.salesCount || 0)
  if (p.createTime && Date.now() - new Date(p.createTime).getTime() <= 7 * 24 * 3600 * 1000) tags.push('新品')
  if (sales >= 100) tags.push('热销')
  if (stock < 10) tags.push('库存预警')
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
  for (const p of products.value) {
    const id = p.merchantId
    if (id == null) continue
    if (!map.has(id)) map.set(id, getMerchantDisplayName(p))
  }
  return Array.from(map.entries()).map(([value, label]) => ({ value, label }))
})

const filteredProducts = computed(() => {
  let list = [...products.value]
  if (keyword.value) {
    const k = keyword.value.toLowerCase()
    list = list.filter((p) => (p.name || '').toLowerCase().includes(k))
  }
  if (minPrice.value != null) list = list.filter((p) => Number(p.price || 0) >= Number(minPrice.value))
  if (maxPrice.value != null) list = list.filter((p) => Number(p.price || 0) <= Number(maxPrice.value))
  if (merchantFilter.value != null) list = list.filter((p) => p.merchantId === merchantFilter.value)
  return list
})

const sortedProducts = computed(() => {
  const list = [...filteredProducts.value]
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

const pagedProducts = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return sortedProducts.value.slice(start, start + pageSize.value)
})

const loadProducts = async () => {
  try {
    const params = activeTab.value ? { status: parseInt(activeTab.value, 10) } : {}
    const res = await api.get('/admin/product/list', { params })
    if (res.code === 200) products.value = res.data || []
  } catch (error) {
    console.error('加载商品失败', error)
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

const handleTabChange = () => loadProducts()
const handleSelectionChange = (rows) => { selectedProducts.value = rows }
const handleSortChange = ({ prop, order }) => { sortState.value = { prop, order } }
const resetFilters = () => { keyword.value = ''; minPrice.value = null; maxPrice.value = null; merchantFilter.value = null }
const showDetail = (p) => { currentProduct.value = p; detailVisible.value = true }

const getMerchantBadgeStyle = (row) => {
  const palette = [
    ['#E8F5E9', '#1B5E20'],
    ['#E3F2FD', '#0D47A1'],
    ['#FFF3E0', '#E65100'],
    ['#F3E5F5', '#4A148C'],
    ['#E0F2F1', '#004D40']
  ]
  const idx = Number(row.merchantId || 0) % palette.length
  return {
    background: palette[idx][0],
    color: palette[idx][1]
  }
}

const getRowClassName = ({ rowIndex }) => {
  if (!groupByMerchant.value || rowIndex === 0) return ''
  const prev = sortedProducts.value[rowIndex - 1]
  const cur = sortedProducts.value[rowIndex]
  return prev && cur && prev.merchantId !== cur.merchantId ? 'merchant-group-start' : ''
}

const updateStatus = async (id, status) => {
  try {
    await ElMessageBox.confirm(status === 1 ? '确认要上架该商品吗？' : '确认要下架该商品吗？', '操作确认', { type: 'warning' })
    const res = await api.post(`/admin/product/status/${id}`, { status })
    if (res.code === 200) {
      ElMessage.success(status === 1 ? '商品已成功上架' : '商品已成功下架')
      loadProducts()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    // cancel
  }
}

const auditProduct = async (id, approve) => {
  try {
    let remark = ''
    if (!approve) {
      const { value } = await ElMessageBox.prompt('请输入拒绝原因', '审核确认', {
        inputPlaceholder: '请输入拒绝原因',
        inputValidator: (v) => !!v || '请输入拒绝原因'
      })
      remark = value || ''
    } else {
      await ElMessageBox.confirm('确认要审核通过该商品吗？', '审核确认', { type: 'warning' })
    }
    const res = await api.post(`/admin/product/audit/${id}`, { approve, remark })
    if (res.code === 200) {
      ElMessage.success(approve ? '商品已审核通过' : '商品已审核拒绝')
      loadProducts()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    // cancel
  }
}

const handleBatchApprove = async () => {
  const targets = selectedProducts.value.filter((p) => p.status === 0)
  if (!targets.length) return ElMessage.info('仅待审核商品可批量通过')
  try {
    await ElMessageBox.confirm(`确认批量审核通过 ${targets.length} 个商品吗？`, '批量确认', { type: 'warning' })
    await Promise.all(targets.map((p) => api.post(`/admin/product/audit/${p.id}`, { approve: true, remark: '' })))
    ElMessage.success('批量审核通过完成')
    loadProducts()
  } catch (error) {}
}

const handleBatchReject = async () => {
  const targets = selectedProducts.value.filter((p) => p.status === 0)
  if (!targets.length) return ElMessage.info('仅待审核商品可批量拒绝')
  try {
    const { value } = await ElMessageBox.prompt('请输入批量拒绝原因', '批量确认', {
      inputPlaceholder: '请输入拒绝原因',
      inputValidator: (v) => !!v || '请输入拒绝原因'
    })
    await Promise.all(targets.map((p) => api.post(`/admin/product/audit/${p.id}`, { approve: false, remark: value || '' })))
    ElMessage.success('批量审核拒绝完成')
    loadProducts()
  } catch (error) {}
}

const handleBatchUp = async () => {
  const targets = selectedProducts.value.filter((p) => p.status === 2 || p.status === 3)
  if (!targets.length) return ElMessage.info('仅已下架或审核拒绝商品可批量上架')
  try {
    await ElMessageBox.confirm(`确认批量上架 ${targets.length} 个商品吗？`, '批量确认', { type: 'warning' })
    await Promise.all(targets.map((p) => api.post(`/admin/product/status/${p.id}`, { status: 1 })))
    ElMessage.success('批量上架完成')
    loadProducts()
  } catch (error) {}
}

const handleBatchDown = async () => {
  const targets = selectedProducts.value.filter((p) => p.status === 1)
  if (!targets.length) return ElMessage.info('仅已上架商品可批量下架')
  try {
    await ElMessageBox.confirm(`确认批量下架 ${targets.length} 个商品吗？`, '批量确认', { type: 'warning' })
    await Promise.all(targets.map((p) => api.post(`/admin/product/status/${p.id}`, { status: 2 })))
    ElMessage.success('批量下架完成')
    loadProducts()
  } catch (error) {}
}

const exportCsv = () => {
  const headers = ['ID', '商品名称', '所属商家', '价格', '库存', '销量', '评价数', '状态', '创建时间']
  const rows = filteredProducts.value.map((p) => [
    p.id,
    p.name || '',
    getMerchantDisplayName(p),
    p.price ?? 0,
    p.stock ?? 0,
    p.salesCount ?? 0,
    p.reviewCount ?? 0,
    getStatusText(p.status),
    formatTime(p.createTime)
  ])
  const csv = [headers, ...rows].map((r) => r.map((cell) => `"${String(cell).replace(/"/g, '""')}"`).join(',')).join('\n')
  const blob = new Blob(['\uFEFF' + csv], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `商品管理导出_${new Date().toISOString().slice(0, 10)}.csv`
  a.click()
  URL.revokeObjectURL(url)
}

onMounted(async () => {
  await loadMerchantNames()
  await loadProducts()
})
</script>

<style scoped>
.products-page { padding: 24px; background: #f5f7fa; min-height: calc(100vh - 60px); }
.products-card { border: none; border-radius: 8px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.card-title { font-size: 18px; font-weight: 600; color: #2c3e50; }
.card-subtitle { margin-top: 4px; color: #7f8c8d; font-size: 13px; }
.search-input { width: 280px; }
.filters { display: flex; align-items: center; gap: 8px; margin-bottom: 10px; }
.filter-sep { color: #bdc3c7; }
.merchant-filter { width: 180px; }
.status-tabs { margin-bottom: 8px; }
:deep(.status-tabs .el-tabs__item) { color: #7f8c8d; }
:deep(.status-tabs .el-tabs__item.is-active) { color: #2ecc71; font-weight: 600; }
:deep(.status-tabs .el-tabs__active-bar) { background: #2ecc71; }
.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.toolbar-left { color: #7f8c8d; }
.toolbar-right { display: flex; gap: 8px; flex-wrap: wrap; }
.btn-danger { color: #e74c3c; border-color: #e74c3c; }
.btn-success { color: #2ecc71; border-color: #2ecc71; }
.btn-warning { color: #f57c00; border-color: #f57c00; }
.product-cell { display: flex; align-items: center; }
.thumb-wrap { width: 40px; height: 40px; border-radius: 4px; overflow: hidden; background: #f8f9fa; margin-right: 10px; display: flex; align-items: center; justify-content: center; }
.thumb { width: 100%; height: 100%; object-fit: cover; }
.thumb-empty { font-size: 11px; color: #bdc3c7; }
.product-name { color: #2c3e50; font-weight: 500; }
.product-merchant { color: #7f8c8d; font-size: 12px; margin-top: 2px; }
.merchant-badge { display: inline-flex; align-items: center; padding: 4px 10px; border-radius: 12px; font-size: 12px; font-weight: 600; }
.tag-row { margin-top: 4px; display: flex; gap: 4px; flex-wrap: wrap; }
.price-cell { font-weight: 600; color: #2c3e50; }
.stock-low { color: #e74c3c; font-weight: 600; }
.status-pill { border-radius: 16px; padding: 4px 12px; font-size: 12px; display: inline-flex; }
.status-up { background: #d4edda; color: #155724; }
.status-down { background: #fff3e0; color: #f57c00; }
.status-pending { background: #e3f2fd; color: #0288d1; }
.status-reject { background: #f8d7da; color: #721c24; }
.detail-wrap { padding: 6px 0; }
.detail-top { display: flex; gap: 12px; }
.detail-img, .detail-img-empty { width: 90px; height: 90px; border-radius: 8px; background: #f8f9fa; display: flex; align-items: center; justify-content: center; object-fit: cover; color: #bdc3c7; }
.detail-name { font-size: 18px; font-weight: 600; color: #2c3e50; margin-bottom: 6px; }
.detail-merchant { color: #7f8c8d; margin-top: 8px; }
.detail-row { display: flex; justify-content: space-between; margin: 8px 0; color: #2c3e50; }
.detail-desc-title { font-weight: 600; color: #2c3e50; margin-bottom: 8px; }
.detail-desc { background: #f8f9fa; border-radius: 8px; padding: 12px; color: #7f8c8d; line-height: 1.6; }
:deep(.el-table__header-wrapper th) { background: #eaecef; color: #2c3e50; font-weight: 600; }
:deep(.el-table__row:hover) { background-color: #ecf0f1 !important; }
:deep(.merchant-group-start td) { border-top: 2px solid #b7e4c7 !important; }
@media (max-width: 768px) {
  .card-header, .toolbar { flex-direction: column; align-items: flex-start; gap: 10px; }
  .search-input { width: 100%; }
}
</style>

