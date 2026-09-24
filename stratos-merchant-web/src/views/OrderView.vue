<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useMessage } from 'naive-ui'
import { merchantOrderDetail, merchantOrders, remarkOrder, shipOrder } from '@/api'
import { useAuthStore } from '@/stores/auth'
import FormField from '@/components/FormField.vue'
import MerchantPage from '@/components/MerchantPage.vue'
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

const auth = useAuthStore()
const message = useMessage()
const records = ref<any[]>([])
const company = ref('SF')
const detail = ref<any | null>(null)
const remark = ref('')
const keyword = ref('')
const statusFilter = ref<number | ''>('')
const remarkError = ref('')
const actingId = ref<string | number | null>(null)
const shown = computed(() =>
  records.value.filter((item) => {
    const hitStatus = statusFilter.value === '' || Number(item.status) === Number(statusFilter.value)
    const text = `${item.orderNo || ''}${item.receiverName || ''}${item.receiverPhone || ''}`
    const hitKeyword = !keyword.value.trim() || text.includes(keyword.value.trim())
    return hitStatus && hitKeyword
  }),
)

function canShip(status?: number) {
  return status === 10 || status === 20
}

async function load() {
  if (!auth.shopId) return
  const page = await merchantOrders({ pageNum: 1, pageSize: 50, shopId: auth.shopId })
  records.value = page.records || []
}

async function openDetail(id: string | number) {
  detail.value = await merchantOrderDetail(id, auth.shopId!)
}

async function saveRemark(id: string | number) {
  remarkError.value = required(remark.value, '卖家备注') || minLen(remark.value, 2, '卖家备注')
  if (remarkError.value) {
    message.error(remarkError.value)
    return
  }
  actingId.value = id
  try {
    await remarkOrder(id, auth.shopId!, remark.value.trim())
    message.success('已备注')
  } finally {
    actingId.value = null
  }
}

async function ship(id: string | number, status?: number) {
  if (!canShip(status)) {
    message.error('只有已支付或待发货的订单可以发货')
    return
  }
  if (!company.value) {
    message.error('请选择快递公司')
    return
  }
  actingId.value = id
  try {
    await shipOrder(id, auth.shopId!, company.value)
    message.success('已发货')
    await load()
    if (detail.value && (detail.value.id === id || detail.value.orderInfo?.id === id)) {
      await openDetail(id)
    }
  } finally {
    actingId.value = null
  }
}

function badgeClass(status: number) {
  if (status === 0) return 'admin-badge admin-badge-warn'
  if (status >= 10) return 'admin-badge admin-badge-ok'
  return 'admin-badge admin-badge-mute'
}

onMounted(load)
</script>

<template>
  <MerchantPage title="订单" extra="只看本店订单。先填备注和快递公司，再对某一单点备注或发货。">
    <form class="admin-card grid grid-cols-2 gap-4 p-5 xl:grid-cols-4" @submit.prevent>
      <FormField label="快递公司">
        <select v-model="company" :class="selectClass()">
          <option value="SF">顺丰</option>
          <option value="YTO">圆通</option>
          <option value="ZTO">中通</option>
          <option value="YD">韵达</option>
        </select>
      </FormField>
      <FormField label="卖家备注" hint="点某一单的「备注」时使用这段文字" :error="remarkError">
        <input v-model="remark" :class="inputClass(remarkError)" placeholder="例如：已联系买家，今日发出" maxlength="80" />
      </FormField>
      <FormField label="状态筛选">
        <select v-model="statusFilter" class="admin-select">
          <option value="">全部</option>
          <option :value="0">待支付</option>
          <option :value="10">已支付</option>
          <option :value="20">待发货</option>
          <option :value="30">已发货</option>
        </select>
      </FormField>
      <FormField label="搜索">
        <input v-model="keyword" class="admin-input" placeholder="单号 / 收货人 / 手机号" maxlength="40" />
      </FormField>
    </form>
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
            <td>{{ item.receiverName }} {{ item.receiverPhone }}</td>
            <td>¥{{ Number(item.payAmount || 0).toFixed(2) }}</td>
            <td class="space-x-2 whitespace-nowrap">
              <button class="admin-btn" @click="openDetail(item.id)">详情</button>
              <button class="admin-btn" :disabled="actingId === item.id" @click="saveRemark(item.id)">备注</button>
              <button class="admin-btn" :disabled="actingId === item.id || !canShip(item.status)" @click="ship(item.id, item.status)">发货</button>
            </td>
          </tr>
          <tr v-if="!shown.length">
            <td colspan="5" class="py-10 text-center text-mute">{{ records.length ? '没有符合筛选的订单' : '暂无本店订单' }}</td>
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
  </MerchantPage>
</template>
