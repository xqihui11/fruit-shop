<template>
  <div class="coupon-banner">
    <el-carousel height="60px" :interval="5000" indicator-position="none" arrow="hover">
      <el-carousel-item v-for="coupon in coupons" :key="coupon.id">
        <div class="coupon-item" @click="goToCoupons">
          <el-icon class="coupon-icon"><Ticket /></el-icon>
          <span class="coupon-title">
            <span class="coupon-name">{{ coupon.name }}</span>
            <span class="coupon-badge" :class="coupon.merchantId ? 'merchant-badge' : 'platform-badge'">
              {{ coupon.merchantId ? '商家优惠券' : '平台优惠券' }}
            </span>
            <span class="coupon-value">
              <span v-if="coupon.type === 1">立减¥{{ formatMoney(coupon.discountAmount) }}</span>
              <span v-else>{{ (coupon.discountRate * 10).toFixed(1) }}折优惠</span>
            </span>
          </span>
          <el-link 
            type="primary" 
            :underline="false" 
            class="coupon-link"
            @click.stop="goToCoupons"
          >
            立即领取 →
          </el-link>
        </div>
      </el-carousel-item>
    </el-carousel>
  </div>
</template>

<script setup>
import { Ticket } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'

const props = defineProps({
  coupons: {
    type: Array,
    default: () => []
  }
})

const router = useRouter()

const formatMoney = (amount) => {
  if (!amount) return '0.00'
  return parseFloat(amount).toFixed(2)
}

const goToCoupons = () => {
  router.push('/coupons')
}
</script>

<style scoped>
.coupon-banner {
  background: #ffffff;
  color: #333;
  margin-bottom: 20px;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #e8e8e8;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.coupon-item {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 24px;
  height: 60px;
  gap: 12px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.coupon-item:hover {
  background-color: #f5f5f5;
}

.coupon-icon {
  font-size: 20px;
  color: #ff4d4f;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.1);
  }
}

.coupon-title {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.coupon-name {
  font-weight: 600;
  color: #333;
}

.coupon-badge {
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 600;
  text-transform: uppercase;
}

.platform-badge {
  background: #e6f7ff;
  color: #1890ff;
  border: 1px solid #91d5ff;
}

.merchant-badge {
  background: #f6ffed;
  color: #52c41a;
  border: 1px solid #b7eb8f;
}

.coupon-value {
  color: #ff4d4f;
  font-weight: 600;
  margin-left: 4px;
}

.coupon-link {
  color: #1890ff;
  font-size: 13px;
  font-weight: 600;
  transition: all 0.3s;
}

.coupon-link:hover {
  transform: translateX(4px);
  color: #40a9ff;
}
</style>

