<template>
  <div class="home-container">
    <!-- 左侧悬浮入口（随滚动，跳转到特价页） -->
    <div class="special-float-entry" @click="goToSpecialPage" title="查看特价水果">
      <div class="special-float-inner">
        <div class="special-float-top">特价</div>
        <div class="special-float-sub">抢购</div>
      </div>
    </div>

    <!-- 轮播图 -->
    <el-carousel
      :interval="4000"
      type="card"
      height="400px"
      class="banner-carousel"
      indicator-position="outside"
      pause-on-hover
    >
      <el-carousel-item v-for="(banner, index) in banners" :key="index">
        <div class="banner-item" :style="{ backgroundImage: `url(${banner.image})` }">
          <div class="banner-content">
            <h1>{{ banner.title }}</h1>
            <p>{{ banner.subtitle }}</p>
          </div>
        </div>
      </el-carousel-item>
    </el-carousel>

    <el-card class="filter-card" shadow="never">
      <div class="filter-section">
        <div class="filter-row">
          <div class="filter-item">
            <label>商品品类</label>
            <el-select 
              v-model="selectedCategory" 
              placeholder="请选择品类" 
              clearable
              @change="handleCategoryChange"
              style="width: 180px"
            >
              <el-option label="全部" :value="null" />
              <el-option 
                v-for="cat in categories" 
                :key="cat.id" 
                :label="cat.name" 
                :value="cat.id" 
              />
            </el-select>
          </div>
          <div class="filter-item">
            <label>价格区间</label>
            <el-select 
              v-model="priceRange" 
              placeholder="请选择价格区间" 
              clearable
              @change="loadProducts"
              style="width: 180px"
            >
              <el-option label="全部" :value="null" />
              <el-option label="0-20元" value="0-20" />
              <el-option label="20-50元" value="20-50" />
              <el-option label="50-100元" value="50-100" />
              <el-option label="100元以上" value="100-9999" />
            </el-select>
          </div>
          <div class="filter-item">
            <label>新鲜度</label>
            <el-select 
              v-model="freshnessLevel" 
              placeholder="请选择新鲜度" 
              clearable
              @change="loadProducts"
              style="width: 180px"
            >
              <el-option label="全部" :value="null" />
              <el-option label="5星" :value="5" />
              <el-option label="4星及以上" :value="4" />
              <el-option label="3星及以上" :value="3" />
            </el-select>
          </div>
          <div class="filter-item">
            <label>排序方式</label>
            <el-select 
              v-model="sortBy" 
              placeholder="请选择排序方式" 
              @change="loadProducts"
              style="width: 180px"
            >
              <el-option label="随机推荐（默认）" value="" />
              <el-option label="价格从低到高" value="price_asc" />
              <el-option label="价格从高到低" value="price_desc" />
              <el-option label="销量排序" value="sales" />
              <el-option label="新鲜度排序" value="freshness" />
            </el-select>
          </div>
        </div>
        <div class="search-row">
          <el-input
            v-model="keyword"
            placeholder="请输入关键词搜索"
            class="search-input"
            clearable
            @keyup.enter="loadProducts"
            @clear="loadProducts"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
            <template #append>
              <el-button @click="loadProducts">搜索</el-button>
            </template>
          </el-input>
        </div>
      </div>
    </el-card>

    <div
      class="product-list"
      v-infinite-scroll="handleLoadMore"
      infinite-scroll-distance="200"
      :infinite-scroll-disabled="visibleProducts.length >= products.length"
    >
      <el-row :gutter="20">
        <el-col
          v-for="product in visibleProducts"
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
                @error="handleImageError"
              />
              <div v-else class="no-image">暂无图片</div>
            </div>
            <div class="product-info">
              <h3 class="product-name">{{ product.name }}</h3>
              <div class="product-tags" v-if="getProductTags(product).length">
                <el-tag
                  v-for="tag in getProductTags(product)"
                  :key="tag"
                  size="small"
                  class="product-tag"
                  :class="tag === '新品' ? 'tag-new' : 'tag-hot'"
                  effect="light"
                >
                  {{ tag }}
                </el-tag>
              </div>
              <div class="product-price">
                <span class="current-price">¥{{ Number(product.price).toFixed(2) }}</span>
                <!-- 只有打折特价商品才展示划线原价 -->
                <span
                  v-if="product.specialEnabled === 1
                    && product.specialType === 'DISCOUNT'
                    && product.originalPrice
                    && Number(product.originalPrice) > Number(product.price)"
                  class="original-price"
                >
                  ¥{{ Number(product.originalPrice).toFixed(2) }}
                </span>
              </div>
              <div class="product-meta">
                <div class="meta-left">
                  <span class="meta-icon">📊</span>
                  <span class="meta-text">销量 {{ product.salesCount || 0 }}</span>
                </div>
                <div class="meta-right">
                  <el-rate
                    :model-value="product.freshnessLevel || 5"
                    disabled
                    :max="5"
                    class="fresh-rate"
                  />
                  <span class="fresh-text">{{ product.freshnessLevel || 5 }} 星</span>
                </div>
              </div>
            </div>
            <div class="card-actions">
              <el-button
                type="primary"
                size="small"
                round
                class="add-cart-btn"
                @click.stop="handleAddToCart(product)"
              >
                🛒 加入购物车
              </el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>
      <el-empty v-if="products.length === 0" description="暂无商品" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'
import api from '@/utils/api'

const categories = ref([])
const products = ref([])
const selectedCategory = ref(null)
const keyword = ref('')
const sortBy = ref('')
const priceRange = ref('')
const freshnessLevel = ref('')
const displayCount = ref(16)

const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()

// 轮播图数据
const banners = ref([
  {
    title: '新鲜水果，健康生活',
    subtitle: '精选优质水果，为您带来健康与美味',
    image: 'https://images.unsplash.com/photo-1619566636858-adf3ef46400b?w=1200&h=300&fit=crop'
  },
  {
    title: '产地直供，新鲜直达',
    subtitle: '从果园到餐桌，保证每一份新鲜',
    image: 'https://images.unsplash.com/photo-1528821128474-27f963b062bf?w=1200&h=300&fit=crop'
  },
  {
    title: '品质保证，放心购买',
    subtitle: '严格筛选，只为给您最好的水果',
    image: 'https://images.unsplash.com/photo-1600298881974-6be191ceeda1?w=1200&h=300&fit=crop'
  }
])

const loadCategories = async () => {
  try {
    const res = await api.get('/category/list')
    if (res.code === 200) {
      categories.value = res.data || []
    }
  } catch (error) {
    console.error('加载分类失败', error)
  }
}

const loadProducts = async () => {
  try {
    // 首页仅展示非特价商品；特价商品只在“特价专区”页面展示
    const params = { specialOnly: false }
    if (selectedCategory.value !== null && selectedCategory.value !== undefined && selectedCategory.value !== '') {
      params.categoryId = selectedCategory.value
    }
    if (keyword.value && keyword.value.trim()) {
      params.keyword = keyword.value.trim()
    }
    if (sortBy.value && sortBy.value !== '') {
      params.sortBy = sortBy.value
    }
    if (priceRange.value && priceRange.value !== '') {
      params.priceRange = priceRange.value
    }
    if (freshnessLevel.value !== null && freshnessLevel.value !== undefined && freshnessLevel.value !== '') {
      params.freshnessLevel = Number(freshnessLevel.value)
    }
    const res = await api.get('/product/list', { params })
    if (res.code === 200) {
      const list = Array.isArray(res.data) ? res.data : []
      // 默认排序：前端随机打乱，保证每次进入/刷新都不一样
      if (!sortBy.value) {
        products.value = shuffle(list.slice())
      } else {
        products.value = list
      }
      displayCount.value = 16
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

const loadSpecialProducts = async () => {
  try {
    const res = await api.get('/product/list', { params: { specialOnly: true } })
    if (res.code === 200) {
      specialProducts.value = res.data || []
    }
  } catch (error) {
    console.error('加载特价商品失败', error)
  }
}

const handleCategoryChange = () => {
  loadProducts()
}

const resetFilters = () => {
  selectedCategory.value = null
  keyword.value = ''
  sortBy.value = ''
  priceRange.value = null
  freshnessLevel.value = null
  displayCount.value = 16
  loadProducts()
}

const getImageUrl = (url) => {
  if (!url) return ''
  // 如果是完整URL，直接返回
  if (url.startsWith('http://') || url.startsWith('https://')) {
    return url
  }
  // 如果是相对路径，加上API前缀
  if (url.startsWith('/uploads/')) {
    return '/api' + url
  }
  // 如果已经是/api/uploads/开头，直接返回
  if (url.startsWith('/api/uploads/')) {
    return url
  }
  return url
}

const handleImageError = (event) => {
  // 图片加载失败时，隐藏图片显示占位符
  event.target.style.display = 'none'
  const parent = event.target.parentElement
  if (parent && !parent.querySelector('.no-image')) {
    const noImageDiv = document.createElement('div')
    noImageDiv.className = 'no-image'
    noImageDiv.textContent = '暂无图片'
    parent.appendChild(noImageDiv)
  }
}

const visibleProducts = computed(() => {
  return products.value.slice(0, displayCount.value)
})

const handleLoadMore = () => {
  if (displayCount.value < products.value.length) {
    displayCount.value += 8
  }
}

const goToSpecialPage = () => {
  router.push('/special')
}

const getProductTags = (product) => {
  const tags = []
  if (!product) return tags

  // 新品：后端提供 isNew 字段（如无可后续扩展为按创建时间判断）
  if (product.isNew) {
    tags.push('新品')
  }

  // 热销：销量较高
  if ((product.salesCount || 0) >= 50) {
    tags.push('热销')
  }

  return tags
}

const handleAddToCart = async (product) => {
  if (!userStore.user) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  try {
    await cartStore.addToCart(product.id, null, 1)
    ElMessage.success('已加入购物车')
  } catch (error) {
    ElMessage.error(error.message || '加入购物车失败')
  }
}

onMounted(() => {
  loadCategories()
  loadProducts()
})
</script>

<style scoped>
.home-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 8px 0 40px;
}

/* 左侧悬浮入口（吸引人 + 跟随滚动） */
.special-float-entry {
  position: fixed;
  top: 52%;
  transform: translateY(-50%);
  left: 12px;
  z-index: 999;
  width: 64px;
  height: 64px;
  border-radius: 18px;
  background: linear-gradient(135deg, #ff4d4f 0%, #ff7a45 45%, #ffd666 100%);
  box-shadow: 0 10px 24px rgba(255, 77, 79, 0.28);
  cursor: pointer;
  user-select: none;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.18s ease, box-shadow 0.18s ease;
  animation: specialPulse 1.8s ease-in-out infinite;
}

@media (min-width: 1400px) {
  .special-float-entry {
    left: calc((100vw - 1200px) / 2 - 84px);
  }
}

.special-float-entry:hover {
  transform: translateY(-50%) scale(1.04);
  box-shadow: 0 16px 36px rgba(255, 77, 79, 0.38);
}

.special-float-inner {
  text-align: center;
  color: #fff;
  line-height: 1;
}

.special-float-top {
  font-size: 18px;
  font-weight: 900;
  letter-spacing: 1px;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.special-float-sub {
  margin-top: 6px;
  font-size: 12px;
  font-weight: 700;
  opacity: 0.95;
}

@keyframes specialPulse {
  0% { transform: translateY(-50%) scale(1); }
  55% { transform: translateY(-50%) scale(1.06); }
  100% { transform: translateY(-50%) scale(1); }
}

.banner-carousel {
  margin-bottom: 32px;
  border-radius: 8px;
  overflow: hidden;
}

.banner-item {
  width: 100%;
  height: 100%;
  background-size: cover;
  background-position: center;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.banner-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(
    90deg,
    rgba(0, 0, 0, 0.55) 0%,
    rgba(0, 0, 0, 0.25) 40%,
    rgba(0, 0, 0, 0.05) 100%
  );
  z-index: 1;
}

.banner-content {
  position: relative;
  z-index: 2;
  text-align: left;
  color: white;
  max-width: 480px;
}

.banner-content h1 {
  font-size: 36px;
  margin-bottom: 16px;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.5);
}

.banner-content p {
  font-size: 18px;
  opacity: 0.95;
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.5);
}

.banner-carousel :deep(.el-carousel__indicator--horizontal button) {
  width: 24px;
  height: 4px;
  border-radius: 999px;
  background-color: rgba(255, 255, 255, 0.5);
}

.banner-carousel :deep(.el-carousel__indicator.is-active button) {
  background-color: #2ecc71;
}


.filter-card {
  margin-bottom: 24px;
  border-radius: 8px;
  background: #ffffff;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

/* 特价专区相关样式在 SpecialOffers.vue 中维护 */

.filter-section {
  padding: 8px 0;
}

.filter-row {
  display: flex;
  gap: 24px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.filter-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-item label {
  font-size: 14px;
  color: #595959;
  white-space: nowrap;
  min-width: 60px;
}

.search-row {
  display: flex;
  justify-content: flex-end;
}

.search-input {
  width: 400px;
}

.filter-card :deep(.el-input__wrapper),
.filter-card :deep(.el-select .el-input__wrapper) {
  border-radius: 999px;
  box-shadow: none;
  border: 1px solid #e0e0e0;
}

.filter-card :deep(.el-input__wrapper.is-focus),
.filter-card :deep(.el-input__wrapper:hover),
.filter-card :deep(.el-select .el-input.is-focus .el-input__wrapper),
.filter-card :deep(.el-select .el-input__wrapper:hover) {
  border-color: #2ecc71;
  box-shadow: 0 0 0 1px rgba(46, 204, 113, 0.15);
}

.product-list {
  margin-top: 24px;
}

.product-card {
  margin-bottom: 20px;
  cursor: pointer;
  transition: all 0.25s ease;
  border-radius: 12px;
  overflow: hidden;
  position: relative;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.product-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.product-image {
  width: 100%;
  height: 200px;
  overflow: hidden;
  border-radius: 8px;
  margin-bottom: 12px;
  background: #f5f7fa;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.product-card:hover .product-image img {
  transform: scale(1.05);
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
  padding: 4px 4px 10px;
}

.product-name {
  font-size: 16px;
  color: #2c3e50;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-price {
  margin-bottom: 8px;
  display: flex;
  align-items: baseline;
}

.current-price {
  font-size: 20px;
  color: #e74c3c;
  font-weight: 600;
  margin-right: 8px;
}

.original-price {
  font-size: 14px;
  color: #bdc3c7;
  text-decoration: line-through;
}

.product-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #7f8c8d;
  align-items: center;
}

.meta-left {
  display: flex;
  align-items: center;
  gap: 4px;
}

.meta-icon {
  font-size: 13px;
}

.meta-text {
  font-size: 12px;
}

.meta-right {
  display: flex;
  align-items: center;
  gap: 4px;
}

.fresh-rate :deep(.el-rate__icon) {
  font-size: 14px;
  color: #ffc107;
}

.fresh-rate :deep(.el-rate__icon.el-icon-star-off) {
  color: #e0e0e0;
}

.fresh-text {
  font-size: 12px;
  color: #7f8c8d;
}

.product-tags {
  display: flex;
  gap: 6px;
  margin-bottom: 6px;
}

.product-tag {
  border-radius: 999px;
  border: none;
  font-size: 11px;
  padding: 2px 8px;
}

.tag-new {
  background: #e3f2fd;
  color: #3498db;
}

.tag-hot {
  background: #fff3e0;
  color: #f39c12;
}

.card-actions {
  position: absolute;
  right: 14px;
  bottom: 12px;
  opacity: 0;
  transform: translateY(6px);
  transition: all 0.2s ease;
}

.product-card:hover .card-actions {
  opacity: 1;
  transform: translateY(0);
}

.add-cart-btn {
  background-color: #2ecc71;
  border-color: #2ecc71;
}

.add-cart-btn:hover {
  background-color: #27ae60;
  border-color: #27ae60;
}

@media (max-width: 768px) {
  .search-row {
    justify-content: center;
  }

  .search-input {
    width: 100%;
  }
}
</style>

