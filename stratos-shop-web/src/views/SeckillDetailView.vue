<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import { getProduct } from '@/api/product'
import { doSeckill, mySeckill, seckillDetail } from '@/api/promotion'
import type { ProductDetail, SeckillActivity, SeckillRecord } from '@/api/types'
import PriceText from '@/components/PriceText.vue'
import SafeImage from '@/components/SafeImage.vue'
import { useAuthStore } from '@/stores/auth'
import { goSeckillFlow } from '@/utils/activity-order'
import { activityPhase, money, remainText, stockPercent } from '@/utils/format'

const route = useRoute()
const router = useRouter()
const message = useMessage()
const auth = useAuthStore()
const item = ref<SeckillActivity>()
const product = ref<ProductDetail>()
const mine = ref<SeckillRecord | null>(null)
const submitting = ref(false)
const now = ref(Date.now())
let timer = 0

const seckillId = computed(() => String(route.params.id || ''))
const phase = computed(() => activityPhase(item.value, now.value))
const canBuy = computed(() => phase.value === '进行中' && Number(item.value?.seckillStock || 0) > 0)
const hasOrder = computed(() => Boolean(mine.value?.orderId && String(mine.value.orderId) !== '0'))
const actionText = computed(() => {
  if (submitting.value) return '处理中...'
  if (hasOrder.value) return '查看秒杀订单'
  if (mine.value) return '继续下单'
  if (!canBuy.value) return phase.value
  return '立即抢购'
})

async function loadMine() {
  if (!auth.userId) {
    mine.value = null
    return
  }
  try {
    mine.value = (await mySeckill(auth.userId, seckillId.value)) || null
  } catch {
    mine.value = null
  }
}

async function load() {
  item.value = await seckillDetail(seckillId.value)
  if (item.value?.spuId) {
    try {
      product.value = await getProduct(item.value.spuId)
    } catch {
      /* keep activity usable */
    }
  }
  await loadMine()
}

async function buy() {
  if (!auth.userId) {
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  if (!item.value || submitting.value) return
  submitting.value = true
  try {
    let record = mine.value
    if (!record) {
      record = await doSeckill({
        seckillId: item.value.id,
        userId: auth.userId,
        quantity: 1,
      })
      mine.value = record
      message.success(record.reused ? '你已抢过，继续完成订单' : '抢购成功，请尽快下单')
    } else if (hasOrder.value) {
      message.info('已为你打开秒杀订单')
    } else {
      message.info('继续完成秒杀订单')
    }
    await goSeckillFlow(router, {
      seckillId: item.value.id,
      recordId: record.recordId || record.id,
      skuId: item.value.skuId,
      spuId: item.value.spuId,
      orderId: record.orderId,
      orderNo: record.orderNo,
      qty: record.quantity || 1,
    })
  } catch {
    await loadMine()
    if (mine.value) {
      await goSeckillFlow(router, {
        seckillId: item.value?.id,
        recordId: mine.value.recordId || mine.value.id,
        skuId: item.value?.skuId,
        spuId: item.value?.spuId,
        orderId: mine.value.orderId,
        qty: 1,
      })
    }
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
    <section class="overflow-hidden rounded-card bg-white shadow-card">
      <div class="aspect-[16/10] bg-slate-100">
        <SafeImage :src="product?.mainImage" class="h-full w-full object-cover" />
      </div>
      <div class="p-6">
        <div class="text-sm text-price">{{ phase }} · 每人限购 {{ item.limitPerUser || 1 }} 件</div>
        <h1 class="mt-2 text-2xl font-semibold">{{ product?.title || item.activityName || '秒杀详情' }}</h1>
        <p class="mt-3 text-sm leading-6 text-mute">
          {{ product?.sellingPoint || product?.subTitle || '限时秒杀，库存有限，先到先得。' }}
        </p>
      </div>
    </section>
    <aside class="sticky top-36 overflow-hidden rounded-card bg-white shadow-card">
      <div class="bg-gradient-to-br from-rose-50 to-white px-5 py-4">
        <div class="text-xs text-price">秒杀价</div>
        <div class="mt-2 flex items-end gap-2">
          <PriceText :value="item.seckillPrice" size="lg" />
          <span class="mb-1 text-sm text-mute line-through">{{ money(item.originalPrice) }}</span>
        </div>
      </div>
      <div class="space-y-3 px-5 py-4 text-sm">
        <div class="flex justify-between"><span class="text-mute">剩余库存</span><span>{{ item.seckillStock ?? 0 }}</span></div>
        <div class="flex justify-between"><span class="text-mute">已抢</span><span>{{ item.soldCount ?? 0 }}</span></div>
        <div class="flex justify-between"><span class="text-mute">距结束</span><span class="text-price">{{ remainText(item.endTime, now) }}</span></div>
        <div class="h-1.5 overflow-hidden rounded-full bg-rose-50">
          <div class="h-full rounded-full bg-price" :style="{ width: `${Math.max(stockPercent(item.soldCount, item.seckillStock), 6)}%` }" />
        </div>
      </div>
      <div class="border-t border-slate-100 px-5 py-4">
        <p v-if="mine" class="mb-4 rounded-xl bg-rose-50 px-3 py-2 text-sm text-price">
          {{ hasOrder ? '已生成秒杀订单，可直接去支付或查看。' : '你已抢到，还差一步提交订单。' }}
        </p>
        <button
          :disabled="(!canBuy && !mine) || submitting"
          class="h-11 w-full rounded-full bg-ink text-white disabled:bg-slate-200"
          @click="buy"
        >
          {{ actionText }}
        </button>
        <button class="mt-3 h-11 w-full rounded-full border border-slate-200" @click="router.push(`/product/${item.spuId}`)">
          查看商品详情
        </button>
      </div>
    </aside>
  </div>
</template>
