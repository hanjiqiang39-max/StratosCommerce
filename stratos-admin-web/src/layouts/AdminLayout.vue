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
    title: '交易',
    items: [
      { to: '/products', label: '商品', icon: 'solar:box-bold-duotone' },
      { to: '/categories', label: '分类', icon: 'solar:widget-4-bold-duotone' },
      { to: '/brands', label: '品牌', icon: 'solar:medal-star-bold-duotone' },
      { to: '/inventory', label: '库存', icon: 'solar:box-minimalistic-bold-duotone' },
      { to: '/orders', label: '订单', icon: 'solar:bill-list-bold-duotone' },
      { to: '/refunds', label: '售后', icon: 'solar:restart-bold-duotone' },
      { to: '/payments', label: '支付单', icon: 'solar:card-bold-duotone' },
      { to: '/users', label: '买家', icon: 'solar:users-group-rounded-bold-duotone' },
      { to: '/merchants', label: '商家入驻', icon: 'solar:shop-bold-duotone' },
    ],
  },
  {
    title: '运营',
    items: [
      { to: '/promotion', label: '营销', icon: 'solar:bolt-bold-duotone' },
      { to: '/banners', label: '轮播', icon: 'solar:gallery-bold-duotone' },
      { to: '/notices', label: '公告', icon: 'solar:bell-bing-bold-duotone' },
      { to: '/comments', label: '评价', icon: 'solar:chat-round-dots-bold-duotone' },
      { to: '/logistics', label: '物流公司', icon: 'solar:delivery-bold-duotone' },
    ],
  },
  {
    title: '系统',
    items: [
      { to: '/admins', label: '管理员', icon: 'solar:shield-user-bold-duotone' },
      { to: '/roles', label: '角色', icon: 'solar:key-bold-duotone' },
      { to: '/menus', label: '菜单', icon: 'solar:hamburger-menu-bold-duotone' },
      { to: '/dicts', label: '字典', icon: 'solar:notebook-bold-duotone' },
      { to: '/configs', label: '配置', icon: 'solar:settings-bold-duotone' },
    ],
  },
]

const pageTitle = computed(() => {
  for (const group of groups) {
    const hit = group.items.find((item) => item.to === route.path)
    if (hit) return hit.label
  }
  return '运营控制台'
})

const displayName = computed(() => auth.session?.nickname || auth.session?.username || '管理员')
const avatarLetter = computed(() => String(displayName.value).slice(0, 1).toUpperCase())
</script>

<template>
  <div class="flex min-h-screen bg-[#F4F6FB]">
    <aside class="sticky top-0 flex h-screen w-60 shrink-0 flex-col bg-[#0B1220] text-slate-300">
      <div class="flex items-center gap-2 px-5 py-5">
        <span class="inline-flex h-9 w-9 items-center justify-center rounded-xl bg-sky-500 text-sm font-bold text-white">S</span>
        <div>
          <div class="text-sm font-semibold text-white">Stratos 后台</div>
          <div class="text-[11px] text-slate-500">零售运营控制台</div>
        </div>
      </div>
      <nav class="flex-1 space-y-5 overflow-y-auto px-3 pb-6">
        <div v-for="group in groups" :key="group.title">
          <div class="px-3 pb-2 text-[11px] uppercase tracking-wider text-slate-500">{{ group.title }}</div>
          <RouterLink
            v-for="item in group.items"
            :key="item.to"
            :to="item.to"
            class="mb-1 flex items-center gap-2.5 rounded-xl px-3 py-2 text-sm"
            :class="route.path === item.to ? 'bg-sky-500/15 text-sky-300' : 'hover:bg-white/5 hover:text-white'"
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
          <div class="text-xs text-mute">商品、订单、售后和运营配置</div>
        </div>
        <div class="flex items-center gap-3">
          <div class="flex items-center gap-2 rounded-full bg-slate-100 py-1 pl-1 pr-3 text-sm text-slate-600">
            <span class="flex h-7 w-7 items-center justify-center rounded-full bg-sky-500 text-xs text-white">
              {{ avatarLetter }}
            </span>
            {{ displayName }}
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
