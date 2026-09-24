import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', component: () => import('@/views/LoginView.vue') },
    {
      path: '/',
      component: () => import('@/layouts/AdminLayout.vue'),
      children: [
        { path: '', redirect: '/dashboard' },
        { path: 'dashboard', component: () => import('@/views/DashboardView.vue') },
        { path: 'products', component: () => import('@/views/ProductView.vue') },
        { path: 'categories', component: () => import('@/views/CategoryView.vue') },
        { path: 'brands', component: () => import('@/views/BrandView.vue') },
        { path: 'orders', component: () => import('@/views/OrderView.vue') },
        { path: 'refunds', component: () => import('@/views/RefundView.vue') },
        { path: 'inventory', component: () => import('@/views/InventoryView.vue') },
        { path: 'payments', component: () => import('@/views/PaymentView.vue') },
        { path: 'users', component: () => import('@/views/UserView.vue') },
        { path: 'merchants', component: () => import('@/views/MerchantView.vue') },
        { path: 'promotion', component: () => import('@/views/PromotionView.vue') },
        { path: 'banners', component: () => import('@/views/BannerView.vue') },
        { path: 'notices', component: () => import('@/views/NoticeView.vue') },
        { path: 'comments', component: () => import('@/views/CommentView.vue') },
        { path: 'logistics', component: () => import('@/views/LogisticsView.vue') },
        { path: 'admins', component: () => import('@/views/AdminView.vue') },
        { path: 'roles', component: () => import('@/views/RoleView.vue') },
        { path: 'menus', component: () => import('@/views/MenuView.vue') },
        { path: 'dicts', component: () => import('@/views/DictView.vue') },
        { path: 'configs', component: () => import('@/views/ConfigView.vue') },
      ],
    },
  ],
})

router.beforeEach((to) => {
  const auth = useAuthStore()
  if (to.path !== '/login' && !auth.isLogin) return '/login'
  if (to.path === '/login' && auth.isLogin) return '/dashboard'
  return true
})

export default router
