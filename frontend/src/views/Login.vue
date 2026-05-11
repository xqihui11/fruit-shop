<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <h2>欢迎登录</h2>
        <p>飘香水果商城</p>
      </div>
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
      <div class="login-footer">
        <span>还没有账号？</span>
        <el-link type="primary" @click="$router.push('/register')">立即注册</el-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await userStore.login(form.username, form.password)
        ElMessage.success('登录成功')
        router.push('/')
      } catch (error) {
        ElMessage.error(error.message || '登录失败')
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  position: relative;
  overflow: hidden;
  background-color: #ffffff;
  background-image: url('@/assets/a57b2efda0ba4557edbbec582b6d725a.jpg');
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
}

/* 背景图片遮罩层 - 让图片更柔和，不抢夺表单焦点 */
.login-container::before {
  content: '';
  position: absolute;
  inset: 0;
  background: rgba(255, 255, 255, 0.75);
  backdrop-filter: blur(2px);
  z-index: 0;
}

.login-box {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 400px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12), 0 2px 8px rgba(0, 0, 0, 0.08);
  padding: 40px 40px 32px;
  backdrop-filter: blur(8px);
  border: 1px solid rgba(255, 255, 255, 0.8);
}

.login-header {
  text-align: center;
  margin-bottom: 28px;
}

.login-header h2 {
  font-size: 28px;
  color: #111;
  margin-bottom: 6px;
}

.login-header p {
  color: #555;
  font-size: 14px;
}

.login-form {
  margin-top: 20px;
}

.login-button {
  width: 100%;
  height: 44px;
  font-size: 16px;
  background: #ffffff;
  color: #000000;
  border: 1px solid rgba(0, 0, 0, 0.15);
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.12);
}

:deep(.el-button.login-button span) {
  color: #000000;
}

.login-button:hover {
  background: #f5f5f5;
  color: #000000;
}

.login-button:focus-visible {
  outline: 2px solid #000000;
  outline-offset: 2px;
}

.login-footer {
  text-align: center;
  margin-top: 18px;
  color: #666;
  font-size: 14px;
}

.login-footer .el-link {
  margin-left: 6px;
  --el-link-text-color: #000;
  --el-link-hover-text-color: #333;
}
</style>

