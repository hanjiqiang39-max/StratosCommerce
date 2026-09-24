<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useMessage } from 'naive-ui'
import type { EChartsOption } from 'echarts'
import { adminSkus, updateSkuStock } from '@/api'
import AdminPage from '@/components/AdminPage.vue'
import ChartBox from '@/components/ChartBox.vue'
import { chartLabel, skuLabel, skuSpec, skuTitle, stockOf, stockTone } from '@/utils/sku'
import { numberRange } from '@/utils/validate'

const message = useMessage()
const records = ref<any[]>([])
const keyword = ref('')
const drafts = ref<Record<string, number>>({})
const actingId = ref<string | number | null>(null)
const sortMode = ref<'low' | 'high'>('low')

const shown = computed(() => {
  const key = keyword.value.trim().toLowerCase()
  const rows = records.value.filter((item) => {
    if (!key) return true
    return [skuLabel(item), skuTitle(item), item.skuName, item.skuCode, item.id]
      .some((value) => String(value || '').toLowerCase().includes(key))
  })
  return [...rows].sort((a, b) =>
    sortMode.value === 'low' ? stockOf(a) - stockOf(b) : stockOf(b) - stockOf(a),
  )
})

const totalStock = computed(() => records.value.reduce((sum, item) => sum + stockOf(item), 0))
const lockedStock = computed(() => records.value.reduce((sum, item) => sum + Number(item.lockStock || 0), 0))
const emptyCount = computed(() => records.value.filter((item) => stockTone(item) === 'empty').length)
const warnCount = computed(() => records.value.filter((item) => stockTone(item) === 'warn').length)
const lowCount = computed(() => emptyCount.value + warnCount.value)

const barOption = computed<EChartsOption>(() => {
  const rows = shown.value.slice(0, 8)
  return {
    color: ['#0ea5e9', '#f59e0b'],
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter: (params: any) => {
        const index = params?.[0]?.dataIndex ?? 0
        const item = rows[index]
        if (!item) return ''
        return `${skuLabel(item)}<br/>可售 ${stockOf(item)}<br/>锁定 ${Number(item.lockStock || 0)}`
      },
    },
    legend: { data: ['可售库存', '锁定'], bottom: 0 },
    grid: { left: 92, right: 16, top: 12, bottom: 36 },
    xAxis: { type: 'value', minInterval: 1 },
    yAxis: {
      type: 'category',
      data: rows.map((item) => chartLabel(skuLabel(item), 10)).reverse(),
      axisLabel: { width: 80, overflow: 'truncate' },
    },
    series: [
      { name: '可售库存', type: 'bar', barWidth: 12, data: rows.map((item) => stockOf(item)).reverse() },
      { name: '锁定', type: 'bar', barWidth: 12, data: rows.map((item) => Number(item.lockStock || 0)).reverse() },
    ],
  }
})

const pieOption = computed<EChartsOption>(() => ({
  color: ['#0ea5e9', '#f59e0b', '#94a3b8'],
  tooltip: { trigger: 'item', formatter: '{b} {c} 个' },
  legend: { bottom: 0 },
  series: [
    {
      type: 'pie',
      radius: ['46%', '70%'],
      center: ['50%', '44%'],
      label: { formatter: '{b}\n{c}' },
      data: [
        { name: '库存充足', value: records.value.filter((item) => stockTone(item) === 'ok').length },
        { name: '库存预警', value: warnCount.value },
        { name: '已售罄', value: emptyCount.value },
      ],
    },
  ],
}))

async function load() {
  records.value = (await adminSkus()) || []
  drafts.value = Object.fromEntries(records.value.map((item) => [String(item.id), stockOf(item)]))
}

async function save(item: any) {
  const stock = Number(drafts.value[String(item.id)])
  const error = numberRange(stock, 0, 999999, '库存', true)
  if (error) {
    message.error(error)
    return
  }
  actingId.value = item.id
  try {
    await updateSkuStock(item.id, stock)
    message.success('库存已更新')
    await load()
  } finally {
    actingId.value = null
  }
}

onMounted(load)
</script>

<template>
  <AdminPage title="库存" extra="全站 SKU 按商品名称展示，库存偏低可直接改，并同步到下单锁定。">
    <div class="grid grid-cols-2 gap-4 xl:grid-cols-4">
      <div class="admin-card p-5">
        <div class="text-sm text-mute">SKU 数量</div>
        <div class="mt-2 text-3xl font-semibold">{{ records.length }}</div>
      </div>
      <div class="admin-card p-5">
        <div class="text-sm text-mute">可售库存</div>
        <div class="mt-2 text-3xl font-semibold">{{ totalStock }}</div>
      </div>
      <div class="admin-card p-5">
        <div class="text-sm text-mute">锁定库存</div>
        <div class="mt-2 text-3xl font-semibold">{{ lockedStock }}</div>
      </div>
      <div class="admin-card p-5">
        <div class="text-sm text-mute">预警 / 售罄</div>
        <div class="mt-2 text-3xl font-semibold">{{ lowCount }}</div>
      </div>
    </div>

    <div class="grid grid-cols-1 gap-4 xl:grid-cols-[1.3fr_1fr]">
      <div class="admin-card p-5">
        <div class="mb-2 font-medium">库存结构</div>
        <p class="mb-3 text-xs text-mute">按当前筛选结果展示前 8 个规格，悬停可看完整商品名</p>
        <ChartBox v-if="shown.length" :option="barOption" height="300px" />
        <div v-else class="py-16 text-center text-sm text-mute">暂无 SKU，先去发布商品</div>
      </div>
      <div class="admin-card p-5">
        <div class="mb-2 font-medium">库存健康</div>
        <p class="mb-3 text-xs text-mute">充足、预警和售罄的规格占比</p>
        <ChartBox v-if="records.length" :option="pieOption" height="300px" />
        <div v-else class="py-16 text-center text-sm text-mute">暂无数据</div>
      </div>
    </div>

    <div class="admin-card p-5">
      <div class="mb-4 flex flex-wrap items-center gap-3">
        <input v-model="keyword" class="admin-input max-w-sm" placeholder="搜索商品、规格或编码" maxlength="30" />
        <select v-model="sortMode" class="admin-select w-40">
          <option value="low">库存从低到高</option>
          <option value="high">库存从高到低</option>
        </select>
      </div>
      <table class="admin-table">
        <thead>
          <tr>
            <th>商品 / 规格</th>
            <th>编码</th>
            <th>售价</th>
            <th>可售库存</th>
            <th>锁定</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in shown" :key="item.id">
            <td>
              <div class="font-medium">{{ skuTitle(item) || skuLabel(item) }}</div>
              <div class="text-xs text-mute">{{ skuSpec(item) }}</div>
            </td>
            <td class="text-mute">{{ item.skuCode || '-' }}</td>
            <td>¥{{ Number(item.price || 0).toFixed(2) }}</td>
            <td>
              <div class="flex items-center gap-2">
                <input v-model.number="drafts[String(item.id)]" type="number" min="0" class="admin-input w-24" />
                <span
                  :class="stockTone(item) === 'empty'
                    ? 'admin-badge admin-badge-mute'
                    : stockTone(item) === 'warn'
                      ? 'admin-badge admin-badge-warn'
                      : 'admin-badge admin-badge-ok'"
                >
                  {{ stockTone(item) === 'empty' ? '售罄' : stockTone(item) === 'warn' ? '预警' : '充足' }}
                </span>
              </div>
            </td>
            <td>{{ item.lockStock ?? 0 }}</td>
            <td>
              <button class="admin-btn" :disabled="actingId === item.id" @click="save(item)">保存库存</button>
            </td>
          </tr>
          <tr v-if="!shown.length">
            <td colspan="6" class="py-10 text-center text-mute">{{ records.length ? '没有匹配的规格' : '暂无 SKU，先去发布商品' }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </AdminPage>
</template>
