<template>
  <div class="chat-page">
    <div class="notice-bar" :class="{ expanded: noticeExpanded }" @click="toggleNotice">
      <div class="notice-left">
        <span class="notice-icon">ℹ️</span>
        <span class="notice-title">关于遵守网络法律法规，营造健康文明上网环境的倡议</span>
      </div>
      <div class="notice-right">
        <span class="notice-action">{{ noticeExpanded ? '收起' : '展开详情' }}</span>
      </div>
    </div>
    <div v-if="noticeExpanded" class="notice-detail">
      <div class="notice-detail-text">
        近期诈骗案件频发！请勿轻信任何理由引导您添加外部联系方式、离开平台付款或提前确认收货。发现异常请及时联系平台客服处理。
      </div>
    </div>

    <div class="chat-header">
      <div class="chat-header-left">
        <h2>{{ merchant?.shopName || '与商家聊天' }}</h2>
        <span class="sub-text">可发送文字或图片，多行输入按 Enter+Shift 换行</span>
      </div>
      <div class="chat-header-right">
        <el-button text @click="goHistory">历史对话</el-button>
        <el-button text @click="goContact">联系客服</el-button>
      </div>
    </div>

    <div class="chat-body" ref="chatBodyRef">
      <div v-if="!loading && messages.length > visibleCount" class="load-more">
        <el-button text type="primary" @click="loadMore">加载更多</el-button>
      </div>
      <div v-if="loading" class="loading">
        <el-skeleton :rows="6" animated />
      </div>
      <div v-else-if="messages.length === 0" class="empty">
        <el-empty description="还没有聊天记录，快给商家发一条消息吧~" />
      </div>
      <div v-else class="message-list">
        <div
          v-for="msg in visibleMessages"
          :key="msg._key"
          class="message-row"
          :class="[
            msg.senderType === 'USER' ? 'from-user' : 'from-merchant',
            msg._justSent ? 'just-sent' : ''
          ]"
        >
          <div
            v-if="msg.senderType === 'MERCHANT'"
            class="avatar-wrapper"
          >
            <div v-if="merchantAvatar" class="avatar-circle">
              <img :src="getImageUrl(merchantAvatar)" alt="商家头像" />
            </div>
            <div v-else class="avatar-circle placeholder">
              {{ (merchant?.shopName || '商家').slice(0, 1) }}
            </div>
          </div>

          <div class="bubble">
            <div class="bubble-actions">
              <el-button text class="action-btn" @click.stop="copyText(msg.content)" v-if="msg.content">复制</el-button>
            </div>
            <!-- 商品卡片优先，其次才是普通文本 -->
            <div
              v-if="getProductCard(msg)"
              class="product-card"
              @click="goProductDetail(getProductCard(msg).id)"
            >
              <div class="product-thumb-wrap">
                <img
                  v-if="getProductCard(msg).image"
                  :src="getImageUrl(getProductCard(msg).image)"
                  alt="商品图片"
                />
                <div v-else class="product-thumb-placeholder">商品</div>
              </div>
              <div class="product-info-wrap">
                <div class="product-title">
                  {{ getProductCard(msg).name || '商品详情' }}
                </div>
                <div class="product-meta">
                  <span class="product-price" v-if="getProductCard(msg).price != null">
                    ¥{{ Number(getProductCard(msg).price).toFixed(2) }}
                  </span>
                  <span class="product-action">去查看</span>
                </div>
              </div>
            </div>
            <!-- 订单卡片优先：展示订单缩略图 + 订单信息 -->
            <div
              v-if="getOrderCard(msg)"
              class="product-card"
              @click="goOrderDetail(getOrderCard(msg).id)"
            >
              <div class="product-thumb-wrap order">
                <img
                  v-if="getOrderCard(msg).image"
                  :src="getImageUrl(getOrderCard(msg).image)"
                  alt="商品图片"
                />
                <span v-else class="order-icon">🧾</span>
              </div>
              <div class="product-info-wrap">
                <div class="product-title">
                  {{ getOrderCard(msg).name || `订单号：${getOrderCard(msg).orderNo}` }}
                </div>
                <div class="product-meta">
                  <span class="product-price">
                    ¥{{ Number(getOrderCard(msg).payAmount || 0).toFixed(2) }}
                  </span>
                  <span class="product-action">
                    {{ getOrderStatusText(getOrderCard(msg).status) }}
                  </span>
                </div>
              </div>
            </div>
            <!-- 其次是商品卡片 -->
            <div
              v-else-if="getProductCard(msg)"
              class="product-card"
              @click="goProductDetail(getProductCard(msg).id)"
            >
              <div class="product-thumb-wrap">
                <img
                  v-if="getProductCard(msg).image"
                  :src="getImageUrl(getProductCard(msg).image)"
                  class="detail-item-img"
                />
                <div v-else class="detail-item-img empty">暂无</div>
              </div>
              <div class="product-info-wrap">
                <div class="product-title">
                  {{ getProductCard(msg).name || '商品详情' }}
                </div>
                <div class="product-meta">
                  <span class="product-price" v-if="getProductCard(msg).price != null">
                    ¥{{ Number(getProductCard(msg).price).toFixed(2) }}
                  </span>
                  <span class="product-action">去查看</span>
                </div>
              </div>
            </div>
            <!-- 普通文本 -->
            <div
              v-else-if="msg.content"
              class="text"
              v-html="formatContent(msg.content)"
            ></div>
            <!-- 纯图片 -->
            <div v-if="msg.imageUrl" class="image">
              <el-image
                :src="getImageUrl(msg.imageUrl)"
                :preview-src-list="[getImageUrl(msg.imageUrl)]"
                preview-teleported
                fit="cover"
              />
            </div>
            <div class="time">{{ formatTime(msg.createTime) }}</div>
          </div>

          <div
            v-if="msg.senderType === 'USER'"
            class="avatar-wrapper avatar-right"
          >
            <div v-if="userAvatar" class="avatar-circle">
              <img :src="getImageUrl(userAvatar)" alt="用户头像" />
            </div>
            <div v-else class="avatar-circle placeholder">
              {{ (userDisplayName || '我').slice(0, 1) }}
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="chat-input">
      <div class="quick-replies">
        <el-button
          v-for="t in quickReplies"
          :key="t"
          class="chip"
          size="small"
          @click="appendQuickReply(t)"
        >
          {{ t }}
        </el-button>
      </div>

      <div class="input-card">
        <div class="input-left">
          <el-upload
            action="/api/file/upload"
            :headers="uploadHeaders"
            :show-file-list="false"
            accept="image/*"
            :before-upload="beforeChatImageUpload"
            :on-success="handleUploadSuccess"
            :on-error="handleUploadError"
          >
            <el-button class="icon-btn" plain title="发送图片">📎</el-button>
          </el-upload>

          <el-popover placement="top-start" trigger="click" width="260">
            <template #reference>
              <el-button class="icon-btn" plain title="表情">😀</el-button>
            </template>
            <div class="emoji-grid">
              <button
                v-for="e in emojis"
                :key="e"
                class="emoji"
                @click="insertEmoji(e)"
              >
                {{ e }}
              </button>
            </div>
          </el-popover>

          <!-- 发送订单卡片 -->
          <el-button class="icon-btn" plain title="发送订单卡片" @click="openOrderDialog">
            🧾
          </el-button>
        </div>

        <el-input
          v-model="content"
          class="textarea"
          type="textarea"
          :autosize="{ minRows: 3, maxRows: 6 }"
          placeholder="请输入要发送的内容..."
          @keydown.enter.exact.prevent="handleEnter"
          @keydown.enter.shift.exact.stop
        />

        <div class="input-right">
          <el-button class="send-btn" type="primary" :loading="sending" @click="sendText">
            发送
          </el-button>
        </div>
      </div>

      <div v-if="sending" class="sending-hint">正在发送…</div>
    </div>
  </div>

  <!-- 选择历史订单并发送订单卡片 -->
  <el-dialog
    v-model="orderDialogVisible"
    title="选择要咨询的订单"
    width="680px"
  >
    <el-table
      v-loading="orderLoading"
      :data="orderList"
      height="380"
    >
      <el-table-column label="订单" min-width="260">
        <template #default="{ row }">
          <div class="order-select-main">
            <div class="order-select-thumb-wrap">
              <img
                v-if="row.firstProductImage"
                :src="getImageUrl(row.firstProductImage)"
                class="order-select-thumb"
                alt="商品图片"
              />
              <div v-else class="order-select-thumb placeholder">商品</div>
            </div>
            <div class="order-select-text">
              <div class="order-select-no">{{ row.orderNo }}</div>
              <div class="order-select-name">{{ row.firstProductName || '订单商品' }}</div>
              <div class="order-select-sub">
                {{ formatTime(row.createTime) }}
              </div>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="金额" width="120" align="right">
        <template #default="{ row }">
          ¥{{ Number(row.payAmount || 0).toFixed(2) }}
        </template>
      </el-table-column>
      <el-table-column label="状态" width="120">
        <template #default="{ row }">
          {{ getOrderStatusText(row.orderStatus) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="130" align="center">
        <template #default="{ row }">
          <el-button type="primary" size="small" @click="sendOrderCard(row)">
            发送订单卡片
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <template #footer>
      <el-button @click="orderDialogVisible = false">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from '@/utils/api'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()

const PRODUCT_CARD_PREFIX = '__PRODUCT_CARD__'
const ORDER_CARD_PREFIX = '__ORDER_CARD__'

const merchant = ref(null)
const allMessages = ref([])
// 模板里使用 messages.length，这里提供一个兼容的只读别名，避免 undefined.length 直接导致页面崩溃
const messages = computed(() => allMessages.value || [])
const loading = ref(false)
const sending = ref(false)
const content = ref('')
const chatBodyRef = ref(null)
let timer = null
const noticeExpanded = ref(false)
const visibleCount = ref(30)

const quickReplies = ref(['多少钱一斤？', '什么时候发货？', '今天有优惠吗？', '能发实拍图吗？'])
const emojis = ref(['🍓', '🍌', '🍎', '🍇', '🍊', '🥝', '🍉', '😀', '😊', '👍'])

const userStore = useUserStore()
const userAvatar = computed(() => userStore.user?.avatar || '')
const userDisplayName = computed(() => {
  const u = userStore.user
  return u?.nickname || u?.username || ''
})
const merchantAvatar = computed(() => merchant.value?.avatar || '')

// 订单卡片选择相关
const orderDialogVisible = ref(false)
const orderLoading = ref(false)
const orderList = ref([])

const uploadHeaders = computed(() => {
  const token = localStorage.getItem('token')
  return token ? { Authorization: `Bearer ${token}` } : {}
})

const MAX_IMAGE_SIZE_MB = 5
const beforeChatImageUpload = (file) => {
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

const visibleMessages = computed(() => {
  const list = allMessages.value || []
  if (list.length <= visibleCount.value) return list
  return list.slice(list.length - visibleCount.value)
})

const scrollToBottom = () => {
  nextTick(() => {
    const el = chatBodyRef.value
    if (el) {
      el.scrollTo({ top: el.scrollHeight, behavior: 'smooth' })
    }
  })
}

const getImageUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http://') || url.startsWith('https://')) return url
  if (url.startsWith('/api/uploads/')) return url
  if (url.startsWith('/uploads/')) return '/api' + url
  if (!url.startsWith('/')) return '/api/uploads/' + url
  return url
}

const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN')
}

const formatContent = (text) => {
  if (!text) return ''
  return String(text).replace(/\n/g, '<br/>')
}

const getOrderCard = (msg) => {
  if (!msg || !msg.content || typeof msg.content !== 'string') return null
  if (!msg.content.startsWith(ORDER_CARD_PREFIX)) return null
  try {
    const json = msg.content.slice(ORDER_CARD_PREFIX.length)
    const data = JSON.parse(json)
    if (!data || !data.id) return null
    return {
      id: data.id,
      orderNo: data.orderNo,
      payAmount: data.payAmount,
      status: data.status,
      createTime: data.createTime,
      image: data.image,
      name: data.name
    }
  } catch {
    return null
  }
}

const getProductCard = (msg) => {
  if (!msg || !msg.content || typeof msg.content !== 'string') return null
  if (!msg.content.startsWith(PRODUCT_CARD_PREFIX)) return null
  try {
    const json = msg.content.slice(PRODUCT_CARD_PREFIX.length)
    const data = JSON.parse(json)
    if (!data || !data.id) return null
    return {
      id: data.id,
      name: data.name,
      price: data.price,
      image: data.image
    }
  } catch (e) {
    return null
  }
}

const loadMerchant = async () => {
  try {
    const id = route.params.merchantId
    const res = await api.get(`/merchant/detail/${id}`)
    if (res.code === 200) {
      merchant.value = res.data
    }
  } catch (e) {
    console.error('加载商家信息失败', e)
  }
}

const loadMessages = async () => {
  loading.value = true
  try {
    const merchantId = route.params.merchantId
    const res = await api.get('/chat/history', {
      params: { merchantId }
    })
    if (res.code === 200) {
      allMessages.value = (res.data || []).map(decorateMessage)
      scrollToBottom()
    }
  } catch (e) {
    console.error('加载聊天记录失败', e)
  } finally {
    loading.value = false
  }
}

const decorateMessage = (m) => {
  const base = m || {}
  return {
    ...base,
    _key: base.id != null ? base.id : `${Date.now()}_${Math.random()}`,
    _justSent: false
  }
}

const sendMessage = async (text, imageUrl) => {
  if (sending.value) return
  if ((!text || !text.trim()) && !imageUrl) {
    ElMessage.warning('请输入内容或选择图片')
    return
  }
  sending.value = true
  try {
    const merchantId = route.params.merchantId
    const res = await api.post('/chat/send', {
      merchantId,
      content: text || '',
      imageUrl: imageUrl || ''
    })
    if (res.code === 200) {
      const msg = decorateMessage(res.data)
      msg._justSent = true
      allMessages.value.push(msg)
      content.value = ''
      visibleCount.value = Math.max(visibleCount.value, 30)
      scrollToBottom()
      setTimeout(() => {
        msg._justSent = false
      }, 450)
    } else {
      ElMessage.error(res.message || '发送失败')
    }
  } catch (e) {
    console.error('发送消息失败', e)
    ElMessage.error('发送失败')
  } finally {
    sending.value = false
  }
}

const sendText = () => {
  sendMessage(content.value, null)
}

const handleEnter = () => {
  // 单独回车发送，Shift+Enter 换行
  sendText()
}

const handleUploadSuccess = (res) => {
  if (res.code === 200 && res.data?.url) {
    sendMessage('', res.data.url)
  } else {
    ElMessage.error(res.message || '图片上传失败')
  }
}

const handleUploadError = () => {
  ElMessage.error('图片上传失败')
}

const openOrderDialog = async () => {
  orderDialogVisible.value = true
  if (orderList.value.length > 0) return
  orderLoading.value = true
  try {
    const res = await api.get('/order/list')
    if (res.code === 200) {
      orderList.value = res.data || []
    }
  } catch (e) {
    console.error('加载订单列表失败', e)
    ElMessage.error('加载订单失败')
  } finally {
    orderLoading.value = false
  }
}

const sendOrderCard = (order) => {
  if (!order || !order.id) return
  const payload = {
    id: order.id,
    orderNo: order.orderNo,
    payAmount: order.payAmount,
    status: order.orderStatus,
    createTime: order.createTime,
    image: order.firstProductImage,
    name: order.firstProductName
  }
  const content = ORDER_CARD_PREFIX + JSON.stringify(payload)
  sendMessage(content, null)
  orderDialogVisible.value = false
}

const loadMore = () => {
  visibleCount.value += 30
}

const appendQuickReply = (t) => {
  if (!t) return
  content.value = (content.value ? content.value + '\n' : '') + t
}

const insertEmoji = (e) => {
  if (!e) return
  content.value = (content.value || '') + e
}

const copyText = async (text) => {
  if (!text) return
  try {
    await navigator.clipboard.writeText(String(text))
    ElMessage.success('已复制')
  } catch (e) {
    // fallback
    try {
      const ta = document.createElement('textarea')
      ta.value = String(text)
      ta.style.position = 'fixed'
      ta.style.left = '-9999px'
      document.body.appendChild(ta)
      ta.select()
      document.execCommand('copy')
      document.body.removeChild(ta)
      ElMessage.success('已复制')
    } catch (err) {
      ElMessage.error('复制失败')
    }
  }
}

const toggleNotice = () => {
  noticeExpanded.value = !noticeExpanded.value
}

const goProductDetail = (id) => {
  if (!id) return
  router.push({ name: 'ProductDetail', params: { id } })
}

const goOrderDetail = (id) => {
  if (!id) return
  router.push({ name: 'Order', query: { orderId: id } })
}

const getOrderStatusText = (status) => {
  const map = {
    0: '待支付',
    1: '待发货',
    2: '待收货',
    3: '待评价',
    4: '已完成',
    5: '已取消'
  }
  return map[status] || '订单'
}

const goHistory = () => {
  router.push({ path: '/messages', query: { tab: 'chat' } })
}

const goContact = () => {
  router.push('/contact')
}

onMounted(() => {
  loadMerchant()
  loadMessages()
  timer = setInterval(loadMessages, 5000)
})

onUnmounted(() => {
  if (timer) {
    clearInterval(timer)
  }
})
</script>

<style scoped>
.chat-page {
  max-width: 900px;
  margin: 0 auto;
  padding: 16px 24px 24px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  display: flex;
  flex-direction: column;
  min-height: 600px;
}

.notice-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  background: #f3f4f6;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 10px 12px;
  cursor: pointer;
  user-select: none;
}

.notice-left {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}

.notice-icon {
  color: #1677ff;
  font-size: 16px;
}

.notice-title {
  font-size: 13px;
  color: #374151;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.notice-right {
  flex: 0 0 auto;
  font-size: 12px;
  color: #1677ff;
}

.notice-detail {
  margin-top: 10px;
  background: #fafafa;
  border: 1px solid #eeeeee;
  border-radius: 12px;
  padding: 10px 12px;
}

.notice-detail-text {
  font-size: 12px;
  color: #6b7280;
  line-height: 1.7;
}

.chat-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
  margin-top: 12px;
}

.chat-header-left {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.chat-header-right {
  display: flex;
  gap: 6px;
  flex: 0 0 auto;
}

.chat-header h2 {
  margin: 0;
  font-size: 20px;
}

.sub-text {
  font-size: 12px;
  color: #8c8c8c;
}

.chat-body {
  flex: 1;
  overflow-y: auto;
  padding: 12px 8px;
  background: #fafafa;
  border-radius: 12px;
  margin-bottom: 12px;
  scroll-behavior: smooth;
}

.load-more {
  display: flex;
  justify-content: center;
  padding: 4px 0 10px;
}

.message-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.message-row {
  display: flex;
  align-items: flex-start;
  gap: 8px;
}

.message-row.from-user {
  justify-content: flex-end;
}

.message-row.from-merchant {
  justify-content: flex-start;
}

.avatar-wrapper {
  width: 32px;
  height: 32px;
  flex-shrink: 0;
  display: flex;
  align-items: flex-start;
}

.avatar-right {
  justify-content: flex-end;
}

.avatar-circle {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  overflow: hidden;
  background: #e5e7eb;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  color: #4b5563;
}

.avatar-circle img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-circle.placeholder {
  font-weight: 600;
}

.bubble {
  max-width: 70%;
  padding: 8px 10px;
  border-radius: 14px;
  background: #fff;
  position: relative;
  font-size: 14px;
  line-height: 1.6;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.08);
  transition: transform 0.18s ease, box-shadow 0.18s ease;
}

.bubble:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.08);
}

.from-user .bubble {
  background: #2ecc71;
  color: #fff;
}

.from-merchant .bubble {
  background: #f0f0f0;
  color: #262626;
}

.bubble-actions {
  position: absolute;
  top: -28px;
  right: 0;
  opacity: 0;
  transition: opacity 0.15s ease;
}

.message-row:hover .bubble-actions {
  opacity: 1;
}

.action-btn {
  font-size: 12px;
  padding: 0 6px;
}

.bubble .image img {
  max-width: 220px;
  border-radius: 6px;
  display: block;
}

.bubble .time {
  font-size: 11px;
  color: #bfbfbf;
  margin-top: 4px;
  text-align: right;
}

.from-user .bubble .time {
  color: rgba(255, 255, 255, 0.8);
}

.chat-input {
  border-top: 1px solid #e5e5e5;
  padding-top: 8px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.quick-replies {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 2px;
}

.chip {
  border-radius: 999px;
  border: 1px solid #e5e7eb;
  background: rgba(46, 204, 113, 0.08);
  color: #1f2937;
}

.chip:hover {
  border-color: #2ecc71;
  color: #1b5e20;
}

.input-card {
  display: flex;
  gap: 10px;
  border: 1px solid #e5e7eb;
  border-radius: 16px;
  padding: 10px;
  background: #ffffff;
}

.input-left {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding-top: 2px;
}

.icon-btn {
  width: 38px;
  height: 38px;
  border-radius: 12px;
  border: 1px solid #d1d5db;
  background: transparent;
  font-size: 16px;
}

.icon-btn:hover {
  border-color: #2ecc71;
  color: #2ecc71;
}

.textarea :deep(textarea) {
  resize: none;
}

.textarea {
  flex: 1;
}

.input-right {
  display: flex;
  align-items: flex-end;
}

.send-btn {
  border-radius: 12px;
  background: #2ecc71;
  border-color: #2ecc71;
  transition: transform 0.12s ease, filter 0.12s ease;
}

.send-btn:hover {
  filter: brightness(0.95);
  transform: scale(1.02);
}

.sending-hint {
  font-size: 12px;
  color: #9ca3af;
  padding-left: 8px;
}

.loading {
  padding: 16px;
}

.empty {
  padding: 40px 0;
}

.emoji-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 8px;
}

.emoji {
  border: 1px solid #e5e7eb;
  background: #fff;
  border-radius: 10px;
  padding: 6px 0;
  cursor: pointer;
  font-size: 16px;
}

.emoji:hover {
  border-color: #2ecc71;
  background: rgba(46, 204, 113, 0.08);
}

.product-card {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px;
  background: #ffffff;
  border-radius: 10px;
  cursor: pointer;
}

.from-user .product-card {
  background: rgba(255, 255, 255, 0.15);
}

.product-card:hover {
  opacity: 0.96;
}

.product-thumb-wrap {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  overflow: hidden;
  background: #f5f5f5;
  flex-shrink: 0;
}

.product-thumb-wrap img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-thumb-wrap.order {
  display: flex;
  align-items: center;
  justify-content: center;
  background: #e5f6ff;
}

.order-icon {
  font-size: 20px;
}

.order-select-main {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.order-select-no {
  font-size: 14px;
  font-weight: 500;
}

.order-select-sub {
  font-size: 12px;
  color: #9ca3af;
}

.product-thumb-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: #999;
}

/* 选择订单弹窗里的商品缩略图，限制为小图防止撑大弹窗 */
.order-select-thumb-wrap {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  overflow: hidden;
  background: #f5f5f5;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.order-select-thumb {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.order-select-thumb.placeholder {
  font-size: 12px;
  color: #999;
}

.product-info-wrap {
  flex: 1;
  min-width: 0;
}

.product-title {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
}

.product-price {
  color: #ff4d4f;
  font-weight: 600;
}

.product-action {
  color: #1677ff;
}

.just-sent .bubble {
  animation: popIn 0.22s ease-out;
}

@keyframes popIn {
  from { transform: translateY(6px); opacity: 0.6; }
  to { transform: translateY(0); opacity: 1; }
}
</style>



