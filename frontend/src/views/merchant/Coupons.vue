<template>
  <div class="coupons-page">
    <el-card class="coupons-card" shadow="never">
      <template #header>
        <div class="card-header">
          <div class="card-title-wrap">
            <div class="card-title">优惠券管理</div>
            <div class="card-subtitle">创建和管理店铺优惠券，吸引用户购买（仅限本店使用）</div>
          </div>
          <div class="card-actions">
            <el-button type="primary" @click="showDialog()">➕ 添加优惠券</el-button>
          </div>
        </div>
      </template>

      <!-- 搜索和筛选工具栏 -->
      <div class="toolbar">
        <div class="search-section">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索优惠券名称或ID..."
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
          <el-select v-model="typeFilter" placeholder="优惠类型" clearable @change="handleFilter" style="width: 120px;">
            <el-option label="全部" value="" />
            <el-option label="满减券" :value="1" />
            <el-option label="折扣券" :value="2" />
          </el-select>
        </div>
      </div>

      <!-- 状态标签页 -->
      <el-tabs v-model="activeTab" @tab-change="handleTabChange" class="status-tabs">
        <el-tab-pane label="全部" name=""></el-tab-pane>
        <el-tab-pane label="进行中" name="1"></el-tab-pane>
        <el-tab-pane label="已下架" name="0"></el-tab-pane>
        <el-tab-pane label="已结束" name="2"></el-tab-pane>
      </el-tabs>

      <!-- 优惠券表格 -->
      <el-table 
        ref="tableRef"
        :data="pagedCoupons" 
        stripe 
        class="coupons-table"
        :row-class-name="getRowClassName"
      >
        <el-table-column prop="id" label="ID" width="80" sortable="custom" @sort-change="handleSort" />
        <el-table-column label="优惠券名称" min-width="220">
          <template #default="{ row }">
            <div class="coupon-name-cell">
              <span class="coupon-icon">{{ row.type === 1 ? '🧧' : '💸' }}</span>
              <div class="coupon-name-content">
                <div class="coupon-name">{{ row.name }}</div>
                <div class="coupon-desc">{{ row.description || '暂无描述' }}</div>
                <div class="coupon-tags">
                  <el-tag 
                    type="success" 
                    size="small"
                    class="publisher-tag"
                  >
                    <el-icon style="margin-right: 4px;"><Shop /></el-icon>
                    本店优惠券
                  </el-tag>
                </div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="优惠类型" width="120">
          <template #default="{ row }">
            <el-tag :type="row.type === 1 ? 'success' : 'warning'" class="type-tag">
              {{ row.type === 1 ? '满减券' : '折扣券' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="优惠内容" width="200">
          <template #default="{ row }">
            <div class="coupon-content-cell">
              <div v-if="row.type === 1" class="content-text">
                满{{ formatMoney(row.minAmount) }}减{{ formatMoney(row.discountAmount) }}
              </div>
              <div v-else class="content-text">
                {{ (row.discountRate * 10).toFixed(1) }}折
                <span v-if="row.maxDiscountAmount" class="max-discount">(最高减{{ formatMoney(row.maxDiscountAmount) }})</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="有效期" min-width="180" sortable="custom" @sort-change="handleSort">
          <template #default="{ row }">
            <div class="validity-cell">
              <div class="validity-time">{{ getRelativeTime(row.validEndTime) }}</div>
              <div class="validity-full" :title="`${formatTime(row.validStartTime)} 至 ${formatTime(row.validEndTime)}`">
                {{ formatTime(row.validStartTime) }} 至 {{ formatTime(row.validEndTime) }}
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="发放情况" width="200" sortable="custom" @sort-change="handleSort">
          <template #default="{ row }">
            <div class="issuance-cell">
              <div class="issuance-item">
                <span class="issuance-icon">🎫</span>
                <span>已领：{{ row.receivedCount || 0 }}</span>
              </div>
              <div class="issuance-item">
                <span>已用：{{ row.usedCount || 0 }}</span>
              </div>
              <div class="issuance-progress">
                <el-progress 
                  :percentage="getProgress(row)" 
                  :color="getProgressColor(row)"
                  :show-text="false"
                  :stroke-width="6"
                />
                <span class="progress-text">
                  {{ row.totalCount > 0 ? `${row.receivedCount || 0} / ${row.totalCount}` : '不限量' }}
                </span>
              </div>
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
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button link type="primary" @click="showDialog(row)" class="action-btn">
                <el-icon><Edit /></el-icon>
                编辑
              </el-button>
              <el-button 
                link 
                :type="row.status === 1 ? 'warning' : 'success'"
                @click="toggleStatus(row)"
                class="action-btn"
              >
                <el-icon><Upload v-if="row.status !== 1" /><Download v-else /></el-icon>
                {{ row.status === 1 ? '下架' : '上架' }}
              </el-button>
              <el-button link type="danger" @click="deleteCoupon(row.id)" class="action-btn">
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
        :total="filteredCoupons.length"
        @current-change="(p) => (currentPage = p)"
      />
    </div>

    <!-- 编辑抽屉 -->
    <el-drawer
      v-model="drawerVisible"
      :title="editingCoupon ? '编辑优惠券' : '添加优惠券'"
      direction="rtl"
      size="600px"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
        <el-form-item label="优惠券名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入优惠券名称" />
        </el-form-item>
        <el-form-item label="优惠券类型" prop="type">
          <el-radio-group v-model="form.type">
            <el-radio :label="1">满减券</el-radio>
            <el-radio :label="2">折扣券</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item 
          v-if="form.type === 1" 
          label="优惠金额" 
          prop="discountAmount"
        >
          <el-input-number 
            v-model="form.discountAmount" 
            :min="0.01" 
            :precision="2" 
            :step="1"
            placeholder="请输入优惠金额"
          />
          <span style="margin-left: 10px;">元</span>
        </el-form-item>
        <el-form-item 
          v-if="form.type === 2" 
          label="折扣率" 
          prop="discountRate"
        >
          <el-input-number 
            v-model="form.discountRate" 
            :min="0.01" 
            :max="1" 
            :precision="2" 
            :step="0.1"
            placeholder="如0.9表示9折"
          />
          <span style="margin-left: 10px;">（0.9表示9折）</span>
        </el-form-item>
        <el-form-item 
          v-if="form.type === 2" 
          label="最大优惠金额"
        >
          <el-input-number 
            v-model="form.maxDiscountAmount" 
            :min="0" 
            :precision="2"
            placeholder="可选，限制最大优惠金额"
          />
          <span style="margin-left: 10px;">元（可选）</span>
        </el-form-item>
        <el-form-item label="最低使用金额" prop="minAmount">
          <el-input-number 
            v-model="form.minAmount" 
            :min="0" 
            :precision="2"
            placeholder="订单金额需达到此金额才能使用"
          />
          <span style="margin-left: 10px;">元</span>
        </el-form-item>
        <el-form-item label="发放总数">
          <el-input-number 
            v-model="form.totalCount" 
            :min="0"
            placeholder="0表示不限制"
          />
          <span style="margin-left: 10px;">（0表示不限制）</span>
        </el-form-item>
        <el-form-item label="有效期开始" prop="validStartTime">
          <el-date-picker
            v-model="form.validStartTime"
            type="datetime"
            placeholder="选择开始时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="有效期结束" prop="validEndTime">
          <el-date-picker
            v-model="form.validEndTime"
            type="datetime"
            placeholder="选择结束时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="优惠券描述">
          <el-input 
            v-model="form.description" 
            type="textarea" 
            :rows="3"
            placeholder="请输入优惠券描述"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="0">已下架</el-radio>
            <el-radio :label="1">进行中</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="drawerVisible = false">取消</el-button>
        <el-button type="primary" @click="saveCoupon">保存</el-button>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Edit, Delete, Upload, Download, Shop } from '@element-plus/icons-vue'
import api from '@/utils/api'

const coupons = ref([])
const activeTab = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const dialogVisible = ref(false)
const drawerVisible = ref(false)
const editingCoupon = ref(null)
const formRef = ref(null)
const tableRef = ref(null)
const searchKeyword = ref('')
const typeFilter = ref('')
const sortConfig = ref({ prop: null, order: null })

const form = ref({
  name: '',
  type: 1,
  discountAmount: null,
  discountRate: null,
  minAmount: 0,
  maxDiscountAmount: null,
  totalCount: 0,
  validStartTime: '',
  validEndTime: '',
  description: '',
  status: 1
})

const rules = {
  name: [{ required: true, message: '请输入优惠券名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择优惠券类型', trigger: 'change' }],
  discountAmount: [
    { 
      validator: (rule, value, callback) => {
        if (form.value.type === 1 && (!value || value <= 0)) {
          callback(new Error('请输入优惠金额'))
        } else {
          callback()
        }
      }, 
      trigger: 'blur' 
    }
  ],
  discountRate: [
    { 
      validator: (rule, value, callback) => {
        if (form.value.type === 2 && (!value || value <= 0 || value > 1)) {
          callback(new Error('请输入有效的折扣率（0-1之间）'))
        } else {
          callback()
        }
      }, 
      trigger: 'blur' 
    }
  ],
  minAmount: [{ required: true, message: '请输入最低使用金额', trigger: 'blur' }],
  validStartTime: [{ required: true, message: '请选择有效期开始时间', trigger: 'change' }],
  validEndTime: [{ required: true, message: '请选择有效期结束时间', trigger: 'change' }]
}

const filteredCoupons = computed(() => {
  let list = [...coupons.value]
  
  // 状态筛选
  if (activeTab.value !== '') {
    list = list.filter(c => c.status === parseInt(activeTab.value))
  }
  
  // 关键词搜索
  if (searchKeyword.value.trim()) {
    const keyword = searchKeyword.value.trim().toLowerCase()
    list = list.filter(c => {
      const name = (c.name || '').toLowerCase()
      const id = String(c.id || '')
      return name.includes(keyword) || id.includes(keyword)
    })
  }
  
  // 类型筛选
  if (typeFilter.value !== '') {
    list = list.filter(c => c.type === typeFilter.value)
  }
  
  // 排序
  if (sortConfig.value.prop) {
    list.sort((a, b) => {
      let aVal, bVal
      switch (sortConfig.value.prop) {
        case 'id':
          aVal = a.id || 0
          bVal = b.id || 0
          break
        case 'validEndTime':
          aVal = new Date(a.validEndTime || 0).getTime()
          bVal = new Date(b.validEndTime || 0).getTime()
          break
        case 'receivedCount':
          aVal = a.receivedCount || 0
          bVal = b.receivedCount || 0
          break
        default:
          return 0
      }
      if (sortConfig.value.order === 'ascending') {
        return aVal - bVal
      } else {
        return bVal - aVal
      }
    })
  } else {
    // 默认按ID倒序
    list.sort((a, b) => (b.id || 0) - (a.id || 0))
  }
  
  return list
})

const pagedCoupons = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return filteredCoupons.value.slice(start, start + pageSize.value)
})

const getStatusText = (status) => {
  const map = { 0: '已下架', 1: '进行中', 2: '已结束' }
  return map[status] || '未知'
}

const getStatusClass = (status) => {
  const map = { 0: 'status-disabled', 1: 'status-active', 2: 'status-ended' }
  return map[status] || ''
}

const formatMoney = (amount) => {
  if (!amount) return '0.00'
  return parseFloat(amount).toFixed(2)
}

const formatTime = (time) => {
  if (!time) return '—'
  return new Date(time).toLocaleString('zh-CN')
}

const getRelativeTime = (time) => {
  if (!time) return '—'
  const now = new Date()
  const end = new Date(time)
  const diff = end.getTime() - now.getTime()
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  
  if (days < 0) return '已结束'
  if (days === 0) return '今天结束'
  if (days === 1) return '明天结束'
  if (days <= 7) return `${days}天后结束`
  return `${Math.floor(days / 30)}个月后结束`
}

const getProgress = (row) => {
  if (!row.totalCount || row.totalCount === 0) return 0
  return Math.round(((row.receivedCount || 0) / row.totalCount) * 100)
}

const getProgressColor = (row) => {
  const progress = getProgress(row)
  if (progress >= 80) return '#f56c6c'
  if (progress >= 50) return '#e6a23c'
  return '#67c23a'
}

const getRowClassName = ({ row, rowIndex }) => {
  return 'table-row'
}

const handleSearch = () => {
  currentPage.value = 1
}

const handleFilter = () => {
  currentPage.value = 1
}

const handleSort = ({ prop, order }) => {
  sortConfig.value = { prop, order }
}

const loadCoupons = async () => {
  try {
    const res = await api.get('/merchant/coupon/list')
    if (res.code === 200) {
      coupons.value = res.data || []
    }
  } catch (error) {
    console.error('加载优惠券失败', error)
  }
}

const showDialog = (coupon = null) => {
  editingCoupon.value = coupon
  if (coupon) {
    form.value = {
      id: coupon.id,
      name: coupon.name,
      type: coupon.type,
      discountAmount: coupon.discountAmount,
      discountRate: coupon.discountRate,
      minAmount: coupon.minAmount,
      maxDiscountAmount: coupon.maxDiscountAmount,
      totalCount: coupon.totalCount,
      validStartTime: coupon.validStartTime,
      validEndTime: coupon.validEndTime,
      description: coupon.description,
      status: coupon.status
    }
  } else {
    form.value = {
      name: '',
      type: 1,
      discountAmount: null,
      discountRate: null,
      minAmount: 0,
      maxDiscountAmount: null,
      totalCount: 0,
      validStartTime: '',
      validEndTime: '',
      description: '',
      status: 1
    }
  }
  drawerVisible.value = true
}

const saveCoupon = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    try {
      if (editingCoupon.value) {
        await api.put('/merchant/coupon/update', form.value)
        ElMessage.success('更新成功')
      } else {
        await api.post('/merchant/coupon/add', form.value)
        ElMessage.success('添加成功')
      }
      drawerVisible.value = false
      // 延迟一下再刷新，确保数据库已更新
      setTimeout(() => {
        loadCoupons()
      }, 100)
    } catch (error) {
      ElMessage.error(error.response?.data?.message || '操作失败')
    }
  })
}

const toggleStatus = async (coupon) => {
  const newStatus = coupon.status === 1 ? 0 : 1
  const action = newStatus === 1 ? '上架' : '下架'
  
  try {
    await ElMessageBox.confirm(
      `确认要${action}该优惠券吗？${newStatus === 0 ? '下架后用户将无法领取' : ''}`,
      `${action}确认`,
      { type: 'warning' }
    )
    await api.post(`/merchant/coupon/status/${coupon.id}`, { status: newStatus })
    ElMessage.success(`优惠券已成功${action}`)
    loadCoupons()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '操作失败')
    }
  }
}

const deleteCoupon = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该优惠券吗？', '删除确认', { type: 'warning' })
    await api.delete(`/merchant/coupon/delete/${id}`)
    ElMessage.success('删除成功')
    loadCoupons()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '删除失败')
    }
  }
}

const handleTabChange = () => {
  currentPage.value = 1
}

onMounted(() => {
  loadCoupons()
})
</script>

<style scoped>
.coupons-page {
  padding: 24px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.coupons-card {
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

/* 工具栏 */
.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin: 16px 0;
  padding: 16px;
  background: #ffffff;
  border-radius: 8px;
  border: 1px solid #e8e8e8;
}

.search-section {
  flex: 1;
  max-width: 300px;
}

.search-input {
  width: 100%;
}

.filter-section {
  display: flex;
  align-items: center;
  gap: 12px;
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

/* 表格样式 */
.coupons-table {
  background: #ffffff;
}

:deep(.coupons-table .el-table__header) {
  background: #eaecef;
}

:deep(.coupons-table .el-table__header th) {
  background: #eaecef;
  color: #2c3e50;
  font-weight: 600;
  padding: 12px 16px;
  border-bottom: 2px solid #dee2e6;
}

:deep(.coupons-table .el-table__body tr) {
  height: 52px;
}

:deep(.coupons-table .el-table__body td) {
  padding: 12px 16px;
  vertical-align: middle;
}

:deep(.coupons-table .table-row:hover) {
  background: #ecf0f1 !important;
}

:deep(.coupons-table .el-table__body tr:nth-child(even)) {
  background: #f8f9fa;
}

:deep(.coupons-table .el-table__body tr:nth-child(odd)) {
  background: #ffffff;
}

/* 优惠券名称单元格 */
.coupon-name-cell {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.coupon-icon {
  font-size: 20px;
  line-height: 1;
  margin-top: 2px;
}

.coupon-name-content {
  flex: 1;
}

.coupon-name {
  font-weight: 500;
  color: #2c3e50;
  font-size: 14px;
  margin-bottom: 4px;
}

.coupon-desc {
  font-size: 12px;
  color: #7f8c8d;
  margin-bottom: 6px;
  line-height: 1.4;
}

.coupon-tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.publisher-tag {
  font-size: 11px;
  font-weight: 500;
}

.publisher-tag :deep(.el-icon) {
  font-size: 12px;
}

/* 优惠类型标签 */
.type-tag {
  font-weight: 500;
}

/* 优惠内容 */
.coupon-content-cell {
  font-size: 13px;
  color: #2c3e50;
}

.content-text {
  line-height: 1.6;
}

.max-discount {
  color: #7f8c8d;
  font-size: 12px;
}

/* 有效期 */
.validity-cell {
  font-size: 13px;
}

.validity-time {
  color: #2c3e50;
  font-weight: 500;
  margin-bottom: 4px;
}

.validity-full {
  color: #7f8c8d;
  font-size: 12px;
  cursor: help;
}

/* 发放情况 */
.issuance-cell {
  font-size: 13px;
}

.issuance-item {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 6px;
  color: #2c3e50;
}

.issuance-icon {
  font-size: 14px;
}

.issuance-progress {
  margin-top: 8px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.issuance-progress :deep(.el-progress) {
  flex: 1;
}

.progress-text {
  font-size: 11px;
  color: #7f8c8d;
  white-space: nowrap;
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

.status-active {
  background: #d4edda;
  color: #155724;
}

.status-disabled {
  background: #fff3e0;
  color: #f57c00;
}

.status-ended {
  background: #eaecef;
  color: #6c757d;
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
</style>

