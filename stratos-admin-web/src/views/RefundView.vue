<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useMessage } from 'naive-ui'
import type { EChartsOption } from 'echarts'
import { adminRefunds, auditRefund } from '@/api'
import AdminPage from '@/components/AdminPage.vue'
import ChartBox from '@/components/ChartBox.vue'
import FormField from '@/components/FormField.vue'
import { minLen, textareaClass } from '@/utils/validate'

const statusText: Record<number, string> = {
  0: '待审核',
  1: '已通过',
  2: '已拒绝',
  10: '退款中',
  20: '退款成功',
  [-10]: '已取消',
}

const message = useMessage()
const list = ref<any[]>([])
const remarks = ref<Record<string, string>>({})
const remarkErrors = ref<Record<string, string>>({})
const filter = ref<number | ''>('')

const shown = computed(() =>
  filter.value === '' ? list.value : list.value.filter((item) => Number(item.status) === Number(filter.value)),
)

const option = computed<EChartsOption>(() => ({
  color: ['#f59e0b', '#10b981', '#fb7185', '#94a3b8'],
  tooltip: { trigger: 'item' },
  series: [
    {
      type: 'pie',
      radius: ['42%', '70%'],
      label: { formatter: '{b}\n{c}' },
      data: [
        { name: '待审核', value: list.value.filter((item) => item.status === 0).length },
        { name: '已通过', value: list.value.filter((item) => [1, 10, 20].includes(Number(item.status))).length },
        { name: '已拒绝', value: list.value.filter((item) => item.status === 2).length },
        { name: '已取消', value: list.value.filter((item) => item.status === -10).length },
      ],
    },
  ],
}))

async function load() {
  list.value = (await adminRefunds()) || []
}

async function decide(id: string | number, approved: boolean) {
  const key = String(id)
  const remark = (remarks.value[key] || '').trim()
  if (!approved) {
    const error = minLen(remark, 4, '拒绝原因')
    remarkErrors.value[key] = error
    if (error) {
      message.error(error)
      return
    }
  } else {
    remarkErrors.value[key] = ''
  }
  await auditRefund({
    refundId: id,
    approved,
    auditRemark: remark || (approved ? '后台同意退款' : '后台拒绝退款'),
  })
  message.success(approved ? '已同意' : '已拒绝')
  await load()
}

onMounted(load)
</script>

<template>
  <AdminPage title="售后" extra="买家申请退款后，在这里填写审核意见并处理">
    <div class="grid grid-cols-1 gap-4 xl:grid-cols-[1fr_280px]">
      <div class="admin-card flex flex-wrap items-center gap-2 p-5">
        <button
          v-for="item in [
            { id: '', label: '全部' },
            { id: 0, label: '待审核' },
            { id: 1, label: '已通过' },
            { id: 2, label: '已拒绝' },
          ]"
          :key="String(item.id)"
          class="h-9 rounded-full px-4 text-sm"
          :class="filter === item.id ? 'bg-sky-500 text-white' : 'bg-slate-100 text-mute'"
          @click="filter = item.id as any"
        >
          {{ item.label }}
        </button>
      </div>
      <div class="admin-card p-5">
        <div class="mb-2 text-sm font-medium">售后分布</div>
        <ChartBox :option="option" height="180px" />
      </div>
    </div>

    <div class="space-y-3">
      <div v-for="item in shown" :key="item.id" class="admin-card space-y-3 p-5">
        <div class="flex items-center justify-between">
          <div>
            <div class="font-medium">{{ item.refundNo }}</div>
            <div class="mt-1 text-sm text-mute">订单 {{ item.orderNo }} · ¥{{ Number(item.refundAmount || 0).toFixed(2) }}</div>
          </div>
          <span class="admin-badge" :class="item.status === 0 ? 'admin-badge-warn' : item.status === 2 ? 'admin-badge-mute' : 'admin-badge-ok'">
            {{ statusText[item.status] || item.status }}
          </span>
        </div>
        <div class="text-sm">{{ item.refundDesc || item.refundReason || '买家未填写说明' }}</div>
        <form v-if="item.status === 0" class="space-y-3" @submit.prevent>
          <FormField label="审核意见" :error="remarkErrors[String(item.id)]" hint="拒绝时必须填写至少 4 个字">
            <textarea
              v-model="remarks[String(item.id)]"
              :class="textareaClass(remarkErrors[String(item.id)])"
              placeholder="同意或拒绝时会带上这段说明"
              maxlength="120"
            />
          </FormField>
          <div class="space-x-2">
            <button type="button" class="admin-btn" @click="decide(item.id, true)">同意退款</button>
            <button type="button" class="admin-btn-danger" @click="decide(item.id, false)">拒绝</button>
          </div>
        </form>
      </div>
      <div v-if="!shown.length" class="admin-card py-10 text-center text-sm text-mute">
        {{ list.length ? '没有符合筛选的售后单' : '暂无售后单' }}
      </div>
    </div>
  </AdminPage>
</template>
