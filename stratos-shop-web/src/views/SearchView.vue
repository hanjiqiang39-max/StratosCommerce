<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import { listProducts } from '@/api/product'
import { hotWords, searchProducts } from '@/api/search'
import type { ProductSpu } from '@/api/types'
import ProductCard from '@/components/ProductCard.vue'
import EmptyState from '@/components/EmptyState.vue'
import PageHeader from '@/components/PageHeader.vue'
import { hydrateProducts } from '@/utils/catalog'
import { productPrice } from '@/utils/format'

const route = useRoute()
const keyword = computed(() => String(route.query.keyword || ''))
const products = ref<ProductSpu[]>([])
const hots = ref<string[]>([])
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    if (keyword.value) {
      try {
        const data = await searchProducts({ keyword: keyword.value, page: 1, size: 20 })
        const list = (data as any).records || (data as any).list || (data as any).items || (data as any).content || []
        products.value = await hydrateProducts(list)
      } catch {
        const page = await listProducts({ keyword: keyword.value, pageNum: 1, pageSize: 20, status: 1 })
        products.value = await hydrateProducts(page.records || [])
      }
    } else {
      hots.value = (await hotWords()) || []
      products.value = []
    }
  } finally {
    loading.value = false
  }
}

watch(keyword, load, { immediate: true })
</script>

<template>
  <div>
    <PageHeader :title="keyword ? `搜索“${keyword}”` : '搜索商品'" extra="支持标题和商品名" />
    <div v-if="!keyword && hots.length" class="mb-5 flex flex-wrap gap-2">
      <RouterLink v-for="word in hots" :key="word" :to="{ path: '/search', query: { keyword: word } }" class="rounded-full bg-white px-3 py-1 text-sm">
        {{ word }}
      </RouterLink>
    </div>
    <div v-if="products.length" class="grid grid-cols-4 gap-4">
      <ProductCard
        v-for="item in products"
        :key="item.id"
        :id="item.id"
        :title="item.title || item.spuName || (item as any).name"
        :image="item.mainImage || (item as any).image"
        :price="productPrice(item) || (item as any).price"
      />
    </div>
    <EmptyState v-else-if="keyword && !loading" title="没有找到相关商品" />
  </div>
</template>
