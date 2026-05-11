<template>
  <div class="merchant-chat-page">
    <div class="sidebar">
      <div class="sidebar-header">
        <h3>商家会话</h3>
      </div>
      <div class="conversation-list">
        <div class="conversation-search">
          <el-input
            v-model="searchKeyword"
            size="small"
            placeholder="搜索商家名称或ID"
            clearable
          />
        </div>
        <div
          v-for="c in filteredConversations"
          :key="c.id"
          class="conversation-item"
          :class="{ active: c.id === activeConversationId }"
          @click="selectConversation(c)"
        >
          <div class="conversation-avatar">
            <div v-if="getMerchantAvatar(c.merchantId)" class="avatar-circle">
              <img :src="getImageUrl(getMerchantAvatar(c.merchantId))" alt="商家头像" />
            </div>
            <div v-else class="avatar-circle">
              {{ getMerchantInitial(c.merchantId) }}
            </div>
          </div>
          <div class="conversation-main">
            <div class="title">
              <span>{{ getMerchantLabel(c.merchantId) }}</span>
            </div>
            <div class="preview">
              <span>{{ c.lastMessage || '[图片]' }}</span>
            </div>
            <div class="meta">
              <span class="time">{{ formatTime(c.lastTime) }}</span>
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
        <h3>{{ activeMerchantLabel || '选择左侧一个商家开始聊天' }}</h3>
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
            :class="msg.senderType === 'ADMIN' ? 'from-merchant' : 'from-user'"
          >
            <!-- 商家头像在左侧 -->
            <div v-if="msg.senderType !== 'ADMIN'" class="avatar-wrapper">
              <div class="avatar-circle">
                {{ activeMerchantInitial }}
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
              <div
                v-if="msg.content"
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

            <!-- 管理员头像在右侧 -->
            <div
              v-if="msg.senderType === 'ADMIN'"
              class="avatar-wrapper avatar-right"
            >
              <div class="avatar-circle admin-avatar">
                管
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
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import api from '@/utils/api'

const ADMIN_USER_ID = 0

const conversations = ref([])
const loading = ref(false)
const messages = ref([])
const loadingMessages = ref(false)
const activeConversationId = ref(null)
const activeMerchantId = ref(null)
const content = ref('')
const sending = ref(false)
const chatBodyRef = ref(null)
let timer = null
const emojis = ref(['🍓', '🍌', '🍎', '🍇', '🍊', '🥝', '🍉', '😀', '😊', '👍'])
const merchantNameMap = ref({})
const merchantAvatarMap = ref({})
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
    const name = getMerchantLabel(c.merchantId).toLowerCase()
    const idStr = String(c.merchantId || '')
    return name.includes(kw) || idStr.includes(kw)
  })
})

const uploadHeaders = computed(() => {
  const token = localStorage.getItem('admin_token')
  return token ? { Authorization: `Bearer ${token}` } : {}
})

const activeMerchantLabel = computed(() => {
  if (!activeMerchantId.value) return ''
  return `商家 ID：${activeMerchantId.value}`
})

const activeMerchantInitial = computed(() => {
  if (!activeMerchantId.value) return '商'
  return getMerchantInitial(activeMerchantId.value)
})

const getMerchantLabel = (merchantId) => {
  if (!merchantId) return '未知商家'
  return merchantNameMap.value[merchantId] || `商家 ID：${merchantId}`
}

const getMerchantAvatar = (merchantId) => {
  if (!merchantId) return ''
  return merchantAvatarMap.value[merchantId] || ''
}

const getMerchantInitial = (merchantId) => {
  const name = merchantNameMap.value[merchantId]
  if (name) return String(name).slice(0, 1)
  if (!merchantId) return '商'
  return String(merchantId).slice(-1)
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
    const res = await api.get('/chat/admin/conversations')
    if (res.code === 200) {
      const list = res.data || []
      conversations.value = list
      // 预加载商家名称
      const merchantIds = Array.from(
        new Set(
          list
            .map((c) => c.merchantId)
            .filter((id) => id != null)
        )
      )
      for (const id of merchantIds) {
        if (merchantNameMap.value[id]) continue
        try {
          const detail = await api.get(`/merchant/detail/${id}`)
          if (detail.code === 200 && detail.data) {
            merchantNameMap.value[id] = detail.data.shopName || `商家 ${id}`
            merchantAvatarMap.value[id] = detail.data.avatar || ''
          }
        } catch (e) {
          merchantNameMap.value[id] = `商家 ${id}`
        }
      }
    }
  } catch (e) {
    console.error('加载管理员会话失败', e)
  } finally {
    loading.value = false
  }
}

const loadMessages = async () => {
  if (!activeMerchantId.value) return
  loadingMessages.value = true
  try {
    const res = await api.get('/chat/admin/history', {
      params: { merchantId: activeMerchantId.value }
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
  activeMerchantId.value = c.merchantId
  loadMessages()
}

const sendMessage = async (text, imageUrl) => {
  if (!activeMerchantId.value) return
  if (sending.value) return
  if ((!text || !text.trim()) && !imageUrl) {
    ElMessage.warning('请输入内容或选择图片')
    return
  }
  sending.value = true
  try {
    const res = await api.post('/chat/admin/send', {
      merchantId: activeMerchantId.value,
      content: text || '',
      imageUrl: imageUrl || ''
    })
    if (res.code === 200) {
      messages.value.push(res.data)
      content.value = ''
      scrollToBottom()
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
  loadConversations()
  timer = setInterval(() => {
    loadConversations()
    if (activeMerchantId.value) {
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

.avatar-circle.admin-avatar {
  background: #1890ff;
  color: #fff;
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

.loading {
  padding: 16px;
}

.empty {
  padding: 40px 0;
}
</style>



