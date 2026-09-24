<script setup lang="ts">
defineProps<{ title: string; extra?: string }>()
</script>

<template>
  <div class="space-y-5">
    <div class="flex items-end justify-between gap-4">
      <div>
        <h1 class="text-2xl font-semibold tracking-tight">{{ title }}</h1>
        <p v-if="extra" class="mt-1 text-sm text-mute">{{ extra }}</p>
      </div>
      <slot name="action" />
    </div>
    <slot />
  </div>
</template>
