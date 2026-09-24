<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useMessage } from 'naive-ui'
import type { EChartsOption } from 'echarts'
import { auditMerchant, merchantList, shopList, updateMerchantStatus } from '@/api'
import AdminPage from '@/components/AdminPage.vue'
import ChartBox from '@/components/ChartBox.vue'
import FormField from '@/components/FormField.vue'
import { minLen, textareaClass } from '@/utils/validate'

const auditText: Record<number, string> = { 0: '待审核', 1: '已通过', 2: '已拒绝' }
const message = useMessage()
const records = ref<any[]>([])
const shops = ref<any[]>([])
const remarks = ref<Record<string, string>>({})
const remarkErrors = ref<Record<string, string>>({})
const filter = ref<number | ''>('')

const shown = computed(() =>
  filter.value === '' ? records.value : records.value.filter((item) => Number(item.auditStatus) === Number(filter.value)),
)

const option = computed<EChartsOption>(() => ({
  color: ['#f59e0b', '#10b981', '#fb7185'],
  tooltip: { trigger: 'item' },
  series: [
    {
      type: 'pie',
      radius: ['42%', '70%'],
      label: { formatter: '{b}\n{c}' },
      data: [
        { name: '待审核', value: records.value.filter((item) => item.auditStatus === 0).length },
        { name: '已通过', value: records.value.filter((item) => item.auditStatus === 1).length },
        { name: '已拒绝', value: records.value.filter((item) => item.auditStatus === 2).length },
      ],
    },
  ],
}))

function shopName(merchantId: string | number) {
  return shops.value.find((item) => String(item.merchantId) === String(merchantId))?.shopName || '-'
}

async function load() {
  const page = await merchantList({ pageNum: 1, pageSize: 50 })
  records.value = page.records || []
  shops.value = (await shopList().catch(() => [])) || []
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
  await auditMerchant({
    merchantId: id,
    approved,
    auditRemark: remark || (approved ? '后台通过入驻' : '后台拒绝入驻'),
  })
  message.success(approved ? '已通过' : '已拒绝')
  await load()
}

async function toggle(item: any) {
  const next = item.status === 1 ? 2 : 1
  if (next === 2 && !window.confirm(`确定禁用商家 ${item.username}？`)) return
  await updateMerchantStatus(item.id, next)
  message.success(next === 1 ? '已启用' : '已禁用')
  await load()
}

onMounted(load)
</script>

<template>
  <AdminPage title="商家入驻" extra="审核通过后商家才能登录商家端。拒绝时请写明原因。">
    <div class="grid grid-cols-1 gap-4 xl:grid-cols-[1fr_260px]">
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
        <ChartBox :option="option" height="160px" />
      </div>
    </div>

    <div class="space-y-3">
      <div v-for="item in shown" :key="item.id" class="admin-card p-5">
        <div class="flex flex-wrap items-start justify-between gap-4">
          <div>
            <div class="font-medium">{{ item.username }} · {{ shopName(item.id) }}</div>
            <div class="mt-1 text-sm text-mute">{{ item.realName || '-' }} / {{ item.phone || '-' }}</div>
          </div>
          <div class="flex items-center gap-2">
            <span :class="item.auditStatus === 1 ? 'admin-badge admin-badge-ok' : 'admin-badge admin-badge-warn'">
              {{ auditText[item.auditStatus] || item.auditStatus }}
            </span>
            <span class="admin-badge admin-badge-mute">{{ item.status === 1 ? '正常' : item.status === 2 ? '禁用' : '待审核' }}</span>
          </div>
        </div>
        <form v-if="item.auditStatus === 0" class="mt-4 space-y-3" @submit.prevent>
          <FormField label="审核意见" :error="remarkErrors[String(item.id)]" hint="拒绝时至少写 4 个字">
            <textarea
              v-model="remarks[String(item.id)]"
              :class="textareaClass(remarkErrors[String(item.id)])"
              maxlength="120"
              placeholder="通过或拒绝时会带上这段说明"
            />
          </FormField>
          <div class="space-x-2">
            <button type="button" class="admin-btn" @click="decide(item.id, true)">通过</button>
            <button type="button" class="admin-btn-danger" @click="decide(item.id, false)">拒绝</button>
          </div>
        </form>
        <div v-else class="mt-4">
          <button class="admin-btn" @click="toggle(item)">{{ item.status === 1 ? '禁用' : '启用' }}</button>
        </div>
      </div>
      <div v-if="!shown.length" class="admin-card py-10 text-center text-sm text-mute">
        {{ records.length ? '没有符合筛选的商家' : '暂无商家' }}
      </div>
    </div>
  </AdminPage>
</template>
