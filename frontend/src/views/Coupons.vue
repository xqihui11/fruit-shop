<template>
  <div class="coupons-page">
    <div class="page-header">
      <h2>我的优惠券</h2>
    </div>

    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane label="未使用" name="0"></el-tab-pane>
      <el-tab-pane label="已使用" name="1"></el-tab-pane>
      <el-tab-pane label="已过期" name="2"></el-tab-pane>
      <el-tab-pane label="可领取" name="available"></el-tab-pane>
    </el-tabs>

    <div v-if="activeTab === 'available'" class="coupons-grid">
      <div v-for="coupon in availableCoupons" :key="coupon.id" class="coupon-card-new available">
        <div class="coupon-badge-top" v-if="coupon.merchantId">
          <span class="badge-text">商家券</span>
        </div>
        <div class="coupon-content">
          <div class="coupon-left">
            <div class="coupon-amount">
              <span v-if="coupon.type === 1" class="amount-value">
                {{ formatMoney(coupon.discountAmount) }}<span class="amount-unit">元</span>
              </span>
              <span v-else class="amount-value">
                {{ (coupon.discountRate * 10).toFixed(1) }}<span class="amount-unit">折</span>
              </span>
            </div>
            <div class="coupon-threshold">
              <span v-if="coupon.minAmount > 0">满{{ formatMoney(coupon.minAmount) }}可用</span>
              <span v-else>无门槛</span>
            </div>
          </div>
          <div class="coupon-divider"></div>
          <div class="coupon-right">
            <div class="coupon-title-row">
              <div class="coupon-title">{{ coupon.name }}</div>
              <div class="coupon-merchant" v-if="coupon.merchantId && coupon.merchantName">
                {{ coupon.merchantName }}
              </div>
            </div>
            <div class="coupon-desc-new">{{ coupon.description || '精选优惠，限时领取' }}</div>
            <div class="coupon-info-row">
              <span class="coupon-expire">
                {{ getExpireText(coupon.validEndTime) }}
              </span>
              <span class="coupon-rule-link" @click.stop="showRule(coupon)">规则 ></span>
            </div>
            <div class="coupon-action">
              <el-button 
                type="danger" 
                :disabled="!coupon.canReceive"
                @click.stop="receiveCoupon(coupon.id)"
                class="use-btn"
              >
                {{ coupon.canReceive ? '立即领取' : '已领取' }}
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-else class="coupons-grid">
      <div 
        v-for="coupon in myCoupons" 
        :key="coupon.id" 
        class="coupon-card-new"
        :class="{ 'used': activeTab === '1', 'expired': activeTab === '2' }"
      >
        <div class="coupon-badge-top" v-if="coupon.merchantId && coupon.merchantName">
          <span class="badge-text">商家券</span>
        </div>
        <div class="coupon-content">
          <div class="coupon-left">
            <div class="coupon-amount">
              <span v-if="coupon.type === 1" class="amount-value">
                {{ formatMoney(coupon.discountAmount) }}<span class="amount-unit">元</span>
              </span>
              <span v-else class="amount-value">
                {{ (coupon.discountRate * 10).toFixed(1) }}<span class="amount-unit">折</span>
              </span>
            </div>
            <div class="coupon-threshold">
              <span v-if="coupon.minAmount > 0">满{{ formatMoney(coupon.minAmount) }}可用</span>
              <span v-else>无门槛</span>
            </div>
          </div>
          <div class="coupon-divider"></div>
          <div class="coupon-right">
            <div class="coupon-title-row">
              <div class="coupon-title">{{ coupon.name }}</div>
              <div class="coupon-merchant" v-if="coupon.merchantId && coupon.merchantName">
                {{ coupon.merchantName }}
              </div>
            </div>
            <div class="coupon-desc-new">{{ coupon.description || '精选优惠，限时使用' }}</div>
            <div class="coupon-info-row">
              <span class="coupon-expire">
                <span v-if="activeTab === '0'">{{ getExpireText(coupon.validEndTime) }}</span>
                <span v-else-if="activeTab === '1'">已使用</span>
                <span v-else>已过期</span>
              </span>
              <span class="coupon-rule-link" @click.stop="showRule(coupon)">规则 ></span>
            </div>
            <div class="coupon-action">
              <el-button 
                v-if="activeTab === '0'"
                type="danger" 
                @click.stop="useCoupon(coupon)"
                class="use-btn"
              >
                去使用
              </el-button>
              <span v-else class="status-text-new">
                {{ activeTab === '1' ? '已使用' : '已过期' }}
              </span>
            </div>
          </div>
        </div>
      </div>
      <div v-if="myCoupons.length === 0" class="empty-state">
        <el-empty description="暂无优惠券" />
      </div>
      <div v-if="(activeTab === '1' || activeTab === '2') && myCoupons.length > 0" class="clear-all-container">
        <el-button 
          type="text" 
          @click="clearCoupons(parseInt(activeTab))"
          class="clear-all-btn"
        >
          清除全部{{ activeTab === '1' ? '已使用' : '已过期' }}优惠券
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import api from '@/utils/api'

const router = useRouter()
const activeTab = ref('0')
const myCoupons = ref([])
const availableCoupons = ref([])

const formatMoney = (amount) => {
  if (!amount) return '0.00'
  return parseFloat(amount).toFixed(2)
}

const formatTime = (time) => {
  if (!time) return '—'
  return new Date(time).toLocaleDateString('zh-CN')
}

const getExpireText = (time) => {
  if (!time) return '永久有效'
  const now = new Date()
  const expire = new Date(time)
  const diff = expire.getTime() - now.getTime()
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  
  if (days < 0) return '已过期'
  if (days === 0) return '今天过期'
  if (days === 1) return '明天过期'
  if (days <= 7) return `${days}天后过期`
  return `有效期至 ${formatTime(time)}`
}

const showRule = (coupon) => {
  let ruleText = ''
  if (coupon.type === 1) {
    ruleText = `满${formatMoney(coupon.minAmount)}元可用，立减${formatMoney(coupon.discountAmount)}元`
  } else {
    ruleText = `满${formatMoney(coupon.minAmount)}元可用，享受${(coupon.discountRate * 10).toFixed(1)}折优惠`
    if (coupon.maxDiscountAmount) {
      ruleText += `，最高减${formatMoney(coupon.maxDiscountAmount)}元`
    }
  }
  if (coupon.merchantId && coupon.merchantName) {
    ruleText += `\n仅限${coupon.merchantName}使用`
  } else {
    ruleText += '\n全平台通用'
  }
  ElMessage.info(ruleText)
}

const loadMyCoupons = async () => {
  try {
    const status = activeTab.value === 'available' ? null : parseInt(activeTab.value)
    const res = await api.get('/coupon/my', { params: { status } })
    if (res.code === 200) {
      myCoupons.value = res.data || []
    }
  } catch (error) {
    console.error('加载优惠券失败', error)
  }
}

const loadAvailableCoupons = async () => {
  try {
    const res = await api.get('/coupon/available')
    if (res.code === 200) {
      availableCoupons.value = res.data || []
    }
  } catch (error) {
    console.error('加载可领取优惠券失败', error)
  }
}

const receiveCoupon = async (couponId) => {
  try {
    await api.post(`/coupon/receive/${couponId}`)
    ElMessage.success('领取成功')
    loadAvailableCoupons()
    if (activeTab.value === '0') {
      loadMyCoupons()
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '领取失败')
  }
}

const useCoupon = (coupon) => {
  router.push('/')
}

const clearCoupons = async (status) => {
  try {
    await ElMessageBox.confirm(
      status === 1 
        ? '确定要清除所有已使用的优惠券吗？清除后无法恢复。' 
        : '确定要清除所有已过期的优惠券吗？清除后无法恢复。',
      '确认清除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    
    await api.delete('/coupon/clear', { params: { status } })
    ElMessage.success('清除成功')
    loadMyCoupons()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '清除失败')
    }
  }
}

const handleTabChange = () => {
  if (activeTab.value === 'available') {
    loadAvailableCoupons()
  } else {
    loadMyCoupons()
  }
}

onMounted(() => {
  loadMyCoupons()
})
</script>

<style scoped>
.coupons-page {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 24px;
}

.page-header h2 {
  margin: 0;
  font-size: 24px;
  color: #2c3e50;
}

.coupons-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
  margin-top: 20px;
}

/* 新样式 - 类似支付宝神券 */
.coupon-card-new {
  background: #fff5f5;
  border-radius: 12px;
  padding: 0;
  position: relative;
  overflow: hidden;
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border: 1px solid #ffe0e0;
}

.coupon-card-new:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.coupon-card-new.used {
  background: #f5f5f5;
  border-color: #d9d9d9;
  opacity: 0.7;
}

.coupon-card-new.expired {
  background: #f5f5f5;
  border-color: #d9d9d9;
  opacity: 0.6;
}

.coupon-badge-top {
  position: absolute;
  top: 0;
  left: 0;
  background: #ff4d4f;
  color: #fff;
  padding: 4px 12px;
  border-radius: 0 0 8px 0;
  font-size: 11px;
  font-weight: 600;
  z-index: 1;
}

.badge-text {
  display: inline-block;
}

.coupon-content {
  display: flex;
  padding: 16px;
  min-height: 140px;
}

.coupon-left {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-width: 120px;
  padding-right: 16px;
  border-right: 2px dashed #ffcccc;
}

.coupon-amount {
  text-align: center;
  margin-bottom: 8px;
}

.amount-value {
  font-size: 36px;
  font-weight: bold;
  color: #ff4d4f;
  line-height: 1;
}

.amount-unit {
  font-size: 18px;
  margin-left: 2px;
}

.coupon-threshold {
  font-size: 12px;
  color: #999;
  text-align: center;
}

.coupon-divider {
  width: 2px;
  background: #ffcccc;
  margin: 0 16px;
}

.coupon-right {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding-left: 16px;
}

.coupon-title-row {
  margin-bottom: 8px;
}

.coupon-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
}

.coupon-merchant {
  font-size: 12px;
  color: #ff4d4f;
  font-weight: 500;
  margin-top: 4px;
}

.coupon-desc-new {
  font-size: 12px;
  color: #999;
  margin-bottom: 8px;
  line-height: 1.5;
}

.coupon-info-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
  font-size: 12px;
}

.coupon-expire {
  color: #ff4d4f;
}

.coupon-rule-link {
  color: #999;
  cursor: pointer;
  transition: color 0.3s;
}

.coupon-rule-link:hover {
  color: #ff4d4f;
}

.coupon-action {
  display: flex;
  justify-content: flex-end;
}

.use-btn {
  background: #ff4d4f;
  border-color: #ff4d4f;
  color: #fff;
  font-weight: 600;
  padding: 8px 24px;
}

.use-btn:hover {
  background: #ff7875;
  border-color: #ff7875;
}

.status-text-new {
  color: #999;
  font-size: 14px;
  padding: 8px 24px;
}

.empty-state {
  grid-column: 1 / -1;
  padding: 40px;
}

.clear-all-container {
  grid-column: 1 / -1;
  display: flex;
  justify-content: center;
  padding: 20px 0;
  margin-top: 10px;
}

.clear-all-btn {
  color: #999;
  font-size: 14px;
  padding: 8px 16px;
}

.clear-all-btn:hover {
  color: #ff4d4f;
}

:deep(.el-tabs__item) {
  font-size: 14px;
}
</style>

