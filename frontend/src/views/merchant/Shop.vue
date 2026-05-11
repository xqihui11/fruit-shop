<template>
  <div class="shop-page">
    <div class="shop-settings-container">
      <!-- 页面标题 -->
      <div class="page-header">
        <h2 class="page-title">店铺设置</h2>
        <div class="title-divider"></div>
      </div>

      <!-- 表单卡片 -->
      <div class="form-card">
        <el-form 
          ref="formRef"
          :model="form" 
          :rules="formRules"
          label-width="100px"
          class="shop-form"
          @keydown.enter.prevent="handleEnterKey"
        >
          <!-- 头像区域 -->
          <el-form-item label="头像">
            <div class="avatar-section">
              <el-avatar :size="80" :src="getAvatarUrl(form.avatar)" class="shop-avatar">
                <span>{{ form.shopName?.charAt(0) || '商' }}</span>
              </el-avatar>
              <el-upload
                class="avatar-uploader"
                :action="uploadAction"
                :show-file-list="false"
                :on-success="handleAvatarSuccess"
                :before-upload="beforeAvatarUpload"
                :headers="uploadHeaders"
                :drag="true"
              >
                <el-button type="primary" class="change-avatar-btn">
                  <el-icon><Upload /></el-icon>
                  更换头像
                </el-button>
              </el-upload>
            </div>
          </el-form-item>

          <!-- 店铺名称 -->
          <el-form-item label="店铺名称" prop="shopName">
            <el-input 
              v-model="form.shopName" 
              placeholder="请输入店铺名称"
              class="form-input"
              clearable
            >
              <template #prefix>
                <el-icon class="input-icon"><Shop /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <!-- 店铺描述 -->
          <el-form-item label="店铺描述" prop="shopDescription">
            <div class="textarea-wrapper">
              <el-input 
                v-model="form.shopDescription" 
                type="textarea" 
                :rows="4" 
                placeholder="请输入店铺描述"
                class="form-textarea"
                maxlength="200"
                show-word-limit
              />
            </div>
          </el-form-item>

          <!-- 店铺地址 -->
          <el-form-item label="店铺地址" prop="shopAddress">
            <el-input 
              v-model="form.shopAddress" 
              placeholder="请输入店铺地址"
              class="form-input"
              clearable
            >
              <template #prefix>
                <el-icon class="input-icon"><Location /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <!-- 营业时间 -->
          <el-form-item label="营业时间" prop="businessHours">
            <el-input 
              v-model="form.businessHours" 
              placeholder="例如：09:00-21:00"
              class="form-input"
              clearable
            >
              <template #prefix>
                <el-icon class="input-icon"><Clock /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <!-- 营业状态 -->
          <el-form-item label="营业状态" prop="businessStatus">
            <el-select v-model="form.businessStatus" class="form-input">
              <el-option :value="0" label="按营业时间自动判断" />
              <el-option :value="1" label="强制营业中" />
              <el-option :value="2" label="强制休息中" />
            </el-select>
          </el-form-item>

          <!-- 联系人 -->
          <el-form-item label="联系人" prop="contactName">
            <el-input 
              v-model="form.contactName" 
              placeholder="请输入联系人姓名"
              class="form-input"
              clearable
            >
              <template #prefix>
                <el-icon class="input-icon"><User /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <!-- 联系电话 -->
          <el-form-item label="联系电话" prop="contactPhone" :error="phoneError">
            <el-input 
              v-model="form.contactPhone" 
              placeholder="请输入联系电话"
              class="form-input"
              clearable
              @blur="validatePhoneField"
              @input="clearPhoneError"
            >
              <template #prefix>
                <el-icon class="input-icon"><Phone /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <!-- 联系邮箱 -->
          <el-form-item label="联系邮箱" prop="contactEmail" :error="emailError">
            <el-input 
              v-model="form.contactEmail" 
              placeholder="请输入联系邮箱"
              class="form-input"
              clearable
              @blur="validateEmailField"
              @input="clearEmailError"
            >
              <template #prefix>
                <el-icon class="input-icon"><Message /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <!-- 保存按钮 -->
          <el-form-item>
            <el-button 
              type="primary" 
              class="save-btn"
              @click="saveShop" 
              :loading="saving"
              :disabled="!form.shopName || !form.shopName.trim()"
            >
              <el-icon v-if="!saving"><Check /></el-icon>
              {{ saving ? '保存中...' : '保存' }}
            </el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch, onBeforeUnmount } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Shop,
  Location,
  Clock,
  User,
  Phone,
  Message,
  Upload,
  Check
} from '@element-plus/icons-vue'
import api from '@/utils/api'

const formRef = ref(null)
const form = ref({
  avatar: '',
  shopName: '',
  shopDescription: '',
  shopAddress: '',
  businessHours: '',
  businessStatus: 0,
  contactName: '',
  contactPhone: '',
  contactEmail: ''
})
const saving = ref(false)
const phoneError = ref('')
const emailError = ref('')
const hasUnsavedChanges = ref(false)

// 表单校验规则
const validatePhone = (rule, value, callback) => {
  if (!value) {
    callback()
    return
  }
  const phoneReg = /^1[3-9]\d{9}$/
  if (!phoneReg.test(value)) {
    phoneError.value = '请输入正确的手机号'
    callback(new Error('请输入正确的手机号'))
  } else {
    phoneError.value = ''
    callback()
  }
}

const validateEmail = (rule, value, callback) => {
  if (!value) {
    callback()
    return
  }
  const emailReg = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailReg.test(value)) {
    emailError.value = '请输入正确的邮箱地址'
    callback(new Error('请输入正确的邮箱地址'))
  } else {
    emailError.value = ''
    callback()
  }
}

const formRules = {
  shopName: [
    { required: true, message: '店铺名称不能为空', trigger: 'blur' }
  ],
  contactPhone: [
    { validator: validatePhone, trigger: 'blur' }
  ],
  contactEmail: [
    { validator: validateEmail, trigger: 'blur' }
  ]
}

// 手动验证电话号码字段
const validatePhoneField = () => {
  if (!form.value.contactPhone) {
    phoneError.value = ''
    return
  }
  const phoneReg = /^1[3-9]\d{9}$/
  if (!phoneReg.test(form.value.contactPhone)) {
    phoneError.value = '请输入正确的手机号'
  } else {
    phoneError.value = ''
  }
}

// 手动验证邮箱字段
const validateEmailField = () => {
  if (!form.value.contactEmail) {
    emailError.value = ''
    return
  }
  const emailReg = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailReg.test(form.value.contactEmail)) {
    emailError.value = '请输入正确的邮箱地址'
  } else {
    emailError.value = ''
  }
}

// 清除电话号码错误（输入时）
const clearPhoneError = () => {
  if (phoneError.value) {
    phoneError.value = ''
  }
}

// 清除邮箱错误（输入时）
const clearEmailError = () => {
  if (emailError.value) {
    emailError.value = ''
  }
}

// 监听表单变化
const originalForm = ref({})
const watchFormChanges = () => {
  const currentForm = JSON.stringify(form.value)
  const originalFormStr = JSON.stringify(originalForm.value)
  hasUnsavedChanges.value = currentForm !== originalFormStr
}

// Enter键处理
const handleEnterKey = (e) => {
  if (e.target.tagName !== 'TEXTAREA') {
    saveShop()
  }
}

const loadShopInfo = async () => {
  try {
    const res = await api.get('/merchant/info')
    if (res.code === 200 && res.data) {
      form.value = {
        avatar: res.data.avatar || '',
        shopName: res.data.shopName || '',
        shopDescription: res.data.shopDescription || '',
        shopAddress: res.data.shopAddress || '',
        businessHours: res.data.businessHours || '',
        businessStatus: res.data.businessStatus ?? 0,
        contactName: res.data.contactName || '',
        contactPhone: res.data.contactPhone || '',
        contactEmail: res.data.contactEmail || ''
      }
      // 保存原始数据用于检测变化
      originalForm.value = JSON.parse(JSON.stringify(form.value))
      hasUnsavedChanges.value = false
      
      // 同时更新本地存储
      const merchantInfo = JSON.parse(localStorage.getItem('merchant_info') || '{}')
      Object.assign(merchantInfo, res.data)
      localStorage.setItem('merchant_info', JSON.stringify(merchantInfo))
    }
  } catch (error) {
    console.error('加载店铺信息失败', error)
    ElMessage.error('加载店铺信息失败')
  }
}

const saveShop = async () => {
  // 表单校验
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
  } catch (error) {
    ElMessage.warning('请检查表单输入')
    return
  }

  // 检查店铺名称
  if (!form.value.shopName || !form.value.shopName.trim()) {
    ElMessage.warning('店铺名称不能为空')
    return
  }

  saving.value = true
  try {
    const res = await api.put('/merchant/update', form.value)
    if (res.code === 200) {
      ElMessage.success('店铺信息已更新')
      // 重新加载最新信息并更新本地存储
      await loadShopInfo()
      hasUnsavedChanges.value = false
    } else {
      ElMessage.error(res.message || '保存失败')
    }
  } catch (error) {
    ElMessage.error('保存失败，请稍后重试')
  } finally {
    saving.value = false
  }
}

// 头像上传相关
const merchantToken = localStorage.getItem('merchant_token')
const uploadAction = '/api/file/upload'
const uploadHeaders = {
  Authorization: `Bearer ${merchantToken}`
}

const getAvatarUrl = (avatar) => {
  if (!avatar || avatar.trim() === '') return null
  const trimmedAvatar = avatar.trim()
  if (trimmedAvatar.startsWith('http://') || trimmedAvatar.startsWith('https://')) {
    return trimmedAvatar
  }
  if (trimmedAvatar.startsWith('/api/uploads/')) {
    return trimmedAvatar
  }
  if (trimmedAvatar.startsWith('/uploads/')) {
    return '/api' + trimmedAvatar
  }
  if (!trimmedAvatar.startsWith('/')) {
    return '/api/uploads/' + trimmedAvatar
  }
  return trimmedAvatar
}

const beforeAvatarUpload = (file) => {
  const MAX_IMAGE_SIZE_MB = 5
  const isImage = file.type.startsWith('image/')
  const isLtLimit = file.size / 1024 / 1024 < MAX_IMAGE_SIZE_MB

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLtLimit) {
    ElMessage.error(`图片大小不能超过 ${MAX_IMAGE_SIZE_MB}MB!`)
    return false
  }
  return true
}

const handleAvatarSuccess = (response) => {
  if (response.code === 200 && response.data) {
    const avatarUrl = response.data.url || response.data
    form.value.avatar = avatarUrl
    hasUnsavedChanges.value = true
    // 立即更新本地存储
    const merchantInfo = JSON.parse(localStorage.getItem('merchant_info') || '{}')
    merchantInfo.avatar = avatarUrl
    localStorage.setItem('merchant_info', JSON.stringify(merchantInfo))
    ElMessage.success('头像上传成功，请点击保存按钮保存')
  } else {
    ElMessage.error(response.message || '头像上传失败')
  }
}

// 监听表单字段变化
watch(() => form.value, () => {
  watchFormChanges()
}, { deep: true })

// 页面离开前确认（浏览器刷新/关闭）
const handleBeforeUnload = (e) => {
  if (hasUnsavedChanges.value) {
    e.preventDefault()
    e.returnValue = '您有未保存的更改，确定要离开吗？'
    return e.returnValue
  }
}

onMounted(() => {
  loadShopInfo()
  window.addEventListener('beforeunload', handleBeforeUnload)
})

onBeforeUnmount(() => {
  window.removeEventListener('beforeunload', handleBeforeUnload)
})
</script>

<style scoped>
.shop-page {
  min-height: calc(100vh - 60px);
  background: #f5f7fa;
  padding: 24px;
}

.shop-settings-container {
  max-width: 800px;
  margin: 0 auto;
}

/* 页面标题 */
.page-header {
  margin-bottom: 24px;
}

.page-title {
  font-size: 18px;
  font-weight: bold;
  color: #2c3e50;
  margin: 0 0 12px 0;
}

.title-divider {
  height: 1px;
  background: #e0e0e0;
  width: 100%;
}

/* 表单卡片 */
.form-card {
  background: #ffffff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.08);
  padding: 32px;
}

.shop-form {
  max-width: 600px;
}

/* 头像区域 */
.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.shop-avatar {
  border: 2px solid #e0e0e0;
}

.change-avatar-btn {
  background: #2ecc71;
  border-color: #2ecc71;
  color: #ffffff;
  padding: 8px 16px;
  border-radius: 4px;
  font-size: 14px;
  transition: all 0.3s;
}

.change-avatar-btn:hover {
  background: #27ae60;
  border-color: #27ae60;
}

.avatar-uploader {
  margin-top: 0;
}

/* 表单输入框样式 */
:deep(.el-form-item__label) {
  color: #2c3e50;
  font-weight: bold;
  font-size: 14px;
  text-align: right;
  padding-right: 12px;
}

.form-input {
  height: 40px;
}

:deep(.el-input__inner) {
  height: 40px;
  padding: 8px 12px;
  border-radius: 4px;
  border: 1px solid #e0e0e0;
  color: #34495e;
  transition: all 0.3s;
}

:deep(.el-input__inner:focus) {
  border-color: #2ecc71;
  box-shadow: 0 0 0 2px rgba(46, 204, 113, 0.1);
}

:deep(.el-input__prefix) {
  left: 12px;
}

.input-icon {
  color: #bdc3c7;
  font-size: 16px;
}

.form-textarea {
  border-radius: 4px;
}

:deep(.el-textarea__inner) {
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  padding: 8px 12px;
  color: #34495e;
  transition: all 0.3s;
  font-family: inherit;
}

:deep(.el-textarea__inner:focus) {
  border-color: #2ecc71;
  box-shadow: 0 0 0 2px rgba(46, 204, 113, 0.1);
}

:deep(.el-input__count) {
  color: #bdc3c7;
  font-size: 12px;
}

.textarea-wrapper {
  width: 100%;
}

/* 错误提示 */
.error-message {
  color: #e74c3c;
  font-size: 12px;
  margin-top: 4px;
  line-height: 1.5;
}

/* 保存按钮 */
.save-btn {
  width: 100%;
  height: 44px;
  background: #2ecc71;
  border-color: #2ecc71;
  color: #ffffff;
  font-size: 16px;
  font-weight: 500;
  border-radius: 4px;
  transition: all 0.3s;
}

.save-btn:hover:not(:disabled) {
  background: #27ae60;
  border-color: #27ae60;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(46, 204, 113, 0.3);
}

.save-btn:disabled {
  background: #bdc3c7;
  border-color: #bdc3c7;
  cursor: not-allowed;
}

.save-btn:active:not(:disabled) {
  transform: translateY(0);
}

/* 表单项间距 */
:deep(.el-form-item) {
  margin-bottom: 24px;
}

:deep(.el-form-item:last-child) {
  margin-bottom: 0;
}

/* 清除按钮样式 */
:deep(.el-input__suffix) {
  right: 8px;
}

:deep(.el-input__clear) {
  color: #bdc3c7;
}

:deep(.el-input__clear:hover) {
  color: #2ecc71;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .shop-page {
    padding: 16px;
  }

  .form-card {
    padding: 20px;
  }

  .shop-form {
    max-width: 100%;
  }

  :deep(.el-form-item__label) {
    text-align: left;
    padding-bottom: 8px;
  }

  .page-title {
    font-size: 16px;
  }
}

/* 上传组件样式 */
:deep(.el-upload) {
  width: 100%;
}

:deep(.el-upload-dragger) {
  width: 100%;
  border: 1px dashed #e0e0e0;
  border-radius: 4px;
  background: #fafafa;
  transition: all 0.3s;
}

:deep(.el-upload-dragger:hover) {
  border-color: #2ecc71;
  background: #f0f9f4;
}
</style>

