<script setup lang="ts">
defineProps<{
  label: string
  hint?: string
  required?: boolean
  error?: string
}>()
</script>

<template>
  <label class="block space-y-1.5">
    <span class="text-sm font-medium text-slate-800">
      {{ label }}
      <span v-if="required" class="text-rose-500">*</span>
    </span>
    <slot />
    <p v-if="error" class="text-xs leading-5 text-rose-500">{{ error }}</p>
    <p v-else-if="hint" class="text-xs leading-5 text-mute">{{ hint }}</p>
  </label>
</template>
