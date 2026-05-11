<template>
  <div class="contact-page">
    <div class="contact-hero">
      <h1>联系我们</h1>
      <p class="subtitle">我们随时为您服务</p>
    </div>

    <div class="contact-content">
      <el-row :gutter="24">
        <el-col :xs="24" :md="8">
          <el-card class="contact-card">
            <div class="card-icon">
              <el-icon><Phone /></el-icon>
            </div>
            <h3>客服热线</h3>
            <p class="contact-value">400-888-8888</p>
            <p class="contact-desc">工作日：9:00 - 21:00</p>
            <p class="contact-desc">节假日：10:00 - 18:00</p>
            <el-button type="primary" @click="callPhone">立即拨打</el-button>
          </el-card>
        </el-col>

        <el-col :xs="24" :md="8">
          <el-card class="contact-card">
            <div class="card-icon">
              <el-icon><Message /></el-icon>
            </div>
            <h3>客服邮箱</h3>
            <p class="contact-value">service@fruit.com</p>
            <p class="contact-desc">我们会在24小时内回复您的邮件</p>
            <el-button type="primary" @click="sendEmail">发送邮件</el-button>
          </el-card>
        </el-col>

        <el-col :xs="24" :md="8">
          <el-card class="contact-card">
            <div class="card-icon">
              <el-icon><Location /></el-icon>
            </div>
            <h3>公司地址</h3>
            <p class="contact-value">北京市朝阳区</p>
            <p class="contact-desc">水果商城总部</p>
            <p class="contact-desc">欢迎预约参观</p>
            <el-button type="primary" @click="viewMap">查看地图</el-button>
          </el-card>
        </el-col>
      </el-row>

      <el-card class="form-card">
        <template #header>
          <div class="card-header">
            <el-icon><Edit /></el-icon>
            <span>在线留言</span>
          </div>
        </template>
        <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
          <el-row :gutter="20">
            <el-col :xs="24" :md="12">
              <el-form-item label="姓名" prop="name">
                <el-input v-model="form.name" placeholder="请输入您的姓名" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :md="12">
              <el-form-item label="联系电话" prop="phone">
                <el-input v-model="form.phone" placeholder="请输入您的联系电话" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="form.email" placeholder="请输入您的邮箱（选填）" />
          </el-form-item>
          <el-form-item label="留言类型" prop="type">
            <el-select v-model="form.type" placeholder="请选择留言类型" style="width: 100%">
              <el-option label="咨询问题" value="咨询问题" />
              <el-option label="意见建议" value="意见建议" />
              <el-option label="投诉反馈" value="投诉反馈" />
              <el-option label="合作洽谈" value="合作洽谈" />
              <el-option label="其他" value="其他" />
            </el-select>
          </el-form-item>
          <el-form-item label="留言内容" prop="content">
            <el-input
              v-model="form.content"
              type="textarea"
              :rows="6"
              placeholder="请输入您的留言内容，我们会尽快回复您"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" size="large" @click="submitForm" :loading="loading">
              提交留言
            </el-button>
            <el-button size="large" @click="resetForm">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <el-card class="hours-card">
        <template #header>
          <div class="card-header">
            <el-icon><Clock /></el-icon>
            <span>服务时间</span>
          </div>
        </template>
        <div class="hours-content">
          <div class="hours-item">
            <div class="hours-day">工作日</div>
            <div class="hours-time">9:00 - 21:00</div>
          </div>
          <div class="hours-item">
            <div class="hours-day">周末</div>
            <div class="hours-time">10:00 - 18:00</div>
          </div>
          <div class="hours-item">
            <div class="hours-day">节假日</div>
            <div class="hours-time">10:00 - 18:00</div>
          </div>
        </div>
        <el-alert
          title="温馨提示"
          type="info"
          :closable="false"
          show-icon
          style="margin-top: 20px;"
        >
          <template #default>
            <p>非服务时间提交的留言，我们会在下一个工作日及时处理并回复。</p>
          </template>
        </el-alert>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { Phone, Message, Location, Edit, Clock } from '@element-plus/icons-vue'
import api from '@/utils/api'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref()
const loading = ref(false)

const form = reactive({
  name: '',
  phone: '',
  email: '',
  type: '',
  content: ''
})

const rules = {
  name: [{ required: true, message: '请输入您的姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  type: [{ required: true, message: '请选择留言类型', trigger: 'change' }],
  content: [{ required: true, message: '请输入留言内容', trigger: 'blur' }]
}

const callPhone = () => {
  window.location.href = 'tel:400-888-8888'
}

const sendEmail = () => {
  window.location.href = 'mailto:service@fruit.com'
}

const viewMap = () => {
  ElMessage.info('地图功能开发中，敬请期待')
}

const submitForm = async () => {
  if (!userStore.user || !userStore.token) {
    ElMessage.warning('请先登录后再提交在线留言')
    router.push({ path: '/login', query: { redirect: '/contact' } })
    return
  }
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const res = await api.post('/contact/message', form)
        if (res.code === 200) {
          ElMessage.success('留言提交成功！我们会尽快与您联系')
          resetForm()
        } else {
          ElMessage.error(res.message || '提交失败')
        }
      } catch (error) {
        ElMessage.error('提交失败，请稍后重试')
      } finally {
        loading.value = false
      }
    }
  })
}

const resetForm = () => {
  formRef.value?.resetFields()
}
</script>

<style scoped>
.contact-page {
  min-height: calc(100vh - 200px);
}

.contact-hero {
  text-align: center;
  padding: 60px 20px;
  background: #ffffff;
  color: #2c3e50;
  margin-bottom: 40px;
  border-radius: 12px;
  border: 1px solid #e8e8e8;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.contact-hero h1 {
  font-size: 42px;
  margin-bottom: 16px;
  font-weight: 700;
}

.contact-hero .subtitle {
  font-size: 20px;
  opacity: 0.9;
}

.contact-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px 40px;
}

.contact-card {
  height: 100%;
  text-align: center;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transition: transform 0.3s, box-shadow 0.3s;
}

.contact-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.12);
}

.card-icon {
  margin-bottom: 20px;
}

.card-icon .el-icon {
  font-size: 64px;
  color: #409eff;
}

.contact-card h3 {
  margin-bottom: 16px;
  color: #303133;
  font-size: 20px;
}

.contact-value {
  font-size: 24px;
  font-weight: 600;
  color: #409eff;
  margin: 12px 0;
}

.contact-desc {
  color: #606266;
  font-size: 14px;
  margin: 8px 0;
  line-height: 1.6;
}

.contact-card .el-button {
  margin-top: 16px;
  width: 100%;
}

.form-card,
.hours-card {
  margin-top: 24px;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.card-header .el-icon {
  font-size: 20px;
  color: #409eff;
}

.hours-content {
  display: flex;
  justify-content: space-around;
  gap: 24px;
  flex-wrap: wrap;
}

.hours-item {
  text-align: center;
  padding: 24px;
  background: linear-gradient(135deg, #f5f7fa 0%, #ffffff 100%);
  border-radius: 8px;
  min-width: 200px;
  flex: 1;
}

.hours-day {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
}

.hours-time {
  font-size: 24px;
  color: #409eff;
  font-weight: 600;
}

@media (max-width: 768px) {
  .contact-hero h1 {
    font-size: 32px;
  }

  .contact-hero .subtitle {
    font-size: 16px;
  }

  .hours-content {
    flex-direction: column;
  }

  .hours-item {
    width: 100%;
  }
}
</style>

