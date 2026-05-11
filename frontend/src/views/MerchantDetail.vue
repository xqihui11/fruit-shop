<template>
  <div class="merchant-detail-page">
    <div v-if="merchant" class="merchant-header">
      <el-card>
        <div class="merchant-header-inner">
          <div class="merchant-avatar">
            <img
              v-if="merchant.avatar"
              :src="getImageUrl(merchant.avatar)"
              :alt="merchant.shopName"
            />
            <div v-else class="avatar-placeholder">
              {{ (merchant.shopName || '店铺').slice(0, 1) }}
            </div>
          </div>

          <div class="merchant-info">
            <div class="merchant-title-row">
              <div class="merchant-title">
                <h2 class="merchant-name">{{ merchant.shopName }}</h2>
                <div v-if="averageRating !== null" class="merchant-rating">
                  <el-rate
                    :model-value="averageRating"
                    disabled
                    allow-half
                    :colors="['#FFC107', '#FFC107', '#FFC107']"
                  />
                  <span class="rating-score">
                    {{ (averageRating || 0).toFixed(1) }}
                  </span>
                </div>
              </div>
              <div class="merchant-actions">
                <el-button
                  :type="isFavorite ? 'warning' : 'primary'"
                  @click="toggleFavorite"
                >
                  {{ isFavorite ? '已收藏' : '收藏店铺' }}
                </el-button>
                <el-button type="success" @click="goChat">
                  私聊商家
                </el-button>
              </div>
            </div>

            <p class="merchant-desc">{{ merchant.shopDescription || '暂无描述' }}</p>
            <div class="merchant-meta">
              <span class="meta-status">
                营业状态：{{ getBusinessStatus(merchant) }}
              </span>
              <span v-if="merchant.shopAddress">地址：{{ merchant.shopAddress }}</span>
              <span v-if="merchant.businessHours">营业时间：{{ merchant.businessHours }}</span>
              <span v-if="merchant.contactName">联系人：{{ merchant.contactName }}</span>
              <span v-if="merchant.contactPhone">联系电话：{{ merchant.contactPhone }}</span>
              <span v-if="merchant.contactEmail">邮箱：{{ merchant.contactEmail }}</span>
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <el-tabs v-model="activeTab" class="merchant-tabs">
      <el-tab-pane label="商品列表" name="products">
        <div class="product-list">
          <el-row :gutter="20">
            <el-col
              v-for="product in products"
              :key="product.id"
              :xs="12"
              :sm="8"
              :md="6"
              :lg="6"
            >
              <el-card
                class="product-card"
                shadow="hover"
                @click="$router.push(`/product/${product.id}`)"
              >
                <div class="product-image">
                  <img
                    v-if="product.mainImage"
                    :src="getImageUrl(product.mainImage)"
                    :alt="product.name"
                  />
                  <div v-else class="no-image">暂无图片</div>
                </div>
                <div class="product-info">
                  <h3 class="product-name">{{ product.name }}</h3>
                  <div class="product-price">
                    <span class="current-price">¥{{ product.price }}</span>
                    <span v-if="product.originalPrice" class="original-price">
                      ¥{{ product.originalPrice }}
                    </span>
                  </div>
                  <div class="product-meta">
                    <span>销量: {{ product.salesCount || 0 }}</span>
                    <span>新鲜度: {{ product.freshnessLevel || 5 }}星</span>
                  </div>
                </div>
              </el-card>
            </el-col>
          </el-row>
          <el-empty v-if="products.length === 0" description="该店铺暂无商品" />
        </div>
      </el-tab-pane>
      <el-tab-pane label="店铺评价" name="reviews">
        <div class="reviews-section">
          <div v-if="reviews.length === 0" class="no-reviews">
            <el-empty description="暂无评价" />
          </div>
          <div v-else class="review-list">
            <div v-for="review in reviews" :key="review.id" class="review-item">
              <div class="review-header">
                <span class="review-user">{{ review.username || '匿名用户' }}</span>
                <el-rate v-model="review.rating" disabled />
                <span class="review-time">{{ formatTime(review.createTime) }}</span>
              </div>
              <div class="review-content">{{ review.content || '暂无评价内容' }}</div>
              <div v-if="review.productName" class="review-product">
                商品：{{ review.productName }}
              </div>
            </div>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import api from '@/utils/api'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const merchant = ref(null)
const products = ref([])
const reviews = ref([])
const averageRating = ref(0)
const isFavorite = ref(false)
const activeTab = ref('products')

const loadMerchantDetail = async () => {
  try {
    const merchantId = route.params.id
    const res = await api.get(`/merchant/detail/${merchantId}`)
    if (res.code === 200) {
      merchant.value = res.data
      checkFavorite()
      loadProducts()
      loadReviews()
    }
  } catch (error) {
    ElMessage.error('加载店铺信息失败')
  }
}

const loadProducts = async () => {
  try {
    const merchantId = route.params.id
    const res = await api.get('/product/list', {
      params: { merchantId }
    })
    if (res.code === 200) {
      const list = Array.isArray(res.data) ? res.data : []
      products.value = shuffle(list.slice())
    }
  } catch (error) {
    console.error('加载商品失败', error)
  }
}

// Fisher–Yates 洗牌：随机打乱数组
const shuffle = (arr) => {
  for (let i = arr.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1))
    ;[arr[i], arr[j]] = [arr[j], arr[i]]
  }
  return arr
}

const loadReviews = async () => {
  try {
    const merchantId = route.params.id
    const res = await api.get(`/review/merchant/${merchantId}`)
    if (res.code === 200) {
      const data = res.data || {}
      reviews.value = data.reviews || []
      const r = Number(data.averageRating)
      averageRating.value = Number.isFinite(r) ? r : 0
    }
  } catch (error) {
    console.error('加载评价失败', error)
  }
}

const checkFavorite = async () => {
  if (!userStore.user) return
  try {
    const merchantId = route.params.id
    const res = await api.get(`/favorite/merchant/check/${merchantId}`)
    if (res.code === 200) {
      // 处理布尔值或字符串类型的返回值
      isFavorite.value = res.data === true || res.data === 'true' || res.data === 1
    }
  } catch (error) {
    // 未登录或未收藏时忽略错误
    console.debug('检查收藏状态失败:', error)
    isFavorite.value = false
  }
}

const toggleFavorite = async () => {
  if (!userStore.user) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  try {
    const merchantId = Number(route.params.id) // 确保是数字类型
    if (isFavorite.value) {
      const res = await api.delete(`/favorite/merchant/remove/${merchantId}`)
      if (res.code === 200) {
        ElMessage.success('取消收藏成功')
        isFavorite.value = false
        window.dispatchEvent(new CustomEvent('favorite-merchants-changed'))
      } else {
        ElMessage.error(res.message || '取消收藏失败')
      }
    } else {
      const res = await api.post('/favorite/merchant/add', { merchantId: merchantId })
      if (res.code === 200) {
        ElMessage.success('收藏成功')
        isFavorite.value = true
        window.dispatchEvent(new CustomEvent('favorite-merchants-changed'))
      } else {
        ElMessage.error(res.message || '收藏失败')
      }
    }
  } catch (error) {
    const errorMsg = error.response?.data?.message || error.message || '操作失败'
    ElMessage.error(errorMsg)
    console.error('收藏操作失败:', error)
  }
}

const goChat = () => {
  if (!userStore.user) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  const merchantId = route.params.id
  router.push(`/chat/${merchantId}`)
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

const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN')
}

// 解析营业时间字符串，计算当前是否营业；同时支持商家手动设置营业状态
// businessStatus: 0-自动判断，1-强制营业中，2-强制休息中
// businessHours 支持格式示例：09:00-21:00、9:00-23:30
const getBusinessStatus = (merchant) => {
  if (!merchant) return '未知'

  const manual = Number(merchant.businessStatus)
  if (manual === 1) return '营业中'
  if (manual === 2) return '休息中'

  const businessHours = merchant.businessHours
  if (!businessHours) return '未知'
  const parts = String(businessHours).split('-')
  if (parts.length !== 2) return '未知'

  const parseToMinutes = (str) => {
    const m = String(str).trim().match(/^(\d{1,2}):(\d{2})$/)
    if (!m) return null
    const h = Number(m[1])
    const mi = Number(m[2])
    if (h < 0 || h > 23 || mi < 0 || mi > 59) return null
    return h * 60 + mi
  }

  const start = parseToMinutes(parts[0])
  const end = parseToMinutes(parts[1])
  if (start == null || end == null) return '未知'

  const now = new Date()
  const nowMinutes = now.getHours() * 60 + now.getMinutes()

  // 处理跨天营业，例如 22:00-06:00
  let isOpen
  if (end > start) {
    isOpen = nowMinutes >= start && nowMinutes <= end
  } else {
    isOpen = nowMinutes >= start || nowMinutes <= end
  }

  return isOpen ? '营业中' : '休息中'
}

onMounted(() => {
  // 保留 onMounted 兜底：首次进入时会拉取数据
  loadMerchantDetail()
})

// 当路由参数变化（例如同一组件复用但 id 变了）时，确保重新加载
watch(
  () => route.params.id,
  () => {
    merchant.value = null
    products.value = []
    reviews.value = []
    averageRating.value = 0
    isFavorite.value = false
    loadMerchantDetail()
  }
)
</script>

<style scoped>
.merchant-detail-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

.merchant-header {
  margin-bottom: 24px;
}

.merchant-header-inner {
  display: flex;
  align-items: flex-start;
  gap: 16px;
}

.merchant-avatar {
  width: 64px;
  height: 64px;
  border-radius: 12px;
  overflow: hidden;
  flex: 0 0 64px;
  background: #ffffff;
  border: 1px solid #f0f0f0;
}

.merchant-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  background: #e8f5e9;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #2ecc71;
  font-size: 20px;
  font-weight: 800;
}

.merchant-info {
  flex: 1;
}

.merchant-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.merchant-title {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.merchant-name {
  font-size: 24px;
  color: #262626;
  margin: 0;
  font-weight: 700;
}

.merchant-rating {
  display: flex;
  align-items: center;
  gap: 6px;
}

.rating-score {
  font-size: 13px;
  color: #7f8c8d;
}

.merchant-desc {
  color: #595959;
  font-size: 14px;
  margin-bottom: 16px;
  line-height: 1.6;
}

.merchant-meta {
  display: flex;
  gap: 24px;
  font-size: 14px;
  color: #8c8c8c;
  margin-bottom: 16px;
}

.meta-status {
  font-weight: 600;
  color: #27ae60;
}

.merchant-actions {
  margin-top: 16px;
}

.merchant-tabs {
  margin-top: 24px;
}

.product-list {
  margin-top: 16px;
}

.product-card {
  margin-bottom: 20px;
  cursor: pointer;
  transition: all 0.3s;
}

.product-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.product-image {
  width: 100%;
  height: 200px;
  overflow: hidden;
  border-radius: 4px;
  margin-bottom: 12px;
  background: #f5f7fa;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.no-image {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #8c8c8c;
}

.product-info {
  padding: 0 4px;
}

.product-name {
  font-size: 16px;
  color: #262626;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-price {
  margin-bottom: 8px;
}

.current-price {
  font-size: 20px;
  color: #ff4d4f;
  font-weight: 600;
  margin-right: 8px;
}

.original-price {
  font-size: 14px;
  color: #8c8c8c;
  text-decoration: line-through;
}

.product-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #8c8c8c;
}

.reviews-section {
  margin-top: 16px;
}

.review-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.review-item {
  padding: 16px;
  background: #fafafa;
  border-radius: 8px;
}

.review-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.review-user {
  font-weight: 500;
  color: #262626;
}

.review-time {
  margin-left: auto;
  color: #8c8c8c;
  font-size: 12px;
}

.review-content {
  color: #595959;
  line-height: 1.6;
  margin-bottom: 8px;
}

.review-product {
  font-size: 12px;
  color: #8c8c8c;
}
</style>

