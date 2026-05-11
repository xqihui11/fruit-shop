<template>
  <div class="merchant-layout">
    <el-container>
      <el-aside width="200px">
        <div class="logo">
          <h2>商家后台</h2>
        </div>
        <el-menu
          :default-active="$route.path"
          router
          background-color="#001529"
          text-color="#fff"
          active-text-color="#1890ff"
        >
          <el-menu-item index="/merchant">
            <el-icon><DataAnalysis /></el-icon>
            <span>数据概览</span>
          </el-menu-item>
          <el-menu-item index="/merchant/products">
            <el-icon><Goods /></el-icon>
            <span>商品管理</span>
          </el-menu-item>
          <el-menu-item index="/merchant/orders">
            <el-icon><List /></el-icon>
            <span>订单管理</span>
          </el-menu-item>
          <el-menu-item index="/merchant/aftersales">
            <el-icon><Service /></el-icon>
            <span>售后管理</span>
          </el-menu-item>
          <el-menu-item index="/merchant/reviews">
            <el-icon><ChatDotRound /></el-icon>
            <span>用户评价</span>
          </el-menu-item>
          <el-menu-item index="/merchant/chats">
            <el-icon><ChatDotRound /></el-icon>
            <span>私聊消息</span>
          </el-menu-item>
          <el-menu-item index="/merchant/coupons">
            <el-icon><Ticket /></el-icon>
            <span>优惠券管理</span>
          </el-menu-item>
          <el-menu-item index="/merchant/announcements">
            <el-icon><Bell /></el-icon>
            <span>公告中心</span>
          </el-menu-item>
          <el-menu-item index="/merchant/shop">
            <el-icon><Setting /></el-icon>
            <span>店铺设置</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      <el-container>
        <el-header>
          <div class="header-content">
            <span>{{ merchantInfo?.shopName || '商家后台' }}</span>
            <el-dropdown @command="handleCommand">
              <span class="user-dropdown">
                <el-avatar :size="32" :src="getAvatarUrl(merchantInfo?.avatar)" class="merchant-avatar">
                  <span>{{ (merchantInfo?.shopName || merchantInfo?.username || '商')?.charAt(0) }}</span>
                </el-avatar>
                <span class="merchant-name">{{ merchantInfo?.shopName || merchantInfo?.username || '商家' }}</span>
                <el-icon><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>
        <el-main>
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { DataAnalysis, Goods, List, Service, Setting, ArrowDown, ChatDotRound, Ticket, Bell } from '@element-plus/icons-vue'
import api from '@/utils/api'

const router = useRouter()
const merchantInfo = ref(null)

const getAvatarUrl = (avatar) => {
  if (!avatar || avatar.trim() === '') return ''
  const trimmedAvatar = avatar.trim()
  if (trimmedAvatar.startsWith('http://') || trimmedAvatar.startsWith('https://')) {
    return trimmedAvatar
  }
  if (trimmedAvatar.startsWith('/uploads/')) {
    return '/api' + trimmedAvatar
  }
  if (trimmedAvatar.startsWith('/api/uploads/')) {
    return trimmedAvatar
  }
  return '/api/uploads/' + trimmedAvatar.replace(/^\//, '')
}

const loadMerchantInfo = async () => {
  try {
    const res = await api.get('/merchant/info')
    if (res.code === 200 && res.data) {
      merchantInfo.value = res.data
      localStorage.setItem('merchant_info', JSON.stringify(res.data))
    }
  } catch (error) {
    // 如果接口失败，尝试从本地存储读取
    const info = localStorage.getItem('merchant_info')
    if (info) {
      try {
        merchantInfo.value = JSON.parse(info)
      } catch (e) {
        console.error('解析商家信息失败', e)
      }
    }
  }
}

onMounted(() => {
  // 先从本地存储加载，避免页面闪烁
  const info = localStorage.getItem('merchant_info')
  if (info) {
    try {
      merchantInfo.value = JSON.parse(info)
    } catch (e) {
      console.error('解析商家信息失败', e)
    }
  }
  // 然后从服务器获取最新信息
  loadMerchantInfo()
})

const handleCommand = (command) => {
  if (command === 'logout') {
    localStorage.removeItem('merchant_token')
    localStorage.removeItem('merchant_info')
    router.push('/merchant/login')
  }
}
</script>

<style scoped>
.merchant-layout {
  height: 100vh;
}

.el-container {
  height: 100%;
}

.el-aside {
  background-color: #001529;
}

.logo {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #002140;
}

.logo h2 {
  color: #fff;
  font-size: 18px;
  margin: 0;
}

.el-header {
  background-color: #fff;
  border-bottom: 1px solid #e8e8e8;
  display: flex;
  align-items: center;
}

.header-content {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.user-dropdown {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
}

.merchant-avatar {
  margin-right: 6px;
  border: 1px solid rgba(0, 0, 0, 0.08);
}

.merchant-name {
  max-width: 160px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.el-main {
  background-color: #f0f2f5;
}
</style>

