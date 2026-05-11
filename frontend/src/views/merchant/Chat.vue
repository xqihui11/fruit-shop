<template>
  <div class="merchant-chat-page">
    <div class="sidebar">
      <div class="sidebar-header">
        <h3>会话列表</h3>
      </div>
      <div class="conversation-list">
        <div class="conversation-search">
          <el-input
            v-model="searchKeyword"
            size="small"
            placeholder="搜索用户昵称或ID"
            clearable
          />
        </div>
        <!-- 置顶的管理员会话 -->
        <div
          class="conversation-item admin-conversation"
          :class="{ active: activeConversationId === 'admin' }"
          @click="selectAdminConversation"
        >
          <div class="conversation-avatar">
            <div class="avatar-circle admin-avatar">
              管
            </div>
          </div>
          <div class="conversation-main">
            <div class="title">
              <span>平台管理员</span>
            </div>
            <div class="preview">
              <span>{{ adminLastMessage || '联系平台获取帮助' }}</span>
            </div>
            <div class="meta">
              <span class="time">{{ adminLastTime }}</span>
              <el-badge
                v-if="adminUnreadCount > 0"
                :value="adminUnreadCount"
                class="badge"
              />
            </div>
          </div>
        </div>

        <div
          v-for="c in filteredConversations"
          :key="c.id"
          class="conversation-item"
          :class="{ active: c.id === activeConversationId }"
          @click="selectConversation(c)"
        >
          <div class="conversation-avatar">
            <div
              v-if="getUserAvatar(c.userId)"
              class="avatar-circle"
            >
              <img :src="getAvatarUrl(getUserAvatar(c.userId))" alt="用户头像" />
            </div>
            <div
              v-else
              class="avatar-circle placeholder"
            >
              {{ getUserInitial(c) }}
            </div>
          </div>
          <div class="conversation-main">
            <div class="title">
              <span>{{ getUserLabel(c) }}</span>
            </div>
            <div class="preview">
              <span>{{ c.lastMessage || '[图片]' }}</span>
            </div>
            <div class="meta">
              <span class="time">{{ formatTime(c.lastTime) }}</span>
              <el-badge
                v-if="c.merchantUnreadCount && c.merchantUnreadCount > 0"
                :value="c.merchantUnreadCount"
                class="badge"
              />
            </div>
          </div>
        </div>
        <div v-if="!loading && conversations.length === 0" class="empty">
          <el-empty description="暂无会话" :image-size="80" />
        </div>
      </div>
    </div>

    <div class="chat-main">
      <div class="chat-header">
        <h3>{{ activeUserLabel || '选择左侧一个会话开始聊天' }}</h3>
      </div>
      <div class="chat-body" ref="chatBodyRef">
        <div v-if="loadingMessages" class="loading">
          <el-skeleton :rows="6" animated />
        </div>
        <div v-else-if="messages.length === 0 && activeConversationId" class="empty">
          <el-empty description="暂无聊天记录" :image-size="80" />
        </div>
        <div v-else class="message-list">
          <div
            v-for="msg in messages"
            :key="msg.id"
            class="message-row"
            :class="msg.senderType === 'MERCHANT' ? 'from-merchant' : 'from-user'"
          >
            <!-- 用户头像在左侧 -->
            <div v-if="msg.senderType === 'USER'" class="avatar-wrapper">
              <div v-if="activeUserAvatar" class="avatar-circle">
                <img :src="getAvatarUrl(activeUserAvatar)" alt="用户头像" />
              </div>
              <div v-else class="avatar-circle placeholder">
                {{ activeUserInitial }}
              </div>
            </div>

            <div class="bubble">
              <div class="bubble-actions">
                <el-button
                  text
                  class="action-btn"
                  @click.stop="copyText(msg.content)"
                  v-if="msg.content"
                >
                  复制
                </el-button>
              </div>
              <!-- 先展示用户发来的订单卡片：带商品缩略图 -->
              <div
                v-if="getOrderCard(msg)"
                class="product-card"
                @click="copyText(getOrderCard(msg).orderNo)"
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
                    <span class="product-action">点击复制订单号</span>
                  </div>
                </div>
              </div>
              <!-- 其次是商品卡片 -->
              <div
                v-else-if="getProductCard(msg)"
                class="product-card"
                @click="openProductFromMerchant(getProductCard(msg))"
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
                    <span class="product-action">查看商品</span>
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

            <!-- 商家头像在右侧 -->
            <div
              v-if="msg.senderType === 'MERCHANT'"
              class="avatar-wrapper avatar-right"
            >
              <div v-if="getMerchantAvatar()" class="avatar-circle">
                <img :src="getAvatarUrl(getMerchantAvatar())" alt="商家头像" />
              </div>
              <div v-else class="avatar-circle placeholder">
                {{ (merchantName || '商家').slice(0, 1) }}
              </div>
            </div>
          </div>
        </div>
      </div>
      <div class="chat-input" v-if="activeConversationId">
        <div class="toolbar">
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
          <!-- 发送商品卡片 -->
          <el-button class="icon-btn" plain title="发送商品卡片" @click="openProductDialog">
            🛒
          </el-button>
        </div>
        <el-input
          v-model="content"
          type="textarea"
          :autosize="{ minRows: 3, maxRows: 6 }"
          placeholder="请输入要发送的内容..."
          @keydown.enter.exact.prevent="handleEnter"
          @keydown.enter.shift.exact.stop
        />
        <div class="actions">
          <el-button type="primary" :loading="sending" @click="sendText">
            发送
          </el-button>
        </div>
      </div>
    </div>
  </div>

  <!-- 选择商品并发送商品卡片 -->
  <el-dialog
    v-model="productDialogVisible"
    title="选择要推荐的商品"
    width="640px"
  >
    <el-table
      v-loading="productLoading"
      :data="productList"
      height="360"
    >
      <el-table-column label="商品" min-width="220">
        <template #default="{ row }">
          <div class="product-select-row">
            <img
              v-if="row.mainImage"
              :src="getImageUrl(row.mainImage)"
              class="product-select-thumb"
              alt="商品图片"
            />
            <div v-else class="product-select-thumb placeholder">
              商品
            </div>
            <div class="product-select-info">
              <div class="product-select-name">
                {{ row.name }}
              </div>
              <div class="product-select-sub">
                库存：{{ row.stock }}　销量：{{ row.salesCount || 0 }}
              </div>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="价格" width="120" align="right">
        <template #default="{ row }">
          ¥{{ Number(row.price).toFixed(2) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120" align="center">
        <template #default="{ row }">
          <el-button type="primary" size="small" @click="sendProductCard(row)">
            发送卡片
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <template #footer>
      <el-button @click="productDialogVisible = false">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from '@/utils/api'

const router = useRouter()

const PRODUCT_CARD_PREFIX = '__PRODUCT_CARD__'
const ORDER_CARD_PREFIX = '__ORDER_CARD__'
// 与后端约定：userId = 0 代表“平台管理员”的虚拟用户
const ADMIN_USER_ID = 0

const conversations = ref([])
const loading = ref(false)
const messages = ref([])
const loadingMessages = ref(false)
const activeConversationId = ref(null)
const activeUserId = ref(null)
const content = ref('')
const sending = ref(false)
const chatBodyRef = ref(null)
let timer = null
const emojis = ref(['🍓', '🍌', '🍎', '🍇', '🍊', '🥝', '🍉', '😀', '😊', '👍'])
const userAvatarMap = ref({})
const userNameMap = ref({})
const merchantAvatar = ref('')
const merchantName = ref('')
const searchKeyword = ref('')

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

const filteredConversations = computed(() => {
  if (!searchKeyword.value) return conversations.value
  const kw = searchKeyword.value.trim().toLowerCase()
  return conversations.value.filter(c => {
    const name = getUserLabel(c).toLowerCase()
    const idStr = String(c.userId || '')
    return name.includes(kw) || idStr.includes(kw)
  })
})

// 商品选择相关
const productDialogVisible = ref(false)
const productLoading = ref(false)
const productList = ref([])

const uploadHeaders = computed(() => {
  const token = localStorage.getItem('merchant_token')
  return token ? { Authorization: `Bearer ${token}` } : {}
})

const activeUserAvatar = computed(() => {
  // 只有在真正选中了某个用户会话（非 null/undefined）时才返回头像；
  // userId=0（平台管理员虚拟用户）也视为有效 ID
  if (activeUserId.value === null || activeUserId.value === undefined) return ''
  return userAvatarMap.value[activeUserId.value] || ''
})

const activeUserInitial = computed(() => {
  const c = conversations.value.find((item) => item.id === activeConversationId.value)
  if (c) {
    return getUserInitial(c)
  }
  return '访'
})

const activeUserLabel = computed(() => {
  if (activeConversationId.value === 'admin') {
    return '与平台管理员聊天'
  }
  if (!activeUserId.value) return ''
  return `用户 ID：${activeUserId.value}`
})

// 单独存一份管理员会话，避免在普通会话列表里重复显示
const adminConversation = ref(null)

const adminUnreadCount = computed(() => adminConversation.value?.merchantUnreadCount || 0)

const adminLastMessage = computed(() => adminConversation.value?.lastMessage || '')

const adminLastTime = computed(() => {
  if (!adminConversation.value?.lastTime) return ''
  return formatTime(adminConversation.value.lastTime)
})

const getUserLabel = (c) => {
  if (!c || !c.userId) return '未知用户'
  const name = userNameMap.value[c.userId]
  if (name) {
    return name
  }
  return `用户 ID：${c.userId}`
}

const getUserInitial = (c) => {
  if (!c || !c.userId) return '访'
  const name = userNameMap.value[c.userId]
  if (name) {
    return String(name).slice(0, 1)
  }
  return String(c.userId).slice(-1)
}

const getUserAvatar = (userId) => {
  if (!userId) return ''
  return userAvatarMap.value[userId] || ''
}

const getAvatarUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http://') || url.startsWith('https://')) return url
  if (url.startsWith('/api/uploads/')) return url
  if (url.startsWith('/uploads/')) return '/api' + url
  if (!url.startsWith('/')) return '/api/uploads/' + url
  return url
}

const getMerchantAvatar = () => {
  return merchantAvatar.value || ''
}

const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN')
}

const formatContent = (text) => {
  if (!text) return ''
  return String(text).replace(/\n/g, '<br/>')
}

const getImageUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http://') || url.startsWith('https://')) return url
  if (url.startsWith('/api/uploads/')) return url
  if (url.startsWith('/uploads/')) return '/api' + url
  if (!url.startsWith('/')) return '/api/uploads/' + url
  return url
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

const openProductFromMerchant = (card) => {
  if (!card || !card.id) return
  // 商家端点击商品卡片：跳转到商品管理，并自动打开对应商品的编辑弹窗
  router.push({
    name: 'MerchantProducts',
    query: { productId: card.id }
  })
}

const scrollToBottom = () => {
  nextTick(() => {
    const el = chatBodyRef.value
    if (el) {
      el.scrollTo({ top: el.scrollHeight, behavior: 'smooth' })
    }
  })
}

const loadConversations = async () => {
  loading.value = true
  try {
    const res = await api.get('/chat/merchant/conversations')
    if (res.code === 200) {
      const list = res.data || []
      // 把管理员这条会话(userId === 0)单独拿出来，其他会话展示在列表中
      const adminConv = list.find(c => Number(c.userId) === ADMIN_USER_ID)
      adminConversation.value = adminConv || null
      conversations.value = list.filter(c => Number(c.userId) !== ADMIN_USER_ID)
      // 收集所有用户ID，批量拉取基础信息（昵称+头像）
      const userIds = Array.from(
        new Set(
          list
            .map((c) => c.userId)
            .filter((id) => id != null)
        )
      )
      if (userIds.length > 0) {
        try {
          const infoRes = await api.post('/user/basic-info/batch', { userIds })
          if (infoRes.code === 200 && Array.isArray(infoRes.data)) {
            infoRes.data.forEach((u) => {
              userAvatarMap.value[u.id] = u.avatar || ''
              userNameMap.value[u.id] = u.nickname || u.username || `用户 ${u.id}`
            })
          }
        } catch (e) {
          console.error('加载用户基本信息失败', e)
        }
      }
    }
  } catch (e) {
    console.error('加载会话失败', e)
  } finally {
    loading.value = false
  }
}

const loadMessages = async () => {
  if (activeUserId.value === null || activeUserId.value === undefined) return
  loadingMessages.value = true
  try {
    const res = await api.get('/chat/merchant/history', {
      params: { userId: activeUserId.value }
    })
    if (res.code === 200) {
      messages.value = res.data || []
      scrollToBottom()
    }
  } catch (e) {
    console.error('加载聊天记录失败', e)
  } finally {
    loadingMessages.value = false
  }
}

const selectConversation = (c) => {
  activeConversationId.value = c.id
  activeUserId.value = c.userId
  loadMessages()
}

// 选择管理员会话（置顶项）
const selectAdminConversation = () => {
  activeConversationId.value = 'admin'
  activeUserId.value = ADMIN_USER_ID
  loadMessages()
}

const loadMerchantInfoFromStorage = () => {
  const info = localStorage.getItem('merchant_info')
  if (!info) return
  try {
    const parsed = JSON.parse(info)
    merchantAvatar.value = parsed.avatar || ''
    merchantName.value = parsed.shopName || parsed.username || '商家'
  } catch (e) {
    console.error('解析商家信息失败', e)
  }
}

const openProductDialog = async () => {
  productDialogVisible.value = true
  if (productList.value.length > 0) return
  productLoading.value = true
  try {
    const res = await api.get('/merchant/product/list')
    if (res.code === 200) {
      // 只展示已上架商品
      productList.value = (res.data || []).filter((p) => p.status === 1)
    }
  } catch (e) {
    console.error('加载商品列表失败', e)
    ElMessage.error('加载商品列表失败')
  } finally {
    productLoading.value = false
  }
}

const sendProductCard = (product) => {
  if (!product || !product.id) return
  const payload = {
    id: product.id,
    name: product.name,
    price: product.price,
    image: product.mainImage
  }
  const content = PRODUCT_CARD_PREFIX + JSON.stringify(payload)
  sendMessage(content, null)
  productDialogVisible.value = false
}

const sendMessage = async (text, imageUrl) => {
  // 这里只限制“没有选中任何会话”的情况，userId=0（管理员）也允许发送
  if (activeUserId.value === null || activeUserId.value === undefined) return
  if (sending.value) return
  if ((!text || !text.trim()) && !imageUrl) {
    ElMessage.warning('请输入内容或选择图片')
    return
  }
  sending.value = true
  try {
    const res = await api.post('/chat/merchant/send', {
      userId: activeUserId.value,
      content: text || '',
      imageUrl: imageUrl || ''
    })
    if (res.code === 200) {
      messages.value.push(res.data)
      content.value = ''
      scrollToBottom()
      // 发送后刷新会话列表以更新未读数和最后一条内容
      loadConversations()
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

onMounted(() => {
  loadMerchantInfoFromStorage()
  loadConversations()
  timer = setInterval(() => {
    loadConversations()
    if (activeUserId.value) {
      loadMessages()
    }
  }, 7000)
})

onUnmounted(() => {
  if (timer) {
    clearInterval(timer)
  }
})
</script>

<style scoped>
.merchant-chat-page {
  display: flex;
  height: 100%;
}

.sidebar {
  width: 260px;
  border-right: 1px solid #e5e5e5;
  background: #ffffff;
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
}

.sidebar-header h3 {
  margin: 0;
  font-size: 16px;
}

.conversation-list {
  flex: 1;
  overflow-y: auto;
}

.conversation-item {
  padding: 10px 12px;
  border-bottom: 1px solid #f5f5f5;
  cursor: pointer;
  display: flex;
  gap: 8px;
  align-items: center;
}

.conversation-item:hover {
  background: #f5f7fa;
}

.conversation-item.active {
  background: #e6f7ff;
}

.conversation-avatar {
  flex-shrink: 0;
}

.conversation-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.conversation-item .title {
  font-size: 14px;
  font-weight: 500;
}

.conversation-item .preview {
  font-size: 12px;
  color: #8c8c8c;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.conversation-item .meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 11px;
  color: #bfbfbf;
}

.conversation-item .badge {
  margin-left: 4px;
}

.avatar-circle {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #e5e7eb;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  color: #4b5563;
  font-weight: 600;
}

.avatar-circle img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-circle.placeholder {
  background: #e5e7eb;
}

.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
}

.chat-header {
  padding: 10px 16px;
  border-bottom: 1px solid #e5e5e5;
  background: #ffffff;
}

.chat-header h3 {
  margin: 0;
  font-size: 16px;
}

.chat-body {
  flex: 1;
  padding: 10px 12px;
  overflow-y: auto;
  scroll-behavior: smooth;
}

.message-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.message-row {
  display: flex;
  align-items: flex-start;
}

.message-row.from-merchant {
  justify-content: flex-end;
}

.message-row.from-user {
  justify-content: flex-start;
}

.bubble {
  max-width: 70%;
  padding: 8px 10px;
  border-radius: 14px;
  background: #fff;
  font-size: 14px;
  line-height: 1.6;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.08);
  transition: transform 0.18s ease, box-shadow 0.18s ease;
}

.bubble:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.08);
}

.from-merchant .bubble {
  background: #2ecc71;
  color: #fff;
}

.from-user .bubble {
  background: #f0f0f0;
  color: #262626;
}

.avatar-wrapper {
  width: 32px;
  height: 32px;
  flex-shrink: 0;
  display: flex;
  align-items: flex-start;
  justify-content: center;
}

.avatar-right {
  justify-content: flex-end;
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
  margin-top: 4px;
  font-size: 11px;
  text-align: right;
  color: #bfbfbf;
}

.from-merchant .bubble .time {
  color: rgba(255, 255, 255, 0.85);
}

.chat-input {
  border-top: 1px solid #e5e5e5;
  padding: 8px 12px 12px;
  background: #ffffff;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.toolbar {
  display: flex;
  justify-content: flex-start;
  margin-bottom: 4px;
  gap: 8px;
}

.actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 4px;
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
  border-color: #409eff;
  color: #409eff;
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
  border-color: #409eff;
  background: rgba(64, 158, 255, 0.08);
}

.product-select-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.product-select-thumb {
  width: 40px;
  height: 40px;
  border-radius: 6px;
  object-fit: cover;
  background: #f5f5f5;
}

.product-select-thumb.placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: #999;
}

.product-select-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.product-select-name {
  font-size: 14px;
  font-weight: 500;
}

.product-select-sub {
  font-size: 12px;
  color: #999;
}

.product-card {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 10px;
  cursor: pointer;
}

.from-merchant .product-card {
  background: rgba(255, 255, 255, 0.15);
}

.product-card:hover {
  opacity: 0.95;
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

.product-thumb-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
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

.product-action::before {
  content: '去查看';
}

.loading {
  padding: 16px;
}

.empty {
  padding: 40px 0;
}
</style>



