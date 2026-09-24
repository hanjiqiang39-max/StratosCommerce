<script setup lang="ts">
import { Icon } from '@iconify/vue'
import { computed } from 'vue'
import { RouterLink, RouterView, useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const groups = [
  {
    title: '概览',
    items: [{ to: '/dashboard', label: '工作台', icon: 'solar:widget-2-bold-duotone' }],
  },
  {
    title: '店铺',
    items: [
      { to: '/shop', label: '店铺资料', icon: 'solar:shop-bold-duotone' },
      { to: '/products', label: '商品', icon: 'solar:box-bold-duotone' },
      { to: '/inventory', label: '库存', icon: 'solar:box-minimalistic-bold-duotone' },
    ],
  },
  {
    title: '交易',
    items: [
      { to: '/orders', label: '订单', icon: 'solar:bill-list-bold-duotone' },
      { to: '/refunds', label: '售后', icon: 'solar:restart-bold-duotone' },
      { to: '/comments', label: '评价', icon: 'solar:chat-round-dots-bold-duotone' },
      { to: '/promotion', label: '营销', icon: 'solar:bolt-bold-duotone' },
    ],
  },
]

const pageTitle = computed(() => {
  for (const group of groups) {
    const hit = group.items.find((item) => item.to === route.path)
    if (hit) return hit.label
  }
  return '商家中心'
})
</script>

<template>
  <div class="flex min-h-screen bg-[#F4F6FB]">
    <aside class="sticky top-0 flex h-screen w-60 shrink-0 flex-col bg-[#042f2e] text-slate-300">
      <div class="flex items-center gap-2 px-5 py-5">
        <span class="inline-flex h-9 w-9 items-center justify-center rounded-xl bg-teal-500 text-sm font-bold text-white">店</span>
        <div>
          <div class="text-sm font-semibold text-white">Stratos 商家</div>
          <div class="text-[11px] text-teal-200/70">{{ auth.session?.shopName || '店铺工作台' }}</div>
        </div>
      </div>
      <nav class="flex-1 space-y-5 overflow-y-auto px-3 pb-6">
        <div v-for="group in groups" :key="group.title">
          <div class="px-3 pb-2 text-[11px] uppercase tracking-wider text-teal-200/50">{{ group.title }}</div>
          <RouterLink
            v-for="item in group.items"
            :key="item.to"
            :to="item.to"
            class="mb-1 flex items-center gap-2.5 rounded-xl px-3 py-2 text-sm"
            :class="route.path === item.to ? 'bg-teal-500/20 text-teal-200' : 'hover:bg-white/5 hover:text-white'"
          >
            <Icon :icon="item.icon" width="18" />
            {{ item.label }}
          </RouterLink>
        </div>
      </nav>
    </aside>
    <div class="flex min-w-0 flex-1 flex-col">
      <header class="flex h-16 items-center justify-between border-b border-slate-200/80 bg-white/90 px-7 backdrop-blur">
        <div>
          <div class="text-sm font-medium text-ink">{{ pageTitle }}</div>
          <div class="text-xs text-mute">发布商品、处理订单、回复评价</div>
        </div>
        <div class="flex items-center gap-3">
          <div class="flex items-center gap-2 rounded-full bg-slate-100 py-1 pl-1 pr-3 text-sm text-slate-600">
            <span class="flex h-7 w-7 items-center justify-center rounded-full bg-teal-600 text-xs text-white">
              {{ (auth.session?.nickname || auth.session?.username || '商').slice(0, 1) }}
            </span>
            {{ auth.session?.nickname || auth.session?.username || '商家' }}
          </div>
          <button
            class="rounded-full border border-slate-200 px-3 py-1.5 text-sm text-mute hover:text-ink"
            @click="auth.logout(); router.push('/login')"
          >
            退出
          </button>
        </div>
      </header>
      <main class="flex-1 p-7">
        <RouterView />
      </main>
    </div>
  </div>
</template>
