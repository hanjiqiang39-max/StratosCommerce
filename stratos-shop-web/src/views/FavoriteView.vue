<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { listFavorites, removeFavorite } from '@/api/favorite'
import { getProduct } from '@/api/product'
import { useAuthStore } from '@/stores/auth'
import EmptyState from '@/components/EmptyState.vue'
import PageHeader from '@/components/PageHeader.vue'
import PriceText from '@/components/PriceText.vue'
import SafeImage from '@/components/SafeImage.vue'
import { productPrice } from '@/utils/format'

const auth = useAuthStore()
const list = ref<Array<{ id: number; targetId: number; title?: string; image?: string; price?: number }>>([])

async function load() {
  if (!auth.userId) return
  const rows = (await listFavorites(auth.userId)) || []
  list.value = await Promise.all(
    rows.map(async (item) => {
      try {
        const product = await getProduct(item.targetId)
        return {
          ...item,
          title: product.title,
          image: product.mainImage,
          price: productPrice(product),
        }
      } catch {
        return item
      }
    }),
  )
}

onMounted(load)
</script>

<template>
  <div>
    <PageHeader title="我的收藏" />
    <EmptyState v-if="!list.length" title="还没有收藏" />
    <div class="grid grid-cols-2 gap-4">
      <div v-for="item in list" :key="item.id" class="flex items-center gap-4 rounded-card bg-white p-4">
        <div class="h-16 w-16 overflow-hidden rounded-xl bg-slate-100">
          <SafeImage :src="item.image" class="h-full w-full object-cover" />
        </div>
        <div class="min-w-0 flex-1">
          <RouterLink :to="`/product/${item.targetId}`" class="truncate font-medium">{{ item.title || `商品 ${item.targetId}` }}</RouterLink>
          <PriceText v-if="item.price" :value="item.price" size="sm" class="mt-1 block" />
        </div>
        <button class="text-sm text-mute" @click="auth.userId && removeFavorite(auth.userId, item.targetId).then(load)">取消</button>
      </div>
    </div>
  </div>
</template>
