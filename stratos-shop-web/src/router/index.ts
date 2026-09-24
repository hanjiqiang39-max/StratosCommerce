import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(),
  scrollBehavior: () => ({ top: 0 }),
  routes: [
    {
      path: '/',
      component: () => import('@/layouts/ShopLayout.vue'),
      children: [
        { path: '', name: 'home', component: () => import('@/views/HomeView.vue'), meta: { tab: 'home' } },
        { path: 'search', name: 'search', component: () => import('@/views/SearchView.vue') },
        { path: 'category', name: 'category', component: () => import('@/views/CategoryView.vue'), meta: { tab: 'category' } },
        { path: 'product/:id', name: 'product', component: () => import('@/views/ProductView.vue') },
        { path: 'cart', name: 'cart', component: () => import('@/views/CartView.vue'), meta: { tab: 'cart', auth: true } },
        { path: 'checkout', name: 'checkout', component: () => import('@/views/CheckoutView.vue'), meta: { auth: true } },
        { path: 'pay/:orderId', name: 'pay', component: () => import('@/views/PayView.vue'), meta: { auth: true } },
        { path: 'orders', name: 'orders', component: () => import('@/views/OrdersView.vue'), meta: { auth: true } },
        { path: 'order/:id', name: 'order', component: () => import('@/views/OrderDetailView.vue'), meta: { auth: true } },
        { path: 'me', name: 'me', component: () => import('@/views/MeView.vue'), meta: { tab: 'me' } },
        { path: 'address', name: 'address', component: () => import('@/views/AddressView.vue'), meta: { auth: true } },
        { path: 'coupons', name: 'coupons', component: () => import('@/views/CouponView.vue') },
        { path: 'points', name: 'points', component: () => import('@/views/PointsView.vue'), meta: { auth: true } },
        { path: 'favorites', name: 'favorites', component: () => import('@/views/FavoriteView.vue'), meta: { auth: true } },
        { path: 'notices', name: 'notices', component: () => import('@/views/NoticeView.vue') },
        { path: 'seckill', name: 'seckill', component: () => import('@/views/SeckillView.vue') },
        { path: 'seckill/:id', name: 'seckill-detail', component: () => import('@/views/SeckillDetailView.vue') },
        { path: 'group', name: 'group', component: () => import('@/views/GroupView.vue') },
        { path: 'group/record/:groupNo', name: 'group-record', component: () => import('@/views/GroupRecordView.vue') },
        { path: 'group/:id', name: 'group-detail', component: () => import('@/views/GroupDetailView.vue') },
        { path: 'notifications', name: 'notifications', component: () => import('@/views/NotifyView.vue'), meta: { auth: true } },
      ],
    },
    { path: '/login', name: 'login', component: () => import('@/views/LoginView.vue') },
    { path: '/register', name: 'register', component: () => import('@/views/RegisterView.vue') },
    { path: '/oauth/callback', name: 'oauth-callback', component: () => import('@/views/OauthCallbackView.vue') },
  ],
})

router.beforeEach((to) => {
  const auth = useAuthStore()
  if (to.meta.auth && !auth.isLogin) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  return true
})

export default router
