<template>
  <div class="products-page">
    <!-- 页面标题区 -->
    <div class="page-header">
      <h1 class="page-title">商品管理</h1>
      <el-button type="primary" class="add-btn" @click="showProductDialog()">
        <el-icon><Plus /></el-icon>
        添加商品
      </el-button>
    </div>

    <!-- 筛选栏 -->
    <el-card class="filter-card" shadow="never">
      <el-form :inline="true" :model="filterForm" class="filter-form">
        <el-form-item label="状态">
          <el-select v-model="filterForm.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="全部" value="" />
            <el-option label="已上架" :value="1" />
            <el-option label="已下架" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="价格区间">
          <el-input-number v-model="filterForm.minPrice" :min="0" :precision="2" placeholder="最低" style="width: 150px" />
          <span style="margin: 0 8px">-</span>
          <el-input-number v-model="filterForm.maxPrice" :min="0" :precision="2" placeholder="最高" style="width: 150px" />
        </el-form-item>
        <el-form-item label="销量区间">
          <el-input-number v-model="filterForm.minSales" :min="0" placeholder="最低" style="width: 150px" />
          <span style="margin: 0 8px">-</span>
          <el-input-number v-model="filterForm.maxSales" :min="0" placeholder="最高" style="width: 150px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleFilter">筛选</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 批量操作栏 -->
    <el-card v-if="selectedProducts.length > 0" class="batch-actions-card" shadow="never">
      <div class="batch-info">
        <span>已选择 <strong>{{ selectedProducts.length }}</strong> 个商品</span>
        <div class="batch-btns">
          <el-button type="success" size="small" @click="batchUpdateStatus(1)">
            <el-icon><Top /></el-icon>
            批量上架
          </el-button>
          <el-button type="warning" size="small" @click="batchUpdateStatus(2)">
            <el-icon><Bottom /></el-icon>
            批量下架
          </el-button>
          <el-button type="danger" size="small" @click="batchDelete">
            <el-icon><Delete /></el-icon>
            批量删除
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 商品列表表格 -->
    <el-card class="table-card" shadow="never">
      <el-table
        :data="filteredProducts"
        v-loading="loading"
        stripe
        @selection-change="handleSelectionChange"
        class="products-table"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="商品" min-width="200">
          <template #default="{ row }">
            <div class="product-info">
              <img
                v-if="row.mainImage"
                :src="getImageUrl(row.mainImage)"
                class="product-thumb"
                alt="商品图片"
              />
              <div v-else class="product-thumb-placeholder">
                <el-icon><Picture /></el-icon>
              </div>
              <div class="product-name-wrap">
                <span class="product-name">{{ row.name }}</span>
                <el-tag
                  v-if="row.specialEnabled === 1"
                  size="small"
                  class="special-tag"
                  type="danger"
                  effect="light"
                >
                  {{ row.specialLabel || '特价' }}
                </el-tag>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="price" label="价格" width="120" sortable>
          <template #default="{ row }">
            <span class="price-text">¥{{ Number(row.price).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="120" sortable>
          <template #default="{ row }">
            <span :class="{ 'stock-warning': row.stock < 10 }">{{ row.stock }}</span>
            <el-tooltip v-if="row.stock < 10" content="库存预警：库存不足10件" placement="top">
              <el-icon class="warning-icon"><Warning /></el-icon>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column prop="salesCount" label="销量" width="150" sortable>
          <template #default="{ row }">
            <div class="sales-info">
              <span class="sales-count">{{ row.salesCount || 0 }}</span>
              <el-progress
                v-if="maxSalesCount > 0"
                :percentage="((row.salesCount || 0) / maxSalesCount * 100)"
                :stroke-width="6"
                :show-text="false"
                class="sales-progress"
              />
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" class="status-tag">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              link
              size="small"
              @click="showProductDialog(row)"
            >
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button
              v-if="row.status === 1"
              type="warning"
              link
              size="small"
              @click="handleUpdateStatus(row.id, 2)"
            >
              <el-icon><Bottom /></el-icon>
              下架
            </el-button>
            <el-button
              v-if="row.status === 2"
              type="success"
              link
              size="small"
              @click="handleUpdateStatus(row.id, 1)"
            >
              <el-icon><Top /></el-icon>
              上架
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 空状态 -->
      <el-empty
        v-if="!loading && filteredProducts.length === 0"
        description="暂无商品数据，可尝试调整筛选条件或添加新商品"
        :image-size="120"
      />
    </el-card>

    <!-- 商品编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="editingProduct ? '编辑商品' : '添加商品'" width="600px">
      <el-form :model="productForm" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="productForm.name" placeholder="请输入商品名称" />
        </el-form-item>
        <el-form-item label="商品分类" prop="categoryId">
          <el-select v-model="productForm.categoryId" placeholder="请选择分类" style="width: 100%">
            <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
          </el-select>
        </el-form-item>
        <!-- 价格 / 原价：
             - 普通商品、买几送几：只展示一个“原价”（红色），内部同步到 price
             - 打折特价：展示“原价 + 现价”，原价仍为红色，现价可单独查看/微调 -->
        <template v-if="productForm.specialEnabled === 1 && productForm.specialType === 'DISCOUNT'">
          <el-form-item label="原价" class="price-original-item">
            <el-input-number
              v-model="productForm.originalPrice"
              :min="0"
              :precision="2"
              @change="handleOriginalPriceChange"
              class="price-original-input"
            />
          </el-form-item>
          <el-form-item label="现价" prop="price">
            <el-input-number
              v-model="productForm.price"
              :min="0"
              :precision="2"
              class="price-current-input"
            />
          </el-form-item>
        </template>
        <template v-else>
          <el-form-item label="原价" class="price-original-item">
            <el-input-number
              v-model="productForm.originalPrice"
              :min="0"
              :precision="2"
              @change="handleOriginalPriceChange"
              class="price-original-input"
            />
          </el-form-item>
        </template>

        <el-divider content-position="left">特价设置</el-divider>
        <el-form-item label="设为特价">
          <el-switch
            v-model="productForm.specialEnabled"
            :active-value="1"
            :inactive-value="0"
            active-text="开启"
            inactive-text="关闭"
            @change="handleSpecialEnabledChange"
          />
        </el-form-item>

        <template v-if="productForm.specialEnabled === 1">
          <el-form-item label="特价方式" prop="specialType">
            <el-select v-model="productForm.specialType" placeholder="请选择特价方式" style="width: 100%" @change="handleSpecialTypeChange">
              <el-option label="打折" value="DISCOUNT" />
              <el-option label="买N送M（如买二送一）" value="BUY_N_GET_M" />
            </el-select>
          </el-form-item>

          <template v-if="productForm.specialType === 'DISCOUNT'">
            <el-form-item label="折扣" prop="specialDiscount">
              <el-input-number
                v-model="productForm.specialDiscount"
                :min="0.01"
                :max="1"
                :step="0.05"
                :precision="2"
                style="width: 180px"
                @change="syncDiscountPrice"
              />
              <span class="special-help">0.8 = 8折（系统会自动按【原价×折扣】计算现价）</span>
            </el-form-item>
          </template>

          <template v-else-if="productForm.specialType === 'BUY_N_GET_M'">
            <el-form-item label="买N送M" required>
              <el-input-number v-model="productForm.specialBuy" :min="1" :step="1" style="width: 120px" />
              <span style="margin: 0 8px">送</span>
              <el-input-number v-model="productForm.specialFree" :min="1" :step="1" style="width: 120px" />
              <span style="margin-left: 8px">件</span>
            </el-form-item>
          </template>

          <el-form-item label="展示标签">
            <el-input v-model="productForm.specialLabel" placeholder="可选：如 8折 / 买二送一（不填会自动生成）" />
          </el-form-item>
        </template>

        <el-form-item label="库存" prop="stock">
          <el-input-number v-model="productForm.stock" :min="0" />
        </el-form-item>
        <el-form-item label="新鲜度">
          <el-rate v-model="productForm.freshnessLevel" />
        </el-form-item>
        <el-form-item label="商品主图">
          <el-upload
            class="image-uploader"
            :http-request="handleUpload"
            :show-file-list="false"
            :before-upload="beforeImageUpload"
            accept="image/*"
          >
            <img v-if="productForm.mainImage" :src="getImageUrl(productForm.mainImage)" class="uploaded-image" />
            <el-icon v-else class="uploader-icon"><Plus /></el-icon>
            <template #tip>
              <div class="el-upload__tip">支持 jpg/png 格式，大小不超过 5MB</div>
            </template>
          </el-upload>
        </el-form-item>
        <el-form-item label="商品描述">
          <el-input v-model="productForm.description" type="textarea" :rows="4" placeholder="请输入商品描述" />
        </el-form-item>
        <el-form-item label="商品规格">
          <div class="specs-section">
            <div v-for="(spec, index) in productForm.specs" :key="index" class="spec-item">
              <el-input
                v-model="spec.specName"
                placeholder="规格名称（如：500g、1kg装）"
                class="spec-field spec-name-field"
              />
              <div class="spec-field spec-with-label">
                <div class="spec-label">售价（元）</div>
                <el-input-number
                  v-model="spec.price"
                  :min="0"
                  :precision="2"
                  controls-position="right"
                  class="spec-number-field"
                />
              </div>
              <div class="spec-field spec-with-label">
                <div class="spec-label">库存（件）</div>
                <el-input-number
                  v-model="spec.stock"
                  :min="0"
                  controls-position="right"
                  class="spec-number-field"
                />
              </div>
              <div class="spec-field spec-with-label">
                <div class="spec-label">排序</div>
                <el-input-number
                  v-model="spec.sortOrder"
                  :min="0"
                  controls-position="right"
                  class="spec-sort-field"
                />
              </div>
              <el-button type="danger" size="small" class="spec-delete-btn" @click="removeSpec(index)">删除</el-button>
            </div>
            <el-button type="primary" size="small" @click="addSpec">添加规格</el-button>
            <div class="spec-tip">提示：如果不添加规格，将使用商品的基础价格和库存</div>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveProduct" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus,
  Edit,
  Delete,
  Top,
  Bottom,
  Picture,
  Warning
} from '@element-plus/icons-vue'
import api from '@/utils/api'

const route = useRoute()
const products = ref([])
const categories = ref([])
const dialogVisible = ref(false)
const editingProduct = ref(null)
const formRef = ref(null)
const saving = ref(false)
const uploading = ref(false)
const loading = ref(false)
const selectedProducts = ref([])

// 筛选表单
const filterForm = ref({
  status: '',
  minPrice: null,
  maxPrice: null,
  minSales: null,
  maxSales: null
})

// 计算最大销量（用于进度条）
const maxSalesCount = computed(() => {
  if (products.value.length === 0) return 0
  return Math.max(...products.value.map(p => p.salesCount || 0), 1)
})

// 筛选后的商品列表
const filteredProducts = computed(() => {
  let result = [...products.value]
  
  if (filterForm.value.status !== '') {
    result = result.filter(p => p.status === filterForm.value.status)
  }
  
  if (filterForm.value.minPrice !== null) {
    result = result.filter(p => Number(p.price) >= filterForm.value.minPrice)
  }
  
  if (filterForm.value.maxPrice !== null) {
    result = result.filter(p => Number(p.price) <= filterForm.value.maxPrice)
  }
  
  if (filterForm.value.minSales !== null) {
    result = result.filter(p => (p.salesCount || 0) >= filterForm.value.minSales)
  }
  
  if (filterForm.value.maxSales !== null) {
    result = result.filter(p => (p.salesCount || 0) <= filterForm.value.maxSales)
  }
  
  return result
})

const productForm = ref({
  name: '',
  categoryId: null,
  price: 0,
  originalPrice: null,
  specialEnabled: 0,
  specialType: '',
  specialDiscount: null,
  specialBuy: null,
  specialFree: null,
  specialLabel: '',
  stock: 0,
  freshnessLevel: 5,
  mainImage: '',
  description: '',
  specs: []
})

const rules = {
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入库存', trigger: 'blur' }],
  specialType: [{ required: false, message: '请选择特价方式', trigger: 'change' }],
  specialDiscount: [{ required: false, message: '请输入折扣', trigger: 'blur' }]
}

const getStatusText = (status) => {
  const map = { 0: '待审核', 1: '已上架', 2: '已下架', 3: '审核拒绝' }
  return map[status] || '未知'
}

const getStatusType = (status) => {
  // 已上架用绿色，已下架用橙色
  const map = { 0: 'warning', 1: 'success', 2: 'warning', 3: 'danger' }
  return map[status] || 'info'
}

const loadProducts = async () => {
  loading.value = true
  try {
    const res = await api.get('/merchant/product/list')
    if (res.code === 200) {
      products.value = res.data || []
    }
  } catch (error) {
    console.error('加载商品失败', error)
    ElMessage.error('加载商品失败')
  } finally {
    loading.value = false
  }
}

// 筛选处理
const handleFilter = () => {
  // 筛选逻辑已在 computed 中实现
  ElMessage.success('筛选完成')
}

// 重置筛选
const handleReset = () => {
  filterForm.value = {
    status: '',
    minPrice: null,
    maxPrice: null,
    minSales: null,
    maxSales: null
  }
  ElMessage.success('筛选条件已重置')
}

// 选择变化
const handleSelectionChange = (selection) => {
  selectedProducts.value = selection
}

// 批量更新状态
const batchUpdateStatus = async (status) => {
  if (selectedProducts.value.length === 0) {
    ElMessage.warning('请先选择商品')
    return
  }
  
  const statusText = status === 1 ? '上架' : '下架'
  try {
    await ElMessageBox.confirm(
      `确认要批量${statusText}选中的 ${selectedProducts.value.length} 个商品吗？`,
      '批量操作确认',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const promises = selectedProducts.value.map(product =>
      api.post(`/merchant/product/status/${product.id}`, { status })
    )
    
    await Promise.all(promises)
    ElMessage.success(`批量${statusText}成功`)
    selectedProducts.value = []
    loadProducts()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(`批量${statusText}失败`)
    }
  }
}

// 批量删除
const batchDelete = async () => {
  if (selectedProducts.value.length === 0) {
    ElMessage.warning('请先选择商品')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确认要删除选中的 ${selectedProducts.value.length} 个商品吗？此操作不可恢复！`,
      '批量删除确认',
      {
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
        type: 'error'
      }
    )
    
    // 注意：这里需要后端提供批量删除接口，暂时用循环调用单个删除
    ElMessage.warning('批量删除功能需要后端支持，请先实现批量删除接口')
    selectedProducts.value = []
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除失败', error)
    }
  }
}

// 更新状态（带确认）
const handleUpdateStatus = async (id, status) => {
  const product = products.value.find(p => p.id === id)
  const statusText = status === 1 ? '上架' : '下架'
  const productName = product?.name || '该商品'
  
  try {
    await ElMessageBox.confirm(
      `确认要${statusText}商品"${productName}"吗？`,
      '操作确认',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await updateStatus(id, status)
  } catch (error) {
    if (error !== 'cancel') {
      console.error('操作失败', error)
    }
  }
}

const loadCategories = async () => {
  try {
    const res = await api.get('/category/list')
    if (res.code === 200) {
      categories.value = res.data || []
    }
  } catch (error) {
    console.error('加载分类失败', error)
  }
}

const showProductDialog = async (product = null) => {
  editingProduct.value = product
  if (product) {
    productForm.value = { 
      ...product, 
      specialEnabled: product.specialEnabled ?? 0,
      specialType: product.specialType ?? '',
      specialDiscount: product.specialDiscount ?? null,
      specialBuy: product.specialBuy ?? null,
      specialFree: product.specialFree ?? null,
      specialLabel: product.specialLabel ?? '',
      specs: [] 
    }
    // 加载规格
    try {
      const res = await api.get(`/product/spec/list/${product.id}`)
      if (res.code === 200) {
        productForm.value.specs = res.data || []
      }
    } catch (error) {
      console.error('加载规格失败', error)
    }
    // 普通商品或“买几送几”场景下，如果后端没给原价，则用当前价格作为原价，保持只填一个价
    if (productForm.value.specialEnabled !== 1 || productForm.value.specialType === 'BUY_N_GET_M') {
      if (productForm.value.originalPrice == null || Number.isNaN(Number(productForm.value.originalPrice))) {
        productForm.value.originalPrice = productForm.value.price ?? 0
      }
    }
  } else {
    productForm.value = {
      name: '',
      categoryId: null,
      price: 0,
      originalPrice: null,
      specialEnabled: 0,
      specialType: '',
      specialDiscount: null,
      specialBuy: null,
      specialFree: null,
      specialLabel: '',
      stock: 0,
      freshnessLevel: 5,
      mainImage: '',
      description: '',
      specs: []
    }
  }
  dialogVisible.value = true
}

const handleSpecialEnabledChange = () => {
  if (productForm.value.specialEnabled !== 1) {
    productForm.value.specialType = ''
    productForm.value.specialDiscount = null
    productForm.value.specialBuy = null
    productForm.value.specialFree = null
    productForm.value.specialLabel = ''
  } else {
    if (!productForm.value.specialType) {
      productForm.value.specialType = 'DISCOUNT'
    }
    syncDiscountPrice()
    ensureDefaultSpecialLabel()
  }
}

const handleSpecialTypeChange = () => {
  // 切换方式时清理无关字段
  if (productForm.value.specialType === 'DISCOUNT') {
    productForm.value.specialBuy = null
    productForm.value.specialFree = null
    if (productForm.value.specialDiscount == null) {
      productForm.value.specialDiscount = 0.8
    }
    syncDiscountPrice()
  } else if (productForm.value.specialType === 'BUY_N_GET_M') {
    productForm.value.specialDiscount = null
    if (productForm.value.specialBuy == null) productForm.value.specialBuy = 2
    if (productForm.value.specialFree == null) productForm.value.specialFree = 1
  }
  ensureDefaultSpecialLabel()
}

const ensureDefaultSpecialLabel = () => {
  if (productForm.value.specialEnabled !== 1) return
  if (productForm.value.specialLabel && String(productForm.value.specialLabel).trim()) return

  if (productForm.value.specialType === 'DISCOUNT' && productForm.value.specialDiscount != null) {
    const ten = Number(productForm.value.specialDiscount) * 10
    if (!Number.isNaN(ten) && ten > 0) {
      // 8折/9.5折
      productForm.value.specialLabel = `${parseFloat(ten.toFixed(2))}折`
    }
  } else if (productForm.value.specialType === 'BUY_N_GET_M' && productForm.value.specialBuy && productForm.value.specialFree) {
    productForm.value.specialLabel = `买${productForm.value.specialBuy}送${productForm.value.specialFree}`
  }
}

// 原价变化时的统一处理：
// - 打折特价：用原价 + 折扣计算现价
// - 其他情况（普通 / 买几送几）：直接把原价同步为现价
const handleOriginalPriceChange = () => {
  const original = Number(productForm.value.originalPrice)
  if (!original || Number.isNaN(original)) return

  if (productForm.value.specialEnabled === 1 && productForm.value.specialType === 'DISCOUNT') {
    syncDiscountPrice()
  } else {
    productForm.value.price = Number(original.toFixed(2))
  }
}

const syncDiscountPrice = () => {
  if (productForm.value.specialEnabled !== 1) return
  if (productForm.value.specialType !== 'DISCOUNT') return
  const original = Number(productForm.value.originalPrice)
  const discount = Number(productForm.value.specialDiscount)
  if (!original || original <= 0) return
  if (!discount || discount <= 0 || discount > 1) return

  const newPrice = original * discount
  productForm.value.price = Number(newPrice.toFixed(2))
  ensureDefaultSpecialLabel()
}

const addSpec = () => {
  productForm.value.specs.push({
    specName: '',
    price: 0,
    originalPrice: null,
    stock: 0,
    sortOrder: productForm.value.specs.length
  })
}

const removeSpec = (index) => {
  productForm.value.specs.splice(index, 1)
}

// 根据地址栏 query.productId 自动打开对应商品编辑弹窗
const openProductFromQuery = async () => {
  const { productId } = route.query
  if (!productId) return
  const idNum = Number(productId)
  if (!idNum) return
  const target = products.value.find(p => Number(p.id) === idNum)
  if (!target) return
  await showProductDialog(target)
}

const saveProduct = async () => {
  try {
    await formRef.value.validate()

    // 特价前端校验，避免后端报错但前端不知道原因
    if (productForm.value.specialEnabled === 1) {
      if (!productForm.value.specialType) {
        ElMessage.warning('请选择特价方式')
        return
      }
      if (productForm.value.specialType === 'DISCOUNT') {
        if (!productForm.value.originalPrice || Number(productForm.value.originalPrice) <= 0) {
          ElMessage.warning('请先填写原价，再设置折扣')
          return
        }
        if (!productForm.value.specialDiscount || Number(productForm.value.specialDiscount) <= 0 || Number(productForm.value.specialDiscount) > 1) {
          ElMessage.warning('请填写 0~1 之间的折扣，例如 0.8 表示 8 折')
          return
        }
      } else if (productForm.value.specialType === 'BUY_N_GET_M') {
        if (!productForm.value.specialBuy || productForm.value.specialBuy <= 0 ||
            !productForm.value.specialFree || productForm.value.specialFree <= 0) {
          ElMessage.warning('请填写买N送M中的 N 和 M，且都要大于 0')
          return
        }
      }
    }

    saving.value = true
    let res
    const productData = { ...productForm.value }
    const specs = productData.specs || []
    delete productData.specs
    
    if (editingProduct.value) {
      res = await api.put('/merchant/product/update', productData)
    } else {
      res = await api.post('/merchant/product/add', productData)
    }
    
    if (res.code === 200) {
      const productId = editingProduct.value ? editingProduct.value.id : res.data.id
      // 保存规格
      if (specs.length > 0) {
        try {
          await api.post('/product/spec/save', {
            productId: productId,
            specs: specs
          })
        } catch (error) {
          console.error('保存规格失败', error)
        }
      }
      ElMessage.success('保存成功')
      dialogVisible.value = false
      loadProducts()
    } else {
      ElMessage.error(res.message || '保存失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('保存失败')
    }
  } finally {
    saving.value = false
  }
}

const updateStatus = async (id, status) => {
  try {
    const res = await api.post(`/merchant/product/status/${id}`, { status })
    if (res.code === 200) {
      const statusText = status === 1 ? '上架' : '下架'
      ElMessage.success(`商品已成功${statusText}`)
      loadProducts()
    } else {
      ElMessage.error(res.message || '更新失败')
    }
  } catch (error) {
    ElMessage.error('更新失败')
  }
}

const handleUpload = async (options) => {
  const formData = new FormData()
  formData.append('file', options.file)
  
  uploading.value = true
  try {
    const res = await api.post('/file/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    if (res.code === 200 && res.data) {
      productForm.value.mainImage = res.data.url
      ElMessage.success('图片上传成功')
    } else {
      ElMessage.error(res.message || '图片上传失败')
    }
  } catch (error) {
    ElMessage.error('图片上传失败')
  } finally {
    uploading.value = false
  }
}

const beforeImageUpload = (file) => {
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

const getImageUrl = (url) => {
  if (!url) return ''
  // 如果是完整URL，直接返回
  if (url.startsWith('http://') || url.startsWith('https://')) {
    return url
  }
  // 如果是相对路径，加上API前缀
  if (url.startsWith('/uploads/')) {
    return '/api' + url
  }
  // 如果已经是/api/uploads/开头，直接返回
  if (url.startsWith('/api/uploads/')) {
    return url
  }
  return url
}

onMounted(async () => {
  await Promise.all([loadProducts(), loadCategories()])
  await openProductFromQuery()
})

watch(
  () => route.query.productId,
  async (val) => {
    if (val) {
      await openProductFromQuery()
    }
  }
)
</script>

<style scoped>
.products-page {
  padding: 24px;
  background: #f5f7fa;
  min-height: 100vh;
  font-family: 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

/* 页面标题区 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e6e6e6;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0;
}

.add-btn {
  background: #2ecc71;
  border-color: #2ecc71;
  border-radius: 4px;
  padding: 8px 16px;
  font-size: 14px;
}

.add-btn:hover {
  background: #27ae60;
  border-color: #27ae60;
}

/* 筛选卡片 */
.filter-card {
  margin-bottom: 16px;
  border-radius: 8px;
  background: #ffffff;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.04);
}

.filter-card :deep(.el-card__body) {
  padding: 20px;
}

.filter-form {
  margin: 0;
}

.filter-form :deep(.el-form-item) {
  margin-bottom: 0;
  margin-right: 24px;
}

.filter-form :deep(.el-form-item__label) {
  font-weight: 500;
  color: #2c3e50;
  padding-right: 12px;
}

.filter-form :deep(.el-input-number) {
  width: 150px;
}

.filter-form :deep(.el-input-number .el-input__inner) {
  text-align: left;
  padding-left: 10px;
  padding-right: 35px;
  font-size: 14px;
  width: 150px;
}

.filter-form :deep(.el-input-number .el-input__wrapper) {
  width: 150px;
  padding-right: 0;
}

.filter-form :deep(.el-input-number .el-input-number__decrease),
.filter-form :deep(.el-input-number .el-input-number__increase) {
  width: 28px;
}

/* 批量操作栏 */
.batch-actions-card {
  margin-bottom: 16px;
  border-radius: 8px;
  background: #fff3cd;
  border: 1px solid #ffc107;
}

.batch-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.batch-btns {
  display: flex;
  gap: 8px;
}

/* 表格卡片 */
.table-card {
  border-radius: 8px;
  background: #ffffff;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.04);
}

.products-table {
  width: 100%;
}

/* 表头样式 */
.products-table :deep(.el-table__header-wrapper) {
  background: #eaecef;
}

.products-table :deep(.el-table__header th) {
  background: #eaecef;
  color: #2c3e50;
  font-weight: 600;
  font-size: 15px;
  padding: 12px 16px;
}

/* 表格行样式 */
.products-table :deep(.el-table__body tr) {
  height: 48px;
}

.products-table :deep(.el-table__body td) {
  padding: 12px 16px;
  vertical-align: middle;
}

.products-table :deep(.el-table__body tr:hover) {
  background: #ecf0f1;
}

.products-table :deep(.el-table__row) {
  background: #ffffff;
}

.products-table :deep(.el-table--striped .el-table__body tr.el-table__row--striped) {
  background: #f8f9fa;
}

.products-table :deep(.el-table__border) {
  border: none;
}

.products-table :deep(.el-table__inner-wrapper::before) {
  display: none;
}

.products-table :deep(.el-table td) {
  border-bottom: 1px solid #e6e6e6;
}

/* 商品信息 */
.product-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.product-thumb {
  width: 40px;
  height: 40px;
  border-radius: 4px;
  object-fit: cover;
  border: 1px solid #e6e6e6;
}

.product-thumb-placeholder {
  width: 40px;
  height: 40px;
  border-radius: 4px;
  background: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #bdc3c7;
  border: 1px solid #e6e6e6;
}

.product-name {
  font-size: 14px;
  color: #2c3e50;
  font-weight: 500;
}

.product-name-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
}

.special-tag {
  border-radius: 999px;
  font-weight: 600;
  letter-spacing: 0.5px;
}

.special-help {
  margin-left: 10px;
  font-size: 12px;
  color: #909399;
}

/* 原价显示为红色（包含标签与输入内容） */
.price-original-item :deep(.el-form-item__label) {
  color: #e74c3c;
}

.price-original-input :deep(.el-input__inner),
.price-original-input :deep(.el-input-number__inner) {
  color: #e74c3c;
}

/* 价格 */
.price-text {
  font-size: 14px;
  color: #2c3e50;
  font-weight: 500;
  text-align: right;
  display: block;
}

/* 库存预警 */
.stock-warning {
  color: #e74c3c;
  font-weight: 600;
}

.warning-icon {
  color: #e74c3c;
  margin-left: 4px;
  font-size: 16px;
}

/* 销量 */
.sales-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.sales-count {
  font-size: 14px;
  color: #2c3e50;
  min-width: 40px;
}

.sales-progress {
  flex: 1;
  max-width: 80px;
}

.sales-progress :deep(.el-progress-bar__outer) {
  background: #ecf0f1;
}

.sales-progress :deep(.el-progress-bar__inner) {
  background: linear-gradient(90deg, #2ecc71, #27ae60);
}

/* 状态标签 */
.status-tag {
  border-radius: 12px;
  padding: 4px 12px;
  font-size: 12px;
  border: none;
}

.status-tag :deep(.el-tag__content) {
  font-weight: 500;
}

/* 已上架 - 绿色 */
.products-table :deep(.el-tag--success) {
  background: #27ae60;
  color: #ffffff;
}

/* 已下架 - 橙色 */
.products-table :deep(.el-tag--warning) {
  background: #e67e22;
  color: #ffffff;
}

/* 操作按钮 */
.products-table :deep(.el-button--primary.is-link) {
  color: #3498db;
}

.products-table :deep(.el-button--warning.is-link) {
  color: #e67e22;
}

.products-table :deep(.el-button--success.is-link) {
  color: #27ae60;
}

.products-table :deep(.el-button.is-link) {
  padding: 4px 8px;
  font-size: 13px;
}

.products-table :deep(.el-button.is-link:hover) {
  transform: scale(1.05);
}

.products-table :deep(.el-button.is-link:active) {
  transform: scale(0.98);
}

/* 商品编辑对话框相关样式 */
.image-uploader {
  width: 178px;
}

.image-uploader :deep(.el-upload) {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: all 0.3s;
  width: 178px;
  height: 178px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.image-uploader :deep(.el-upload:hover) {
  border-color: #2ecc71;
}

.uploader-icon {
  font-size: 28px;
  color: #8c939d;
}

.uploaded-image {
  width: 178px;
  height: 178px;
  object-fit: cover;
  display: block;
}

.el-upload__tip {
  color: #606266;
  font-size: 12px;
  margin-top: 8px;
}

.specs-section {
  width: 100%;
}

.spec-item {
  display: grid;
  grid-template-columns: 2.2fr 1.2fr 1.2fr 1.2fr;
  grid-auto-rows: minmax(40px, auto);
  column-gap: 12px;
  row-gap: 8px;
  margin-bottom: 12px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 4px;
}

.spec-field {
  width: 100%;
}

.spec-name-field {
  grid-column: 1 / 5; /* 第一行占满所有列 */
}

.spec-with-label {
  display: flex;
  flex-direction: column;
}

.spec-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.spec-number-field {
  width: 100%;
}

.spec-sort-field {
  width: 100%;
}

.spec-name-field :deep(.el-input__wrapper),
.spec-number-field :deep(.el-input-number__wrapper),
.spec-sort-field :deep(.el-input-number__wrapper) {
  width: 100%;
  min-height: 44px;
  font-size: 15px;
}

.spec-name-field :deep(.el-input__inner) {
  font-size: 15px;
}

.spec-number-field :deep(.el-input-number__inner),
.spec-sort-field :deep(.el-input-number__inner) {
  font-size: 15px;
}

.spec-delete-btn {
  align-self: center;
  height: 32px;
}

.spec-tip {
  margin-top: 12px;
  font-size: 12px;
  color: #8c8c8c;
}

/* 响应式适配 */
@media (max-width: 1200px) {
  .products-table :deep(.el-table__body-wrapper) {
    overflow-x: auto;
  }
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .filter-form {
    flex-direction: column;
  }

  .filter-form .el-form-item {
    margin-right: 0;
  }
}

.image-uploader {
  width: 178px;
}

.image-uploader :deep(.el-upload) {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: all 0.3s;
  width: 178px;
  height: 178px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.image-uploader :deep(.el-upload:hover) {
  border-color: #409eff;
}

.uploader-icon {
  font-size: 28px;
  color: #8c939d;
}

.uploaded-image {
  width: 178px;
  height: 178px;
  object-fit: cover;
  display: block;
}

.el-upload__tip {
  color: #606266;
  font-size: 12px;
  margin-top: 8px;
}

.specs-section {
  width: 100%;
}

.spec-tip {
  margin-top: 12px;
  font-size: 12px;
  color: #8c8c8c;
}
</style>

