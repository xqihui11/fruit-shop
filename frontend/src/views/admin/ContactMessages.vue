<template>
  <div class="contact-messages-page">
    <!-- 页面标题区 -->
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">在线留言管理</h2>
        <p class="page-subtitle">查看和管理用户提交的在线留言</p>
      </div>
      <el-button 
        type="primary" 
        :icon="Refresh" 
        @click="loadMessages" 
        :loading="loading"
        class="refresh-btn"
      >
        刷新
      </el-button>
    </div>

    <!-- 筛选与搜索栏 -->
    <el-card class="filter-card" shadow="never">
      <div class="filter-toolbar">
        <div class="filter-left">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索姓名、电话、邮箱"
            clearable
            class="search-input"
            @input="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-select 
            v-model="statusFilter" 
            placeholder="全部状态" 
            clearable
            class="filter-select"
            @change="handleFilter"
          >
            <el-option label="全部状态" :value="null" />
            <el-option label="未处理" :value="0" />
            <el-option label="处理中" :value="1" />
            <el-option label="已处理" :value="2" />
          </el-select>
          <el-select 
            v-model="typeFilter" 
            placeholder="全部类型" 
            clearable
            class="filter-select"
            @change="handleFilter"
          >
            <el-option label="全部类型" :value="null" />
            <el-option label="咨询问题" value="咨询问题" />
            <el-option label="意见建议" value="意见建议" />
            <el-option label="投诉反馈" value="投诉反馈" />
            <el-option label="合作洽谈" value="合作洽谈" />
            <el-option label="其他" value="其他" />
          </el-select>
        </div>
        <div class="filter-right">
          <el-button 
            type="success" 
            :disabled="selectedMessages.length === 0"
            @click="handleBatchProcess"
          >
            <el-icon><Check /></el-icon>
            批量标记已处理
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 表格区 -->
    <el-card class="table-card" shadow="never">
      <el-table 
        :data="filteredMessages" 
        v-loading="loading" 
        stripe
        class="messages-table"
        @selection-change="handleSelectionChange"
        @row-click="handleRowClick"
        :row-class-name="getRowClassName"
      >
        <el-table-column type="selection" width="48" />
        
        <el-table-column prop="id" label="ID" width="80" sortable="custom" />
        
        <el-table-column label="姓名" min-width="150">
          <template #default="{ row }">
            <div class="name-cell">
              <el-avatar
                :size="32"
                class="user-avatar"
              >
                {{ (row.name || '').charAt(0) }}
              </el-avatar>
              <span class="name-text">{{ row.name }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="联系电话" min-width="140">
          <template #default="{ row }">
            <el-tooltip :content="row.phone" placement="top">
              <span class="phone-text">{{ maskPhone(row.phone) }}</span>
            </el-tooltip>
          </template>
        </el-table-column>

        <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip />

        <el-table-column label="留言类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.type)" class="type-tag">
              {{ row.type }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="留言内容" min-width="250">
          <template #default="{ row }">
            <div class="content-cell">
              <div class="content-preview" v-if="!row.expanded">
                {{ truncateContent(row.content) }}
              </div>
              <div class="content-full" v-else>
                {{ row.content }}
              </div>
              <el-button 
                type="text" 
                size="small" 
                @click.stop="toggleExpand(row)"
                class="expand-btn"
              >
                {{ row.expanded ? '收起' : '展开' }}
              </el-button>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)" class="status-tag">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="提交时间" width="140" sortable="custom" prop="createTime">
          <template #default="{ row }">
            <el-tooltip :content="formatFullTime(row.createTime)" placement="top">
              <span class="time-text">{{ formatRelativeTime(row.createTime) }}</span>
            </el-tooltip>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button 
                type="primary" 
                size="small" 
                :icon="Message"
                @click.stop="handleReply(row)"
                class="action-btn"
              >
                回复
              </el-button>
              <el-button 
                type="success" 
                size="small" 
                :icon="Check"
                @click.stop="handleMarkProcessed(row)"
                v-if="row.status !== 2"
                class="action-btn"
              >
                标记已处理
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 空状态 -->
      <el-empty 
        v-if="!loading && filteredMessages.length === 0" 
        description="暂无留言，快去邀请用户留言吧～"
        :image-size="120"
        class="empty-state"
      >
        <el-button type="primary" @click="loadMessages">刷新数据</el-button>
      </el-empty>

      <!-- 分页 -->
      <div class="pagination" v-if="filteredMessages.length > 0">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handlePageChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 查看/回复对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="700px"
      @close="handleDialogClose"
      class="message-dialog"
    >
      <el-form :model="currentMessage" label-width="100px" class="message-form">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="姓名">
              <el-input v-model="currentMessage.name" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话">
              <el-input v-model="currentMessage.phone" disabled />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="邮箱">
          <el-input v-model="currentMessage.email" disabled />
        </el-form-item>
        <el-form-item label="留言类型">
          <el-tag :type="getTypeTagType(currentMessage.type)">
            {{ currentMessage.type }}
          </el-tag>
        </el-form-item>
        <el-form-item label="留言内容">
          <el-input
            v-model="currentMessage.content"
            type="textarea"
            :rows="4"
            disabled
            class="content-textarea"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="currentMessage.status" style="width: 100%">
            <el-option label="未处理" :value="0" />
            <el-option label="处理中" :value="1" />
            <el-option label="已处理" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="回复内容">
          <el-input
            v-model="currentMessage.reply"
            type="textarea"
            :rows="6"
            placeholder="请输入回复内容，支持 @用户 功能"
            class="reply-textarea"
          />
          <div class="reply-tip">
            <el-icon><InfoFilled /></el-icon>
            <span>回复后，状态将自动更新为"已处理"</span>
          </div>
        </el-form-item>
        <el-form-item label="回复记录" v-if="currentMessage.reply">
          <div class="reply-history">
            <div class="reply-item">
              <span class="reply-time">{{ formatFullTime(currentMessage.updateTime) }}</span>
              <div class="reply-content">{{ currentMessage.reply }}</div>
            </div>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">
          <el-icon><Check /></el-icon>
          保存并回复
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Search, Check, Message, InfoFilled } from '@element-plus/icons-vue'
import api from '@/utils/api'

const messages = ref([])
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('查看留言')
const currentMessage = ref({})
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const selectedMessages = ref([])
const searchKeyword = ref('')
const statusFilter = ref(null)
const typeFilter = ref(null)

// 计算过滤后的消息列表
const filteredMessages = computed(() => {
  let result = [...messages.value]
  
  // 搜索过滤
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(msg => 
      msg.name?.toLowerCase().includes(keyword) ||
      msg.phone?.includes(keyword) ||
      msg.email?.toLowerCase().includes(keyword)
    )
  }
  
  // 状态过滤
  if (statusFilter.value !== null) {
    result = result.filter(msg => msg.status === statusFilter.value)
  }
  
  // 类型过滤
  if (typeFilter.value) {
    result = result.filter(msg => msg.type === typeFilter.value)
  }
  
  // 分页
  total.value = result.length
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return result.slice(start, end)
})

const loadMessages = async () => {
  loading.value = true
  try {
    const res = await api.get('/admin/contact/messages')
    if (res.code === 200) {
      messages.value = (res.data || []).map(msg => ({
        ...msg,
        expanded: false
      }))
    } else {
      ElMessage.error(res.message || '加载失败')
    }
  } catch (error) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
}

const handleFilter = () => {
  currentPage.value = 1
}

const handleSelectionChange = (selection) => {
  selectedMessages.value = selection
}

const handleRowClick = (row) => {
  // 点击行可以展开/收起内容
  toggleExpand(row)
}

const toggleExpand = (row) => {
  row.expanded = !row.expanded
}

const handleReply = (row) => {
  currentMessage.value = { ...row }
  if (!currentMessage.value.reply) {
    currentMessage.value.reply = ''
  }
  dialogTitle.value = '回复留言'
  dialogVisible.value = true
}

const handleMarkProcessed = async (row) => {
  try {
    await ElMessageBox.confirm(
      '确认要标记该留言为已处理吗？',
      '确认操作',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const res = await api.put(`/admin/contact/message/${row.id}`, {
      status: 2,
      reply: row.reply || '已处理'
    })
    
    if (res.code === 200) {
      ElMessage.success('已成功标记为已处理')
      loadMessages()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

const handleBatchProcess = async () => {
  if (selectedMessages.value.length === 0) {
    ElMessage.warning('请先选择要处理的留言')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确认要批量标记 ${selectedMessages.value.length} 条留言为已处理吗？`,
      '批量操作确认',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const promises = selectedMessages.value.map(msg =>
      api.put(`/admin/contact/message/${msg.id}`, {
        status: 2,
        reply: msg.reply || '已处理'
      })
    )
    
    await Promise.all(promises)
    ElMessage.success(`已成功处理 ${selectedMessages.value.length} 条留言`)
    selectedMessages.value = []
    loadMessages()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量操作失败')
    }
  }
}

const handleSave = async () => {
  if (!currentMessage.value.reply?.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }
  
  saving.value = true
  try {
    // 如果有回复内容，自动设置为已处理
    const status = currentMessage.value.reply ? 2 : currentMessage.value.status
    const res = await api.put(`/admin/contact/message/${currentMessage.value.id}`, {
      status: status,
      reply: currentMessage.value.reply
    })
    if (res.code === 200) {
      ElMessage.success('已成功回复留言')
      dialogVisible.value = false
      loadMessages()
    } else {
      ElMessage.error(res.message || '保存失败')
    }
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

const handleDialogClose = () => {
  currentMessage.value = {}
}

const handlePageChange = () => {
  // 分页变化时自动处理
}

const getRowClassName = ({ row }) => {
  return row.status === 0 ? 'row-pending' : row.status === 2 ? 'row-processed' : ''
}

const getStatusText = (status) => {
  const statusMap = {
    0: '未处理',
    1: '处理中',
    2: '已处理'
  }
  return statusMap[status] || '未知'
}

const getStatusTagType = (status) => {
  const typeMap = {
    0: 'warning',
    1: 'info',
    2: 'success'
  }
  return typeMap[status] || 'info'
}

const getTypeTagType = (type) => {
  const typeMap = {
    '咨询问题': 'primary',
    '意见建议': 'success',
    '投诉反馈': 'danger',
    '合作洽谈': 'warning',
    '其他': 'info'
  }
  return typeMap[type] || 'info'
}

const maskPhone = (phone) => {
  if (!phone) return '-'
  if (phone.length < 7) return phone
  return phone.substring(0, 3) + '****' + phone.substring(phone.length - 4)
}

const truncateContent = (content) => {
  if (!content) return '-'
  return content.length > 50 ? content.substring(0, 50) + '...' : content
}

const formatRelativeTime = (time) => {
  if (!time) return '-'
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(minutes / 60)
  const days = Math.floor(hours / 24)
  
  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  return date.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' })
}

const formatFullTime = (time) => {
  if (!time) return '-'
  const date = new Date(time)
  return date.toLocaleString('zh-CN')
}

onMounted(() => {
  loadMessages()
})
</script>

<style scoped>
.contact-messages-page {
  padding: 24px;
  background: #F5F7FA;
  min-height: calc(100vh - 64px);
}

/* 页面标题区 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 2px solid #EAECEF;
}

.header-left {
  flex: 1;
}

.page-title {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 700;
  color: #2C3E50;
  letter-spacing: 0.5px;
}

.page-subtitle {
  margin: 0;
  font-size: 14px;
  color: #7F8C8D;
}

.refresh-btn {
  border-radius: 4px;
  padding: 8px 16px;
  font-weight: 500;
}

.refresh-btn:hover {
  opacity: 0.9;
}

/* 筛选卡片 */
.filter-card {
  margin-bottom: 16px;
  border-radius: 8px;
}

.filter-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.filter-left {
  display: flex;
  gap: 12px;
  flex: 1;
}

.search-input {
  width: 280px;
}

.filter-select {
  width: 140px;
}

.filter-right {
  display: flex;
  gap: 8px;
}

/* 表格卡片 */
.table-card {
  border-radius: 8px;
}

.messages-table {
  font-size: 14px;
}

/* 表格行样式 */
:deep(.messages-table .el-table__row) {
  height: 52px;
  transition: background-color 0.2s;
}

:deep(.messages-table .el-table__row:hover) {
  background-color: #ECF0F1 !important;
  cursor: pointer;
}

:deep(.messages-table .row-pending) {
  background-color: #FFF3E0;
}

:deep(.messages-table .row-processed) {
  background-color: #D4EDDA;
}

:deep(.messages-table .el-table__header) {
  background-color: #EAECEF;
}

:deep(.messages-table .el-table__header th) {
  background-color: #EAECEF;
  color: #2C3E50;
  font-weight: 600;
  padding: 12px 16px;
}

:deep(.messages-table .el-table__body td) {
  padding: 12px 16px;
  vertical-align: middle;
}

/* 姓名列 */
.name-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-avatar {
  flex-shrink: 0;
  background: linear-gradient(135deg, #2ECC71 0%, #27AE60 100%);
  color: white;
  font-weight: 600;
}

.name-text {
  font-weight: 500;
  color: #2C3E50;
}

/* 电话脱敏 */
.phone-text {
  color: #7F8C8D;
  font-family: 'Courier New', monospace;
}

.phone-text:hover {
  color: #2C3E50;
}

/* 类型标签 */
.type-tag {
  border-radius: 16px;
  padding: 4px 12px;
  font-size: 12px;
  font-weight: 500;
}

/* 状态标签 */
.status-tag {
  border-radius: 16px;
  padding: 4px 12px;
  font-size: 12px;
  font-weight: 500;
}

/* 留言内容 */
.content-cell {
  position: relative;
}

.content-preview {
  color: #7F8C8D;
  line-height: 1.6;
  max-height: 40px;
  overflow: hidden;
}

.content-full {
  color: #2C3E50;
  line-height: 1.6;
  white-space: pre-wrap;
}

.expand-btn {
  margin-top: 4px;
  padding: 0;
  color: #3498DB;
  font-size: 12px;
}

.expand-btn:hover {
  color: #2980B9;
}

/* 时间显示 */
.time-text {
  color: #7F8C8D;
  font-size: 13px;
}

/* 操作按钮 */
.action-buttons {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.action-btn {
  border-radius: 4px;
  font-size: 12px;
  padding: 6px 12px;
}

/* 空状态 */
.empty-state {
  padding: 60px 20px;
}

/* 分页 */
.pagination {
  margin-top: 24px;
  display: flex;
  justify-content: flex-end;
  padding-top: 16px;
  border-top: 1px solid #EAECEF;
}

:deep(.el-pagination.is-background .el-pager li.is-active) {
  background-color: #2ECC71;
  color: white;
}

/* 对话框 */
.message-dialog :deep(.el-dialog__header) {
  padding: 20px 24px;
  border-bottom: 1px solid #EAECEF;
}

.message-dialog :deep(.el-dialog__body) {
  padding: 24px;
}

.message-form {
  margin-top: 8px;
}

.content-textarea :deep(.el-textarea__inner) {
  background-color: #F8F9FA;
  color: #7F8C8D;
}

.reply-textarea {
  margin-bottom: 8px;
}

.reply-tip {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #BDC3C7;
  font-size: 12px;
  margin-top: 8px;
}

.reply-history {
  background: #F8F9FA;
  border-radius: 6px;
  padding: 12px;
}

.reply-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.reply-time {
  font-size: 12px;
  color: #7F8C8D;
}

.reply-content {
  color: #2C3E50;
  line-height: 1.6;
  white-space: pre-wrap;
}

/* 响应式 */
@media (max-width: 768px) {
  .contact-messages-page {
    padding: 16px;
  }

  .filter-toolbar {
    flex-direction: column;
    align-items: stretch;
  }

  .filter-left {
    flex-direction: column;
  }

  .search-input,
  .filter-select {
    width: 100%;
  }

  .action-buttons {
    flex-direction: column;
  }

  .action-btn {
    width: 100%;
  }
}
</style>
