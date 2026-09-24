<script setup lang="ts">
import { money } from '@/utils/format'
defineProps<{ value?: number | string | null; size?: 'sm' | 'md' | 'lg' }>()
</script>

<template>
  <span class="price" :class="{ 'text-lg': size === 'md' || !size, 'text-xl': size === 'lg', 'text-sm': size === 'sm' }">
    {{ money(value) }}
  </span>
</template>
