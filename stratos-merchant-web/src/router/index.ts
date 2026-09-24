import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', component: () => import('@/views/LoginView.vue') },
    { path: '/register', component: () => import('@/views/RegisterView.vue') },
    {
      path: '/',
      component: () => import('@/layouts/MerchantLayout.vue'),
      children: [
        { path: '', redirect: '/dashboard' },
        { path: 'dashboard', component: () => import('@/views/DashboardView.vue') },
        { path: 'shop', component: () => import('@/views/ShopView.vue') },
        { path: 'products', component: () => import('@/views/ProductView.vue') },
        { path: 'inventory', component: () => import('@/views/InventoryView.vue') },
        { path: 'orders', component: () => import('@/views/OrderView.vue') },
        { path: 'refunds', component: () => import('@/views/RefundView.vue') },
        { path: 'comments', component: () => import('@/views/CommentView.vue') },
        { path: 'promotion', component: () => import('@/views/PromotionView.vue') },
      ],
    },
  ],
})

router.beforeEach((to) => {
  const auth = useAuthStore()
  if (to.path !== '/login' && to.path !== '/register' && !auth.isLogin) return '/login'
  if ((to.path === '/login' || to.path === '/register') && auth.isLogin) return '/dashboard'
  return true
})

export default router
