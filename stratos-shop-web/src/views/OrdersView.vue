<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { listOrders } from '@/api/order'
import { groupByOrder, listMyGroups, listMySeckills, seckillByOrder } from '@/api/promotion'
import type { OrderInfo } from '@/api/types'
import EmptyState from '@/components/EmptyState.vue'
import PageHeader from '@/components/PageHeader.vue'
import PriceText from '@/components/PriceText.vue'
import { useAuthStore } from '@/stores/auth'
import { orderStatusText } from '@/utils/format'

const auth = useAuthStore()
const orders = ref<OrderInfo[]>([])
const tags = ref<Record<string, string>>({})

const filter = ref<'all' | 0 | 20 | 30 | -20>('all')
const shown = computed(() =>
  filter.value === 'all' ? orders.value : orders.value.filter((item) => item.status === filter.value),
)

function tagOf(order: OrderInfo) {
  return tags.value[String(order.id)] || (order.buyerRemark?.includes('秒杀') ? '秒杀' : order.buyerRemark?.includes('拼团') ? '拼团' : '')
}

async function loadTags(list: OrderInfo[]) {
  if (!auth.userId) return
  const next: Record<string, string> = {}
  try {
    const [seckills, groups] = await Promise.all([listMySeckills(auth.userId), listMyGroups(auth.userId)])
    for (const item of seckills || []) {
      if (item.orderId && String(item.orderId) !== '0') next[String(item.orderId)] = '秒杀'
    }
    for (const item of groups || []) {
      if (item.orderId && String(item.orderId) !== '0') next[String(item.orderId)] = '拼团'
    }
  } catch {
    /* fallback below */
  }
  await Promise.all(
    list.map(async (order) => {
      const key = String(order.id)
      if (next[key]) return
      try {
        if (await seckillByOrder(order.id)) next[key] = '秒杀'
        else if (await groupByOrder(order.id)) next[key] = '拼团'
      } catch {
        /* keep untagged */
      }
    }),
  )
  tags.value = next
}

onMounted(async () => {
  if (!auth.userId) return
  const page = await listOrders(auth.userId, 1, 50)
  orders.value = page.records || []
  await loadTags(orders.value)
})
</script>

<template>
  <div>
    <PageHeader title="我的订单" extra="秒杀、拼团和普通订单会标出来" />
    <div class="mb-4 flex flex-wrap gap-2">
      <button
        v-for="item in [
          { id: 'all', label: '全部' },
          { id: 0, label: '待支付' },
          { id: 20, label: '待发货' },
          { id: 30, label: '待收货' },
          { id: -20, label: '售后' },
        ]"
        :key="String(item.id)"
        class="h-9 rounded-full px-4 text-sm"
        :class="filter === item.id ? 'bg-ink text-white' : 'bg-white'"
        @click="filter = item.id as any"
      >
        {{ item.label }}
      </button>
    </div>
    <EmptyState v-if="!shown.length" title="还没有这类订单" />
    <div class="space-y-3">
      <RouterLink
        v-for="item in shown"
        :key="String(item.id)"
        :to="`/order/${item.id}?orderNo=${item.orderNo}`"
        class="flex items-center justify-between rounded-card bg-white p-5 shadow-card"
      >
        <div class="min-w-0">
          <div class="flex items-center gap-2">
            <span class="text-sm text-mute">{{ item.orderNo }}</span>
            <span
              v-if="tagOf(item)"
              class="rounded-full px-2 py-0.5 text-[11px]"
              :class="tagOf(item) === '秒杀' ? 'bg-rose-50 text-price' : 'bg-sky-50 text-sky-600'"
            >
              {{ tagOf(item) }}
            </span>
          </div>
          <div class="mt-1 font-medium">{{ orderStatusText(item.status) }}</div>
          <div v-if="item.createTime" class="mt-1 text-xs text-mute">{{ item.createTime }}</div>
        </div>
        <PriceText :value="item.payAmount" />
      </RouterLink>
    </div>
  </div>
</template>
