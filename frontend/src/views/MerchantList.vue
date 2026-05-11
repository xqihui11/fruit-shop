<template>
  <div class="merchant-list-page">
    <!-- 标题区 -->
    <div class="page-header">
      <h2 class="page-title">店铺列表</h2>
      <div class="title-divider"></div>

      <!-- 排序栏：默认 / 好评 / 销量 -->
      <div class="sort-bar">
        <el-select v-model="filters.sort" class="sort-select" @change="onSortChange">
          <el-option label="默认排序" value="default" />
          <el-option label="好评排序" value="rating" />
          <el-option label="销量排序" value="sales" />
        </el-select>
      </div>
    </div>

    <!-- 列表 -->
    <div v-if="sortedMerchants.length" class="merchant-list">
      <el-card
        v-for="merchant in sortedMerchants"
        :key="merchant.id"
        class="merchant-item"
        shadow="hover"
        @click="$router.push(`/merchant/${merchant.id}`)"
      >
        <div class="merchant-top">
          <div class="merchant-avatar">
            <img
              v-if="merchant.avatar"
              :src="getImageUrl(merchant.avatar)"
              :alt="merchant.shopName"
            />
            <div v-else class="avatar-placeholder">{{ (merchant.shopName || '店铺').slice(0, 1) }}</div>
          </div>

          <div class="merchant-main">
            <div class="merchant-title-row">
              <div class="merchant-title">
                <span class="merchant-name">{{ merchant.shopName }}</span>
                <div class="merchant-rating">
                  <el-rate
                    :model-value="getRating(merchant)"
                    disabled
                    allow-half
                    :colors="['#FFC107', '#FFC107', '#FFC107']"
                  />
                  <span class="rating-score">{{ getRating(merchant).toFixed(1) }}</span>
                </div>
              </div>
              <div class="merchant-actions">
                <el-button
                  class="fav-btn"
                  link
                  :type="favoriteMerchantIds.has(Number(merchant.id)) ? 'warning' : 'default'"
                  @click.stop="toggleFavoriteMerchant(merchant.id)"
                >
                  <el-icon>
                    <StarFilled v-if="favoriteMerchantIds.has(Number(merchant.id))" />
                    <Star v-else />
                  </el-icon>
                  {{ favoriteMerchantIds.has(Number(merchant.id)) ? '已收藏' : '收藏' }}
                </el-button>
                <el-button
                  type="success"
                  class="chat-btn"
                  @click.stop="goChat(merchant.id)"
                >
                  私聊
                </el-button>
                <el-button
                  type="primary"
                  class="enter-btn"
                  @click.stop="$router.push(`/merchant/${merchant.id}`)"
                >
                  进店
                </el-button>
              </div>
            </div>

            <p class="merchant-desc">{{ merchant.shopDescription || '暂无描述' }}</p>

            <!-- 店铺基础信息：营业状态 / 地址 / 营业时间 / 联系方式（只在有值时展示） -->
            <div class="merchant-meta">
              <span class="meta-item meta-status">
                营业状态：{{ getBusinessStatus(merchant) }}
              </span>
              <span v-if="merchant.shopAddress" class="meta-item">
                地址：{{ merchant.shopAddress }}
              </span>
              <span v-if="merchant.businessHours" class="meta-item">
                营业时间：{{ merchant.businessHours }}
              </span>
              <span v-if="merchant.contactPhone" class="meta-item">
                电话：{{ merchant.contactPhone }}
              </span>
              <span v-if="merchant.contactName" class="meta-item">
                联系人：{{ merchant.contactName }}
              </span>
              <span v-if="merchant.contactEmail" class="meta-item">
                邮箱：{{ merchant.contactEmail }}
              </span>
            </div>

            <!-- 店内热销水果展示（TOP3） -->
            <div class="top-products">
              <template v-if="topProductsMap[merchant.id]?.length">
                <div
                  v-for="p in topProductsMap[merchant.id]"
                  :key="p.id"
                  class="product-tile"
                  @click.stop="$router.push(`/product/${p.id}`)"
                >
                  <img v-if="p.mainImage" :src="getImageUrl(p.mainImage)" :alt="p.name" />
                  <div v-else class="product-placeholder">🍎</div>
                  <div class="product-price">¥{{ formatMoney(p.price) }}</div>
                </div>
              </template>
              <template v-else>
                <div class="top-products-empty">暂无热销商品</div>
              </template>
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <el-empty v-else description="暂无店铺" class="empty-state" :image-size="120" />
  </div>
</template>

<script setup>
import { ref, onMounted, computed, reactive, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Star, StarFilled } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import api from '@/utils/api'

const userStore = useUserStore()
const router = useRouter()

const merchants = ref([])
const filters = ref({
  sort: 'default'
})

// 每个店铺的热销商品 TOP6：{ [merchantId]: Product[] }
const topProductsMap = ref({})
// 店铺评分缓存：{ [merchantId]: number }
const ratingMap = ref({})
// 默认顺序（接口返回顺序）
const defaultOrderIndex = ref({})
// 收藏店铺集合
const favoriteMerchantIds = reactive(new Set())

const loadFavoriteMerchants = async () => {
  favoriteMerchantIds.clear()
  if (!userStore.token) return
  try {
    const res = await api.get('/favorite/merchant/list')
    if (res.code === 200) {
      ;(res.data || []).forEach(it => {
        if (it?.merchantId != null) favoriteMerchantIds.add(Number(it.merchantId))
      })
    }
  } catch (e) {
    // 未登录或接口异常时忽略
  }
}

const loadMerchants = async () => {
  try {
    const res = await api.get('/merchant/list')
    if (res.code === 200) {
      merchants.value = res.data || []
      defaultOrderIndex.value = Object.fromEntries(
        merchants.value.map((m, idx) => [m.id, idx])
      )

      // 先把“热销水果”拉出来，用于展示 + 销量排序
      await loadTopProductsForMerchants(merchants.value)
      // 拉取收藏店铺状态
      await loadFavoriteMerchants()
      // 默认也加载一遍评分数据，这样初次进入列表就能看到评分
      await ensureRatingsLoaded()
    }
  } catch (error) {
    console.error('加载店铺失败', error)
  }
}

// 监听来自其他页面的收藏变更事件，保持列表中的收藏状态同步
const onFavoriteMerchantsChanged = () => {
  if (userStore.token) {
    loadFavoriteMerchants()
  } else {
    favoriteMerchantIds.clear()
  }
}

const getRating = (merchant) => {
  const v = merchant.rating ?? merchant.avgRating ?? ratingMap.value[merchant.id]
  const n = Number(v)
  return Number.isFinite(n) ? n : 0
}

const getImageUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http://') || url.startsWith('https://')) return url
  if (url.startsWith('/api/uploads/')) return url
  if (url.startsWith('/uploads/')) return '/api' + url
  if (!url.startsWith('/')) return '/api/uploads/' + url
  return url
}

const formatMoney = (v) => {
  const n = Number(v)
  if (!Number.isFinite(n)) return '0.00'
  return n.toFixed(2).replace(/\.00$/, '')
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

const loadTopProductsForMerchants = async (list) => {
  const tasks = (list || []).map(async (m) => {
    if (!m?.id) return
    if (topProductsMap.value[m.id]) return
    try {
      const res = await api.get('/product/list', {
        params: { merchantId: m.id, sortBy: 'sales' }
      })
      if (res.code === 200) {
        const products = Array.isArray(res.data) ? res.data : []
        topProductsMap.value[m.id] = products.slice(0, 6)
      } else {
        topProductsMap.value[m.id] = []
      }
    } catch (e) {
      topProductsMap.value[m.id] = []
    }
  })
  await Promise.all(tasks)
}

const ensureRatingsLoaded = async () => {
  const tasks = merchants.value.map(async (m) => {
    if (!m?.id) return
    if (ratingMap.value[m.id] != null) return
    try {
      const res = await api.get(`/review/merchant/${m.id}`)
      if (res.code === 200) {
        const r = Number(res.data?.averageRating)
        ratingMap.value[m.id] = Number.isFinite(r) ? r : 0
      } else {
        ratingMap.value[m.id] = 0
      }
    } catch (e) {
      ratingMap.value[m.id] = 0
    }
  })
  await Promise.all(tasks)
}

const getStoreSales = (merchantId) => {
  const list = topProductsMap.value[merchantId] || []
  return list.reduce((sum, p) => sum + (Number(p.salesCount) || 0), 0)
}

const sortedMerchants = computed(() => {
  const list = merchants.value.slice()
  if (filters.value.sort === 'default') {
    return list.sort((a, b) => (defaultOrderIndex.value[a.id] ?? 0) - (defaultOrderIndex.value[b.id] ?? 0))
  }
  if (filters.value.sort === 'rating') {
    return list.sort((a, b) => getRating(b) - getRating(a))
  }
  if (filters.value.sort === 'sales') {
    return list.sort((a, b) => getStoreSales(b.id) - getStoreSales(a.id))
  }
  return list
})

const onSortChange = async (v) => {
  if (v === 'rating') {
    await ensureRatingsLoaded()
  }
}

const toggleFavoriteMerchant = async (merchantId) => {
  if (!userStore.token) {
    ElMessage.warning('请先登录后再收藏')
    return
  }
  const id = Number(merchantId)
  try {
    if (favoriteMerchantIds.has(id)) {
      const res = await api.delete(`/favorite/merchant/remove/${id}`)
      if (res.code === 200) {
        favoriteMerchantIds.delete(id)
        ElMessage.success('已取消收藏')
        window.dispatchEvent(new CustomEvent('favorite-merchants-changed'))
      } else {
        ElMessage.error(res.message || '取消收藏失败')
      }
    } else {
      const res = await api.post('/favorite/merchant/add', { merchantId: id })
      if (res.code === 200) {
        favoriteMerchantIds.add(id)
        ElMessage.success('收藏成功')
        window.dispatchEvent(new CustomEvent('favorite-merchants-changed'))
      } else {
        ElMessage.error(res.message || '收藏失败')
      }
    }
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

const goChat = (merchantId) => {
  if (!userStore.token) {
    ElMessage.warning('请先登录后再私聊商家')
    router.push('/login')
    return
  }
  router.push(`/chat/${merchantId}`)
}

onMounted(() => {
  loadMerchants()
  window.addEventListener('favorite-merchants-changed', onFavoriteMerchantsChanged)
})

onBeforeUnmount(() => {
  window.removeEventListener('favorite-merchants-changed', onFavoriteMerchantsChanged)
})
</script>

<style scoped>
.merchant-list-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.page-header {
  margin-bottom: 24px;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: #2c3e50;
  margin: 0 0 12px 0;
}

.title-divider {
  height: 1px;
  background: #e0e0e0;
  margin-bottom: 16px;
}

.sort-bar {
  display: flex;
  justify-content: flex-end;
}

.sort-select {
  width: 160px;
}

.merchant-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.merchant-item {
  cursor: pointer;
  transition: all 0.3s;
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid transparent;
}

.merchant-item:hover {
  transform: translateY(-4px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.12);
  border-color: #2ecc71;
}

.merchant-top {
  display: flex;
  gap: 14px;
}

.merchant-main {
  padding: 4px 6px 4px 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex: 1;
}

.merchant-avatar {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  overflow: hidden;
  flex: 0 0 56px;
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

.merchant-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.merchant-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 0 0 auto;
}

.fav-btn {
  color: #7f8c8d;
}

.fav-btn:hover {
  color: #f39c12;
}

.merchant-title {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.merchant-name {
  font-size: 18px;
  color: #2c3e50;
  margin: 0;
  font-weight: 800;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 320px;
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
  color: #8c8c8c;
  font-size: 14px;
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-height: 1.6;
}

.merchant-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 4px;
  font-size: 13px;
  color: #7f8c8d;
}

.meta-item {
  white-space: nowrap;
}

.meta-status {
  font-weight: 600;
  color: #27ae60;
}

.top-products {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  width: 100%;
}

.product-tile {
  position: relative;
  flex: 0 0 110px;
  max-width: 110px;
  height: 90px;
  border-radius: 10px;
  overflow: hidden;
  background: #ffffff;
  border: 1px solid #f0f0f0;
}

.product-tile img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f8f9fa;
  font-size: 20px;
}

.product-price {
  position: absolute;
  left: 8px;
  bottom: 8px;
  background: rgba(0, 0, 0, 0.55);
  color: #fff;
  padding: 3px 8px;
  border-radius: 999px;
  font-size: 12px;
  line-height: 1;
}

.top-products-empty {
  grid-column: 1 / -1;
  color: #bdc3c7;
  font-size: 13px;
}

.enter-btn {
  background: #2ecc71;
  border-color: #2ecc71;
  border-radius: 6px;
}

.enter-btn:hover {
  background: #27ae60;
  border-color: #27ae60;
}

.empty-state {
  margin-top: 40px;
}

@media (max-width: 768px) {
  .merchant-list-page {
    padding: 16px;
  }
  .merchant-name { max-width: 180px; }
  .product-tile { height: 78px; }
}
</style>

