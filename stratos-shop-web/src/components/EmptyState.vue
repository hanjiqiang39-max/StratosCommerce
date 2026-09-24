<script setup lang="ts">
import { Icon } from '@iconify/vue'

defineProps<{ title?: string; desc?: string; icon?: string }>()
</script>

<template>
  <div class="flex flex-col items-center justify-center rounded-card bg-white px-6 py-16 text-center shadow-card">
    <div class="mb-4 flex h-14 w-14 items-center justify-center rounded-3xl bg-sky-50 text-sky-500">
      <Icon :icon="icon || 'solar:box-bold-duotone'" width="28" />
    </div>
    <div class="text-base font-medium text-ink">{{ title || '暂无内容' }}</div>
    <div v-if="desc" class="mt-1 text-sm text-mute">{{ desc }}</div>
    <div v-if="$slots.default" class="mt-5">
      <slot />
    </div>
  </div>
</template>
