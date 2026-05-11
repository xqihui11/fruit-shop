<template>
  <div class="reviews-page">
    <!-- 页面标题区 -->
    <div class="page-header">
      <h1 class="page-title">用户评价</h1>
      <div class="avg-rating">
        <span class="rating-label">店铺评分：</span>
        <el-rate v-model="avgRating" disabled :colors="['#FFC107', '#FFC107', '#FFC107']" />
        <span class="rating-score">{{ avgRating.toFixed(1) }}</span>
      </div>
    </div>

    <!-- 筛选栏 -->
    <el-card class="filter-card" shadow="never">
      <el-form :inline="true" :model="filterForm" class="filter-form">
        <el-form-item label="评分">
          <el-select v-model="filterForm.rating" placeholder="全部" clearable style="width: 150px">
            <el-option label="全部" value="" />
            <el-option label="5星" :value="5" />
            <el-option label="4星" :value="4" />
            <el-option label="3星" :value="3" />
            <el-option label="2星" :value="2" />
            <el-option label="1星" :value="1" />
            <el-option label="4星及以上" value="4+" />
            <el-option label="2-3星" value="2-3" />
          </el-select>
        </el-form-item>
        <el-form-item label="商品">
          <el-select v-model="filterForm.productId" placeholder="全部" clearable style="width: 150px">
            <el-option label="全部" value="" />
            <el-option
              v-for="product in productList"
              :key="product.id"
              :label="product.name"
              :value="product.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="排序">
          <el-select v-model="filterForm.sort" placeholder="最新" style="width: 120px">
            <el-option label="最新" value="latest" />
            <el-option label="最早" value="oldest" />
            <el-option label="评分最高" value="highest" />
            <el-option label="评分最低" value="lowest" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 评价列表 -->
    <div v-if="filteredReviews.length === 0 && !loading" class="empty-container">
      <el-empty description="暂无用户评价" :image-size="120" />
    </div>
    <div v-else class="reviews-grid">
      <div
        v-for="review in filteredReviews"
        :key="review.id"
        class="review-card"
        @mouseenter="hoveredCard = review.id"
        @mouseleave="hoveredCard = null"
      >
        <!-- 评价头部 -->
        <div class="review-header">
          <div class="user-info">
            <el-avatar :size="40" :src="review.avatar ? getAvatarUrl(review.avatar) : undefined">
              {{ review.username?.charAt(0) || 'U' }}
            </el-avatar>
            <div class="user-detail">
              <span class="username">{{ review.username || '匿名用户' }}</span>
              <div class="time-info">
                <el-icon class="time-icon"><Clock /></el-icon>
                <span class="time">{{ formatTime(review.createTime) }}</span>
              </div>
            </div>
          </div>
          <el-rate
            v-model="review.rating"
            disabled
            :colors="['#FFC107', '#FFC107', '#FFC107']"
            class="rating-stars"
          />
        </div>

        <!-- 商品信息 -->
        <div class="review-product">
          <img
            v-if="review.productImage"
            :src="getImageUrl(review.productImage)"
            class="product-thumb"
            alt="商品图片"
          />
          <div v-else class="product-thumb-placeholder">
            <el-icon><Picture /></el-icon>
          </div>
          <el-tag class="product-tag" size="small">
            {{ review.productName || '商品' }}
          </el-tag>
        </div>

        <!-- 评价内容 -->
        <div class="review-content-wrapper">
          <div
            v-if="!expandedContent[review.id] && (review.content || '').length > 100"
            class="review-content"
          >
            {{ (review.content || '用户未填写评价内容').substring(0, 100) }}...
          </div>
          <div v-else class="review-content">
            {{ review.content || '用户未填写评价内容' }}
          </div>
          <el-button
            v-if="(review.content || '').length > 100"
            link
            size="small"
            class="expand-btn"
            @click="toggleContent(review.id)"
          >
            {{ expandedContent[review.id] ? '收起' : '展开' }}
          </el-button>
        </div>

        <!-- 评价图片 -->
        <div v-if="review.images && review.images.length > 0" class="review-images">
          <el-image
            v-for="(img, index) in getReviewImages(review.images)"
            :key="index"
            :src="getImageUrl(img)"
            :preview-src-list="getReviewImages(review.images).map(i => getImageUrl(i))"
            fit="cover"
            class="review-image"
          />
        </div>
          
        <!-- 回复区 -->
        <div v-if="review.replies && Array.isArray(review.replies) && review.replies.length > 0" class="replies-section">
          <div class="replies-header">
            <span class="replies-count">相关回复共{{ review.replies.length }}条</span>
          </div>
          <!-- 显示回复列表（默认只显示2条） -->
          <div class="replies-list">
            <div
              v-for="reply in getDisplayReplies(review)"
              :key="reply.id"
              class="reply-item"
            >
              <div class="reply-avatar">
                <el-avatar
                  :size="32"
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
                    <el-tag v-if="reply.replyType === 1" class="merchant-reply-tag" size="small">
                      商家回复
                    </el-tag>
                    <span v-if="reply.replyToId" class="reply-to-info">
                      <span class="reply-to-text">回复</span>
                      <span class="reply-to-name">
                        {{ reply.replyToMerchantName || reply.replyToUsername || '用户' }}
                      </span>
                    </span>
                  </div>
                  <span class="reply-time">{{ formatTime(reply.createTime) }}</span>
                </div>
                <div class="reply-content-text">{{ reply.content }}</div>
                <!-- 楼中楼回复：商家可回复任意一条回复，参与后续讨论 -->
                <div class="reply-actions">
                  <el-button
                    link
                    size="small"
                    class="reply-btn"
                    @click="showReplyDialog(review, reply)"
                  >
                    <el-icon><ChatLineRound /></el-icon>
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
          <div
            v-if="review.replies.length > 2 && !expandedReplies[review.id]"
            class="more-replies"
          >
            <el-button link class="more-replies-btn" @click="expandReplies(review.id)">
              查看全部 {{ review.replies.length }} 条回复 >
            </el-button>
          </div>
          <!-- 收起按钮 -->
          <div
            v-if="expandedReplies[review.id] && review.replies.length > 2"
            class="collapse-replies"
          >
            <el-button link class="collapse-replies-btn" @click="collapseReplies(review.id)">
              收起回复
            </el-button>
          </div>
        </div>

        <!-- 兼容旧数据格式：显示商家回复 -->
        <div v-else-if="review.merchantReply" class="merchant-reply">
          <el-tag class="merchant-reply-tag" size="small">商家回复</el-tag>
          <div class="reply-content">{{ review.merchantReply }}</div>
        </div>

        <!-- 快捷操作（hover时显示） -->
        <div v-if="hoveredCard === review.id" class="quick-actions">
          <el-button
            type="primary"
            size="small"
            @click="showReplyDialog(review, null)"
          >
            <el-icon><ChatLineRound /></el-icon>
            回复
          </el-button>
        </div>
      </div>
    </div>

    <!-- 回复对话框 -->
    <el-dialog v-model="replyDialogVisible" title="回复评价" width="500px">
      <div class="reply-review-info">
        <p><strong>用户评价：</strong>{{ currentReview?.content || '用户未填写评价内容' }}</p>
        <p v-if="replyTo" class="reply-to-hint">
          <strong>回复对象：</strong>@{{ replyTo.name }}
        </p>
      </div>
      <el-input
        v-model="replyContent"
        type="textarea"
        :rows="4"
        placeholder="请输入回复内容..."
        maxlength="500"
        show-word-limit
      />
      <template #footer>
        <el-button @click="replyDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReply" :loading="submitting" :disabled="!replyContent.trim()">
          提交回复
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Clock,
  Picture,
  ChatLineRound
} from '@element-plus/icons-vue'
import api from '@/utils/api'

const reviews = ref([])
const avgRating = ref(5)
const replyDialogVisible = ref(false)
const currentReview = ref(null)
const replyContent = ref('')
const submitting = ref(false)
const loading = ref(false)
const expandedReplies = ref({}) // 存储每个评价的回复展开状态
const expandedContent = ref({}) // 存储每个评价的内容展开状态
const hoveredCard = ref(null) // 当前hover的卡片ID
const productList = ref([]) // 商品列表
const replyTo = ref(null) // { id, name } - 回复目标（楼中楼）
const replyPages = ref({}) // 存储每个评价的回复分页状态 { reviewId: currentPage }

// 筛选表单
const filterForm = ref({
  rating: '',
  productId: '',
  sort: 'latest'
})

// 筛选后的评价列表
const filteredReviews = computed(() => {
  let result = [...reviews.value]

  // 按评分筛选
  if (filterForm.value.rating) {
    const ratingFilter = filterForm.value.rating
    if (ratingFilter === '4+') {
      // 4星及以上
      result = result.filter(review => review.rating >= 4)
    } else if (ratingFilter === '2-3') {
      // 2-3星之间
      result = result.filter(review => review.rating >= 2 && review.rating <= 3)
    } else {
      // 单个星级（1-5星）
      const exactRating = Number(ratingFilter)
      result = result.filter(review => review.rating === exactRating)
    }
  }

  // 按商品筛选
  if (filterForm.value.productId) {
    result = result.filter(review => review.productId === filterForm.value.productId)
  }

  // 排序
  if (filterForm.value.sort === 'latest') {
    result.sort((a, b) => new Date(b.createTime) - new Date(a.createTime))
  } else if (filterForm.value.sort === 'oldest') {
    result.sort((a, b) => new Date(a.createTime) - new Date(b.createTime))
  } else if (filterForm.value.sort === 'highest') {
    result.sort((a, b) => b.rating - a.rating)
  } else if (filterForm.value.sort === 'lowest') {
    result.sort((a, b) => a.rating - b.rating)
  }

  return result
})

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

// 获取评价图片列表
const getReviewImages = (images) => {
  if (!images) return []
  if (typeof images === 'string') {
    return images.split(',').filter(img => img.trim())
  }
  return Array.isArray(images) ? images : []
}

// 切换内容展开/收起
const toggleContent = (reviewId) => {
  expandedContent.value[reviewId] = !expandedContent.value[reviewId]
}

// 重置筛选
const handleReset = () => {
  filterForm.value = {
    rating: '',
    productId: '',
    sort: 'latest'
  }
  ElMessage.success('筛选条件已重置')
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

// 获取要显示的回复列表（默认2条，展开后分页显示，每页5条）
const getDisplayReplies = (review) => {
  if (!review.replies || review.replies.length === 0) {
    return []
  }
  
  // 如果未展开，只显示前2条
  if (!expandedReplies.value[review.id]) {
    return review.replies.length <= 2 ? review.replies : review.replies.slice(0, 2)
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

const hasMerchantReply = (review) => {
  if (review.replies && Array.isArray(review.replies)) {
    return review.replies.some(reply => reply.replyType === 1)
  }
  return !!review.merchantReply
}

// 打开回复框：支持回复评价本身、也支持回复某一条回复（楼中楼）
const showReplyDialog = (review, reply = null) => {
  currentReview.value = review
  replyContent.value = ''
  if (reply && reply.id) {
    const targetName =
      reply.replyType === 1
        ? reply.merchantName || '商家'
        : reply.username || '用户'
    replyTo.value = { id: reply.id, name: targetName }
  } else {
    replyTo.value = null
  }
  replyDialogVisible.value = true
}

const loadReviews = async () => {
  loading.value = true
  try {
    // 获取当前商家信息
    const infoRes = await api.get('/merchant/info')
    if (infoRes.code === 200 && infoRes.data) {
      const merchantId = infoRes.data.id
      const res = await api.get(`/review/merchant/${merchantId}`)
      if (res.code === 200) {
        reviews.value = (res.data.reviews || []).map(review => {
          // 确保 replies 是一个数组
          if (!review.replies) {
            review.replies = []
          }
          return review
        })
        avgRating.value = res.data.averageRating || 5
      }
    }
  } catch (error) {
    console.error('加载评价失败', error)
    ElMessage.error('加载评价失败')
  } finally {
    loading.value = false
  }
}

// 加载商品列表
const loadProducts = async () => {
  try {
    const res = await api.get('/merchant/product/list')
    if (res.code === 200) {
      productList.value = res.data || []
    }
  } catch (error) {
    console.error('加载商品列表失败', error)
  }
}

// 获取图片URL
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

const submitReply = async () => {
  if (!replyContent.value.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }
  submitting.value = true
  try {
    const payload = {
      content: replyContent.value.trim()
    }
    if (replyTo.value?.id) {
      payload.replyToId = replyTo.value.id
    }
    const res = await api.post(`/review/reply/${currentReview.value.id}`, payload)
    if (res.code === 200) {
      ElMessage.success('回复成功')
      replyDialogVisible.value = false
      replyContent.value = ''
      replyTo.value = null
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
    submitting.value = false
  }
}

onMounted(() => {
  loadReviews()
  loadProducts()
})
</script>

<style scoped>
.reviews-page {
  padding: 24px;
  background: #f5f7fa;
  min-height: 100vh;
  font-family: 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

/* 页面标题区 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
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

.avg-rating {
  display: flex;
  align-items: center;
  gap: 8px;
}

.rating-label {
  font-size: 14px;
  color: #7f8c8d;
  font-weight: 500;
}

.rating-score {
  font-size: 18px;
  font-weight: 600;
  color: #2ecc71;
  margin-left: 4px;
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

/* 评价网格布局 */
.reviews-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: 20px;
}

/* 评价卡片 */
.review-card {
  padding: 20px;
  background: #ffffff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  position: relative;
}

.review-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
  transform: translateY(-2px);
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.user-detail {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.username {
  font-weight: 500;
  color: #2c3e50;
  font-size: 14px;
}

.time-info {
  display: flex;
  align-items: center;
  gap: 4px;
}

.time-icon {
  font-size: 12px;
  color: #bdc3c7;
}

.time {
  font-size: 12px;
  color: #bdc3c7;
}

.rating-stars {
  flex-shrink: 0;
}

.rating-stars :deep(.el-rate__icon) {
  font-size: 18px;
}

/* 商品信息 */
.review-product {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.product-thumb {
  width: 40px;
  height: 40px;
  border-radius: 4px;
  object-fit: cover;
  border: 1px solid #e6e6e6;
}

.product-thumb-placeholder {
  width: 40px;
  height: 40px;
  border-radius: 4px;
  background: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #bdc3c7;
  border: 1px solid #e6e6e6;
}

.product-tag {
  background: #e8f5e9;
  color: #2e7d32;
  border: none;
  border-radius: 4px;
  padding: 2px 8px;
  font-size: 12px;
}

/* 评价内容 */
.review-content-wrapper {
  margin-bottom: 16px;
}

.review-content {
  color: #7f8c8d;
  line-height: 1.6;
  font-size: 14px;
  word-break: break-word;
}

.expand-btn {
  color: #2ecc71;
  font-size: 12px;
  padding: 4px 0;
  margin-top: 4px;
}

.expand-btn:hover {
  color: #27ae60;
}

/* 评价图片 */
.review-images {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 16px;
}

.review-image {
  width: 80px;
  height: 80px;
  border-radius: 4px;
  cursor: pointer;
  border: 1px solid #e6e6e6;
}

/* 回复区 */
.replies-section {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #e6e6e6;
  background: #f8f9fa;
  border-radius: 8px;
  padding: 16px;
}

.replies-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid #e6e6e6;
}

.replies-count {
  font-size: 13px;
  color: #7f8c8d;
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
  gap: 10px;
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
}

.reply-item:last-child {
  border-bottom: none;
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
  gap: 6px;
}

.reply-username {
  font-size: 13px;
  font-weight: 500;
  color: #2c3e50;
}

.merchant-reply-tag {
  background: #2ecc71;
  color: #ffffff;
  border: none;
  border-radius: 4px;
  padding: 2px 6px;
  font-size: 11px;
}

.reply-to-info {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  margin-left: 4px;
  color: #7f8c8d;
  font-size: 12px;
}

.reply-to-text {
  color: #7f8c8d;
}

.reply-to-name {
  color: #2ecc71;
  cursor: pointer;
}

.reply-to-name:hover {
  text-decoration: underline;
}

.reply-time {
  font-size: 12px;
  color: #bdc3c7;
}

.reply-content-text {
  font-size: 13px;
  color: #7f8c8d;
  line-height: 1.6;
  word-break: break-word;
}

.reply-actions {
  margin-top: 6px;
}

.reply-btn {
  color: #2ecc71;
  padding: 0;
}

.reply-btn:hover {
  color: #27ae60;
}

.more-replies {
  margin-top: 12px;
  text-align: center;
  padding: 8px 0;
}

.more-replies-btn {
  color: #2ecc71;
  font-size: 13px;
  padding: 4px 8px;
}

.more-replies-btn:hover {
  color: #27ae60;
}

.replies-pagination {
  margin-top: 16px;
  display: flex;
  justify-content: center;
  padding: 12px 0;
}

.collapse-replies {
  margin-top: 12px;
  text-align: center;
  padding: 8px 0;
}

.collapse-replies-btn {
  color: #2ecc71;
  font-size: 13px;
  padding: 4px 8px;
}

.collapse-replies-btn:hover {
  color: #27ae60;
}

/* 商家回复（旧格式） */
.merchant-reply {
  margin-top: 16px;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 8px;
  display: flex;
  align-items: flex-start;
  gap: 8px;
}

.merchant-reply .reply-content {
  color: #7f8c8d;
  font-size: 14px;
  line-height: 1.6;
  flex: 1;
}

/* 快捷操作 */
.quick-actions {
  position: absolute;
  top: 20px;
  right: 20px;
  display: flex;
  gap: 8px;
}

.quick-actions .el-button {
  background: #2ecc71;
  border-color: #2ecc71;
  color: #ffffff;
}

.quick-actions .el-button:hover {
  background: #27ae60;
  border-color: #27ae60;
}

/* 空状态 */
.empty-container {
  padding: 60px 0;
  text-align: center;
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
  font-size: 14px;
  color: #595959;
  line-height: 1.6;
  word-break: break-word;
}

.more-replies {
  margin-top: 12px;
  text-align: center;
  padding: 8px 0;
}

.more-replies-btn {
  color: #1890ff;
  font-size: 14px;
  padding: 4px 8px;
}

.more-replies-btn:hover {
  color: #40a9ff;
}

.collapse-replies {
  margin-top: 12px;
  text-align: center;
  padding: 8px 0;
}

.collapse-replies-btn {
  color: #1890ff;
  font-size: 14px;
  padding: 4px 8px;
}

.collapse-replies-btn:hover {
  color: #40a9ff;
}

.reply-review-info {
  margin-bottom: 16px;
  padding: 12px;
  background: #f5f5f5;
  border-radius: 4px;
}

.reply-review-info p {
  margin: 0;
  color: #595959;
}

.reply-to-hint {
  margin-top: 8px !important;
  color: #7f8c8d !important;
}

/* 响应式适配 */
@media (max-width: 1200px) {
  .reviews-grid {
    grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  }
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .reviews-grid {
    grid-template-columns: 1fr;
  }

  .filter-form {
    flex-direction: column;
  }

  .filter-form :deep(.el-form-item) {
    margin-right: 0;
    margin-bottom: 12px;
  }

  .review-card {
    padding: 16px;
  }

  .quick-actions {
    position: static;
    margin-top: 12px;
    justify-content: flex-end;
  }
}
</style>

