<template>
  <div class="checkout-container">
    <el-card class="checkout-card">
      <template #header>
        <h2>确认订单</h2>
      </template>

      <!-- 收货地址 -->
      <div class="section">
        <h3>
          <el-icon><Location /></el-icon>
          收货地址
        </h3>
        <div v-if="addresses.length === 0" class="empty-address">
          <p>暂无收货地址</p>
          <el-button type="primary" @click="showAddressDialog()">添加地址</el-button>
        </div>
        <div v-else class="address-list">
          <div
            v-for="addr in addresses"
            :key="addr.id"
            class="address-item"
            :class="{ selected: selectedAddressId === addr.id }"
            @click="selectedAddressId = addr.id"
          >
            <div class="address-content">
              <div class="address-top">
                <span class="receiver">{{ addr.receiverName }}</span>
                <span class="phone">{{ addr.receiverPhone }}</span>
                <el-tag v-if="addr.isDefault" type="primary" size="small">默认</el-tag>
              </div>
              <div class="address-detail">
                {{ addr.province }} {{ addr.city }} {{ addr.district }} {{ addr.detailAddress }}
              </div>
            </div>
            <el-icon v-if="selectedAddressId === addr.id" class="check-icon"><Check /></el-icon>
          </div>
          <el-button type="text" @click="showAddressDialog()">+ 添加新地址</el-button>
        </div>
      </div>

      <!-- 商品清单 -->
      <div class="section">
        <h3>
          <el-icon><Goods /></el-icon>
          商品清单
        </h3>
        <div class="product-list">
          <div v-for="item in cartItems" :key="item.id" class="product-item">
            <img
              v-if="item.product?.mainImage || item.productImage"
              :src="getImageUrl(item.product?.mainImage || item.productImage)"
              class="product-image"
            />
            <div v-else class="product-image no-image">暂无图片</div>
            <div class="product-info">
              <div class="product-name">{{ item.product?.name || item.productName || '商品已下架' }}</div>
              <div class="product-spec" v-if="item.specName">规格：{{ item.specName }}</div>
              <div v-if="getFreeCount(item) > 0" class="product-promo">
                {{ item.product?.specialLabel || '买N送M' }}：含赠 {{ getFreeCount(item) }} 件，按 {{ getPayableQuantity(item) }} 件计价
              </div>
            </div>
            <div class="product-price">¥{{ (item.product?.price || item.price || 0).toFixed(2) }}</div>
            <div class="product-quantity">x{{ item.quantity }}</div>
            <div class="product-subtotal">¥{{ getItemSubtotal(item).toFixed(2) }}</div>
          </div>
        </div>
      </div>

      <!-- 优惠券选择 -->
      <div class="section">
        <h3>
          <el-icon><Ticket /></el-icon>
          优惠券
        </h3>
        <div class="coupon-selector">
          <el-button 
            v-if="!selectedCoupon"
            type="primary" 
            plain
            @click="openCouponDialog"
          >
            选择优惠券
          </el-button>
          <div v-else class="selected-coupon">
            <div class="coupon-info">
              <span class="coupon-name">{{ selectedCoupon.name }}</span>
              <span class="coupon-desc">
                <span v-if="selectedCoupon.type === 1">
                  满{{ formatMoney(selectedCoupon.minAmount) }}减{{ formatMoney(selectedCoupon.discountAmount) }}
                </span>
                <span v-else>
                  {{ (selectedCoupon.discountRate * 10).toFixed(1) }}折
                </span>
              </span>
            </div>
            <el-button link type="danger" @click="selectedCoupon = null">取消</el-button>
          </div>
          <span v-if="couponDiscount > 0" class="coupon-discount">
            已优惠 ¥{{ couponDiscount.toFixed(2) }}
          </span>
        </div>
      </div>

      <!-- 订单备注 -->
      <div class="section">
        <h3>
          <el-icon><EditPen /></el-icon>
          订单备注
        </h3>
        <el-input
          v-model="remark"
          type="textarea"
          :rows="2"
          placeholder="选填，可以告诉商家您的特殊需求"
          maxlength="200"
          show-word-limit
        />
      </div>

      <!-- 结算信息 -->
      <div class="checkout-footer">
        <div class="total-info">
          <div class="price-breakdown">
            <div class="price-item">
              <span>商品总额：</span>
              <span>¥{{ originalTotalPrice.toFixed(2) }}</span>
            </div>
            <div v-if="promotionDiscount > 0" class="price-item discount">
              <span>特价优惠：</span>
              <span>-¥{{ promotionDiscount.toFixed(2) }}</span>
            </div>
            <div v-if="couponDiscount > 0" class="price-item discount">
              <span>优惠券：</span>
              <span>-¥{{ couponDiscount.toFixed(2) }}</span>
            </div>
          </div>
          <div class="total-line">
            <span>
              共 {{ totalQuantity }} 件商品
              <span v-if="payableQuantityTotal !== totalQuantity">（按 {{ payableQuantityTotal }} 件计价）</span>
              ，合计：
            </span>
            <span class="total-price">¥{{ totalPrice.toFixed(2) }}</span>
          </div>
        </div>
        <el-button
          type="primary"
          size="large"
          :loading="submitting"
          :disabled="!selectedAddressId || cartItems.length === 0"
          @click="submitOrder"
        >
          提交订单
        </el-button>
      </div>
    </el-card>

    <!-- 添加地址对话框 -->
    <el-dialog v-model="addressDialogVisible" title="添加收货地址" width="500px">
      <el-form :model="addressForm" :rules="addressRules" ref="addressFormRef" label-width="100px">
        <el-form-item label="收货人" prop="receiverName">
          <el-input v-model="addressForm.receiverName" placeholder="请输入收货人姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="receiverPhone">
          <el-input v-model="addressForm.receiverPhone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="省份" prop="province">
          <el-input v-model="addressForm.province" placeholder="请输入省份" />
        </el-form-item>
        <el-form-item label="城市" prop="city">
          <el-input v-model="addressForm.city" placeholder="请输入城市" />
        </el-form-item>
        <el-form-item label="区县" prop="district">
          <el-input v-model="addressForm.district" placeholder="请输入区县" />
        </el-form-item>
        <el-form-item label="详细地址" prop="detailAddress">
          <el-input v-model="addressForm.detailAddress" type="textarea" :rows="2" placeholder="请输入详细地址" />
        </el-form-item>
        <el-form-item label="设为默认">
          <el-switch v-model="addressForm.isDefault" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addressDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveAddress" :loading="savingAddress">保存        </el-button>
      </template>
    </el-dialog>

    <!-- 优惠券选择对话框 -->
    <el-dialog v-model="showCouponDialog" title="选择优惠券" width="600px">
      <div v-if="availableCoupons.length === 0" class="empty-coupons">
        <el-empty :description="couponFilterReason || '暂无可用的优惠券'" />
        <div v-if="couponFilterReason" class="coupon-tips">
          <p>💡 提示：</p>
          <ul>
            <li v-if="couponFilterReason.includes('过期')">已过期的优惠券可以在"我的优惠券-已过期"中查看</li>
            <li v-if="couponFilterReason.includes('商家')">商家优惠券仅限对应商家使用，请确认购物车商品是否匹配</li>
            <li v-if="couponFilterReason.includes('金额')">部分优惠券有最低使用金额要求，请增加购物车商品</li>
          </ul>
        </div>
      </div>
      <div v-else class="coupon-list">
        <div
          v-for="coupon in availableCoupons"
          :key="coupon.id"
          class="coupon-item"
          :class="{ 'selected': selectedCoupon?.id === coupon.id }"
          @click="selectCoupon(coupon)"
        >
          <div class="coupon-left">
            <div class="coupon-value">
              <span v-if="coupon.type === 1" class="value-amount">
                ¥{{ formatMoney(coupon.discountAmount) }}
              </span>
              <span v-else class="value-rate">
                {{ (coupon.discountRate * 10).toFixed(1) }}折
              </span>
            </div>
            <div class="coupon-type-tag">
              <el-tag :type="coupon.type === 1 ? 'success' : 'warning'" size="small">
                {{ coupon.type === 1 ? '满减券' : '折扣券' }}
              </el-tag>
            </div>
          </div>
          <div class="coupon-right">
            <div class="coupon-name-row">
              <div class="coupon-name">{{ coupon.name }}</div>
              <el-tag 
                :type="coupon.merchantId ? 'success' : 'info'" 
                size="small"
                class="coupon-publisher"
              >
                {{ coupon.merchantId ? (coupon.merchantName || '商家优惠券') : '平台优惠券' }}
              </el-tag>
            </div>
            <div class="coupon-rule">
              满{{ formatMoney(coupon.minAmount) }}可用
              <span v-if="coupon.type === 2 && coupon.maxDiscountAmount">
                ，最高减{{ formatMoney(coupon.maxDiscountAmount) }}
              </span>
            </div>
            <div class="coupon-time">有效期至 {{ formatTime(coupon.validEndTime) }}</div>
          </div>
          <el-icon v-if="selectedCoupon?.id === coupon.id" class="check-icon"><Check /></el-icon>
        </div>
      </div>
      <template #footer>
        <el-button @click="showCouponDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmCoupon">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Location, Goods, EditPen, Ticket, Check } from '@element-plus/icons-vue'
import api from '@/utils/api'

const router = useRouter()
const route = useRoute()

const cartItems = ref([])
const addresses = ref([])
const selectedAddressId = ref(null)
const remark = ref('')
const submitting = ref(false)
const selectedCoupon = ref(null)
const availableCoupons = ref([])
const showCouponDialog = ref(false)
const couponFilterReason = ref('') // 记录优惠券过滤的原因

// 地址相关
const addressDialogVisible = ref(false)
const addressFormRef = ref(null)
const savingAddress = ref(false)
const addressForm = ref({
  receiverName: '',
  receiverPhone: '',
  province: '',
  city: '',
  district: '',
  detailAddress: '',
  isDefault: false
})

const addressRules = {
  receiverName: [{ required: true, message: '请输入收货人姓名', trigger: 'blur' }],
  receiverPhone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  province: [{ required: true, message: '请输入省份', trigger: 'blur' }],
  city: [{ required: true, message: '请输入城市', trigger: 'blur' }],
  district: [{ required: true, message: '请输入区县', trigger: 'blur' }],
  detailAddress: [{ required: true, message: '请输入详细地址', trigger: 'blur' }]
}

const getImageUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http://') || url.startsWith('https://')) {
    return url
  }
  if (url.startsWith('/uploads/')) {
    return '/api' + url
  }
  if (url.startsWith('/api/uploads/')) {
    return url
  }
  return url
}

const getPayableQuantity = (item) => {
  const q = Number(item?.quantity) || 0
  if (q <= 0) return 0
  const p = item?.product
  if (!p || p.specialEnabled !== 1 || !p.specialType) return q
  if (String(p.specialType).toUpperCase() !== 'BUY_N_GET_M') return q
  // BUY_N_GET_M：购物车里的 quantity 表示“已购买（需要支付）的件数”
  // 赠品单独展示/落单为价格0的明细，不从应付件数中扣减。
  return q
}

const getFreeCount = (item) => {
  const q = Number(item?.quantity) || 0
  if (q <= 0) return 0
  const p = item?.product
  if (!p || p.specialEnabled !== 1 || !p.specialType) return 0
  if (String(p.specialType).toUpperCase() !== 'BUY_N_GET_M') return 0

  const buy = Number(p.specialBuy) || 0
  const free = Number(p.specialFree) || 0
  if (buy <= 0 || free <= 0) return 0

  return Math.floor(q / buy) * free
}

const getItemUnitPrice = (item) => {
  // 优先使用规格价格
  if (item.specId && item.product?.specs) {
    const spec = item.product.specs.find(s => s.id === item.specId)
    if (spec && spec.price != null) {
      return Number(spec.price) || 0
    }
  }
  // 其次使用购物车项自身携带的价格（如立即购买时传入）
  if (item.price != null) {
    return Number(item.price) || 0
  }
  // 最后回退到商品基础价格
  return Number(item.product?.price || 0)
}

const getItemSubtotal = (item) => {
  const price = getItemUnitPrice(item)
  return price * getPayableQuantity(item)
}

const originalTotalPrice = computed(() => {
  return cartItems.value.reduce((sum, item) => {
    const price = getItemUnitPrice(item)
    return sum + price * getPayableQuantity(item)
  }, 0)
})

const promotionDiscount = computed(() => {
  return cartItems.value.reduce((sum, item) => {
    const price = getItemUnitPrice(item)
    const free = getFreeCount(item)
    return sum + price * free
  }, 0)
})

const couponDiscount = computed(() => {
  if (!selectedCoupon.value) return 0
  const coupon = selectedCoupon.value
  const total = originalTotalPrice.value
  
  if (total < coupon.minAmount) return 0
  
  if (coupon.type === 1) {
    // 满减券
    return coupon.discountAmount
  } else {
    // 折扣券
    let discount = total * (1 - coupon.discountRate)
    if (coupon.maxDiscountAmount && discount > coupon.maxDiscountAmount) {
      discount = coupon.maxDiscountAmount
    }
    return discount
  }
})

const totalPrice = computed(() => {
  return Math.max(0, originalTotalPrice.value - couponDiscount.value)
})

const formatMoney = (amount) => {
  if (!amount) return '0.00'
  return parseFloat(amount).toFixed(2)
}

const formatTime = (time) => {
  if (!time) return '—'
  return new Date(time).toLocaleDateString('zh-CN')
}

const loadAvailableCoupons = async () => {
  try {
    // 获取当前订单的商家ID（从购物车商品中获取）
    let currentMerchantId = null
    if (cartItems.value.length > 0) {
      // 尝试多种方式获取merchantId
      const firstItem = cartItems.value[0]
      currentMerchantId = firstItem.merchantId || firstItem.product?.merchantId || null
      
      // 转换为数字类型，确保类型一致
      if (currentMerchantId != null) {
        currentMerchantId = Number(currentMerchantId)
        // 如果转换后是NaN，重置为null
        if (isNaN(currentMerchantId)) {
          currentMerchantId = null
        }
      }
      
      // 如果还没有，尝试从商品详情获取
      if (!currentMerchantId && firstItem.productId) {
        try {
          const productRes = await api.get(`/product/detail/${firstItem.productId}`)
          if (productRes.code === 200 && productRes.data?.product?.merchantId) {
            currentMerchantId = Number(productRes.data.product.merchantId)
            // 如果转换后是NaN，重置为null
            if (isNaN(currentMerchantId)) {
              currentMerchantId = null
            } else {
              // 更新购物车项中的商品信息
              if (!firstItem.product) {
                firstItem.product = productRes.data.product
              }
              firstItem.merchantId = currentMerchantId
            }
          }
        } catch (e) {
          console.error('获取商品信息失败', e)
        }
      }
    }
    
    console.log('当前商家ID:', currentMerchantId, '类型:', typeof currentMerchantId)
    console.log('购物车商品数量:', cartItems.value.length)
    console.log('订单总额:', originalTotalPrice.value)
    
    const res = await api.get('/coupon/my', { params: { status: 0 } })
    if (res.code === 200) {
      const coupons = res.data || []
      console.log('获取到的优惠券总数:', coupons.length)
      
      // 过滤出可用的优惠券：
      // 1. 平台优惠券（merchantId为null或undefined）- 全平台可用
      // 2. 当前商家的优惠券（merchantId匹配）
      // 3. 订单金额满足最低使用金额
      // 4. 优惠券未过期
      const now = new Date()
      availableCoupons.value = coupons.filter(coupon => {
        // 检查优惠券是否过期
        if (coupon.validEndTime) {
          const endTime = new Date(coupon.validEndTime)
          if (now > endTime) {
            console.log('优惠券已过期:', coupon.name, '过期时间:', coupon.validEndTime)
            return false
          }
        }
        
        // 检查优惠券使用范围
        const couponMerchantId = coupon.merchantId != null ? Number(coupon.merchantId) : null
        
        if (couponMerchantId != null) {
          // 这是商家优惠券，只能用于对应商家
          if (currentMerchantId == null || couponMerchantId !== currentMerchantId) {
            console.log('优惠券被过滤（商家不匹配）:', coupon.name, '优惠券商家ID:', couponMerchantId, '当前商家ID:', currentMerchantId)
            return false
          }
        }
        // 平台优惠券（merchantId为null）全平台可用，不需要检查商家
        
        // 检查订单金额是否满足最低使用金额（确保类型转换）
        const minAmount = parseFloat(coupon.minAmount) || 0
        const totalPrice = parseFloat(originalTotalPrice.value) || 0
        const amountCheck = totalPrice >= minAmount
        if (!amountCheck) {
          console.log('优惠券被过滤（金额不足）:', coupon.name, '需要:', minAmount, '当前:', totalPrice)
        }
        return amountCheck
      })
      
      console.log('过滤后的可用优惠券数量:', availableCoupons.value.length)
      if (availableCoupons.value.length > 0) {
        console.log('可用优惠券列表:', availableCoupons.value.map(c => ({ name: c.name, merchantId: c.merchantId, merchantName: c.merchantName, minAmount: c.minAmount })))
        couponFilterReason.value = ''
      } else {
        // 生成友好的提示信息
        const reasons = []
        if (coupons.length === 0) {
          reasons.push('您还没有领取任何优惠券')
        } else {
          const expiredCount = coupons.filter(c => {
            if (c.validEndTime) {
              return new Date() > new Date(c.validEndTime)
            }
            return false
          }).length
          if (expiredCount > 0) {
            reasons.push(`${expiredCount}张优惠券已过期`)
          }
          
          const merchantMismatchCount = coupons.filter(c => {
            const couponMerchantId = c.merchantId != null ? Number(c.merchantId) : null
            if (couponMerchantId != null && (currentMerchantId == null || couponMerchantId !== currentMerchantId)) {
              return true
            }
            return false
          }).length
          if (merchantMismatchCount > 0) {
            reasons.push(`${merchantMismatchCount}张优惠券仅限指定商家使用`)
          }
          
          const amountNotEnoughCount = coupons.filter(c => {
            const minAmount = parseFloat(c.minAmount) || 0
            const totalPrice = parseFloat(originalTotalPrice.value) || 0
            return totalPrice < minAmount
          }).length
          if (amountNotEnoughCount > 0) {
            reasons.push(`${amountNotEnoughCount}张优惠券需要更高的订单金额`)
          }
        }
        
        couponFilterReason.value = reasons.length > 0 ? reasons.join('；') : '暂无可用的优惠券'
        
        console.log('没有可用优惠券，原因：')
        console.log('- 当前商家ID:', currentMerchantId)
        console.log('- 订单金额:', originalTotalPrice.value)
        console.log('- 提示信息:', couponFilterReason.value)
        console.log('- 所有优惠券:', coupons.map(c => ({ 
          name: c.name, 
          merchantId: c.merchantId, 
          minAmount: c.minAmount,
          validEndTime: c.validEndTime,
          isExpired: c.validEndTime ? new Date() > new Date(c.validEndTime) : false,
          canUse: currentMerchantId == null || c.merchantId == null || Number(c.merchantId) === currentMerchantId,
          amountOk: parseFloat(originalTotalPrice.value) >= parseFloat(c.minAmount || 0)
        })))
      }
    }
  } catch (error) {
    console.error('加载优惠券失败', error)
    ElMessage.error('加载优惠券失败：' + (error.response?.data?.message || error.message))
  }
}

const openCouponDialog = () => {
  showCouponDialog.value = true
  // 打开对话框时立即加载优惠券
  loadAvailableCoupons()
}

const selectCoupon = (coupon) => {
  selectedCoupon.value = coupon
}

const confirmCoupon = () => {
  showCouponDialog.value = false
}

// 监听订单总额变化，重新加载可用优惠券
watch(originalTotalPrice, () => {
  if (showCouponDialog.value) {
    loadAvailableCoupons()
  }
  // 如果当前选中的优惠券不满足条件，清除选择
  if (selectedCoupon.value) {
    // 检查金额条件
    if (originalTotalPrice.value < selectedCoupon.value.minAmount) {
      selectedCoupon.value = null
      return
    }
    // 检查商家条件
    if (cartItems.value.length > 0 && cartItems.value[0].product?.merchantId) {
      const currentMerchantId = cartItems.value[0].product.merchantId
      // 如果是商家优惠券，必须匹配当前商家
      if (selectedCoupon.value.merchantId != null && selectedCoupon.value.merchantId !== currentMerchantId) {
        selectedCoupon.value = null
      }
    }
  }
})

// 监听购物车变化，重新加载可用优惠券并验证已选优惠券
watch(() => cartItems.value, () => {
  if (showCouponDialog.value) {
    loadAvailableCoupons()
  }
  // 如果当前选中的优惠券不匹配商家，清除选择
  if (selectedCoupon.value && cartItems.value.length > 0 && cartItems.value[0].product?.merchantId) {
    const currentMerchantId = cartItems.value[0].product.merchantId
    // 如果是商家优惠券，必须匹配当前商家
    if (selectedCoupon.value.merchantId != null && selectedCoupon.value.merchantId !== currentMerchantId) {
      selectedCoupon.value = null
      ElMessage.warning('当前优惠券仅限指定商家使用，已自动取消选择')
    }
  }
}, { deep: true })

const totalQuantity = computed(() => {
  // quantity：已购买件数；还要加上赠品件数，才是“共X件商品”
  return cartItems.value.reduce((sum, item) => {
    const q = Number(item?.quantity) || 0
    return sum + q + getFreeCount(item)
  }, 0)
})

const payableQuantityTotal = computed(() => {
  return cartItems.value.reduce((sum, item) => sum + getPayableQuantity(item), 0)
})

const loadCart = async () => {
  // 检查是否是立即购买
  const buyNow = route.query.buyNow === 'true'
  const buyItemStr = route.query.item
  
  if (buyNow && buyItemStr) {
    // 立即购买模式：使用传递的商品信息
    try {
      const buyItem = JSON.parse(buyItemStr)
      // 构造购物车项格式
      const item = {
        id: 'buyNow_' + Date.now(), // 临时ID
        productId: buyItem.productId,
        specId: buyItem.specId,
        quantity: buyItem.quantity,
        merchantId: buyItem.merchantId, // 保存商家ID
        product: {
          id: buyItem.productId,
          name: buyItem.productName,
          mainImage: buyItem.productImage,
          price: buyItem.price
        },
        specName: buyItem.specName
      }
      cartItems.value = [item]
      return
    } catch (error) {
      console.error('解析立即购买商品信息失败', error)
    }
  }
  
  // 正常模式：从购物车加载
  try {
    const res = await api.get('/cart/list')
    if (res.code === 200) {
      // 只展示已勾选（isSelected=1）的商品，保证“展示=实际下单”一致
      const allItems = res.data || []
      cartItems.value = allItems.filter(i => Number(i?.isSelected) === 1)
      // 加载商品详情
      for (const item of cartItems.value) {
        if (item.productId) {
          try {
            const productRes = await api.get(`/product/detail/${item.productId}`)
            if (productRes.code === 200) {
              item.product = productRes.data.product
              item.product.specs = productRes.data.specs || []
              // 保存商家ID到item级别，方便后续使用
              if (item.product.merchantId) {
                item.merchantId = item.product.merchantId
              }
              // 如果有规格，设置规格名称
              if (item.specId && item.product.specs) {
                const spec = item.product.specs.find(s => s.id === item.specId)
                if (spec) {
                  item.specName = spec.specName
                  item.product.price = spec.price // 使用规格价格
                }
              }
            }
          } catch (error) {
            console.error('加载商品失败', error)
          }
        }
      }
      
      // 商品加载完成后，如果优惠券对话框已打开，重新加载优惠券
      if (showCouponDialog.value) {
        loadAvailableCoupons()
      }
    }
  } catch (error) {
    console.error('加载购物车失败', error)
  }
}

const loadAddresses = async () => {
  try {
    const res = await api.get('/address/list')
    if (res.code === 200) {
      addresses.value = res.data || []
      // 默认选择默认地址
      const defaultAddr = addresses.value.find(a => a.isDefault === 1)
      if (defaultAddr) {
        selectedAddressId.value = defaultAddr.id
      } else if (addresses.value.length > 0) {
        selectedAddressId.value = addresses.value[0].id
      }
    }
  } catch (error) {
    console.error('加载地址失败', error)
  }
}

const showAddressDialog = () => {
  addressForm.value = {
    receiverName: '',
    receiverPhone: '',
    province: '',
    city: '',
    district: '',
    detailAddress: '',
    isDefault: false
  }
  addressDialogVisible.value = true
}

const saveAddress = async () => {
  try {
    await addressFormRef.value.validate()
    savingAddress.value = true
    const data = { ...addressForm.value, isDefault: addressForm.value.isDefault ? 1 : 0 }
    const res = await api.post('/address/add', data)
    if (res.code === 200) {
      ElMessage.success('地址添加成功')
      addressDialogVisible.value = false
      await loadAddresses()
      // 选择新添加的地址
      if (res.data?.id) {
        selectedAddressId.value = res.data.id
      }
    } else {
      ElMessage.error(res.message || '添加失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('添加失败')
    }
  } finally {
    savingAddress.value = false
  }
}

const submitOrder = async () => {
  if (!selectedAddressId.value) {
    ElMessage.warning('请选择收货地址')
    return
  }
  if (cartItems.value.length === 0) {
    ElMessage.warning('购物车为空')
    return
  }

  submitting.value = true
  try {
    // 如果是立即购买模式，需要先将商品添加到购物车
    const buyNow = route.query.buyNow === 'true'
    if (buyNow && cartItems.value.length > 0) {
      const item = cartItems.value[0]
      // 先添加到购物车
      await api.post('/cart/add', {
        productId: item.productId,
        specId: item.specId,
        quantity: item.quantity
      })
      // 等待一下确保购物车已更新
      await new Promise(resolve => setTimeout(resolve, 300))
    }
    
    const res = await api.post('/order/create', {
      addressId: selectedAddressId.value,
      remark: remark.value,
      couponId: selectedCoupon.value?.id || null
    })
    if (res.code === 200) {
      ElMessage.success('订单创建成功')
      // 清除路由参数
      router.replace({ path: '/order', query: {} })
    } else {
      ElMessage.error(res.message || '创建订单失败')
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '创建订单失败')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadCart()
  loadAddresses()
  loadAvailableCoupons()
})
</script>

<style scoped>
.checkout-container {
  max-width: 900px;
  margin: 0 auto;
}

.checkout-card {
  background: #ffffff;
  border-radius: 12px;
}

.checkout-card h2 {
  margin: 0;
  font-size: 20px;
  color: #262626;
}

.section {
  margin-bottom: 32px;
  padding-bottom: 24px;
  border-bottom: 1px solid #f0f0f0;
}

.section:last-of-type {
  border-bottom: none;
}

.section h3 {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  color: #262626;
  margin-bottom: 16px;
}

.empty-address {
  text-align: center;
  padding: 24px;
  background: #fafafa;
  border-radius: 8px;
}

.empty-address p {
  color: #8c8c8c;
  margin-bottom: 12px;
}

.address-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.address-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  border: 2px solid #e8e8e8;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.address-item:hover {
  border-color: #1890ff;
}

.address-item.selected {
  border-color: #1890ff;
  background: #e6f7ff;
}

.address-content {
  flex: 1;
}

.address-top {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.receiver {
  font-weight: 600;
  color: #262626;
}

.phone {
  color: #595959;
}

.address-detail {
  color: #8c8c8c;
}

.check-icon {
  color: #1890ff;
  font-size: 24px;
}

.product-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.product-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px;
  background: #fafafa;
  border-radius: 8px;
}

.product-image {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 8px;
}

.product-image.no-image {
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f0f0f0;
  color: #8c8c8c;
  font-size: 12px;
}

.product-info {
  flex: 1;
}

.product-name {
  font-size: 14px;
  color: #262626;
  margin-bottom: 4px;
}

.product-spec {
  font-size: 12px;
  color: #8c8c8c;
}

.product-promo {
  margin-top: 6px;
  font-size: 12px;
  color: #ff4d4f;
}

.product-price {
  width: 80px;
  color: #595959;
  text-align: right;
}

.product-quantity {
  width: 50px;
  color: #8c8c8c;
  text-align: center;
}

.product-subtotal {
  width: 100px;
  font-weight: 600;
  color: #ff4d4f;
  text-align: right;
}

.checkout-footer {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 24px;
  padding-top: 24px;
  border-top: 1px solid #f0f0f0;
}

.total-info {
  font-size: 16px;
  color: #262626;
}

.total-price {
  font-size: 28px;
  font-weight: 700;
  color: #ff4d4f;
  margin-left: 8px;
}

.coupon-selector {
  display: flex;
  align-items: center;
  gap: 16px;
}

.selected-coupon {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: #f0f9ff;
  border: 1px solid #91d5ff;
  border-radius: 8px;
  flex: 1;
}

.coupon-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.coupon-name-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 4px;
}

.coupon-name {
  font-weight: 500;
  color: #1890ff;
  flex: 1;
}

.coupon-publisher {
  flex-shrink: 0;
}

.coupon-desc {
  font-size: 12px;
  color: #666;
}

.coupon-discount {
  color: #ff4d4f;
  font-weight: 500;
}

.coupon-list {
  max-height: 400px;
  overflow-y: auto;
}

.coupon-item {
  display: flex;
  align-items: center;
  padding: 16px;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  margin-bottom: 12px;
  cursor: pointer;
  transition: all 0.3s;
  position: relative;
}

.coupon-item:hover {
  border-color: #1890ff;
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.2);
}

.coupon-item.selected {
  border-color: #1890ff;
  background: #f0f9ff;
}

.coupon-left {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-width: 100px;
  padding-right: 16px;
  border-right: 1px dashed #e8e8e8;
}

.coupon-value .value-amount {
  font-size: 24px;
  font-weight: bold;
  color: #ff4d4f;
}

.coupon-value .value-rate {
  font-size: 20px;
  font-weight: bold;
  color: #ff4d4f;
}

.coupon-type-tag {
  margin-top: 8px;
}

.coupon-right {
  flex: 1;
  padding-left: 16px;
}

.coupon-rule {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.coupon-time {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.check-icon {
  position: absolute;
  top: 8px;
  right: 8px;
  color: #1890ff;
  font-size: 20px;
}

.empty-coupons {
  padding: 40px;
  text-align: center;
}

.coupon-tips {
  margin-top: 20px;
  padding: 16px;
  background: #f5f5f5;
  border-radius: 8px;
  text-align: left;
  font-size: 14px;
  color: #666;
}

.coupon-tips p {
  margin: 0 0 8px 0;
  font-weight: 500;
  color: #333;
}

.coupon-tips ul {
  margin: 0;
  padding-left: 20px;
}

.coupon-tips li {
  margin-bottom: 4px;
  line-height: 1.6;
}

.price-breakdown {
  margin-bottom: 8px;
}

.price-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 4px;
  font-size: 14px;
  color: #666;
}

.price-item.discount {
  color: #ff4d4f;
}

.total-line {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px solid #e8e8e8;
}
</style>

