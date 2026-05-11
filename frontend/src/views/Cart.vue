<template>
  <div class="cart-container">
    <el-card>
      <template #header>
        <div class="cart-header">
          <h2>购物车</h2>
          <el-button v-if="cartItems.length > 0" type="text" @click="clearCart">
            清空购物车
          </el-button>
        </div>
      </template>
      <div v-if="cartItems.length === 0" class="empty-cart">
        <el-empty description="购物车是空的">
          <el-button type="primary" @click="$router.push('/')">去购物</el-button>
        </el-empty>
      </div>
      <div v-else>
        <div class="merchant-groups">
          <div v-for="group in groupedCartItems" :key="group.merchantKey" class="merchant-group">
            <div class="merchant-header-row">
              <div class="merchant-left">
                <el-checkbox
                  class="merchant-select"
                  :model-value="isGroupAllSelected(group)"
                  :indeterminate="isGroupIndeterminate(group)"
                  @change="(val) => onToggleGroupSelected(group, val)"
                />
                <img
                  v-if="group.merchant?.avatar"
                  class="merchant-avatar"
                  :src="getImageUrl(group.merchant.avatar)"
                />
                <span class="merchant-name">
                  {{ group.merchant?.shopName || group.merchant?.name || '未知商家' }}
                </span>
              </div>
              <span class="merchant-count">共 {{ group.items.length }} 件</span>
            </div>

            <el-table :data="group.items" style="width: 100%">
              <el-table-column width="55">
                <template #default="{ row }">
                  <el-checkbox
                    :model-value="Number(row.isSelected) === 1"
                    @change="(val) => onToggleSelected(row, val)"
                  />
                </template>
              </el-table-column>
              <el-table-column label="商品" width="300">
                <template #default="{ row }">
                  <div class="product-cell">
                    <img
                      v-if="row.product?.mainImage"
                      :src="getImageUrl(row.product.mainImage)"
                      class="product-thumb"
                    />
                    <div class="product-info-cell">
                      <span>{{ row.product?.name || '商品已下架' }}</span>
                      <span v-if="row.specId && row.product?.specs" class="spec-name">
                        {{ getSpecName(row) }}
                      </span>
                    </div>
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="单价" width="120">
                <template #default="{ row }">
                  ¥{{ getItemPrice(row) }}
                </template>
              </el-table-column>
              <el-table-column label="数量" width="150">
                <template #default="{ row }">
                  <div class="qty-cell">
                    <el-input-number
                      v-model="row.quantity"
                      :min="1"
                      :max="row.product?.stock || 999"
                      @change="updateQuantity(row.id, row.quantity)"
                    />
                    <div v-if="getFreeCount(row) > 0" class="promo-tip">
                      含赠 {{ getFreeCount(row) }} 件，按 {{ getPayableQuantity(row) }} 件计价
                    </div>
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="小计" width="120">
                <template #default="{ row }">
                  ¥{{ getItemSubtotal(row).toFixed(2) }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="180">
                <template #default="{ row }">
                  <el-button type="primary" link @click="buyNow(row)">
                    立即购买
                  </el-button>
                  <el-button type="danger" link @click="deleteItem(row.id)">
                    删除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>
        <div class="cart-footer">
          <div class="total-info">
            <span>合计：</span>
            <span class="total-price">¥{{ totalPrice.toFixed(2) }}</span>
          </div>
          <el-button type="primary" size="large" @click="checkout">
            去结算
          </el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCartStore } from '@/stores/cart'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '@/utils/api'

const router = useRouter()
const cartStore = useCartStore()

const cartItems = ref([])
const merchantMap = ref({}) // { [merchantId]: merchant }

const getItemPrice = (item) => {
  // 如果有规格，使用规格价格；否则使用商品价格
  if (item.specId && item.product?.specs) {
    const spec = item.product.specs.find(s => s.id === item.specId)
    if (spec) {
      return Number(spec.price) || 0
    }
  }
  return Number(item.product?.price) || 0
}

const getPayableQuantity = (item) => {
  const q = Number(item?.quantity) || 0
  if (q <= 0) return 0
  const p = item?.product
  if (!p || p.specialEnabled !== 1 || !p.specialType) return q
  if (String(p.specialType).toUpperCase() !== 'BUY_N_GET_M') return q
  // BUY_N_GET_M：购物车里的 quantity 表示“已购买（需要支付）的件数”
  // 赠品单独展示/落单为价格0的明细项，不从应付件数中扣减。
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

const getItemSubtotal = (item) => {
  return getItemPrice(item) * getPayableQuantity(item)
}

const getSpecName = (item) => {
  if (item.specId && item.product?.specs) {
    const spec = item.product.specs.find(s => s.id === item.specId)
    if (spec) {
      return spec.specName
    }
  }
  return ''
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

const totalPrice = computed(() => {
  return cartItems.value.reduce((sum, item) => {
    if (Number(item?.isSelected) !== 1) return sum
    return sum + getItemSubtotal(item)
  }, 0)
})

const groupedCartItems = computed(() => {
  const groups = new Map()
  for (const item of cartItems.value) {
    const mid = item.merchantId ?? item.product?.merchantId ?? null
    const key = mid == null ? 'unknown' : String(mid)
    if (!groups.has(key)) {
      const merchant = mid != null ? merchantMap.value[String(mid)] : null
      groups.set(key, {
        merchantKey: key,
        merchantId: mid,
        merchant,
        items: []
      })
    }
    groups.get(key).items.push(item)
  }
  // 再补一次 merchant（merchantMap 可能后到）
  const result = Array.from(groups.values()).map(g => {
    if (g.merchantId != null) {
      g.merchant = merchantMap.value[String(g.merchantId)] || g.merchant
    }
    return g
  })
  // unknown 放最后
  result.sort((a, b) => {
    if (a.merchantKey === 'unknown') return 1
    if (b.merchantKey === 'unknown') return -1
    return 0
  })
  return result
})

const loadMerchantsByIds = async (merchantIds) => {
  const uniq = Array.from(new Set((merchantIds || []).filter(v => v != null).map(v => String(v))))
  const toLoad = uniq.filter(id => !merchantMap.value[id])
  if (toLoad.length === 0) return

  await Promise.all(
    toLoad.map(async (id) => {
      try {
        const res = await api.get(`/merchant/detail/${id}`)
        if (res.code === 200 && res.data) {
          merchantMap.value[id] = res.data
        }
      } catch (e) {
        // 忽略单个商家失败
        console.error('加载商家信息失败', id, e)
      }
    })
  )
}

const loadCart = async () => {
  try {
    const res = await api.get('/cart/list')
    if (res.code === 200) {
      cartItems.value = res.data || []
      // 加载商品详情
      for (const item of cartItems.value) {
        if (item.productId) {
          try {
            const productRes = await api.get(`/product/detail/${item.productId}`)
            if (productRes.code === 200) {
              item.product = productRes.data.product
              item.product.specs = productRes.data.specs || []
              if (item.product?.merchantId != null) {
                item.merchantId = item.product.merchantId
              }
            }
          } catch (error) {
            console.error('加载商品失败', error)
          }
        }
      }

      // 批量加载商家信息
      const merchantIds = cartItems.value
        .map(i => i.merchantId ?? i.product?.merchantId ?? null)
        .filter(v => v != null)
      await loadMerchantsByIds(merchantIds)
    }
  } catch (error) {
    console.error('加载购物车失败', error)
  }
}

const updateQuantity = async (id, quantity) => {
  try {
    await cartStore.updateQuantity(id, quantity)
    await loadCart()
  } catch (error) {
    ElMessage.error('更新失败')
  }
}

const deleteItem = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这个商品吗？', '提示', {
      type: 'warning'
    })
    await cartStore.deleteItem(id)
    await loadCart()
    ElMessage.success('删除成功')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const clearCart = async () => {
  try {
    await ElMessageBox.confirm('确定要清空购物车吗？', '提示', {
      type: 'warning'
    })
    // 这里应该调用清空购物车的API
    cartItems.value = []
    ElMessage.success('已清空')
  } catch (error) {
    // 用户取消
  }
}

const buyNow = async (item) => {
  try {
    // 直接跳转到结算页面，只结算当前商品
    const selectedItems = [{
      id: item.id,
      productId: item.productId,
      specId: item.specId,
      quantity: item.quantity
    }]
    router.push({
      path: '/checkout',
      query: { items: JSON.stringify(selectedItems) }
    })
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const checkout = () => {
  const selectedCount = cartItems.value.filter(i => Number(i?.isSelected) === 1).length
  if (selectedCount <= 0) {
    ElMessage.warning('请先勾选要结算的商品')
    return
  }
  router.push('/checkout')
}

const onToggleSelected = async (item, val) => {
  try {
    const selected = val ? 1 : 0
    await api.put('/cart/select', { id: item.id, selected })
    item.isSelected = selected
  } catch (e) {
    ElMessage.error('更新选中状态失败')
  }
}

const isGroupAllSelected = (group) => {
  const items = group?.items || []
  if (items.length === 0) return false
  return items.every(i => Number(i?.isSelected) === 1)
}

const isGroupIndeterminate = (group) => {
  const items = group?.items || []
  if (items.length === 0) return false
  const selectedCount = items.filter(i => Number(i?.isSelected) === 1).length
  return selectedCount > 0 && selectedCount < items.length
}

const onToggleGroupSelected = async (group, val) => {
  try {
    const items = group?.items || []
    if (items.length === 0) return
    const selected = val ? 1 : 0
    const ids = items.map(i => i.id).filter(Boolean)
    await api.put('/cart/select', { ids, selected })
    for (const item of items) {
      item.isSelected = selected
    }
  } catch (e) {
    ElMessage.error('更新选中状态失败')
  }
}

onMounted(() => {
  loadCart()
})
</script>

<style scoped>
.cart-container {
  background: #ffffff;
  border-radius: 8px;
}

.cart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.cart-header h2 {
  margin: 0;
  font-size: 20px;
  color: #262626;
}

.empty-cart {
  padding: 60px 0;
}

.product-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.product-thumb {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 4px;
}

.cart-footer {
  margin-top: 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 24px;
  border-top: 1px solid #e8e8e8;
}

.total-info {
  font-size: 18px;
  color: #262626;
}

.total-price {
  font-size: 24px;
  color: #ff4d4f;
  font-weight: 600;
  margin-left: 8px;
}

.product-info-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.spec-name {
  font-size: 12px;
  color: #8c8c8c;
}

.qty-cell {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.promo-tip {
  font-size: 12px;
  color: #ff4d4f;
  line-height: 1.2;
}

.merchant-groups {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.merchant-group {
  padding: 12px;
  border: 1px solid #f0f0f0;
  border-radius: 10px;
  background: #fff;
}

.merchant-header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 4px 4px 12px 4px;
}

.merchant-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.merchant-select {
  margin-right: 6px;
}

.merchant-avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  object-fit: cover;
  background: #f5f5f5;
  border: 1px solid #f0f0f0;
}

.merchant-name {
  font-weight: 600;
  color: #262626;
}

.merchant-count {
  font-size: 12px;
  color: #8c8c8c;
}
</style>

