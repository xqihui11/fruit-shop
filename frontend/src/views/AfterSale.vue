<template>
  <div class="aftersale-page">
    <div class="aftersale-hero">
      <h1>售后服务</h1>
      <p class="subtitle">完善的售后保障，让您购物更放心</p>
    </div>

    <div class="aftersale-content">
      <el-row :gutter="24">
        <el-col :xs="24" :md="12">
          <el-card class="service-card">
            <div class="card-icon">
              <el-icon><Refresh /></el-icon>
            </div>
            <h3>退换货政策</h3>
            <div class="policy-content">
              <h4>支持退换货的情况：</h4>
              <ul>
                <li>商品存在质量问题（腐烂、变质等）</li>
                <li>商品与描述不符</li>
                <li>收到商品数量不对</li>
                <li>非人为损坏的商品</li>
              </ul>
              <h4>退换货时间：</h4>
              <p>自收到商品之日起7天内可申请退换货</p>
              <h4>退换货流程：</h4>
              <ol>
                <li>联系客服说明情况</li>
                <li>提供相关照片或视频证据</li>
                <li>客服审核通过后安排退换货</li>
                <li>我们承担退换货运费</li>
              </ol>
            </div>
          </el-card>
        </el-col>

        <el-col :xs="24" :md="12">
          <el-card class="service-card">
            <div class="card-icon">
              <el-icon><Money /></el-icon>
            </div>
            <h3>退款说明</h3>
            <div class="policy-content">
              <h4>退款方式：</h4>
              <p>退款将原路返回到您的支付账户</p>
              <div class="refund-timeline">
                <div class="timeline-item">
                  <span class="time">1-3个工作日</span>
                  <span class="desc">支付宝/微信支付</span>
                </div>
                <div class="timeline-item">
                  <span class="time">3-7个工作日</span>
                  <span class="desc">银行卡支付</span>
                </div>
              </div>
              <h4>退款条件：</h4>
              <ul>
                <li>商品未使用或未开封</li>
                <li>商品保持原包装</li>
                <li>提供完整的退换货凭证</li>
              </ul>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-card class="process-card">
        <template #header>
          <div class="card-header">
            <el-icon><List /></el-icon>
            <span>售后申请流程</span>
          </div>
        </template>
        <el-steps :active="4" finish-status="success" align-center>
          <el-step title="提交申请" description="在订单页面提交售后申请" />
          <el-step title="审核处理" description="客服审核您的申请（1-2个工作日）" />
          <el-step title="退换货" description="审核通过后安排退换货" />
          <el-step title="退款/换货" description="完成退款或换货处理" />
        </el-steps>
      </el-card>

      <el-card class="warranty-card">
        <template #header>
          <div class="card-header">
            <el-icon><Medal /></el-icon>
            <span>质量保证</span>
          </div>
        </template>
        <div class="warranty-grid">
          <div class="warranty-item">
            <el-icon class="warranty-icon"><Check /></el-icon>
            <h4>新鲜保证</h4>
            <p>所有水果均经过严格筛选，确保新鲜度</p>
          </div>
          <div class="warranty-item">
            <el-icon class="warranty-icon"><Check /></el-icon>
            <h4>品质承诺</h4>
            <p>如发现质量问题，我们承诺全额退款</p>
          </div>
          <div class="warranty-item">
            <el-icon class="warranty-icon"><Check /></el-icon>
            <h4>快速响应</h4>
            <p>客服24小时内响应您的售后需求</p>
          </div>
          <div class="warranty-item">
            <el-icon class="warranty-icon"><Check /></el-icon>
            <h4>无忧售后</h4>
            <p>完善的售后体系，让您购物无后顾之忧</p>
          </div>
        </div>
      </el-card>

      <el-card class="notice-card">
        <template #header>
          <div class="card-header">
            <el-icon><InfoFilled /></el-icon>
            <span>注意事项</span>
          </div>
        </template>
        <el-alert
          title="重要提示"
          type="warning"
          :closable="false"
          show-icon
          style="margin-bottom: 20px;"
        >
          <template #default>
            <div class="alert-content">
              <p>1. 请保持商品原包装，便于我们核实情况</p>
              <p>2. 申请退换货时请提供清晰的商品照片</p>
              <p>3. 生鲜类商品因保质期短，请在收到后及时检查</p>
              <p>4. 如因个人原因（不喜欢、买错等）申请退货，需承担运费</p>
            </div>
          </template>
        </el-alert>

        <div class="notice-list">
          <div class="notice-item">
            <el-icon class="notice-icon"><Clock /></el-icon>
            <div>
              <h4>处理时效</h4>
              <p>我们承诺在收到您的申请后1-2个工作日内完成审核，审核通过后立即安排退换货</p>
            </div>
          </div>
          <div class="notice-item">
            <el-icon class="notice-icon"><Picture /></el-icon>
            <div>
              <h4>证据要求</h4>
              <p>申请退换货时，请提供清晰的商品照片或视频，包括商品外观、包装、问题部位等</p>
            </div>
          </div>
          <div class="notice-item">
            <el-icon class="notice-icon"><Box /></el-icon>
            <div>
              <h4>运费说明</h4>
              <p>因商品质量问题导致的退换货，运费由我们承担；因个人原因导致的退货，运费由客户承担</p>
            </div>
          </div>
        </div>
      </el-card>

      <el-card class="contact-card">
        <template #header>
          <div class="card-header">
            <el-icon><Service /></el-icon>
            <span>售后咨询</span>
          </div>
        </template>
        <div class="contact-info">
          <p>如需申请售后服务或有任何疑问，欢迎联系我们的客服团队：</p>
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
          <el-button type="primary" size="large" @click="$router.push('/order')" style="margin-top: 24px;">
            查看我的订单
          </el-button>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { Refresh, Money, List, Medal, Check, InfoFilled, Clock, Picture, Box, Service, Phone, Message } from '@element-plus/icons-vue'
</script>

<style scoped>
.aftersale-page {
  min-height: calc(100vh - 200px);
}

.aftersale-hero {
  text-align: center;
  padding: 60px 20px;
  background: #ffffff;
  color: #2c3e50;
  margin-bottom: 40px;
  border-radius: 12px;
  border: 1px solid #e8e8e8;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.aftersale-hero h1 {
  font-size: 42px;
  margin-bottom: 16px;
  font-weight: 700;
}

.aftersale-hero .subtitle {
  font-size: 20px;
  opacity: 0.9;
}

.aftersale-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px 40px;
}

.service-card {
  height: 100%;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transition: transform 0.3s, box-shadow 0.3s;
}

.service-card:hover {
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

.service-card h3 {
  text-align: center;
  margin-bottom: 20px;
  color: #303133;
  font-size: 20px;
}

.policy-content {
  line-height: 1.8;
  color: #606266;
}

.policy-content h4 {
  color: #303133;
  margin: 20px 0 12px 0;
  font-size: 16px;
}

.policy-content ul,
.policy-content ol {
  padding-left: 20px;
  margin-bottom: 16px;
}

.policy-content li {
  margin-bottom: 8px;
}

.refund-timeline {
  margin: 20px 0;
}

.timeline-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  margin-bottom: 12px;
  background: #f5f7fa;
  border-radius: 6px;
}

.timeline-item .time {
  font-weight: 600;
  color: #409eff;
}

.timeline-item .desc {
  color: #606266;
}

.process-card,
.warranty-card,
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

.warranty-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 24px;
  margin-top: 20px;
}

.warranty-item {
  text-align: center;
  padding: 24px;
  background: linear-gradient(135deg, #f5f7fa 0%, #ffffff 100%);
  border-radius: 8px;
  transition: transform 0.3s;
}

.warranty-item:hover {
  transform: translateY(-4px);
}

.warranty-icon {
  font-size: 48px;
  color: #67c23a;
  margin-bottom: 16px;
}

.warranty-item h4 {
  margin: 0 0 12px 0;
  color: #303133;
  font-size: 18px;
}

.warranty-item p {
  margin: 0;
  color: #606266;
  line-height: 1.6;
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
  border-left: 4px solid #e6a23c;
}

.notice-icon {
  font-size: 24px;
  color: #e6a23c;
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
  .aftersale-hero h1 {
    font-size: 32px;
  }

  .aftersale-hero .subtitle {
    font-size: 16px;
  }

  .warranty-grid {
    grid-template-columns: 1fr;
  }

  .contact-methods {
    flex-direction: column;
    gap: 16px;
  }
}
</style>

