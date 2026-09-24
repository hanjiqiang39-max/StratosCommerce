<script setup lang="ts">
import { Icon } from '@iconify/vue'
import { computed, onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import type { EChartsOption } from 'echarts'
import { merchantOrders, orderReport, productSkus } from '@/api'
import { useAuthStore } from '@/stores/auth'
import { chartLabel, skuLabel, stockOf, stockTone } from '@/utils/sku'
import ChartBox from '@/components/ChartBox.vue'
import MerchantPage from '@/components/MerchantPage.vue'

const auth = useAuthStore()
const data = ref<Record<string, number>>({})
const orders = ref<any[]>([])
const skus = ref<any[]>([])

const cards = [
  { key: 'orderCount', label: '本店订单', hint: '全部订单', icon: 'solar:bill-list-bold-duotone', tone: 'bg-teal-50 text-teal-700' },
  { key: 'payAmount', label: '成交额', hint: '已支付金额', icon: 'solar:wad-of-money-bold-duotone', tone: 'bg-emerald-50 text-emerald-600' },
  { key: 'paidCount', label: '待发货', hint: '已付款待履约', icon: 'solar:box-bold-duotone', tone: 'bg-amber-50 text-amber-600' },
  { key: 'shippedCount', label: '已发货', hint: '履约中', icon: 'solar:delivery-bold-duotone', tone: 'bg-violet-50 text-violet-600' },
]
const shortcuts = [
  { to: '/products', label: '发布商品', desc: '上架后会出现在商城', icon: 'solar:box-bold-duotone' },
  { to: '/orders', label: '处理订单', desc: '备注、发货', icon: 'solar:clipboard-list-bold-duotone' },
  { to: '/refunds', label: '售后审核', desc: '同意或拒绝退款', icon: 'solar:restart-bold-duotone' },
  { to: '/promotion', label: '营销活动', desc: '秒杀、拼团、优惠券', icon: 'solar:bolt-bold-duotone' },
]

function dayKey(offset: number) {
  const date = new Date()
  date.setHours(0, 0, 0, 0)
  date.setDate(date.getDate() + offset)
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return {
    label: `${month}-${day}`,
    stamp: `${date.getFullYear()}-${month}-${day}`,
  }
}

function orderDay(item: any) {
  return String(item.createTime || item.create_time || '').replace('T', ' ').slice(0, 10)
}

const trendDays = computed(() => Array.from({ length: 7 }, (_, index) => dayKey(index - 6)))

const trendOption = computed<EChartsOption>(() => {
  const counts = trendDays.value.map((day) => orders.value.filter((item) => orderDay(item) === day.stamp).length)
  const amounts = trendDays.value.map((day) =>
    orders.value
      .filter((item) => orderDay(item) === day.stamp && Number(item.status) >= 10)
      .reduce((sum, item) => sum + Number(item.payAmount || 0), 0),
  )
  return {
    color: ['#0f766e', '#f59e0b'],
    tooltip: { trigger: 'axis' },
    legend: { data: ['订单数', '成交额'], bottom: 0 },
    grid: { left: 36, right: 36, top: 24, bottom: 36 },
    xAxis: { type: 'category', data: trendDays.value.map((item) => item.label), boundaryGap: false },
    yAxis: [
      { type: 'value', name: '单', minInterval: 1 },
      { type: 'value', name: '元', splitLine: { show: false } },
    ],
    series: [
      { name: '订单数', type: 'line', smooth: true, data: counts, areaStyle: { color: 'rgba(15,118,110,0.12)' } },
      { name: '成交额', type: 'bar', yAxisIndex: 1, barWidth: 14, data: amounts.map((value) => Number(value.toFixed(2))) },
    ],
  }
})

const statusOption = computed<EChartsOption>(() => ({
  color: ['#f59e0b', '#14b8a6', '#8b5cf6', '#94a3b8'],
  tooltip: { trigger: 'item' },
  legend: { bottom: 0 },
  series: [
    {
      type: 'pie',
      radius: ['42%', '68%'],
      center: ['50%', '44%'],
      label: { formatter: '{b}\n{c}' },
      data: [
        { name: '待支付', value: Number(data.value.unpaidCount || 0) },
        { name: '待发货', value: Number(data.value.paidCount || 0) },
        { name: '已发货', value: Number(data.value.shippedCount || 0) },
        { name: '已取消/退款', value: Number(data.value.cancelledCount || 0) },
      ],
    },
  ],
}))

const stockOption = computed<EChartsOption>(() => {
  const top = [...skus.value].sort((a, b) => stockOf(b) - stockOf(a)).slice(0, 6)
  return {
    color: ['#0d9488', '#f59e0b'],
    tooltip: {
      trigger: 'axis',
      formatter: (params: any) => {
        const index = params?.[0]?.dataIndex ?? 0
        const item = top[index]
        return item ? `${skuLabel(item)}<br/>可售 ${stockOf(item)}` : ''
      },
    },
    grid: { left: 92, right: 16, top: 16, bottom: 24 },
    xAxis: { type: 'value', minInterval: 1 },
    yAxis: {
      type: 'category',
      data: top.map((item) => chartLabel(skuLabel(item), 10)).reverse(),
      axisLabel: { width: 80, overflow: 'truncate' },
    },
    series: [{ type: 'bar', barWidth: 14, data: top.map((item) => stockOf(item)).reverse() }],
  }
})

const stockHealth = computed(() => ({
  ok: skus.value.filter((item) => stockTone(item) === 'ok').length,
  warn: skus.value.filter((item) => stockTone(item) === 'warn').length,
  empty: skus.value.filter((item) => stockTone(item) === 'empty').length,
}))

onMounted(async () => {
  if (!auth.shopId) return
  const [report, page, skuRows] = await Promise.all([
    orderReport(auth.shopId),
    merchantOrders({ pageNum: 1, pageSize: 100, shopId: auth.shopId }),
    productSkus(auth.shopId),
  ])
  data.value = report || {}
  orders.value = page?.records || []
  skus.value = skuRows || []
})

function display(key: string) {
  const value = data.value[key] ?? 0
  return key === 'payAmount' ? `¥${Number(value).toFixed(2)}` : value
}
</script>

<template>
  <MerchantPage title="工作台" extra="本店成交、库存和待办，图表按最近订单汇总">
    <div class="grid grid-cols-2 gap-4 xl:grid-cols-4">
      <div v-for="item in cards" :key="item.key" class="admin-card p-5">
        <div class="mb-4 inline-flex h-11 w-11 items-center justify-center rounded-2xl" :class="item.tone">
          <Icon :icon="item.icon" width="22" />
        </div>
        <div class="text-sm text-mute">{{ item.label }}</div>
        <div class="mt-2 text-3xl font-semibold tracking-tight">{{ display(item.key) }}</div>
        <div class="mt-1 text-xs text-mute">{{ item.hint }}</div>
      </div>
    </div>

    <div class="grid grid-cols-1 gap-4 xl:grid-cols-[1.4fr_1fr]">
      <div class="admin-card p-5">
        <div class="mb-2 font-medium">近 7 日成交</div>
        <p class="mb-3 text-xs text-mute">折线是下单量，柱状是已支付金额</p>
        <ChartBox :option="trendOption" height="300px" />
      </div>
      <div class="admin-card p-5">
        <div class="mb-2 font-medium">订单状态</div>
        <p class="mb-3 text-xs text-mute">按当前履约状态拆分</p>
        <ChartBox :option="statusOption" height="300px" />
      </div>
    </div>

    <div class="grid grid-cols-1 gap-4 xl:grid-cols-2">
      <div class="admin-card p-5">
        <div class="mb-2 font-medium">库存最多的商品</div>
        <p class="mb-3 text-xs text-mute">按商品名称展示，避免一堆「默认规格」看不清</p>
        <p class="mb-3 text-xs text-mute">充足 {{ stockHealth.ok }} · 预警 {{ stockHealth.warn }} · 售罄 {{ stockHealth.empty }}</p>
        <ChartBox v-if="skus.length" :option="stockOption" height="260px" />
        <div v-else class="py-16 text-center text-sm text-mute">还没有 SKU，先去发布商品</div>
      </div>
      <div class="admin-card p-5">
        <div class="mb-4 font-medium">快捷入口</div>
        <div class="space-y-2">
          <RouterLink
            v-for="item in shortcuts"
            :key="item.to"
            :to="item.to"
            class="flex items-center gap-3 rounded-2xl border border-slate-100 px-4 py-3 transition hover:border-teal-200 hover:bg-teal-50/60"
          >
            <span class="inline-flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-slate-100 text-teal-700">
              <Icon :icon="item.icon" width="20" />
            </span>
            <div>
              <div class="font-medium">{{ item.label }}</div>
              <div class="text-xs text-mute">{{ item.desc }}</div>
            </div>
          </RouterLink>
        </div>
      </div>
    </div>
  </MerchantPage>
</template>
