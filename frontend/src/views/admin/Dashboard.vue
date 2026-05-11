<template>
  <div class="dashboard">
    <!-- 顶部欢迎区 -->
    <div class="welcome-banner">
      <div class="welcome-text">
        <h1>欢迎使用飘香水果商城管理后台</h1>
        <p>今日数据一览，助力平台高效运营</p>
      </div>
      <div class="welcome-right">
        <div class="welcome-date">{{ currentDate }}</div>
        <el-button
          type="primary"
          :icon="Refresh"
          class="refresh-btn"
          @click="handleRefresh"
          :loading="refreshing"
        >
          刷新数据
        </el-button>
      </div>
    </div>

    <!-- 今日实时数据卡片 -->
    <el-row :gutter="20" class="today-stat-cards">
      <el-col :xs="12" :sm="8" :md="6" :lg="6">
        <el-card shadow="hover" class="today-stat-card" @click="$router.push('/admin/orders')">
          <div class="today-stat-content">
            <div class="today-stat-label">今日订单</div>
            <div class="today-stat-value">{{ stats.todayOrders || 0 }}</div>
            <div class="today-stat-trend" v-if="stats.todayOrdersGrowth !== undefined">
              <el-icon v-if="stats.todayOrdersGrowth >= 0" class="trend-up"><ArrowUp /></el-icon>
              <el-icon v-else class="trend-down"><ArrowDown /></el-icon>
              <span>{{ Math.abs(stats.todayOrdersGrowth || 0) }}%</span>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="8" :md="6" :lg="6">
        <el-card shadow="hover" class="today-stat-card" @click="$router.push('/admin/users')">
          <div class="today-stat-content">
            <div class="today-stat-label">今日新增用户</div>
            <div class="today-stat-value">{{ stats.todayNewUsers || 0 }}</div>
            <div class="today-stat-trend" v-if="stats.todayNewUsersGrowth !== undefined">
              <el-icon v-if="stats.todayNewUsersGrowth >= 0" class="trend-up"><ArrowUp /></el-icon>
              <el-icon v-else class="trend-down"><ArrowDown /></el-icon>
              <span>{{ Math.abs(stats.todayNewUsersGrowth || 0) }}%</span>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="8" :md="6" :lg="6">
        <el-card shadow="hover" class="today-stat-card" @click="$router.push('/admin/merchants')">
          <div class="today-stat-content">
            <div class="today-stat-label">今日新增商家</div>
            <div class="today-stat-value">{{ stats.todayNewMerchants || 0 }}</div>
            <div class="today-stat-trend" v-if="stats.todayNewMerchantsGrowth !== undefined">
              <el-icon v-if="stats.todayNewMerchantsGrowth >= 0" class="trend-up"><ArrowUp /></el-icon>
              <el-icon v-else class="trend-down"><ArrowDown /></el-icon>
              <span>{{ Math.abs(stats.todayNewMerchantsGrowth || 0) }}%</span>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="8" :md="6" :lg="6">
        <el-card shadow="hover" class="today-stat-card" @click="$router.push('/admin/orders')">
          <div class="today-stat-content">
            <div class="today-stat-label">今日销售额</div>
            <div class="today-stat-value">¥{{ formatNumber(stats.todaySales || 0) }}</div>
            <div class="today-stat-trend" v-if="stats.todaySalesGrowth !== undefined">
              <el-icon v-if="stats.todaySalesGrowth >= 0" class="trend-up"><ArrowUp /></el-icon>
              <el-icon v-else class="trend-down"><ArrowDown /></el-icon>
              <span>{{ Math.abs(stats.todaySalesGrowth || 0) }}%</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 核心数据卡片 -->
    <el-row :gutter="20" class="stat-cards">
      <el-col :xs="12" :sm="12" :md="6" :lg="6">
        <el-card shadow="hover" class="stat-card users clickable" @click="$router.push('/admin/users')">
          <div class="stat-card-top-bar"></div>
          <div class="stat-card-content">
            <div class="stat-icon-wrapper">
              <div class="stat-icon">
                <el-icon><User /></el-icon>
              </div>
            </div>
            <div class="stat-info">
              <div class="stat-label">用户总数</div>
              <div class="stat-value">{{ formatNumber(stats.totalUsers) }}</div>
              <div class="stat-trend" v-if="stats.usersGrowth !== undefined">
                <el-icon v-if="stats.usersGrowth >= 0" class="trend-icon trend-up"><ArrowUp /></el-icon>
                <el-icon v-else class="trend-icon trend-down"><ArrowDown /></el-icon>
                <span>环比昨日 {{ stats.usersGrowth >= 0 ? '+' : '' }}{{ stats.usersGrowth || 0 }}%</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6" :lg="6">
        <el-card shadow="hover" class="stat-card merchants clickable" @click="$router.push('/admin/merchants')">
          <div class="stat-card-top-bar"></div>
          <div class="stat-card-content">
            <div class="stat-icon-wrapper">
              <div class="stat-icon">
                <el-icon><Shop /></el-icon>
              </div>
            </div>
            <div class="stat-info">
              <div class="stat-label">商家总数</div>
              <div class="stat-value">{{ formatNumber(stats.totalMerchants) }}</div>
              <div class="stat-trend" v-if="stats.merchantsGrowth !== undefined">
                <el-icon v-if="stats.merchantsGrowth >= 0" class="trend-icon trend-up"><ArrowUp /></el-icon>
                <el-icon v-else class="trend-icon trend-down"><ArrowDown /></el-icon>
                <span>环比昨日 {{ stats.merchantsGrowth >= 0 ? '+' : '' }}{{ stats.merchantsGrowth || 0 }}%</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6" :lg="6">
        <el-card shadow="hover" class="stat-card products clickable" @click="$router.push('/admin/products')">
          <div class="stat-card-top-bar"></div>
          <div class="stat-card-content">
            <div class="stat-icon-wrapper">
              <div class="stat-icon">
                <el-icon><Goods /></el-icon>
              </div>
            </div>
            <div class="stat-info">
              <div class="stat-label">商品总数</div>
              <div class="stat-value">{{ formatNumber(stats.totalProducts) }}</div>
              <div class="stat-trend" v-if="stats.productsGrowth !== undefined">
                <el-icon v-if="stats.productsGrowth >= 0" class="trend-icon trend-up"><ArrowUp /></el-icon>
                <el-icon v-else class="trend-icon trend-down"><ArrowDown /></el-icon>
                <span>环比昨日 {{ stats.productsGrowth >= 0 ? '+' : '' }}{{ stats.productsGrowth || 0 }}%</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6" :lg="6">
        <el-card shadow="hover" class="stat-card sales clickable" @click="$router.push('/admin/orders')">
          <div class="stat-card-top-bar"></div>
          <div class="stat-card-content">
            <div class="stat-icon-wrapper">
              <div class="stat-icon">
                <el-icon><TrendCharts /></el-icon>
              </div>
            </div>
            <div class="stat-info">
              <div class="stat-label">总销售额</div>
              <div class="stat-value">¥{{ formatNumber(stats.totalSales) }}</div>
              <div class="stat-trend" v-if="stats.salesGrowth !== undefined">
                <el-icon v-if="stats.salesGrowth >= 0" class="trend-icon trend-up"><ArrowUp /></el-icon>
                <el-icon v-else class="trend-icon trend-down"><ArrowDown /></el-icon>
                <span>环比昨日 {{ stats.salesGrowth >= 0 ? '+' : '' }}{{ stats.salesGrowth || 0 }}%</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 待处理事项和快捷操作 -->
    <el-row :gutter="20" class="action-row">
      <el-col :xs="24" :sm="24" :md="8" :lg="8">
        <el-card shadow="hover" class="pending-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">待处理事项</span>
              <el-tag type="warning" size="small">需要关注</el-tag>
            </div>
          </template>
          <div class="pending-items">
            <div class="pending-item" @click="$router.push('/admin/merchants')">
              <div class="pending-info">
                <el-icon class="pending-icon"><Shop /></el-icon>
                <span>待审核商家</span>
              </div>
              <el-badge :value="stats.pendingMerchants" :max="99" class="pending-badge" />
            </div>
            <div class="pending-item" @click="$router.push('/admin/products')">
              <div class="pending-info">
                <el-icon class="pending-icon"><Goods /></el-icon>
                <span>待审核商品</span>
              </div>
              <el-badge :value="stats.pendingProducts" :max="99" class="pending-badge" />
            </div>
            <div class="pending-item" @click="$router.push('/admin/aftersales')">
              <div class="pending-info">
                <el-icon class="pending-icon"><List /></el-icon>
                <span>待处理订单</span>
              </div>
              <el-badge :value="stats.pendingOrders || 0" :max="99" class="pending-badge" />
            </div>
            <div class="pending-item" @click="$router.push('/admin/chats')">
              <div class="pending-info">
                <el-icon class="pending-icon"><ChatDotRound /></el-icon>
                <span>待回复消息</span>
              </div>
              <el-badge :value="stats.pendingReviews || 0" :max="99" class="pending-badge" />
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="24" :md="16" :lg="16">
        <el-card shadow="hover" class="quick-actions-card">
          <template #header>
            <span class="card-title">快捷操作</span>
          </template>
          <div class="quick-actions">
            <div class="action-item" @click="$router.push('/admin/users')">
              <div class="action-icon">
                <el-icon><User /></el-icon>
              </div>
              <span>用户管理</span>
            </div>
            <div class="action-item" @click="$router.push('/admin/merchants')">
              <div class="action-icon">
                <el-icon><Shop /></el-icon>
              </div>
              <span>商家管理</span>
            </div>
            <div class="action-item" @click="$router.push('/admin/products')">
              <div class="action-icon">
                <el-icon><Goods /></el-icon>
              </div>
              <span>商品管理</span>
            </div>
            <div class="action-item" @click="$router.push('/admin/orders')">
              <div class="action-icon">
                <el-icon><List /></el-icon>
              </div>
              <span>订单管理</span>
            </div>
            <div class="action-item" @click="$router.push('/admin/categories')">
              <div class="action-icon">
                <el-icon><Menu /></el-icon>
              </div>
              <span>分类管理</span>
            </div>
            <div class="action-item" @click="$router.push('/admin/aftersales')">
              <div class="action-icon">
                <el-icon><List /></el-icon>
              </div>
              <span>售后申诉</span>
            </div>
            <div class="action-item" @click="$router.push('/admin/chats')">
              <div class="action-icon">
                <el-icon><ChatDotRound /></el-icon>
              </div>
              <span>私聊消息</span>
            </div>
            <div class="action-item" @click="$router.push('/admin/coupons')">
              <div class="action-icon">
                <el-icon><Ticket /></el-icon>
              </div>
              <span>优惠券管理</span>
            </div>
            <div class="action-item" @click="$router.push('/admin/announcements')">
              <div class="action-icon">
                <el-icon><Bell /></el-icon>
              </div>
              <span>公告管理</span>
            </div>
            <div class="action-item" @click="$router.push('/admin/contact-messages')">
              <div class="action-icon">
                <el-icon><Message /></el-icon>
              </div>
              <span>在线留言</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import api from '@/utils/api'
import { 
  User, 
  Shop, 
  Goods, 
  TrendCharts, 
  List, 
  Menu,
  Refresh,
  ArrowUp,
  ArrowDown,
  ChatDotRound,
  Ticket,
  Bell,
  Message
} from '@element-plus/icons-vue'

const refreshing = ref(false)

const stats = ref({
  totalUsers: 0,
  activeUsers: 0,
  totalMerchants: 0,
  activeMerchants: 0,
  pendingMerchants: 0,
  totalProducts: 0,
  pendingProducts: 0,
  totalOrders: 0,
  pendingOrders: 0,
  totalSales: 0,
  todayOrders: 0,
  todayNewUsers: 0,
  todayNewMerchants: 0,
  todaySales: 0,
  pendingReviews: 0,
  // 环比增长数据（如果后端没有提供，前端可以计算）
  usersGrowth: 0,
  merchantsGrowth: 0,
  productsGrowth: 0,
  salesGrowth: 0,
  todayOrdersGrowth: 0,
  todayNewUsersGrowth: 0,
  todayNewMerchantsGrowth: 0,
  todaySalesGrowth: 0
})

const currentDate = computed(() => {
  return new Date().toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    weekday: 'long'
  })
})

// 格式化数字
const formatNumber = (num) => {
  if (num === null || num === undefined) return '0'
  return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',')
}

// 加载统计数据
const loadStats = async () => {
  try {
    const res = await api.get('/admin/statistics', {
      params: { _t: Date.now() }
    })
    if (res.code === 200) {
      // 合并数据，保留默认值
      stats.value = {
        ...stats.value,
        ...res.data
      }
    }
  } catch (error) {
    console.error('加载统计数据失败', error)
    ElMessage.error('加载统计数据失败')
  }
}

// 刷新数据
const handleRefresh = async () => {
  refreshing.value = true
  try {
    await loadStats()
    ElMessage.success('数据已刷新')
  } catch (error) {
    ElMessage.error('刷新失败')
  } finally {
    refreshing.value = false
  }
}

onMounted(() => {
  loadStats()
})
</script>

<style scoped>
.dashboard {
  padding: 24px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}

/* 欢迎横幅 */
.welcome-banner {
  background: #ffffff;
  border-radius: 8px;
  padding: 32px;
  margin-bottom: 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #2c3e50;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  border: 1px solid #e8e8e8;
}

.welcome-text h1 {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: bold;
}

.welcome-text p {
  margin: 0;
  font-size: 16px;
  color: #7f8c8d;
}

.welcome-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.welcome-date {
  font-size: 14px;
  color: #7f8c8d;
}

.refresh-btn {
  background: #f5f7fa;
  border-color: #e8e8e8;
  color: #2c3e50;
}

.refresh-btn:hover {
  background: #ecf0f1;
  border-color: #d5d5d5;
  color: #2c3e50;
}

/* 今日实时数据卡片 */
.today-stat-cards {
  margin-bottom: 24px;
}

.today-stat-card {
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.today-stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.today-stat-card :deep(.el-card__body) {
  padding: 20px;
}

.today-stat-content {
  text-align: center;
}

.today-stat-label {
  font-size: 14px;
  color: #7f8c8d;
  margin-bottom: 8px;
}

.today-stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #2c3e50;
  margin-bottom: 8px;
}

.today-stat-trend {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  font-size: 12px;
  margin-top: 4px;
}

.today-stat-trend .trend-up {
  color: #2ecc71;
}

.today-stat-trend .trend-down {
  color: #e74c3c;
}

/* 核心数据卡片 */
.stat-cards {
  margin-bottom: 24px;
}

.stat-card {
  position: relative;
  overflow: hidden;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.stat-card.clickable:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.stat-card-top-bar {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
}

.stat-card.users .stat-card-top-bar {
  background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
}

.stat-card.merchants .stat-card-top-bar {
  background: linear-gradient(90deg, #11998e 0%, #38ef7d 100%);
}

.stat-card.products .stat-card-top-bar {
  background: linear-gradient(90deg, #f093fb 0%, #f5576c 100%);
}

.stat-card.sales .stat-card-top-bar {
  background: linear-gradient(90deg, #4facfe 0%, #00f2fe 100%);
}

.stat-card-content {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
}

.stat-icon-wrapper {
  flex-shrink: 0;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: white;
}

.stat-card.users .stat-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stat-card.merchants .stat-icon {
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
}

.stat-card.products .stat-icon {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.stat-card.sales .stat-icon {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.stat-info {
  flex: 1;
}

.stat-label {
  font-size: 14px;
  color: #7f8c8d;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #2c3e50;
  margin-bottom: 4px;
  transition: transform 0.3s;
}

.stat-card:hover .stat-value {
  transform: scale(1.05);
}

.stat-trend {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #7f8c8d;
  margin-top: 4px;
}

.trend-icon {
  font-size: 14px;
}

.trend-up {
  color: #2ecc71;
}

.trend-down {
  color: #e74c3c;
}

/* 待处理事项和快捷操作 */
.action-row {
  margin-bottom: 24px;
}

.pending-card,
.quick-actions-card {
  border-radius: 8px;
  height: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 16px;
  font-weight: bold;
  color: #2c3e50;
}

.pending-items {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.pending-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.pending-item:hover {
  background: #e8f5e9;
  transform: translateX(4px);
}

.pending-info {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #34495e;
}

.pending-icon {
  font-size: 20px;
  color: #2ecc71;
}

.pending-badge :deep(.el-badge__content) {
  background: #e74c3c;
  color: white;
  border: none;
}

.quick-actions {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 16px;
}

.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 24px 16px;
  background: #f8f9fa;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.action-item:hover {
  background: #e8f5e9;
  transform: translateY(-4px) scale(1.02);
  box-shadow: 0 4px 12px rgba(46, 204, 113, 0.15);
}

.action-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  background: rgba(46, 204, 113, 0.1);
  transition: all 0.3s;
}

.action-item:hover .action-icon {
  background: rgba(46, 204, 113, 0.2);
}

.action-icon .el-icon {
  font-size: 24px;
  color: #2ecc71;
}

.action-item span {
  font-size: 14px;
  color: #34495e;
  font-weight: 500;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .dashboard {
    padding: 16px;
  }

  .welcome-banner {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
    padding: 20px;
  }

  .welcome-text h1 {
    font-size: 20px;
  }

  .welcome-text p {
    font-size: 14px;
  }

  .welcome-right {
    width: 100%;
    justify-content: space-between;
  }

  .stat-card-content {
    flex-direction: column;
    text-align: center;
  }

  .quick-actions {
    grid-template-columns: repeat(3, 1fr);
  }

  .today-stat-value,
  .stat-value {
    font-size: 20px;
  }
}

@media (max-width: 576px) {
  .quick-actions {
    grid-template-columns: repeat(2, 1fr);
  }

  .action-item {
    padding: 16px 12px;
  }
}
</style>
