<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { Icon } from '@iconify/vue'
import { NSkeleton } from 'naive-ui'
import { listBanners, listNotices } from '@/api/content'
import { getCategories, getProduct, listProducts } from '@/api/product'
import { listGroups, listSeckill } from '@/api/promotion'
import type { Banner, Category, GroupActivity, Notice, ProductDetail, ProductSpu, SeckillActivity } from '@/api/types'
import ProductCard from '@/components/ProductCard.vue'
import EmptyState from '@/components/EmptyState.vue'
import SafeImage from '@/components/SafeImage.vue'
import { categoryIcon, flattenCategories, hydrateProducts } from '@/utils/catalog'
import { FALLBACK_BANNER, activityPhase, imageUrl, money, productPrice, remainText, stockPercent } from '@/utils/format'

const loading = ref(true)
const banners = ref<Banner[]>([])
const notices = ref<Notice[]>([])
const products = ref<ProductSpu[]>([])
const categories = ref<Category[]>([])
const seckills = ref<SeckillActivity[]>([])
const groups = ref<GroupActivity[]>([])
const extras = ref<Record<string, ProductDetail>>({})
const now = ref(Date.now())
let timer = 0

const hero = computed(() => banners.value[0])
const heroPhoto = computed(() => {
  const url = hero.value ? imageUrl(hero.value.imageUrl) : ''
  if (!url || url === FALLBACK_BANNER || url.endsWith('.svg')) return ''
  return url
})
const shortcuts = computed(() => flattenCategories(categories.value).filter((item) => item.depth === 0).slice(0, 8))
const liveSeckills = computed(() =>
  seckills.value.filter((item) => activityPhase(item, now.value) === '进行中').slice(0, 4),
)
const showcaseSeckills = computed(() => (liveSeckills.value.length ? liveSeckills.value : seckills.value.slice(0, 4)))

async function hydrate(items: Array<{ spuId?: number | string }>) {
  await Promise.all(
    items.map(async (item) => {
      if (!item.spuId) return
      const key = String(item.spuId)
      if (extras.value[key]) return
      try {
        extras.value[key] = await getProduct(item.spuId)
      } catch {
        /* keep homepage usable */
      }
    }),
  )
}

onMounted(async () => {
  try {
    const [bannerRes, noticeRes, productRes, seckillRes, groupRes, categoryRes] = await Promise.allSettled([
      listBanners(),
      listNotices(),
      listProducts({ pageNum: 1, pageSize: 12, status: 1 }),
      listSeckill(),
      listGroups(),
      getCategories(),
    ])
    if (bannerRes.status === 'fulfilled') banners.value = bannerRes.value || []
    if (noticeRes.status === 'fulfilled') notices.value = noticeRes.value || []
    if (productRes.status === 'fulfilled') products.value = await hydrateProducts(productRes.value?.records || [])
    if (seckillRes.status === 'fulfilled') seckills.value = seckillRes.value || []
    if (groupRes.status === 'fulfilled') groups.value = groupRes.value || []
    if (categoryRes.status === 'fulfilled') categories.value = categoryRes.value || []
    await hydrate([...seckills.value, ...groups.value])
  } finally {
    loading.value = false
    timer = window.setInterval(() => {
      now.value = Date.now()
    }, 1000)
  }
})

onUnmounted(() => clearInterval(timer))
</script>

<template>
  <div class="space-y-8">
    <div v-if="loading" class="space-y-6">
      <NSkeleton height="320px" class="rounded-card" />
      <div class="grid grid-cols-4 gap-3">
        <NSkeleton v-for="item in 4" :key="item" height="84px" class="rounded-card" />
      </div>
    </div>

    <template v-else>
      <section class="relative overflow-hidden rounded-card bg-gradient-to-br from-sky-100 via-white to-cyan-50 shadow-card">
        <SafeImage v-if="heroPhoto" class="absolute inset-0 h-full w-full object-cover" :src="heroPhoto" alt="" />
        <div class="pointer-events-none absolute -right-16 -top-16 h-64 w-64 rounded-full bg-sky-200/50" />
        <div class="pointer-events-none absolute bottom-0 right-20 h-44 w-44 rounded-full bg-cyan-100/80" />
        <div class="relative z-10 grid min-h-[320px] items-center gap-8 px-10 py-10 md:grid-cols-[1fr_240px]">
          <div class="max-w-xl">
            <div class="inline-flex items-center gap-2 rounded-full bg-white/80 px-3 py-1 text-xs font-medium text-sky-600 backdrop-blur">
              <Icon icon="solar:star-bold" width="14" />
              Stratos 精选
            </div>
            <h1 class="mt-4 text-4xl font-semibold tracking-tight">{{ hero?.title || '干净好买的精选商城' }}</h1>
            <p class="mt-3 text-base leading-7 text-mute">浏览、加购、领券、秒杀拼团，再到支付宝沙箱支付，一条链路走完。</p>
            <div class="mt-6 flex flex-wrap gap-3">
              <RouterLink :to="hero?.linkUrl || '/category'" class="inline-flex h-11 items-center rounded-full bg-ink px-6 text-sm text-white">
                去逛逛
              </RouterLink>
              <RouterLink to="/seckill" class="inline-flex h-11 items-center rounded-full border border-white/70 bg-white/80 px-6 text-sm backdrop-blur hover:border-rose-200 hover:text-price">
                限时秒杀
              </RouterLink>
              <RouterLink to="/coupons" class="inline-flex h-11 items-center rounded-full border border-white/70 bg-white/80 px-6 text-sm backdrop-blur hover:border-sky-200 hover:text-sky-600">
                领优惠券
              </RouterLink>
            </div>
          </div>
          <div class="grid gap-3">
            <RouterLink to="/seckill" class="rounded-2xl bg-white/80 px-4 py-3 backdrop-blur transition hover:-translate-y-0.5">
              <div class="text-xs text-price">进行中的秒杀</div>
              <div class="mt-1 text-2xl font-semibold">{{ liveSeckills.length || seckills.length }}</div>
            </RouterLink>
            <div class="grid grid-cols-2 gap-3">
              <RouterLink to="/group" class="rounded-2xl bg-white/80 px-4 py-3 backdrop-blur">
                <div class="text-xs text-sky-600">拼团</div>
                <div class="mt-1 text-xl font-semibold">{{ groups.length }}</div>
              </RouterLink>
              <RouterLink to="/category" class="rounded-2xl bg-white/80 px-4 py-3 backdrop-blur">
                <div class="text-xs text-mute">上架商品</div>
                <div class="mt-1 text-xl font-semibold">{{ products.length }}</div>
              </RouterLink>
            </div>
          </div>
        </div>
      </section>

      <div v-if="notices.length" class="flex items-center gap-3 rounded-card bg-white px-5 py-3 shadow-card">
        <span class="inline-flex items-center gap-1 rounded-full bg-sky-50 px-2.5 py-1 text-xs font-medium text-sky-600">
          <Icon icon="solar:bell-bing-bold" width="14" />
          公告
        </span>
        <RouterLink to="/notices" class="truncate text-sm text-mute hover:text-ink">{{ notices[0].title }}</RouterLink>
        <RouterLink to="/notices" class="ml-auto shrink-0 text-xs text-brand">全部</RouterLink>
      </div>

      <section v-if="shortcuts.length" class="grid grid-cols-4 gap-3">
        <RouterLink
          v-for="(item, index) in shortcuts"
          :key="item.id"
          :to="`/category?id=${item.id}`"
          class="group rounded-card bg-white px-4 py-5 text-center shadow-card transition hover:-translate-y-0.5 hover:shadow-lg"
        >
          <div class="mx-auto flex h-11 w-11 items-center justify-center rounded-2xl bg-sky-50 text-sky-500 transition group-hover:bg-sky-100">
            <Icon :icon="categoryIcon(index)" width="22" />
          </div>
          <div class="mt-3 text-sm font-medium">{{ item.categoryName }}</div>
          <div class="mt-1 text-xs text-mute">精选类目</div>
        </RouterLink>
      </section>

      <section v-if="showcaseSeckills.length" class="overflow-hidden rounded-card bg-gradient-to-br from-rose-50 via-white to-white p-6 shadow-card">
        <div class="mb-5 flex items-end justify-between">
          <div>
            <div class="flex items-center gap-2">
              <span class="h-5 w-1 rounded-full bg-price" />
              <h2 class="text-xl font-semibold">限时秒杀</h2>
            </div>
            <p class="mt-1 text-sm text-mute">库存有限，抢到后请尽快下单支付</p>
          </div>
          <RouterLink to="/seckill" class="text-sm text-price hover:underline">全部秒杀</RouterLink>
        </div>
        <div class="grid grid-cols-4 gap-4">
          <RouterLink
            v-for="item in showcaseSeckills"
            :key="item.id"
            :to="`/seckill/${item.id}`"
            class="overflow-hidden rounded-2xl bg-white shadow-sm transition hover:-translate-y-0.5"
          >
            <div class="relative aspect-[4/3] bg-slate-100">
              <SafeImage :src="extras[String(item.spuId)]?.mainImage" class="h-full w-full object-cover" />
              <span class="absolute left-2 top-2 rounded-full bg-price px-2 py-0.5 text-[11px] text-white">
                {{ activityPhase(item, now) }}
              </span>
            </div>
            <div class="p-3">
              <div class="line-clamp-2 min-h-10 text-sm">{{ extras[String(item.spuId)]?.title || item.activityName || '秒杀活动' }}</div>
              <div class="mt-2 flex items-end justify-between">
                <div>
                  <div class="text-lg font-semibold text-price">{{ money(item.seckillPrice) }}</div>
                  <div class="text-[11px] text-mute line-through">{{ money(item.originalPrice) }}</div>
                </div>
                <div class="text-[11px] text-mute">{{ remainText(item.endTime, now) }}</div>
              </div>
              <div class="mt-2 h-1.5 overflow-hidden rounded-full bg-rose-50">
                <div class="h-full rounded-full bg-price" :style="{ width: `${stockPercent(item.soldCount, item.seckillStock)}%` }" />
              </div>
            </div>
          </RouterLink>
        </div>
      </section>

      <section class="grid grid-cols-2 gap-4">
        <RouterLink to="/group" class="group relative overflow-hidden rounded-card bg-gradient-to-br from-sky-50 to-white p-7 shadow-card">
          <Icon icon="solar:users-group-rounded-bold-duotone" width="72" class="absolute -right-2 -top-1 text-sky-100 transition group-hover:scale-110" />
          <div class="relative">
            <div class="text-sm text-sky-600">拼团</div>
            <div class="mt-2 text-2xl font-medium">和朋友一起买更优惠</div>
            <p class="mt-2 text-sm text-mute">{{ groups.length ? `当前 ${groups.length} 场活动可参团` : '满员成团后再支付' }}</p>
            <div class="mt-4 text-sm font-medium text-brand">去参团 →</div>
          </div>
        </RouterLink>
        <RouterLink to="/coupons" class="group relative overflow-hidden rounded-card bg-gradient-to-br from-rose-50 to-white p-7 shadow-card">
          <Icon icon="solar:ticket-bold-duotone" width="72" class="absolute -right-2 -top-1 text-rose-100 transition group-hover:scale-110" />
          <div class="relative">
            <div class="text-sm text-price">优惠券</div>
            <div class="mt-2 text-2xl font-medium">领券再下单更划算</div>
            <p class="mt-2 text-sm text-mute">结算页按订单金额自动试算抵扣</p>
            <div class="mt-4 text-sm font-medium text-price">去领券 →</div>
          </div>
        </RouterLink>
      </section>

      <section class="space-y-4">
        <div class="flex items-end justify-between">
          <div class="flex items-center gap-2">
            <span class="h-5 w-1 rounded-full bg-sky-400" />
            <h2 class="text-xl font-semibold">精选商品</h2>
          </div>
          <RouterLink to="/category" class="text-sm text-brand hover:underline">查看全部分类</RouterLink>
        </div>
        <div v-if="products.length" class="grid grid-cols-4 gap-4">
          <ProductCard
            v-for="item in products"
            :key="item.id"
            :id="item.id"
            :title="item.title || item.spuName"
            :image="item.mainImage"
            :price="productPrice(item)"
            :point="item.sellingPoint"
          />
        </div>
        <EmptyState v-else title="还没有上架商品" desc="后台发布商品后会显示在这里" icon="solar:shop-2-bold-duotone" />
      </section>
    </template>
  </div>
</template>
