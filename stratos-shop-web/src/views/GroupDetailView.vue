<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import { getProduct } from '@/api/product'
import { groupActivity, joinGroup, listOpenGroups, myGroup } from '@/api/promotion'
import type { GroupActivity, GroupRecord, ProductDetail } from '@/api/types'
import PriceText from '@/components/PriceText.vue'
import SafeImage from '@/components/SafeImage.vue'
import { useAuthStore } from '@/stores/auth'
import { goGroupFlow } from '@/utils/activity-order'
import { groupStatusText, money, remainText } from '@/utils/format'

const route = useRoute()
const router = useRouter()
const message = useMessage()
const auth = useAuthStore()
const item = ref<GroupActivity>()
const product = ref<ProductDetail>()
const opens = ref<GroupRecord[]>([])
const mine = ref<GroupRecord | null>(null)
const submitting = ref(false)
const now = ref(Date.now())
let timer = 0

const activityId = computed(() => String(route.params.id || ''))
const hasOrder = computed(() => Boolean(mine.value?.orderId && String(mine.value.orderId) !== '0'))

async function loadMine() {
  if (!auth.userId) {
    mine.value = null
    return
  }
  try {
    mine.value = (await myGroup(auth.userId, activityId.value)) || null
  } catch {
    mine.value = null
  }
}

async function load() {
  item.value = await groupActivity(activityId.value)
  opens.value = (await listOpenGroups(activityId.value)) || []
  if (item.value?.spuId) {
    try {
      product.value = await getProduct(item.value.spuId)
    } catch {
      /* keep activity usable */
    }
  }
  await loadMine()
}

async function continueMine() {
  if (!item.value || !mine.value) return
  await goGroupFlow(router, {
    groupBuyingId: item.value.id,
    groupRecordId: mine.value.id,
    groupNo: mine.value.groupNo,
    skuId: item.value.skuId,
    spuId: item.value.spuId,
    orderId: mine.value.orderId,
  })
}

async function join(groupNo?: string) {
  if (!auth.userId) {
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  if (!item.value || submitting.value) return
  submitting.value = true
  try {
    const record = await joinGroup({
      activityId: item.value.id,
      userId: auth.userId,
      groupNo,
    })
    await loadMine()
    const current = mine.value || record
    message.success(hasOrder.value ? '你已参加该团，正在打开订单' : groupNo ? '已参团，请提交订单' : '已开团，请提交订单')
    await goGroupFlow(router, {
      groupBuyingId: item.value.id,
      groupRecordId: current.id,
      groupNo: current.groupNo,
      skuId: item.value.skuId,
      spuId: item.value.spuId,
      orderId: current.orderId,
    })
  } catch {
    await loadMine()
    if (mine.value) await continueMine()
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  await load()
  timer = window.setInterval(() => {
    now.value = Date.now()
  }, 1000)
})

onUnmounted(() => clearInterval(timer))
</script>

<template>
  <div v-if="item" class="grid items-start gap-6 md:grid-cols-[minmax(0,1fr)_320px]">
    <div class="space-y-4">
      <section class="overflow-hidden rounded-card bg-white shadow-card">
        <div class="aspect-[16/10] bg-slate-100">
          <SafeImage :src="product?.mainImage" class="h-full w-full object-cover" />
        </div>
        <div class="p-6">
          <div class="text-sm text-sky-600">{{ item.requireNum || 2 }} 人成团 · {{ item.limitHours || 24 }} 小时内成团</div>
          <h1 class="mt-2 text-2xl font-semibold">{{ product?.title || item.activityName || '拼团详情' }}</h1>
          <p class="mt-3 text-sm leading-6 text-mute">
            {{ product?.sellingPoint || `${item.requireNum || 2} 人成团，满员后才能支付。` }}
          </p>
        </div>
      </section>
      <section class="rounded-card bg-white p-6 shadow-card">
        <h2 class="font-semibold">可参加的团</h2>
        <p v-if="!opens.length" class="mt-3 text-sm text-mute">还没有进行中的团，你可以开一个新团。</p>
        <div v-for="row in opens" :key="row.id" class="mt-3 flex items-center justify-between rounded-2xl bg-page px-4 py-3">
          <div class="text-sm">
            <div class="font-medium">{{ row.groupNo }}</div>
            <div class="mt-1 text-mute">
              {{ groupStatusText(row.status) }} · {{ row.currentNum || 0 }}/{{ row.requireNum || 0 }} 人
              · 剩余 {{ remainText(row.expireTime, now) }}
            </div>
          </div>
          <button class="h-9 rounded-full bg-ink px-4 text-sm text-white" @click="join(row.groupNo)">参团</button>
        </div>
      </section>
    </div>
    <aside class="sticky top-36 overflow-hidden rounded-card bg-white shadow-card">
      <div class="bg-gradient-to-br from-sky-50 to-white px-5 py-4">
        <div class="text-xs text-sky-600">拼团价</div>
        <div class="mt-2 flex items-end gap-2">
          <PriceText :value="item.groupPrice" size="lg" />
          <span class="mb-1 text-sm text-mute line-through">{{ money(item.originalPrice) }}</span>
        </div>
      </div>
      <div class="px-5 py-4">
        <p v-if="mine" class="rounded-xl bg-sky-50 px-3 py-2 text-sm text-sky-700">
          你已在团 {{ mine.groupNo }} · {{ groupStatusText(mine.status) }}
        </p>
        <button v-if="mine" :disabled="submitting" class="mt-4 h-11 w-full rounded-full bg-ink text-white" @click="continueMine">
          {{ hasOrder ? '查看拼团订单' : '继续提交订单' }}
        </button>
        <button v-else :disabled="submitting" class="mt-4 h-11 w-full rounded-full bg-ink text-white" @click="join()">
          {{ submitting ? '处理中...' : '我要开团' }}
        </button>
        <RouterLink :to="`/product/${item.spuId}`" class="mt-3 block h-11 rounded-full border border-slate-200 text-center leading-[44px]">
          查看商品详情
        </RouterLink>
      </div>
    </aside>
  </div>
</template>
