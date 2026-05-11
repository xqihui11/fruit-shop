import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

// 路由按“用户端 / 商家端 / 管理端”三套入口组织。
// 其中 meta.requiresXxxAuth 用于统一前置鉴权。
const routes = [
  // 消费者端路由
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('@/views/Home.vue')
      },
      {
        path: 'special',
        name: 'SpecialOffers',
        component: () => import('@/views/SpecialOffers.vue')
      },
      {
        path: 'product/:id',
        name: 'ProductDetail',
        component: () => import('@/views/ProductDetail.vue')
      },
      {
        path: 'merchants',
        name: 'MerchantList',
        component: () => import('@/views/MerchantList.vue')
      },
      {
        path: 'merchant/:id',
        name: 'MerchantDetail',
        component: () => import('@/views/MerchantDetail.vue')
      },
      {
        path: 'cart',
        name: 'Cart',
        component: () => import('@/views/Cart.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'order',
        name: 'Order',
        component: () => import('@/views/Order.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'checkout',
        name: 'Checkout',
        component: () => import('@/views/Checkout.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'payment-result',
        name: 'PaymentResult',
        component: () => import('@/views/PaymentResult.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'messages',
        name: 'Messages',
        component: () => import('@/views/Messages.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'chat/:merchantId',
        name: 'ChatDetail',
        component: () => import('@/views/ChatDetail.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/Profile.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'coupons',
        name: 'Coupons',
        component: () => import('@/views/Coupons.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'announcements',
        name: 'Announcements',
        component: () => import('@/views/Announcements.vue')
      },
      {
        path: 'about',
        name: 'About',
        component: () => import('@/views/About.vue')
      },
      {
        path: 'delivery',
        name: 'Delivery',
        component: () => import('@/views/Delivery.vue')
      },
      {
        path: 'aftersale',
        name: 'AfterSale',
        component: () => import('@/views/AfterSale.vue')
      },
      {
        path: 'faq',
        name: 'FAQ',
        component: () => import('@/views/FAQ.vue')
      },
      {
        path: 'contact',
        name: 'Contact',
        component: () => import('@/views/Contact.vue')
      }
    ]
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/UnifiedLogin.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue')
  },

  // 商家端路由
  {
    path: '/merchant/login',
    name: 'MerchantLogin',
    redirect: '/login'
  },
  {
    path: '/merchant/register',
    name: 'MerchantRegister',
    component: () => import('@/views/merchant/Register.vue')
  },
  {
    path: '/merchant',
    component: () => import('@/layouts/MerchantLayout.vue'),
    meta: { requiresMerchantAuth: true },
    children: [
      {
        path: '',
        name: 'MerchantDashboard',
        component: () => import('@/views/merchant/Dashboard.vue')
      },
      {
        path: 'products',
        name: 'MerchantProducts',
        component: () => import('@/views/merchant/Products.vue')
      },
      {
        path: 'orders',
        name: 'MerchantOrders',
        component: () => import('@/views/merchant/Orders.vue')
      },
      {
        path: 'aftersales',
        name: 'MerchantAfterSales',
        component: () => import('@/views/merchant/AfterSales.vue')
      },
      {
        path: 'shop',
        name: 'MerchantShop',
        component: () => import('@/views/merchant/Shop.vue')
      },
      {
        path: 'reviews',
        name: 'MerchantReviews',
        component: () => import('@/views/merchant/Reviews.vue')
      },
      {
        path: 'coupons',
        name: 'MerchantCoupons',
        component: () => import('@/views/merchant/Coupons.vue')
      },
      {
        path: 'announcements',
        name: 'MerchantAnnouncements',
        component: () => import('@/views/merchant/Announcements.vue')
      },
      {
        path: 'chats',
        name: 'MerchantChats',
        component: () => import('@/views/merchant/Chat.vue')
      }
    ]
  },

  // 管理员端路由
  {
    path: '/admin/login',
    name: 'AdminLogin',
    redirect: '/login'
  },
  {
    path: '/admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    meta: { requiresAdminAuth: true },
    children: [
      {
        path: '',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/Dashboard.vue')
      },
      {
        path: 'users',
        name: 'AdminUsers',
        component: () => import('@/views/admin/Users.vue')
      },
      {
        path: 'merchants',
        name: 'AdminMerchants',
        component: () => import('@/views/admin/Merchants.vue')
      },
      {
        path: 'products',
        name: 'AdminProducts',
        component: () => import('@/views/admin/Products.vue')
      },
      {
        path: 'orders',
        name: 'AdminOrders',
        component: () => import('@/views/admin/Orders.vue')
      },
      {
        path: 'chats',
        name: 'AdminChats',
        component: () => import('@/views/admin/Chat.vue')
      },
      {
        path: 'aftersales',
        name: 'AdminAfterSales',
        component: () => import('@/views/admin/AfterSales.vue')
      },
      {
        path: 'categories',
        name: 'AdminCategories',
        component: () => import('@/views/admin/Categories.vue')
      },
      {
        path: 'coupons',
        name: 'AdminCoupons',
        component: () => import('@/views/admin/Coupons.vue')
      },
      {
        path: 'announcements',
        name: 'AdminAnnouncements',
        component: () => import('@/views/admin/Announcements.vue')
      },
      {
        path: 'contact-messages',
        name: 'AdminContactMessages',
        component: () => import('@/views/admin/ContactMessages.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  // 路由守卫负责三端鉴权拦截：
  // - 用户端：Pinia 中的 token
  // - 商家端：admin端：localStorage 对应 token
  // 消费者端认证
  if (to.meta.requiresAuth) {
    const userStore = useUserStore()
    if (!userStore.token) {
      next('/login')
      return
    }
  }

  // 商家端认证
  if (to.meta.requiresMerchantAuth) {
    const merchantToken = localStorage.getItem('merchant_token')
    if (!merchantToken) {
      next('/merchant/login')
      return
    }
  }

  // 管理员端认证
  if (to.meta.requiresAdminAuth) {
    const adminToken = localStorage.getItem('admin_token')
    if (!adminToken) {
      next('/admin/login')
      return
    }
  }

  next()
})

export default router
