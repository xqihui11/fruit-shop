import axios from 'axios'
import { ElMessage } from 'element-plus'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000
})

api.interceptors.request.use(
  config => {
    // 统一请求拦截：
    // 根据“当前页面所在端 + 请求前缀”自动注入对应角色 token，
    // 避免每个页面手动拼接 Authorization 头。
    // 根据请求路径和当前路由选择不同的 token
    let token = null
    const url = config.url || ''
    
    // 获取当前路由路径
    const currentPath = window.location.pathname || ''

    // 修复：用户端店铺详情路由是 /merchant/:id（数字），不应误判为商家后台
    const pathParts = currentPath.split('/').filter(Boolean)
    const isConsumerMerchantDetail =
      pathParts[0] === 'merchant' && pathParts.length === 2 && /^\d+$/.test(pathParts[1])
    
    // 如果当前在商家后台，优先使用商家token
    if (currentPath.startsWith('/merchant') && !isConsumerMerchantDetail) {
      token = localStorage.getItem('merchant_token')
    } 
    // 如果当前在管理员后台，使用管理员token
    else if (currentPath.startsWith('/admin')) {
      token = localStorage.getItem('admin_token')
    }
    // 否则根据URL路径判断
    else if (url.startsWith('/admin')) {
      token = localStorage.getItem('admin_token')
    } else if (url.startsWith('/merchant')) {
      token = localStorage.getItem('merchant_token')
    } else {
      token = localStorage.getItem('token')
    }
    
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

api.interceptors.response.use(
  response => {
    return response.data
  },
  error => {
    // 统一响应拦截：
    // 后端返回 401 时，按角色清理本地登录态并重定向到对应登录页。
    if (error.response) {
      if (error.response.status === 401) {
        const url = error.config?.url || ''
        const currentPath = window.location.pathname || ''
        
        // 根据当前路由路径或请求URL判断使用哪个token
        if (currentPath.startsWith('/admin') || url.startsWith('/admin')) {
          localStorage.removeItem('admin_token')
          import('@/router').then(m => m.default.push('/admin/login'))
        } else if (currentPath.startsWith('/merchant') || url.startsWith('/merchant')) {
          localStorage.removeItem('merchant_token')
          localStorage.removeItem('merchant_info')
          import('@/router').then(m => m.default.push('/merchant/login'))
        } else {
          localStorage.removeItem('token')
          localStorage.removeItem('user')
          import('@/router').then(m => m.default.push('/login'))
        }
      }
      ElMessage.error(error.response.data?.message || '请求失败')
    } else {
      ElMessage.error('网络错误')
    }
    return Promise.reject(error)
  }
)

export default api
