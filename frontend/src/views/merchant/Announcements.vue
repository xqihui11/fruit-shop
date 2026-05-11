<template>
  <div class="announcements-page">
    <div class="page-header">
      <h2>
        <el-icon><Bell /></el-icon>
        公告中心
      </h2>
      <p class="page-subtitle">查看平台最新公告和重要通知</p>
    </div>

    <!-- 类型标签页 -->
    <el-tabs v-model="activeTab" @tab-change="handleTabChange" class="type-tabs">
      <el-tab-pane label="全部" name=""></el-tab-pane>
      <el-tab-pane label="系统公告" name="1"></el-tab-pane>
      <el-tab-pane label="活动公告" name="2"></el-tab-pane>
      <el-tab-pane label="维护公告" name="3"></el-tab-pane>
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

    <!-- 筛选对话框 -->
    <el-dialog v-model="showFilterDialog" title="筛选公告" width="500px">
      <el-form :model="filterForm" label-width="100px">
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

    <!-- 公告列表 -->
    <div class="announcements-list">
      <div v-if="pagedAnnouncements.length === 0" class="empty-state">
        <el-empty description="暂无公告" />
      </div>
      <div
        v-for="announcement in pagedAnnouncements"
        :key="announcement.id"
        class="announcement-card"
        :class="{ 'is-top': announcement.isTop === 1 }"
        @click="showDetail(announcement)"
      >
        <div class="announcement-header">
          <div class="announcement-title-row">
            <h3 class="announcement-title">
              <el-icon v-if="announcement.isTop === 1" class="top-icon"><Top /></el-icon>
              {{ announcement.title }}
            </h3>
            <el-tag :type="getTypeTagType(announcement.type)" size="small">
              {{ getTypeText(announcement.type) }}
            </el-tag>
          </div>
          <div class="announcement-meta">
            <span class="announcement-time">
              <el-icon><Clock /></el-icon>
              {{ formatTime(announcement.publishTime) }}
            </span>
          </div>
        </div>
        <div class="announcement-preview">
          {{ getPreview(announcement.content) }}
        </div>
        <div class="announcement-footer">
          <el-link type="primary" :underline="false" @click.stop="showDetail(announcement)">
            查看详情 →
          </el-link>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <el-pagination
      v-if="filteredAnnouncements.length > 0"
      v-model:current-page="currentPage"
      :page-size="pageSize"
      :total="filteredAnnouncements.length"
      layout="prev, pager, next, jumper"
      @current-change="handlePageChange"
      class="pagination"
    />

    <!-- 公告详情对话框 -->
    <el-dialog 
      v-model="detailVisible" 
      :title="currentAnnouncement?.title" 
      width="700px"
      class="announcement-dialog"
    >
      <div class="announcement-detail">
        <div class="announcement-meta">
          <el-tag :type="getTypeTagType(currentAnnouncement?.type)" size="small">
            {{ getTypeText(currentAnnouncement?.type) }}
          </el-tag>
          <span class="announcement-time">
            <el-icon><Clock /></el-icon>
            {{ formatTime(currentAnnouncement?.publishTime) }}
          </span>
        </div>
        <div class="announcement-content" v-html="formatContent(currentAnnouncement?.content)"></div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Bell, Clock, Top, Search, Filter, Refresh } from '@element-plus/icons-vue'
import api from '@/utils/api'

const activeTab = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const announcements = ref([])
const detailVisible = ref(false)
const currentAnnouncement = ref(null)
const searchKeyword = ref('')
const showFilterDialog = ref(false)
const filterForm = ref({
  dateRange: null,
  isTop: null,
  sortBy: 'top'
})

const getTypeText = (type) => {
  const map = { 1: '系统公告', 2: '活动公告', 3: '维护公告' }
  return map[type] || '公告'
}

const getTypeTagType = (type) => {
  const map = { 1: 'info', 2: 'success', 3: 'warning' }
  return map[type] || ''
}

const formatTime = (time) => {
  if (!time) return '—'
  return new Date(time).toLocaleString('zh-CN')
}

const formatContent = (content) => {
  if (!content) return ''
  return content.replace(/\n/g, '<br>')
}

const getPreview = (content) => {
  if (!content) return '暂无内容'
  // 移除HTML标签，只保留文本
  const text = content.replace(/<[^>]*>/g, '').replace(/\n/g, ' ')
  return text.length > 100 ? text.substring(0, 100) + '...' : text
}

const hasActiveFilters = computed(() => {
  return filterForm.value.dateRange !== null || 
         filterForm.value.isTop !== null ||
         searchKeyword.value.trim() !== ''
})

const activeFilterCount = computed(() => {
  let count = 0
  if (filterForm.value.dateRange !== null) count++
  if (filterForm.value.isTop !== null) count++
  if (searchKeyword.value.trim() !== '') count++
  return count
})

const filteredAnnouncements = computed(() => {
  let list = [...announcements.value]
  
  // 按类型过滤
  if (activeTab.value) {
    list = list.filter(a => a.type === parseInt(activeTab.value))
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
  
  // 时间范围筛选
  if (filterForm.value.dateRange && filterForm.value.dateRange.length === 2) {
    const [startDate, endDate] = filterForm.value.dateRange
    const start = new Date(startDate).getTime()
    const end = new Date(endDate).getTime() + 24 * 60 * 60 * 1000 - 1 // 包含结束日期当天
    list = list.filter(a => {
      if (!a.publishTime) return false
      const publishTime = new Date(a.publishTime).getTime()
      return publishTime >= start && publishTime <= end
    })
  }
  
  // 置顶筛选
  if (filterForm.value.isTop !== null) {
    list = list.filter(a => a.isTop === filterForm.value.isTop)
  }
  
  // 排序
  if (filterForm.value.sortBy === 'top') {
    // 置顶优先，然后按时间倒序
    list.sort((a, b) => {
      if (a.isTop !== b.isTop) return b.isTop - a.isTop
      return new Date(b.publishTime || 0).getTime() - new Date(a.publishTime || 0).getTime()
    })
  } else {
    // 仅按时间倒序
    list.sort((a, b) => {
      return new Date(b.publishTime || 0).getTime() - new Date(a.publishTime || 0).getTime()
    })
  }
  
  return list
})

const pagedAnnouncements = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return filteredAnnouncements.value.slice(start, start + pageSize.value)
})

const loadAnnouncements = async () => {
  try {
    const res = await api.get('/announcement/published', { params: { target: 2 } })
    if (res.code === 200) {
      announcements.value = res.data || []
    }
  } catch (error) {
    console.error('加载公告失败', error)
  }
}

const handleTabChange = () => {
  currentPage.value = 1
}

const handlePageChange = () => {
  window.scrollTo({ top: 0, behavior: 'smooth' })
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
    dateRange: null,
    isTop: null,
    sortBy: 'top'
  }
  searchKeyword.value = ''
  currentPage.value = 1
  showFilterDialog.value = false
}

const showDetail = (announcement) => {
  currentAnnouncement.value = announcement
  detailVisible.value = true
}

onMounted(() => {
  loadAnnouncements()
})
</script>

<style scoped>
.announcements-page {
  max-width: 1000px;
  margin: 0 auto;
  padding: 24px;
}

.page-header {
  margin-bottom: 24px;
  text-align: center;
}

.page-header h2 {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin: 0 0 8px 0;
  font-size: 28px;
  color: #262626;
  font-weight: 600;
}

.page-subtitle {
  margin: 0;
  color: #8c8c8c;
  font-size: 14px;
}

.type-tabs {
  margin-bottom: 24px;
}

.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 24px;
  padding: 16px;
  background: #fafafa;
  border-radius: 8px;
}

.search-section {
  flex: 1;
  max-width: 400px;
}

.search-input {
  width: 100%;
}

.filter-section {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-badge {
  margin-left: 4px;
}

.announcements-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.announcement-card {
  background: #ffffff;
  border: 1px solid #e8e8e8;
  border-radius: 12px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.announcement-card:hover {
  border-color: #1890ff;
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.15);
  transform: translateY(-2px);
}

.announcement-card.is-top {
  border-color: #ff4d4f;
  background: linear-gradient(to right, #fff1f0 0%, #ffffff 10%);
}

.announcement-header {
  margin-bottom: 12px;
}

.announcement-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 8px;
}

.announcement-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #262626;
  flex: 1;
}

.top-icon {
  color: #ff4d4f;
  font-size: 16px;
}

.announcement-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 13px;
  color: #8c8c8c;
}

.announcement-time {
  display: flex;
  align-items: center;
  gap: 4px;
}

.announcement-preview {
  color: #595959;
  font-size: 14px;
  line-height: 1.6;
  margin-bottom: 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.announcement-footer {
  display: flex;
  justify-content: flex-end;
}

.empty-state {
  padding: 60px 0;
}

.pagination {
  margin-top: 32px;
  display: flex;
  justify-content: center;
}

.announcement-detail {
  padding: 20px 0;
}

.announcement-content {
  line-height: 2;
  color: #333;
  white-space: pre-wrap;
  font-size: 15px;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', 'Helvetica Neue', Helvetica, Arial, sans-serif;
  letter-spacing: 0.3px;
  margin-top: 16px;
}

.announcement-content :deep(p) {
  margin: 0 0 12px 0;
  line-height: 2;
}

.announcement-content :deep(ol),
.announcement-content :deep(ul) {
  margin: 12px 0;
  padding-left: 24px;
}

.announcement-content :deep(li) {
  margin-bottom: 8px;
  line-height: 2;
}

:deep(.announcement-dialog .el-dialog__header) {
  padding: 20px 24px 16px;
  border-bottom: 1px solid #f0f0f0;
}

:deep(.announcement-dialog .el-dialog__title) {
  font-size: 18px;
  font-weight: 600;
  color: #262626;
  letter-spacing: 0.5px;
}

:deep(.announcement-dialog .el-dialog__body) {
  padding: 24px;
}
</style>

