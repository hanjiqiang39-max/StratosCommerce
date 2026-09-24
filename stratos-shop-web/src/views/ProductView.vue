<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import { addComment, listComments } from '@/api/comment'
import { addFavorite, favoriteExists, removeFavorite } from '@/api/favorite'
import { getProduct, getShop } from '@/api/product'
import type { Comment, ProductDetail, ProductSku } from '@/api/types'
import PriceText from '@/components/PriceText.vue'
import EmptyState from '@/components/EmptyState.vue'
import SafeImage from '@/components/SafeImage.vue'
import { imageUrl, productPrice, skuLabel } from '@/utils/format'
import { useAuthStore } from '@/stores/auth'
import { useCartStore } from '@/stores/cart'

const route = useRoute()
const router = useRouter()
const message = useMessage()
const auth = useAuthStore()
const cart = useCartStore()
const detail = ref<ProductDetail>()
const shopName = ref('')
const sku = ref<ProductSku>()
const comments = ref<Comment[]>([])
const favored = ref(false)
const content = ref('')
const star = ref(5)
const activeImage = ref('')
const images = computed(() => {
  const raw = detail.value?.imageList
  const list = raw ? raw.split(',').filter(Boolean) : []
  if (detail.value?.mainImage) list.unshift(detail.value.mainImage)
  return [...new Set(list)].map(imageUrl).filter(Boolean)
})

const id = computed(() => String(route.params.id))

async function load() {
  detail.value = await getProduct(id.value)
  shopName.value = ''
  if (detail.value?.shopId) {
    try {
      const shop = await getShop(detail.value.shopId)
      shopName.value = shop?.shopName || ''
    } catch {
      shopName.value = ''
    }
  }
  sku.value = detail.value.skuList?.[0]
  activeImage.value = images.value[0] || ''
  comments.value = (await listComments(id.value)) || []
  if (auth.userId) favored.value = await favoriteExists(auth.userId, id.value)
}

async function addToCart() {
  if (!auth.isLogin) return router.push(`/login?redirect=${route.fullPath}`)
  if (!sku.value) return message.error('暂无可购规格')
  await cart.add(sku.value.id, detail.value!.id, 1)
  message.success('已加入购物车')
}

async function buyNow() {
  await addToCart()
  router.push('/cart')
}

async function toggleFav() {
  if (!auth.userId) return router.push('/login')
  if (favored.value) await removeFavorite(auth.userId, id.value)
  else await addFavorite(auth.userId, id.value)
  favored.value = !favored.value
}

async function submitComment() {
  if (!auth.userId) return router.push('/login')
  await addComment({
    userId: auth.userId,
    spuId: id.value,
    skuId: sku.value?.id,
    orderId: 0,
    nickname: auth.user?.nickname,
    starRating: star.value,
    content: content.value,
  })
  content.value = ''
  comments.value = (await listComments(id.value)) || []
  message.success('评价已提交')
}

onMounted(load)
watch(id, load)
</script>

<template>
  <div v-if="detail" class="grid grid-cols-[420px_1fr] gap-8">
    <div class="space-y-3">
      <div class="overflow-hidden rounded-card bg-white">
        <SafeImage :src="activeImage || images[0]" :alt="detail.title" class="aspect-square w-full object-cover" />
      </div>
      <div v-if="images.length > 1" class="grid grid-cols-5 gap-2">
        <button
          v-for="item in images"
          :key="item"
          class="overflow-hidden rounded-xl border"
          :class="activeImage === item ? 'border-brand' : 'border-transparent'"
          @click="activeImage = item"
        >
          <SafeImage :src="item" class="aspect-square w-full object-cover" />
        </button>
      </div>
    </div>
    <div class="space-y-5">
      <div>
        <h1 class="text-3xl font-semibold">{{ detail.title }}</h1>
        <p class="mt-2 text-mute">{{ detail.sellingPoint || detail.subTitle }}</p>
        <p v-if="shopName" class="mt-2 text-sm text-sky-700">店铺：{{ shopName }}</p>
      </div>
      <div class="rounded-card bg-rose-50 px-5 py-4">
        <div class="text-xs text-mute">售价</div>
        <PriceText :value="sku?.price || productPrice(detail)" size="lg" />
        <div v-if="sku?.originalPrice && Number(sku.originalPrice) > Number(sku.price || 0)" class="mt-1 text-sm text-mute line-through">
          ¥{{ Number(sku.originalPrice).toFixed(2) }}
        </div>
      </div>
      <div class="flex flex-wrap gap-2">
        <button
          v-for="item in detail.skuList"
          :key="item.id"
          class="h-10 rounded-full border px-4 text-sm"
          :class="sku?.id === item.id ? 'border-brand text-brand' : 'border-slate-200'"
          @click="sku = item"
        >
          {{ skuLabel({ skuName: item.skuName, title: detail.title }) }}
        </button>
      </div>
      <div class="flex gap-3">
        <button class="h-11 flex-1 rounded-full bg-brand text-white" @click="buyNow">立即购买</button>
        <button class="h-11 flex-1 rounded-full bg-ink text-white" @click="addToCart">加入购物车</button>
        <button class="h-11 rounded-full border px-5" @click="toggleFav">{{ favored ? '已收藏' : '收藏' }}</button>
      </div>
    </div>
    <section class="col-span-2 rounded-card bg-white p-6">
      <h2 class="mb-3 text-lg font-semibold">商品详情</h2>
      <div v-if="detail.detailHtml" class="prose max-w-none" v-html="detail.detailHtml" />
      <p v-else class="text-mute">暂无图文详情</p>
    </section>
    <section class="col-span-2 space-y-3 rounded-card bg-white p-6">
      <h2 class="text-lg font-semibold">评价</h2>
      <form v-if="auth.isLogin" class="space-y-3 rounded-xl bg-page p-4" @submit.prevent="submitComment">
        <label class="block space-y-1.5">
          <span class="text-sm font-medium">评分</span>
          <select v-model.number="star" class="shop-select max-w-40">
            <option :value="5">5 星 · 很满意</option>
            <option :value="4">4 星 · 满意</option>
            <option :value="3">3 星 · 一般</option>
            <option :value="2">2 星 · 较差</option>
            <option :value="1">1 星 · 很差</option>
          </select>
        </label>
        <label class="block space-y-1.5">
          <span class="text-sm font-medium">评价内容</span>
          <textarea v-model="content" class="shop-textarea" placeholder="写下使用感受，至少 2 个字" required minlength="2" />
        </label>
        <button type="submit" class="h-11 rounded-full bg-ink px-5 text-white">发表评价</button>
      </form>
      <p v-else class="text-sm text-mute">登录后可以发表评价</p>
      <div v-for="item in comments" :key="item.id" class="border-t border-slate-100 py-3">
        <div class="text-sm font-medium">{{ item.nickname || '用户' }} · {{ item.starRating }}星</div>
        <div class="mt-1 text-sm text-mute">{{ item.content }}</div>
        <div v-if="item.replyContent" class="mt-1 text-sm">商家回复：{{ item.replyContent }}</div>
      </div>
      <EmptyState v-if="!comments.length" title="还没有评价" />
    </section>
  </div>
</template>
