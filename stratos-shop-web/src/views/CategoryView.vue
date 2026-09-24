<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Icon } from '@iconify/vue'
import { NSkeleton } from 'naive-ui'
import { getCategories, getProductsByCategory, listProducts } from '@/api/product'
import type { Category, ProductSpu } from '@/api/types'
import ProductCard from '@/components/ProductCard.vue'
import EmptyState from '@/components/EmptyState.vue'
import { categoryIcon, collectCategoryIds, findCategory, hydrateProducts, sameId } from '@/utils/catalog'
import { productPrice } from '@/utils/format'

const route = useRoute()
const router = useRouter()
const tree = ref<Category[]>([])
const current = ref<number | string>()
const products = ref<ProductSpu[]>([])
const loading = ref(false)
const ready = ref(false)

const currentNode = computed(() => findCategory(tree.value, current.value))
const pathText = computed(() => {
  const path = findPath(tree.value, current.value)
  return path.map((item) => item.categoryName).join(' / ') || '全部分类'
})
const chips = computed(() => {
  const node = currentNode.value
  if (!node) return []
  if (node.children?.length) return node.children
  const parent = findCategory(tree.value, node.parentId)
  return parent?.children || []
})

function findPath(list: Category[] = [], id?: number | string, trail: Category[] = []): Category[] {
  for (const item of list) {
    const next = [...trail, item]
    if (sameId(item.id, id)) return next
    const child = findPath(item.children || [], id, next)
    if (child.length) return child
  }
  return []
}

function go(id?: number | string) {
  router.replace(id != null && id !== '' ? { path: '/category', query: { id: String(id) } } : { path: '/category' })
}

async function loadAll() {
  current.value = undefined
  loading.value = true
  try {
    const page = await listProducts({ pageNum: 1, pageSize: 50, status: 1 })
    products.value = await hydrateProducts(page.records || [])
  } finally {
    loading.value = false
  }
}

async function select(id: number | string) {
  current.value = id
  loading.value = true
  try {
    const node = findCategory(tree.value, id)
    const ids = collectCategoryIds(node)
    if (ids.length <= 1) {
      const page = await getProductsByCategory(id)
      products.value = await hydrateProducts(page.records || [])
      return
    }
    const page = await listProducts({ pageNum: 1, pageSize: 50, status: 1 })
    const idSet = new Set(ids.map(String))
    const records = (page.records || []).filter((item) => !item.categoryId || idSet.has(String(item.categoryId)))
    products.value = await hydrateProducts(records.length ? records : page.records || [])
  } finally {
    loading.value = false
  }
}

async function syncFromRoute() {
  const id = String(route.query.id || '')
  const node = findCategory(tree.value, id)
  if (node) {
    await select(node.id)
    return
  }
  await loadAll()
}

onMounted(async () => {
  tree.value = (await getCategories()) || []
  await syncFromRoute()
  ready.value = true
})

watch(
  () => route.query.id,
  () => {
    if (ready.value) syncFromRoute()
  },
)
</script>

<template>
  <div class="grid items-start gap-6 md:grid-cols-[240px_minmax(0,1fr)]">
    <aside class="overflow-hidden rounded-card bg-white shadow-card">
      <div class="border-b border-slate-100 px-5 py-4">
        <div class="text-sm font-semibold">全部分类</div>
        <p class="mt-1 text-xs text-mute">点一级类目，或继续筛选下级</p>
      </div>
      <button
        class="flex w-full items-center gap-2 px-5 py-3 text-left text-sm transition"
        :class="!current ? 'bg-sky-50 font-medium text-sky-600' : 'text-ink hover:bg-page'"
        @click="go()"
      >
        <Icon icon="solar:widget-4-bold-duotone" width="16" />
        全部商品
      </button>
      <button
        v-for="(item, index) in tree"
        :key="item.id"
        class="flex w-full items-center gap-2 px-5 py-3 text-left text-sm transition"
        :class="sameId(current, item.id) || findPath(tree, current).some((row) => sameId(row.id, item.id)) ? 'bg-sky-50 font-medium text-sky-600' : 'text-ink hover:bg-page'"
        @click="go(item.id)"
      >
        <Icon :icon="categoryIcon(index)" width="16" />
        {{ item.categoryName }}
      </button>
    </aside>

    <div class="space-y-4">
      <section class="rounded-card bg-white p-5 shadow-card">
        <div class="flex flex-wrap items-end justify-between gap-3">
          <div>
            <p class="text-sm text-sky-600">分类浏览</p>
            <h1 class="mt-1 text-2xl font-semibold">{{ currentNode?.categoryName || '全部商品' }}</h1>
            <p class="mt-1 text-sm text-mute">当前：{{ pathText }} · {{ products.length }} 件</p>
          </div>
        </div>
        <div v-if="chips.length" class="mt-4 flex flex-wrap gap-2">
          <button
            v-for="item in chips"
            :key="item.id"
            class="rounded-full px-3 py-1.5 text-sm transition"
            :class="sameId(current, item.id) ? 'bg-ink text-white' : 'bg-page text-mute hover:text-ink'"
            @click="go(item.id)"
          >
            {{ item.categoryName }}
          </button>
        </div>
      </section>

      <div v-if="loading" class="grid grid-cols-3 gap-4">
        <NSkeleton v-for="item in 6" :key="item" height="260px" class="rounded-card" />
      </div>
      <section v-else-if="products.length" class="grid grid-cols-3 gap-4">
        <ProductCard
          v-for="item in products"
          :key="item.id"
          :id="item.id"
          :title="item.title || item.spuName"
          :image="item.mainImage"
          :price="productPrice(item)"
          :point="item.sellingPoint"
        />
      </section>
      <EmptyState
        v-else
        title="该分类暂无商品"
        desc="换一个类目，或回到全部商品看看"
        icon="solar:widget-4-bold-duotone"
      />
    </div>
  </div>
</template>
