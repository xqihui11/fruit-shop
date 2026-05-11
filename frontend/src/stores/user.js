import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '@/utils/api'

export const useUserStore = defineStore('user', () => {
  // store 初始化时先从 localStorage 恢复登录态，支持刷新后保持会话。
  const token = ref(localStorage.getItem('token') || '')
  const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))

  const login = async (username, password) => {
    const res = await api.post('/user/login', { username, password })
    if (res.code === 200) {
      // 清除之前的token和用户信息，避免数据混乱
      const oldUserId = user.value?.id
      token.value = res.data.token
      user.value = res.data.user
      localStorage.setItem('token', res.data.token)
      localStorage.setItem('user', JSON.stringify(res.data.user))
      
      // 如果切换了用户，清除可能存在的缓存数据
      if (oldUserId && oldUserId !== res.data.user?.id) {
        // 清除localStorage中可能存在的其他用户数据
        // 注意：这里不删除token和user，因为已经更新了
      }
      
      return true
    }
    throw new Error(res.message)
  }

  const register = async (username, password, phone) => {
    const res = await api.post('/user/register', { username, password, phone })
    if (res.code === 200) {
      return true
    }
    throw new Error(res.message)
  }

  const logout = () => {
    // 退出登录时同时清空内存态与持久化态，避免页面误判已登录。
    token.value = ''
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  const setUser = (userData) => {
    user.value = userData
    localStorage.setItem('user', JSON.stringify(userData))
  }

  return { token, user, login, register, logout, setUser }
})

