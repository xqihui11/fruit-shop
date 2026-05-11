<template>
  <div class="dashboard">
    <!-- 顶部标题区 -->
    <header class="dashboard-header">
      <div>
        <h1 class="page-title">商家数据概览</h1>
        <p class="page-subtitle">
          一站式掌握店铺经营情况 · 商品 · 订单 · 售后 · 热销趋势
        </p>
      </div>
      <div class="header-meta">
        <el-tag type="success" effect="dark" class="header-tag">
          今日订单：{{ stats.todayOrders }} 单
        </el-tag>
        <el-tag type="info" effect="plain" class="header-tag">
          今日销售额：¥{{ todaySalesAmount }}
        </el-tag>
        <el-tag effect="plain" class="header-tag">
          累计订单：{{ stats.totalOrders }} 单
        </el-tag>
        <el-button
          type="primary"
          :icon="Refresh"
          :loading="refreshing"
          class="refresh-btn"
          @click="handleRefresh"
        >
          刷新数据
        </el-button>
      </div>
    </header>

    <!-- 顶部四大核心指标卡片 -->
    <el-row :gutter="20" class="stat-row">
      <el-col :xs="12" :sm="12" :md="6" :lg="6">
        <el-card
          class="stat-card clickable"
          shadow="hover"
          @click="$router.push('/merchant/products')"
        >
          <div class="stat-card-inner">
            <div class="stat-icon stat-icon-primary">
            <el-icon><Goods /></el-icon>
          </div>
            <div class="stat-content">
              <div class="stat-label">商品总数</div>
              <div class="stat-value">{{ stats.totalProducts }}</div>
              <div class="stat-desc">已上架 {{ stats.onSaleProducts }} 个</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="12" :sm="12" :md="6" :lg="6">
        <el-card
          class="stat-card clickable"
          shadow="hover"
          @click="$router.push('/merchant/products?status=1')"
        >
          <div class="stat-card-inner">
            <div class="stat-icon stat-icon-success">
            <el-icon><ShoppingCart /></el-icon>
          </div>
            <div class="stat-content">
              <div class="stat-label">在售商品</div>
              <div class="stat-value">{{ stats.onSaleProducts }}</div>
              <div class="stat-desc">
                库存预警 {{ inventoryWarningCount }} 个
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="12" :sm="12" :md="6" :lg="6">
        <el-card
          class="stat-card clickable"
          shadow="hover"
          @click="$router.push('/merchant/orders?status=1')"
        >
          <div class="stat-card-inner">
            <div class="stat-icon stat-icon-warning">
            <el-icon><Box /></el-icon>
          </div>
            <div class="stat-content">
              <div class="stat-label">待发货订单</div>
              <div class="stat-value">{{ stats.pendingShipOrders }}</div>
              <div class="stat-desc">
                待处理售后 {{ stats.pendingAfterSales }} 单
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="12" :sm="12" :md="6" :lg="6">
        <el-card
          class="stat-card clickable"
          shadow="hover"
          @click="$router.push('/merchant/orders')"
        >
          <div class="stat-card-inner">
            <div class="stat-icon stat-icon-info">
            <el-icon><Money /></el-icon>
          </div>
            <div class="stat-content">
              <div class="stat-label">累计销售额</div>
              <div class="stat-value">¥{{ stats.totalSales }}</div>
              <div class="stat-desc">
                近7日销售 ¥{{ stats.last7DaysSales }} · 损耗率 {{
                  (lossRate * 100).toFixed(1)
                }}%
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 二级分析卡片：订单 / 销售 / 客单价 / 售后 -->
    <el-row :gutter="20" class="analysis-row">
      <el-col :xs="12" :sm="12" :md="6" :lg="6">
        <el-card class="analysis-card" shadow="never">
          <div class="analysis-header">
            <el-icon class="analysis-icon primary"><TrendCharts /></el-icon>
            <span class="analysis-title">订单趋势</span>
          </div>
          <div class="analysis-main">
            <div class="analysis-value">{{ stats.todayOrders }}</div>
            <div class="analysis-label">今日订单数</div>
          </div>
          <div class="analysis-footer">
            近7日订单共
            <span class="highlight">{{ stats.last7DaysOrders }}</span>
            单
          </div>
        </el-card>
      </el-col>

      <el-col :xs="12" :sm="12" :md="6" :lg="6">
        <el-card class="analysis-card" shadow="never">
          <div class="analysis-header">
            <el-icon class="analysis-icon success"><Wallet /></el-icon>
            <span class="analysis-title">销售表现</span>
          </div>
          <div class="analysis-main">
            <div class="analysis-value">¥{{ stats.last7DaysSales }}</div>
            <div class="analysis-label">近7日销售额</div>
          </div>
          <div class="analysis-footer">
            累计销售额
            <span class="highlight">¥{{ stats.totalSales }}</span>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="12" :sm="12" :md="6" :lg="6">
        <el-card class="analysis-card" shadow="never">
          <div class="analysis-header">
            <el-icon class="analysis-icon info"><Tickets /></el-icon>
            <span class="analysis-title">客单价 & 退款率</span>
          </div>
          <div class="analysis-main">
            <div class="analysis-value">¥{{ stats.avgOrderValue }}</div>
            <div class="analysis-label">当前客单价</div>
          </div>
          <div class="analysis-footer">
            退款率
            <span class="highlight">
              {{ (stats.refundRate * 100).toFixed(1) }}%
            </span>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="12" :sm="12" :md="6" :lg="6">
        <el-card class="analysis-card" shadow="never">
          <div class="analysis-header">
            <el-icon class="analysis-icon danger"><DocumentChecked /></el-icon>
            <span class="analysis-title">售后概览</span>
          </div>
          <div class="analysis-main">
            <div class="analysis-value">{{ stats.pendingAfterSales }}</div>
            <div class="analysis-label">待处理售后</div>
          </div>
          <div class="analysis-footer">
            累计售后
            <span class="highlight">{{ stats.totalAfterSales }}</span>
            单
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域：左侧销售趋势折线图 + 右侧销量柱状图 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :xs="24" :md="16">
        <el-card class="section-card" shadow="never">
          <template #header>
            <div class="card-header">
              <div class="card-title-wrap">
                <span class="card-title">近7日销售趋势（金额）</span>
                <span class="card-subtitle">单位：元</span>
              </div>
            </div>
          </template>
          <div ref="salesChartRef" class="chart-container"></div>
        </el-card>
      </el-col>

      <el-col :xs="24" :md="8">
        <el-card class="section-card" shadow="never">
          <template #header>
            <div class="card-header">
              <div class="card-title-wrap">
                <span class="card-title">近7日销量趋势（件数）</span>
                <span class="card-subtitle">单位：件</span>
                </div>
              </div>
          </template>
          <div ref="volumeChartRef" class="chart-container small"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 订单状态分布 + 售后原因统计 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :xs="24" :md="12">
        <el-card class="section-card" shadow="never">
          <template #header>
            <div class="card-header">
              <div class="card-title-wrap">
                <span class="card-title">订单状态分布</span>
                <span class="card-subtitle">待发货 / 待收货 / 完成 / 取消 / 售后</span>
            </div>
          </div>
          </template>
          <div ref="statusChartRef" class="chart-container small"></div>
        </el-card>
      </el-col>

      <el-col :xs="24" :md="12">
        <el-card class="section-card" shadow="never">
          <template #header>
            <div class="card-header">
              <div class="card-title-wrap">
                <span class="card-title">售后原因统计</span>
                <span class="card-subtitle">如不新鲜、口感不佳等</span>
            </div>
          </div>
          </template>
          <div ref="reasonChartRef" class="chart-container small"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 热销商品 TOP5 + 快捷操作 -->
    <el-row :gutter="20" class="bottom-row">
      <el-col :xs="24" :md="14">
        <el-card class="section-card" shadow="never">
          <template #header>
            <div class="card-header">
              <div class="card-title-wrap">
                <span class="card-title">热销商品 TOP5</span>
                <span class="card-subtitle">按销量排序</span>
              </div>
            </div>
          </template>
          <div class="hot-products">
            <div
              v-for="(item, index) in hotProducts"
              :key="item.id || index"
              class="hot-item"
            >
              <div class="hot-rank" :class="'hot-rank-' + (index + 1)">
                {{ index + 1 }}
              </div>
              <div class="hot-info">
                <div class="hot-name">{{ item.name }}</div>
                <div class="hot-meta">
                  <span class="hot-sales">
                    {{ item.salesCount || 0 }} 件
                  </span>
                  <span class="hot-rate" v-if="maxSalesCount > 0">
                    占比
                    {{
                      (
                        ((item.salesCount || 0) / maxSalesCount) *
                        100
                      ).toFixed(1)
                    }}%
                  </span>
                </div>
                <el-progress
                  v-if="maxSalesCount > 0"
                  :percentage="
                    ((item.salesCount || 0) / maxSalesCount) * 100
                  "
                  :stroke-width="8"
                  :show-text="false"
                  class="hot-progress"
                />
              </div>
            </div>
            <el-empty
              v-if="hotProducts.length === 0"
              description="暂无热销商品数据"
              :image-size="60"
            />
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :md="10">
        <el-card class="section-card quick-actions" shadow="never">
      <template #header>
            <div class="card-header">
              <div class="card-title-wrap">
                <span class="card-title">快捷操作</span>
                <span class="card-subtitle">常用管理入口</span>
              </div>
            </div>
      </template>
          <div class="actions-grid">
            <el-button
              class="action-btn primary"
              type="primary"
              @click="$router.push('/merchant/products')"
            >
            <el-icon><Goods /></el-icon>
              <span>商品管理</span>
          </el-button>
            <el-button
              class="action-btn success"
              type="success"
              @click="$router.push('/merchant/orders')"
            >
            <el-icon><List /></el-icon>
              <span>订单管理</span>
          </el-button>
            <el-button
              class="action-btn warning"
              type="warning"
              @click="$router.push('/merchant/aftersales')"
            >
            <el-icon><Service /></el-icon>
              <span>售后处理</span>
          </el-button>
            <el-button
              class="action-btn info"
              type="info"
              @click="$router.push('/merchant/shop')"
            >
            <el-icon><Setting /></el-icon>
              <span>店铺设置</span>
          </el-button>
          </div>
        </el-card>
        </el-col>
      </el-row>

    <!-- AI 智能洞察 -->
    <el-row :gutter="20" class="ai-row">
      <el-col :span="24">
        <el-card class="section-card" shadow="never">
          <template #header>
            <div class="card-header">
              <div class="card-title-wrap">
                <span class="card-title">AI 智能洞察</span>
                <span class="card-subtitle">基于当前数据生成的经营建议</span>
              </div>
            </div>
          </template>
          <ul class="insight-list">
            <li v-for="(item, index) in insights" :key="index" class="insight-item">
              <span class="insight-index">0{{ index + 1 }}</span>
              <span class="insight-text">{{ item }}</span>
            </li>
          </ul>
    </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
/**
 * 商家后台 - 数据概览页（UI/UX 优化版）
 * - 使用 Element Plus + ECharts
 * - 保持原有数据结构不变：/merchant/statistics、/merchant/product/list
 */
import { ref, onMounted, onBeforeUnmount, nextTick, computed } from 'vue'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import api from '@/utils/api'

// Element Plus 图标
import {
  Goods,
  ShoppingCart,
  Box,
  Money,
  TrendCharts,
  Wallet,
  Tickets,
  DocumentChecked,
  List,
  Service,
  Setting,
  Refresh
} from '@element-plus/icons-vue'

// 核心统计数据结构（保持与后端返回字段一致）
const refreshing = ref(false)

const stats = ref({
  totalProducts: 0,
  onSaleProducts: 0,
  pendingShipOrders: 0,
  totalSales: 0,
  totalOrders: 0,
  todayOrders: 0,
  last7DaysOrders: 0,
  last7DaysSales: 0,
  totalAfterSales: 0,
  pendingAfterSales: 0,
  avgOrderValue: 0,
  refundRate: 0
})

// 销售趋势 & 销量趋势（基于真实订单数据计算）
const salesTrend = ref([]) // 金额
const volumeTrend = ref([]) // 销量（单数）

// 热销商品
const hotProducts = ref([])
const maxSalesCount = ref(0)

// 其他业务统计
const inventoryWarningCount = ref(0) // 库存预警商品数（简单模拟）
const lossRate = ref(0) // 生鲜损耗率（基于售后退款简单估算）

// ECharts 实例与 DOM 引用
const salesChartRef = ref(null)
const volumeChartRef = ref(null)
const statusChartRef = ref(null)
const reasonChartRef = ref(null)
let salesChartInstance = null
let volumeChartInstance = null
let statusChartInstance = null
let reasonChartInstance = null

// 订单状态 & 售后原因统计
const orderStatusStats = ref({
  pendingShip: 0,
  pendingReceive: 0,
  finished: 0,
  canceled: 0,
  afterSale: 0
})

const refundReasonStats = ref([])

// AI 智能洞察
const insights = ref([])

// 今日销售额（这里简单用趋势图最后一天金额代替）
const todaySalesAmount = computed(() => {
  if (!salesTrend.value.length) return 0
  return salesTrend.value[salesTrend.value.length - 1].amount || 0
})

// 加载统计数据
const loadStats = async () => {
  try {
    const res = await api.get('/merchant/statistics', {
      params: { _t: Date.now() }
    })
    if (res.code === 200 && res.data) {
      // 使用默认值合并后端返回，避免缺失字段导致 NaN
      stats.value = {
        ...stats.value,
        ...res.data
      }
      await nextTick()
      initStatusChart()
    }
  } catch (e) {
    console.error('加载统计数据失败', e)
  }
}

// 加载热销商品数据
const loadHotProducts = async () => {
  try {
    const res = await api.get('/merchant/product/list', {
      params: { _t: Date.now() }
    })
    if (res.code === 200 && res.data) {
      const list = (res.data || []).sort(
        (a, b) => (b.salesCount || 0) - (a.salesCount || 0)
      )
      hotProducts.value = list.slice(0, 5)
      maxSalesCount.value = hotProducts.value[0]
        ? hotProducts.value[0].salesCount || 0
        : 0

      // 简单计算库存预警商品数（示例：库存字段小于 10 的商品）
      inventoryWarningCount.value = (list || []).filter(
        item => typeof item.stock === 'number' && item.stock < 10
      ).length
    }
  } catch (e) {
    console.error('加载热销商品失败', e)
  }
}

// 加载订单与售后数据，用于订单状态分布 & 售后原因统计 & 趋势图
const loadOrdersAndAfterSales = async () => {
  try {
    const [orderRes, afterRes] = await Promise.all([
      api.get('/merchant/order/list', { params: { _t: Date.now() } }),
      api.get('/merchant/aftersale/list', { params: { _t: Date.now() } })
    ])

    // 订单状态统计
    if (orderRes.code === 200 && orderRes.data) {
      const statsMap = {
        pendingShip: 0,
        pendingReceive: 0,
        finished: 0,
        canceled: 0
      }

      // 近7日趋势基于真实订单（按支付成功时间或创建时间统计）
      const today = new Date()
      const dayMap = new Map()
      for (let i = 6; i >= 0; i--) {
        const d = new Date(today)
        d.setDate(d.getDate() - i)
        const key = d.toISOString().slice(0, 10) // YYYY-MM-DD
        dayMap.set(key, { amount: 0, count: 0 })
      }

      ;(orderRes.data || []).forEach(order => {
        switch (order.orderStatus) {
          case 1:
            statsMap.pendingShip++
            break
          case 2:
            statsMap.pendingReceive++
            break
          case 3:
            statsMap.finished++
            break
          case 4:
            statsMap.canceled++
            break
          default:
            break
        }

        // 使用 payAmount & paymentTime 统计近7日销售趋势
        const timeStr = (order.paymentTime || order.createTime || '').slice(
          0,
          10
        )
        if (dayMap.has(timeStr)) {
          const bucket = dayMap.get(timeStr)
          if (order.paymentStatus === 1 && order.payAmount) {
            bucket.amount += Number(order.payAmount)
            bucket.count += 1
          }
        }
      })

      const trend = []
      const volumes = []
      dayMap.forEach((val, key) => {
        const [year, month, day] = key.split('-')
        trend.push({
          date: `${month}-${day}`,
          amount: Number(val.amount.toFixed(2))
        })
        volumes.push({
          date: `${month}-${day}`,
          count: val.count
        })
      })
      salesTrend.value = trend
      volumeTrend.value = volumes

      orderStatusStats.value = {
        ...orderStatusStats.value,
        ...statsMap,
        afterSale: stats.value.totalAfterSales || 0
      }
    }

    // 售后原因统计
    if (afterRes.code === 200 && afterRes.data) {
      const reasonMap = {}
      ;(afterRes.data || []).forEach(item => {
        const key = item.reason || '其他'
        reasonMap[key] = (reasonMap[key] || 0) + 1
      })
      refundReasonStats.value = Object.entries(reasonMap).map(
        ([name, value]) => ({ name, value })
      )

      // 简单用退款单数量 / 总订单数 估算损耗率（和退款率接近，但完全基于真实单量）
      const totalOrders = stats.value.totalOrders || 0
      if (totalOrders > 0) {
        lossRate.value = (afterRes.data || []).length / totalOrders
      } else {
        lossRate.value = 0
      }
    }
  } catch (e) {
    console.error('加载订单/售后数据失败', e)
  }
}

/** 初始化 / 更新 销售趋势折线图 */
const initSalesChart = () => {
  if (!salesChartRef.value) return
  if (!salesChartInstance) {
    salesChartInstance = echarts.init(salesChartRef.value)
  }
  const xData = salesTrend.value.map(i => i.date)
  const yData = salesTrend.value.map(i => i.amount)
  const option = {
    grid: { left: 40, right: 20, top: 30, bottom: 30 },
    tooltip: {
      trigger: 'axis'
    },
    xAxis: {
      type: 'category',
      data: xData,
      boundaryGap: false,
      axisLine: { lineStyle: { color: '#d9d9d9' } },
      axisLabel: { color: '#8c8c8c' }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      axisTick: { show: false },
      splitLine: { lineStyle: { color: '#f0f0f0' } },
      axisLabel: { color: '#8c8c8c' }
    },
    series: [
      {
        name: '销售额',
        type: 'line',
        smooth: true,
        data: yData,
        symbol: 'circle',
        symbolSize: 8,
        lineStyle: {
          color: '#3b82f6',
          width: 3
        },
        itemStyle: {
          color: '#3b82f6',
          borderColor: '#ffffff',
          borderWidth: 2
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(59,130,246,0.35)' },
            { offset: 1, color: 'rgba(59,130,246,0.02)' }
          ])
        }
      }
    ]
  }
  salesChartInstance.setOption(option)
}

/** 初始化 / 更新 近7日销量柱状图 */
const initVolumeChart = () => {
  if (!volumeChartRef.value) return
  if (!volumeChartInstance) {
    volumeChartInstance = echarts.init(volumeChartRef.value)
  }
  const xData = volumeTrend.value.map(i => i.date)
  const yData = volumeTrend.value.map(i => i.count)
  const option = {
    grid: { left: 40, right: 20, top: 30, bottom: 30 },
    tooltip: {
      trigger: 'axis'
    },
    xAxis: {
      type: 'category',
      data: xData,
      axisLine: { lineStyle: { color: '#d9d9d9' } },
      axisLabel: { color: '#8c8c8c' }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      axisTick: { show: false },
      splitLine: { lineStyle: { color: '#f0f0f0' } },
      axisLabel: { color: '#8c8c8c' }
    },
    series: [
      {
        name: '销量（件）',
        type: 'bar',
        data: yData,
        barWidth: 18,
        itemStyle: {
          borderRadius: [6, 6, 0, 0],
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(16,185,129,0.9)' },
            { offset: 1, color: 'rgba(16,185,129,0.2)' }
          ])
        }
      }
    ]
  }
  volumeChartInstance.setOption(option)
}

/** 初始化 / 更新 订单状态环形图 */
const initStatusChart = () => {
  if (!statusChartRef.value) return
  if (!statusChartInstance) {
    statusChartInstance = echarts.init(statusChartRef.value)
  }

  const { pendingShip, pendingReceive, finished, canceled, afterSale } =
    orderStatusStats.value

  const option = {
    tooltip: {
      trigger: 'item'
    },
    legend: {
      bottom: 0,
      orient: 'horizontal',
      icon: 'circle',
      textStyle: { color: '#595959' }
    },
    series: [
      {
        name: '订单状态',
        type: 'pie',
        radius: ['55%', '80%'],
        avoidLabelOverlap: false,
        label: {
          show: false
        },
        labelLine: {
          show: false
        },
        data: [
          { value: pendingShip, name: '待发货', itemStyle: { color: '#f97316' } },
          {
            value: pendingReceive,
            name: '待收货',
            itemStyle: { color: '#38bdf8' }
          },
          { value: finished, name: '已完成', itemStyle: { color: '#22c55e' } },
          { value: canceled, name: '已取消', itemStyle: { color: '#9ca3af' } },
          // 使用与待发货不同的颜色，避免视觉混淆
          { value: afterSale, name: '售后单', itemStyle: { color: '#8b5cf6' } }
        ]
      }
    ]
  }

  statusChartInstance.setOption(option)
}

/** 初始化 / 更新 售后原因条形图 */
const initRefundReasonChart = () => {
  if (!reasonChartRef.value) return
  if (!reasonChartInstance) {
    reasonChartInstance = echarts.init(reasonChartRef.value)
  }
  const names = refundReasonStats.value.map(i => i.name)
  const values = refundReasonStats.value.map(i => i.value)
  const option = {
    grid: { left: 80, right: 20, top: 20, bottom: 30 },
    tooltip: { trigger: 'axis' },
    xAxis: {
      type: 'value',
      axisLine: { show: false },
      splitLine: { lineStyle: { color: '#f0f0f0' } },
      axisLabel: { color: '#8c8c8c' }
    },
    yAxis: {
      type: 'category',
      data: names,
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: { color: '#6b7280' }
    },
    series: [
      {
        type: 'bar',
        data: values,
        barWidth: 14,
        itemStyle: {
          borderRadius: [0, 6, 6, 0],
          color: new echarts.graphic.LinearGradient(1, 0, 0, 0, [
            { offset: 0, color: 'rgba(239,68,68,0.9)' },
            { offset: 1, color: 'rgba(248,113,113,0.3)' }
          ])
        }
      }
    ]
  }
  reasonChartInstance.setOption(option)
}

// 生成简单的 AI 智能洞察文案
const generateInsights = () => {
  const result = []
  if (hotProducts.value.length > 0) {
    const top1 = hotProducts.value[0]
    result.push(
      `洞察：${top1.name} 近期销量领先，建议适当增加进货量，并在首页做主推位。`
    )
  }
  if ((stats.value.refundRate || 0) > 0.05) {
    result.push(
      `洞察：当前退款率约为 ${(stats.value.refundRate * 100).toFixed(
        1
      )}% ，建议排查商品品质与配送体验。`
    )
  }
  if (inventoryWarningCount.value > 0) {
    result.push(
      `洞察：共有 ${inventoryWarningCount.value} 个商品处于库存预警状态，请及时补货，避免爆品断货。`
    )
  }
  if (!result.length) {
    result.push('当前经营数据表现稳定，建议持续关注高峰时段与热销单品。')
  }
  insights.value = result
}

// 手动刷新，确保重拉最新统计、图表和洞察
const handleRefresh = async () => {
  refreshing.value = true
  try {
    await loadStats()
    await loadHotProducts()
    await loadOrdersAndAfterSales()
    await nextTick()
    initSalesChart()
    initVolumeChart()
    initStatusChart()
    initRefundReasonChart()
    generateInsights()
    ElMessage.success('数据已刷新')
  } catch (e) {
    console.error('刷新数据失败', e)
    ElMessage.error('刷新失败，请稍后重试')
  } finally {
    refreshing.value = false
  }
}

// 响应式：窗口尺寸变化时自适应
const handleResize = () => {
  salesChartInstance && salesChartInstance.resize()
  volumeChartInstance && volumeChartInstance.resize()
  statusChartInstance && statusChartInstance.resize()
  reasonChartInstance && reasonChartInstance.resize()
}

onMounted(async () => {
  await loadStats()
  await loadHotProducts()
  await loadOrdersAndAfterSales()
  await nextTick()
  initSalesChart()
  initVolumeChart()
  initStatusChart()
  initRefundReasonChart()
  generateInsights()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  salesChartInstance && salesChartInstance.dispose()
  volumeChartInstance && volumeChartInstance.dispose()
  statusChartInstance && statusChartInstance.dispose()
  reasonChartInstance && reasonChartInstance.dispose()
})
</script>

<style scoped>
.dashboard {
  padding: 24px;
  background: #f5f6fa;
  min-height: 100vh;
  box-sizing: border-box;
  font-family: system-ui, -apple-system, BlinkMacSystemFont, 'Inter',
    'Segoe UI', sans-serif;
}

.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  gap: 16px;
}

.page-title {
  margin: 0 0 4px;
  font-size: 22px;
  font-weight: 600;
  color: #0f172a;
}

.page-subtitle {
  margin: 0;
  font-size: 13px;
  color: #6b7280;
}

.header-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.header-tag {
  border-radius: 999px;
}

.refresh-btn {
  border-radius: 999px;
}

.stat-row {
  margin-bottom: 20px;
}

.stat-card {
  border-radius: 14px;
  overflow: hidden;
  border: none;
  background: linear-gradient(135deg, #ffffff 0%, #f9fbff 100%);
  transition: transform 0.15s ease, box-shadow 0.15s ease;
}

.stat-card.clickable {
  cursor: pointer;
}

.stat-card.clickable:hover {
  transform: translateY(-3px);
  box-shadow: 0 14px 30px rgba(15, 23, 42, 0.12);
}

.stat-card-inner {
  display: flex;
  align-items: center;
  gap: 12px;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  font-size: 24px;
}

.stat-icon-primary {
  background: linear-gradient(135deg, #2563eb, #4f46e5);
}

.stat-icon-success {
  background: linear-gradient(135deg, #10b981, #22c55e);
}

.stat-icon-warning {
  background: linear-gradient(135deg, #f97316, #f59e0b);
}

.stat-icon-info {
  background: linear-gradient(135deg, #0ea5e9, #38bdf8);
}

.stat-content {
  flex: 1;
}

.stat-label {
  font-size: 13px;
  color: #6b7280;
  margin-bottom: 4px;
}

.stat-value {
  font-size: 22px;
  font-weight: 600;
  color: #111827;
  margin-bottom: 2px;
}

.stat-desc {
  font-size: 12px;
  color: #9ca3af;
}

.analysis-row {
  margin-bottom: 20px;
}

.analysis-card {
  border-radius: 14px;
  border: none;
  background: linear-gradient(135deg, #ffffff 0%, #f4f7ff 100%);
}

.analysis-header {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.analysis-icon {
  width: 26px;
  height: 26px;
  border-radius: 999px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 8px;
  color: #ffffff;
}

.analysis-icon.primary {
  background: linear-gradient(135deg, #2563eb, #4f46e5);
}

.analysis-icon.success {
  background: linear-gradient(135deg, #10b981, #22c55e);
}

.analysis-icon.info {
  background: linear-gradient(135deg, #0ea5e9, #22c55e);
}

.analysis-icon.danger {
  background: linear-gradient(135deg, #f97316, #ef4444);
}

.analysis-title {
  font-size: 13px;
  font-weight: 500;
  color: #4b5563;
}

.analysis-main {
  margin-bottom: 6px;
}

.analysis-value {
  font-size: 20px;
  font-weight: 600;
  color: #111827;
}

.analysis-label {
  font-size: 12px;
  color: #9ca3af;
}

.analysis-footer {
  font-size: 12px;
  color: #6b7280;
}

.highlight {
  color: #2563eb;
  font-weight: 500;
}

.chart-row {
  margin-bottom: 20px;
}

.section-card {
  border-radius: 14px;
  border: none;
  background: #ffffff;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title-wrap {
  display: flex;
  align-items: center;
  gap: 6px;
}

.card-title {
  font-size: 14px;
  font-weight: 600;
  color: #111827;
}

.card-subtitle {
  font-size: 12px;
  color: #9ca3af;
}

.chart-container {
  height: 320px;
}

.chart-container.small {
  height: 280px;
}

.bottom-row {
  margin-bottom: 8px;
}

.ai-row {
  margin-bottom: 8px;
}

.hot-products {
  min-height: 220px;
}

.hot-item {
  display: flex;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #f1f5f9;
}

.hot-item:last-child {
  border-bottom: none;
}

.hot-rank {
  width: 28px;
  height: 28px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
  color: #ffffff;
  margin-right: 12px;
  background: #9ca3af;
}

.hot-rank-1 {
  background: linear-gradient(135deg, #f59e0b, #ef4444);
}

.hot-rank-2 {
  background: linear-gradient(135deg, #6b7280, #4b5563);
}

.hot-rank-3 {
  background: linear-gradient(135deg, #a855f7, #ec4899);
}

.hot-info {
  flex: 1;
}

.hot-name {
  font-size: 14px;
  font-weight: 500;
  color: #111827;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.hot-meta {
  display: flex;
  gap: 8px;
  font-size: 12px;
  color: #6b7280;
  margin-bottom: 4px;
}

.hot-sales {
  color: #0f172a;
}

.hot-rate {
  color: #2563eb;
}

.hot-progress {
  max-width: 260px;
}

.quick-actions {
  min-height: 220px;
}

.actions-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.action-btn {
  width: 100%;
  justify-content: flex-start;
  border-radius: 999px;
  display: flex;
  gap: 6px;
}

.action-btn span {
  font-size: 13px;
}

.action-btn.primary {
  --el-button-bg-color: #2563eb;
}

.action-btn.success {
  --el-button-bg-color: #16a34a;
}

.action-btn.warning {
  --el-button-bg-color: #f97316;
}

.action-btn.info {
  --el-button-bg-color: #0ea5e9;
}

.insight-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.insight-item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  font-size: 13px;
  color: #4b5563;
}

.insight-index {
  flex-shrink: 0;
  padding: 2px 8px;
  border-radius: 999px;
  background: #e0f2fe;
  color: #0369a1;
  font-size: 12px;
  font-weight: 600;
}

.insight-text {
  line-height: 1.6;
}

@media (max-width: 768px) {
  .dashboard-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .chart-container {
    height: 260px;
  }
}
</style>


