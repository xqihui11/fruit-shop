<template>
  <div class="main-layout">
    <el-header class="header">
      <div class="header-content">
        <div class="logo" @click="$router.push('/')">
          <el-icon><Shop /></el-icon>
          <span>飘香水果商城</span>
        </div>
        <div class="nav-menu">
          <el-menu
            mode="horizontal"
            :default-active="activeMenu"
            router
            class="nav-menu-item"
          >
            <el-menu-item index="/">
              <el-icon><House /></el-icon>
              <span>首页</span>
            </el-menu-item>
            <el-menu-item index="/merchants">
              <el-icon><Shop /></el-icon>
              <span>店铺列表</span>
            </el-menu-item>
            <el-menu-item v-if="userStore.user" index="/profile">
              <el-icon><User /></el-icon>
              <span>个人中心</span>
            </el-menu-item>
            <el-menu-item v-if="userStore.user" index="/order">
              <el-icon><List /></el-icon>
              <span>我的订单</span>
            </el-menu-item>
            <el-menu-item index="/cart">
              <el-icon><ShoppingCart /></el-icon>
              <span>购物车</span>
              <el-badge v-if="cartCount > 0" :value="cartCount" class="cart-badge" />
            </el-menu-item>
            <el-menu-item v-if="userStore.user" index="/coupons">
              <el-icon><Ticket /></el-icon>
              <span>优惠券</span>
            </el-menu-item>
            <el-menu-item index="/announcements">
              <el-icon><Bell /></el-icon>
              <span>公告中心</span>
            </el-menu-item>
            <el-menu-item v-if="userStore.user" index="/messages" class="message-menu-item">
              <el-icon><Bell /></el-icon>
              <span>消息</span>
              <el-badge v-if="unreadCount > 0" :value="unreadCount" class="message-badge" />
            </el-menu-item>
          </el-menu>
        </div>
        <div class="user-info">
          <template v-if="userStore.user">
            <el-dropdown>
              <span class="user-name">
                <el-avatar :size="32" :src="getAvatarUrl(userStore.user.avatar)" class="user-avatar">
                  {{ (userStore.user.nickname || userStore.user.username)?.charAt(0) || 'U' }}
                </el-avatar>
                {{ userStore.user.nickname || userStore.user.username }}
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <el-button type="text" @click="$router.push('/login')">登录</el-button>
            <el-button type="primary" @click="$router.push('/register')">注册</el-button>
          </template>
        </div>
      </div>
    </el-header>
    <el-main class="main-content">
      <!-- 公告展示 -->
      <AnnouncementBanner v-if="announcements.length > 0" :announcements="announcements" />
      <!-- 优惠券横幅展示 -->
      <CouponBanner v-if="availableCoupons.length > 0" :coupons="availableCoupons" />
      <router-view />
    </el-main>
    <footer class="footer">
      <div class="footer-content">
        <div class="footer-section">
          <h4>关于我们</h4>
          <p>飘香水果商城致力于为您提供新鲜、优质的水果，从产地直达您的餐桌。</p>
          <el-link type="primary" :underline="false" @click="$router.push('/about')" class="footer-link">
            了解更多 →
          </el-link>
        </div>
        <div class="footer-section">
          <h4>客户服务</h4>
          <ul>
            <li>
              <el-link type="primary" :underline="false" @click="$router.push('/delivery')" class="footer-link">
                配送说明 →
              </el-link>
            </li>
            <li>
              <el-link type="primary" :underline="false" @click="$router.push('/aftersale')" class="footer-link">
                售后服务 →
              </el-link>
            </li>
            <li>
              <el-link type="primary" :underline="false" @click="$router.push('/faq')" class="footer-link">
                常见问题 →
              </el-link>
            </li>
          </ul>
        </div>
        <div class="footer-section">
          <h4>联系我们</h4>
          <ul>
            <li>客服热线：400-888-8888</li>
            <li>工作时间：9:00-21:00</li>
            <li>客服邮箱：service@fruit.com</li>
          </ul>
          <el-link type="primary" :underline="false" @click="$router.push('/contact')" class="footer-link">
            在线留言 →
          </el-link>
        </div>
        <div class="footer-section">
          <h4>入驻合作</h4>
          <p>欢迎优质水果供应商入驻</p>
          <el-button type="primary" size="small" @click="$router.push('/merchant/register')">商家入驻</el-button>
        </div>
      </div>
      <div class="footer-bottom">
        <p>© 2024 飘香水果商城 - 毕业设计项目</p>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { computed, ref, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'
import { ElMessage } from 'element-plus'
import { Bell, ShoppingCart, User, List, Shop, House, Ticket } from '@element-plus/icons-vue'
import AnnouncementBanner from '@/components/AnnouncementBanner.vue'
import CouponBanner from '@/components/CouponBanner.vue'
import api from '@/utils/api'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()

const activeMenu = computed(() => route.path)

const cartCount = computed(() => cartStore.items.length)
const unreadCount = ref(0)
const announcements = ref([])
const availableCoupons = ref([])
let messageTimer = null

const loadUnreadCount = async () => {
  if (!userStore.user) {
    unreadCount.value = 0
    return
  }
  try {
    const res = await api.get('/message/unread-count')
    if (res.code === 200) {
      unreadCount.value = res.data?.count || 0
    }
  } catch (error) {
    console.error('加载未读消息数失败', error)
  }
}

const loadAnnouncements = async () => {
  try {
    const res = await api.get('/announcement/published', { params: { target: 1 } })
    if (res.code === 200) {
      announcements.value = (res.data || []).slice(0, 3) // 只显示前3条
    }
  } catch (error) {
    console.error('加载公告失败', error)
  }
}

const loadAvailableCoupons = async () => {
  try {
    const res = await api.get('/coupon/available')
    if (res.code === 200) {
      // 只显示前3条可领取的优惠券
      availableCoupons.value = (res.data || []).filter(c => c.canReceive).slice(0, 3)
    }
  } catch (error) {
    console.error('加载优惠券失败', error)
  }
}

onMounted(() => {
  loadUnreadCount()
  loadAnnouncements()
  loadAvailableCoupons()
  // 每30秒刷新一次未读消息数
  messageTimer = setInterval(loadUnreadCount, 30000)
})

onUnmounted(() => {
  if (messageTimer) {
    clearInterval(messageTimer)
  }
})

const handleLogout = () => {
  userStore.logout()
  ElMessage.success('已退出登录')
  router.push('/')
  unreadCount.value = 0
}

const getAvatarUrl = (avatar) => {
  if (!avatar) return ''
  if (avatar.startsWith('http://') || avatar.startsWith('https://')) {
    return avatar
  }
  if (avatar.startsWith('/uploads/')) {
    return '/api' + avatar
  }
  if (avatar.startsWith('/api/uploads/')) {
    return avatar
  }
  return '/api/uploads/' + avatar
}
</script>

<style scoped>
.main-layout {
  min-height: 100vh;
  background-color: #f5f7fa;
}

.header {
  background: #ffffff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  padding: 0;
  height: 64px;
  line-height: 64px;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 20px;
  font-weight: 600;
  color: #2ecc71;
  cursor: pointer;
}

.nav-menu {
  flex: 1;
  display: flex;
  justify-content: center;
}

.nav-menu-item {
  border: none;
  background: transparent;
}

.nav-menu-item :deep(.el-menu) {
  --el-menu-bg-color: transparent;
  --el-menu-text-color: #2c3e50;
  --el-menu-active-color: #2ecc71;
  --el-menu-hover-text-color: #2ecc71;
  border-bottom: none;
}

.nav-menu-item :deep(.el-menu-item) {
  padding: 0 16px;
}

.nav-menu-item :deep(.el-menu-item.is-active) {
  border-bottom: 2px solid #2ecc71;
  font-weight: 600;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-name {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: #262626;
}

.user-avatar {
  flex-shrink: 0;
}

.cart-badge {
  margin-left: 4px;
}

.message-menu-item {
  position: relative;
}

.message-badge {
  margin-left: 4px;
}

.main-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
  min-height: calc(100vh - 64px - 280px);
}

.footer {
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
  color: #ffffff;
  padding: 40px 0 0;
  margin-top: 40px;
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 40px;
}

.footer-section h4 {
  font-size: 16px;
  margin-bottom: 16px;
  color: #ffffff;
  position: relative;
  padding-bottom: 8px;
}

.footer-section h4::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 30px;
  height: 2px;
  background: #1890ff;
}

.footer-section p {
  color: #b0b0b0;
  font-size: 14px;
  line-height: 1.6;
}

.footer-section ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.footer-section ul li {
  color: #b0b0b0;
  font-size: 14px;
  margin-bottom: 8px;
  cursor: pointer;
  transition: color 0.3s;
}

.footer-section ul li:hover {
  color: #1890ff;
}

.footer-link {
  margin-top: 12px;
  display: inline-block;
  font-size: 14px;
  transition: transform 0.3s;
}

.footer-link:hover {
  transform: translateX(4px);
}

.footer-bottom {
  margin-top: 32px;
  padding: 20px;
  border-top: 1px solid #2a2a3e;
  text-align: center;
}

.footer-bottom p {
  margin: 0;
  color: #6c6c80;
  font-size: 14px;
}

@media (max-width: 768px) {
  .footer-content {
    grid-template-columns: repeat(2, 1fr);
    gap: 24px;
  }
}

@media (max-width: 480px) {
  .footer-content {
    grid-template-columns: 1fr;
  }
}
</style>

