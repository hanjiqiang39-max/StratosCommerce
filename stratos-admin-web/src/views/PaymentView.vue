<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import type { EChartsOption } from 'echarts'
import { paymentList } from '@/api'
import AdminPage from '@/components/AdminPage.vue'
import ChartBox from '@/components/ChartBox.vue'

const statusText: Record<number, string> = {
  0: '待支付',
  1: '支付中',
  2: '支付成功',
  3: '支付失败',
  4: '已关闭',
}
const channelText: Record<number, string> = {
  1: '支付宝',
  2: '微信',
  3: '银联',
  4: '余额',
}

const records = ref<any[]>([])
const statusFilter = ref<number | ''>('')
const keyword = ref('')

const shown = computed(() => {
  const key = keyword.value.trim().toLowerCase()
  return records.value.filter((item) => {
    if (statusFilter.value !== '' && Number(item.status) !== Number(statusFilter.value)) return false
    if (!key) return true
    return [item.payNo, item.orderNo, item.outTradeNo].some((value) => String(value || '').toLowerCase().includes(key))
  })
})

const option = computed<EChartsOption>(() => ({
  color: ['#f59e0b', '#0ea5e9', '#10b981', '#fb7185', '#94a3b8'],
  tooltip: { trigger: 'item' },
  series: [
    {
      type: 'pie',
      radius: ['42%', '70%'],
      label: { formatter: '{b}\n{c}' },
      data: [0, 1, 2, 3, 4].map((status) => ({
        name: statusText[status],
        value: records.value.filter((item) => Number(item.status) === status).length,
      })),
    },
  ],
}))

const paidAmount = computed(() =>
  records.value
    .filter((item) => Number(item.status) === 2)
    .reduce((sum, item) => sum + Number(item.payAmount || 0), 0),
)

function badgeClass(status: number) {
  if (status === 2) return 'admin-badge admin-badge-ok'
  if (status === 0 || status === 1) return 'admin-badge admin-badge-warn'
  return 'admin-badge admin-badge-mute'
}

async function load() {
  records.value = (await paymentList()) || []
}

onMounted(load)
</script>

<template>
  <AdminPage title="支付单" extra="查看支付宝沙箱等渠道的支付记录，方便核对订单是否到账">
    <div class="grid grid-cols-1 gap-4 xl:grid-cols-[1fr_280px]">
      <div class="admin-card grid grid-cols-2 gap-4 p-5">
        <select v-model="statusFilter" class="admin-select">
          <option value="">全部状态</option>
          <option :value="0">待支付</option>
          <option :value="2">支付成功</option>
          <option :value="3">支付失败</option>
          <option :value="4">已关闭</option>
        </select>
        <input v-model="keyword" class="admin-input" placeholder="支付单号 / 订单号" maxlength="40" />
        <div class="rounded-2xl bg-emerald-50 px-4 py-3">
          <div class="text-xs text-emerald-700">成功金额</div>
          <div class="mt-1 text-2xl font-semibold">¥{{ paidAmount.toFixed(2) }}</div>
        </div>
        <div class="rounded-2xl bg-sky-50 px-4 py-3">
          <div class="text-xs text-sky-700">支付单数</div>
          <div class="mt-1 text-2xl font-semibold">{{ records.length }}</div>
        </div>
      </div>
      <div class="admin-card p-5">
        <div class="mb-2 text-sm font-medium">支付状态</div>
        <ChartBox :option="option" height="180px" />
      </div>
    </div>

    <div class="admin-card">
      <table class="admin-table">
        <thead>
          <tr>
            <th>支付单</th>
            <th>订单</th>
            <th>渠道</th>
            <th>金额</th>
            <th>状态</th>
            <th>时间</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in shown" :key="item.id">
            <td class="font-medium">{{ item.payNo }}</td>
            <td class="text-mute">{{ item.orderNo || '-' }}</td>
            <td>{{ channelText[item.payChannel] || item.payChannel || '-' }}</td>
            <td>¥{{ Number(item.payAmount || 0).toFixed(2) }}</td>
            <td>
              <span :class="badgeClass(item.status)">{{ statusText[item.status] || item.status }}</span>
            </td>
            <td class="text-xs text-mute">{{ item.payTime || item.createTime || '-' }}</td>
          </tr>
          <tr v-if="!shown.length">
            <td colspan="6" class="py-10 text-center text-mute">{{ records.length ? '没有符合筛选的支付单' : '暂无支付单' }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </AdminPage>
</template>
