import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '@/utils/api'

export const useCartStore = defineStore('cart', () => {
  // 购物车采用“服务端为准”的策略：每次变更后重新拉取列表，保证数据一致。
  const items = ref([])

  const fetchCartList = async () => {
    try {
      const res = await api.get('/cart/list')
      if (res.code === 200) {
        items.value = res.data || []
      }
    } catch (error) {
      console.error('获取购物车失败', error)
    }
  }

  const addToCart = async (productId, specId, quantity) => {
    try {
      const res = await api.post('/cart/add', { productId, specId, quantity })
      if (res.code === 200) {
        await fetchCartList()
        return true
      }
    } catch (error) {
      throw error
    }
  }

  const updateQuantity = async (id, quantity) => {
    try {
      const res = await api.put('/cart/update', { id, quantity })
      if (res.code === 200) {
        await fetchCartList()
      }
    } catch (error) {
      throw error
    }
  }

  const deleteItem = async (id) => {
    try {
      const res = await api.delete(`/cart/delete/${id}`)
      if (res.code === 200) {
        await fetchCartList()
      }
    } catch (error) {
      throw error
    }
  }

  return { items, fetchCartList, addToCart, updateQuantity, deleteItem }
})

