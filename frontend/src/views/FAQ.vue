<template>
  <div class="faq-page">
    <div class="faq-hero">
      <h1>常见问题</h1>
      <p class="subtitle">快速解答您的疑问</p>
    </div>

    <div class="faq-content">
      <el-row :gutter="24">
        <el-col :xs="24" :lg="12">
          <el-card class="faq-category-card">
            <template #header>
              <div class="category-header">
                <el-icon><ShoppingCart /></el-icon>
                <span>购物相关</span>
              </div>
            </template>
            <el-collapse v-model="activeShopping">
              <el-collapse-item title="如何下单购买？" name="1">
                <div class="faq-answer">
                  <p>1. 浏览商品，选择您喜欢的商品加入购物车</p>
                  <p>2. 进入购物车，确认商品信息</p>
                  <p>3. 点击结算，填写收货地址和联系方式</p>
                  <p>4. 选择支付方式完成支付</p>
                  <p>5. 等待商品配送</p>
                </div>
              </el-collapse-item>
              <el-collapse-item title="支持哪些支付方式？" name="2">
                <div class="faq-answer">
                  <p>我们支持以下支付方式：</p>
                  <ul>
                    <li>支付宝</li>
                    <li>微信支付</li>
                    <li>银行卡支付</li>
                  </ul>
                </div>
              </el-collapse-item>
              <el-collapse-item title="如何查看订单状态？" name="3">
                <div class="faq-answer">
                  <p>登录后，在"我的订单"页面可以查看所有订单的详细状态，包括：待付款、待发货、待收货、已完成等状态。</p>
                </div>
              </el-collapse-item>
              <el-collapse-item title="可以修改订单吗？" name="4">
                <div class="faq-answer">
                  <p>订单提交后，在未发货前可以联系客服修改订单信息（如收货地址、商品数量等）。一旦发货，将无法修改订单。</p>
                </div>
              </el-collapse-item>
            </el-collapse>
          </el-card>
        </el-col>

        <el-col :xs="24" :lg="12">
          <el-card class="faq-category-card">
            <template #header>
              <div class="category-header">
                <el-icon><Box /></el-icon>
                <span>配送相关</span>
              </div>
            </template>
            <el-collapse v-model="activeDelivery">
              <el-collapse-item title="配送范围包括哪些地区？" name="1">
                <div class="faq-answer">
                  <p>我们目前支持全国主要城市的配送，包括一线城市、新一线城市等。具体配送范围可在下单时查看，偏远地区可能暂不支持配送。</p>
                </div>
              </el-collapse-item>
              <el-collapse-item title="一般多久能收到货？" name="2">
                <div class="faq-answer">
                  <p>一般情况下，工作日下单次日即可送达（部分区域支持当日达）。周末和节假日可能略有延迟，具体以实际配送时间为准。</p>
                </div>
              </el-collapse-item>
              <el-collapse-item title="运费如何计算？" name="3">
                <div class="faq-answer">
                  <p>订单满99元免运费，不满99元收取10元运费。新用户首单免运费。</p>
                </div>
              </el-collapse-item>
              <el-collapse-item title="可以指定配送时间吗？" name="4">
                <div class="faq-answer">
                  <p>可以在订单备注中说明您希望的配送时间，我们会尽量安排，但无法完全保证。如有特殊需求，建议提前联系客服。</p>
                </div>
              </el-collapse-item>
            </el-collapse>
          </el-card>
        </el-col>

        <el-col :xs="24" :lg="12">
          <el-card class="faq-category-card">
            <template #header>
              <div class="category-header">
                <el-icon><Refresh /></el-icon>
                <span>退换货</span>
              </div>
            </template>
            <el-collapse v-model="activeReturn">
              <el-collapse-item title="什么情况下可以退换货？" name="1">
                <div class="faq-answer">
                  <p>以下情况支持退换货：</p>
                  <ul>
                    <li>商品存在质量问题（腐烂、变质等）</li>
                    <li>商品与描述不符</li>
                    <li>收到商品数量不对</li>
                    <li>非人为损坏的商品</li>
                  </ul>
                </div>
              </el-collapse-item>
              <el-collapse-item title="退换货需要多长时间？" name="2">
                <div class="faq-answer">
                  <p>提交退换货申请后，我们会在1-2个工作日内完成审核。审核通过后，会立即安排退换货处理。退款将在3-7个工作日内到账。</p>
                </div>
              </el-collapse-item>
              <el-collapse-item title="退换货的运费谁承担？" name="3">
                <div class="faq-answer">
                  <p>因商品质量问题导致的退换货，运费由我们承担。因个人原因（不喜欢、买错等）导致的退货，运费由客户承担。</p>
                </div>
              </el-collapse-item>
              <el-collapse-item title="如何申请退换货？" name="4">
                <div class="faq-answer">
                  <p>1. 登录后进入"我的订单"页面</p>
                  <p>2. 找到需要退换货的订单，点击"申请售后"</p>
                  <p>3. 填写退换货原因并上传相关照片</p>
                  <p>4. 提交申请，等待客服审核</p>
                </div>
              </el-collapse-item>
            </el-collapse>
          </el-card>
        </el-col>

        <el-col :xs="24" :lg="12">
          <el-card class="faq-category-card">
            <template #header>
              <div class="category-header">
                <el-icon><User /></el-icon>
                <span>账户相关</span>
              </div>
            </template>
            <el-collapse v-model="activeAccount">
              <el-collapse-item title="如何注册账号？" name="1">
                <div class="faq-answer">
                  <p>点击页面右上角的"注册"按钮，填写用户名、手机号和密码即可完成注册。注册后即可享受购物服务。</p>
                </div>
              </el-collapse-item>
              <el-collapse-item title="忘记密码怎么办？" name="2">
                <div class="faq-answer">
                  <p>在登录页面点击"忘记密码"，输入注册时的手机号，系统会发送验证码到您的手机，按照提示即可重置密码。</p>
                </div>
              </el-collapse-item>
              <el-collapse-item title="如何修改个人信息？" name="3">
                <div class="faq-answer">
                  <p>登录后，进入"个人中心"页面，可以修改昵称、头像、收货地址等个人信息。</p>
                </div>
              </el-collapse-item>
              <el-collapse-item title="如何查看积分和优惠券？" name="4">
                <div class="faq-answer">
                  <p>在"个人中心"页面可以查看您的积分余额和可用优惠券。积分可通过购物获得，优惠券可在结算时使用。</p>
                </div>
              </el-collapse-item>
            </el-collapse>
          </el-card>
        </el-col>
      </el-row>

      <el-card class="contact-card">
        <template #header>
          <div class="card-header">
            <el-icon><Service /></el-icon>
            <span>还有其他问题？</span>
          </div>
        </template>
        <div class="contact-info">
          <p>如果以上问题无法解答您的疑问，欢迎联系我们的客服团队：</p>
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
import { ref } from 'vue'
import { ShoppingCart, Box, Refresh, User, Service, Phone, Message, Clock } from '@element-plus/icons-vue'

const activeShopping = ref(['1'])
const activeDelivery = ref(['1'])
const activeReturn = ref(['1'])
const activeAccount = ref(['1'])
</script>

<style scoped>
.faq-page {
  min-height: calc(100vh - 200px);
}

.faq-hero {
  text-align: center;
  padding: 60px 20px;
  background: #ffffff;
  color: #2c3e50;
  margin-bottom: 40px;
  border-radius: 12px;
  border: 1px solid #e8e8e8;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.faq-hero h1 {
  font-size: 42px;
  margin-bottom: 16px;
  font-weight: 700;
}

.faq-hero .subtitle {
  font-size: 20px;
  opacity: 0.9;
}

.faq-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px 40px;
}

.faq-category-card {
  margin-bottom: 24px;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transition: transform 0.3s, box-shadow 0.3s;
}

.faq-category-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.12);
}

.category-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.category-header .el-icon {
  font-size: 20px;
  color: #409eff;
}

.faq-answer {
  padding: 12px 0;
  line-height: 1.8;
  color: #606266;
}

.faq-answer p {
  margin: 8px 0;
}

.faq-answer ul {
  padding-left: 20px;
  margin: 8px 0;
}

.faq-answer li {
  margin-bottom: 4px;
}

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

:deep(.el-collapse-item__header) {
  font-weight: 500;
  color: #303133;
  padding: 16px 0;
}

:deep(.el-collapse-item__content) {
  padding-bottom: 16px;
}

@media (max-width: 768px) {
  .faq-hero h1 {
    font-size: 32px;
  }

  .faq-hero .subtitle {
    font-size: 16px;
  }

  .contact-methods {
    flex-direction: column;
    gap: 16px;
  }
}
</style>

