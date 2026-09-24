<script setup lang="ts">
import { Icon } from '@iconify/vue'
import { computed, onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import type { EChartsOption } from 'echarts'
import {
  adminOrders,
  adminRefunds,
  adminSkus,
  merchantList,
  orderReport,
  productList,
  promoCoupon,
  promoGroup,
  promoSeckill,
} from '@/api'
import { chartLabel, skuLabel, stockOf, stockTone } from '@/utils/sku'
import AdminPage from '@/components/AdminPage.vue'
import ChartBox from '@/components/ChartBox.vue'

const data = ref<Record<string, any>>({})
const orders = ref<any[]>([])
const products = ref<any[]>([])
const seckills = ref<any[]>([])
const coupons = ref<any[]>([])
const groups = ref<any[]>([])
const refunds = ref<any[]>([])
const merchants = ref<any[]>([])
const skus = ref<any[]>([])

const cards = [
  { key: 'orderCount', label: '订单数', hint: '全部订单', icon: 'solar:bill-list-bold-duotone', tone: 'bg-sky-50 text-sky-600' },
  { key: 'payAmount', label: '成交额', hint: '已支付金额', icon: 'solar:wad-of-money-bold-duotone', tone: 'bg-emerald-50 text-emerald-600' },
  { key: 'unpaidCount', label: '待支付', hint: '需要跟进', icon: 'solar:clock-circle-bold-duotone', tone: 'bg-amber-50 text-amber-600' },
  { key: 'shippedCount', label: '已发货', hint: '履约中', icon: 'solar:delivery-bold-duotone', tone: 'bg-violet-50 text-violet-600' },
]
const shortcuts = [
  { to: '/orders', label: '订单履约', desc: '备注、发货和取消', icon: 'solar:clipboard-list-bold-duotone' },
  { to: '/refunds', label: '售后审核', desc: '同意或拒绝退款', icon: 'solar:restart-bold-duotone' },
  { to: '/products', label: '商品管理', desc: '上架、改图和下架', icon: 'solar:box-bold-duotone' },
  { to: '/inventory', label: '库存预警', desc: '查看和调整 SKU', icon: 'solar:box-minimalistic-bold-duotone' },
  { to: '/promotion', label: '营销活动', desc: '秒杀、优惠券、拼团', icon: 'solar:bolt-bold-duotone' },
  { to: '/merchants', label: '商家审核', desc: '入驻通过才能开店', icon: 'solar:shop-bold-duotone' },
]

function dayKey(offset: number) {
  const date = new Date()
  date.setHours(0, 0, 0, 0)
  date.setDate(date.getDate() + offset)
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return { label: `${month}-${day}`, stamp: `${date.getFullYear()}-${month}-${day}` }
}

function orderDay(item: any) {
  return String(item.createTime || item.create_time || '').replace('T', ' ').slice(0, 10)
}

const trendDays = computed(() => Array.from({ length: 7 }, (_, index) => dayKey(index - 6)))

const trendOption = computed<EChartsOption>(() => {
  const backend = Array.isArray(data.value.daily) ? data.value.daily : []
  const counts = trendDays.value.map((day, index) => {
    const row = backend.find((item: any) => String(item.date) === day.stamp)
    if (row) return Number(row.orderCount || 0)
    return orders.value.filter((item) => orderDay(item) === day.stamp).length || (backend[index] ? Number(backend[index].orderCount || 0) : 0)
  })
  const amounts = trendDays.value.map((day, index) => {
    const row = backend.find((item: any) => String(item.date) === day.stamp)
    if (row) return Number(row.payAmount || 0)
    const fromOrders = orders.value
      .filter((item) => orderDay(item) === day.stamp && Number(item.status) >= 10)
      .reduce((sum, item) => sum + Number(item.payAmount || 0), 0)
    return fromOrders || (backend[index] ? Number(backend[index].payAmount || 0) : 0)
  })
  return {
    color: ['#0ea5e9', '#f59e0b'],
    tooltip: { trigger: 'axis' },
    legend: { data: ['订单数', '成交额'], bottom: 0 },
    grid: { left: 36, right: 36, top: 24, bottom: 36 },
    xAxis: { type: 'category', data: trendDays.value.map((item) => item.label), boundaryGap: false },
    yAxis: [
      { type: 'value', name: '单', minInterval: 1 },
      { type: 'value', name: '元', splitLine: { show: false } },
    ],
    series: [
      { name: '订单数', type: 'line', smooth: true, data: counts, areaStyle: { color: 'rgba(14,165,233,0.12)' } },
      { name: '成交额', type: 'bar', yAxisIndex: 1, barWidth: 14, data: amounts.map((value) => Number(value.toFixed(2))) },
    ],
  }
})

const statusOption = computed<EChartsOption>(() => ({
  color: ['#f59e0b', '#0ea5e9', '#8b5cf6', '#94a3b8'],
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
        { name: '取消/退款', value: Number(data.value.cancelledCount || 0) },
      ],
    },
  ],
}))

const bizOption = computed<EChartsOption>(() => ({
  color: ['#0ea5e9'],
  tooltip: { trigger: 'axis' },
  grid: { left: 36, right: 16, top: 16, bottom: 28 },
  xAxis: { type: 'category', data: ['上架商品', '秒杀', '拼团', '优惠券', '待审商家', '待审售后'] },
  yAxis: { type: 'value', minInterval: 1 },
  series: [
    {
      type: 'bar',
      barWidth: 22,
      data: [
        products.value.filter((item) => item.status === 1).length,
        seckills.value.length,
        groups.value.length,
        coupons.value.length,
        merchants.value.filter((item) => item.auditStatus === 0).length,
        refunds.value.filter((item) => item.status === 0).length,
      ],
    },
  ],
}))

const pendingMerchants = computed(() => merchants.value.filter((item) => item.auditStatus === 0).length)
const pendingRefunds = computed(() => refunds.value.filter((item) => item.status === 0).length)

const stockOption = computed<EChartsOption>(() => {
  const top = [...skus.value].sort((a, b) => stockOf(b) - stockOf(a)).slice(0, 6)
  return {
    color: ['#0ea5e9'],
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
  const [report, page, productPage, seckillRows, couponRows, groupRows, refundRows, merchantPage, skuRows] = await Promise.all([
    orderReport(),
    adminOrders({ pageNum: 1, pageSize: 100 }),
    productList({ pageNum: 1, pageSize: 50, status: -1 }),
    promoSeckill(),
    promoCoupon(),
    promoGroup(),
    adminRefunds().catch(() => []),
    merchantList({ pageNum: 1, pageSize: 50 }),
    adminSkus().catch(() => []),
  ])
  data.value = report || {}
  orders.value = page?.records || []
  products.value = productPage?.records || []
  seckills.value = seckillRows || []
  coupons.value = couponRows || []
  groups.value = groupRows || []
  refunds.value = refundRows || []
  merchants.value = merchantPage?.records || []
  skus.value = skuRows || []
})

function display(key: string) {
  const value = data.value[key] ?? 0
  return key === 'payAmount' ? `¥${Number(value).toFixed(2)}` : value
}
</script>

<template>
  <AdminPage title="工作台" extra="订单、营销和待办都汇总在这里，图表按最近数据刷新">
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
        <p class="mb-3 text-xs text-mute">充足 {{ stockHealth.ok }} · 预警 {{ stockHealth.warn }} · 售罄 {{ stockHealth.empty }}</p>
        <ChartBox v-if="skus.length" :option="stockOption" height="260px" />
        <div v-else class="py-16 text-center text-sm text-mute">还没有 SKU</div>
      </div>
      <div class="admin-card p-5">
        <div class="mb-2 font-medium">运营盘点</div>
        <p class="mb-3 text-xs text-mute">商品、活动和待审核事项</p>
        <ChartBox :option="bizOption" height="260px" />
      </div>
    </div>

    <div class="grid grid-cols-1 gap-4">
      <div class="admin-card p-5">
        <div class="mb-4 font-medium">待办与入口</div>
        <div class="mb-4 grid grid-cols-2 gap-3 text-sm">
          <RouterLink to="/merchants" class="rounded-2xl bg-amber-50 px-4 py-3">
            <div class="text-xs text-amber-700">待审商家</div>
            <div class="mt-1 text-2xl font-semibold">{{ pendingMerchants }}</div>
          </RouterLink>
          <RouterLink to="/refunds" class="rounded-2xl bg-rose-50 px-4 py-3">
            <div class="text-xs text-rose-600">待审售后</div>
            <div class="mt-1 text-2xl font-semibold">{{ pendingRefunds }}</div>
          </RouterLink>
        </div>
        <div class="space-y-2">
          <RouterLink
            v-for="item in shortcuts"
            :key="item.to"
            :to="item.to"
            class="flex items-center gap-3 rounded-2xl border border-slate-100 px-4 py-3 transition hover:border-sky-200 hover:bg-sky-50/60"
          >
            <span class="inline-flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-slate-100 text-sky-600">
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
  </AdminPage>
</template>
