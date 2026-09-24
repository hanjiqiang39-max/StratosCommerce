<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { Icon } from '@iconify/vue'
import { NSkeleton } from 'naive-ui'
import { getProduct } from '@/api/product'
import { listSeckill } from '@/api/promotion'
import type { ProductDetail, SeckillActivity } from '@/api/types'
import EmptyState from '@/components/EmptyState.vue'
import SafeImage from '@/components/SafeImage.vue'
import { activityPhase, money, remainText, stockPercent } from '@/utils/format'

const list = ref<SeckillActivity[]>([])
const extras = ref<Record<string, ProductDetail>>({})
const now = ref(Date.now())
const loading = ref(true)
const tab = ref<'all' | '进行中' | '未开始' | '已结束'>('all')
let timer = 0

const shown = computed(() => {
  if (tab.value === 'all') return list.value
  return list.value.filter((item) => activityPhase(item, now.value) === tab.value)
})

const tabs = computed(() => [
  { key: 'all' as const, label: '全部', count: list.value.length },
  { key: '进行中' as const, label: '进行中', count: list.value.filter((item) => activityPhase(item, now.value) === '进行中').length },
  { key: '未开始' as const, label: '未开始', count: list.value.filter((item) => activityPhase(item, now.value) === '未开始').length },
  { key: '已结束' as const, label: '已结束', count: list.value.filter((item) => activityPhase(item, now.value) === '已结束').length },
])

async function hydrate(items: SeckillActivity[]) {
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
    list.value = (await listSeckill()) || []
    await hydrate(list.value)
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
    <section class="overflow-hidden rounded-card bg-gradient-to-br from-rose-500 via-rose-400 to-orange-300 p-6 text-white shadow-card">
      <div class="flex items-end justify-between gap-4">
        <div>
          <div class="inline-flex items-center gap-1 rounded-full bg-white/15 px-2.5 py-1 text-xs">
            <Icon icon="solar:bolt-bold" width="14" />
            限时秒杀
          </div>
          <h1 class="mt-3 text-3xl font-semibold">先到先得，库存有限</h1>
          <p class="mt-2 text-sm text-white/80">抢到后请尽快下单支付，超时未付会释放库存</p>
        </div>
        <div class="rounded-2xl bg-white/15 px-4 py-3 text-right backdrop-blur">
          <div class="text-xs text-white/70">进行中</div>
          <div class="mt-1 text-2xl font-semibold">{{ tabs[1].count }}</div>
        </div>
      </div>
    </section>

    <div class="flex flex-wrap gap-2">
      <button
        v-for="item in tabs"
        :key="item.key"
        class="h-9 rounded-full px-4 text-sm"
        :class="tab === item.key ? 'bg-ink text-white' : 'bg-white text-mute shadow-card hover:text-ink'"
        @click="tab = item.key"
      >
        {{ item.label }} {{ item.count ? `(${item.count})` : '' }}
      </button>
    </div>

    <div v-if="loading" class="grid grid-cols-2 gap-4">
      <NSkeleton v-for="item in 4" :key="item" height="168px" class="rounded-card" />
    </div>
    <EmptyState v-else-if="!shown.length" title="暂无这类秒杀" desc="换个状态看看，或等后台上新活动" icon="solar:bolt-bold-duotone" />
    <div v-else class="grid grid-cols-2 gap-4">
      <RouterLink
        v-for="item in shown"
        :key="item.id"
        :to="`/seckill/${item.id}`"
        class="flex gap-4 overflow-hidden rounded-card bg-white p-4 shadow-card transition hover:-translate-y-0.5"
      >
        <div class="relative h-28 w-28 shrink-0 overflow-hidden rounded-2xl bg-slate-100">
          <SafeImage :src="extras[String(item.spuId)]?.mainImage" class="h-full w-full object-cover" />
          <span class="absolute left-2 top-2 rounded-full bg-price px-2 py-0.5 text-[11px] text-white">
            {{ activityPhase(item, now) }}
          </span>
        </div>
        <div class="min-w-0 flex-1">
          <div class="line-clamp-2 font-medium leading-6">
            {{ extras[String(item.spuId)]?.title || item.activityName || '秒杀活动' }}
          </div>
          <div class="mt-2 text-xs text-mute">每人限购 {{ item.limitPerUser || 1 }} 件 · 剩余 {{ item.seckillStock ?? 0 }}</div>
          <div class="mt-3 flex items-end justify-between">
            <div>
              <div class="text-xl font-semibold text-price">{{ money(item.seckillPrice) }}</div>
              <div class="text-xs text-mute line-through">{{ money(item.originalPrice) }}</div>
            </div>
            <div class="text-right text-xs text-mute">
              <div>已抢 {{ stockPercent(item.soldCount, item.seckillStock) }}%</div>
              <div class="mt-1 text-price">{{ remainText(item.endTime, now) }}</div>
            </div>
          </div>
          <div class="mt-2 h-1.5 overflow-hidden rounded-full bg-rose-50">
            <div class="h-full rounded-full bg-price" :style="{ width: `${Math.max(stockPercent(item.soldCount, item.seckillStock), 6)}%` }" />
          </div>
        </div>
      </RouterLink>
    </div>
  </div>
</template>
