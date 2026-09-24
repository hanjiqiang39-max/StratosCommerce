<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useMessage } from 'naive-ui'
import type { EChartsOption } from 'echarts'
import { buyerList, updateBuyerStatus } from '@/api'
import AdminPage from '@/components/AdminPage.vue'
import ChartBox from '@/components/ChartBox.vue'

const message = useMessage()
const records = ref<any[]>([])
const keyword = ref('')
const statusFilter = ref<number | ''>('')

const shown = computed(() => {
  const key = keyword.value.trim().toLowerCase()
  return records.value.filter((item) => {
    if (statusFilter.value !== '' && Number(item.status) !== Number(statusFilter.value)) return false
    if (!key) return true
    return [item.username, item.nickname, item.phone].some((value) => String(value || '').toLowerCase().includes(key))
  })
})

const option = computed<EChartsOption>(() => ({
  color: ['#10b981', '#94a3b8'],
  tooltip: { trigger: 'item' },
  series: [
    {
      type: 'pie',
      radius: ['42%', '70%'],
      label: { formatter: '{b}\n{c}' },
      data: [
        { name: '正常', value: records.value.filter((item) => item.status === 1).length },
        { name: '停用', value: records.value.filter((item) => item.status !== 1).length },
      ],
    },
  ],
}))

async function load() {
  const page = await buyerList({ pageNum: 1, pageSize: 100, keyword: keyword.value || undefined })
  records.value = page.records || []
}

async function toggle(item: any) {
  const next = item.status === 1 ? 0 : 1
  if (next === 0 && !window.confirm(`确定停用买家 ${item.username}？停用后无法登录商城。`)) return
  await updateBuyerStatus(item.id, next)
  message.success(next === 1 ? '已启用' : '已停用')
  await load()
}

onMounted(load)
</script>

<template>
  <AdminPage title="买家" extra="搜索会员并启用/停用。停用后该账号不能再登录商城。">
    <div class="grid grid-cols-1 gap-4 xl:grid-cols-[1fr_260px]">
      <div class="admin-card grid grid-cols-2 gap-4 p-5">
        <input v-model="keyword" class="admin-input" placeholder="用户名 / 昵称 / 手机号" maxlength="30" @keyup.enter="load" />
        <select v-model="statusFilter" class="admin-select">
          <option value="">全部状态</option>
          <option :value="1">正常</option>
          <option :value="0">停用</option>
        </select>
      </div>
      <div class="admin-card p-5">
        <ChartBox :option="option" height="160px" />
      </div>
    </div>
    <div class="admin-card">
      <table class="admin-table">
        <thead>
          <tr>
            <th>用户</th>
            <th>手机</th>
            <th>积分</th>
            <th>状态</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in shown" :key="item.id">
            <td>
              <div class="font-medium">{{ item.username }}</div>
              <div class="text-xs text-mute">{{ item.nickname || '-' }}</div>
            </td>
            <td>{{ item.phone || '-' }}</td>
            <td>{{ item.points ?? 0 }}</td>
            <td>
              <span :class="item.status === 1 ? 'admin-badge admin-badge-ok' : 'admin-badge admin-badge-mute'">
                {{ item.status === 1 ? '正常' : '停用' }}
              </span>
            </td>
            <td>
              <button class="admin-btn" @click="toggle(item)">
                {{ item.status === 1 ? '停用' : '启用' }}
              </button>
            </td>
          </tr>
          <tr v-if="!shown.length">
            <td colspan="5" class="py-10 text-center text-mute">{{ records.length ? '没有匹配的买家' : '暂无买家' }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </AdminPage>
</template>
