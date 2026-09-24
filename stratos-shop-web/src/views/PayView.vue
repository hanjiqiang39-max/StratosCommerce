<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import { getOrder, getOrderByNo } from '@/api/order'
import { createPay, queryPayByOrder, simulatePay } from '@/api/payment'
import { groupPayable } from '@/api/promotion'
import { useAuthStore } from '@/stores/auth'
import PageHeader from '@/components/PageHeader.vue'
import PriceText from '@/components/PriceText.vue'

const route = useRoute()
const router = useRouter()
const message = useMessage()
const auth = useAuthStore()
const paying = ref(false)
const opened = ref(false)
const lastPayNo = ref('')
const waitingGroup = ref(false)
const orderId = computed(() => String(route.params.orderId || ''))
const amount = computed(() => Number(route.query.amount || 0))
const orderNo = computed(() => String(route.query.orderNo || ''))

async function refreshGroup() {
  try {
    const ok = await groupPayable(orderId.value)
    waitingGroup.value = ok === false
    if (ok) message.success('已成团，可以支付')
  } catch {
    waitingGroup.value = false
  }
}

onMounted(async () => {
  try {
    waitingGroup.value = (await groupPayable(orderId.value)) === false
  } catch {
    waitingGroup.value = false
  }
})

async function loadOrder() {
  if (orderNo.value) {
    return getOrderByNo(orderNo.value)
  }
  return getOrder(orderId.value)
}

function openAlipay(payForm: string) {
  const html = payForm.trim()
  if (html.startsWith('http://') || html.startsWith('https://')) {
    window.open(html, '_blank')
    return
  }
  const wrap = document.createElement('div')
  wrap.style.display = 'none'
  wrap.innerHTML = html
  document.body.appendChild(wrap)
  const form = wrap.querySelector('form')
  if (form) {
    form.setAttribute('method', 'get')
    form.setAttribute('target', '_blank')
    form.submit()
    return
  }
  window.open(html, '_blank')
}

async function ensurePay() {
  const detail = await loadOrder()
  const vo = await createPay({
    orderId: detail.id,
    orderNo: detail.orderNo,
    userId: auth.userId,
    payChannel: 1,
    payAmount: Number(detail.payAmount),
    subject: `订单${detail.orderNo}`,
  })
  lastPayNo.value = vo.payNo
  return { detail, vo }
}

async function pay() {
  if (!auth.userId) return
  paying.value = true
  try {
    const { vo } = await ensurePay()
    if (vo.payForm) {
      openAlipay(vo.payForm)
      opened.value = true
      message.info('已打开支付宝沙箱，控制台红字可忽略。本地点亮订单请用下方按钮。')
      return
    }
    await finishSimulate()
  } finally {
    paying.value = false
  }
}

async function finishSimulate() {
  if (!auth.userId) return
  paying.value = true
  try {
    const detail = await loadOrder()
    let payNo = lastPayNo.value
    if (!payNo) {
      payNo = (await ensurePay()).vo.payNo
    }
    const latest = await queryPayByOrder(detail.orderNo)
    if (Number(latest?.status) === 2) {
      message.success('订单已支付')
      router.replace(`/order/${detail.id}?orderNo=${detail.orderNo}`)
      return
    }
    await simulatePay(payNo)
    message.success('已完成本地支付')
    router.replace(`/order/${detail.id}?orderNo=${detail.orderNo}`)
  } finally {
    paying.value = false
  }
}
</script>

<template>
  <div class="mx-auto max-w-lg">
    <PageHeader title="订单支付" extra="沙箱收银台已打通，控制台 mixed content / unload 是支付宝页面自己的，不用管" />
    <div class="rounded-card bg-white p-8 text-center shadow-card">
      <div class="text-mute">订单 {{ orderNo || orderId }}</div>
      <div class="my-5"><PriceText :value="amount" size="lg" /></div>
      <p v-if="waitingGroup" class="mb-4 text-left text-sm leading-6 text-mute">
        这是拼团订单，满员成团后才能支付。把拼团页链接发给好友，成团后点下方刷新即可付款。
      </p>
      <button v-if="waitingGroup" class="mb-3 h-11 w-full rounded-full border" @click="refreshGroup">
        刷新成团状态
      </button>
      <button :disabled="paying || waitingGroup" class="h-11 w-full rounded-full bg-brand text-white disabled:opacity-50" @click="pay">
        {{ paying ? '处理中...' : opened ? '再次打开支付宝' : '打开支付宝沙箱' }}
      </button>
      <button :disabled="paying || waitingGroup" class="mt-3 h-11 w-full rounded-full border disabled:opacity-50" @click="finishSimulate">
        完成本地支付（推荐联调）
      </button>
      <p v-if="opened" class="mt-4 text-left text-sm leading-6 text-mute">
        沙箱页请用开放平台里的<strong>沙箱买家账号</strong>登录，不要扫正式支付宝。
        扫码、埋点、WebSocket 报错都不影响下单。异步通知到不了 localhost，点上面按钮即可把订单改成已支付。
      </p>
    </div>
  </div>
</template>
