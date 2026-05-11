<template>
  <div class="order-container">
    <el-tabs v-model="activeTab" @tab-change="loadOrders">
      <el-tab-pane label="全部订单" name=""></el-tab-pane>
      <el-tab-pane label="待支付" name="0"></el-tab-pane>
      <el-tab-pane label="待发货" name="1"></el-tab-pane>
      <el-tab-pane label="待收货" name="2"></el-tab-pane>
      <el-tab-pane label="待评价" name="3"></el-tab-pane>
      <el-tab-pane label="已完成" name="4"></el-tab-pane>
    </el-tabs>

    <div class="order-list">
      <el-card
        v-for="order in orders"
        :key="order.id"
        class="order-card"
        :class="{ 'order-card-highlight': highlightOrderId === Number(order.id) }"
        shadow="hover"
      >
        <div class="order-header">
          <div class="order-info">
            <span class="order-no">订单号：{{ order.orderNo }}</span>
            <span class="order-time">{{ formatTime(order.createTime) }}</span>
            <!-- 商家头像 + 名称 -->
            <div v-if="order.merchantName || order.merchantAvatar || order.merchantId" class="order-merchant">
              <div class="merchant-avatar">
                <img
                  v-if="order.merchant?.avatar || order.merchantAvatar"
                  :src="getImageUrl(order.merchant?.avatar || order.merchantAvatar)"
                  :alt="order.merchant?.shopName || order.merchantName || '商家头像'"
                />
                <div v-else class="avatar-placeholder">
                  {{ (order.merchant?.shopName || order.merchantName || '店').slice(0, 1) }}
                </div>
              </div>
              <div class="merchant-meta">
                <div class="merchant-name">
                  {{ order.merchant?.shopName || order.merchantName || '商家' }}
                </div>
                <div class="merchant-tag">商家</div>
              </div>
            </div>
          </div>
          <el-tag :type="getStatusType(order.orderStatus, order)">
            {{ getStatusText(order.orderStatus, order) }}
          </el-tag>
        </div>
        <div class="order-content">
          <!-- 订单商品列表 -->
          <div v-if="order.items && order.items.length > 0" class="order-products">
            <div v-for="item in order.items" :key="item.id" class="order-product-item">
              <img 
                v-if="item.productImage" 
                :src="getImageUrl(item.productImage)" 
                class="product-image"
                @error="handleImageError"
              />
              <div v-else class="product-image no-image">暂无图片</div>
              <div class="product-info">
                <div class="product-name">{{ item.productName }}</div>
                <div v-if="item.specName" class="product-spec">{{ item.specName }}</div>
                <div class="product-quantity">x{{ item.quantity }}</div>
              </div>
            </div>
          </div>
          <div class="order-amount">
            <span>订单金额：</span>
            <span class="amount">¥{{ order.payAmount }}</span>
          </div>
          <div v-if="order.logisticsNo" class="logistics-info">
            <span>物流：{{ order.logisticsCompany }} {{ order.logisticsNo }}</span>
          </div>
        </div>
        <div class="order-actions">
          <el-button
            v-if="order.orderStatus === 0"
            type="primary"
            @click="payOrder(order)"
          >
            去支付
          </el-button>
          <el-button
            v-if="order.orderStatus === 0"
            @click="cancelOrder(order.id)"
          >
            取消订单
          </el-button>
          <el-button
            v-if="order.orderStatus === 2"
            type="primary"
            @click="confirmReceive(order.id)"
          >
            确认收货
          </el-button>
          <el-button
            v-if="order.orderStatus === 3"
            type="success"
            @click="showReviewDialog(order)"
          >
            去评价
          </el-button>
          <el-button
            v-if="order.orderStatus >= 2 && order.orderStatus <= 4 && !order.afterSalePending"
            @click="showAfterSaleDialog(order)"
          >
            申请售后
          </el-button>
          <el-button type="text" @click="viewDetail(order)">
            查看详情
          </el-button>
        </div>
      </el-card>
      <el-empty v-if="orders.length === 0" description="暂无订单" />
    </div>

    <!-- 支付对话框（本地模拟支付，可选择成功/失败） -->
    <el-dialog v-model="payDialogVisible" title="订单支付（模拟）" width="420px">
      <div class="pay-dialog-content">
        <div class="pay-info">
          <p>订单号：{{ currentOrder?.orderNo }}</p>
          <p class="pay-amount">支付金额：<span>¥{{ currentOrder?.payAmount }}</span></p>
        </div>
        <div class="pay-method">
          <p>说明</p>
          <p class="pay-tip">
            当前环境为<strong>本地模拟支付</strong>，不会真实扣款，点击“确认支付”即视为支付成功。
          </p>
        </div>
        <div class="pay-method">
          <p>模拟结果</p>
          <el-radio-group v-model="payResult">
            <el-radio label="success">支付成功</el-radio>
            <el-radio label="fail">支付失败</el-radio>
          </el-radio-group>
          <el-input
            v-if="payResult === 'fail'"
            v-model="failReason"
            placeholder="可选：填写失败原因，例如 余额不足 / 用户取消"
            style="margin-top: 12px"
          />
        </div>
      </div>
      <template #footer>
        <el-button @click="payDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="startPay" :loading="paying">
          确认（模拟）
        </el-button>
      </template>
    </el-dialog>

    <!-- 评价对话框 -->
    <el-dialog v-model="reviewDialogVisible" title="商品评价" width="600px">
      <div v-if="reviewOrderItems.length > 0" class="review-products">
        <p class="review-tip">请选择要评价的商品：</p>
        <div class="review-product-list">
          <div 
            v-for="item in reviewOrderItems" 
            :key="item.id" 
            class="review-product-item"
            :class="{ selected: reviewForm.productId === item.productId }"
            @click="reviewForm.productId = item.productId"
          >
            <img v-if="item.productImage" :src="item.productImage" class="review-product-image" />
            <div class="review-product-info">
              <div class="review-product-name">{{ item.productName }}</div>
              <div class="review-product-spec" v-if="item.specName">{{ item.specName }}</div>
            </div>
          </div>
        </div>
      </div>
      <el-form :model="reviewForm" label-width="80px" style="margin-top: 16px;">
        <el-form-item label="评分">
          <el-rate v-model="reviewForm.rating" :colors="['#99A9BF', '#F7BA2A', '#FF9900']" />
        </el-form-item>
        <el-form-item label="评价内容">
          <el-input
            v-model="reviewForm.content"
            type="textarea"
            :rows="4"
            placeholder="请输入您的评价..."
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reviewDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReview" :loading="submittingReview" :disabled="!reviewForm.productId">提交评价</el-button>
      </template>
    </el-dialog>

    <!-- 售后申请对话框 -->
    <el-dialog v-model="afterSaleDialogVisible" title="申请售后" width="520px">
      <el-form :model="afterSaleForm" :rules="afterSaleRules" ref="afterSaleFormRef" label-width="80px">
        <el-form-item label="售后类型" prop="type">
          <el-radio-group v-model="afterSaleForm.type">
            <el-radio :label="1">退货退款</el-radio>
            <el-radio :label="2">仅退款</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="申请原因" prop="reason">
          <el-select v-model="afterSaleForm.reason" placeholder="请选择原因" style="width: 100%">
            <el-option label="商品质量问题" value="商品质量问题" />
            <el-option label="商品与描述不符" value="商品与描述不符" />
            <el-option label="收到商品损坏" value="收到商品损坏" />
            <el-option label="发错货" value="发错货" />
            <el-option label="其他原因" value="其他原因" />
          </el-select>
        </el-form-item>
        <el-form-item label="详细描述">
          <el-input
            v-model="afterSaleForm.description"
            type="textarea"
            :rows="3"
            placeholder="请详细描述问题..."
          />
        </el-form-item>
        <el-form-item label="图片凭证" required>
          <el-upload
            class="aftersale-uploader"
            :action="afterSaleUploadAction"
            :headers="afterSaleUploadHeaders"
            list-type="picture-card"
            accept="image/*"
            :before-upload="beforeAfterSaleImageUpload"
            :on-success="handleAfterSaleUploadSuccess"
            :on-remove="handleAfterSaleUploadRemove"
            :file-list="afterSaleFileList"
            :limit="6"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
          <div class="upload-tip">最多上传 6 张，支持 jpg/png，单张不超过 5MB</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="afterSaleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAfterSale" :loading="submittingAfterSale">提交申请</el-button>
      </template>
    </el-dialog>

    <!-- 订单详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="订单详情" width="700px">
      <div v-if="currentOrder" class="order-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单号">{{ currentOrder.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="订单状态">{{ getStatusText(currentOrder.orderStatus) }}</el-descriptions-item>
          <el-descriptions-item label="下单时间">{{ formatTime(currentOrder.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="支付方式">{{ currentOrder.paymentMethod === 'alipay' ? '支付宝' : '-' }}</el-descriptions-item>
          <el-descriptions-item label="订单金额">¥{{ currentOrder.totalAmount }}</el-descriptions-item>
          <el-descriptions-item label="实付金额">¥{{ currentOrder.payAmount }}</el-descriptions-item>
          <el-descriptions-item v-if="currentOrder.logisticsCompany" label="物流公司">{{ currentOrder.logisticsCompany }}</el-descriptions-item>
          <el-descriptions-item v-if="currentOrder.logisticsNo" label="物流单号">{{ currentOrder.logisticsNo }}</el-descriptions-item>
          <el-descriptions-item v-if="currentOrder.remark" label="备注" :span="2">{{ currentOrder.remark }}</el-descriptions-item>
        </el-descriptions>
        
        <!-- 订单商品列表 -->
          <div class="order-items-section" v-if="orderItems.length > 0">
          <h4>商品信息</h4>
          <div class="order-items-list">
            <div v-for="item in orderItems" :key="item.id" class="order-item">
              <img
                v-if="item.productImage"
                :src="getImageUrl(item.productImage)"
                class="item-image"
                @error="handleImageError"
              />
              <div v-else class="item-image no-image">暂无图片</div>
              <div class="item-info">
                <div class="item-name">{{ item.productName }}</div>
                <div class="item-spec" v-if="item.specName">规格：{{ item.specName }}</div>
              </div>
              <div class="item-price">¥{{ item.price }}</div>
              <div class="item-quantity">x{{ item.quantity }}</div>
              <div class="item-subtotal">¥{{ item.subtotal }}</div>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '@/utils/api'
import { Plus } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()

const activeTab = ref('')
const orders = ref([])
const currentOrder = ref(null)
const userStore = useUserStore()

// 支付相关（改为简单本地模拟支付，可选择成功/失败）
const payDialogVisible = ref(false)
const paymentMethod = ref('mock')
const paying = ref(false)
const payResult = ref('success') // success / fail
const failReason = ref('')

// 评价相关
const reviewDialogVisible = ref(false)
const submittingReview = ref(false)
const reviewOrderItems = ref([])
const reviewForm = ref({
  rating: 5,
  content: '',
  productId: null
})

// 售后相关
const afterSaleDialogVisible = ref(false)
const afterSaleFormRef = ref(null)
const submittingAfterSale = ref(false)
const afterSaleForm = ref({
  type: 2,
  reason: '',
  description: ''
})
const afterSaleRules = {
  type: [{ required: true, message: '请选择售后类型', trigger: 'change' }],
  reason: [{ required: true, message: '请选择申请原因', trigger: 'change' }]
}
const afterSaleUploadAction = '/api/file/upload'
const afterSaleUploadHeaders = {
  Authorization: `Bearer ${userStore.token || ''}`
}
const afterSaleFileList = ref([])
const afterSaleImageUrls = ref([])

const MAX_IMAGE_SIZE_MB = 5
const beforeAfterSaleImageUpload = (file) => {
  const isImage = file.type?.startsWith('image/')
  const isLtLimit = file.size / 1024 / 1024 < MAX_IMAGE_SIZE_MB
  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLtLimit) {
    ElMessage.error(`图片大小不能超过 ${MAX_IMAGE_SIZE_MB}MB!`)
    return false
  }
  return true
}

// 详情对话框
const detailDialogVisible = ref(false)
const orderItems = ref([])

// 高亮订单 ID，用于从消息跳转后短暂高亮提示
const highlightOrderId = ref(null)

const openOrderFromQuery = async (orderId) => {
  if (!orderId || !orders.value || orders.value.length === 0) return
  const idNum = Number(orderId)
  const target = orders.value.find(o => Number(o.id) === idNum)
  if (!target) return
  await viewDetail(target)

  // 设置高亮并在 5 秒后自动清除
  highlightOrderId.value = idNum
  // 滚动到该订单卡片位置
  setTimeout(() => {
    const el = document.querySelector('.order-card-highlight')
    if (el) {
      el.scrollIntoView({ behavior: 'smooth', block: 'center' })
    }
  }, 100)
  setTimeout(() => {
    if (highlightOrderId.value === idNum) {
      highlightOrderId.value = null
    }
  }, 5000)
}

const loadOrders = async () => {
  try {
    const params = activeTab.value ? { status: parseInt(activeTab.value) } : {}
    const res = await api.get('/order/list', { params })
    if (res.code === 200) {
      orders.value = res.data || []

      // 先加载当前用户的售后记录，标记哪些订单存在未完结售后（0/1/4）
      const pendingOrderIds = new Set()
      try {
        const afterRes = await api.get('/aftersale/user/list')
        if (afterRes.code === 200) {
          ;(afterRes.data || []).forEach(a => {
            if ([0, 1, 4].includes(Number(a.status))) {
              pendingOrderIds.add(a.orderId)
            }
          })
        }
      } catch (e) {
        console.warn('加载售后记录失败，不影响订单列表显示', e)
      }

      // 为每个订单加载商品信息，并附加 afterSalePending 标记
      for (const order of orders.value) {
        order.afterSalePending = pendingOrderIds.has(order.id)
        try {
          const itemsRes = await api.get(`/order/items/${order.id}`)
          if (itemsRes.code === 200 && itemsRes.data) {
            order.items = itemsRes.data
          }
        } catch (error) {
          console.error(`加载订单 ${order.id} 的商品失败`, error)
          order.items = []
        }
      }
      // 如果地址栏携带了 orderId，则在列表加载后自动打开对应订单详情
      if (route.query.orderId) {
        openOrderFromQuery(route.query.orderId)
      }
    }
  } catch (error) {
    console.error('加载订单失败', error)
  }
}

const getImageUrl = (image) => {
  if (!image) return ''
  if (image.startsWith('http://') || image.startsWith('https://')) {
    return image
  }
  if (image.startsWith('/uploads/')) {
    return '/api' + image
  }
  if (image.startsWith('/api/uploads/')) {
    return image
  }
  return '/api/uploads/' + image
}

const handleImageError = (event) => {
  event.target.style.display = 'none'
  const parent = event.target.parentElement
  if (parent && !parent.querySelector('.no-image')) {
    const noImageDiv = document.createElement('div')
    noImageDiv.className = 'no-image'
    noImageDiv.textContent = '暂无图片'
    parent.appendChild(noImageDiv)
  }
}

const getStatusText = (status, order) => {
  // 如果订单存在未完结售后，则优先显示“售后处理中”
  if (order?.afterSalePending) {
    return '售后处理中'
  }
  const statusMap = {
    0: '待支付',
    1: '待发货',
    2: '待收货',
    3: '待评价',
    4: '已完成',
    5: '已取消'
  }
  return statusMap[status] || '未知'
}

const getStatusType = (status, order) => {
  if (order?.afterSalePending) {
    return 'warning'
  }
  const typeMap = {
    0: 'warning',
    1: 'info',
    2: 'primary',
    3: 'success',
    4: 'success',
    5: 'info'
  }
  return typeMap[status] || 'info'
}

const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN')
}

const payOrder = (order) => {
  currentOrder.value = order
  // 每次打开支付弹窗时重置模拟结果与失败原因
  payResult.value = 'success'
  failReason.value = ''
  payDialogVisible.value = true
}

// 本地模拟支付：直接调用后端模拟接口，不再弹出二维码和轮询，可选择成功/失败
const startPay = async () => {
  if (!currentOrder.value) return
  paying.value = true
  try {
    const isSuccess = payResult.value === 'success'
    const payload = {
      orderId: currentOrder.value.id,
      success: isSuccess
    }
    if (!isSuccess && failReason.value) {
      // 后端兼容 failReason / failureReason 字段
      payload.failReason = failReason.value
    }

    const res = await api.post('/payment/simulateByOrder', payload)
    if (res.code !== 200) {
      ElMessage.error(res.message || '模拟支付失败')
      return
    }

    payDialogVisible.value = false
    if (isSuccess) {
      ElMessage.success('支付成功（模拟）')
    } else {
      ElMessage.error(failReason.value || '支付失败（模拟）')
    }

    const amount = currentOrder.value?.payAmount
    const orderNo = currentOrder.value?.orderNo

    // 跳转到支付结果页
    router.push({
      path: '/payment-result',
      query: {
        status: isSuccess ? 'success' : 'fail',
        orderId: currentOrder.value?.id,
        orderNo,
        amount,
        message: isSuccess
          ? '支付成功（模拟）'
          : failReason.value || '支付失败（模拟）'
      }
    })

    // 刷新订单列表状态
    loadOrders()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '模拟支付失败')
  } finally {
    paying.value = false
  }
}

const cancelOrder = async (id) => {
  try {
    const { value: reason } = await ElMessageBox.prompt('请输入取消原因', '取消订单', {
      inputPlaceholder: '请输入取消原因',
      inputValidator: (value) => {
        if (!value) {
          return '请输入取消原因'
        }
        return true
      }
    })
    const res = await api.post(`/order/cancel/${id}`, { reason })
    if (res.code === 200) {
      ElMessage.success('订单已取消')
      loadOrders()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('取消失败')
    }
  }
}

const confirmReceive = async (id) => {
  try {
    await ElMessageBox.confirm('确认已收到商品？', '确认收货', {
      type: 'warning'
    })
    const res = await api.post(`/order/confirm/${id}`)
    if (res.code === 200) {
      ElMessage.success('确认收货成功')
      loadOrders()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

const showReviewDialog = async (order) => {
  currentOrder.value = order
  reviewForm.value = { rating: 5, content: '', productId: null }
  // 加载订单商品
  try {
    const res = await api.get(`/order/items/${order.id}`)
    if (res.code === 200) {
      reviewOrderItems.value = res.data || []
      // 默认选择第一个商品
      if (reviewOrderItems.value.length > 0) {
        reviewForm.value.productId = reviewOrderItems.value[0].productId
      }
    }
  } catch (error) {
    console.error('加载订单商品失败', error)
  }
  reviewDialogVisible.value = true
}

const submitReview = async () => {
  if (!reviewForm.value.productId) {
    ElMessage.warning('请选择要评价的商品')
    return
  }
  submittingReview.value = true
  try {
    const res = await api.post('/review/create', {
      orderId: currentOrder.value.id,
      productId: reviewForm.value.productId,
      rating: reviewForm.value.rating,
      content: reviewForm.value.content
    })
    if (res.code === 200) {
      ElMessage.success('评价成功')
      reviewDialogVisible.value = false
      loadOrders()
    } else {
      ElMessage.error(res.message || '评价失败')
    }
  } catch (error) {
    ElMessage.error('评价失败')
  } finally {
    submittingReview.value = false
  }
}

const showAfterSaleDialog = (order) => {
  currentOrder.value = order
  afterSaleForm.value = { type: 2, reason: '', description: '' }
  afterSaleFileList.value = []
  afterSaleImageUrls.value = []
  afterSaleDialogVisible.value = true
}

const submitAfterSale = async () => {
  try {
    await afterSaleFormRef.value.validate()
    if (afterSaleImageUrls.value.length === 0) {
      ElMessage.warning('请至少上传一张图片凭证')
      return
    }
    submittingAfterSale.value = true
    const res = await api.post('/aftersale/apply', {
      orderId: currentOrder.value.id,
      type: afterSaleForm.value.type,
      reason: afterSaleForm.value.reason,
      description: afterSaleForm.value.description,
      images: afterSaleImageUrls.value.join(','),
      refundAmount: currentOrder.value.payAmount
    })
    if (res.code === 200) {
      ElMessage.success('售后申请提交成功')
      afterSaleDialogVisible.value = false
    } else {
      ElMessage.error(res.message || '提交失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('提交失败')
    }
  } finally {
    submittingAfterSale.value = false
  }
}

const handleAfterSaleUploadSuccess = (response, file, fileList) => {
  if (response.code === 200 && response.data) {
    const url = response.data.url || response.data
    afterSaleImageUrls.value.push(url)
    afterSaleFileList.value = fileList
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error(response.message || '图片上传失败')
  }
}

const handleAfterSaleUploadRemove = (file, fileList) => {
  afterSaleFileList.value = fileList
  const url = file.response?.data?.url || file.response?.data || file.url
  afterSaleImageUrls.value = afterSaleImageUrls.value.filter(u => u !== url)
}

const viewDetail = async (order) => {
  currentOrder.value = order
  detailDialogVisible.value = true
  // 加载订单商品项
  try {
    const res = await api.get(`/order/items/${order.id}`)
    if (res.code === 200) {
      orderItems.value = res.data || []
    }
  } catch (error) {
    console.error('加载订单商品失败', error)
  }
}

onMounted(() => {
  loadOrders()
})

onUnmounted(() => {
  // 当前仅为本地模拟支付，这里无需轮询支付结果，保留钩子避免后续扩展时报错
})

watch(
  () => route.query.orderId,
  (val) => {
    if (val) {
      openOrderFromQuery(val)
    }
  }
)
</script>

<style scoped>
.order-container {
  background: #ffffff;
  padding: 24px;
  border-radius: 8px;
}

.order-list {
  margin-top: 24px;
}

.order-card {
  margin-bottom: 16px;
}

.order-card-highlight {
  border: 2px solid #1890ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.15);
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e8e8e8;
}

.order-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.order-merchant {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 4px;
  font-size: 12px;
  color: #8c8c8c;
}

.order-merchant .merchant-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  overflow: hidden;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.order-merchant .merchant-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.order-merchant .avatar-placeholder {
  font-size: 16px;
  color: #8c8c8c;
}

.order-merchant .merchant-meta {
  display: flex;
  align-items: center;
  gap: 6px;
}

.order-merchant .merchant-name {
  max-width: 160px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  color: #595959;
}

.order-merchant .merchant-tag {
  padding: 0 6px;
  border-radius: 10px;
  background: #f6ffed;
  color: #52c41a;
  border: 1px solid #b7eb8f;
  font-size: 11px;
}

.order-no {
  font-weight: 600;
  color: #262626;
}

.order-time {
  font-size: 12px;
  color: #8c8c8c;
}

.order-content {
  margin-bottom: 16px;
}

.order-products {
  margin-bottom: 16px;
  padding: 12px;
  background: #fafafa;
  border-radius: 8px;
}

.order-product-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}

.order-product-item:last-child {
  border-bottom: none;
}

.product-image {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 4px;
  flex-shrink: 0;
}

.product-image.no-image {
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f0f0f0;
  color: #8c8c8c;
  font-size: 10px;
}

.product-info {
  flex: 1;
  min-width: 0;
}

.product-name {
  color: #262626;
  font-size: 14px;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-spec {
  font-size: 12px;
  color: #8c8c8c;
  margin-bottom: 4px;
}

.product-quantity {
  font-size: 12px;
  color: #595959;
}

.order-amount {
  font-size: 16px;
  color: #262626;
  margin-top: 12px;
}

.amount {
  font-size: 20px;
  color: #ff4d4f;
  font-weight: 600;
  margin-left: 8px;
}

.logistics-info {
  margin-top: 8px;
  font-size: 14px;
  color: #595959;
}

.order-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.pay-dialog-content {
  padding: 16px 0;
}

.pay-info {
  margin-bottom: 24px;
}

.pay-info p {
  margin: 8px 0;
  color: #595959;
}

.pay-amount span {
  font-size: 24px;
  color: #ff4d4f;
  font-weight: 600;
}

.pay-method p {
  margin-bottom: 12px;
  color: #262626;
  font-weight: 500;
}

.pay-option {
  margin-left: 8px;
}

.qr-dialog-content {
  padding: 16px 0;
}

.qr-info {
  margin-bottom: 16px;
}

.qr-box-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.qr-box {
  width: 200px;
  height: 200px;
  border-radius: 12px;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 0 0 1px #d9d9d9 inset;
}

.qr-inner {
  width: 160px;
  height: 160px;
  background-image: repeating-linear-gradient(
      45deg,
      #000,
      #000 4px,
      #fff 4px,
      #fff 8px
    ),
    repeating-linear-gradient(
      -45deg,
      #000,
      #000 4px,
      #fff 4px,
      #fff 8px
    );
  mix-blend-mode: multiply;
  opacity: 0.7;
}

.qr-tip {
  font-size: 12px;
  color: #8c8c8c;
  text-align: center;
}

.order-detail {
  padding: 16px 0;
}

.order-items-section {
  margin-top: 24px;
}

.order-items-section h4 {
  margin-bottom: 12px;
  color: #262626;
}

.order-items-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.order-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #fafafa;
  border-radius: 8px;
}

.item-image {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 4px;
}

.item-image.no-image {
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f0f0f0;
  color: #8c8c8c;
  font-size: 10px;
}

.item-info {
  flex: 1;
}

.item-name {
  color: #262626;
  margin-bottom: 4px;
}

.item-spec {
  font-size: 12px;
  color: #8c8c8c;
}

.item-price, .item-quantity {
  width: 60px;
  text-align: right;
  color: #595959;
}

.item-subtotal {
  width: 80px;
  text-align: right;
  font-weight: 600;
  color: #ff4d4f;
}

.review-products {
  margin-bottom: 16px;
}

.review-tip {
  margin-bottom: 12px;
  color: #595959;
}

.review-product-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.review-product-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  border: 2px solid #e8e8e8;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.review-product-item:hover {
  border-color: #1890ff;
}

.review-product-item.selected {
  border-color: #1890ff;
  background: #e6f7ff;
}

.review-product-image {
  width: 40px;
  height: 40px;
  object-fit: cover;
  border-radius: 4px;
}

.review-product-info {
  max-width: 150px;
}

.review-product-name {
  font-size: 14px;
  color: #262626;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.review-product-spec {
  font-size: 12px;
  color: #8c8c8c;
}
</style>
