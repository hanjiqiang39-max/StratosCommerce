<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import { applyRefund, cancelOrder, confirmOrder, getOrder, getOrderByNo } from '@/api/order'
import { groupByOrder, seckillByOrder } from '@/api/promotion'
import { traceByOrder } from '@/api/logistics'
import type { GroupRecord, OrderDetail, SeckillRecord } from '@/api/types'
import PageHeader from '@/components/PageHeader.vue'
import PriceText from '@/components/PriceText.vue'
import SafeImage from '@/components/SafeImage.vue'
import { useAuthStore } from '@/stores/auth'
import { orderStatusText } from '@/utils/format'

const route = useRoute()
const router = useRouter()
const message = useMessage()
const auth = useAuthStore()
const order = ref<OrderDetail>()
const traces = ref<any[]>([])
const seckill = ref<SeckillRecord | null>(null)
const group = ref<GroupRecord | null>(null)
const id = computed(() => String(route.params.id || ''))
const orderNo = computed(() => String(route.query.orderNo || ''))

async function load() {
  order.value = orderNo.value ? await getOrderByNo(orderNo.value) : await getOrder(id.value)
  try {
    traces.value = (await traceByOrder(order.value.orderNo)) || []
  } catch {
    traces.value = []
  }
  try {
    seckill.value = (await seckillByOrder(order.value.id)) || null
  } catch {
    seckill.value = null
  }
  try {
    group.value = (await groupByOrder(order.value.id)) || null
  } catch {
    group.value = null
  }
}

async function cancel() {
  await cancelOrder(order.value?.id || id.value)
  message.success('已取消')
  await load()
}

async function confirm() {
  await confirmOrder(order.value?.id || id.value)
  message.success('已确认收货')
  await load()
}

async function refund() {
  if (!auth.userId || !order.value) return
  await applyRefund({
    orderId: order.value.id,
    userId: auth.userId,
    refundType: 1,
    refundReason: 1,
    refundDesc: '用户申请退款',
    refundAmount: order.value.payAmount,
  })
  message.success('已提交售后')
  await load()
}

onMounted(load)
</script>

<template>
  <div v-if="order">
    <PageHeader :title="orderStatusText(order.status)" :extra="order.orderNo" />
    <div v-if="seckill || group" class="mb-4 rounded-card px-5 py-3 text-sm" :class="seckill ? 'bg-rose-50 text-price' : 'bg-sky-50 text-sky-700'">
      <span v-if="seckill">这是秒杀订单，价格以秒杀活动为准。</span>
      <span v-else>这是拼团订单{{ group?.groupNo ? ` · 团号 ${group.groupNo}` : '' }}{{ group?.status === 0 ? '，满员后才能支付。' : '。' }}</span>
    </div>
    <div class="grid grid-cols-2 gap-4">
      <section class="rounded-card bg-white p-5">
        <div class="text-sm text-mute">实付金额</div>
        <div class="mt-2"><PriceText :value="order.payAmount" size="lg" /></div>
        <div class="mt-4 text-sm">{{ order.receiverName }} {{ order.receiverPhone }}</div>
        <div class="text-sm text-mute">{{ order.receiverAddress }}</div>
      </section>
      <section class="rounded-card bg-white p-5">
        <div class="mb-3 font-medium">商品</div>
        <div v-for="item in order.items || []" :key="item.skuId" class="mb-3 flex items-center gap-3">
          <div class="h-14 w-14 overflow-hidden rounded-lg bg-slate-100">
            <SafeImage :src="item.skuImage" class="h-full w-full object-cover" />
          </div>
          <div class="min-w-0 flex-1">
            <div class="truncate text-sm">{{ item.skuName || `SKU ${item.skuId}` }}</div>
            <div class="text-xs text-mute">x{{ item.quantity }}</div>
          </div>
          <PriceText :value="item.price" size="sm" />
        </div>
      </section>
    </div>
    <div class="mt-4 flex flex-wrap gap-2">
      <button v-if="order.status === 0" class="h-11 rounded-full bg-brand px-5 text-white" @click="router.push(`/pay/${order.id}?orderNo=${order.orderNo}&amount=${order.payAmount}`)">去支付</button>
      <button v-if="order.status === 0" class="h-11 rounded-full border px-5" @click="cancel">取消订单</button>
      <button v-if="order.status === 30" class="h-11 rounded-full bg-ink px-5 text-white" @click="confirm">确认收货</button>
      <button v-if="order.status >= 10" class="h-11 rounded-full border px-5" @click="refund">申请售后</button>
    </div>
    <div v-if="traces.length" class="mt-4 rounded-card bg-white p-5">
      <h2 class="mb-2 font-semibold">物流</h2>
      <div v-for="(item, index) in traces" :key="index" class="text-sm text-mute">{{ item.traceTime }} {{ item.traceDesc }}</div>
    </div>
  </div>
</template>
