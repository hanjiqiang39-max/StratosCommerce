<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { deleteNotification, listNotifications } from '@/api/content'
import { useAuthStore } from '@/stores/auth'
import EmptyState from '@/components/EmptyState.vue'
import PageHeader from '@/components/PageHeader.vue'

const auth = useAuthStore()
const list = ref<Array<{ id: number; title: string; content: string }>>([])

async function load() {
  if (auth.userId) list.value = (await listNotifications(auth.userId)) || []
}

onMounted(load)
</script>

<template>
  <div>
    <PageHeader title="消息通知" />
    <EmptyState v-if="!list.length" title="暂无通知" />
    <div class="space-y-3">
      <div v-for="item in list" :key="item.id" class="rounded-card bg-white p-5">
        <div class="font-medium">{{ item.title }}</div>
        <div class="mt-1 text-sm text-mute">{{ item.content }}</div>
        <button class="mt-3 text-sm text-mute" @click="auth.userId && deleteNotification(item.id, auth.userId).then(load)">删除</button>
      </div>
    </div>
  </div>
</template>
