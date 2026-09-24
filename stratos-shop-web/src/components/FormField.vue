<script setup lang="ts">
defineProps<{
  label: string
  hint?: string
  required?: boolean
}>()
</script>

<template>
  <label class="block space-y-1.5">
    <span class="text-sm font-medium text-ink">
      {{ label }}
      <span v-if="required" class="text-price">*</span>
    </span>
    <slot />
    <p v-if="hint" class="text-xs leading-5 text-mute">{{ hint }}</p>
  </label>
</template>
