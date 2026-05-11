<template>
  <div class="messages-page">
    <div class="page-header">
      <h2>我的消息</h2>
      <el-button v-if="unreadCount > 0 && activeTab !== 'chat'" type="text" @click="markAllAsRead">
        全部标记为已读
      </el-button>
    </div>

    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane label="全部" name="all" />
      <el-tab-pane label="订单发货" name="1" />
      <el-tab-pane label="商家回复" name="2" />
      <el-tab-pane label="用户回复" name="3" />
      <el-tab-pane label="管理员回复" name="4" />
      <el-tab-pane label="聊天" name="chat" />
    </el-tabs>

    <div class="messages-list" v-if="activeTab !== 'chat'">
      <div v-if="loading" class="loading">
        <el-skeleton :rows="5" animated />
      </div>
      <div v-else-if="messages.length === 0" class="empty">
        <el-empty description="暂无消息" />
      </div>
      <div v-else>
        <div
          v-for="message in messages"
          :key="message.id"
          class="message-item"
          :class="{ unread: message.isRead === 0 }"
          @click="handleMessageClick(message)"
        >
          <div class="message-content">
            <div class="message-header">
              <h3>{{ message.title }}</h3>
              <span class="message-time">{{ formatTime(message.createTime) }}</span>
            </div>
            <div class="message-body">{{ message.content }}</div>
          </div>
          <div v-if="message.isRead === 0" class="unread-dot"></div>
        </div>
      </div>
    </div>

    <div class="messages-list" v-else>
      <div v-if="chatLoading" class="loading">
        <el-skeleton :rows="5" animated />
      </div>
      <div v-else-if="conversations.length === 0" class="empty">
        <el-empty description="暂无聊天会话" />
      </div>
      <div v-else>
        <div
          v-for="c in conversations"
          :key="c.id"
          class="chat-conversation-item"
          :class="{ unread: (c.userUnreadCount || 0) > 0 }"
          @click="goChat(c.merchantId)"
        >
          <div class="chat-avatar-wrapper">
            <div v-if="getMerchantAvatar(c.merchantId)" class="chat-avatar">
              <img :src="getImageUrl(getMerchantAvatar(c.merchantId))" alt="商家头像" />
            </div>
            <div v-else class="chat-avatar placeholder">
              {{ (getMerchantName(c.merchantId) || '店铺').slice(0, 1) }}
            </div>
          </div>
          <div class="chat-main">
            <div class="chat-header-row">
              <span class="chat-title">{{ getMerchantName(c.merchantId) }}</span>
              <span class="chat-time">{{ formatTime(c.lastTime) }}</span>
            </div>
            <div class="chat-preview">
              {{ c.lastMessage || '[图片]' }}
            </div>
          </div>
          <div v-if="(c.userUnreadCount || 0) > 0" class="chat-unread-dot"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from '@/utils/api'

const router = useRouter()
const route = useRoute()
const activeTab = ref('all')
const messages = ref([])
const loading = ref(false)
const unreadCount = ref(0)
const conversations = ref([])
const chatLoading = ref(false)
const merchantNameMap = ref({})
const merchantAvatarMap = ref({})

const loadMessages = async () => {
  loading.value = true
  try {
    const type = activeTab.value === 'all' ? null : parseInt(activeTab.value)
    const res = await api.get('/message/list', { params: { type } })
    if (res.code === 200) {
      messages.value = res.data || []
      console.log('加载消息成功，类型:', type, '数量:', messages.value.length)
    } else {
      console.error('加载消息失败:', res.message)
      ElMessage.error(res.message || '加载消息失败')
    }
  } catch (error) {
    console.error('加载消息异常:', error)
    ElMessage.error('加载消息失败')
  } finally {
    loading.value = false
  }
}

const loadChatConversations = async () => {
  chatLoading.value = true
  try {
    const res = await api.get('/chat/conversations')
    if (res.code === 200) {
      const list = res.data || []
      conversations.value = list
      // 预加载店铺名称
      const tasks = list.map(async (c) => {
        const id = c.merchantId
        if (!id || merchantNameMap.value[id]) return
        try {
          const detail = await api.get(`/merchant/detail/${id}`)
          if (detail.code === 200 && detail.data) {
            merchantNameMap.value[id] = detail.data.shopName || `店铺 ${id}`
            merchantAvatarMap.value[id] = detail.data.avatar || ''
          }
        } catch (e) {
          merchantNameMap.value[id] = `店铺 ${id}`
        }
      })
      await Promise.all(tasks)
    }
  } catch (e) {
    console.error('加载聊天会话失败', e)
  } finally {
    chatLoading.value = false
  }
}

const loadUnreadCount = async () => {
  try {
    const res = await api.get('/message/unread-count')
    if (res.code === 200) {
      unreadCount.value = res.data?.count || 0
    }
  } catch (error) {
    console.error('获取未读消息数失败', error)
  }
}

const handleTabChange = () => {
  if (activeTab.value === 'chat') {
    loadChatConversations()
  } else {
  loadMessages()
  }
}

const handleMessageClick = async (message) => {
  // 先跳转，再标记为已读（避免401错误阻止跳转）
  if (message.linkUrl) {
    try {
      // 确保链接以 / 开头
      let targetPath = message.linkUrl
      if (!targetPath.startsWith('/')) {
        targetPath = '/' + targetPath
      }
      
      // 立即跳转
      router.push(targetPath)
    } catch (error) {
      console.error('跳转失败', error)
      ElMessage.warning('无法跳转到目标页面')
    }
  }

  // 标记为已读（异步执行，不阻塞跳转）
  if (message.isRead === 0) {
    // 使用 setTimeout 延迟执行，确保跳转先完成
    setTimeout(async () => {
      try {
        await api.post(`/message/read/${message.id}`)
        message.isRead = 1
        loadUnreadCount()
      } catch (error) {
        // 静默失败，不影响用户体验
        console.error('标记已读失败', error)
      }
    }, 100)
  }
}

const markAllAsRead = async () => {
  try {
    await api.post('/message/read-all')
    ElMessage.success('全部标记为已读')
    loadMessages()
    loadUnreadCount()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const getMerchantName = (merchantId) => {
  if (!merchantId) return '未知店铺'
  return merchantNameMap.value[merchantId] || `店铺 ${merchantId}`
}

const getMerchantAvatar = (merchantId) => {
  if (!merchantId) return ''
  return merchantAvatarMap.value[merchantId] || ''
}

const goChat = (merchantId) => {
  if (!merchantId) return
  router.push(`/chat/${merchantId}`)
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
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)

  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  return date.toLocaleString('zh-CN', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}

onMounted(() => {
  const tab = route.query?.tab
  if (tab === 'chat') {
    activeTab.value = 'chat'
    loadChatConversations()
  } else {
    loadMessages()
  }
  loadUnreadCount()
})
</script>

<style scoped>
.messages-page {
  max-width: 1000px;
  margin: 0 auto;
  padding: 24px;
  background: #fff;
  min-height: 600px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-header h2 {
  margin: 0;
  font-size: 24px;
  color: #262626;
}

.messages-list {
  margin-top: 24px;
}
.message-item {
  display: flex;
  padding: 16px;
  margin-bottom: 12px;
  background: #fafafa;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  position: relative;
}

.message-item:hover {
  background: #f0f0f0;
}

.message-item.unread {
  background: #e6f7ff;
  border-left: 4px solid #1890ff;
}

.message-content {
  flex: 1;
}

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.message-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 500;
  color: #262626;
}

.message-item.unread .message-header h3 {
  font-weight: 600;
}

.message-time {
  font-size: 12px;
  color: #8c8c8c;
}

.message-body {
  font-size: 14px;
  color: #595959;
  line-height: 1.6;
}

.unread-dot {
  width: 8px;
  height: 8px;
  background: #ff4d4f;
  border-radius: 50%;
  margin-left: 12px;
  margin-top: 8px;
}

/* 聊天会话列表样式（类似微信） */
.chat-conversation-item {
  display: flex;
  align-items: center;
  padding: 10px 12px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  background: #ffffff;
  transition: background 0.2s ease;
}

.chat-conversation-item:hover {
  background: #f5f5f5;
}

.chat-conversation-item.unread {
  font-weight: 500;
}

.chat-avatar-wrapper {
  flex-shrink: 0;
  margin-right: 10px;
}

.chat-avatar,
.chat-avatar.placeholder {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  overflow: hidden;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  color: #4b5563;
}

.chat-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.chat-main {
  flex: 1;
  min-width: 0;
}

.chat-header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.chat-title {
  font-size: 15px;
  color: #262626;
  font-weight: 500;
  max-width: 70%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.chat-time {
  font-size: 11px;
  color: #b0b0b0;
  flex-shrink: 0;
  margin-left: 8px;
}

.chat-preview {
  font-size: 13px;
  color: #8c8c8c;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.chat-unread-dot {
  width: 10px;
  height: 10px;
  background: #ff4d4f;
  border-radius: 50%;
  margin-left: 8px;
}

.loading {
  padding: 24px;
}

.empty {
  padding: 60px 0;
}
</style>

