<script setup lang="ts">
import { RouterLink } from 'vue-router'
import SafeImage from './SafeImage.vue'
import PriceText from './PriceText.vue'
import { productPrice } from '@/utils/format'

const props = defineProps<{
  id: number
  title?: string
  image?: string
  price?: number | string | null
  point?: string
}>()
</script>

<template>
  <RouterLink :to="`/product/${id}`" class="group block overflow-hidden rounded-card bg-white shadow-card transition hover:-translate-y-0.5 hover:shadow-lg">
    <div class="aspect-square overflow-hidden bg-slate-100">
      <SafeImage :src="image" :alt="title" class="h-full w-full object-cover transition duration-300 group-hover:scale-105" />
    </div>
    <div class="space-y-1.5 p-4">
      <div class="line-clamp-2 min-h-10 text-sm leading-5 text-ink">{{ title }}</div>
      <div v-if="point" class="line-clamp-1 text-xs text-mute">{{ point }}</div>
      <div class="flex items-end justify-between">
        <PriceText :value="productPrice({ minPrice: props.price, price: props.price })" />
        <span class="text-xs text-mute">立即购买</span>
      </div>
    </div>
  </RouterLink>
</template>
