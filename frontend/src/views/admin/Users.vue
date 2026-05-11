<template>
  <div class="users-page">
    <el-card class="users-card" shadow="never">
      <!-- 标题与搜索 -->
      <template #header>
        <div class="card-header">
          <div class="card-title-wrap">
            <div class="card-title">用户管理</div>
            <div class="card-subtitle">管理平台用户账号、状态与活跃度</div>
          </div>
          <div class="card-actions">
            <el-input
              v-model="keyword"
              class="search-input"
              placeholder="搜索用户名 / 手机号"
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

      <!-- 筛选 & 批量操作栏 -->
      <div class="toolbar">
        <div class="toolbar-left">
          <el-select v-model="statusFilter" placeholder="按状态筛选" clearable class="toolbar-item">
            <el-option label="全部状态" :value="null" />
            <el-option label="正常" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
          <el-date-picker
            v-model="registerRange"
            type="daterange"
            range-separator="至"
            start-placeholder="注册开始日期"
            end-placeholder="注册结束日期"
            value-format="YYYY-MM-DD"
            class="toolbar-item"
          />
        </div>
        <div class="toolbar-right">
          <el-button
            class="toolbar-btn"
            :disabled="selectedUsers.length === 0"
            @click="handleBatchEnable"
          >
            启用所选
          </el-button>
          <el-button
            class="toolbar-btn danger"
            :disabled="selectedUsers.length === 0"
            @click="handleBatchDisable"
          >
            禁用所选
          </el-button>
        </div>
      </div>

      <!-- 用户表格 -->
      <el-table
        :data="pagedUsers"
        stripe
        class="users-table"
        @selection-change="handleSelectionChange"
        @sort-change="handleSortChange"
      >
        <el-table-column type="selection" width="48" />
        <el-table-column prop="id" label="ID" width="80" sortable="custom" />

        <el-table-column label="用户" min-width="220">
          <template #default="{ row }">
            <div class="user-cell">
              <el-avatar
                :size="32"
                :src="getAvatarUrl(row.avatar)"
                class="user-avatar"
              >
                {{ (row.nickname || row.username || '').charAt(0).toUpperCase() }}
              </el-avatar>
              <div class="user-meta">
                <div class="user-name">{{ row.username }}</div>
                <div class="user-nickname" v-if="row.nickname">{{ row.nickname }}</div>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="手机号" min-width="160">
          <template #default="{ row }">
            <el-tooltip :content="row.phone || '未填写手机号'" placement="top">
              <span>{{ maskPhone(row.phone) }}</span>
            </el-tooltip>
          </template>
        </el-table-column>

        <el-table-column label="注册时间" prop="registerTime" min-width="160" sortable="custom">
          <template #default="{ row }">
            <el-tooltip :content="formatTime(row.registerTime)" placement="top">
              <span>{{ formatRelativeTime(row.registerTime) }}</span>
            </el-tooltip>
          </template>
        </el-table-column>

        <el-table-column label="最后登录" prop="lastLoginTime" min-width="160" sortable="custom">
          <template #default="{ row }">
            <el-tooltip :content="formatTime(row.lastLoginTime)" placement="top">
              <span>{{ formatRelativeTime(row.lastLoginTime) || '从未登录' }}</span>
            </el-tooltip>
          </template>
        </el-table-column>

        <el-table-column label="订单数" prop="orderCount" width="100" sortable="custom">
          <template #default="{ row }">
            <span>{{ row.orderCount ?? '--' }}</span>
          </template>
        </el-table-column>

        <el-table-column label="用户标签" min-width="180">
          <template #default="{ row }">
            <el-space wrap size="4">
              <el-tag
                v-for="tag in getUserTags(row)"
                :key="tag"
                size="small"
                effect="plain"
                class="user-tag"
              >
                {{ tag }}
              </el-tag>
            </el-space>
          </template>
        </el-table-column>

        <el-table-column label="状态" prop="status" width="110">
          <template #default="{ row }">
            <span
              class="status-pill"
              :class="row.status === 1 ? 'status-normal' : 'status-disabled'"
            >
              {{ row.status === 1 ? '正常' : '禁用' }}
            </span>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-tooltip content="查看详情" placement="top">
              <el-button
                link
                type="primary"
                @click="showDetail(row)"
              >
                ℹ️ 详情
              </el-button>
            </el-tooltip>
            <el-tooltip :content="row.status === 1 ? '禁用用户' : '启用用户'" placement="top">
              <el-button
                link
                :type="row.status === 1 ? 'danger' : 'success'"
                @click="updateStatus(row.id, row.status === 1 ? 0 : 1)"
              >
                <span v-if="row.status === 1">🚫 禁用</span>
                <span v-else>✅ 启用</span>
              </el-button>
            </el-tooltip>
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
        :total="sortedUsers.length"
        @current-change="(p) => (currentPage = p)"
      />
    </div>

    <!-- 用户详情抽屉 -->
    <el-drawer
      v-model="detailVisible"
      size="420px"
      title="用户详情"
      direction="rtl"
    >
      <div v-if="currentUser" class="detail-body">
        <div class="detail-header">
          <el-avatar :size="56" :src="getAvatarUrl(currentUser.avatar)" class="detail-avatar">
            {{ (currentUser.nickname || currentUser.username || '').charAt(0).toUpperCase() }}
          </el-avatar>
          <div class="detail-main">
            <div class="detail-name-row">
              <span class="detail-name">{{ currentUser.username }}</span>
              <span
                class="status-pill"
                :class="currentUser.status === 1 ? 'status-normal' : 'status-disabled'"
              >
                {{ currentUser.status === 1 ? '正常' : '禁用' }}
              </span>
            </div>
            <div class="detail-nickname" v-if="currentUser.nickname">
              昵称：{{ currentUser.nickname }}
            </div>
            <div class="detail-tags">
              <el-tag
                v-for="tag in getUserTags(currentUser)"
                :key="tag"
                size="small"
                effect="plain"
                class="user-tag"
              >
                {{ tag }}
              </el-tag>
            </div>
          </div>
        </div>

        <el-divider />

        <div class="detail-section">
          <div class="detail-section-title">账号信息</div>
          <div class="detail-row">
            <span class="detail-label">手机号</span>
            <span class="detail-value">{{ currentUser.phone || '未填写' }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">邮箱</span>
            <span class="detail-value">{{ currentUser.email || '未填写' }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">注册时间</span>
            <span class="detail-value">{{ formatTime(currentUser.registerTime) || '—' }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">最后登录</span>
            <span class="detail-value">
              {{ formatTime(currentUser.lastLoginTime) || '从未登录' }}
            </span>
          </div>
        </div>

        <div class="detail-section">
          <div class="detail-section-title">订单与价值</div>
          <div class="detail-row">
            <span class="detail-label">订单数</span>
            <span class="detail-value">{{ currentUser.orderCount ?? '--' }}</span>
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

const users = ref([])
const keyword = ref('')
const statusFilter = ref(null)
const registerRange = ref([])
const selectedUsers = ref([])
const sortState = ref({ prop: 'id', order: null })
const currentPage = ref(1)
const pageSize = ref(10)

const detailVisible = ref(false)
const currentUser = ref(null)

const filteredUsers = computed(() => {
  let list = [...users.value]

  // 关键字搜索
  if (keyword.value) {
    const k = keyword.value.toLowerCase()
    list = list.filter(user =>
      (user.username && user.username.toLowerCase().includes(k)) ||
      (user.phone && user.phone.includes(k)) ||
      (user.nickname && user.nickname.toLowerCase().includes(k))
    )
  }

  // 状态筛选
  if (statusFilter.value !== null) {
    list = list.filter(user => user.status === statusFilter.value)
  }

  // 注册时间范围
  if (registerRange.value && registerRange.value.length === 2) {
    const [start, end] = registerRange.value
    const startTime = new Date(start).getTime()
    const endTime = new Date(end).getTime() + 24 * 60 * 60 * 1000 - 1
    list = list.filter(user => {
      if (!user.registerTime) return false
      const t = new Date(user.registerTime).getTime()
      return t >= startTime && t <= endTime
    })
  }

  return list
})

const sortedUsers = computed(() => {
  const { prop, order } = sortState.value
  const list = [...filteredUsers.value]
  if (!prop || !order) return list

  const factor = order === 'ascending' ? 1 : -1
  return list.sort((a, b) => {
    const va = a[prop]
    const vb = b[prop]
    if (va == null && vb == null) return 0
    if (va == null) return -1 * factor
    if (vb == null) return 1 * factor
    if (va instanceof Date || vb instanceof Date || prop.toLowerCase().includes('time')) {
      return (new Date(va).getTime() - new Date(vb).getTime()) * factor
    }
    if (typeof va === 'number' && typeof vb === 'number') {
      return (va - vb) * factor
    }
    return String(va).localeCompare(String(vb)) * factor
  })
})

const pagedUsers = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return sortedUsers.value.slice(start, start + pageSize.value)
})

const handleSearch = () => {
  // 搜索逻辑已在 computed 中实现，此方法保留用于未来扩展为后端搜索
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

const getUserTags = (user) => {
  const tags = []
  if (!user) return tags

  // 新用户：注册 7 天内
  if (user.registerTime) {
    const days = (Date.now() - new Date(user.registerTime).getTime()) / (24 * 60 * 60 * 1000)
    if (days <= 7) tags.push('新用户')
  }

  // 活跃用户：3 天内登录
  if (user.lastLoginTime) {
    const days = (Date.now() - new Date(user.lastLoginTime).getTime()) / (24 * 60 * 60 * 1000)
    if (days <= 3) tags.push('活跃用户')
  }

  // 高价值：订单数较多（预留字段）
  const orderCount = user.orderCount ?? user.totalOrders
  if (typeof orderCount === 'number' && orderCount >= 10) {
    tags.push('高价值用户')
  }

  return tags
}

const loadUsers = async () => {
  try {
    const res = await api.get('/admin/user/list')
    if (res.code === 200) {
      users.value = res.data || []
    }
  } catch (error) {
    console.error('加载用户失败', error)
  }
}

const updateStatus = async (id, status) => {
  try {
    await ElMessageBox.confirm(
      status === 0 ? '确认要禁用该用户吗？' : '确认要启用该用户吗？',
      '操作确认',
      { type: 'warning' }
    )
    const res = await api.post(`/admin/user/status/${id}`, { status })
    if (res.code === 200) {
      ElMessage.success(status === 0 ? '用户已成功禁用' : '用户已成功启用')
      loadUsers()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    // 用户取消
  }
}

const handleSelectionChange = (selection) => {
  selectedUsers.value = selection
}

const handleSortChange = ({ prop, order }) => {
  sortState.value = { prop, order }
}

const handleBatchEnable = async () => {
  const ids = selectedUsers.value.map(u => u.id)
  if (!ids.length) return
  try {
    await ElMessageBox.confirm(
      `确认要启用选中的 ${ids.length} 个用户吗？`,
      '批量操作确认',
      { type: 'warning' }
    )
    await Promise.all(ids.map(id => api.post(`/admin/user/status/${id}`, { status: 1 })))
    ElMessage.success('已启用所选用户')
    loadUsers()
  } catch (error) {
    // 取消或异常
  }
}

const handleBatchDisable = async () => {
  const ids = selectedUsers.value.map(u => u.id)
  if (!ids.length) return
  try {
    await ElMessageBox.confirm(
      `确认要禁用选中的 ${ids.length} 个用户吗？`,
      '批量操作确认',
      { type: 'warning' }
    )
    await Promise.all(ids.map(id => api.post(`/admin/user/status/${id}`, { status: 0 })))
    ElMessage.success('已禁用所选用户')
    loadUsers()
  } catch (error) {
    // 取消或异常
  }
}

const showDetail = (user) => {
  currentUser.value = user
  detailVisible.value = true
}

onMounted(() => {
  loadUsers()
})
</script>

<style scoped>
.users-page {
  padding: 24px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.users-card {
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
  width: 260px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.toolbar-left,
.toolbar-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.toolbar-item {
  min-width: 180px;
}

.toolbar-btn {
  border-radius: 20px;
}

.toolbar-btn.danger {
  color: #e74c3c;
  border-color: #e74c3c;
}

.users-table {
  --el-table-header-bg-color: #eaecef;
}

.user-cell {
  display: flex;
  align-items: center;
}

.user-avatar {
  margin-right: 10px;
}

.user-meta {
  display: flex;
  flex-direction: column;
}

.user-name {
  font-weight: 500;
  color: #2c3e50;
}

.user-nickname {
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

.status-normal {
  background: #d4edda;
  color: #155724;
}

.status-disabled {
  background: #fff3e0;
  color: #f57c00;
}

.user-tag {
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

.detail-name {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
}

.detail-nickname {
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
}

/* 表格行 hover 效果与表头样式 */
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

