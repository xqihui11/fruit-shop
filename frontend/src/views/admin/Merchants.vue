<template>
  <div class="merchants-page">
    <el-card class="merchants-card" shadow="never">
      <!-- 标题 & 搜索 -->
      <template #header>
        <div class="card-header">
          <div class="card-title-wrap">
            <div class="card-title">商家管理</div>
            <div class="card-subtitle">审核与管理平台商家账号、店铺及经营情况</div>
          </div>
          <div class="card-actions">
            <el-input
              v-model="keyword"
              class="search-input"
              placeholder="搜索用户名 / 店铺名称 / 联系人"
              clearable
              @input="handleSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </div>
        </div>
      </template>

      <!-- 状态标签页 -->
      <el-tabs v-model="activeTab" @tab-change="handleTabChange" class="status-tabs">
        <el-tab-pane label="全部" name=""></el-tab-pane>
        <el-tab-pane label="待审核" name="0"></el-tab-pane>
        <el-tab-pane label="已通过" name="1"></el-tab-pane>
        <el-tab-pane label="已拒绝" name="2"></el-tab-pane>
        <el-tab-pane label="已禁用" name="3"></el-tab-pane>
      </el-tabs>

      <!-- 批量操作栏 -->
      <div class="toolbar">
        <div class="toolbar-left">
          <span class="toolbar-text">
            已选中
            <strong>{{ selectedMerchants.length }}</strong>
            家商户
          </span>
        </div>
        <div class="toolbar-right">
          <el-button
            class="toolbar-btn"
            :disabled="selectedMerchants.length === 0"
            @click="handleBatchApprove"
          >
            ✅ 批量通过
          </el-button>
          <el-button
            class="toolbar-btn danger"
            :disabled="selectedMerchants.length === 0"
            @click="handleBatchReject"
          >
            ❌ 批量拒绝
          </el-button>
          <el-button
            class="toolbar-btn warning"
            :disabled="selectedMerchants.length === 0"
            @click="handleBatchDisable"
          >
            🚫 批量禁用
          </el-button>
        </div>
      </div>

      <!-- 商家表格 -->
      <el-table
        :data="pagedMerchants"
        stripe
        class="merchants-table"
        @selection-change="handleSelectionChange"
        @sort-change="handleSortChange"
      >
        <el-table-column type="selection" width="48" />
        <el-table-column prop="id" label="ID" width="80" sortable="custom" />

        <el-table-column label="用户 / 店铺" min-width="260" sortable="custom" prop="shopName">
          <template #default="{ row }">
            <div class="merchant-cell">
              <el-avatar
                :size="32"
                :src="getAvatarUrl(row.avatar)"
                class="merchant-avatar"
              >
                {{ (row.shopName || row.username || '').charAt(0).toUpperCase() }}
              </el-avatar>
              <div class="merchant-meta">
                <div class="merchant-shop">{{ row.shopName || '未填写店铺名称' }}</div>
                <div class="merchant-username">{{ row.username }}</div>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="联系人" prop="contactName" min-width="120" />

        <el-table-column label="联系电话" min-width="150">
          <template #default="{ row }">
            <el-tooltip :content="row.contactPhone || '未填写电话'" placement="top">
              <span>{{ maskPhone(row.contactPhone) }}</span>
            </el-tooltip>
          </template>
        </el-table-column>

        <el-table-column
          label="注册时间"
          prop="registerTime"
          min-width="160"
          sortable="custom"
        >
          <template #default="{ row }">
            <el-tooltip :content="formatTime(row.registerTime)" placement="top">
              <span>{{ formatRelativeTime(row.registerTime) }}</span>
            </el-tooltip>
          </template>
        </el-table-column>

        <el-table-column
          label="最后活跃"
          prop="lastLoginTime"
          min-width="160"
          sortable="custom"
        >
          <template #default="{ row }">
            <template v-if="row.lastLoginTime">
              <el-tooltip :content="formatTime(row.lastLoginTime)" placement="top">
                <span>{{ formatRelativeTime(row.lastLoginTime) }}</span>
              </el-tooltip>
            </template>
            <template v-else>
              <span style="color: #BDC3C7;">从未有订单</span>
            </template>
          </template>
        </el-table-column>

        <el-table-column label="商品数" prop="productCount" width="100" sortable="custom">
          <template #default="{ row }">
            <span>{{ formatNumber(row.productCount) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="商家标签" min-width="180">
          <template #default="{ row }">
            <el-space wrap size="4">
              <el-tag
                v-for="tag in getMerchantTags(row)"
                :key="tag"
                size="small"
                effect="plain"
                class="merchant-tag"
              >
                {{ tag }}
              </el-tag>
            </el-space>
          </template>
        </el-table-column>

        <el-table-column label="状态" prop="status" width="120">
          <template #default="{ row }">
            <span class="status-pill" :class="getStatusClass(row.status)">
              {{ getStatusText(row.status) }}
            </span>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-tooltip content="查看详情" placement="top">
              <el-button link type="primary" @click="showDetail(row)">
                ℹ️ 详情
              </el-button>
            </el-tooltip>

            <template v-if="row.status === 0">
              <el-tooltip content="通过审核" placement="top">
                <el-button link type="success" @click="auditMerchant(row.id, true)">
                  ✅ 通过
                </el-button>
              </el-tooltip>
              <el-tooltip content="拒绝审核" placement="top">
                <el-button link type="danger" @click="auditMerchant(row.id, false)">
                  ❌ 拒绝
                </el-button>
              </el-tooltip>
            </template>
            <template v-else-if="row.status === 1">
              <el-tooltip content="禁用商家" placement="top">
                <el-button link type="danger" @click="updateStatus(row.id, 3)">
                  🚫 禁用
                </el-button>
              </el-tooltip>
            </template>
            <template v-else-if="row.status === 3">
              <el-tooltip content="启用商家" placement="top">
                <el-button link type="success" @click="updateStatus(row.id, 1)">
                  ✅ 启用
                </el-button>
              </el-tooltip>
            </template>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <div class="pagination-wrap">
      <el-pagination
        background
        layout="prev, pager, next"
        :current-page="currentPage"
        :page-size="pageSize"
        :total="sortedMerchants.length"
        @current-change="(p) => (currentPage = p)"
      />
    </div>

    <!-- 商家详情抽屉 -->
    <el-drawer
      v-model="detailVisible"
      size="480px"
      title="商家详情"
      direction="rtl"
    >
      <div v-if="currentMerchant" class="detail-body">
        <div class="detail-header">
          <el-avatar
            :size="56"
            :src="getAvatarUrl(currentMerchant.avatar)"
            class="detail-avatar"
          >
            {{ (currentMerchant.shopName || currentMerchant.username || '').charAt(0).toUpperCase() }}
          </el-avatar>
          <div class="detail-main">
            <div class="detail-name-row">
              <span class="detail-shop">{{ currentMerchant.shopName || '未填写店铺名称' }}</span>
              <span class="status-pill" :class="getStatusClass(currentMerchant.status)">
                {{ getStatusText(currentMerchant.status) }}
              </span>
            </div>
            <div class="detail-username">
              登录账号：{{ currentMerchant.username }}
            </div>
            <div class="detail-tags">
              <el-tag
                v-for="tag in getMerchantTags(currentMerchant)"
                :key="tag"
                size="small"
                effect="plain"
                class="merchant-tag"
              >
                {{ tag }}
              </el-tag>
            </div>
          </div>
        </div>

        <el-divider />

        <div class="detail-section">
          <div class="detail-section-title">账号与联系信息</div>
          <div class="detail-row">
            <span class="detail-label">联系人</span>
            <span class="detail-value">{{ currentMerchant.contactName || '未填写' }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">联系电话</span>
            <span class="detail-value">{{ currentMerchant.contactPhone || '未填写' }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">联系邮箱</span>
            <span class="detail-value">{{ currentMerchant.contactEmail || '未填写' }}</span>
          </div>
        </div>

        <div class="detail-section">
          <div class="detail-section-title">店铺信息</div>
          <div class="detail-row">
            <span class="detail-label">店铺描述</span>
            <span class="detail-value">
              {{ currentMerchant.shopDescription || '暂无描述' }}
            </span>
          </div>
          <div class="detail-row">
            <span class="detail-label">店铺地址</span>
            <span class="detail-value">
              {{ currentMerchant.shopAddress || '未填写' }}
            </span>
          </div>
          <div class="detail-row">
            <span class="detail-label">营业时间</span>
            <span class="detail-value">
              {{ currentMerchant.businessHours || '未设置' }}
            </span>
          </div>
        </div>

        <div class="detail-section">
          <div class="detail-section-title">审核与时间</div>
          <div class="detail-row">
            <span class="detail-label">注册时间</span>
            <span class="detail-value">
              {{ formatTime(currentMerchant.registerTime) || '—' }}
            </span>
          </div>
          <div class="detail-row">
            <span class="detail-label">审核时间</span>
            <span class="detail-value">
              {{ formatTime(currentMerchant.auditTime) || '—' }}
            </span>
          </div>
          <div class="detail-row">
            <span class="detail-label">最后活跃</span>
            <span class="detail-value">
              <template v-if="currentMerchant.lastLoginTime">
                <el-tooltip :content="formatTime(currentMerchant.lastLoginTime)" placement="top">
                  <span>{{ formatRelativeTime(currentMerchant.lastLoginTime) }}</span>
                </el-tooltip>
              </template>
              <template v-else>从未有订单</template>
            </span>
          </div>
        </div>

        <div class="detail-section">
          <div class="detail-section-title">经营概况</div>
          <div class="detail-row">
            <span class="detail-label">商品数</span>
            <span class="detail-value">
              {{ formatNumber(currentMerchant.productCount) }}
            </span>
          </div>
          <div class="detail-row">
            <span class="detail-label">订单数</span>
            <span class="detail-value">
              {{ formatNumber(currentMerchant.orderCount) }}
            </span>
          </div>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import api from '@/utils/api'

const merchants = ref([])
const activeTab = ref('')
const keyword = ref('')
const selectedMerchants = ref([])
const sortState = ref({ prop: 'id', order: null })
const currentPage = ref(1)
const pageSize = ref(10)

const detailVisible = ref(false)
const currentMerchant = ref(null)

const handleTabChange = () => {
  loadMerchants()
}

const filteredMerchants = computed(() => {
  let list = [...merchants.value]

  // 关键字搜索
  if (keyword.value) {
    const k = keyword.value.toLowerCase()
    list = list.filter((m) => {
      return (
        (m.username && m.username.toLowerCase().includes(k)) ||
        (m.shopName && m.shopName.toLowerCase().includes(k)) ||
        (m.contactName && m.contactName.toLowerCase().includes(k))
      )
    })
  }

  return list
})

const sortedMerchants = computed(() => {
  const { prop, order } = sortState.value
  let list = [...filteredMerchants.value]
  
  // 在"全部"标签页中，优先按状态分组：已通过(1)在前，其他状态在后
  if (activeTab.value === '') {
    list = list.sort((a, b) => {
      // 已通过(status=1)排在前面
      if (a.status === 1 && b.status !== 1) return -1
      if (a.status !== 1 && b.status === 1) return 1
      // 同组内按注册时间倒序（新注册的在前）
      if (a.registerTime && b.registerTime) {
        return new Date(b.registerTime).getTime() - new Date(a.registerTime).getTime()
      }
      return 0
    })
  }

  // 如果用户点击了表头排序，则按用户选择的排序方式
  if (prop && order) {
    const factor = order === 'ascending' ? 1 : -1
    return list.sort((a, b) => {
      const va = a[prop]
      const vb = b[prop]
      if (va == null && vb == null) return 0
      if (va == null) return -1 * factor
      if (vb == null) return 1 * factor
      if (prop.toLowerCase().includes('time')) {
        return (new Date(va).getTime() - new Date(vb).getTime()) * factor
      }
      if (typeof va === 'number' && typeof vb === 'number') {
        return (va - vb) * factor
      }
      return String(va).localeCompare(String(vb)) * factor
    })
  }

  return list
})

const pagedMerchants = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return sortedMerchants.value.slice(start, start + pageSize.value)
})

const handleSearch = () => {
  // 搜索逻辑在 computed 中实现，此方法为未来扩展预留
}

const getAvatarUrl = (avatar) => {
  if (!avatar) return ''
  if (avatar.startsWith('http://') || avatar.startsWith('https://')) {
    return avatar
  }
  if (avatar.startsWith('/uploads/')) {
    return '/api' + avatar
  }
  if (avatar.startsWith('/api/uploads/')) {
    return avatar
  }
  return avatar
}

const getStatusText = (status) => {
  const map = {
    0: '待审核',
    1: '已通过',
    2: '已拒绝',
    3: '已禁用'
  }
  return map[status] || '未知'
}

const getStatusClass = (status) => {
  switch (status) {
    case 1:
      return 'status-approved'
    case 2:
      return 'status-rejected'
    case 3:
      return 'status-disabled'
    case 0:
    default:
      return 'status-pending'
  }
}

const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN')
}

const formatRelativeTime = (time) => {
  if (!time) return ''
  const t = new Date(time).getTime()
  const now = Date.now()
  const diff = now - t
  if (diff < 0) return '刚刚'

  const minute = 60 * 1000
  const hour = 60 * minute
  const day = 24 * hour

  if (diff < minute) return '刚刚'
  if (diff < hour) return `${Math.floor(diff / minute)} 分钟前`
  if (diff < day) return `${Math.floor(diff / hour)} 小时前`
  if (diff < 7 * day) return `${Math.floor(diff / day)} 天前`
  return new Date(time).toLocaleDateString('zh-CN')
}

const maskPhone = (phone) => {
  if (!phone) return '—'
  if (phone.length < 7) return phone
  return `${phone.slice(0, 3)}****${phone.slice(-4)}`
}

// 格式化数字显示：如果值为 null/undefined 显示 '--'，否则显示实际数字（包括0）
const formatNumber = (num) => {
  if (num === null || num === undefined) return '--'
  return num.toString()
}

const getMerchantTags = (merchant) => {
  const tags = []
  if (!merchant) return tags

  // 新商家：注册 7 天内
  if (merchant.registerTime) {
    const days =
      (Date.now() - new Date(merchant.registerTime).getTime()) /
      (24 * 60 * 60 * 1000)
    if (days <= 7) tags.push('新商家')
  }

  // 优质商家：状态已通过，且商品数较多
  if (merchant.status === 1 && typeof merchant.productCount === 'number') {
    if (merchant.productCount >= 10) {
      tags.push('优质商家')
    }
  }

  // 高销量商家：订单数较多（如果后端提供）
  const orderCount = merchant.orderCount
  if (typeof orderCount === 'number' && orderCount >= 50) {
    tags.push('高销量商家')
  }

  return tags
}

const loadMerchants = async () => {
  try {
    const params = activeTab.value ? { status: parseInt(activeTab.value) } : {}
    const res = await api.get('/admin/merchant/list', { params })
    if (res.code === 200) {
      merchants.value = res.data || []
    }
  } catch (error) {
    console.error('加载商家失败', error)
  }
}

const auditMerchant = async (id, approve) => {
  try {
    await ElMessageBox.confirm(
      approve
        ? '确定要通过该商家的入驻申请吗？'
        : '确定要拒绝该商家的入驻申请吗？',
      '审核确认',
      { type: 'warning' }
    )
    const res = await api.post(`/admin/merchant/audit/${id}`, { approve })
    if (res.code === 200) {
      ElMessage.success(approve ? '审核通过' : '审核拒绝')
      loadMerchants()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    // 用户取消
  }
}

const showDetail = (merchant) => {
  currentMerchant.value = merchant
  detailVisible.value = true
}

const updateStatus = async (id, status) => {
  try {
    await ElMessageBox.confirm(
      status === 3 ? '确定要禁用该商家吗？' : '确定要启用该商家吗？',
      '操作确认',
      { type: 'warning' }
    )
    const res = await api.post(`/admin/merchant/status/${id}`, { status })
    if (res.code === 200) {
      ElMessage.success(status === 3 ? '商家已成功禁用' : '商家已成功启用')
      loadMerchants()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    // 用户取消
  }
}

const handleSelectionChange = (selection) => {
  selectedMerchants.value = selection
}

const handleSortChange = ({ prop, order }) => {
  sortState.value = { prop, order }
}

const handleBatchApprove = async () => {
  const list = selectedMerchants.value.filter((m) => m.status === 0)
  if (!list.length) {
    ElMessage.info('当前选中的商家中没有待审核的')
    return
  }
  try {
    await ElMessageBox.confirm(
      `确认要通过选中的 ${list.length} 个商家的入驻申请吗？`,
      '批量审核确认',
      { type: 'warning' }
    )
    await Promise.all(
      list.map((m) => api.post(`/admin/merchant/audit/${m.id}`, { approve: true }))
    )
    ElMessage.success('已通过选中的商家')
    loadMerchants()
  } catch (error) {
    // 取消或异常
  }
}

const handleBatchReject = async () => {
  const list = selectedMerchants.value.filter((m) => m.status === 0)
  if (!list.length) {
    ElMessage.info('当前选中的商家中没有待审核的')
    return
  }
  try {
    await ElMessageBox.confirm(
      `确认要拒绝选中的 ${list.length} 个商家的入驻申请吗？`,
      '批量审核确认',
      { type: 'warning' }
    )
    await Promise.all(
      list.map((m) => api.post(`/admin/merchant/audit/${m.id}`, { approve: false }))
    )
    ElMessage.success('已拒绝选中的商家')
    loadMerchants()
  } catch (error) {
    // 取消或异常
  }
}

const handleBatchDisable = async () => {
  const list = selectedMerchants.value.filter((m) => m.status === 1)
  if (!list.length) {
    ElMessage.info('当前选中的商家中没有已通过的商家')
    return
  }
  try {
    await ElMessageBox.confirm(
      `确认要禁用选中的 ${list.length} 个商家吗？`,
      '批量禁用确认',
      { type: 'warning' }
    )
    await Promise.all(
      list.map((m) => api.post(`/admin/merchant/status/${m.id}`, { status: 3 }))
    )
    ElMessage.success('已禁用选中的商家')
    loadMerchants()
  } catch (error) {
    // 取消或异常
  }
}

onMounted(() => {
  loadMerchants()
})
</script>

<style scoped>
.merchants-page {
  padding: 24px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.merchants-card {
  border-radius: 8px;
  border: none;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 4px 0;
}

.card-title-wrap {
  display: flex;
  flex-direction: column;
}

.card-title {
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
}

.card-subtitle {
  margin-top: 4px;
  font-size: 13px;
  color: #7f8c8d;
}

.card-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.search-input {
  width: 280px;
}

.status-tabs {
  margin-top: 4px;
  margin-bottom: 8px;
}

:deep(.status-tabs .el-tabs__header) {
  margin-bottom: 8px;
  border-bottom: 1px solid #e0e0e0;
}

:deep(.status-tabs .el-tabs__item) {
  font-size: 14px;
  color: #7f8c8d;
}

:deep(.status-tabs .el-tabs__item.is-active) {
  color: #2ecc71;
  font-weight: 600;
}

:deep(.status-tabs .el-tabs__active-bar) {
  background-color: #2ecc71;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.toolbar-text {
  font-size: 13px;
  color: #7f8c8d;
}

.toolbar-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.toolbar-btn {
  border-radius: 20px;
}

.toolbar-btn.danger {
  color: #e74c3c;
  border-color: #e74c3c;
}

.toolbar-btn.warning {
  color: #f57c00;
  border-color: #f57c00;
}

.merchants-table {
  --el-table-header-bg-color: #eaecef;
}

.merchant-cell {
  display: flex;
  align-items: center;
}

.merchant-avatar {
  margin-right: 10px;
}

.merchant-meta {
  display: flex;
  flex-direction: column;
}

.merchant-shop {
  font-weight: 500;
  color: #2c3e50;
}

.merchant-username {
  font-size: 12px;
  color: #7f8c8d;
}

.status-pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 4px 12px;
  border-radius: 16px;
  font-size: 12px;
  font-weight: 500;
}

.status-approved {
  background: #d4edda;
  color: #155724;
}

.status-rejected {
  background: #f8d7da;
  color: #721c24;
}

.status-disabled {
  background: #fff3e0;
  color: #f57c00;
}

.status-pending {
  background: #e3f2fd;
  color: #0288d1;
}

.merchant-tag {
  border-radius: 12px;
}

.detail-body {
  padding: 4px 0;
}

.detail-header {
  display: flex;
  align-items: center;
}

.detail-avatar {
  margin-right: 16px;
}

.detail-main {
  flex: 1;
}

.detail-name-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.detail-shop {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
}

.detail-username {
  margin-top: 4px;
  font-size: 13px;
  color: #7f8c8d;
}

.detail-tags {
  margin-top: 8px;
}

.detail-section {
  margin-top: 16px;
}

.detail-section-title {
  margin-bottom: 8px;
  font-size: 14px;
  font-weight: 600;
  color: #2c3e50;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 6px;
  font-size: 13px;
}

.detail-label {
  color: #7f8c8d;
}

.detail-value {
  color: #2c3e50;
  text-align: right;
}

/* 表格 hover 效果 */
:deep(.el-table__header-wrapper th) {
  background: #eaecef;
  color: #2c3e50;
  font-weight: 600;
}

:deep(.el-table__row) {
  transition: background-color 0.2s ease;
}

:deep(.el-table__row:hover) {
  background-color: #ecf0f1 !important;
}

.pagination-wrap {
  margin-top: 16px;
  text-align: right;
}
</style>


