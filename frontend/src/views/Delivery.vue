<template>
  <div class="delivery-page">
    <div class="delivery-hero">
      <h1>配送说明</h1>
      <p class="subtitle">快速、安全、便捷的配送服务</p>
    </div>

    <div class="delivery-content">
      <el-row :gutter="24">
        <el-col :xs="24" :md="8">
          <el-card class="delivery-card">
            <div class="card-icon">
              <el-icon><Box /></el-icon>
            </div>
            <h3>配送范围</h3>
            <p>我们目前支持全国主要城市的配送服务，包括：</p>
            <ul>
              <li>一线城市：北京、上海、广州、深圳</li>
              <li>新一线城市：杭州、成都、武汉、西安等</li>
              <li>其他城市：逐步扩展中</li>
            </ul>
            <p class="note">注：偏远地区配送时间可能延长，具体以实际配送为准</p>
          </el-card>
        </el-col>

        <el-col :xs="24" :md="8">
          <el-card class="delivery-card">
            <div class="card-icon">
              <el-icon><Clock /></el-icon>
            </div>
            <h3>配送时间</h3>
            <div class="time-schedule">
              <div class="time-item">
                <strong>工作日：</strong>
                <span>当日下单，次日送达（部分区域支持当日达）</span>
              </div>
              <div class="time-item">
                <strong>周末：</strong>
                <span>正常配送，不休息</span>
              </div>
              <div class="time-item">
                <strong>节假日：</strong>
                <span>部分区域可能延迟，请提前下单</span>
              </div>
            </div>
            <p class="highlight">配送时间：每日 9:00 - 21:00</p>
          </el-card>
        </el-col>

        <el-col :xs="24" :md="8">
          <el-card class="delivery-card">
            <div class="card-icon">
              <el-icon><Money /></el-icon>
            </div>
            <h3>配送费用</h3>
            <div class="fee-table">
              <div class="fee-row">
                <span class="fee-amount">满99元</span>
                <span class="fee-desc">免运费</span>
              </div>
              <div class="fee-row">
                <span class="fee-amount">不满99元</span>
                <span class="fee-desc">运费10元</span>
              </div>
              <div class="fee-row highlight">
                <span class="fee-amount">新用户首单</span>
                <span class="fee-desc">免运费</span>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-card class="process-card">
        <template #header>
          <div class="card-header">
            <el-icon><List /></el-icon>
            <span>配送流程</span>
          </div>
        </template>
        <el-steps :active="4" finish-status="success" align-center>
          <el-step title="下单" description="选择商品并完成支付" />
          <el-step title="确认" description="系统确认订单信息" />
          <el-step title="发货" description="仓库打包并安排配送" />
          <el-step title="配送" description="快递员送货上门" />
          <el-step title="签收" description="确认收货完成订单" />
        </el-steps>
      </el-card>

      <el-card class="notice-card">
        <template #header>
          <div class="card-header">
            <el-icon><InfoFilled /></el-icon>
            <span>配送须知</span>
          </div>
        </template>
        <el-alert
          title="重要提示"
          type="info"
          :closable="false"
          show-icon
          style="margin-bottom: 20px;"
        >
          <template #default>
            <div class="alert-content">
              <p>1. 请确保收货地址准确，联系电话畅通</p>
              <p>2. 配送前会提前电话联系，请保持手机畅通</p>
              <p>3. 如遇特殊情况无法收货，请及时联系客服</p>
              <p>4. 收到商品后请及时检查，如有问题请立即联系客服</p>
            </div>
          </template>
        </el-alert>

        <div class="notice-list">
          <div class="notice-item">
            <el-icon class="notice-icon"><Warning /></el-icon>
            <div>
              <h4>特殊天气</h4>
              <p>如遇恶劣天气（暴雨、暴雪等），配送时间可能延迟，我们会及时通知您</p>
            </div>
          </div>
          <div class="notice-item">
            <el-icon class="notice-icon"><Location /></el-icon>
            <div>
              <h4>地址变更</h4>
              <p>如需修改收货地址，请在发货前联系客服，发货后无法修改</p>
            </div>
          </div>
          <div class="notice-item">
            <el-icon class="notice-icon"><Lock /></el-icon>
            <div>
              <h4>代收服务</h4>
              <p>支持代收服务，但需提前告知代收人信息，并确保代收人能够及时收货</p>
            </div>
          </div>
        </div>
      </el-card>

      <el-card class="contact-card">
        <template #header>
          <div class="card-header">
            <el-icon><Phone /></el-icon>
            <span>配送问题咨询</span>
          </div>
        </template>
        <div class="contact-info">
          <p>如对配送服务有任何疑问，欢迎联系我们的客服团队：</p>
          <div class="contact-methods">
            <div class="contact-method">
              <el-icon><Phone /></el-icon>
              <span>客服热线：400-888-8888</span>
            </div>
            <div class="contact-method">
              <el-icon><Message /></el-icon>
              <span>客服邮箱：service@fruit.com</span>
            </div>
            <div class="contact-method">
              <el-icon><Clock /></el-icon>
              <span>服务时间：9:00 - 21:00</span>
            </div>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { Box, Clock, Money, List, InfoFilled, Warning, Location, Lock, Phone, Message } from '@element-plus/icons-vue'
</script>

<style scoped>
.delivery-page {
  min-height: calc(100vh - 200px);
}

.delivery-hero {
  text-align: center;
  padding: 60px 20px;
  background: #ffffff;
  color: #2c3e50;
  margin-bottom: 40px;
  border-radius: 12px;
  border: 1px solid #e8e8e8;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.delivery-hero h1 {
  font-size: 42px;
  margin-bottom: 16px;
  font-weight: 700;
}

.delivery-hero .subtitle {
  font-size: 20px;
  opacity: 0.9;
}

.delivery-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px 40px;
}

.delivery-card {
  height: 100%;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transition: transform 0.3s, box-shadow 0.3s;
}

.delivery-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.12);
}

.card-icon {
  text-align: center;
  margin-bottom: 20px;
}

.card-icon .el-icon {
  font-size: 64px;
  color: #409eff;
}

.delivery-card h3 {
  text-align: center;
  margin-bottom: 20px;
  color: #303133;
  font-size: 20px;
}

.delivery-card p {
  line-height: 1.8;
  color: #606266;
  margin-bottom: 12px;
}

.delivery-card ul {
  padding-left: 20px;
  color: #606266;
  line-height: 1.8;
}

.delivery-card .note {
  font-size: 12px;
  color: #909399;
  margin-top: 12px;
}

.time-schedule {
  margin: 20px 0;
}

.time-item {
  margin-bottom: 16px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 6px;
  line-height: 1.6;
}

.time-item strong {
  color: #303133;
  display: block;
  margin-bottom: 4px;
}

.time-item span {
  color: #606266;
}

.highlight {
  color: #409eff;
  font-weight: 600;
}

.fee-table {
  margin: 20px 0;
}

.fee-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  margin-bottom: 12px;
  background: #f5f7fa;
  border-radius: 6px;
}

.fee-row.highlight {
  background: #ecf5ff;
  border: 1px solid #409eff;
}

.fee-amount {
  font-weight: 600;
  color: #303133;
}

.fee-desc {
  color: #409eff;
  font-weight: 600;
}

.process-card,
.notice-card,
.contact-card {
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

.notice-list {
  margin-top: 20px;
}

.notice-item {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
  padding: 20px;
  background: #f9fafc;
  border-radius: 8px;
  border-left: 4px solid #409eff;
}

.notice-icon {
  font-size: 24px;
  color: #409eff;
  flex-shrink: 0;
  margin-top: 4px;
}

.notice-item h4 {
  margin: 0 0 8px 0;
  color: #303133;
  font-size: 16px;
}

.notice-item p {
  margin: 0;
  color: #606266;
  line-height: 1.6;
}

.contact-info {
  text-align: center;
}

.contact-info p {
  margin-bottom: 24px;
  color: #606266;
  font-size: 16px;
}

.contact-methods {
  display: flex;
  justify-content: center;
  gap: 40px;
  flex-wrap: wrap;
}

.contact-method {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 16px 24px;
  background: #f5f7fa;
  border-radius: 8px;
  transition: all 0.3s;
}

.contact-method:hover {
  background: #ecf5ff;
  transform: translateY(-2px);
}

.contact-method .el-icon {
  font-size: 20px;
  color: #409eff;
}

.contact-method span {
  color: #303133;
  font-weight: 500;
}

.alert-content p {
  margin: 8px 0;
  line-height: 1.6;
}

@media (max-width: 768px) {
  .delivery-hero h1 {
    font-size: 32px;
  }

  .delivery-hero .subtitle {
    font-size: 16px;
  }

  .contact-methods {
    flex-direction: column;
    gap: 16px;
  }
}
</style>

