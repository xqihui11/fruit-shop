<template>
  <div class="login-page">
    <div class="login-main">
      <!-- 左侧品牌展示区 -->
      <div class="brand-panel">
        <div class="brand-inner">
          <div class="brand-logo">
            <div class="brand-mark">🍎</div>
            <div class="brand-text">
              <h1 class="brand-title">飘香水果商城</h1>
              <p class="brand-subtitle">产地直供 · 新鲜直达</p>
            </div>
          </div>

          <p class="brand-slogan">每天一份新鲜水果，让生活更有味道。</p>

          <div class="brand-features">
            <div class="feature-card">
              <div class="feature-icon">🍉</div>
              <div class="feature-body">
                <div class="feature-title">产地直采</div>
                <div class="feature-desc">当季鲜果，从果园到餐桌</div>
              </div>
            </div>
            <div class="feature-card">
              <div class="feature-icon">🚚</div>
              <div class="feature-body">
                <div class="feature-title">极速配送</div>
                <div class="feature-desc">同城当日达，新鲜不等待</div>
              </div>
            </div>
            <div class="feature-card">
              <div class="feature-icon">🛡️</div>
              <div class="feature-body">
                <div class="feature-title">坏果包赔</div>
                <div class="feature-desc">全程售后保障，购物无忧</div>
              </div>
            </div>
            <div class="feature-card">
              <div class="feature-icon">💰</div>
              <div class="feature-body">
                <div class="feature-title">新人优惠</div>
                <div class="feature-desc">首单立减，专属福利</div>
              </div>
            </div>
          </div>
        </div>

        <!-- 左侧装饰水果 -->
        <div class="fruit-decor fruit-decor-1">🍓</div>
        <div class="fruit-decor fruit-decor-2">🍇</div>
        <div class="fruit-decor fruit-decor-3">🍊</div>
      </div>

      <!-- 右侧登录 / 注册区 -->
      <div class="auth-panel">
        <div class="login-box">
          <div class="login-header">
            <div class="login-header-main">
              <h2>欢迎登录</h2>
              <p>请选择登录身份开始您的新鲜之旅</p>
            </div>
            <el-button
              class="back-home-btn"
              link
              type="primary"
              @click="router.push('/')"
            >
              返回首页
            </el-button>
          </div>

          <!-- 身份选择 -->
          <div class="role-selector">
            <div
              class="role-item"
              :class="{ active: role === 'user' }"
              @click="role = 'user'"
            >
              <el-icon><User /></el-icon>
              <span>消费者</span>
            </div>
            <div
              class="role-item"
              :class="{ active: role === 'merchant' }"
              @click="role = 'merchant'"
            >
              <el-icon><Shop /></el-icon>
              <span>商家</span>
            </div>
            <div
              class="role-item"
              :class="{ active: role === 'admin' }"
              @click="role = 'admin'"
            >
              <el-icon><Setting /></el-icon>
              <span>管理员</span>
            </div>
          </div>

          <!-- 表单 -->
          <el-form :model="form" :rules="rules" ref="formRef" class="login-form">
            <el-form-item prop="username">
              <el-input
                v-model="form.username"
                placeholder="请输入用户名"
                size="large"
                prefix-icon="User"
              />
            </el-form-item>
            <el-form-item prop="password">
              <el-input
                v-model="form.password"
                type="password"
                placeholder="请输入密码"
                size="large"
                prefix-icon="Lock"
                show-password
                @keyup.enter="handleLogin"
              />
            </el-form-item>
            <el-form-item>
              <el-button
                type="primary"
                size="large"
                :loading="loading"
                @click="handleLogin"
                class="login-button"
              >
                登录
              </el-button>
            </el-form-item>
          </el-form>

          <!-- 底部链接 -->
          <div class="login-footer">
            <template v-if="role === 'user'">
              <span>还没有账号？</span>
              <el-link type="primary" @click="$router.push('/register')">
                立即注册
              </el-link>
            </template>
            <template v-else-if="role === 'merchant'">
              <span>还没有店铺？</span>
              <el-link
                type="primary"
                @click="$router.push('/merchant/register')"
              >
                申请入驻
              </el-link>
            </template>
            <template v-else>
              <span class="admin-tip">管理员账号请联系超级管理员获取</span>
            </template>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import api from '@/utils/api'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)
const role = ref('user') // 'user' | 'merchant' | 'admin'

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  try {
    await formRef.value.validate()
    loading.value = true

    let loginUrl = ''
    let redirectPath = ''

    if (role.value === 'user') {
      loginUrl = '/user/login'
      redirectPath = '/'
    } else if (role.value === 'merchant') {
      loginUrl = '/merchant/login'
      redirectPath = '/merchant'
    } else {
      loginUrl = '/admin/login'
      redirectPath = '/admin'
    }

    const res = await api.post(loginUrl, form)

    if (res.code === 200) {
      if (role.value === 'user') {
        localStorage.setItem('token', res.data.token)
        localStorage.setItem('user', JSON.stringify(res.data.user))
        userStore.token = res.data.token
        userStore.user = res.data.user
      } else if (role.value === 'merchant') {
        localStorage.setItem('merchant_token', res.data.token)
        localStorage.setItem('merchant_info', JSON.stringify(res.data.merchant))
      } else {
        localStorage.setItem('admin_token', res.data.token)
      }

      ElMessage.success('登录成功')
      router.push(redirectPath)
    } else {
      ElMessage.error(res.message || '登录失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      const errorMessage =
        error.response?.data?.message || error.message || '登录失败'
      ElMessage.error(errorMessage)
      console.error('登录错误:', error)
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: stretch;
  justify-content: center;
  background: #f5f7fa;
}

.login-main {
  width: 100vw;
  max-width: none;
  margin: 0;
  display: flex;
  gap: 0;
  border-radius: 0;
  overflow: hidden;
  box-shadow: none;
  background: #fff;
}

/* 左侧品牌展示区 */
.brand-panel {
  position: relative;
  flex: 3;
  min-height: 520px;
  background-image:
    linear-gradient(120deg, rgba(46, 204, 113, 0.35), rgba(255, 255, 255, 0.85)),
    url('@/assets/a57b2efda0ba4557edbbec582b6d725a.jpg');
  background-size: cover;
  background-position: center;
  overflow: hidden;
}

.brand-inner {
  position: relative;
  z-index: 1;
  height: 100%;
  padding: 40px 48px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  color: #103b28;
}

.brand-logo {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 18px;
}

.brand-mark {
  width: 52px;
  height: 52px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.92);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.brand-text {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.brand-title {
  font-size: 32px;
  color: #ffffff;
  text-shadow: 0 4px 12px rgba(0, 0, 0, 0.25);
  letter-spacing: 4px;
  margin: 0;
}

.brand-subtitle {
  font-size: 16px;
  color: #e8fff3;
}

.brand-slogan {
  font-size: 15px;
  color: #f2fff8;
  max-width: 340px;
  line-height: 1.7;
  margin-bottom: 32px;
}

/* 卖点卡片 */
.brand-features {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
  max-width: 420px;
}

.feature-card {
  background: rgba(255, 255, 255, 0.96);
  border-radius: 12px;
  padding: 10px 12px;
  display: flex;
  align-items: flex-start;
  gap: 10px;
  box-shadow: 0 10px 24px rgba(0, 0, 0, 0.12);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.feature-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 14px 35px rgba(0, 0, 0, 0.18);
}

.feature-icon {
  font-size: 20px;
}

.feature-body {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.feature-title {
  font-size: 14px;
  font-weight: 600;
  color: #1f5136;
}

.feature-desc {
  font-size: 12px;
  color: #6d8b7b;
}

/* 装饰水果 */
.fruit-decor {
  position: absolute;
  font-size: 32px;
  opacity: 0.25;
}

.fruit-decor-1 {
  top: 32px;
  right: 40px;
}

.fruit-decor-2 {
  bottom: 42px;
  right: 80px;
}

.fruit-decor-3 {
  bottom: 16px;
  left: 32px;
}

/* 右侧登录区 */
.auth-panel {
  flex: 2;
  background: linear-gradient(180deg, #ffffff 0%, #f9fffc 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 32px 40px;
}

.login-box {
  width: 100%;
  max-width: 420px;
  background: #ffffff;
  border-radius: 20px;
  box-shadow: 0 12px 40px rgba(12, 71, 36, 0.14);
  padding: 40px 40px 32px;
  border: 1px solid #e8f6ee;
}

/* 标题区 */
.login-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 24px;
}

.login-header-main {
  text-align: left;
}

.login-header h2 {
  font-size: 26px;
  color: #1f2933;
  margin-bottom: 4px;
}

.login-header p {
  color: #7b8794;
  font-size: 13px;
}

.back-home-btn {
  font-size: 13px;
  padding: 0 4px;
}

/* 角色选择 */
.role-selector {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
}

.role-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 14px 10px;
  border: 1px solid rgba(15, 108, 67, 0.16);
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s ease;
  color: #4b5563;
  background: #f9fffc;
}

.role-item:hover {
  border-color: #2ecc71;
  color: #1f5136;
  background: #f1fff7;
}

.role-item.active {
  border-color: #2ecc71;
  background: #e9fff3;
  color: #145a32;
  box-shadow: 0 6px 16px rgba(46, 204, 113, 0.25);
}

.role-item .el-icon {
  font-size: 22px;
}

.role-item span {
  font-size: 14px;
  font-weight: 500;
}

/* 表单与按钮 */
.login-form {
  margin-top: 20px;
}

.login-button {
  width: 100%;
  height: 44px;
  font-size: 16px;
  background: #2ecc71;
  border-color: #2ecc71;
  color: #ffffff;
  box-shadow: 0 10px 25px rgba(46, 204, 113, 0.35);
}

::deep(.el-button.login-button span) {
  color: #ffffff;
}

.login-button:hover {
  background: #27ae60;
  border-color: #27ae60;
}

.login-button:focus-visible {
  outline: 2px solid #145a32;
  outline-offset: 2px;
}

/* 底部链接 */
.login-footer {
  text-align: center;
  margin-top: 18px;
  color: #666;
  font-size: 14px;
}

.login-footer .el-link {
  margin-left: 6px;
  --el-link-text-color: #2ecc71;
  --el-link-hover-text-color: #27ae60;
}

.admin-tip {
  color: #999;
  font-size: 13px;
}

/* 输入框细节 */
::deep(.el-input__wrapper) {
  border-radius: 8px;
  box-shadow: none;
  border: 1px solid #e5e7eb;
  padding: 0 14px;
}

::deep(.el-input__wrapper.is-focus) {
  border-color: #2ecc71;
  box-shadow: 0 0 0 1px rgba(46, 204, 113, 0.2);
}

::deep(.el-input__inner::placeholder) {
  color: #9ca3af;
}

/* 响应式：移动端单列布局 */
@media (max-width: 960px) {
  .login-main {
    flex-direction: column;
    max-width: 100%;
    margin: 0;
    border-radius: 0;
    box-shadow: none;
  }

  .brand-panel {
    flex: none;
    min-height: 260px;
  }

  .brand-inner {
    padding: 28px 20px;
  }

  .auth-panel {
    padding: 24px 16px 32px;
  }

  .login-box {
    max-width: 480px;
    margin: 0 auto;
  }

  .brand-features {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>