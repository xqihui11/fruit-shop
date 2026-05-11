<template>
  <div class="admin-aftersales-page">
    <el-card class="filter-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">售后申诉处理</span>
          <span class="card-subtitle">用户对商家驳回的售后发起的申诉，由平台管理员最终裁决</span>
        </div>
      </template>
      <el-form :inline="true" :model="filterForm" class="filter-form">
        <el-form-item label="订单号">
          <el-input
            v-model="filterForm.orderNo"
            placeholder="请输入订单号"
            clearable
            style="width: 220px"
            @keyup.enter="loadAppeals"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filterForm.status" placeholder="全部" clearable style="width: 160px">
            <el-option label="待管理员处理" :value="4" />
            <el-option label="已完成（同意退款）" :value="3" />
            <el-option label="已驳回申诉" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadAppeals">
            查询
          </el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange" class="status-tabs">
        <el-tab-pane label="待管理员处理" name="pending" />
        <el-tab-pane label="已处理" name="processed" />
      </el-tabs>

      <el-table
        :data="pagedAppeals"
        v-loading="loading"
        stripe
        class="appeals-table"
      >
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
        <el-table-column prop="orderNo" label="订单号" width="200" />
        <el-table-column prop="type" label="类型" width="110">
          <template #default="{ row }">
            <el-tag size="small" :type="row.type === 1 ? 'info' : 'success'">
              {{ row.type === 1 ? '退货退款' : '仅退款' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="refundAmount" label="申请金额" width="130">
          <template #default="{ row }">
            ¥{{ Number(row.refundAmount || 0).toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="原因" min-width="160" />
        <el-table-column prop="status" label="状态" width="130">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="申请时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === 4">
              <el-button
                type="success"
                link
                size="small"
                @click="handleAppeal(row, true)"
              >
                同意退款
              </el-button>
              <el-button
                type="danger"
                link
                size="small"
                @click="handleAppeal(row, false)"
              >
                驳回申诉
              </el-button>
            </template>
            <el-button
              type="primary"
              link
              size="small"
              @click="viewDetail(row)"
            >
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty
        v-if="!loading && filteredAppeals.length === 0"
        description="暂无相关申诉记录"
        :image-size="120"
      />
    </el-card>

    <div class="pagination-wrap">
      <el-pagination
        background
        layout="prev, pager, next"
        :current-page="currentPage"
        :page-size="pageSize"
        :total="filteredAppeals.length"
        @current-change="(p) => (currentPage = p)"
      />
    </div>

    <!-- 详情抽屉 -->
    <el-drawer v-model="detailDrawerVisible" title="申诉详情" size="600px">
      <div v-if="currentDetail" class="detail-content">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="售后ID">{{ currentDetail.id }}</el-descriptions-item>
          <el-descriptions-item label="订单号">{{ currentDetail.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="类型">
            {{ currentDetail.type === 1 ? '退货退款' : '仅退款' }}
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(currentDetail.status)">
              {{ getStatusText(currentDetail.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="申请金额">
            ¥{{ Number(currentDetail.refundAmount || 0).toFixed(2) }}
          </el-descriptions-item>
          <el-descriptions-item label="申请时间">
            {{ formatTime(currentDetail.createTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="原始原因" :span="2">
            {{ currentDetail.reason }}
          </el-descriptions-item>
          <el-descriptions-item v-if="currentDetail.description" label="说明 / 申诉内容" :span="2">
            <pre class="multi-line-text">{{ currentDetail.description }}</pre>
          </el-descriptions-item>
          <el-descriptions-item v-if="currentDetail.returnLogisticsCompany" label="退货物流公司" :span="2">
            {{ currentDetail.returnLogisticsCompany }}
          </el-descriptions-item>
          <el-descriptions-item v-if="currentDetail.returnLogisticsNo" label="退货物流单号" :span="2">
            {{ currentDetail.returnLogisticsNo }}
          </el-descriptions-item>
          <el-descriptions-item v-if="currentDetail.merchantReply" label="商家回复" :span="2">
            {{ currentDetail.merchantReply }}
          </el-descriptions-item>
          <el-descriptions-item v-if="currentDetail.processTime" label="最后处理时间" :span="2">
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
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '@/utils/api'

const appeals = ref([])
const loading = ref(false)
const activeTab = ref('pending')
const currentPage = ref(1)
const pageSize = ref(10)

const filterForm = ref({
  orderNo: '',
  status: null
})

const detailDrawerVisible = ref(false)
const currentDetail = ref(null)

const loadAppeals = async () => {
  loading.value = true
  try {
    // activeTab 决定默认状态：pending=4, processed=2/3
    let statusParam = filterForm.value.status
    if (!statusParam) {
      if (activeTab.value === 'pending') {
        statusParam = 4
      } else {
        // 已处理：管理员可能关心 2 和 3，这里不传 status 让后端自行过滤所有状态，再在前端按 tab 过滤
        statusParam = null
      }
    }
    const params = {}
    if (statusParam != null) params.status = statusParam
    const res = await api.get('/admin/aftersale/appeals', { params })
    if (res.code === 200) {
      appeals.value = res.data || []
    }
  } catch (error) {
    console.error('加载申诉列表失败', error)
    ElMessage.error('加载申诉列表失败')
  } finally {
    loading.value = false
  }
}

const resetFilter = () => {
  filterForm.value = {
    orderNo: '',
    status: null
  }
  loadAppeals()
}

const handleTabChange = () => {
  // 切换标签时重置状态筛选，重新拉取数据
  filterForm.value.status = null
  loadAppeals()
}

const filteredAppeals = computed(() => {
  let list = [...appeals.value]

  // activeTab 进一步按状态区分
  if (activeTab.value === 'pending') {
    list = list.filter(item => item.status === 4)
  } else {
    list = list.filter(item => item.status === 2 || item.status === 3)
  }

  if (filterForm.value.orderNo) {
    const key = filterForm.value.orderNo.trim()
    list = list.filter(item => item.orderNo && item.orderNo.includes(key))
  }

  return list
})

const pagedAppeals = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return filteredAppeals.value.slice(start, start + pageSize.value)
})

const getStatusText = (status) => {
  const map = {
    0: '待商家处理',
    1: '待退货',
    2: '商家已驳回',
    3: '已完成（同意退款）',
    4: '待管理员处理'
  }
  return map[status] || '未知'
}

const getStatusType = (status) => {
  const map = {
    0: 'warning',
    1: 'primary',
    2: 'danger',
    3: 'success',
    4: 'warning'
  }
  return map[status] || 'info'
}

const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN')
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

const handleAppeal = async (item, approve) => {
  const actionText = approve ? '同意退款' : '驳回申诉'
  try {
    const { value: remark } = await ElMessageBox.prompt(
      approve ? '请输入同意退款的说明（可选）' : '请输入驳回申诉的原因',
      actionText,
      {
        inputPlaceholder: approve ? '说明内容（可不填）' : '请输入原因',
        inputValidator: (value) => {
          if (!approve && !value) {
            return '请输入驳回原因'
          }
          return true
        },
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: approve ? 'success' : 'warning'
      }
    )

    const res = await api.post(`/admin/aftersale/appeals/process/${item.id}`, {
      approve,
      remark: remark || ''
    })
    if (res.code === 200) {
      ElMessage.success(`${actionText}成功`)
      loadAppeals()
    } else {
      ElMessage.error(res.message || `${actionText}失败`)
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(`${actionText}失败`)
    }
  }
}

const viewDetail = (item) => {
  currentDetail.value = item
  detailDrawerVisible.value = true
}

onMounted(() => {
  loadAppeals()
})
</script>

<style scoped>
.admin-aftersales-page {
  padding: 24px;
}

.filter-card {
  margin-bottom: 16px;
}

.card-header {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
}

.card-subtitle {
  font-size: 12px;
  color: #909399;
}

.filter-form :deep(.el-form-item) {
  margin-bottom: 0;
  margin-right: 16px;
}

.table-card {
  margin-top: 8px;
}

.status-tabs {
  margin-bottom: 12px;
}

.appeals-table {
  width: 100%;
}

.pagination-wrap {
  margin-top: 16px;
  text-align: right;
}

.detail-content {
  padding: 16px;
}

.multi-line-text {
  white-space: pre-wrap;
  font-family: inherit;
  margin: 0;
}

.product-thumb {
  width: 40px;
  height: 40px;
  border-radius: 4px;
  object-fit: cover;
}
</style>


