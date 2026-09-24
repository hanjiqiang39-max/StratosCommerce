<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useMessage } from 'naive-ui'
import type { EChartsOption } from 'echarts'
import { adminOrders, cancelOrder, companyAdminList, getOrder, remarkOrder, shipOrder } from '@/api'
import AdminPage from '@/components/AdminPage.vue'
import ChartBox from '@/components/ChartBox.vue'
import FormField from '@/components/FormField.vue'
import { inputClass, minLen, required, selectClass } from '@/utils/validate'

const statusText: Record<number, string> = {
  0: '待支付',
  10: '已支付',
  20: '待发货',
  30: '已发货',
  40: '已收货',
  50: '已完成',
  [-10]: '已取消',
  [-20]: '退款中',
  [-30]: '已退款',
}

const message = useMessage()
const records = ref<any[]>([])
const companies = ref<any[]>([])
const company = ref('SF')
const remark = ref('')
const remarkError = ref('')
const statusFilter = ref<number | ''>('')
const keyword = ref('')
const actingId = ref<string | number | null>(null)
const detail = ref<any>(null)

const shown = computed(() => {
  const key = keyword.value.trim().toLowerCase()
  return records.value.filter((item) => {
    if (statusFilter.value !== '' && Number(item.status) !== Number(statusFilter.value)) return false
    if (!key) return true
    return [item.orderNo, item.receiverName, item.receiverPhone].some((value) => String(value || '').toLowerCase().includes(key))
  })
})

const option = computed<EChartsOption>(() => {
  const keys = [0, 10, 20, 30, -10, -20]
  return {
    color: ['#f59e0b', '#0ea5e9', '#14b8a6', '#8b5cf6', '#94a3b8', '#fb7185'],
    tooltip: { trigger: 'item' },
    series: [
      {
        type: 'pie',
        radius: ['40%', '68%'],
        label: { formatter: '{b} {c}' },
        data: keys.map((status) => ({
          name: statusText[status],
          value: records.value.filter((item) => Number(item.status) === status).length,
        })),
      },
    ],
  }
})

function badgeClass(status: number) {
  if (status === 0) return 'admin-badge admin-badge-warn'
  if (status >= 10) return 'admin-badge admin-badge-ok'
  return 'admin-badge admin-badge-mute'
}

function canShip(status: number) {
  return status === 10 || status === 20
}

async function load() {
  const page = await adminOrders({ pageNum: 1, pageSize: 100 })
  records.value = page.records || []
  companies.value = (await companyAdminList()) || []
  if (companies.value[0]?.companyCode) company.value = companies.value[0].companyCode
}

async function openDetail(id: string | number) {
  detail.value = await getOrder(id)
}

async function saveRemark(id: string | number) {
  const error = required(remark.value, '卖家备注') || minLen(remark.value, 2, '卖家备注')
  remarkError.value = error
  if (error) {
    message.error(error)
    return
  }
  actingId.value = id
  try {
    await remarkOrder(id, remark.value.trim())
    message.success('已备注')
    await load()
  } finally {
    actingId.value = null
  }
}

async function ship(id: string | number, status: number) {
  if (!canShip(status)) {
    message.error('只有已支付或待发货的订单可以发货')
    return
  }
  actingId.value = id
  try {
    await shipOrder(id, company.value)
    message.success('已发货')
    await load()
  } finally {
    actingId.value = null
  }
}

async function cancel(id: string | number, status: number) {
  if (status !== 0) {
    message.error('只能取消待支付订单')
    return
  }
  if (!window.confirm('确定取消这张待支付订单？')) return
  actingId.value = id
  try {
    await cancelOrder(id)
    message.success('已取消')
    await load()
  } finally {
    actingId.value = null
  }
}

onMounted(load)
</script>

<template>
  <AdminPage title="订单" extra="筛选后处理备注、发货或取消。发货会走物流公司编码。">
    <div class="grid grid-cols-1 gap-4 xl:grid-cols-[1fr_280px]">
      <form class="admin-card grid grid-cols-2 gap-4 p-5 xl:grid-cols-4" @submit.prevent>
        <FormField label="快递公司">
          <select v-model="company" :class="selectClass()">
            <option v-for="item in companies" :key="item.id" :value="item.companyCode">{{ item.companyName }}</option>
            <option v-if="!companies.length" value="SF">顺丰</option>
          </select>
        </FormField>
        <FormField label="卖家备注" hint="点某一单的「备注」时使用" :error="remarkError">
          <input v-model="remark" :class="inputClass(remarkError)" placeholder="例如：今日发出" maxlength="80" />
        </FormField>
        <FormField label="状态筛选">
          <select v-model="statusFilter" class="admin-select">
            <option value="">全部</option>
            <option :value="0">待支付</option>
            <option :value="10">已支付</option>
            <option :value="20">待发货</option>
            <option :value="30">已发货</option>
            <option :value="-10">已取消</option>
            <option :value="-20">退款中</option>
          </select>
        </FormField>
        <FormField label="搜索">
          <input v-model="keyword" class="admin-input" placeholder="单号 / 收货人 / 手机号" maxlength="40" />
        </FormField>
      </form>
      <div class="admin-card p-5">
        <div class="mb-2 text-sm font-medium">状态分布</div>
        <ChartBox :option="option" height="180px" />
      </div>
    </div>

    <div class="admin-card">
      <table class="admin-table">
        <thead>
          <tr>
            <th>单号</th>
            <th>状态</th>
            <th>收货人</th>
            <th>金额</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in shown" :key="item.id">
            <td class="font-medium">{{ item.orderNo }}</td>
            <td>
              <span :class="badgeClass(item.status)">{{ statusText[item.status] || item.status }}</span>
            </td>
            <td>{{ item.receiverName || '-' }} {{ item.receiverPhone || '' }}</td>
            <td>¥{{ Number(item.payAmount || 0).toFixed(2) }}</td>
            <td class="space-x-2 whitespace-nowrap">
              <button class="admin-btn" @click="openDetail(item.id)">详情</button>
              <button class="admin-btn" :disabled="actingId === item.id" @click="saveRemark(item.id)">备注</button>
              <button class="admin-btn" :disabled="actingId === item.id || !canShip(item.status)" @click="ship(item.id, item.status)">发货</button>
              <button class="admin-btn-danger" :disabled="actingId === item.id || item.status !== 0" @click="cancel(item.id, item.status)">取消</button>
            </td>
          </tr>
          <tr v-if="!shown.length">
            <td colspan="5" class="py-10 text-center text-mute">{{ records.length ? '没有符合筛选的订单' : '暂无订单' }}</td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="detail" class="admin-card space-y-3 p-6">
      <div class="flex items-center justify-between">
        <div class="font-medium">订单 {{ detail.orderNo || detail.orderInfo?.orderNo }}</div>
        <button class="admin-btn" @click="detail = null">关闭</button>
      </div>
      <div class="text-sm text-mute">
        {{ detail.receiverName || detail.orderInfo?.receiverName }}
        {{ detail.receiverPhone || detail.orderInfo?.receiverPhone }}
        {{ detail.receiverDetailAddress || detail.orderInfo?.receiverDetailAddress }}
      </div>
      <div v-for="item in (detail.items || detail.itemList || [])" :key="item.id" class="flex justify-between text-sm">
        <span>{{ item.skuName }} × {{ item.quantity }}</span>
        <span>¥{{ Number(item.realAmount || item.totalAmount || 0).toFixed(2) }}</span>
      </div>
    </div>
  </AdminPage>
</template>
