<template>
  <div class="profile-page">
    <el-tabs v-model="activeTab" class="profile-tabs">
      <el-tab-pane label="个人信息" name="info">
        <el-card class="profile-card" shadow="hover">
          <el-alert
            v-if="infoHighlight"
            title="请完善您的个人信息"
            type="warning"
            show-icon
            class="info-highlight-alert"
            description="头像、昵称、手机和邮箱越完整，收货通知和售后服务会更顺畅。"
          />
          <el-form :model="form" label-width="90px" class="profile-form">
            <el-form-item label="头像">
              <div class="avatar-upload">
                <el-avatar :size="100" :src="getAvatarUrl(form.avatar)" class="profile-avatar">
                  <span>{{ form.nickname?.charAt(0) || form.username?.charAt(0) || 'U' }}</span>
                </el-avatar>
                <div class="avatar-hover-tip">
                  <el-upload
                    class="avatar-uploader"
                    :action="uploadAction"
                    :show-file-list="false"
                    :on-success="handleAvatarSuccess"
                    :before-upload="beforeAvatarUpload"
                    :headers="uploadHeaders"
                  >
                    <el-button type="primary" size="small" class="ghost-btn">
                      更换头像
                    </el-button>
                  </el-upload>
                </div>
              </div>
            </el-form-item>
            <div class="profile-form-grid">
              <el-form-item label="用户名">
                <el-input v-model="form.username" disabled />
              </el-form-item>
              <el-form-item label="昵称">
                <el-input v-model="form.nickname" placeholder="请输入昵称" />
              </el-form-item>
              <el-form-item label="手机号">
                <el-input v-model="form.phone" placeholder="请输入手机号" />
              </el-form-item>
              <el-form-item label="邮箱">
                <el-input v-model="form.email" placeholder="请输入邮箱" />
              </el-form-item>
            </div>
            <el-form-item class="form-actions">
              <el-button type="primary" class="primary-btn" @click="saveProfile" :loading="saving">
                保存
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="修改密码" name="password">
        <el-card class="profile-card" shadow="hover">
          <el-form
            :model="passwordForm"
            :rules="passwordRules"
            ref="passwordFormRef"
            label-width="90px"
            class="password-form"
          >
            <el-form-item label="原密码" prop="oldPassword">
              <el-input
                v-model="passwordForm.oldPassword"
                type="password"
                show-password
                placeholder="🔒  请输入原密码"
              />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input
                v-model="passwordForm.newPassword"
                type="password"
                show-password
                placeholder="🔏  请输入新密码"
              />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input
                v-model="passwordForm.confirmPassword"
                type="password"
                show-password
                placeholder="✅  请再次输入新密码"
              />
            </el-form-item>
            <el-form-item class="form-actions">
              <el-button
                type="primary"
                class="primary-btn"
                @click="changePassword"
                :loading="changingPassword"
              >
                修改密码
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="收货地址" name="address">
        <el-card class="profile-card" shadow="hover">
          <template #header>
            <div class="address-header">
              <span>我的收货地址</span>
              <el-button
                type="primary"
                size="small"
                class="primary-btn"
                @click="showAddressDialog()"
              >
                + 新增地址
              </el-button>
            </div>
          </template>
          <div v-if="addresses.length === 0" class="empty-address">
            <el-empty description="暂无收货地址" />
          </div>
          <div v-else class="address-list">
            <div v-for="addr in addresses" :key="addr.id" class="address-item">
              <div class="address-info">
                <div class="address-top">
                  <span class="receiver">{{ addr.receiverName }}</span>
                  <span class="phone">{{ addr.receiverPhone }}</span>
                  <span v-if="addr.isDefault" class="tag-default">默认</span>
                </div>
                <div class="address-detail">
                  {{ addr.province }} {{ addr.city }} {{ addr.district }} {{ addr.detailAddress }}
                </div>
              </div>
              <div class="address-actions">
                <el-button type="text" class="text-btn" @click="showAddressDialog(addr)">✏️ 编辑</el-button>
                <el-button type="text" class="text-btn danger" @click="deleteAddress(addr.id)">
                  🗑️ 删除
                </el-button>
                <el-button
                  v-if="!addr.isDefault"
                  type="text"
                  class="text-btn"
                  @click="setDefault(addr.id)"
                >
                  设为默认
                </el-button>
              </div>
            </div>
          </div>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="我的收藏" name="favorites">
        <el-card class="profile-card" shadow="hover">
          <el-tabs v-model="favoriteTab">
            <el-tab-pane label="收藏商品" name="products">
              <div class="favorite-list">
                <el-row :gutter="20">
                  <el-col
                    v-for="item in favoriteProducts"
                    :key="item.id"
                    :xs="12"
                    :sm="8"
                    :md="6"
                    :lg="6"
                  >
                    <el-card class="favorite-item" shadow="hover">
                      <div class="item-image">
                        <img
                          v-if="item.product?.mainImage"
                          :src="getImageUrl(item.product.mainImage)"
                          :alt="item.product.name"
                        />
                        <div v-else class="no-image">暂无图片</div>
                      </div>
                      <div class="item-info">
                        <h4>{{ item.product?.name || '商品已下架' }}</h4>
                        <div class="item-price">¥{{ item.product?.price || 0 }}</div>
                        <!-- 所属店铺信息 -->
                        <div
                          v-if="
                            item.product?.merchant ||
                            item.product?.merchantName ||
                            item.product?.merchantAvatar
                          "
                          class="item-merchant"
                        >
                          <div
                            class="merchant-avatar"
                            v-if="item.product?.merchant?.avatar || item.product?.merchantAvatar"
                          >
                            <img
                              :src="
                                getImageUrl(
                                  item.product?.merchant?.avatar || item.product?.merchantAvatar
                                )
                              "
                              :alt="
                                item.product?.merchant?.shopName ||
                                item.product?.merchantName ||
                                '店铺头像'
                              "
                            />
                          </div>
                          <div class="merchant-text">
                            <div class="merchant-name">
                              {{
                                item.product?.merchant?.shopName ||
                                item.product?.merchantName ||
                                '所属店铺'
                              }}
                            </div>
                            <div class="merchant-tag">店铺</div>
                          </div>
                        </div>
                      </div>
                      <div class="item-actions">
                        <el-button
                          type="primary"
                          size="small"
                          class="primary-btn"
                          @click.stop="buyFavoriteProduct(item.productId)"
                        >
                          立即购买
                        </el-button>
                        <el-button size="small" class="secondary-btn" @click.stop="removeFavoriteProduct(item.productId)">
                          取消收藏
                        </el-button>
                      </div>
                    </el-card>
                  </el-col>
                </el-row>
                <el-empty v-if="favoriteProducts.length === 0" description="暂无收藏商品" />
              </div>
            </el-tab-pane>
            <el-tab-pane label="收藏店铺" name="merchants">
              <div class="favorite-list">
                <el-row :gutter="20">
                  <el-col
                    v-for="item in favoriteMerchants"
                    :key="item.id"
                    :xs="12"
                    :sm="8"
                    :md="6"
                    :lg="6"
                  >
                    <el-card class="favorite-item" shadow="hover">
                      <div class="item-info merchant-info">
                        <div class="merchant-header">
                          <div class="merchant-avatar">
                            <img
                              v-if="item.merchant?.avatar"
                              :src="getImageUrl(item.merchant.avatar)"
                              :alt="item.merchant?.shopName || '店铺头像'"
                            />
                            <div v-else class="avatar-placeholder">
                              {{ (item.merchant?.shopName || '店铺').slice(0, 1) }}
                            </div>
                          </div>
                          <div class="merchant-main">
                            <h4>{{ item.merchant?.shopName || '店铺已关闭' }}</h4>
                            <p class="item-desc">
                              {{ item.merchant?.shopDescription || '暂无描述' }}
                            </p>
                          </div>
                        </div>
                      </div>
                      <div class="item-actions">
                        <el-button
                          size="small"
                          class="secondary-btn"
                          @click.stop="removeFavoriteMerchant(item.merchantId)"
                        >
                          取消收藏
                        </el-button>
                      </div>
                    </el-card>
                  </el-col>
                </el-row>
                <el-empty v-if="favoriteMerchants.length === 0" description="暂无收藏店铺" />
              </div>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="售后记录" name="aftersale">
        <el-card class="profile-card" shadow="hover">
          <div v-if="afterSales.length === 0">
            <el-empty description="暂无售后记录" />
          </div>
          <div v-else class="aftersale-list">
            <div v-for="item in afterSales" :key="item.id" class="aftersale-item">
              <div class="aftersale-header">
                <span class="order-no">订单号：{{ item.orderNo }}</span>
                <span class="status-pill" :class="'status-' + getAfterSaleStatusType(item.status)">
                  {{ getAfterSaleStatusText(item.status) }}
                </span>
              </div>
              <div class="aftersale-body">
                <div class="aftersale-thumb">
                  <img
                    v-if="item.productImage"
                    :src="getImageUrl(item.productImage)"
                    alt="商品图片"
                  />
                  <div v-else class="no-image">暂无图片</div>
                </div>
                <div class="aftersale-content">
                  <p class="line-strong"><strong>类型：</strong>{{ item.type === 1 ? '退货退款' : '仅退款' }}</p>
                  <p class="line-muted"><strong>原因：</strong>{{ item.reason }}</p>
                  <p v-if="item.merchantReply" class="line-merchant">
                    <strong>商家回复：</strong>{{ item.merchantReply }}
                  </p>
                  <p v-if="item.description" class="line-user">
                    <strong>说明：</strong>{{ item.description }}
                  </p>
                </div>
              </div>
              <div class="aftersale-actions">
                <el-button
                  v-if="item.type === 1 && item.status === 1"
                  size="small"
                  type="primary"
                  @click="openReturnDialog(item)"
                >
                  我已退货，填写物流单
                </el-button>
                <el-button
                  v-if="item.status === 2"
                  size="small"
                  type="danger"
                  @click="openAppealDialog(item)"
                >
                  申请管理员介入
                </el-button>
              </div>
            </div>
          </div>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <!-- 地址编辑对话框 -->
    <el-dialog v-model="addressDialogVisible" :title="editingAddress ? '编辑地址' : '新增地址'" width="500px">
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
        <el-button type="primary" @click="saveAddress" :loading="savingAddress">保存</el-button>
      </template>
    </el-dialog>

    <!-- 退货物流填写对话框 -->
    <el-dialog v-model="returnDialogVisible" title="填写退货物流信息" width="480px">
      <el-form :model="returnForm" label-width="100px">
        <el-form-item label="物流公司" required>
          <el-input v-model="returnForm.company" placeholder="例如：顺丰速运 / 圆通快递" />
        </el-form-item>
        <el-form-item label="物流单号" required>
          <el-input v-model="returnForm.no" placeholder="请输入物流单号" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="returnForm.remark"
            type="textarea"
            :rows="3"
            placeholder="可填写退货说明、取件时间等（选填）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="returnDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReturn" :loading="submittingReturn">提交</el-button>
      </template>
    </el-dialog>

    <!-- 申诉对话框 -->
    <el-dialog v-model="appealDialogVisible" title="申请管理员介入" width="520px">
      <el-form :model="appealForm" label-width="100px">
        <el-form-item label="申诉原因" required>
          <el-input
            v-model="appealForm.reason"
            type="textarea"
            :rows="3"
            placeholder="请详细描述您和商家分歧的具体情况"
          />
        </el-form-item>
        <el-form-item label="图片证据">
          <el-upload
            class="appeal-uploader"
            :action="appealUploadAction"
            :headers="appealUploadHeaders"
            list-type="picture-card"
            accept="image/*"
            :before-upload="beforeAppealImageUpload"
            :on-success="handleAppealUploadSuccess"
            :on-remove="handleAppealUploadRemove"
            :file-list="appealFileList"
            :limit="6"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
          <div class="upload-tip">最多上传 6 张，支持 jpg/png，单张不超过 5MB</div>
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="appealForm.contact" placeholder="方便管理员联系您（选填）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="appealDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAppeal" :loading="submittingAppeal">提交申诉</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, onActivated, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '@/utils/api'
import { Plus } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()

const userStore = useUserStore()
const activeTab = ref('info')
// 是否高亮提示“完善个人信息”
const infoHighlight = ref(false)
const saving = ref(false)
const changingPassword = ref(false)
const passwordFormRef = ref(null)
const addressFormRef = ref(null)

// 个人信息表单
const form = ref({
  username: '',
  nickname: '',
  phone: '',
  email: '',
  avatar: ''
})

// 修改密码表单
const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.value.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 地址相关
const addresses = ref([])
const addressDialogVisible = ref(false)
const editingAddress = ref(null)
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
  receiverPhone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  province: [{ required: true, message: '请输入省份', trigger: 'blur' }],
  city: [{ required: true, message: '请输入城市', trigger: 'blur' }],
  district: [{ required: true, message: '请输入区县', trigger: 'blur' }],
  detailAddress: [{ required: true, message: '请输入详细地址', trigger: 'blur' }]
}

// 收藏
const favoriteTab = ref('products')
const favoriteProducts = ref([])
const favoriteMerchants = ref([])

// 售后记录
const afterSales = ref([])
const currentAfterSale = ref(null)

// 退货物流填写
const returnDialogVisible = ref(false)
const submittingReturn = ref(false)
const returnForm = ref({
  company: '',
  no: '',
  remark: ''
})

// 申诉
const appealDialogVisible = ref(false)
const submittingAppeal = ref(false)
const appealForm = ref({
  reason: '',
  contact: ''
})
const appealUploadAction = '/api/file/upload'
const appealUploadHeaders = {
  Authorization: `Bearer ${userStore.token}`
}
const appealFileList = ref([])
const appealImageUrls = ref([])

// 根据路由参数控制当前tab和高亮提示
const applyRouteParams = () => {
  const tab = route.query.tab
  if (typeof tab === 'string' && tab) {
    activeTab.value = tab
  }
  const highlight = route.query.highlight
  infoHighlight.value = highlight === 'info' || highlight === 'profile'
}

const loadProfile = () => {
  if (userStore.user) {
    form.value = {
      username: userStore.user.username || '',
      nickname: userStore.user.nickname || '',
      phone: userStore.user.phone || '',
      email: userStore.user.email || '',
      avatar: userStore.user.avatar || ''
    }
  }
}

// 头像上传相关
const uploadAction = '/api/file/upload'
const uploadHeaders = {
  Authorization: `Bearer ${userStore.token}`
}

const getAvatarUrl = (avatar) => {
  if (!avatar) return ''
  if (avatar.startsWith('http://') || avatar.startsWith('https://')) {
    return avatar
  }
  if (avatar.startsWith('/uploads/')) {
    return '/api' + avatar
  }
  if (avatar.startsWith('/api/uploads/')) {
    return avatar
  }
  return avatar
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

const beforeAppealImageUpload = (file) => {
  const MAX_IMAGE_SIZE_MB = 5
  const isImage = file.type?.startsWith('image/')
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
    // 确保头像URL正确设置
    const avatarUrl = response.data.url || response.data
    form.value.avatar = avatarUrl
    // 立即更新预览，同时更新userStore中的头像（临时）
    if (userStore.user) {
      const tempUser = { ...userStore.user, avatar: avatarUrl }
      userStore.setUser(tempUser)
    }
    ElMessage.success('头像上传成功，请点击保存按钮保存')
  } else {
    ElMessage.error(response.message || '头像上传失败')
  }
}

const saveProfile = async () => {
  saving.value = true
  try {
    // 确保头像字段被正确传递
    const profileData = {
      ...form.value,
      avatar: form.value.avatar || userStore.user?.avatar || null
    }
    console.log('保存的用户信息:', profileData)
    
    const res = await api.put('/user/profile', profileData)
    if (res.code === 200) {
      ElMessage.success('保存成功')
      
      // 重新从服务器获取最新用户信息，确保数据完全同步
      try {
        const userInfoRes = await api.get('/user/info')
        if (userInfoRes.code === 200 && userInfoRes.data) {
          console.log('从服务器获取的最新用户信息:', userInfoRes.data)
          userStore.setUser(userInfoRes.data)
          loadProfile()
        } else {
          // 如果重新获取失败，使用返回的数据
          const updatedUser = {
            ...userStore.user,
            ...res.data,
            avatar: profileData.avatar || res.data?.avatar || userStore.user?.avatar
          }
          userStore.setUser(updatedUser)
          loadProfile()
        }
      } catch (error) {
        console.error('重新获取用户信息失败:', error)
        // 即使重新获取失败，也使用已更新的数据
        const updatedUser = {
          ...userStore.user,
          ...res.data,
          avatar: profileData.avatar || res.data?.avatar || userStore.user?.avatar
        }
        userStore.setUser(updatedUser)
        loadProfile()
      }
    } else {
      ElMessage.error(res.message || '保存失败')
    }
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

const changePassword = async () => {
  try {
    await passwordFormRef.value.validate()
    changingPassword.value = true
    const res = await api.post('/user/changePassword', {
      oldPassword: passwordForm.value.oldPassword,
      newPassword: passwordForm.value.newPassword
    })
    if (res.code === 200) {
      ElMessage.success('密码修改成功，请重新登录')
      passwordForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
      userStore.logout()
    } else {
      ElMessage.error(res.message || '修改失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('修改失败')
    }
  } finally {
    changingPassword.value = false
  }
}

const loadAddresses = async () => {
  try {
    const res = await api.get('/address/list')
    if (res.code === 200) {
      addresses.value = res.data || []
    }
  } catch (error) {
    console.error('加载地址失败', error)
  }
}

const showAddressDialog = (addr = null) => {
  editingAddress.value = addr
  if (addr) {
    addressForm.value = { ...addr, isDefault: addr.isDefault === 1 }
  } else {
    addressForm.value = {
      receiverName: '',
      receiverPhone: '',
      province: '',
      city: '',
      district: '',
      detailAddress: '',
      isDefault: false
    }
  }
  addressDialogVisible.value = true
}

const saveAddress = async () => {
  try {
    await addressFormRef.value.validate()
    savingAddress.value = true
    const data = { ...addressForm.value, isDefault: addressForm.value.isDefault ? 1 : 0 }
    let res
    if (editingAddress.value) {
      res = await api.put('/address/update', data)
    } else {
      res = await api.post('/address/add', data)
    }
    if (res.code === 200) {
      ElMessage.success('保存成功')
      addressDialogVisible.value = false
      loadAddresses()
    } else {
      ElMessage.error(res.message || '保存失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('保存失败')
    }
  } finally {
    savingAddress.value = false
  }
}

const deleteAddress = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这个地址吗？', '提示', { type: 'warning' })
    const res = await api.delete(`/address/delete/${id}`)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadAddresses()
    }
  } catch (error) {
    // 用户取消
  }
}

const setDefault = async (id) => {
  try {
    const res = await api.post(`/address/setDefault/${id}`)
    if (res.code === 200) {
      ElMessage.success('设置成功')
      loadAddresses()
    }
  } catch (error) {
    ElMessage.error('设置失败')
  }
}

const loadAfterSales = async () => {
  try {
    const res = await api.get('/aftersale/user/list')
    if (res.code === 200) {
      afterSales.value = res.data || []
    }
  } catch (error) {
    console.error('加载售后记录失败', error)
  }
}

const getAfterSaleStatusText = (status) => {
  // 0-待商家处理，1-待退货（商家已同意退货退款），2-商家已驳回，3-已完成，4-待管理员处理
  const map = {
    0: '待商家处理',
    1: '待退货',
    2: '商家已驳回',
    3: '已完成',
    4: '待管理员处理'
  }
  return map[status] || '未知'
}

const getAfterSaleStatusType = (status) => {
  const map = {
    0: 'warning',  // 待商家处理
    1: 'primary',  // 待退货
    2: 'danger',   // 商家已驳回
    3: 'success',  // 已完成
    4: 'warning'   // 待管理员处理
  }
  return map[status] || 'info'
}

// 打开退货物流对话框
const openReturnDialog = (item) => {
  currentAfterSale.value = item
  returnForm.value = {
    company: '',
    no: '',
    remark: ''
  }
  returnDialogVisible.value = true
}

const submitReturn = async () => {
  if (!returnForm.value.company || !returnForm.value.no) {
    ElMessage.warning('请填写完整的物流公司和单号')
    return
  }
  submittingReturn.value = true
  try {
    const res = await api.post(`/aftersale/confirm-return/${currentAfterSale.value.id}`, {
      logisticsCompany: returnForm.value.company,
      logisticsNo: returnForm.value.no
    })
    if (res.code === 200) {
      ElMessage.success('已提交退货物流信息，等待退款完成')
      returnDialogVisible.value = false
      loadAfterSales()
    } else {
      ElMessage.error(res.message || '提交失败')
    }
  } catch (error) {
    ElMessage.error('提交失败')
  } finally {
    submittingReturn.value = false
  }
}

// 打开申诉对话框
const openAppealDialog = (item) => {
  currentAfterSale.value = item
  appealForm.value = {
    reason: '',
    contact: ''
  }
  appealFileList.value = []
  appealImageUrls.value = []
  appealDialogVisible.value = true
}

// 用户对被驳回的售后发起申诉
const submitAppeal = async () => {
  if (!appealForm.value.reason) {
    ElMessage.warning('请先填写申诉原因')
    return
  }
  submittingAppeal.value = true
  try {
    // 将多项信息拼接成一段结构化文本，后台仍然按一个 reason 接收
    let reasonText = `【申诉原因】${appealForm.value.reason.trim()}`
    if (appealForm.value.contact) {
      reasonText += `\n【联系电话】${appealForm.value.contact.trim()}`
    }

    const res = await api.post(`/aftersale/appeal/${currentAfterSale.value.id}`, {
      reason: reasonText,
      images: appealImageUrls.value.join(',')
    })
    if (res.code === 200) {
      ElMessage.success('申诉已提交，请等待管理员处理')
      appealDialogVisible.value = false
      loadAfterSales()
    } else {
      ElMessage.error(res.message || '提交失败')
    }
  } catch (error) {
    ElMessage.error('提交失败')
  } finally {
    submittingAppeal.value = false
  }
}

const handleAppealUploadSuccess = (response, file, fileList) => {
  if (response.code === 200 && response.data) {
    const url = response.data.url || response.data
    appealImageUrls.value.push(url)
    appealFileList.value = fileList
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error(response.message || '图片上传失败')
  }
}

const handleAppealUploadRemove = (file, fileList) => {
  appealFileList.value = fileList
  const url = file.response?.data?.url || file.response?.data || file.url
  appealImageUrls.value = appealImageUrls.value.filter(u => u !== url)
}

const loadFavorites = async () => {
  // 先清空之前的收藏列表，避免显示其他用户的数据
  favoriteProducts.value = []
  favoriteMerchants.value = []
  
  // 如果用户未登录，直接返回
  if (!userStore.user || !userStore.token) {
    return
  }
  
  try {
    const productRes = await api.get('/favorite/product/list')
    if (productRes.code === 200) {
      // 后端已按当前用户过滤；前端只做类型兜底，避免 userId(数字) 与 currentUserId(字符串) 导致过滤为空
      const currentUserId = userStore.user?.id
      if (currentUserId != null) {
        favoriteProducts.value = (productRes.data || []).filter(item =>
          Number(item.userId) === Number(currentUserId)
        )
      } else {
        favoriteProducts.value = productRes.data || []
      }
    }
    const merchantRes = await api.get('/favorite/merchant/list')
    if (merchantRes.code === 200) {
      // 同上：避免类型不一致导致收藏店铺列表被过滤为空
      const currentUserId = userStore.user?.id
      if (currentUserId != null) {
        favoriteMerchants.value = (merchantRes.data || []).filter(item =>
          Number(item.userId) === Number(currentUserId)
        )
      } else {
        favoriteMerchants.value = merchantRes.data || []
      }
    }
  } catch (error) {
    console.error('加载收藏失败', error)
    // 如果加载失败，确保列表为空
    favoriteProducts.value = []
    favoriteMerchants.value = []
  }
}

const buyFavoriteProduct = async (productId) => {
  try {
    // 先添加到购物车，然后跳转到结算页面
    await api.post('/cart/add', {
      productId: productId,
      specId: null,
      quantity: 1
    })
    ElMessage.success('已添加到购物车')
    // 跳转到结算页面
    router.push('/checkout')
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const removeFavoriteProduct = async (productId) => {
  try {
    const res = await api.delete(`/favorite/product/remove/${productId}`)
    if (res.code === 200) {
      ElMessage.success('取消收藏成功')
      loadFavorites()
    } else {
      ElMessage.error(res.message || '取消收藏失败')
    }
  } catch (error) {
    const errorMsg = error.response?.data?.message || error.message || '操作失败'
    ElMessage.error(errorMsg)
    console.error('取消收藏失败:', error)
  }
}

const removeFavoriteMerchant = async (merchantId) => {
  try {
    const res = await api.delete(`/favorite/merchant/remove/${merchantId}`)
    if (res.code === 200) {
      ElMessage.success('取消收藏成功')
      loadFavorites()
      // 通知其他页面（店铺列表、店铺详情等）同步更新收藏状态
      window.dispatchEvent(new CustomEvent('favorite-merchants-changed'))
    } else {
      ElMessage.error(res.message || '取消收藏失败')
    }
  } catch (error) {
    const errorMsg = error.response?.data?.message || error.message || '操作失败'
    ElMessage.error(errorMsg)
    console.error('取消收藏失败:', error)
  }
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

// 头像上传相关（已在上面定义，这里保留getImageUrl用于其他图片）

// 监听用户变化，当用户切换时重新加载数据
watch(() => userStore.user?.id, (newUserId, oldUserId) => {
  if (newUserId !== oldUserId) {
    // 用户切换了，清空所有数据并重新加载
    favoriteProducts.value = []
    favoriteMerchants.value = []
    addresses.value = []
    afterSales.value = []
    if (newUserId) {
      loadProfile()
      loadAddresses()
      loadAfterSales()
      loadFavorites()
    }
  }
}, { immediate: false })

// 监听activeTab变化，切换到收藏tab时重新加载
watch(() => activeTab.value, (newTab, oldTab) => {
  if (newTab === 'favorites' && newTab !== oldTab) {
    // 切换tab时先清空，再加载
    favoriteProducts.value = []
    favoriteMerchants.value = []
    loadFavorites()
  }
})

// 组件激活时（比如从其他页面返回），重新加载收藏数据
onActivated(() => {
  if (userStore.user && activeTab.value === 'favorites') {
    // 先清空，再加载
    favoriteProducts.value = []
    favoriteMerchants.value = []
    loadFavorites()
  }
})

onMounted(() => {
  // 先清空所有数据，确保不会显示其他用户的数据
  favoriteProducts.value = []
  favoriteMerchants.value = []
  addresses.value = []
  afterSales.value = []
  
  // 检查用户是否登录
  if (userStore.user && userStore.token) {
    loadProfile()
    loadAddresses()
    loadAfterSales()
    loadFavorites()
  }

  // 监听收藏店铺变更事件（从列表/详情收藏后回到个人中心自动刷新）
  const onFavChanged = () => {
    if (userStore.user && userStore.token) {
      loadFavorites()
    }
  }
  window.addEventListener('favorite-merchants-changed', onFavChanged)

  onBeforeUnmount(() => {
    window.removeEventListener('favorite-merchants-changed', onFavChanged)
  })

  // 应用路由参数（例如从消息中心跳转到指定tab并高亮提示）
  applyRouteParams()
})
</script>

<style scoped>
.profile-page {
  padding: 24px;
  background: transparent;
  animation: fadeIn 0.3s ease;
}

.profile-tabs :deep(.el-tabs__nav-wrap) {
  margin-bottom: 16px;
}

.profile-tabs :deep(.el-tabs__item) {
  padding: 12px 24px;
  font-size: 15px;
  color: var(--text-color-secondary);
  transition: color 0.2s ease;
}

.profile-tabs :deep(.el-tabs__item.is-active) {
  color: var(--el-color-primary);
  font-weight: 600;
}

.profile-tabs :deep(.el-tabs__active-bar) {
  height: 3px;
  border-radius: 999px;
  background-color: var(--el-color-primary);
}

.profile-card {
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.info-highlight-alert {
  margin-bottom: 16px;
  border-radius: 8px;
}

.profile-form {
  max-width: 720px;
}

.profile-form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  column-gap: 24px;
}

.profile-form-grid :deep(.el-form-item) {
  margin-bottom: 16px;
}

.profile-avatar {
  border: 3px solid var(--el-color-primary);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.address-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.empty-address {
  padding: 40px 0;
}

.address-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.address-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-radius: 10px;
  background: #fafafa;
  border: 1px solid #eeeeee;
}

.address-info {
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

.tag-default {
  background: #e8f5e9;
  color: #4caf50;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
}

.address-detail {
  color: #8c8c8c;
}

.address-actions {
  display: flex;
  gap: 8px;
}

.text-btn {
  font-size: 13px;
  color: var(--el-color-primary);
  transition: color 0.2s ease;
}

.text-btn.danger {
  color: #f44336;
}

.text-btn:hover {
  filter: brightness(0.9);
}

.aftersale-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.aftersale-item {
  padding: 16px;
  border-radius: 10px;
  background: #fafafa;
  border: 1px solid #eeeeee;
}

.aftersale-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.order-no {
  font-weight: 600;
  font-size: 15px;
}

.status-pill {
  padding: 4px 12px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 500;
}

.status-success {
  background: #e8f5e9;
  color: #4caf50;
}

.status-primary {
  background: #e3f2fd;
  color: #2196f3;
}

.status-danger {
  background: #ffebee;
  color: #f44336;
}

.status-warning {
  background: #fff8e1;
  color: #ff9800;
}

.aftersale-body {
  display: flex;
  gap: 12px;
}

.aftersale-thumb {
  width: 80px;
  height: 80px;
  border-radius: 6px;
  overflow: hidden;
  background: #f5f5f5;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.aftersale-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.aftersale-thumb .no-image {
  font-size: 12px;
  color: #999;
}

.aftersale-content p {
  margin: 8px 0;
  color: #595959;
}

.line-strong {
  color: #333333;
}

.line-muted {
  color: #666666;
}

.line-merchant {
  color: #666666;
}

.line-user {
  color: #2196f3;
}

.favorite-list {
  margin-top: 16px;
}

.favorite-item {
  margin-bottom: 20px;
  cursor: default;
  transition: all 0.3s;
}

.favorite-item:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.item-image {
  width: 100%;
  height: 150px;
  overflow: hidden;
  border-radius: 4px;
  margin-bottom: 12px;
  background: #f5f7fa;
}

.item-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.item-info {
  padding: 0 4px;
  margin-bottom: 12px;
}

.item-info h4 {
  font-size: 16px;
  color: #262626;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-price {
  font-size: 18px;
  color: #ff4d4f;
  font-weight: 600;
}

/* 收藏商品中的店铺信息 */
.item-merchant {
  margin-top: 8px;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #8c8c8c;
}

.item-merchant .merchant-avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
}

.item-merchant .merchant-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.item-merchant .merchant-text {
  display: flex;
  align-items: center;
  gap: 6px;
}

.merchant-name {
  max-width: 120px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  color: #595959;
}

.merchant-tag {
  padding: 0 6px;
  border-radius: 10px;
  background: #f6ffed;
  color: #52c41a;
  font-size: 11px;
  border: 1px solid #b7eb8f;
}

.item-desc {
  color: #8c8c8c;
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

/* 收藏店铺卡片中的头像布局 */
.merchant-info {
  padding: 8px 4px 12px;
}

.merchant-header {
  display: flex;
  align-items: center;
  gap: 10px;
}

.merchant-header .merchant-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  overflow: hidden;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.merchant-header .merchant-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.merchant-header .avatar-placeholder {
  font-size: 18px;
  color: #8c8c8c;
}

.merchant-main h4 {
  margin-bottom: 4px;
}

.item-actions {
  text-align: center;
}

.primary-btn {
  background: var(--el-color-primary);
  border-color: var(--el-color-primary);
  border-radius: 8px;
  padding: 10px 24px;
  transition: transform 0.2s ease, box-shadow 0.2s ease, background-color 0.2s ease;
}

.primary-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
  background: var(--el-color-primary-dark-2);
}

.secondary-btn {
  border-radius: 8px;
  background: #f5f5f5;
  border-color: #e0e0e0;
  color: #666666;
}

.secondary-btn:hover {
  background: #eeeeee;
  color: #333333;
}

.avatar-upload {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 10px;
}

.avatar-uploader {
  display: inline-block;
}

.ghost-btn {
  background: #fff;
  color: var(--el-color-primary);
  border-color: var(--el-color-primary-light-5);
}

.ghost-btn:hover {
  background: var(--el-color-primary-light-7);
}

.form-actions {
  margin-top: 8px;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
