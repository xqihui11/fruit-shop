<template>
  <div class="categories-page">
    <el-card class="categories-card" shadow="never">
      <template #header>
        <div class="header">
          <div class="title-wrap">
            <div class="title">分类管理</div>
            <div class="subtitle">维护商品分类结构与展示优先级</div>
          </div>
          <div class="header-actions">
            <el-input
              v-model="keyword"
              class="search-input"
              placeholder="搜索分类名称"
              clearable
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-button type="primary" @click="showDialog()">添加分类</el-button>
          </div>
        </div>
      </template>

      <div class="toolbar">
        <div class="toolbar-left">已选中 <strong>{{ selectedCategories.length }}</strong> 个分类</div>
        <div class="toolbar-right">
          <el-button :disabled="!selectedCategories.length" @click="handleBatchEnable">启用</el-button>
          <el-button :disabled="!selectedCategories.length" class="btn-warning" @click="handleBatchDisable">禁用</el-button>
          <el-button :disabled="!selectedCategories.length" class="btn-danger" @click="handleBatchDelete">删除</el-button>
        </div>
      </div>

      <el-table
        :data="pagedCategories"
        stripe
        class="categories-table"
        @selection-change="handleSelectionChange"
        @sort-change="handleSortChange"
      >
        <el-table-column type="selection" width="48" />
        <el-table-column prop="id" label="ID" width="80" sortable="custom" />

        <el-table-column label="分类名称" min-width="220" sortable="custom" prop="name">
          <template #default="{ row }">
            <div class="name-cell">
              <div class="icon-wrap">
                <span class="icon-text">{{ getIconText(row.name) }}</span>
              </div>
              <div class="name-meta">
                <div class="name-main">{{ row.name }}</div>
                <div class="name-sub">父级：{{ getParentName(row.parentId) }}</div>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="sortOrder" label="排序" width="100" sortable="custom" align="right">
          <template #default="{ row }">
            <span class="sort-text">{{ row.sortOrder ?? 0 }}</span>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <span class="status-pill" :class="row.status === 1 ? 'status-enabled' : 'status-disabled'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </span>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="showDialog(row)">✏️ 编辑</el-button>
            <el-button link class="btn-danger" @click="deleteCategory(row.id)">🗑️ 删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <div class="pagination-wrap">
      <el-pagination
        background
        layout="prev, pager, next"
        :current-page="currentPage"
        :page-size="pageSize"
        :total="sortedCategories.length"
        @current-change="(p) => (currentPage = p)"
      />
    </div>

    <!-- 编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="editingCategory ? '编辑分类' : '添加分类'" width="420px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="90px">
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="父级分类">
          <el-select v-model="form.parentId" placeholder="无（顶级分类）" clearable>
            <el-option :value="0" label="无（顶级分类）" />
            <el-option v-for="c in categories" :key="c.id" :value="c.id" :label="c.name" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveCategory">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import api from '@/utils/api'

const categories = ref([])
const dialogVisible = ref(false)
const editingCategory = ref(null)
const formRef = ref(null)
const saving = ref(false)
const keyword = ref('')
const selectedCategories = ref([])
const sortState = ref({ prop: '', order: '' })
const currentPage = ref(1)
const pageSize = ref(10)

const form = ref({
  id: null,
  name: '',
  parentId: 0,
  sortOrder: 0,
  status: 1
})

const rules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }]
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

const getIconText = (name) => {
  if (!name) return '类'
  return name.trim().charAt(0)
}

const getParentName = (parentId) => {
  if (!parentId || parentId === 0) return '顶级分类'
  const parent = categories.value.find((c) => c.id === parentId)
  return parent ? parent.name : '未知'
}

const filteredCategories = computed(() => {
  let list = [...categories.value]
  if (keyword.value) {
    const k = keyword.value.toLowerCase()
    list = list.filter((c) => (c.name || '').toLowerCase().includes(k))
  }
  return list
})

const sortedCategories = computed(() => {
  const list = [...filteredCategories.value]
  const { prop, order } = sortState.value
  if (!prop || !order) {
    return list.sort((a, b) => (a.sortOrder || 0) - (b.sortOrder || 0))
  }
  const factor = order === 'ascending' ? 1 : -1
  return list.sort((a, b) => {
    const va = a[prop]
    const vb = b[prop]
    if (va == null && vb == null) return 0
    if (va == null) return -1 * factor
    if (vb == null) return 1 * factor
    if (!Number.isNaN(Number(va)) && !Number.isNaN(Number(vb))) {
      return (Number(va) - Number(vb)) * factor
    }
    return String(va).localeCompare(String(vb)) * factor
  })
})

const pagedCategories = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return sortedCategories.value.slice(start, start + pageSize.value)
})

const showDialog = (category = null) => {
  editingCategory.value = category
  if (category) {
    form.value = { ...category }
  } else {
    form.value = { id: null, name: '', parentId: 0, sortOrder: 0, status: 1 }
  }
  dialogVisible.value = true
}

const saveCategory = async () => {
  try {
    await formRef.value.validate()
    saving.value = true
    let res
    const payload = { ...form.value }
    if (editingCategory.value && payload.id) {
      res = await api.put('/admin/category/update', payload)
    } else {
      res = await api.post('/admin/category/add', payload)
    }
    if (res.code === 200) {
      ElMessage.success('分类已保存')
      dialogVisible.value = false
      loadCategories()
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

const deleteCategory = async (id) => {
  try {
    await ElMessageBox.confirm(
      '确认要删除该分类吗？删除后该分类下的商品将被移至未分类',
      '删除确认',
      { type: 'warning' }
    )
    const res = await api.delete(`/admin/category/delete/${id}`)
    if (res.code === 200) {
      ElMessage.success('分类已成功删除')
      loadCategories()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (error) {
    // 用户取消
  }
}

const handleSelectionChange = (rows) => {
  selectedCategories.value = rows
}

const handleSortChange = ({ prop, order }) => {
  sortState.value = { prop, order }
}

const updateStatus = async (rows, status) => {
  await Promise.all(
    rows.map((c) => {
      const payload = { ...c, status }
      return api.put('/admin/category/update', payload)
    })
  )
}

const handleBatchEnable = async () => {
  const targets = selectedCategories.value.filter((c) => c.status !== 1)
  if (!targets.length) return
  try {
    await ElMessageBox.confirm(`确认启用选中的 ${targets.length} 个分类吗？`, '批量启用', {
      type: 'warning'
    })
    await updateStatus(targets, 1)
    ElMessage.success('批量启用成功')
    loadCategories()
  } catch (e) {}
}

const handleBatchDisable = async () => {
  const targets = selectedCategories.value.filter((c) => c.status === 1)
  if (!targets.length) return
  try {
    await ElMessageBox.confirm(`确认禁用选中的 ${targets.length} 个分类吗？`, '批量禁用', {
      type: 'warning'
    })
    await updateStatus(targets, 0)
    ElMessage.success('批量禁用成功')
    loadCategories()
  } catch (e) {}
}

const handleBatchDelete = async () => {
  const targets = selectedCategories.value
  if (!targets.length) return
  try {
    await ElMessageBox.confirm(
      `确认删除选中的 ${targets.length} 个分类吗？删除后这些分类下的商品将被移至未分类`,
      '批量删除',
      { type: 'warning' }
    )
    await Promise.all(targets.map((c) => api.delete(`/admin/category/delete/${c.id}`)))
    ElMessage.success('批量删除成功')
    loadCategories()
  } catch (e) {}
}

onMounted(() => {
  loadCategories()
})
</script>

<style scoped>
.categories-page {
  padding: 24px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.categories-card {
  border-radius: 8px;
  border: none;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title-wrap {
  display: flex;
  flex-direction: column;
}

.title {
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
}

.subtitle {
  margin-top: 4px;
  font-size: 13px;
  color: #7f8c8d;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.search-input {
  width: 260px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.toolbar-left {
  color: #7f8c8d;
  font-size: 13px;
}

.toolbar-right {
  display: flex;
  gap: 8px;
}

.btn-warning {
  color: #f57c00;
  border-color: #f57c00;
}

.btn-danger {
  color: #e74c3c;
  border-color: #e74c3c;
}

.categories-table {
  --el-table-header-bg-color: #eaecef;
}

.name-cell {
  display: flex;
  align-items: center;
}

.icon-wrap {
  width: 32px;
  height: 32px;
  border-radius: 4px;
  background: #e8f5e9;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 10px;
  color: #2e7d32;
  font-weight: 600;
}

.name-meta {
  display: flex;
  flex-direction: column;
}

.name-main {
  color: #2c3e50;
  font-weight: 550;
}

.name-sub {
  color: #7f8c8d;
  font-size: 12px;
  margin-top: 2px;
}

.sort-text {
  font-weight: 500;
  color: #2c3e50;
}

.status-pill {
  display: inline-flex;
  align-items: center;
  padding: 4px 12px;
  border-radius: 16px;
  font-size: 12px;
  font-weight: 500;
}

.status-enabled {
  background: #d4edda;
  color: #155724;
}

.status-disabled {
  background: #fff3e0;
  color: #f57c00;
}

.pagination-wrap {
  margin-top: 16px;
  text-align: right;
}

.categories-page :deep(.el-table__header-wrapper th) {
  background: #eaecef;
  color: #2c3e50;
  font-weight: 600;
}

.categories-page :deep(.el-table__row:hover) {
  background: #ecf0f1 !important;
}

.categories-page :deep(.el-table__cell) {
  padding-top: 10px;
  padding-bottom: 10px;
  font-size: 13px;
}

@media (max-width: 768px) {
  .header,
  .toolbar {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .search-input {
    width: 100%;
  }
}
</style>

