<template>
  <div class="announcements-page">
    <el-card class="announcements-card" shadow="never">
      <template #header>
        <div class="card-header">
          <div class="card-title-wrap">
            <div class="card-title">公告管理</div>
            <div class="card-subtitle">发布和管理平台公告，及时通知用户和商家重要信息</div>
          </div>
          <div class="card-actions">
            <el-button type="primary" class="publish-btn" @click="showDialog()">
              <el-icon><Plus /></el-icon>
              发布公告
            </el-button>
          </div>
        </div>
      </template>

      <!-- 状态标签页 -->
      <el-tabs v-model="activeTab" @tab-change="handleTabChange" class="status-tabs">
        <el-tab-pane label="全部" name=""></el-tab-pane>
        <el-tab-pane label="已发布" name="1"></el-tab-pane>
        <el-tab-pane label="草稿" name="0"></el-tab-pane>
        <el-tab-pane label="已下线" name="2"></el-tab-pane>
      </el-tabs>

      <!-- 搜索和筛选工具栏 -->
      <div class="toolbar">
        <div class="search-section">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索公告标题或内容..."
            clearable
            class="search-input"
            @input="handleSearch"
            @clear="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>
        <div class="filter-section">
          <el-button 
            :icon="Filter" 
            @click="showFilterDialog = true"
            :type="hasActiveFilters ? 'primary' : 'default'"
          >
            筛选
            <el-badge v-if="hasActiveFilters" :value="activeFilterCount" class="filter-badge" />
          </el-button>
          <el-button 
            v-if="hasActiveFilters" 
            @click="resetFilters"
            :icon="Refresh"
          >
            重置
          </el-button>
        </div>
      </div>

      <!-- 批量操作栏 -->
      <div v-if="selectedAnnouncements.length > 0" class="batch-actions">
        <span class="selected-count">已选择 {{ selectedAnnouncements.length }} 项</span>
        <el-button size="small" @click="batchUnpublish">批量下线</el-button>
        <el-button size="small" type="danger" @click="batchDelete">批量删除</el-button>
        <el-button size="small" @click="batchExport">批量导出</el-button>
        <el-button size="small" @click="clearSelection">取消选择</el-button>
      </div>

      <!-- 筛选对话框 -->
      <el-dialog v-model="showFilterDialog" title="筛选公告" width="500px">
        <el-form :model="filterForm" label-width="100px">
          <el-form-item label="公告类型">
            <el-radio-group v-model="filterForm.type">
              <el-radio :label="null">全部</el-radio>
              <el-radio :label="1">系统公告</el-radio>
              <el-radio :label="2">活动公告</el-radio>
              <el-radio :label="3">维护公告</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="目标用户">
            <el-radio-group v-model="filterForm.target">
              <el-radio :label="null">全部</el-radio>
              <el-radio :label="0">全部</el-radio>
              <el-radio :label="1">用户</el-radio>
              <el-radio :label="2">商家</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="时间范围">
            <el-date-picker
              v-model="filterForm.dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="是否置顶">
            <el-radio-group v-model="filterForm.isTop">
              <el-radio :label="null">全部</el-radio>
              <el-radio :label="1">仅置顶</el-radio>
              <el-radio :label="0">非置顶</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="排序方式">
            <el-radio-group v-model="filterForm.sortBy">
              <el-radio label="time">按时间排序</el-radio>
              <el-radio label="top">置顶优先</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="showFilterDialog = false">取消</el-button>
          <el-button type="primary" @click="applyFilters">确定</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </template>
      </el-dialog>

      <!-- 公告表格 -->
      <el-table 
        ref="tableRef"
        :data="pagedAnnouncements" 
        stripe 
        class="announcements-table"
        @selection-change="handleSelectionChange"
        @sort-change="handleSort"
        :row-class-name="getRowClassName"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" sortable="custom" />
        <el-table-column label="公告标题" min-width="280" sortable="custom" :sort-by="'title'">
          <template #default="{ row }">
            <div class="announcement-title-cell">
              <span class="announcement-icon">{{ getTypeIcon(row.type) }}</span>
              <div class="announcement-title-content">
                <div class="announcement-title-row">
                  <span v-if="row.isTop === 1" class="top-badge">置</span>
                  <span class="announcement-title-text">{{ row.title }}</span>
                </div>
                <div class="announcement-tags">
                  <el-tag v-if="row.isTop === 1" type="danger" size="small" class="tag-top">重要公告</el-tag>
                </div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="公告类型" width="130" sortable="custom" :sort-by="'type'">
          <template #default="{ row }">
            <el-tag 
              :type="getTypeTagType(row.type)" 
              :class="['type-tag', `type-tag-${row.type}`]"
            >
              {{ getTypeText(row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="目标用户" width="120">
          <template #default="{ row }">
            <div class="target-user-cell">
              <span class="target-icon">{{ getTargetIcon(row.target) }}</span>
              <span>{{ getTargetText(row.target) }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="发布时间" width="180" sortable="custom" :sort-by="'publishTime'">
          <template #default="{ row }">
            <div class="publish-time-cell">
              <div v-if="row.publishTime" class="relative-time" :title="formatTime(row.publishTime)">
                {{ getRelativeTime(row.publishTime) }}
              </div>
              <div v-else class="not-published">未发布</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="阅读统计" width="120">
          <template #default="{ row }">
            <div class="read-stats">
              <el-icon><View /></el-icon>
              <span>{{ row.readCount || 0 }} 人</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <span class="status-pill" :class="getStatusClass(row.status)">
              <span class="status-dot"></span>
              {{ getStatusText(row.status) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button link type="primary" @click="showPreview(row)" class="action-btn">
                <el-icon><View /></el-icon>
                预览
              </el-button>
              <el-button link type="primary" @click="showDialog(row)" class="action-btn">
                <el-icon><Edit /></el-icon>
                编辑
              </el-button>
              <el-button 
                v-if="row.status === 0"
                link 
                type="success"
                @click="publishAnnouncement(row.id)"
                class="action-btn"
              >
                <el-icon><Upload /></el-icon>
                发布
              </el-button>
              <el-button 
                v-if="row.status === 1"
                link 
                type="warning"
                @click="unpublishAnnouncement(row.id)"
                class="action-btn"
              >
                <el-icon><Download /></el-icon>
                下线
              </el-button>
              <el-button link type="danger" @click="deleteAnnouncement(row.id)" class="action-btn">
                <el-icon><Delete /></el-icon>
                删除
              </el-button>
            </div>
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
        :total="filteredAnnouncements.length"
        @current-change="(p) => (currentPage = p)"
      />
    </div>

    <!-- 预览侧边栏 -->
    <el-drawer
      v-model="previewVisible"
      title="公告预览"
      direction="rtl"
      size="500px"
    >
      <div class="preview-content" v-if="previewAnnouncement">
        <div class="preview-header">
          <h2>{{ previewAnnouncement.title }}</h2>
          <div class="preview-meta">
            <el-tag :type="getTypeTagType(previewAnnouncement.type)" size="small">
              {{ getTypeText(previewAnnouncement.type) }}
            </el-tag>
            <span class="preview-time">{{ formatTime(previewAnnouncement.publishTime) }}</span>
          </div>
        </div>
        <div class="preview-body" v-html="formatContent(previewAnnouncement.content)"></div>
      </div>
    </el-drawer>

    <!-- 编辑抽屉 -->
    <el-drawer
      v-model="drawerVisible"
      :title="editingAnnouncement ? '编辑公告' : '发布公告'"
      direction="rtl"
      size="700px"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
        <el-form-item label="公告标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入公告标题" />
        </el-form-item>
        <el-form-item label="公告内容" prop="content">
          <el-input 
            v-model="form.content" 
            type="textarea" 
            :rows="10"
            placeholder="请输入公告内容，支持换行"
          />
        </el-form-item>
        <el-form-item label="公告类型" prop="type">
          <el-radio-group v-model="form.type">
            <el-radio :label="1">系统公告</el-radio>
            <el-radio :label="2">活动公告</el-radio>
            <el-radio :label="3">维护公告</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="目标用户" prop="target">
          <el-radio-group v-model="form.target">
            <el-radio :label="0">全部</el-radio>
            <el-radio :label="1">用户</el-radio>
            <el-radio :label="2">商家</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="是否置顶">
          <el-switch v-model="form.isTop" :active-value="1" :inactive-value="0" />
          <span style="margin-left: 12px; color: #7f8c8d; font-size: 12px;">置顶公告将优先显示</span>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="0">草稿</el-radio>
            <el-radio :label="1">已发布</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="drawerVisible = false">取消</el-button>
        <el-button type="primary" @click="saveAnnouncement">保存</el-button>
      </template>
    </el-drawer>

    <!-- 编辑对话框（保留作为备用） -->
    <el-dialog v-model="dialogVisible" :title="editingAnnouncement ? '编辑公告' : '发布公告'" width="700px" style="display: none;">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="公告标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入公告标题" />
        </el-form-item>
        <el-form-item label="公告内容" prop="content">
          <el-input 
            v-model="form.content" 
            type="textarea" 
            :rows="8"
            placeholder="请输入公告内容"
          />
        </el-form-item>
        <el-form-item label="公告类型" prop="type">
          <el-radio-group v-model="form.type">
            <el-radio :label="1">系统公告</el-radio>
            <el-radio :label="2">活动公告</el-radio>
            <el-radio :label="3">维护公告</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="目标用户" prop="target">
          <el-radio-group v-model="form.target">
            <el-radio :label="0">全部</el-radio>
            <el-radio :label="1">用户</el-radio>
            <el-radio :label="2">商家</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="是否置顶">
          <el-switch v-model="form.isTop" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="0">草稿</el-radio>
            <el-radio :label="1">已发布</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveAnnouncement">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Filter, Refresh, Plus, Edit, Delete, Upload, Download, View } from '@element-plus/icons-vue'
import api from '@/utils/api'

const announcements = ref([])
const activeTab = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const dialogVisible = ref(false)
const drawerVisible = ref(false)
const previewVisible = ref(false)
const editingAnnouncement = ref(null)
const previewAnnouncement = ref(null)
const formRef = ref(null)
const tableRef = ref(null)
const searchKeyword = ref('')
const showFilterDialog = ref(false)
const selectedAnnouncements = ref([])
const sortConfig = ref({ prop: null, order: null })
const filterForm = ref({
  type: null,
  target: null,
  dateRange: null,
  isTop: null,
  sortBy: 'top'
})

const form = ref({
  title: '',
  content: '',
  type: 1,
  target: 0,
  isTop: 0,
  status: 0
})

const rules = {
  title: [{ required: true, message: '请输入公告标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入公告内容', trigger: 'blur' }],
  type: [{ required: true, message: '请选择公告类型', trigger: 'change' }],
  target: [{ required: true, message: '请选择目标用户', trigger: 'change' }]
}

const hasActiveFilters = computed(() => {
  return filterForm.value.type !== null || 
         filterForm.value.target !== null ||
         filterForm.value.dateRange !== null || 
         filterForm.value.isTop !== null ||
         searchKeyword.value.trim() !== ''
})

const activeFilterCount = computed(() => {
  let count = 0
  if (filterForm.value.type !== null) count++
  if (filterForm.value.target !== null) count++
  if (filterForm.value.dateRange !== null) count++
  if (filterForm.value.isTop !== null) count++
  if (searchKeyword.value.trim() !== '') count++
  return count
})

const filteredAnnouncements = computed(() => {
  let list = [...announcements.value]
  
  // 按状态过滤（标签页）
  if (activeTab.value !== '') {
    list = list.filter(a => a.status === parseInt(activeTab.value))
  }
  
  // 关键词搜索（搜索标题和内容）
  if (searchKeyword.value.trim()) {
    const keyword = searchKeyword.value.trim().toLowerCase()
    list = list.filter(a => {
      const title = (a.title || '').toLowerCase()
      const content = (a.content || '').replace(/<[^>]*>/g, '').toLowerCase()
      return title.includes(keyword) || content.includes(keyword)
    })
  }
  
  // 公告类型筛选
  if (filterForm.value.type !== null) {
    list = list.filter(a => a.type === filterForm.value.type)
  }
  
  // 目标用户筛选
  if (filterForm.value.target !== null) {
    list = list.filter(a => a.target === filterForm.value.target)
  }
  
  // 时间范围筛选
  if (filterForm.value.dateRange && filterForm.value.dateRange.length === 2) {
    const [startDate, endDate] = filterForm.value.dateRange
    const start = new Date(startDate).getTime()
    const end = new Date(endDate).getTime() + 24 * 60 * 60 * 1000 - 1 // 包含结束日期当天
    list = list.filter(a => {
      const time = a.publishTime || a.createTime
      if (!time) return false
      const itemTime = new Date(time).getTime()
      return itemTime >= start && itemTime <= end
    })
  }
  
  // 置顶筛选
  if (filterForm.value.isTop !== null) {
    list = list.filter(a => a.isTop === filterForm.value.isTop)
  }
  
  // 排序
  if (sortConfig.value.prop && sortConfig.value.order) {
    // 表头排序优先
    const prop = sortConfig.value.prop
    const order = sortConfig.value.order === 'ascending' ? 1 : -1
    
    list.sort((a, b) => {
      let aVal, bVal
      switch (prop) {
        case 'id':
          aVal = a.id || 0
          bVal = b.id || 0
          break
        case 'title':
          aVal = (a.title || '').toLowerCase()
          bVal = (b.title || '').toLowerCase()
          break
        case 'type':
          aVal = a.type || 0
          bVal = b.type || 0
          break
        case 'publishTime':
          aVal = new Date(a.publishTime || 0).getTime()
          bVal = new Date(b.publishTime || 0).getTime()
          break
        default:
          return 0
      }
      
      if (aVal < bVal) return -1 * order
      if (aVal > bVal) return 1 * order
      return 0
    })
  } else if (filterForm.value.sortBy === 'top') {
    // 置顶优先，然后按创建时间倒序
    list.sort((a, b) => {
      if (a.isTop !== b.isTop) return b.isTop - a.isTop
      return new Date(b.createTime || 0).getTime() - new Date(a.createTime || 0).getTime()
    })
  } else {
    // 仅按创建时间倒序
    list.sort((a, b) => {
      return new Date(b.createTime || 0).getTime() - new Date(a.createTime || 0).getTime()
    })
  }
  
  return list
})

const pagedAnnouncements = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return filteredAnnouncements.value.slice(start, start + pageSize.value)
})

const getStatusText = (status) => {
  const map = { 0: '草稿', 1: '已发布', 2: '已下线' }
  return map[status] || '未知'
}

const getStatusClass = (status) => {
  const map = { 0: 'status-draft', 1: 'status-published', 2: 'status-offline' }
  return map[status] || ''
}

const getTypeText = (type) => {
  const map = { 1: '系统公告', 2: '活动公告', 3: '维护公告' }
  return map[type] || '未知'
}

const getTypeTagType = (type) => {
  // 系统公告用浅蓝，活动公告用浅橙，维护公告用浅紫
  const map = { 1: 'info', 2: 'warning', 3: '' }
  return map[type] || ''
}

const getTargetText = (target) => {
  const map = { 0: '全部', 1: '用户', 2: '商家' }
  return map[target] || '未知'
}

const getTargetIcon = (target) => {
  const map = { 0: '👥', 1: '👤', 2: '🏪' }
  return map[target] || '👥'
}

const getTypeIcon = (type) => {
  const map = { 1: '📢', 2: '🎉', 3: '🔧' }
  return map[type] || '📢'
}

const formatTime = (time) => {
  if (!time) return '—'
  return new Date(time).toLocaleString('zh-CN')
}

const getRelativeTime = (time) => {
  if (!time) return '未发布'
  const now = new Date()
  const publish = new Date(time)
  const diff = now.getTime() - publish.getTime()
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60))
  
  if (days === 0) {
    if (hours === 0) return '刚刚'
    return `今天 ${publish.getHours().toString().padStart(2, '0')}:${publish.getMinutes().toString().padStart(2, '0')}`
  }
  if (days === 1) return '昨天'
  if (days <= 7) return `${days}天前`
  return formatTime(time)
}

const formatContent = (content) => {
  if (!content) return ''
  return content.replace(/\n/g, '<br>')
}

const getRowClassName = ({ row, rowIndex }) => {
  return 'table-row'
}

const handleSelectionChange = (selection) => {
  selectedAnnouncements.value = selection
}

const clearSelection = () => {
  selectedAnnouncements.value = []
  if (tableRef.value) {
    tableRef.value.clearSelection()
  }
}

const handleSort = ({ prop, order }) => {
  sortConfig.value = { prop, order }
  // 重新应用排序
  if (prop && order) {
    // 排序逻辑已在 filteredAnnouncements 中处理
  }
}

const showPreview = (announcement) => {
  previewAnnouncement.value = announcement
  previewVisible.value = true
}

const batchUnpublish = async () => {
  if (selectedAnnouncements.value.length === 0) return
  
  try {
    await ElMessageBox.confirm(
      `确认要批量下线这 ${selectedAnnouncements.value.length} 条公告吗？下线后用户将无法查看`,
      '批量下线确认',
      { type: 'warning' }
    )
    
    const promises = selectedAnnouncements.value
      .filter(a => a.status === 1)
      .map(a => api.post(`/admin/announcement/unpublish/${a.id}`))
    
    await Promise.all(promises)
    ElMessage.success('批量下线成功')
    clearSelection()
    loadAnnouncements()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '操作失败')
    }
  }
}

const batchDelete = async () => {
  if (selectedAnnouncements.value.length === 0) return
  
  try {
    await ElMessageBox.confirm(
      `确认要删除这 ${selectedAnnouncements.value.length} 条公告吗？此操作不可恢复！`,
      '批量删除确认',
      { type: 'warning' }
    )
    
    const promises = selectedAnnouncements.value.map(a =>
      api.delete(`/admin/announcement/delete/${a.id}`)
    )
    await Promise.all(promises)
    ElMessage.success('批量删除成功')
    clearSelection()
    loadAnnouncements()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '删除失败')
    }
  }
}

const batchExport = () => {
  if (selectedAnnouncements.value.length === 0) {
    ElMessage.warning('请先选择要导出的公告')
    return
  }
  
  // 导出功能（可以导出为CSV或JSON）
  const data = selectedAnnouncements.value.map(a => ({
    ID: a.id,
    标题: a.title,
    类型: getTypeText(a.type),
    目标用户: getTargetText(a.target),
    发布时间: a.publishTime ? formatTime(a.publishTime) : '未发布',
    状态: getStatusText(a.status),
    是否置顶: a.isTop === 1 ? '是' : '否'
  }))
  
  const csv = [
    Object.keys(data[0]).join(','),
    ...data.map(row => Object.values(row).join(','))
  ].join('\n')
  
  const blob = new Blob(['\ufeff' + csv], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = `公告列表_${new Date().toISOString().split('T')[0]}.csv`
  link.click()
  
  ElMessage.success('导出成功')
  clearSelection()
}

const loadAnnouncements = async () => {
  try {
    const res = await api.get('/admin/announcement/list')
    if (res.code === 200) {
      announcements.value = res.data || []
    }
  } catch (error) {
    console.error('加载公告失败', error)
  }
}

const showDialog = (announcement = null) => {
  editingAnnouncement.value = announcement
  if (announcement) {
    form.value = {
      id: announcement.id,
      title: announcement.title,
      content: announcement.content,
      type: announcement.type,
      target: announcement.target,
      isTop: announcement.isTop,
      status: announcement.status
    }
  } else {
    form.value = {
      title: '',
      content: '',
      type: 1,
      target: 0,
      isTop: 0,
      status: 0
    }
  }
  drawerVisible.value = true
}

const saveAnnouncement = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    try {
      if (editingAnnouncement.value) {
        await api.put('/admin/announcement/update', form.value)
        ElMessage.success('更新成功')
      } else {
        await api.post('/admin/announcement/add', form.value)
        ElMessage.success('添加成功')
      }
      drawerVisible.value = false
      loadAnnouncements()
    } catch (error) {
      ElMessage.error(error.response?.data?.message || '操作失败')
    }
  })
}

const publishAnnouncement = async (id) => {
  try {
    await api.post(`/admin/announcement/publish/${id}`)
    ElMessage.success('发布成功')
    loadAnnouncements()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '操作失败')
  }
}

const unpublishAnnouncement = async (id) => {
  try {
    await ElMessageBox.confirm(
      '确认要下线该公告吗？下线后用户将无法查看',
      '下线确认',
      { type: 'warning' }
    )
    await api.post(`/admin/announcement/unpublish/${id}`)
    ElMessage.success('公告已成功下线')
    loadAnnouncements()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '操作失败')
    }
  }
}

const deleteAnnouncement = async (id) => {
  try {
    await ElMessageBox.confirm(
      '确认要删除该公告吗？此操作不可恢复！',
      '删除确认',
      { type: 'warning' }
    )
    await api.delete(`/admin/announcement/delete/${id}`)
    ElMessage.success('公告已成功删除')
    loadAnnouncements()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '删除失败')
    }
  }
}

const handleTabChange = () => {
  currentPage.value = 1
}

const handleSearch = () => {
  currentPage.value = 1
}

const applyFilters = () => {
  showFilterDialog.value = false
  currentPage.value = 1
}

const resetFilters = () => {
  filterForm.value = {
    type: null,
    target: null,
    dateRange: null,
    isTop: null,
    sortBy: 'top'
  }
  searchKeyword.value = ''
  currentPage.value = 1
  showFilterDialog.value = false
}

onMounted(() => {
  loadAnnouncements()
})
</script>

<style scoped>
.announcements-page {
  padding: 24px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.announcements-card {
  border-radius: 8px;
  border: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #e8e8e8;
}

.card-title-wrap {
  display: flex;
  flex-direction: column;
}

.card-title {
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 4px;
}

.card-subtitle {
  font-size: 13px;
  color: #7f8c8d;
}

.publish-btn {
  border-radius: 6px;
  padding: 8px 16px;
  font-weight: 500;
}

.publish-btn :deep(.el-icon) {
  margin-right: 4px;
}

.publish-btn:hover {
  background: #27ae60;
  border-color: #27ae60;
}

/* 标签页 */
.status-tabs {
  margin-top: 8px;
  margin-bottom: 16px;
}

:deep(.status-tabs .el-tabs__item) {
  font-size: 14px;
  color: #7f8c8d;
  font-weight: 400;
  padding: 0 20px;
}

:deep(.status-tabs .el-tabs__item.is-active) {
  color: #2ecc71;
  font-weight: 600;
}

:deep(.status-tabs .el-tabs__active-bar) {
  background-color: #2ecc71;
}

:deep(.status-tabs .el-tabs__item:hover) {
  color: #2ecc71;
}

/* 工具栏 */
.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
  padding: 16px;
  background: #ffffff;
  border-radius: 8px;
  border: 1px solid #e8e8e8;
}

.search-section {
  flex: 1;
  max-width: 400px;
}

.search-input {
  width: 100%;
}

:deep(.search-input .el-input__wrapper) {
  border-color: #e8e8e8;
}

:deep(.search-input .el-input__wrapper.is-focus) {
  border-color: #2ecc71;
  box-shadow: 0 0 0 1px #2ecc71 inset;
}

.filter-section {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-badge {
  margin-left: 4px;
}

/* 批量操作栏 */
.batch-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: #e6f7ff;
  border: 1px solid #91d5ff;
  border-radius: 8px;
  margin-bottom: 16px;
}

.selected-count {
  color: #1890ff;
  font-weight: 500;
  margin-right: auto;
}

/* 表格样式 */
.announcements-table {
  background: #ffffff;
}

:deep(.announcements-table .el-table__header) {
  background: #eaecef;
}

:deep(.announcements-table .el-table__header th) {
  background: #eaecef;
  color: #2c3e50;
  font-weight: 600;
  padding: 12px 16px;
  border-bottom: 2px solid #dee2e6;
}

:deep(.announcements-table .el-table__body tr) {
  height: 52px;
}

:deep(.announcements-table .el-table__body td) {
  padding: 12px 16px;
  vertical-align: middle;
}

:deep(.announcements-table .table-row:hover) {
  background: #ecf0f1 !important;
}

:deep(.announcements-table .el-table__body tr:nth-child(even)) {
  background: #f8f9fa;
}

:deep(.announcements-table .el-table__body tr:nth-child(odd)) {
  background: #ffffff;
}

/* 公告标题单元格 */
.announcement-title-cell {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.announcement-icon {
  font-size: 20px;
  line-height: 1;
  margin-top: 2px;
}

.announcement-title-content {
  flex: 1;
}

.announcement-title-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.announcement-title-text {
  font-weight: 500;
  color: #2c3e50;
  font-size: 14px;
}

.top-badge {
  background: #e74c3c;
  color: #fff;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 600;
  line-height: 1.4;
}

.announcement-tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.tag-top {
  font-size: 11px;
}

/* 公告类型标签 */
.type-tag {
  font-weight: 500;
}

/* 自定义类型标签颜色 */
:deep(.type-tag.el-tag--info) {
  background: #e6f7ff;
  border-color: #91d5ff;
  color: #1890ff;
}

:deep(.type-tag.el-tag--warning) {
  background: #fff7e6;
  border-color: #ffd591;
  color: #fa8c16;
}

/* 维护公告（type=3）使用紫色 */
:deep(.announcements-table .el-table__body .type-tag:not(.el-tag--info):not(.el-tag--warning)) {
  background: #f9f0ff;
  border-color: #d3adf7;
  color: #722ed1;
}

/* 目标用户单元格 */
.target-user-cell {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #2c3e50;
}

.target-icon {
  font-size: 16px;
}

/* 发布时间单元格 */
.publish-time-cell {
  font-size: 13px;
}

.relative-time {
  color: #2c3e50;
  font-weight: 500;
  cursor: help;
}

.not-published {
  color: #bdc3c7;
}

/* 阅读统计 */
.read-stats {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #7f8c8d;
}

.read-stats :deep(.el-icon) {
  font-size: 14px;
}

/* 状态标签 */
.status-pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 4px 12px;
  border-radius: 16px;
  font-size: 12px;
  font-weight: 500;
  gap: 6px;
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: currentColor;
}

.status-published {
  background: #d4edda;
  color: #155724;
}

.status-draft {
  background: #eaecef;
  color: #6c757d;
}

.status-offline {
  background: #fff3e0;
  color: #f57c00;
}

/* 操作按钮 */
.action-buttons {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.action-btn {
  padding: 4px 8px;
  font-size: 13px;
}

.action-btn :deep(.el-icon) {
  margin-right: 4px;
  font-size: 14px;
}

/* 分页 */
.pagination-wrap {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
  padding: 16px;
  background: #ffffff;
  border-radius: 8px;
  border: 1px solid #e8e8e8;
}

:deep(.pagination-wrap .el-pagination .el-pager li.is-active) {
  background-color: #2ecc71;
  color: #ffffff;
}

:deep(.pagination-wrap .el-pagination .btn-prev:hover),
:deep(.pagination-wrap .el-pagination .btn-next:hover) {
  color: #2ecc71;
}

/* 预览内容 */
.preview-content {
  padding: 20px 0;
}

.preview-header {
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e8e8e8;
}

.preview-header h2 {
  margin: 0 0 12px 0;
  font-size: 20px;
  font-weight: 600;
  color: #2c3e50;
}

.preview-meta {
  display: flex;
  align-items: center;
  gap: 12px;
}

.preview-time {
  font-size: 13px;
  color: #7f8c8d;
}

.preview-body {
  line-height: 2;
  color: #2c3e50;
  font-size: 14px;
  white-space: pre-wrap;
}

.preview-body :deep(p) {
  margin: 0 0 12px 0;
}

.preview-body :deep(ol),
.preview-body :deep(ul) {
  margin: 12px 0;
  padding-left: 24px;
}

.preview-body :deep(li) {
  margin-bottom: 8px;
}
</style>

