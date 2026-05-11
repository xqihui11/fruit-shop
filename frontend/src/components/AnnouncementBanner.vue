<template>
  <div class="announcement-banner">
    <el-carousel height="60px" :interval="5000" indicator-position="none" arrow="hover">
      <el-carousel-item v-for="announcement in announcements" :key="announcement.id">
        <div class="announcement-item" @click="goToAnnouncements">
          <el-icon class="announcement-icon"><Bell /></el-icon>
          <span class="announcement-title">{{ announcement.title }}</span>
          <el-link 
            type="primary" 
            :underline="false" 
            class="announcement-link"
            @click.stop="showDetail(announcement)"
          >
            查看详情 →
          </el-link>
        </div>
      </el-carousel-item>
    </el-carousel>

    <!-- 公告详情对话框 -->
    <el-dialog 
      v-model="detailVisible" 
      :title="currentAnnouncement?.title" 
      width="700px"
      class="announcement-dialog"
    >
      <div class="announcement-detail">
        <div class="announcement-meta">
          <el-tag :type="getTypeTagType(currentAnnouncement?.type)" size="small">
            {{ getTypeText(currentAnnouncement?.type) }}
          </el-tag>
          <span class="announcement-time">
            {{ formatTime(currentAnnouncement?.publishTime) }}
          </span>
        </div>
        <div class="announcement-content" v-html="formatContent(currentAnnouncement?.content)"></div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { Bell } from '@element-plus/icons-vue'

const props = defineProps({
  announcements: {
    type: Array,
    default: () => []
  }
})

const router = useRouter()
const detailVisible = ref(false)
const currentAnnouncement = ref(null)

const getTypeText = (type) => {
  const map = { 1: '系统公告', 2: '活动公告', 3: '维护公告' }
  return map[type] || '公告'
}

const getTypeTagType = (type) => {
  const map = { 1: 'info', 2: 'success', 3: 'warning' }
  return map[type] || ''
}

const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN')
}

const formatContent = (content) => {
  if (!content) return ''
  return content.replace(/\n/g, '<br>')
}

const goToAnnouncements = () => {
  router.push('/announcements')
}

const showDetail = (announcement) => {
  currentAnnouncement.value = announcement
  detailVisible.value = true
}
</script>

<style scoped>
.announcement-banner {
  background: #ffffff;
  color: #333;
  margin-bottom: 20px;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #e8e8e8;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.announcement-item {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 24px;
  height: 60px;
  gap: 12px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.announcement-item:hover {
  background-color: #f5f5f5;
}

.announcement-icon {
  font-size: 20px;
  color: #1890ff;
}

.announcement-title {
  flex: 1;
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.announcement-link {
  color: #1890ff;
  font-size: 13px;
  font-weight: 500;
}

.announcement-detail {
  padding: 20px 0;
}

.announcement-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e8e8e8;
}

.announcement-time {
  color: #999;
  font-size: 13px;
}

.announcement-content {
  line-height: 2;
  color: #333;
  white-space: pre-wrap;
  font-size: 15px;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', 'Helvetica Neue', Helvetica, Arial, sans-serif;
  letter-spacing: 0.3px;
}

.announcement-content :deep(p) {
  margin: 0 0 12px 0;
  line-height: 2;
}

.announcement-content :deep(ol),
.announcement-content :deep(ul) {
  margin: 12px 0;
  padding-left: 24px;
}

.announcement-content :deep(li) {
  margin-bottom: 8px;
  line-height: 2;
}

:deep(.announcement-dialog .el-dialog__header) {
  padding: 20px 24px 16px;
  border-bottom: 1px solid #f0f0f0;
}

:deep(.announcement-dialog .el-dialog__title) {
  font-size: 18px;
  font-weight: 600;
  color: #262626;
  letter-spacing: 0.5px;
}

:deep(.announcement-dialog .el-dialog__body) {
  padding: 24px;
}
</style>

