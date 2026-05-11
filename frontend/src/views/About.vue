<template>
  <div class="about-page">
    <div class="about-hero">
      <h1>关于飘香水果商城</h1>
      <p class="subtitle">新鲜水果，从产地直达您的餐桌</p>
    </div>

    <div class="about-content">
      <el-row :gutter="24">
        <el-col :xs="24" :md="12">
          <el-card class="about-card">
            <template #header>
              <div class="card-header">
                <el-icon><Shop /></el-icon>
                <span>我们的使命</span>
              </div>
            </template>
            <p>飘香水果商城致力于为您提供最新鲜、最优质的水果产品。我们与全国各地的优质果农建立直接合作关系，确保每一份水果都经过严格筛选，从产地直达您的餐桌。</p>
            <p>我们相信，新鲜的水果不仅是一种食物，更是一种生活方式的体现。因此，我们始终坚持"新鲜、健康、便捷"的服务理念，为每一位顾客带来最好的购物体验。</p>
          </el-card>
        </el-col>

        <el-col :xs="24" :md="12">
          <el-card class="about-card">
            <template #header>
              <div class="card-header">
                <el-icon><Star /></el-icon>
                <span>我们的优势</span>
              </div>
            </template>
            <ul class="advantage-list">
              <li>
                <el-icon><Check /></el-icon>
                <span><strong>产地直供：</strong>与优质果农直接合作，减少中间环节</span>
              </li>
              <li>
                <el-icon><Check /></el-icon>
                <span><strong>新鲜保证：</strong>严格的质量控制，确保水果新鲜度</span>
              </li>
              <li>
                <el-icon><Check /></el-icon>
                <span><strong>快速配送：</strong>高效的物流体系，快速送达</span>
              </li>
              <li>
                <el-icon><Check /></el-icon>
                <span><strong>优质服务：</strong>专业的客服团队，贴心服务</span>
              </li>
            </ul>
          </el-card>
        </el-col>
      </el-row>

      <el-card class="about-card">
        <template #header>
          <div class="card-header">
            <el-icon><Trophy /></el-icon>
            <span>我们的承诺</span>
          </div>
        </template>
        <div class="promise-grid">
          <div class="promise-item">
            <el-icon class="promise-icon"><Sunny /></el-icon>
            <h3>新鲜保证</h3>
            <p>所有水果均从产地新鲜采摘，确保最佳品质</p>
          </div>
          <div class="promise-item">
            <el-icon class="promise-icon"><Medal /></el-icon>
            <h3>品质第一</h3>
            <p>严格的质量标准，不合格产品绝不销售</p>
          </div>
          <div class="promise-item">
            <el-icon class="promise-icon"><Service /></el-icon>
            <h3>贴心服务</h3>
            <p>专业的客服团队，随时为您解答疑问</p>
          </div>
          <div class="promise-item">
            <el-icon class="promise-icon"><CreditCard /></el-icon>
            <h3>无忧售后</h3>
            <p>完善的售后保障，让您购物更放心</p>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { Shop, Star, Check, Trophy, Sunny, Medal, Service, CreditCard } from '@element-plus/icons-vue'
</script>

<style scoped>
.about-page {
  min-height: calc(100vh - 200px);
}

.about-hero {
  text-align: center;
  padding: 60px 20px;
  background: #ffffff;
  color: #2c3e50;
  margin-bottom: 40px;
  border-radius: 12px;
  border: 1px solid #e8e8e8;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.about-hero h1 {
  font-size: 42px;
  margin-bottom: 16px;
  font-weight: 700;
}

.about-hero .subtitle {
  font-size: 20px;
  opacity: 0.9;
}

.about-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px 40px;
}

.about-card {
  margin-bottom: 24px;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transition: transform 0.3s, box-shadow 0.3s;
}

.about-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.12);
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

.about-card p {
  line-height: 1.8;
  color: #606266;
  margin-bottom: 12px;
}

.advantage-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.advantage-list li {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 16px;
  line-height: 1.6;
  color: #606266;
}

.advantage-list .el-icon {
  color: #67c23a;
  font-size: 18px;
  margin-top: 2px;
  flex-shrink: 0;
}

.promise-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 24px;
  margin-top: 20px;
}

.promise-item {
  text-align: center;
  padding: 24px;
  background: linear-gradient(135deg, #f5f7fa 0%, #ffffff 100%);
  border-radius: 8px;
  transition: transform 0.3s;
}

.promise-item:hover {
  transform: translateY(-4px);
}

.promise-icon {
  font-size: 48px;
  color: #409eff;
  margin-bottom: 16px;
}

.promise-item h3 {
  margin: 0 0 12px 0;
  color: #303133;
  font-size: 18px;
}

.promise-item p {
  margin: 0;
  color: #606266;
  line-height: 1.6;
}

@media (max-width: 768px) {
  .about-hero h1 {
    font-size: 32px;
  }

  .about-hero .subtitle {
    font-size: 16px;
  }

  .promise-grid {
    grid-template-columns: 1fr;
  }
}
</style>

