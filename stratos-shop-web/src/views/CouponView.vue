<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { Icon } from '@iconify/vue'
import { NSkeleton, useMessage } from 'naive-ui'
import { listCoupons, listUserCoupons, receiveCoupon } from '@/api/promotion'
import type { Coupon, UserCoupon } from '@/api/types'
import EmptyState from '@/components/EmptyState.vue'
import { useAuthStore } from '@/stores/auth'
import { couponLabel, couponStatusText } from '@/utils/format'

const auth = useAuthStore()
const router = useRouter()
const route = useRoute()
const message = useMessage()
const coupons = ref<Coupon[]>([])
const mine = ref<UserCoupon[]>([])
const tab = ref<'center' | 'mine'>('center')
const loading = ref(true)
const receiving = ref<Coupon['id'] | null>(null)

const unused = computed(() => mine.value.filter((item) => item.status === 0))

function received(id: Coupon['id']) {
  return mine.value.some((item) => String(item.couponId) === String(id))
}

function remain(item: Coupon) {
  if (item.publishCount == null || item.publishCount < 0) return '不限量'
  return `剩余 ${Math.max((item.publishCount || 0) - (item.receivedCount || 0), 0)}`
}

function tone(status?: number) {
  if (status === 0) return 'from-rose-500 to-orange-400'
  return 'from-slate-400 to-slate-300'
}

async function load() {
  loading.value = true
  try {
    coupons.value = (await listCoupons()) || []
    if (auth.userId) mine.value = (await listUserCoupons(auth.userId)) || []
  } finally {
    loading.value = false
  }
}

async function receive(id: Coupon['id']) {
  if (!auth.userId) {
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  receiving.value = id
  try {
    await receiveCoupon(auth.userId, id)
    message.success('领取成功，结算时可抵扣')
    await load()
    tab.value = 'mine'
  } finally {
    receiving.value = null
  }
}

onMounted(load)
</script>

<template>
  <div class="space-y-5">
    <section class="overflow-hidden rounded-card bg-gradient-to-br from-rose-500 via-rose-400 to-orange-300 p-6 text-white shadow-card">
      <div class="flex items-end justify-between gap-4">
        <div>
          <div class="inline-flex items-center gap-1 rounded-full bg-white/15 px-2.5 py-1 text-xs">
            <Icon icon="solar:ticket-bold" width="14" />
            优惠券
          </div>
          <h1 class="mt-3 text-3xl font-semibold">领券后下单自动抵扣</h1>
          <p class="mt-2 text-sm text-white/80">在结算页按订单金额试算，满减和折扣都能用</p>
        </div>
        <div class="rounded-2xl bg-white/15 px-4 py-3 text-right backdrop-blur">
          <div class="text-xs text-white/70">未使用</div>
          <div class="mt-1 text-2xl font-semibold">{{ unused.length }}</div>
        </div>
      </div>
    </section>

    <div class="flex gap-2">
      <button class="h-9 rounded-full px-4 text-sm" :class="tab === 'center' ? 'bg-ink text-white' : 'bg-white text-mute shadow-card'" @click="tab = 'center'">
        领券中心
      </button>
      <button class="h-9 rounded-full px-4 text-sm" :class="tab === 'mine' ? 'bg-ink text-white' : 'bg-white text-mute shadow-card'" @click="tab = 'mine'">
        我的券 {{ unused.length ? `(${unused.length})` : '' }}
      </button>
    </div>

    <div v-if="loading" class="grid grid-cols-2 gap-4">
      <NSkeleton v-for="item in 4" :key="item" height="140px" class="rounded-card" />
    </div>

    <div v-else-if="tab === 'center'">
      <EmptyState v-if="!coupons.length" title="暂无可领优惠券" desc="后台发布优惠券后会出现在这里" icon="solar:ticket-bold-duotone" />
      <div v-else class="grid grid-cols-2 gap-4">
        <article v-for="item in coupons" :key="item.id" class="flex overflow-hidden rounded-card bg-white shadow-card">
          <div class="flex w-28 shrink-0 flex-col items-center justify-center bg-gradient-to-br from-rose-500 to-orange-400 px-3 text-center text-white">
            <div class="text-lg font-semibold leading-6">{{ couponLabel(item) }}</div>
          </div>
          <div class="flex min-w-0 flex-1 items-center justify-between gap-3 p-4">
            <div class="min-w-0">
              <div class="truncate font-medium">{{ item.couponName }}</div>
              <div class="mt-1 text-xs text-mute">{{ remain(item) }} · 每人 {{ item.limitPerUser || 1 }} 张</div>
              <div v-if="item.endTime" class="mt-1 text-xs text-mute">有效期至 {{ item.endTime }}</div>
            </div>
            <button
              class="h-9 shrink-0 rounded-full px-4 text-sm text-white disabled:bg-slate-300"
              :class="received(item.id) ? 'bg-slate-300' : 'bg-ink'"
              :disabled="received(item.id) || receiving === item.id"
              @click="receive(item.id)"
            >
              {{ received(item.id) ? '已领取' : receiving === item.id ? '领取中' : '立即领取' }}
            </button>
          </div>
        </article>
      </div>
    </div>

    <div v-else>
      <EmptyState v-if="!mine.length" title="还没有优惠券" desc="去领券中心领一张，结算时就能抵扣" icon="solar:ticket-bold-duotone">
        <button class="h-10 rounded-full bg-ink px-5 text-sm text-white" @click="tab = 'center'">去领券</button>
      </EmptyState>
      <div v-else class="grid grid-cols-2 gap-4">
        <article v-for="item in mine" :key="item.id" class="flex overflow-hidden rounded-card bg-white shadow-card">
          <div class="flex w-28 shrink-0 flex-col items-center justify-center bg-gradient-to-br px-3 text-center text-white" :class="tone(item.status)">
            <div class="text-lg font-semibold leading-6">{{ couponLabel(item) }}</div>
          </div>
          <div class="flex min-w-0 flex-1 items-center justify-between gap-3 p-4">
            <div class="min-w-0">
              <div class="truncate font-medium">{{ item.couponName || `优惠券 #${item.id}` }}</div>
              <div class="mt-1 text-xs text-mute">{{ couponStatusText(item.status) }} · 有效期至 {{ item.endTime || '-' }}</div>
            </div>
            <RouterLink
              v-if="item.status === 0"
              to="/category"
              class="inline-flex h-9 shrink-0 items-center rounded-full bg-ink px-4 text-sm text-white"
            >
              去使用
            </RouterLink>
            <span v-else class="text-xs text-mute">{{ couponStatusText(item.status) }}</span>
          </div>
        </article>
      </div>
    </div>
  </div>
</template>
