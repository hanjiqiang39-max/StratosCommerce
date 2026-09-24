<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { Icon } from '@iconify/vue'
import { NCheckbox, NSkeleton, useMessage } from 'naive-ui'
import { getProduct, getShop, listProducts } from '@/api/product'
import type { CartItem, ProductDetail, ProductSpu } from '@/api/types'
import ProductCard from '@/components/ProductCard.vue'
import PriceText from '@/components/PriceText.vue'
import SafeImage from '@/components/SafeImage.vue'
import { useCartStore } from '@/stores/cart'
import { hydrateProducts } from '@/utils/catalog'
import { money, productPrice, skuLabel } from '@/utils/format'

const router = useRouter()
const cart = useCartStore()
const message = useMessage()
const extras = ref<Record<string, ProductDetail>>({})
const shopNames = ref<Record<string, string>>({})
const recommends = ref<ProductSpu[]>([])
const loading = ref(true)
const busy = ref(false)

const allSelected = computed(() => cart.items.length > 0 && cart.items.every((item) => item.selected === 1))
const selectedItems = computed(() => cart.items.filter((item) => item.selected === 1))

function extraOf(item: CartItem) {
  return extras.value[String(item.spuId)]
}

function skuOf(item: CartItem) {
  return extraOf(item)?.skuList?.find((sku) => String(sku.id) === String(item.skuId))
}

function titleOf(item: CartItem) {
  const detail = extraOf(item)
  return detail?.title || detail?.subTitle || `商品 ${item.spuId}`
}

function specOf(item: CartItem) {
  return skuLabel({
    skuName: skuOf(item)?.skuName,
    title: extraOf(item)?.title,
  })
}

function unitPrice(item: CartItem) {
  const sku = skuOf(item)
  const detail = extraOf(item)
  return productPrice({ price: sku?.price, minPrice: detail?.minPrice })
}

function originPrice(item: CartItem) {
  return Number(skuOf(item)?.originalPrice || 0)
}

function lineTotal(item: CartItem) {
  return unitPrice(item) * item.quantity
}

const shopGroups = computed(() => {
  const groups: { key: string; name: string; items: CartItem[] }[] = []
  const index = new Map<string, number>()
  for (const item of cart.items) {
    const detail = extraOf(item)
    const shopId = detail?.shopId ? String(detail.shopId) : ''
    const name = (shopId && shopNames.value[shopId]) || detail?.shopName || 'Stratos 自营'
    const key = shopId || name
    if (!index.has(key)) {
      index.set(key, groups.length)
      groups.push({ key, name, items: [] })
    }
    groups[index.get(key)!].items.push(item)
  }
  return groups
})

const selectedQty = computed(() => selectedItems.value.reduce((sum, item) => sum + item.quantity, 0))
const total = computed(() => selectedItems.value.reduce((sum, item) => sum + lineTotal(item), 0))

async function loadExtras() {
  const ids = [...new Set(cart.items.map((item) => item.spuId))]
  await Promise.all(
    ids.map(async (id) => {
      try {
        extras.value[String(id)] = await getProduct(id)
      } catch {
        /* keep cart usable even if one product is gone */
      }
    }),
  )
  const shopIds = [...new Set(Object.values(extras.value).map((item) => item.shopId).filter(Boolean).map(String))]
  await Promise.all(
    shopIds.map(async (id) => {
      if (shopNames.value[id]) return
      try {
        const shop = await getShop(id)
        shopNames.value[id] = shop?.shopName || ''
      } catch {
        shopNames.value[id] = ''
      }
    }),
  )
}

async function loadRecommends() {
  try {
    const page = await listProducts({ pageNum: 1, pageSize: 8, status: 1 })
    recommends.value = await hydrateProducts(page?.records || [])
  } catch {
    recommends.value = []
  }
}

async function removeSelected() {
  if (!selectedItems.value.length) return
  if (!window.confirm(`确定删除已选的 ${selectedItems.value.length} 件商品？`)) return
  busy.value = true
  try {
    await cart.removeMany(selectedItems.value.map((item) => item.id))
    message.success('已删除选中商品')
  } finally {
    busy.value = false
  }
}

async function removeOne(item: CartItem) {
  if (!window.confirm('确定从购物车移除这件商品？')) return
  await cart.remove(item.id)
}

function checkout() {
  if (!selectedItems.value.length) {
    message.warning('请先选择要结算的商品')
    return
  }
  router.push('/checkout')
}

onMounted(async () => {
  try {
    await cart.refresh()
    await Promise.all([loadExtras(), loadRecommends()])
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="space-y-6">
    <div class="flex items-end justify-between">
      <div>
        <p class="text-sm text-sky-600">购物车</p>
        <h1 class="mt-1 text-2xl font-semibold tracking-tight">确认要买的商品</h1>
        <p class="mt-1 text-sm text-mute">
          {{ cart.items.length ? `共 ${cart.items.length} 件，已选 ${selectedItems.length} 件` : '还没有加入商品' }}
        </p>
      </div>
      <RouterLink to="/" class="inline-flex h-10 items-center gap-1 rounded-full border border-slate-200 bg-white px-4 text-sm hover:border-sky-200 hover:text-sky-600">
        <Icon icon="solar:arrow-left-linear" width="16" />
        继续逛逛
      </RouterLink>
    </div>

    <div v-if="loading" class="grid grid-cols-[1fr_300px] gap-6">
      <NSkeleton height="280px" class="rounded-card" />
      <NSkeleton height="220px" class="rounded-card" />
    </div>

    <div v-else-if="!cart.items.length" class="overflow-hidden rounded-card bg-white shadow-card">
      <div class="flex flex-col items-center px-6 py-16 text-center">
        <div class="flex h-16 w-16 items-center justify-center rounded-3xl bg-sky-50 text-sky-500">
          <Icon icon="solar:bag-3-bold-duotone" width="32" />
        </div>
        <div class="mt-4 text-lg font-semibold">购物车还是空的</div>
        <p class="mt-1 text-sm text-mute">去首页挑一件喜欢的，或者先领一张优惠券</p>
        <div class="mt-5 flex gap-3">
          <RouterLink to="/" class="inline-flex h-10 items-center rounded-full bg-ink px-5 text-sm text-white">去逛逛</RouterLink>
          <RouterLink to="/coupons" class="inline-flex h-10 items-center rounded-full border border-slate-200 px-5 text-sm">领优惠券</RouterLink>
        </div>
      </div>
    </div>

    <div v-else class="grid items-start gap-6 md:grid-cols-[minmax(0,1fr)_280px]">
      <div class="space-y-4">
        <div class="flex items-center justify-between rounded-card bg-white px-5 py-3 shadow-card">
          <label class="flex items-center gap-2 text-sm">
            <NCheckbox :checked="allSelected" @update:checked="(v: boolean) => cart.toggleAll(v ? 1 : 0)" />
            全选
            <span class="text-mute">已选 {{ selectedItems.length }} 件</span>
          </label>
          <button
            class="text-sm text-mute transition hover:text-price disabled:opacity-40"
            :disabled="!selectedItems.length || busy"
            @click="removeSelected"
          >
            删除已选
          </button>
        </div>

        <section v-for="group in shopGroups" :key="group.key" class="overflow-hidden rounded-card bg-white shadow-card">
          <div class="flex items-center gap-2 border-b border-slate-100 px-5 py-3 text-sm">
            <Icon icon="solar:shop-2-bold-duotone" width="18" class="text-sky-500" />
            <span class="font-medium">{{ group.name }}</span>
          </div>
          <div class="divide-y divide-slate-100">
            <article v-for="item in group.items" :key="item.id" class="flex items-center gap-4 px-5 py-4">
              <NCheckbox :checked="item.selected === 1" @update:checked="(v: boolean) => cart.toggle(item.id, v ? 1 : 0)" />
              <RouterLink :to="`/product/${item.spuId}`" class="h-24 w-24 shrink-0 overflow-hidden rounded-2xl bg-slate-100">
                <SafeImage :src="skuOf(item)?.skuImage || extraOf(item)?.mainImage" class="h-full w-full object-cover" />
              </RouterLink>
              <div class="min-w-0 flex-1">
                <RouterLink :to="`/product/${item.spuId}`" class="line-clamp-2 font-medium leading-6 hover:text-sky-600">
                  {{ titleOf(item) }}
                </RouterLink>
                <div class="mt-2 inline-flex rounded-full bg-slate-50 px-2.5 py-1 text-xs text-mute">
                  {{ specOf(item) }}
                </div>
                <div class="mt-3 flex items-end gap-2">
                  <PriceText :value="unitPrice(item)" size="sm" />
                  <span v-if="originPrice(item) > unitPrice(item)" class="text-xs text-mute line-through">
                    {{ money(originPrice(item)) }}
                  </span>
                </div>
              </div>
              <div class="flex flex-col items-end gap-3">
                <div class="inline-flex items-center rounded-full border border-slate-200 bg-page">
                  <button
                    class="flex h-8 w-8 items-center justify-center text-mute hover:text-ink disabled:opacity-30"
                    :disabled="item.quantity <= 1"
                    @click="cart.changeQty(item.id, Math.max(1, item.quantity - 1))"
                  >
                    −
                  </button>
                  <span class="w-8 text-center text-sm tabular-nums">{{ item.quantity }}</span>
                  <button
                    class="flex h-8 w-8 items-center justify-center text-mute hover:text-ink"
                    @click="cart.changeQty(item.id, item.quantity + 1)"
                  >
                    +
                  </button>
                </div>
                <div class="text-right">
                  <div class="text-xs text-mute">小计</div>
                  <div class="font-semibold text-price">{{ money(lineTotal(item)) }}</div>
                </div>
                <button class="text-xs text-mute hover:text-price" @click="removeOne(item)">移除</button>
              </div>
            </article>
          </div>
        </section>
      </div>

      <aside class="sticky top-36 overflow-hidden rounded-card bg-white shadow-card">
        <div class="bg-gradient-to-br from-sky-50 to-white px-5 py-4">
          <div class="text-sm font-medium">结算明细</div>
          <p class="mt-1 text-xs text-mute">支持优惠券和积分，下一步再选</p>
        </div>
        <div class="space-y-3 px-5 py-4 text-sm">
          <div class="flex justify-between">
            <span class="text-mute">已选件数</span>
            <span>{{ selectedQty }} 件</span>
          </div>
          <div class="flex justify-between">
            <span class="text-mute">商品合计</span>
            <span>{{ money(total) }}</span>
          </div>
          <div class="flex justify-between">
            <span class="text-mute">运费</span>
            <span class="text-emerald-600">免运费</span>
          </div>
        </div>
        <div class="border-t border-slate-100 px-5 py-4">
          <div class="flex items-end justify-between">
            <span class="text-sm text-mute">应付金额</span>
            <span class="text-2xl font-semibold text-price">{{ money(total) }}</span>
          </div>
          <button
            class="mt-4 h-11 w-full rounded-full text-sm font-medium text-white transition disabled:cursor-not-allowed disabled:bg-slate-200"
            :class="selectedItems.length ? 'bg-ink hover:bg-slate-800' : 'bg-slate-200'"
            :disabled="!selectedItems.length"
            @click="checkout"
          >
            {{ selectedItems.length ? `去结算（${selectedItems.length}）` : '请先选择商品' }}
          </button>
          <RouterLink to="/coupons" class="mt-3 block text-center text-xs text-sky-600 hover:underline">
            去领优惠券，下单更划算
          </RouterLink>
        </div>
      </aside>
    </div>

    <section v-if="recommends.length" class="space-y-4">
      <div class="flex items-center justify-between">
        <h2 class="text-lg font-semibold">{{ cart.items.length ? '猜你还想买' : '先看看这些' }}</h2>
        <RouterLink to="/category" class="text-sm text-brand">更多商品</RouterLink>
      </div>
      <div class="grid grid-cols-2 gap-4 md:grid-cols-4">
        <ProductCard
          v-for="item in recommends.slice(0, 4)"
          :id="item.id"
          :key="item.id"
          :title="item.title || item.spuName"
          :image="item.mainImage"
          :price="item.minPrice || item.price"
          :point="item.sellingPoint"
        />
      </div>
    </section>
  </div>
</template>
