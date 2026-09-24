<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { listNotices } from '@/api/content'
import type { Notice } from '@/api/types'
import PageHeader from '@/components/PageHeader.vue'

const notices = ref<Notice[]>([])
onMounted(async () => {
  notices.value = (await listNotices()) || []
})
</script>

<template>
  <div>
    <PageHeader title="商城公告" />
    <div class="space-y-3">
      <article v-for="item in notices" :key="item.id" class="rounded-card bg-white p-5">
        <h2 class="font-medium">{{ item.title }}</h2>
        <p class="mt-2 text-sm text-mute">{{ item.content }}</p>
      </article>
    </div>
  </div>
</template>
