<template>
  <div class="login-page">
    <div class="login-main">
      <!-- 左侧品牌展示区，与登录页保持一致 -->
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

      <!-- 右侧商家入驻申请表单 -->
      <div class="auth-panel">
        <div class="login-box">
          <div class="login-header">
            <h2>商家入驻申请</h2>
            <p>完善店铺信息，开启您的线上水果店</p>
          </div>

          <el-form
            :model="form"
            :rules="rules"
            ref="formRef"
            label-width="90px"
            class="login-form"
          >
            <el-form-item label="用户名" prop="username">
              <el-input v-model="form.username" placeholder="请输入用户名" />
            </el-form-item>
            <el-form-item label="密码" prop="password">
              <el-input
                v-model="form.password"
                type="password"
                placeholder="请输入密码"
                show-password
              />
            </el-form-item>
            <el-form-item label="店铺名称" prop="shopName">
              <el-input v-model="form.shopName" placeholder="请输入店铺名称" />
            </el-form-item>
            <el-form-item label="店铺描述">
              <el-input
                v-model="form.shopDescription"
                type="textarea"
                :rows="3"
                placeholder="请输入店铺描述"
              />
            </el-form-item>
            <el-form-item label="联系人" prop="contactName">
              <el-input v-model="form.contactName" placeholder="请输入联系人姓名" />
            </el-form-item>
            <el-form-item label="联系电话" prop="contactPhone">
              <el-input v-model="form.contactPhone" placeholder="请输入联系电话" />
            </el-form-item>
            <el-form-item label="联系邮箱">
              <el-input v-model="form.contactEmail" placeholder="请输入联系邮箱" />
            </el-form-item>
            <el-form-item label="营业执照号">
              <el-input
                v-model="form.businessLicense"
                placeholder="请输入营业执照号"
              />
            </el-form-item>
            <el-form-item>
              <el-button
                type="primary"
                @click="handleRegister"
                :loading="loading"
                class="login-button"
              >
                提交申请
              </el-button>
            </el-form-item>
          </el-form>

          <div class="login-footer">
            <router-link to="/merchant/login">已有账号？去登录</router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from '@/utils/api'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

const form = ref({
  username: '',
  password: '',
  shopName: '',
  shopDescription: '',
  contactName: '',
  contactPhone: '',
  contactEmail: '',
  businessLicense: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ],
  shopName: [{ required: true, message: '请输入店铺名称', trigger: 'blur' }],
  contactName: [{ required: true, message: '请输入联系人姓名', trigger: 'blur' }],
  contactPhone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }]
}

const handleRegister = async () => {
  try {
    await formRef.value.validate()
    loading.value = true
    const res = await api.post('/merchant/register', form.value)
    if (res.code === 200) {
      ElMessage.success('申请提交成功，请等待审核')
      router.push('/merchant/login')
    } else {
      ElMessage.error(res.message || '提交失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('提交失败')
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

/* 右侧表单区 */
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
  max-width: 520px;
  background: #ffffff;
  border-radius: 20px;
  box-shadow: 0 12px 40px rgba(12, 71, 36, 0.14);
  padding: 40px 40px 32px;
  border: 1px solid #e8f6ee;
}

/* 标题区 */
.login-header {
  text-align: left;
  margin-bottom: 24px;
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

/* 表单与按钮 */
.login-form {
  margin-top: 12px;
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

.login-footer a {
  color: #2ecc71;
  text-decoration: none;
}

.login-footer a:hover {
  color: #27ae60;
}

/* 表单细节 */
::deep(.el-input__wrapper),
::deep(.el-textarea__inner) {
  border-radius: 8px;
  box-shadow: none;
  border: 1px solid #e5e7eb;
}

::deep(.el-input__wrapper.is-focus),
::deep(.el-textarea__inner:focus) {
  border-color: #2ecc71;
  box-shadow: 0 0 0 1px rgba(46, 204, 113, 0.2);
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
    max-width: 100%;
    margin: 0 auto;
  }

  .brand-features {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>


