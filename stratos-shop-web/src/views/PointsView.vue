<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { getPoints } from '@/api/user'
import type { PointsAccount } from '@/api/types'
import { useAuthStore } from '@/stores/auth'
import PageHeader from '@/components/PageHeader.vue'

const auth = useAuthStore()
const account = ref<PointsAccount>()

onMounted(async () => {
  if (auth.userId) account.value = await getPoints(auth.userId)
})
</script>

<template>
  <div>
    <PageHeader title="我的积分" extra="下单时可按 100 积分 = 1 元抵扣" />
    <div class="grid grid-cols-3 gap-4">
      <div class="rounded-card bg-white p-6">
        <div class="text-mute">可用积分</div>
        <div class="mt-2 text-3xl font-semibold">{{ account?.availablePoints || 0 }}</div>
      </div>
      <div class="rounded-card bg-white p-6">
        <div class="text-mute">累计积分</div>
        <div class="mt-2 text-3xl font-semibold">{{ account?.totalPoints || 0 }}</div>
      </div>
      <div class="rounded-card bg-white p-6">
        <div class="text-mute">已用积分</div>
        <div class="mt-2 text-3xl font-semibold">{{ account?.usedPoints || 0 }}</div>
      </div>
    </div>
  </div>
</template>
