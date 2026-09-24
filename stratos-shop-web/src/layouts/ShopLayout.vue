<script setup lang="ts">
import { Icon } from '@iconify/vue'
import { computed, ref, watch } from 'vue'
import { RouterLink, RouterView, useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useCartStore } from '@/stores/cart'
import { imageUrl } from '@/utils/format'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const cart = useCartStore()
const keyword = ref(String(route.query.keyword || ''))

watch(
  () => route.query.keyword,
  (value) => {
    keyword.value = String(value || '')
  },
)

const navs = [
  { label: '首页', to: '/', icon: 'solar:home-2-bold-duotone' },
  { label: '分类', to: '/category', icon: 'solar:widget-4-bold-duotone' },
  { label: '秒杀', to: '/seckill', icon: 'solar:bolt-bold-duotone' },
  { label: '拼团', to: '/group', icon: 'solar:users-group-rounded-bold-duotone' },
  { label: '领券', to: '/coupons', icon: 'solar:ticket-bold-duotone' },
]

function isNavActive(to: string) {
  if (to === '/') return route.path === '/'
  return route.path === to || route.path.startsWith(`${to}/`)
}

const displayName = computed(() => auth.user?.nickname || auth.user?.username || '我的')
const avatarLetter = computed(() => displayName.value.slice(0, 1))

function search() {
  const value = keyword.value.trim()
  router.push(value ? { path: '/search', query: { keyword: value } } : '/search')
}

if (auth.isLogin) {
  cart.refresh()
}
</script>

<template>
  <div class="flex min-h-screen flex-col bg-page text-ink">
    <header class="sticky top-0 z-20 bg-white/90 shadow-[0_8px_24px_rgba(17,24,39,0.04)] backdrop-blur-xl">
      <div class="h-0.5 bg-gradient-to-r from-sky-400 via-cyan-400 to-sky-300" />
      <div class="mx-auto w-full max-w-6xl px-6">
        <div class="flex h-[72px] items-center gap-6">
          <RouterLink to="/" class="group flex shrink-0 items-center gap-2.5">
            <span
              class="flex h-10 w-10 items-center justify-center rounded-2xl bg-gradient-to-br from-sky-400 to-cyan-500 text-white shadow-[0_8px_16px_rgba(56,189,248,0.28)]"
            >
              <Icon icon="solar:shop-2-bold" width="22" />
            </span>
            <span class="leading-tight">
              <span class="block text-lg font-semibold tracking-tight">Stratos</span>
              <span class="block text-[11px] text-mute">精选好物 · 干净好买</span>
            </span>
          </RouterLink>

          <form class="relative mx-auto w-full max-w-xl" @submit.prevent="search">
            <Icon
              icon="solar:magnifer-linear"
              width="18"
              class="pointer-events-none absolute left-4 top-1/2 -translate-y-1/2 text-slate-400"
            />
            <input
              v-model="keyword"
              class="h-11 w-full rounded-full border border-slate-200 bg-page pl-11 pr-24 text-sm outline-none transition placeholder:text-slate-400 focus:border-sky-300 focus:bg-white focus:ring-4 focus:ring-sky-100"
              placeholder="搜索商品、分类或活动"
            />
            <button
              type="submit"
              class="absolute right-1.5 top-1/2 h-8 -translate-y-1/2 rounded-full bg-ink px-4 text-xs font-medium text-white transition hover:bg-slate-800"
            >
              搜索
            </button>
          </form>

          <div class="flex shrink-0 items-center gap-2">
            <RouterLink
              to="/cart"
              class="relative flex h-11 items-center gap-2 rounded-full px-3 text-sm text-ink transition hover:bg-slate-50"
              :class="route.path === '/cart' ? 'bg-sky-50 text-sky-600' : ''"
            >
              <span class="relative inline-flex">
                <Icon icon="solar:bag-3-bold-duotone" width="22" />
                <span
                  v-if="cart.count"
                  class="absolute -right-2 -top-2 min-w-[18px] rounded-full bg-price px-1 text-center text-[10px] leading-[18px] text-white"
                >
                  {{ cart.count > 99 ? '99+' : cart.count }}
                </span>
              </span>
              购物车
            </RouterLink>

            <RouterLink
              v-if="auth.isLogin"
              to="/me"
              class="flex h-11 items-center gap-2 rounded-full py-1 pl-1 pr-3 transition hover:bg-slate-50"
              :class="route.path === '/me' ? 'bg-sky-50' : ''"
            >
              <span class="flex h-8 w-8 overflow-hidden rounded-full bg-sky-100 text-sm font-medium text-sky-600 ring-2 ring-white">
                <img v-if="auth.user?.avatar" :src="imageUrl(auth.user.avatar)" alt="" class="h-full w-full object-cover" />
                <span v-else class="flex h-full w-full items-center justify-center">{{ avatarLetter }}</span>
              </span>
              <span class="max-w-[88px] truncate text-sm">{{ displayName }}</span>
            </RouterLink>
            <RouterLink
              v-else
              to="/login"
              class="inline-flex h-11 items-center rounded-full bg-ink px-4 text-sm font-medium text-white transition hover:bg-slate-800"
            >
              登录 / 注册
            </RouterLink>
          </div>
        </div>

        <nav class="flex h-12 items-center gap-1 border-t border-slate-100">
          <RouterLink
            v-for="item in navs"
            :key="item.to"
            :to="item.to"
            class="relative flex h-full items-center gap-1.5 px-3.5 text-sm transition"
            :class="isNavActive(item.to) ? 'font-medium text-sky-600' : 'text-mute hover:text-ink'"
          >
            <Icon :icon="item.icon" width="16" />
            {{ item.label }}
            <span
              v-if="isNavActive(item.to)"
              class="absolute inset-x-3 bottom-0 h-0.5 rounded-full bg-gradient-to-r from-sky-400 to-cyan-400"
            />
          </RouterLink>
        </nav>
      </div>
    </header>

    <main class="mx-auto w-full max-w-6xl flex-1 px-6 py-8">
      <RouterView />
    </main>

    <footer class="mt-auto border-t border-slate-100 bg-white">
      <div class="mx-auto grid max-w-6xl grid-cols-4 gap-8 px-6 py-8 text-sm">
        <div>
          <div class="text-base font-semibold text-ink">StratosCommerce</div>
          <p class="mt-2 text-mute">干净好买的零售商城，覆盖浏览、下单和支付宝沙箱支付。</p>
        </div>
        <div class="space-y-2 text-mute">
          <div class="font-medium text-ink">购物</div>
          <RouterLink to="/category" class="block hover:text-ink">全部分类</RouterLink>
          <RouterLink to="/seckill" class="block hover:text-ink">限时秒杀</RouterLink>
          <RouterLink to="/coupons" class="block hover:text-ink">优惠券</RouterLink>
        </div>
        <div class="space-y-2 text-mute">
          <div class="font-medium text-ink">订单</div>
          <RouterLink to="/cart" class="block hover:text-ink">购物车</RouterLink>
          <RouterLink to="/orders" class="block hover:text-ink">我的订单</RouterLink>
          <RouterLink to="/address" class="block hover:text-ink">收货地址</RouterLink>
        </div>
        <div class="space-y-2 text-mute">
          <div class="font-medium text-ink">服务</div>
          <RouterLink to="/notices" class="block hover:text-ink">商城公告</RouterLink>
          <RouterLink to="/notifications" class="block hover:text-ink">消息通知</RouterLink>
          <RouterLink to="/me" class="block hover:text-ink">个人中心</RouterLink>
        </div>
      </div>
    </footer>
  </div>
</template>
