<script setup lang="ts">
import { onMounted, onUnmounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { Icon } from '@iconify/vue'
import { NSkeleton } from 'naive-ui'
import { getProduct } from '@/api/product'
import { listGroups, listMyGroups } from '@/api/promotion'
import type { GroupActivity, GroupRecord, ProductDetail } from '@/api/types'
import EmptyState from '@/components/EmptyState.vue'
import SafeImage from '@/components/SafeImage.vue'
import { useAuthStore } from '@/stores/auth'
import { groupStatusText, money, remainText } from '@/utils/format'

const auth = useAuthStore()
const list = ref<GroupActivity[]>([])
const mine = ref<GroupRecord[]>([])
const extras = ref<Record<string, ProductDetail>>({})
const now = ref(Date.now())
const loading = ref(true)
let timer = 0

function progress(item: GroupRecord) {
  const requireNum = Number(item.requireNum || 0)
  if (!requireNum) return 0
  return Math.min(100, Math.round((Number(item.currentNum || 0) / requireNum) * 100))
}

async function hydrate(items: GroupActivity[]) {
  await Promise.all(
    items.map(async (item) => {
      if (!item.spuId) return
      const key = String(item.spuId)
      if (extras.value[key]) return
      try {
        extras.value[key] = await getProduct(item.spuId)
      } catch {
        /* keep list usable */
      }
    }),
  )
}

onMounted(async () => {
  try {
    list.value = (await listGroups()) || []
    await hydrate(list.value)
    if (auth.userId) mine.value = (await listMyGroups(auth.userId)) || []
  } finally {
    loading.value = false
    timer = window.setInterval(() => {
      now.value = Date.now()
    }, 1000)
  }
})

onUnmounted(() => clearInterval(timer))
</script>

<template>
  <div class="space-y-5">
    <section class="overflow-hidden rounded-card bg-gradient-to-br from-sky-500 via-sky-400 to-cyan-300 p-6 text-white shadow-card">
      <div class="flex items-end justify-between gap-4">
        <div>
          <div class="inline-flex items-center gap-1 rounded-full bg-white/15 px-2.5 py-1 text-xs">
            <Icon icon="solar:users-group-rounded-bold" width="14" />
            拼团
          </div>
          <h1 class="mt-3 text-3xl font-semibold">满员成团，买得更便宜</h1>
          <p class="mt-2 text-sm text-white/80">开团或参团后提交订单，成团才能支付</p>
        </div>
        <div class="rounded-2xl bg-white/15 px-4 py-3 text-right backdrop-blur">
          <div class="text-xs text-white/70">可参与</div>
          <div class="mt-1 text-2xl font-semibold">{{ list.length }}</div>
        </div>
      </div>
    </section>

    <div v-if="loading" class="grid grid-cols-2 gap-4">
      <NSkeleton v-for="item in 4" :key="item" height="168px" class="rounded-card" />
    </div>
    <EmptyState v-else-if="!list.length" title="暂无拼团活动" desc="后台创建活动后会显示在这里" icon="solar:users-group-rounded-bold-duotone" />
    <div v-else class="grid grid-cols-2 gap-4">
      <RouterLink
        v-for="item in list"
        :key="item.id"
        :to="`/group/${item.id}`"
        class="flex gap-4 overflow-hidden rounded-card bg-white p-4 shadow-card transition hover:-translate-y-0.5"
      >
        <div class="relative h-28 w-28 shrink-0 overflow-hidden rounded-2xl bg-slate-100">
          <SafeImage :src="extras[String(item.spuId)]?.mainImage" class="h-full w-full object-cover" />
          <span class="absolute left-2 top-2 rounded-full bg-sky-500 px-2 py-0.5 text-[11px] text-white">
            {{ item.requireNum || 2 }} 人成团
          </span>
        </div>
        <div class="min-w-0 flex-1">
          <div class="line-clamp-2 font-medium leading-6">
            {{ extras[String(item.spuId)]?.title || item.activityName || '拼团活动' }}
          </div>
          <div class="mt-2 text-xs text-mute">{{ item.limitHours || 24 }} 小时内成团 · 限购 {{ item.limitPerUser || 1 }} 件</div>
          <div class="mt-3 flex items-end justify-between">
            <div>
              <div class="text-xl font-semibold text-price">{{ money(item.groupPrice) }}</div>
              <div class="text-xs text-mute line-through">{{ money(item.originalPrice) }}</div>
            </div>
            <div class="rounded-full bg-sky-50 px-3 py-1 text-xs text-sky-600">去开团 / 参团</div>
          </div>
        </div>
      </RouterLink>
    </div>

    <section v-if="mine.length" class="space-y-4">
      <div class="flex items-center gap-2">
        <span class="h-5 w-1 rounded-full bg-sky-400" />
        <h2 class="text-lg font-semibold">我参加的团</h2>
      </div>
      <div class="grid grid-cols-2 gap-4">
        <RouterLink
          v-for="item in mine"
          :key="item.id"
          :to="`/group/record/${item.groupNo}`"
          class="rounded-card bg-white p-5 shadow-card transition hover:-translate-y-0.5"
        >
          <div class="flex items-start justify-between gap-3">
            <div>
              <div class="text-xs text-sky-600">{{ groupStatusText(item.status) }}</div>
              <div class="mt-1 font-medium">团号 {{ item.groupNo }}</div>
            </div>
            <div class="text-sm text-mute">{{ item.currentNum || 0 }}/{{ item.requireNum || 0 }} 人</div>
          </div>
          <div class="mt-3 h-1.5 overflow-hidden rounded-full bg-sky-50">
            <div class="h-full rounded-full bg-sky-500" :style="{ width: `${Math.max(progress(item), 8)}%` }" />
          </div>
          <div v-if="item.status === 0" class="mt-2 text-xs text-price">剩余 {{ remainText(item.expireTime, now) }}</div>
          <div v-else class="mt-2 text-xs text-mute">点击查看拼团详情</div>
        </RouterLink>
      </div>
    </section>
  </div>
</template>
