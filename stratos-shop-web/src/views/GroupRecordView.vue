<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import { groupActivity, groupMembers, groupRecord, joinGroup, myGroup } from '@/api/promotion'
import type { GroupActivity, GroupRecord } from '@/api/types'
import PageHeader from '@/components/PageHeader.vue'
import { useAuthStore } from '@/stores/auth'
import { goGroupFlow } from '@/utils/activity-order'
import { groupStatusText, remainText } from '@/utils/format'

const route = useRoute()
const router = useRouter()
const message = useMessage()
const auth = useAuthStore()
const record = ref<GroupRecord>()
const activity = ref<GroupActivity>()
const members = ref<any[]>([])
const submitting = ref(false)
const now = ref(Date.now())
let timer = 0

const groupNo = computed(() => String(route.params.groupNo || ''))
const joined = computed(() => members.value.some((item) => String(item.userId) === String(auth.userId)))
const myMember = computed(() => members.value.find((item) => String(item.userId) === String(auth.userId)))
const shareLink = computed(() => `${location.origin}/group/record/${groupNo.value}`)

async function load() {
  record.value = await groupRecord(groupNo.value)
  members.value = (await groupMembers(groupNo.value)) || []
  if (record.value?.groupBuyingId) {
    activity.value = await groupActivity(record.value.groupBuyingId)
  }
}

async function join() {
  if (!auth.userId) {
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  if (!activity.value || submitting.value) return
  submitting.value = true
  try {
    const next = await joinGroup({
      activityId: activity.value.id,
      userId: auth.userId,
      groupNo: groupNo.value,
    })
    const mine = auth.userId ? await myGroup(auth.userId, activity.value.id) : null
    message.success(mine?.orderId ? '你已参加该团，正在打开订单' : '已参团，请提交订单')
    await goGroupFlow(router, {
      groupBuyingId: activity.value.id,
      groupRecordId: next.id,
      groupNo: next.groupNo,
      skuId: activity.value.skuId,
      spuId: activity.value.spuId,
      orderId: mine?.orderId,
    })
  } finally {
    submitting.value = false
  }
}

async function copyShare() {
  await navigator.clipboard.writeText(shareLink.value)
  message.success('邀请链接已复制')
}

onMounted(async () => {
  await load()
  timer = window.setInterval(() => {
    now.value = Date.now()
  }, 1000)
})

onUnmounted(() => clearInterval(timer))
</script>

<template>
  <div class="mx-auto max-w-xl">
    <PageHeader title="拼团详情" extra="把链接发给好友，满员即可支付" />
    <div v-if="record" class="rounded-card bg-white p-6 shadow-card">
      <div class="text-sm text-mute">{{ activity?.activityName || '拼团活动' }}</div>
      <div class="mt-2 text-xl font-semibold">{{ record.groupNo }}</div>
      <div class="mt-3 text-sm">
        {{ groupStatusText(record.status) }} · {{ record.currentNum || 0 }}/{{ record.requireNum || 0 }} 人
      </div>
      <div v-if="record.status === 0" class="mt-1 text-sm text-price">剩余 {{ remainText(record.expireTime, now) }}</div>
      <div class="mt-5 space-y-2 text-sm">
        <div v-for="item in members" :key="item.id" class="rounded-xl bg-slate-50 px-4 py-3">
          成员 {{ item.userId }} {{ item.isLeader === 1 ? '· 团长' : '' }}
        </div>
      </div>
      <button class="mt-5 h-11 w-full rounded-full border" @click="copyShare">复制邀请链接</button>
      <button
        v-if="!joined && record.status === 0"
        :disabled="submitting"
        class="mt-3 h-11 w-full rounded-full bg-ink text-white"
        @click="join"
      >
        {{ submitting ? '处理中...' : '参加这个团' }}
      </button>
      <button
        v-if="joined"
        class="mt-3 h-11 w-full rounded-full bg-brand text-white"
        @click="goGroupFlow(router, {
          groupBuyingId: activity?.id,
          groupRecordId: record.id,
          groupNo: record.groupNo,
          skuId: activity?.skuId,
          spuId: activity?.spuId,
          orderId: myMember?.orderId,
        })"
      >
        {{ myMember?.orderId && String(myMember.orderId) !== '0' ? '查看拼团订单' : '继续提交订单' }}
      </button>
    </div>
  </div>
</template>
