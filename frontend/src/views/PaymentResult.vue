<template>
  <div class="payment-result-page">
    <el-card class="result-card" shadow="hover">
      <div class="result-icon" :class="statusClass">
        <el-icon v-if="status === 'success'" class="icon-success">
          <CircleCheckFilled />
        </el-icon>
        <el-icon v-else class="icon-fail">
          <CircleCloseFilled />
        </el-icon>
      </div>
      <h2 class="result-title">
        {{ status === 'success' ? '支付成功' : '支付失败' }}
      </h2>
      <p class="result-subtitle">
        {{ message }}
      </p>

      <el-descriptions class="result-info" :column="1" border>
        <el-descriptions-item label="订单号">
          {{ orderNo || '—' }}
        </el-descriptions-item>
        <el-descriptions-item label="订单金额">
          <span class="amount" v-if="amount">¥{{ Number(amount).toFixed(2) }}</span>
          <span v-else>—</span>
        </el-descriptions-item>
      </el-descriptions>

      <div class="result-actions">
        <el-button type="primary" @click="goOrder">
          查看订单
        </el-button>
        <el-button @click="goHome">
          返回首页
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { CircleCheckFilled, CircleCloseFilled } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()

const status = computed(() => route.query.status === 'fail' ? 'fail' : 'success')
const orderNo = computed(() => route.query.orderNo || '')
const amount = computed(() => route.query.amount || '')
const orderId = computed(() => route.query.orderId || '')
const message = computed(() => {
  if (route.query.message) return route.query.message
  return status.value === 'success' ? '您的订单已支付成功，商家会尽快为您发货。' : '支付未成功，您可以返回订单页重新发起支付或更换支付方式。'
})

const statusClass = computed(() => status.value === 'success' ? 'success' : 'fail')

const goOrder = () => {
  const query = {}
  if (orderId.value) {
    query.orderId = orderId.value
  }
  router.push({ path: '/order', query })
}

const goHome = () => {
  router.push('/')
}
</script>

<style scoped>
.payment-result-page {
  max-width: 600px;
  margin: 40px auto;
}

.result-card {
  text-align: center;
  padding: 24px 32px;
}

.result-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 80px;
  height: 80px;
  border-radius: 50%;
  margin-bottom: 16px;
}

.result-icon.success {
  background: #f6ffed;
  color: #52c41a;
}

.result-icon.fail {
  background: #fff1f0;
  color: #ff4d4f;
}

.icon-success,
.icon-fail {
  font-size: 40px;
}

.result-title {
  margin: 0 0 8px;
  font-size: 22px;
  color: #262626;
}

.result-subtitle {
  margin: 0 0 16px;
  color: #8c8c8c;
}

.result-info {
  margin: 16px 0 24px;
  text-align: left;
}

.amount {
  color: #ff4d4f;
  font-weight: 600;
}

.result-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 8px;
}
</style>


