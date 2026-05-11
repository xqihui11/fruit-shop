<template>
  <div class="aftersales-page">
    <!-- 页面标题区 -->
    <div class="page-header">
      <h1 class="page-title">售后管理</h1>
    </div>

    <!-- 筛选栏 -->
    <el-card class="filter-card" shadow="never">
      <el-form :inline="true" :model="filterForm" class="filter-form">
        <el-form-item label="搜索">
          <el-input
            v-model="filterForm.keyword"
            placeholder="订单号、退款金额"
            clearable
            style="width: 200px"
            @keyup.enter="handleFilter"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="申请时间">
          <el-date-picker
            v-model="filterForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            style="width: 240px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleFilter">
            <el-icon><Search /></el-icon>
            筛选
          </el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 批量操作栏 -->
    <el-card v-if="selectedAfterSales.length > 0" class="batch-actions-card" shadow="never">
      <div class="batch-info">
        <span>已选择 <strong>{{ selectedAfterSales.length }}</strong> 个售后单</span>
        <div class="batch-btns">
          <el-button type="success" size="small" @click="batchProcess(true)">
            <el-icon><Check /></el-icon>
            批量同意
          </el-button>
          <el-button type="danger" size="small" @click="batchProcess(false)">
            <el-icon><Close /></el-icon>
            批量驳回
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 标签页和表格 -->
    <el-card class="table-card" shadow="never">
      <el-tabs v-model="activeTab" @tab-change="loadAfterSales" class="status-tabs">
        <el-tab-pane label="全部" name=""></el-tab-pane>
        <el-tab-pane label="待处理" name="0"></el-tab-pane>
        <el-tab-pane label="已同意/待退货" name="1"></el-tab-pane>
        <el-tab-pane label="已驳回" name="2"></el-tab-pane>
        <el-tab-pane label="已完成" name="3"></el-tab-pane>
        <el-tab-pane label="用户申诉" name="4"></el-tab-pane>
      </el-tabs>

      <el-table
        :data="pagedAfterSales"
        v-loading="loading"
        stripe
        @selection-change="handleSelectionChange"
        class="aftersales-table"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="商品" width="80">
          <template #default="{ row }">
            <img
              v-if="row.productImage"
              :src="getImageUrl(row.productImage)"
              class="product-thumb"
            />
            <span v-else>暂无</span>
          </template>
        </el-table-column>
        <el-table-column prop="orderNo" label="订单号" width="200">
          <template #default="{ row }">
            <el-tooltip :content="row.orderNo" placement="top">
              <span class="order-no">{{ truncateOrderNo(row.orderNo) }}</span>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="120">
          <template #default="{ row }">
            <div class="type-cell">
              <el-icon class="type-icon" :class="row.type === 1 ? 'return-icon' : 'refund-icon'">
                <component :is="row.type === 1 ? 'Refresh' : 'Money'" />
              </el-icon>
              <!-- 与用户端/管理端保持一致：1-退货退款，其他-仅退款 -->
              <span>{{ row.type === 1 ? '退货退款' : '仅退款' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="原因" min-width="150" />
        <el-table-column prop="refundAmount" label="退款金额" width="130" sortable>
          <template #default="{ row }">
            <span :class="{ 'amount-warning': Number(row.refundAmount) > 1000 }" class="amount-text">
              ¥{{ Number(row.refundAmount).toFixed(2) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="申请时间" width="180" sortable>
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" class="status-tag">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === 0">
              <el-button
                type="success"
                link
                size="small"
                @click="handleProcess(row, true)"
              >
                <el-icon><Check /></el-icon>
                同意
              </el-button>
              <el-button
                type="danger"
                link
                size="small"
                @click="handleProcess(row, false)"
              >
                <el-icon><Close /></el-icon>
                驳回
              </el-button>
            </template>
            <el-button
              v-else
              type="primary"
              link
              size="small"
              @click="viewDetail(row)"
            >
              <el-icon><InfoFilled /></el-icon>
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 空状态 -->
      <el-empty
        v-if="!loading && filteredAfterSales.length === 0"
        description="暂无售后数据，可尝试调整筛选条件"
        :image-size="120"
      />

      <div v-if="filteredAfterSales.length > 0" class="pagination-wrapper">
        <el-pagination
          background
          layout="total, prev, pager, next, jumper"
          :page-size="pageSize"
          :current-page="currentPage"
          :total="filteredAfterSales.length"
          @current-change="(p) => (currentPage = p)"
        />
      </div>
    </el-card>

    <!-- 详情抽屉 -->
    <el-drawer v-model="detailDrawerVisible" title="售后详情" size="600px">
      <div v-if="currentDetail" class="detail-content">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="售后ID">{{ currentDetail.id }}</el-descriptions-item>
          <el-descriptions-item label="订单号">{{ currentDetail.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="类型">
            <!-- 与列表和用户端保持一致：1-退货退款，其他-仅退款 -->
            {{ currentDetail.type === 1 ? '退货退款' : '仅退款' }}
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(currentDetail.status)">
              {{ getStatusText(currentDetail.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="退款金额">
            <span :class="{ 'amount-warning': Number(currentDetail.refundAmount) > 1000 }">
              ¥{{ Number(currentDetail.refundAmount).toFixed(2) }}
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="申请时间">
            {{ formatTime(currentDetail.createTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="原因" :span="2">
            {{ currentDetail.reason }}
          </el-descriptions-item>
          <el-descriptions-item label="说明" :span="2" v-if="currentDetail.description">
            {{ currentDetail.description }}
          </el-descriptions-item>
          <el-descriptions-item v-if="currentDetail.returnLogisticsCompany" label="退货物流公司" :span="2">
            {{ currentDetail.returnLogisticsCompany }}
          </el-descriptions-item>
          <el-descriptions-item v-if="currentDetail.returnLogisticsNo" label="退货物流单号" :span="2">
            {{ currentDetail.returnLogisticsNo }}
          </el-descriptions-item>
          <el-descriptions-item label="商家回复" :span="2" v-if="currentDetail.merchantReply">
            {{ currentDetail.merchantReply }}
          </el-descriptions-item>
          <el-descriptions-item label="处理时间" :span="2" v-if="currentDetail.processTime">
            {{ formatTime(currentDetail.processTime) }}
          </el-descriptions-item>
        </el-descriptions>

        <div v-if="currentDetail.images" class="images-section">
          <h3>凭证图片</h3>
          <div class="image-list">
            <el-image
              v-for="(img, index) in currentDetail.images.split(',')"
              :key="index"
              :src="getImageUrl(img)"
              :preview-src-list="currentDetail.images.split(',').map(i => getImageUrl(i))"
              fit="cover"
              class="detail-image"
            />
          </div>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search,
  Check,
  Close,
  InfoFilled,
  Refresh,
  Money
} from '@element-plus/icons-vue'
import api from '@/utils/api'

const afterSales = ref([])
const activeTab = ref('')
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const selectedAfterSales = ref([])
const detailDrawerVisible = ref(false)
const currentDetail = ref(null)

// 筛选表单
const filterForm = ref({
  keyword: '',
  dateRange: null
})

// 筛选后的售后列表
const filteredAfterSales = computed(() => {
  let result = [...afterSales.value]

  // 关键词搜索
  if (filterForm.value.keyword) {
    const keyword = filterForm.value.keyword.toLowerCase()
    result = result.filter(item => {
      return (
        item.orderNo?.toLowerCase().includes(keyword) ||
        String(item.refundAmount)?.includes(keyword)
      )
    })
  }

  // 时间范围筛选
  if (filterForm.value.dateRange && filterForm.value.dateRange.length === 2) {
    const [start, end] = filterForm.value.dateRange
    result = result.filter(item => {
      if (!item.createTime) return false
      const createTime = new Date(item.createTime)
      return createTime >= start && createTime <= end
    })
  }

  return result
})

const pagedAfterSales = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return filteredAfterSales.value.slice(start, start + pageSize.value)
})

const getStatusText = (status) => {
  const map = {
    0: '待处理',
    1: '已同意/待退货',
    2: '已驳回',
    3: '已完成',
    4: '用户已申诉'
  }
  return map[status] || '未知'
}

const getStatusType = (status) => {
  // 待处理用橙色，已同意/已完成用绿色，已驳回用红色，用户申诉用橙色
  const map = {
    0: 'warning',
    1: 'success',
    2: 'danger',
    3: 'success',
    4: 'warning'
  }
  return map[status] || 'info'
}

const truncateOrderNo = (orderNo) => {
  if (!orderNo) return ''
  if (orderNo.length > 16) {
    return orderNo.substring(0, 12) + '...'
  }
  return orderNo
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const loadAfterSales = async () => {
  loading.value = true
  try {
    const params = activeTab.value ? { status: parseInt(activeTab.value) } : {}
    const res = await api.get('/merchant/aftersale/list', { params })
    if (res.code === 200) {
      afterSales.value = res.data || []
      currentPage.value = 1
    }
  } catch (error) {
    console.error('加载售后列表失败', error)
    ElMessage.error('加载售后列表失败')
  } finally {
    loading.value = false
  }
}

// 筛选处理
const handleFilter = () => {
  currentPage.value = 1
  ElMessage.success('筛选完成')
}

// 重置筛选
const handleReset = () => {
  filterForm.value = {
    keyword: '',
    dateRange: null
  }
  currentPage.value = 1
  ElMessage.success('筛选条件已重置')
}

// 选择变化
const handleSelectionChange = (selection) => {
  selectedAfterSales.value = selection
}

// 批量处理
const batchProcess = async (agree) => {
  if (selectedAfterSales.value.length === 0) {
    ElMessage.warning('请先选择售后单')
    return
  }

  const actionText = agree ? '同意' : '驳回'
  try {
    await ElMessageBox.confirm(
      `确认要批量${actionText}选中的 ${selectedAfterSales.value.length} 个售后单吗？`,
      '批量操作确认',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const promises = selectedAfterSales.value.map(item => {
      const reply = agree ? '批量同意' : '批量驳回'
      return api.post(`/merchant/aftersale/process/${item.id}`, { agree, reply })
    })

    await Promise.all(promises)
    ElMessage.success(`已成功批量${actionText}`)
    selectedAfterSales.value = []
    loadAfterSales()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(`批量${actionText}失败`)
    }
  }
}

// 处理售后（带确认）
const handleProcess = async (item, agree) => {
  const actionText = agree ? '同意' : '驳回'
  const orderNo = item.orderNo || '该订单'

  try {
    const { value: reply } = await ElMessageBox.prompt(
      agree ? '请输入回复（可选）' : '请输入驳回原因',
      `${actionText}售后申请`,
      {
        inputPlaceholder: agree ? '回复内容' : '请输入驳回原因',
        inputValidator: (value) => {
          if (!agree && !value) {
            return '请输入驳回原因'
          }
          return true
        },
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: agree ? 'success' : 'warning'
      }
    )

    const res = await api.post(`/merchant/aftersale/process/${item.id}`, {
      agree,
      reply: reply || ''
    })

    if (res.code === 200) {
      ElMessage.success(`已成功${actionText}退款申请`)
      loadAfterSales()
    } else {
      ElMessage.error(res.message || '处理失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('处理失败', error)
    }
  }
}

// 查看详情
const viewDetail = async (item) => {
  try {
    // 这里可以调用详情接口，暂时使用列表数据
    currentDetail.value = item
    detailDrawerVisible.value = true
  } catch (error) {
    ElMessage.error('加载详情失败')
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
  loadAfterSales()
})
</script>

<style scoped>
.aftersales-page {
  padding: 24px;
  background: #f5f7fa;
  min-height: 100vh;
  font-family: 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

/* 页面标题区 */
.page-header {
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e6e6e6;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0;
}

/* 筛选卡片 */
.filter-card {
  margin-bottom: 16px;
  border-radius: 8px;
  background: #ffffff;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.04);
}

.filter-card :deep(.el-card__body) {
  padding: 20px;
}

.filter-form {
  margin: 0;
}

.filter-form :deep(.el-form-item) {
  margin-bottom: 0;
  margin-right: 24px;
}

.filter-form :deep(.el-form-item__label) {
  font-weight: 500;
  color: #2c3e50;
  padding-right: 12px;
}

/* 批量操作栏 */
.batch-actions-card {
  margin-bottom: 16px;
  border-radius: 8px;
  background: #fff3cd;
  border: 1px solid #ffc107;
}

.batch-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.batch-btns {
  display: flex;
  gap: 8px;
}

/* 表格卡片 */
.table-card {
  border-radius: 8px;
  background: #ffffff;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.04);
}

/* 标签页样式 */
.status-tabs {
  margin-bottom: 16px;
}

.status-tabs :deep(.el-tabs__header) {
  margin: 0;
  border-bottom: 1px solid #e6e6e6;
}

.status-tabs :deep(.el-tabs__item) {
  font-size: 14px;
  color: #7f8c8d;
  font-weight: 400;
  padding: 0 20px;
  height: 48px;
  line-height: 48px;
}

.status-tabs :deep(.el-tabs__item:hover) {
  color: #2ecc71;
}

.status-tabs :deep(.el-tabs__item.is-active) {
  color: #2ecc71;
  font-weight: 600;
}

.status-tabs :deep(.el-tabs__active-bar) {
  background: #2ecc71;
  height: 3px;
}

/* 表格样式 */
.aftersales-table {
  width: 100%;
}

.pagination-wrapper {
  margin-top: 16px;
  display: flex;
  justify-content: center;
}

.aftersales-table :deep(.el-table__header-wrapper) {
  background: #eaecef;
}

.aftersales-table :deep(.el-table__header th) {
  background: #eaecef;
  color: #2c3e50;
  font-weight: 600;
  font-size: 15px;
  padding: 12px 16px;
}

.aftersales-table :deep(.el-table__body tr) {
  height: 52px;
}

.aftersales-table :deep(.el-table__body td) {
  padding: 12px 16px;
  vertical-align: middle;
}

.aftersales-table :deep(.el-table__body tr:hover) {
  background: #ecf0f1;
}

.aftersales-table :deep(.el-table__row) {
  background: #ffffff;
}

.aftersales-table :deep(.el-table--striped .el-table__body tr.el-table__row--striped) {
  background: #f8f9fa;
}

.aftersales-table :deep(.el-table__border) {
  border: none;
}

.aftersales-table :deep(.el-table__inner-wrapper::before) {
  display: none;
}

.aftersales-table :deep(.el-table td) {
  border-bottom: 1px solid #e6e6e6;
}

/* 订单号 */
.order-no {
  color: #3498db;
  cursor: pointer;
  font-size: 13px;
}

.order-no:hover {
  text-decoration: underline;
}

/* 类型列 */
.type-cell {
  display: flex;
  align-items: center;
  gap: 6px;
}

.type-icon {
  font-size: 16px;
}

.return-icon {
  color: #3498db;
}

.refund-icon {
  color: #2ecc71;
}

/* 退款金额 */
.amount-text {
  text-align: right;
  display: block;
  font-size: 14px;
  color: #2c3e50;
  font-weight: 500;
}

.amount-warning {
  color: #e74c3c;
  font-weight: 600;
}

/* 状态标签 */
.status-tag {
  border-radius: 16px;
  padding: 4px 12px;
  font-size: 12px;
  border: none;
}

.status-tag :deep(.el-tag__content) {
  font-weight: 500;
}

/* 待处理 - 橙色 */
.aftersales-table :deep(.el-tag--warning) {
  background: #f39c12;
  color: #ffffff;
}

/* 已同意 - 绿色 */
.aftersales-table :deep(.el-tag--success) {
  background: #2ecc71;
  color: #ffffff;
}

/* 已驳回 - 红色 */
.aftersales-table :deep(.el-tag--danger) {
  background: #e74c3c;
  color: #ffffff;
}

/* 操作按钮 */
.aftersales-table :deep(.el-button--success.is-link) {
  color: #2ecc71;
}

.aftersales-table :deep(.el-button--danger.is-link) {
  color: #e74c3c;
}

.aftersales-table :deep(.el-button--primary.is-link) {
  color: #3498db;
}

.aftersales-table :deep(.el-button.is-link) {
  padding: 4px 8px;
  font-size: 13px;
}

.aftersales-table :deep(.el-button.is-link:hover) {
  transform: scale(1.05);
}

.aftersales-table :deep(.el-button.is-link:active) {
  transform: scale(0.98);
}

/* 详情抽屉 */
.detail-content {
  padding: 20px;
}

.images-section {
  margin-top: 24px;
}

.images-section h3 {
  font-size: 16px;
  color: #2c3e50;
  margin-bottom: 12px;
}

.image-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.detail-image {
  width: 120px;
  height: 120px;
  border-radius: 8px;
  cursor: pointer;
}

.product-thumb {
  width: 48px;
  height: 48px;
  border-radius: 4px;
  object-fit: cover;
}

/* 响应式适配 */
@media (max-width: 1200px) {
  .aftersales-table :deep(.el-table__body-wrapper) {
    overflow-x: auto;
  }
}

@media (max-width: 768px) {
  .filter-form {
    flex-direction: column;
  }

  .filter-form :deep(.el-form-item) {
    margin-right: 0;
    margin-bottom: 12px;
  }
}
</style>
