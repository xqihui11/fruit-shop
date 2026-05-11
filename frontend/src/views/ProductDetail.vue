<template>
  <div class="product-detail" v-if="product">
    <el-row :gutter="24">
      <el-col :span="12">
        <div class="product-image-wrapper">
          <img 
            v-if="product.mainImage" 
            :src="getImageUrl(product.mainImage)" 
            :alt="product.name"
            @error="handleImageError"
          />
          <div v-else class="no-image">暂无图片</div>
        </div>
      </el-col>
      <el-col :span="12">
        <div class="product-info">
          <h1 class="product-title">{{ product.name }}</h1>
          <div class="product-price-section">
            <span class="current-price">¥{{ displayMainPrice }}</span>
            <!-- 详情页同样只在打折特价时展示划线原价（规格不再单独维护原价） -->
            <span
              v-if="!selectedSpec
                && product.specialEnabled === 1
                && product.specialType === 'DISCOUNT'
                && product.originalPrice
                && Number(product.originalPrice) > Number(product.price)"
              class="original-price"
            >
              ¥{{ displayOriginalTotalPrice }}
            </span>
          </div>
          <div class="product-meta-info">
            <el-tag>销量: {{ product.salesCount || 0 }}</el-tag>
            <el-tag type="success">新鲜度: {{ product.freshnessLevel || 5 }}星</el-tag>
            <el-tag type="info">库存: {{ selectedSpec ? selectedSpec.stock : (product.stock || 0) }}</el-tag>
          </div>
          
          <!-- 店铺信息 -->
          <div v-if="merchant" class="merchant-info-section">
            <h3>店铺信息</h3>
            <div class="merchant-card" @click="$router.push(`/merchant/${merchant.id}`)">
              <span class="merchant-name">{{ merchant.shopName }}</span>
              <el-button type="text" size="small">进入店铺 ></el-button>
            </div>
          </div>
          
          <!-- 规格选择 -->
          <div v-if="specs && specs.length > 0" class="spec-section">
            <h3>选择规格</h3>
            <div class="spec-list">
              <div
                v-for="spec in specs"
                :key="spec.id"
                class="spec-item"
                :class="{ active: selectedSpec?.id === spec.id }"
                @click="selectSpec(spec)"
              >
                <div class="spec-name">{{ spec.specName }}</div>
                <div class="spec-price">
                  <span class="current-price">
                    ¥{{ getSpecDisplayPrice(spec).toFixed(2) }}
                  </span>
                  <!-- 规格的划线价仅在商品开启折扣特价时展示，使用当前规格原价 -->
                  <span
                    v-if="showSpecOriginalPrice"
                    class="original-price"
                  >
                    ¥{{ Number(spec.price).toFixed(2) }}
                  </span>
                </div>
              </div>
            </div>
          </div>
          
          <div class="product-description">
            <h3>商品详情</h3>
            <p>{{ product.description || '暂无描述' }}</p>
          </div>
          <div class="product-actions">
            <el-input-number
              v-model="quantity"
              :min="1"
              :max="quantityMax"
              :disabled="quantityDisabled"
              size="large"
            />
            <el-button
              type="danger"
              size="large"
              @click="buyNow"
              :disabled="!userStore.user || quantityDisabled"
            >
              <el-icon><ShoppingBag /></el-icon>
              立即购买
            </el-button>
            <el-button
              type="primary"
              size="large"
              @click="addToCart"
              :disabled="!userStore.user || quantityDisabled"
            >
              <el-icon><ShoppingCart /></el-icon>
              加入购物车
            </el-button>
            <el-button
              :type="isFavorite ? 'warning' : 'default'"
              size="large"
              @click="toggleFavorite"
              :disabled="!userStore.user"
            >
              <el-icon><Star /></el-icon>
              {{ isFavorite ? '已收藏' : '收藏商品' }}
            </el-button>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-tabs v-model="activeTab" class="detail-tabs" @tab-change="handleTabChange">
      <el-tab-pane label="商品评价" name="productReviews">
        <div class="reviews-section">
            <h2>商品评价 ({{ reviews.length }})</h2>
            <div v-if="reviews.length === 0" class="no-reviews">
              <el-empty description="暂无评价" />
            </div>
            <div v-else class="reviews-list">
              <div
                v-for="review in reviews"
                :key="review.id"
                class="review-item"
                :class="{ 'review-item-highlight': highlightedReviewId === Number(review.id) }"
                :data-review-id="review.id"
              >
                <div class="review-main-content">
                  <div class="review-left">
                    <div class="review-header">
                      <div class="reviewer-info">
                        <el-avatar 
                          :size="36" 
                          :src="review.avatar ? getAvatarUrl(review.avatar) : undefined"
                        >
                          {{ review.username?.charAt(0) || 'U' }}
                        </el-avatar>
                        <span class="reviewer-name">{{ review.username || '匿名用户' }}</span>
                      </div>
                      <el-rate v-model="review.rating" disabled />
                    </div>
                    <div class="review-content">{{ review.content || '用户未填写评价内容' }}</div>
                    
                    <!-- 购买商品信息（显示商品图片、数量、购买时间） -->
                    <div v-if="review.productImage || review.quantity || review.orderCreateTime" class="review-product-info">
                      <div v-if="review.productImage" class="product-image-wrapper">
                        <img 
                          :src="getImageUrl(review.productImage)" 
                          :alt="product?.name || '商品图片'"
                          class="review-product-image"
                          @error="handleImageError"
                        />
                      </div>
                      <div class="product-details">
                        <div v-if="review.specName" class="product-info-item">
                          <span class="product-label">规格：</span>
                          <span class="product-value">{{ review.specName }}</span>
                      </div>
                        <div v-if="review.quantity" class="product-info-item">
                          <span class="product-label">购买数量：</span>
                          <span class="product-value">{{ review.quantity }}件</span>
                        </div>
                        <div v-if="review.orderCreateTime" class="product-info-item">
                          <span class="product-label">购买时间：</span>
                          <span class="product-value">{{ formatTime(review.orderCreateTime) }}</span>
                        </div>
                      </div>
                    </div>
                    
                    <div class="review-time">评价时间：{{ formatTime(review.createTime) }}</div>
                  </div>
                  
                  <!-- 点赞功能 - 移到右侧 -->
                  <div class="review-actions-right">
                    <div class="like-actions">
                      <button 
                        class="like-btn"
                        :class="{ active: review.userLikeType === 1 }"
                        @click="toggleLike(review.id, 1)"
                        :disabled="!userStore.user"
                        title="认同"
                      >
                        <span class="like-icon">👍</span>
                        <span class="like-count" v-if="review.likeCount > 0">{{ review.likeCount }}</span>
                      </button>
                      <button 
                        class="like-btn dislike-btn"
                        :class="{ active: review.userLikeType === 2 }"
                        @click="toggleLike(review.id, 2)"
                        :disabled="!userStore.user"
                        title="不认同"
                      >
                        <span class="like-icon">👎</span>
                        <span class="like-count" v-if="review.dislikeCount > 0">{{ review.dislikeCount }}</span>
                      </button>
                    </div>
                  </div>
                </div>
                
                <!-- 显示回复（默认只显示3条） -->
                <div v-if="review.replies && Array.isArray(review.replies) && review.replies.length > 0" class="replies-section">
                  <div class="replies-header">
                    <span class="replies-count">相关回复共{{ review.replies.length }}条</span>
                  </div>
                  <!-- 显示回复列表 -->
                  <div class="replies-list">
                    <div v-for="reply in getDisplayReplies(review)" :key="reply.id" class="reply-item">
                      <div class="reply-avatar">
                        <el-avatar 
                          :size="40" 
                          :src="reply.avatar ? getAvatarUrl(reply.avatar) : undefined"
                        >
                        {{ (reply.replyType === 1 ? reply.merchantName : reply.username)?.charAt(0) || 'U' }}
                      </el-avatar>
                      </div>
                      <div class="reply-content-wrapper">
                        <div class="reply-header-info">
                          <div class="reply-username-wrapper">
                        <span class="reply-username">
                          <span v-if="reply.replyType === 1">
                            {{ reply.merchantName || '商家' }}
                          </span>
                          <span v-else>
                            {{ reply.username || '用户' }}
                          </span>
                        </span>
                            <span v-if="reply.replyToId" class="reply-to-info">
                              <span class="reply-to-text">回复</span>
                              <span class="reply-to-name">
                                {{ reply.replyToMerchantName || reply.replyToUsername || '用户' }}
                              </span>
                            </span>
                            <el-tag v-if="reply.replyType === 1" type="warning" size="small" class="merchant-tag">
                              商家
                            </el-tag>
                          </div>
                        <span class="reply-time">{{ formatTime(reply.createTime) }}</span>
                    </div>
                    <div class="reply-content-text">{{ reply.content }}</div>
                    <div class="reply-actions">
                          <el-button type="text" size="small" class="reply-btn" @click="replyToReply(review.id, reply)">
                        回复
                      </el-button>
                        </div>
                      </div>
                    </div>
                  </div>
                  
                  <!-- 分页组件（展开后且回复数>5时显示） -->
                  <div
                    v-if="expandedReplies[review.id] && review.replies.length > 5"
                    class="replies-pagination"
                  >
                    <el-pagination
                      v-model:current-page="replyPages[review.id]"
                      :page-size="5"
                      :total="review.replies.length"
                      layout="prev, pager, next, jumper"
                      small
                      @current-change="() => {}"
                    />
                  </div>
                  
                  <!-- 更多回复按钮 -->
                  <div v-if="review.replies.length > 3 && !expandedReplies[review.id]" class="more-replies">
                    <el-button type="text" class="more-replies-btn" @click="expandReplies(review.id)">
                      共{{ review.replies.length }}条回复 >
                    </el-button>
                  </div>
                  <!-- 收起按钮 -->
                  <div v-if="expandedReplies[review.id] && review.replies.length > 3" class="collapse-replies">
                    <el-button type="text" class="collapse-replies-btn" @click="collapseReplies(review.id)">
                      收起回复
                    </el-button>
                  </div>
                </div>
                
                <!-- 显示旧的商家回复（兼容旧数据） -->
                <div v-if="review.merchantReply && (!review.replies || review.replies.length === 0)" class="replies-section">
                  <div class="reply-item merchant-reply">
                    <div class="reply-header">
                      <span class="reply-label">商家回复</span>
                    </div>
                    <div class="reply-content">{{ review.merchantReply }}</div>
                  </div>
                </div>
                
                <!-- 回复输入框（仅对已登录用户显示） -->
                <div v-if="userStore.user" class="reply-input-section">
                  <el-input
                    v-model="replyContents[review.id]"
                    type="textarea"
                    :rows="2"
                    placeholder="回复这条评价..."
                    maxlength="200"
                    show-word-limit
                  />
                  <el-button
                    type="primary"
                    size="small"
                    @click="submitReply(review.id)"
                    :loading="replying[review.id]"
                    style="margin-top: 8px;"
                  >
                    回复
                  </el-button>
                </div>
              </div>
            </div>
          </div>
      </el-tab-pane>
      <el-tab-pane v-if="merchant" label="店铺评价" name="merchantReviews">
        <div class="reviews-section">
            <h2>店铺评价 ({{ merchantReviews.length }})</h2>
            <div v-if="merchantReviews.length === 0" class="no-reviews">
              <el-empty description="暂无评价" />
            </div>
            <div v-else class="reviews-list">
              <div
                v-for="review in merchantReviews"
                :key="review.id"
                class="review-item"
                :class="{ 'review-item-highlight': highlightedReviewId === Number(review.id) }"
                :data-review-id="review.id"
              >
                <div class="review-main-content">
                  <div class="review-left">
                    <div class="review-header">
                      <div class="reviewer-info">
                        <el-avatar 
                          :size="36" 
                          :src="review.avatar ? getAvatarUrl(review.avatar) : undefined"
                        >
                          {{ review.username?.charAt(0) || 'U' }}
                        </el-avatar>
                        <span class="reviewer-name">{{ review.username || '匿名用户' }}</span>
                      </div>
                      <el-rate v-model="review.rating" disabled />
                    </div>
                    <div class="review-content">{{ review.content || '用户未填写评价内容' }}</div>
                    
                    <!-- 购买商品信息（显示商品图片、数量、购买时间） -->
                    <div v-if="review.productImage || review.quantity || review.orderCreateTime" class="review-product-info">
                      <div v-if="review.productImage" class="product-image-wrapper">
                        <img 
                          :src="getImageUrl(review.productImage)" 
                          :alt="review.productName || '商品图片'"
                          class="review-product-image"
                          @error="handleImageError"
                        />
                      </div>
                      <div class="product-details">
                        <div v-if="review.productName" class="product-info-item">
                          <span class="product-label">商品：</span>
                          <span class="product-value">{{ review.productName }}</span>
                        </div>
                        <div v-if="review.specName" class="product-info-item">
                          <span class="product-label">规格：</span>
                          <span class="product-value">{{ review.specName }}</span>
                        </div>
                        <div v-if="review.quantity" class="product-info-item">
                          <span class="product-label">购买数量：</span>
                          <span class="product-value">{{ review.quantity }}件</span>
                        </div>
                        <div v-if="review.orderCreateTime" class="product-info-item">
                          <span class="product-label">购买时间：</span>
                          <span class="product-value">{{ formatTime(review.orderCreateTime) }}</span>
                        </div>
                      </div>
                    </div>
                    
                    <div class="review-time">评价时间：{{ formatTime(review.createTime) }}</div>
                  </div>
                  
                  <!-- 点赞功能 - 移到右侧 -->
                  <div class="review-actions-right">
                    <div class="like-actions">
                      <button 
                        class="like-btn"
                        :class="{ active: review.userLikeType === 1 }"
                        @click="toggleLike(review.id, 1)"
                        :disabled="!userStore.user"
                        title="认同"
                      >
                        <span class="like-icon">👍</span>
                        <span class="like-count" v-if="review.likeCount > 0">{{ review.likeCount }}</span>
                      </button>
                      <button 
                        class="like-btn dislike-btn"
                        :class="{ active: review.userLikeType === 2 }"
                        @click="toggleLike(review.id, 2)"
                        :disabled="!userStore.user"
                        title="不认同"
                      >
                        <span class="like-icon">👎</span>
                        <span class="like-count" v-if="review.dislikeCount > 0">{{ review.dislikeCount }}</span>
                      </button>
                    </div>
                  </div>
                </div>
                
                <!-- 显示回复（默认只显示3条） -->
                <div v-if="review.replies && Array.isArray(review.replies) && review.replies.length > 0" class="replies-section">
                  <div class="replies-header">
                    <span class="replies-count">相关回复共{{ review.replies.length }}条</span>
                  </div>
                  <!-- 显示回复列表 -->
                  <div class="replies-list">
                    <div v-for="reply in getDisplayReplies(review)" :key="reply.id" class="reply-item">
                      <div class="reply-avatar">
                        <el-avatar 
                          :size="40" 
                          :src="reply.avatar ? getAvatarUrl(reply.avatar) : undefined"
                        >
                        {{ (reply.replyType === 1 ? reply.merchantName : reply.username)?.charAt(0) || 'U' }}
                      </el-avatar>
                      </div>
                      <div class="reply-content-wrapper">
                        <div class="reply-header-info">
                          <div class="reply-username-wrapper">
                        <span class="reply-username">
                          <span v-if="reply.replyType === 1">
                            {{ reply.merchantName || '商家' }}
                          </span>
                          <span v-else>
                            {{ reply.username || '用户' }}
                          </span>
                        </span>
                            <span v-if="reply.replyToId" class="reply-to-info">
                              <span class="reply-to-text">回复</span>
                              <span class="reply-to-name">
                                {{ reply.replyToMerchantName || reply.replyToUsername || '用户' }}
                              </span>
                            </span>
                            <el-tag v-if="reply.replyType === 1" type="warning" size="small" class="merchant-tag">
                              商家
                            </el-tag>
                          </div>
                        <span class="reply-time">{{ formatTime(reply.createTime) }}</span>
                    </div>
                    <div class="reply-content-text">{{ reply.content }}</div>
                    <div class="reply-actions">
                          <el-button type="text" size="small" class="reply-btn" @click="replyToReply(review.id, reply)">
                        回复
                      </el-button>
                        </div>
                      </div>
                    </div>
                  </div>
                  
                  <!-- 分页组件（展开后且回复数>5时显示） -->
                  <div
                    v-if="expandedReplies[review.id] && review.replies.length > 5"
                    class="replies-pagination"
                  >
                    <el-pagination
                      v-model:current-page="replyPages[review.id]"
                      :page-size="5"
                      :total="review.replies.length"
                      layout="prev, pager, next, jumper"
                      small
                      @current-change="() => {}"
                    />
                  </div>
                  
                  <!-- 更多回复按钮 -->
                  <div v-if="review.replies.length > 3 && !expandedReplies[review.id]" class="more-replies">
                    <el-button type="text" class="more-replies-btn" @click="expandReplies(review.id)">
                      共{{ review.replies.length }}条回复 >
                    </el-button>
                  </div>
                  <!-- 收起按钮 -->
                  <div v-if="expandedReplies[review.id] && review.replies.length > 3" class="collapse-replies">
                    <el-button type="text" class="collapse-replies-btn" @click="collapseReplies(review.id)">
                      收起回复
                    </el-button>
                  </div>
                </div>
                
                <!-- 兼容旧数据格式 -->
                <div v-if="review.merchantReply && (!review.replies || review.replies.length === 0)" class="replies-section">
                  <div class="reply-item merchant-reply">
                    <div class="reply-header">
                      <span class="reply-label">商家回复</span>
                    </div>
                    <div class="reply-content">{{ review.merchantReply }}</div>
                  </div>
                </div>
              </div>
            </div>
          </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'
import { ElMessage } from 'element-plus'
import { ShoppingCart, Star, ShoppingBag } from '@element-plus/icons-vue'
import api from '@/utils/api'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()

const product = ref(null)
const merchant = ref(null)
const specs = ref([])
const selectedSpec = ref(null)
const quantity = ref(1)
const reviews = ref([])
const merchantReviews = ref([])
const isFavorite = ref(false)
const activeTab = ref('productReviews')
const replyContents = ref({}) // 存储每个评价的回复内容
const replying = ref({}) // 存储每个评价的回复状态
const expandedReplies = ref({}) // 存储每个评价的回复展开状态
const replyPages = ref({}) // 存储每个评价的回复分页状态 { reviewId: currentPage }

// 高亮的评价 ID，用于从消息跳转后短暂高亮提示
const highlightedReviewId = ref(null)

// 是否为折扣特价商品
const isDiscountSpecial = computed(() => {
  if (!product.value) return false
  if (product.value.specialEnabled !== 1) return false
  if (!product.value.specialType) return false
  return String(product.value.specialType).toUpperCase() === 'DISCOUNT'
})

// 商品折扣（0~1），从 specialDiscount 读取，兜底用 price/originalPrice 推算
const productDiscount = computed(() => {
  if (!isDiscountSpecial.value) return 1
  const p = product.value
  if (!p) return 1

  const sd = Number(p.specialDiscount)
  if (sd > 0 && sd <= 1) {
    return sd
  }

  // 兜底：如果有原价和现价，用现价 / 原价 反推折扣
  const price = Number(p.price)
  const original = Number(p.originalPrice)
  if (original > 0 && price > 0 && price < original) {
    return price / original
  }
  return 1
})

// 单件价格（用于计算总价）
const unitPrice = computed(() => {
  if (!product.value) return 0
  if (selectedSpec.value) {
    return getSpecDisplayPrice(selectedSpec.value)
  }
  return Number(product.value.price || 0)
})

// 顶部展示价格：根据当前数量显示总价
const displayMainPrice = computed(() => {
  if (!product.value) return '0.00'
  const total = unitPrice.value * Number(quantity.value || 1)
  return Number(total || 0).toFixed(2)
})

// 为了避免 ElementPlus 的 ElInputNumber 在 max/min/modelValue 非法时抛异常：
// - quantityMax 永远 >= 1
// - 当库存为 0 时禁用数量和购买按钮
const quantityMax = computed(() => {
  const specStock = selectedSpec.value?.stock
  const rawMax = specStock != null ? Number(specStock) : Number(product.value?.stock ?? 999)
  if (!Number.isFinite(rawMax)) return 999
  // ElementPlus 要求 max >= min，这里兜底为 1
  return rawMax >= 1 ? rawMax : 1
})

const quantityDisabled = computed(() => {
  // 规格模式但未选中规格：禁用
  if (specs.value.length > 0 && !selectedSpec.value) return true

  // 库存为 0：禁用
  const specStock = selectedSpec.value?.stock
  const rawStock = specStock != null ? Number(specStock) : Number(product.value?.stock ?? 0)
  if (!Number.isFinite(rawStock)) return false
  return rawStock <= 0
})

// 划线原价同样根据数量变化（仅商品级别折扣时展示）
const displayOriginalTotalPrice = computed(() => {
  if (!product.value) return ''
  const original = Number(product.value.originalPrice || 0)
  if (!original) return ''
  const total = original * Number(quantity.value || 1)
  return Number(total || 0).toFixed(2)
})

// 计算规格展示用价格（仅用于前端展示，不修改原始数据）
const getSpecDisplayPrice = (spec) => {
  if (!spec) return 0
  const base = Number(spec.price) || 0
  if (!isDiscountSpecial.value) {
    return base
  }
  const d = productDiscount.value
  if (d <= 0 || d > 1) {
    return base
  }
  return Number((base * d).toFixed(2))
}

// 是否展示规格划线原价（仅在折扣特价时）
const showSpecOriginalPrice = computed(() => isDiscountSpecial.value)

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  
  if (days === 0) {
    return '今天'
  } else if (days === 1) {
    return '昨天'
  } else if (days < 7) {
    return `${days}天前`
  } else {
    return date.toLocaleDateString('zh-CN')
  }
}

// 获取要显示的回复列表（默认3条，展开后分页显示，每页5条）
const getDisplayReplies = (review) => {
  if (!review.replies || review.replies.length === 0) {
    return []
  }
  
  // 如果未展开，只显示前3条
  if (!expandedReplies.value[review.id]) {
    return review.replies.length <= 3 ? review.replies : review.replies.slice(0, 3)
  }
  
  // 如果展开后，根据分页显示
  const currentPage = replyPages.value[review.id] || 1
  const pageSize = 5
  const start = (currentPage - 1) * pageSize
  const end = start + pageSize
  
  return review.replies.slice(start, end)
}

// 展开回复
const expandReplies = (reviewId) => {
  expandedReplies.value[reviewId] = true
  // 初始化分页为第1页
  if (!replyPages.value[reviewId]) {
    replyPages.value[reviewId] = 1
  }
}

// 收起回复
const collapseReplies = (reviewId) => {
  expandedReplies.value[reviewId] = false
  // 重置分页
  replyPages.value[reviewId] = 1
}

// 回复某个回复
// 存储每个评价的回复目标ID
const replyToIds = ref({})

const replyToReply = (reviewId, reply) => {
  // 记录回复目标ID
  replyToIds.value[reviewId] = reply.id
  // 在回复输入框中显示@用户名
  const replyToName = reply.replyType === 1 ? (reply.merchantName || '商家') : (reply.username || '用户')
  replyContents.value[reviewId] = `@${replyToName} `
  // 滚动到回复输入框
  setTimeout(() => {
    const inputSection = document.querySelector(`.review-item[data-review-id="${reviewId}"] .reply-input-section`)
    if (inputSection) {
      inputSection.scrollIntoView({ behavior: 'smooth', block: 'nearest' })
      const textarea = inputSection.querySelector('textarea')
      if (textarea) {
        textarea.focus()
      }
    }
  }, 100)
}

const scrollToHighlightedReview = (reviewId) => {
  if (!reviewId) return
  // 等 DOM 渲染完再滚动
  setTimeout(() => {
    const el = document.querySelector('.review-item-highlight')
    if (el) {
      el.scrollIntoView({ behavior: 'smooth', block: 'center' })
    }
  }, 100)
}

const loadProduct = async () => {
  try {
    const productId = route.params.id
    console.log('加载商品，ID:', productId)
    const res = await api.get(`/product/detail/${productId}`)
    if (res.code === 200) {
      product.value = res.data.product
      merchant.value = res.data.merchant
      specs.value = res.data.specs || []
      
      // 如果有规格，默认选择第一个
      if (specs.value.length > 0) {
        selectedSpec.value = specs.value[0]
      }

      // 切换商品时重置数量，避免新商品的 max < 旧 quantity 触发 ElInputNumber 异常
      quantity.value = 1
      
      // 确保商品加载成功后再加载评论
      if (product.value && product.value.id) {
        console.log('开始加载评论，商品ID:', product.value.id)
        await loadReviews()
      }
      
      if (merchant.value) {
        await loadMerchantReviews()
      }

      // 评论加载完成后，如果带有 reviewId，则滚动到该条评价并短暂高亮
      const reviewIdParam = route.query.reviewId
      if (reviewIdParam) {
        const idNum = Number(reviewIdParam)
        if (idNum) {
          highlightedReviewId.value = idNum
          scrollToHighlightedReview(idNum)
          // 5 秒后自动取消高亮
          setTimeout(() => {
            if (highlightedReviewId.value === idNum) {
              highlightedReviewId.value = null
            }
          }, 5000)
        }
      }
      await checkFavorite()
    }
  } catch (error) {
    console.error('加载商品失败:', error)
    ElMessage.error('加载商品失败')
    product.value = null
    merchant.value = null
    specs.value = []
    selectedSpec.value = null
    reviews.value = []
    merchantReviews.value = []
  }
}

const selectSpec = (spec) => {
  selectedSpec.value = spec
  quantity.value = 1
}

// quantityMax 变化时把 quantity 夹到 [1, quantityMax]，确保 ElInputNumber 永远合法
watch(
  () => quantityMax.value,
  (max) => {
    if (!Number.isFinite(max)) return
    if (quantity.value < 1) quantity.value = 1
    if (quantity.value > max) quantity.value = max
  }
)

const loadMerchantReviews = async () => {
  if (!merchant.value) return
  try {
    const res = await api.get(`/review/merchant/${merchant.value.id}`)
    if (res.code === 200) {
      merchantReviews.value = (res.data.reviews || []).map(review => {
        // 确保 replies 是一个数组
        if (!review.replies) {
          review.replies = []
        }
        return review
      })
    }
  } catch (error) {
    console.error('加载店铺评价失败', error)
  }
}

const handleTabChange = (tabName) => {
  if (tabName === 'merchantReviews' && merchantReviews.value.length === 0 && merchant.value) {
    loadMerchantReviews()
  }
}

const checkFavorite = async () => {
  if (!userStore.user) return
  try {
    const res = await api.get(`/favorite/product/check/${route.params.id}`)
    if (res.code === 200) {
      isFavorite.value = res.data === true || res.data === 'true'
    }
  } catch (error) {
    // 未登录或未收藏时忽略错误
    console.debug('检查收藏状态失败:', error)
  }
}

const toggleFavorite = async () => {
  if (!userStore.user) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  try {
    if (isFavorite.value) {
      const res = await api.delete(`/favorite/product/remove/${route.params.id}`)
      if (res.code === 200) {
        ElMessage.success('取消收藏成功')
        isFavorite.value = false
      } else {
        ElMessage.error(res.message || '取消收藏失败')
      }
    } else {
      const res = await api.post('/favorite/product/add', { productId: route.params.id })
      if (res.code === 200) {
        ElMessage.success('收藏成功')
        isFavorite.value = true
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

const submitReply = async (reviewId) => {
  if (!userStore.user) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  const content = replyContents.value[reviewId]
  if (!content || !content.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }
  replying.value[reviewId] = true
  try {
    const replyToId = replyToIds.value[reviewId] || null
    const res = await api.post(`/review/reply/${reviewId}`, { 
      content: content.trim(),
      replyToId: replyToId
    })
    if (res.code === 200) {
      ElMessage.success('回复成功')
      replyContents.value[reviewId] = ''
      replyToIds.value[reviewId] = null // 清空回复目标
      // 重新加载评价列表
      await loadReviews()
    } else {
      ElMessage.error(res.message || '回复失败')
    }
  } catch (error) {
    const errorMsg = error.response?.data?.message || error.message || '回复失败'
    ElMessage.error(errorMsg)
    console.error('回复失败:', error)
  } finally {
    replying.value[reviewId] = false
  }
}

const toggleLike = async (reviewId, type) => {
  if (!userStore.user) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  try {
    console.log('点赞请求:', { reviewId, type, url: `/review/like/${reviewId}` })
    const res = await api.post(`/review/like/${reviewId}`, { type })
    console.log('点赞响应:', res)
    if (res.code === 200) {
      // 更新商品评价数据
      const review = reviews.value.find(r => r.id === reviewId)
      if (review) {
        review.likeCount = res.data.likeCount || 0
        review.dislikeCount = res.data.dislikeCount || 0
        review.userLikeType = res.data.userLikeType
      }
      // 更新店铺评价数据
      const merchantReview = merchantReviews.value.find(r => r.id === reviewId)
      if (merchantReview) {
        merchantReview.likeCount = res.data.likeCount || 0
        merchantReview.dislikeCount = res.data.dislikeCount || 0
        merchantReview.userLikeType = res.data.userLikeType
      }
      ElMessage.success('操作成功')
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    console.error('点赞失败详情:', {
      message: error.message,
      response: error.response,
      status: error.response?.status,
      data: error.response?.data,
      url: error.config?.url
    })
    const errorMsg = error.response?.data?.message || error.message || '操作失败'
    if (error.response?.status === 404) {
      ElMessage.error('接口不存在，请检查后端服务是否启动')
    } else {
      ElMessage.error(errorMsg)
    }
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

// 获取头像URL（专门处理头像路径）
const getAvatarUrl = (avatar) => {
  if (!avatar || avatar.trim() === '') return null
  const trimmedAvatar = avatar.trim()
  // 如果是完整的URL，直接返回
  if (trimmedAvatar.startsWith('http://') || trimmedAvatar.startsWith('https://')) {
    return trimmedAvatar
  }
  // 如果已经包含 /api/uploads/，直接返回
  if (trimmedAvatar.startsWith('/api/uploads/')) {
    return trimmedAvatar
  }
  // 如果以 /uploads/ 开头，添加 /api 前缀
  if (trimmedAvatar.startsWith('/uploads/')) {
    return '/api' + trimmedAvatar
  }
  // 如果路径不完整，尝试添加完整前缀
  if (!trimmedAvatar.startsWith('/')) {
    return '/api/uploads/' + trimmedAvatar
  }
  // 其他情况，直接返回（可能是相对路径）
  return trimmedAvatar
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

const loadReviews = async () => {
  try {
    const productId = route.params.id
    if (!productId) {
      console.error('商品ID不存在')
      reviews.value = []
      return
    }
    
    console.log('开始加载商品评价，商品ID:', productId)
    const res = await api.get(`/review/product/${productId}`)
    console.log('评论接口返回:', res)
    console.log('返回数据类型:', typeof res.data, Array.isArray(res.data))
    
    if (res && res.code === 200) {
      // 处理不同的数据格式
      let reviewsData = []
      if (Array.isArray(res.data)) {
        reviewsData = res.data
        console.log('直接使用res.data数组，数量:', reviewsData.length)
      } else if (res.data && Array.isArray(res.data.reviews)) {
        reviewsData = res.data.reviews
        console.log('使用res.data.reviews数组，数量:', reviewsData.length)
      } else if (res.data && Array.isArray(res.data.data)) {
        reviewsData = res.data.data
        console.log('使用res.data.data数组，数量:', reviewsData.length)
      } else {
        console.warn('无法解析评论数据，res.data:', res.data)
        reviewsData = []
      }
      
      if (reviewsData.length > 0) {
        reviews.value = reviewsData.map(review => {
          // 确保 replies 是一个数组
          if (!review.replies) {
            review.replies = []
          }
          // 确保点赞数量存在
          if (review.likeCount === undefined || review.likeCount === null) {
            review.likeCount = 0
          }
          if (review.dislikeCount === undefined || review.dislikeCount === null) {
            review.dislikeCount = 0
          }
          return review
        })
        console.log('成功加载评论数量:', reviews.value.length)
        if (reviews.value.length > 0) {
          console.log('第一条评论数据:', reviews.value[0])
        }
      } else {
        console.log('该商品暂无评价')
        reviews.value = []
      }
    } else {
      console.error('加载评价失败:', res?.message || '未知错误', res)
      reviews.value = []
    }
  } catch (error) {
    console.error('加载评价失败', error)
    console.error('错误详情:', {
      message: error.message,
      response: error.response,
      status: error.response?.status,
      data: error.response?.data
    })
    reviews.value = []
    ElMessage.error('加载评价失败: ' + (error.response?.data?.message || error.message))
  }
}

const addToCart = async () => {
  if (!userStore.user) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  if (specs.value.length > 0 && !selectedSpec.value) {
    ElMessage.warning('请选择规格')
    return
  }
  if (quantityDisabled.value) {
    ElMessage.warning('库存不足或不可购买')
    return
  }
  try {
    const specId = selectedSpec.value ? selectedSpec.value.id : null
    await cartStore.addToCart(product.value.id, specId, quantity.value)
    ElMessage.success('已添加到购物车')
  } catch (error) {
    ElMessage.error(error.message || '添加失败')
  }
}

const buyNow = async () => {
  if (!userStore.user) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  if (specs.value.length > 0 && !selectedSpec.value) {
    ElMessage.warning('请选择规格')
    return
  }
  if (quantityDisabled.value) {
    ElMessage.warning('库存不足或不可购买')
    return
  }
  try {
    // 立即购买：直接跳转到结算页面，传递商品信息
    const buyItem = {
      productId: product.value.id,
      specId: selectedSpec.value ? selectedSpec.value.id : null,
      quantity: quantity.value,
      productName: product.value.name,
      productImage: product.value.mainImage,
      price: selectedSpec.value ? selectedSpec.value.price : product.value.price,
      specName: selectedSpec.value ? selectedSpec.value.specName : null
    }
    router.push({
      path: '/checkout',
      query: {
        buyNow: 'true',
        item: JSON.stringify(buyItem)
      }
    })
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

onMounted(() => {
  loadProduct()
})

// 当路由参数变化（例如从某个列表连续打开不同商品），确保重新拉取详情
watch(
  () => route.params.id,
  () => {
    loadProduct()
  }
)
</script>

<style scoped>
.product-detail {
  background: #ffffff;
  padding: 24px;
  border-radius: 8px;
}

.product-image-wrapper {
  width: 100%;
  height: 500px;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  overflow: hidden;
  background: #f5f7fa;
}

.product-image-wrapper img {
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
  padding-left: 24px;
}

.product-title {
  font-size: 24px;
  color: #262626;
  margin-bottom: 16px;
}

.product-price-section {
  margin-bottom: 16px;
}

.current-price {
  font-size: 32px;
  color: #ff4d4f;
  font-weight: 600;
  margin-right: 12px;
}

.original-price {
  font-size: 18px;
  color: #8c8c8c;
  text-decoration: line-through;
}

.product-meta-info {
  margin-bottom: 24px;
  display: flex;
  gap: 12px;
}

.product-description {
  margin-bottom: 32px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 4px;
}

.product-description h3 {
  font-size: 16px;
  color: #262626;
  margin-bottom: 12px;
}

.product-description p {
  color: #595959;
  line-height: 1.6;
}

.product-actions {
  display: flex;
  gap: 16px;
  align-items: center;
}

.product-actions .el-button {
  flex: 1;
}

.reviews-section {
  margin-top: 40px;
  padding-top: 32px;
  border-top: 1px solid #e8e8e8;
}

.reviews-section h2 {
  font-size: 20px;
  color: #262626;
  margin-bottom: 24px;
}

.no-reviews {
  padding: 40px 0;
}

.reviews-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.review-item {
  padding: 20px;
  background: #fafafa;
  border-radius: 8px;
}

.review-item-highlight {
  border: 2px solid #1890ff;
  background: #e6f7ff;
}

.review-main-content {
  display: flex;
  gap: 20px;
  align-items: flex-start;
}

.review-left {
  flex: 1;
  min-width: 0;
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.reviewer-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.reviewer-name {
  font-weight: 500;
  color: #262626;
}

.review-content {
  color: #595959;
  line-height: 1.6;
  margin-bottom: 12px;
}

.review-time {
  font-size: 12px;
  color: #8c8c8c;
  margin-top: 8px;
}

.review-order-info {
  margin-top: 12px;
  padding: 8px 12px;
  background: #f5f5f5;
  border-radius: 4px;
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  font-size: 13px;
}

.order-info-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.order-label {
  color: #8c8c8c;
}

.order-value {
  color: #262626;
  font-weight: 500;
}

.review-product-info {
  margin-top: 12px;
  padding: 12px;
  background: #fafafa;
  border-radius: 6px;
  display: flex;
  align-items: flex-start;
  gap: 12px;
  border: 1px solid #e8e8e8;
}

.review-product-info .product-image-wrapper {
  flex-shrink: 0;
  width: 80px;
  height: 80px;
  border-radius: 6px;
  overflow: hidden;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid #e0e0e0;
}

.review-product-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.review-product-info .product-details {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-width: 0;
}

/* 如果没有商品图片，让详情信息占满宽度 */
.review-product-info:not(:has(.product-image-wrapper)) .product-details {
  width: 100%;
}

.product-info-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  line-height: 1.5;
}

.product-label {
  color: #8c8c8c;
  white-space: nowrap;
}

.product-value {
  color: #262626;
  font-weight: 500;
}

.review-actions-right {
  flex-shrink: 0;
  display: flex;
  align-items: flex-start;
  padding-top: 4px;
}

.like-actions {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 12px;
}

.like-btn {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  background: transparent;
  border: none;
  cursor: pointer;
  border-radius: 4px;
  transition: all 0.2s;
  color: #8c8c8c;
  font-size: 13px;
  min-width: auto;
}

.like-btn:hover:not(:disabled) {
  background: #f5f5f5;
  color: #262626;
}

.like-btn:disabled {
  cursor: not-allowed;
  opacity: 0.5;
}

.like-btn.active {
  color: #1890ff;
}

.like-btn.dislike-btn.active {
  color: #ff4d4f;
}

.like-icon {
  font-size: 16px;
  line-height: 1;
}

.like-count {
  font-size: 13px;
  font-weight: 500;
  line-height: 1;
}

.review-product {
  font-size: 12px;
  color: #8c8c8c;
  margin-top: 8px;
}

.replies-section {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #e8e8e8;
}

.replies-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.replies-count {
  font-size: 14px;
  color: #8c8c8c;
  font-weight: 500;
}

.replies-list {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.reply-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid #f5f5f5;
  transition: background-color 0.2s;
}

.reply-item:last-child {
  border-bottom: none;
}

.reply-item:hover {
  background-color: #fafafa;
  border-radius: 4px;
  padding-left: 8px;
  padding-right: 8px;
}

.reply-avatar {
  flex-shrink: 0;
}

.reply-content-wrapper {
  flex: 1;
  min-width: 0;
}

.reply-header-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 6px;
}

.reply-username-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
}

.reply-username {
  font-size: 14px;
  font-weight: 500;
  color: #262626;
}

.reply-to-info {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  margin-left: 4px;
  color: #8c8c8c;
  font-size: 13px;
}

.reply-to-text {
  color: #8c8c8c;
}

.reply-to-name {
  color: #1890ff;
  cursor: pointer;
}

.reply-to-name:hover {
  text-decoration: underline;
}

.merchant-tag {
  font-size: 11px;
  padding: 2px 6px;
  border-radius: 2px;
  background-color: #fff7e6;
  color: #fa8c16;
  border: 1px solid #ffd591;
}

.reply-time {
  font-size: 12px;
  color: #8c8c8c;
}

.reply-content-text {
  color: #595959;
  line-height: 1.6;
  font-size: 14px;
  margin-bottom: 8px;
  word-wrap: break-word;
  white-space: pre-wrap;
}

.reply-actions {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-top: 4px;
}

.reply-btn {
  padding: 0;
  color: #8c8c8c;
  font-size: 13px;
}

.reply-btn:hover {
  color: #1890ff;
}

.more-replies,
.collapse-replies {
  text-align: center;
  padding: 12px 0;
  margin-top: 8px;
}

.more-replies-btn,
.collapse-replies-btn {
  color: #1890ff;
  font-size: 14px;
  padding: 0;
}

.more-replies-btn:hover,
.collapse-replies-btn:hover {
  color: #40a9ff;
}

.replies-pagination {
  margin-top: 16px;
  display: flex;
  justify-content: center;
  padding: 12px 0;
}

.reply-content {
  color: #595959;
  line-height: 1.6;
  font-size: 14px;
}

.reply-input-section {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #e8e8e8;
}

.merchant-info-section {
  margin-bottom: 24px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 4px;
}

.merchant-info-section h3 {
  font-size: 16px;
  color: #262626;
  margin-bottom: 12px;
}

.merchant-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  background: #ffffff;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.merchant-card:hover {
  background: #e6f7ff;
}

.merchant-name {
  font-weight: 500;
  color: #1890ff;
}

.spec-section {
  margin-bottom: 24px;
}

.spec-section h3 {
  font-size: 16px;
  color: #262626;
  margin-bottom: 12px;
}

.spec-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.spec-item {
  padding: 12px 20px;
  border: 2px solid #e8e8e8;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  background: #ffffff;
  min-width: 120px;
  text-align: center;
  position: relative;
}

.spec-item:hover {
  border-color: #1890ff;
}

.spec-item.active {
  border-color: #1890ff;
  background: #e6f7ff;
}

.spec-name {
  font-size: 14px;
  color: #262626;
  margin-bottom: 8px;
  font-weight: 500;
}

.spec-price {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.spec-price .current-price {
  font-size: 16px;
  color: #ff4d4f;
  font-weight: 600;
}

.spec-price .original-price {
  font-size: 12px;
  color: #8c8c8c;
  text-decoration: line-through;
}

.spec-discount {
  position: absolute;
  top: -8px;
  right: -8px;
  background: #ff4d4f;
  color: #ffffff;
  font-size: 12px;
  padding: 2px 6px;
  border-radius: 4px;
}

.detail-tabs {
  margin-top: 40px;
}

.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from, .fade-leave-to {
  opacity: 0;
}
</style>

