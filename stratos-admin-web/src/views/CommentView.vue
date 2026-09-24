<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useMessage } from 'naive-ui'
import type { EChartsOption } from 'echarts'
import { auditComment, commentAdminList, replyComment } from '@/api'
import AdminPage from '@/components/AdminPage.vue'
import ChartBox from '@/components/ChartBox.vue'
import FormField from '@/components/FormField.vue'
import { maxLen, minLen, required, textareaClass } from '@/utils/validate'

const auditText: Record<number, string> = { 0: '待审核', 1: '已通过', 2: '已拒绝' }
const message = useMessage()
const list = ref<any[]>([])
const drafts = ref<Record<string, string>>({})
const draftErrors = ref<Record<string, string>>({})
const filter = ref<number | ''>('')

const shown = computed(() =>
  filter.value === '' ? list.value : list.value.filter((item) => Number(item.auditStatus ?? 0) === Number(filter.value)),
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
        { name: '待审核', value: list.value.filter((item) => !item.auditStatus || item.auditStatus === 0).length },
        { name: '已通过', value: list.value.filter((item) => item.auditStatus === 1).length },
        { name: '已拒绝', value: list.value.filter((item) => item.auditStatus === 2).length },
      ],
    },
  ],
}))

async function load() {
  list.value = (await commentAdminList()) || []
}

async function reply(item: any) {
  const key = String(item.id)
  const content = (drafts.value[key] || '').trim()
  const error = required(content, '回复内容') || minLen(content, 2, '回复内容') || maxLen(content, 200, '回复内容')
  draftErrors.value[key] = error
  if (error) {
    message.error(error)
    return
  }
  await replyComment(item.id, content)
  message.success('已回复')
  drafts.value[key] = ''
  await load()
}

async function audit(item: any, status: number) {
  await auditComment(item.id, status)
  message.success(status === 1 ? '已通过' : '已拒绝')
  await load()
}

onMounted(load)
</script>

<template>
  <AdminPage title="评价审核" extra="回复买家并审核展示状态。通过后会显示在商品详情。">
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
      <div v-for="item in shown" :key="item.id" class="admin-card space-y-3 p-5">
        <div class="flex items-start justify-between gap-4">
          <div>
            <div class="font-medium">{{ item.nickname || '买家' }} · {{ item.starRating || '-' }} 星</div>
            <div class="mt-1 text-sm text-mute">{{ item.content }}</div>
            <div v-if="item.replyContent" class="mt-2 rounded-2xl bg-slate-50 px-3 py-2 text-sm">商家回复：{{ item.replyContent }}</div>
          </div>
          <span :class="item.auditStatus === 1 ? 'admin-badge admin-badge-ok' : 'admin-badge admin-badge-warn'">
            {{ auditText[item.auditStatus ?? 0] }}
          </span>
        </div>
        <FormField label="回复内容" :error="draftErrors[String(item.id)]">
          <textarea
            v-model="drafts[String(item.id)]"
            :class="textareaClass(draftErrors[String(item.id)])"
            maxlength="200"
            placeholder="感谢支持，欢迎再次光临"
          />
        </FormField>
        <div class="space-x-2">
          <button class="admin-btn" @click="reply(item)">回复</button>
          <button class="admin-btn" @click="audit(item, 1)">通过</button>
          <button class="admin-btn-danger" @click="audit(item, 2)">拒绝</button>
        </div>
      </div>
      <div v-if="!shown.length" class="admin-card py-10 text-center text-sm text-mute">
        {{ list.length ? '没有符合筛选的评价' : '暂无评价' }}
      </div>
    </div>
  </AdminPage>
</template>
