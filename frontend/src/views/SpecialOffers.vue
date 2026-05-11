<template>
  <div class="special-page">
    <el-card class="special-hero" shadow="never">
      <div class="hero-left">
        <h1>特价专区</h1>
        <p>折扣 / 买N送M，一站式捡漏</p>
      </div>
      <div class="hero-right">
        <el-button round @click="$router.push('/')">返回首页</el-button>
      </div>
    </el-card>

    <el-card class="filter-card" shadow="never">
      <div class="filter-section">
        <div class="filter-row">
          <div class="filter-item">
            <label>商品品类</label>
            <el-select
              v-model="selectedCategory"
              placeholder="请选择品类"
              clearable
              @change="loadProducts"
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
            class="product-card special-card"
            shadow="hover"
            @click="$router.push(`/product/${product.id}`)"
          >
            <div class="special-badge">
              {{ product.specialLabel || '特价' }}
            </div>
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
              <div class="product-price">
                <span class="current-price">¥{{ Number(product.price).toFixed(2) }}</span>
                <!-- 特价页只展示打折类商品的划线原价 -->
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
          </el-card>
        </el-col>
      </el-row>

      <el-empty v-if="products.length === 0" description="暂无特价商品" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import api from '@/utils/api'

const categories = ref([])
const products = ref([])

const selectedCategory = ref(null)
const keyword = ref('')
const sortBy = ref('')
const priceRange = ref('')
const freshnessLevel = ref('')
const displayCount = ref(16)

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
    const params = { specialOnly: true }
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
      // 默认排序：随机打乱
      if (!sortBy.value) {
        products.value = shuffle(list.slice())
      } else {
        products.value = list
      }
      displayCount.value = 16
    }
  } catch (error) {
    console.error('加载特价商品失败', error)
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

const visibleProducts = computed(() => {
  return products.value.slice(0, displayCount.value)
})

const handleLoadMore = () => {
  if (displayCount.value < products.value.length) {
    displayCount.value += 8
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

onMounted(() => {
  loadCategories()
  loadProducts()
})
</script>

<style scoped>
.special-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 14px 0 40px;
}

.special-hero {
  margin-bottom: 16px;
  border-radius: 12px;
  background: linear-gradient(135deg, rgba(255, 77, 79, 0.10) 0%, rgba(255, 122, 69, 0.08) 50%, rgba(255, 214, 102, 0.08) 100%);
  border: 1px solid rgba(255, 77, 79, 0.14);
}

.special-hero :deep(.el-card__body) {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}

.hero-left h1 {
  margin: 0;
  font-size: 20px;
  font-weight: 900;
  color: #2c3e50;
}

.hero-left p {
  margin: 6px 0 0;
  font-size: 12px;
  color: #7f8c8d;
}

.filter-card {
  margin-bottom: 20px;
  border-radius: 8px;
  background: #ffffff;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

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
  border-color: #ff4d4f;
  box-shadow: 0 0 0 1px rgba(255, 77, 79, 0.14);
}

.product-list {
  margin-top: 10px;
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
  padding: 4px 4px 12px;
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
  font-weight: 700;
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

.meta-right {
  display: flex;
  align-items: center;
  gap: 4px;
}

.fresh-rate :deep(.el-rate__icon) {
  font-size: 14px;
  color: #ffc107;
}

.fresh-text {
  font-size: 12px;
  color: #7f8c8d;
}

.special-card {
  border: 1px solid rgba(255, 77, 79, 0.14);
}

.special-badge {
  position: absolute;
  top: 12px;
  left: 12px;
  padding: 6px 10px;
  border-radius: 999px;
  background: rgba(255, 77, 79, 0.92);
  color: #fff;
  font-weight: 900;
  font-size: 12px;
  letter-spacing: 0.5px;
  box-shadow: 0 10px 18px rgba(255, 77, 79, 0.22);
  z-index: 2;
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


