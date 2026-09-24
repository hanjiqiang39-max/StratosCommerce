<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { Icon } from '@iconify/vue'
import { useMessage } from 'naive-ui'
import { listOrders } from '@/api/order'
import { listMyGroups, listMySeckills, listUserCoupons } from '@/api/promotion'
import { getPoints, updateProfile, uploadImage } from '@/api/user'
import FormField from '@/components/FormField.vue'
import { useAuthStore } from '@/stores/auth'
import { imageUrl } from '@/utils/format'

const auth = useAuthStore()
const router = useRouter()
const message = useMessage()
const saving = ref(false)
const editing = ref(false)
const form = reactive({
  nickname: '',
  avatar: '',
  gender: 0,
})
const points = ref(0)
const couponCount = ref(0)
const seckillCount = ref(0)
const groupCount = ref(0)
const pendingPay = ref(0)
const pendingShip = ref(0)
const pendingReceive = ref(0)
const aftersale = ref(0)

const menus = [
  { to: '/address', label: '收货地址', desc: '常用地址', icon: 'solar:map-point-bold-duotone', tone: 'bg-sky-50 text-sky-600' },
  { to: '/favorites', label: '我的收藏', desc: '心仪商品', icon: 'solar:heart-bold-duotone', tone: 'bg-rose-50 text-price' },
  { to: '/points', label: '积分账户', desc: '100 分抵 1 元', icon: 'solar:star-bold-duotone', tone: 'bg-amber-50 text-amber-600' },
  { to: '/notifications', label: '消息通知', desc: '交易提醒', icon: 'solar:bell-bing-bold-duotone', tone: 'bg-violet-50 text-violet-600' },
  { to: '/seckill', label: '限时秒杀', desc: '去抢购', icon: 'solar:bolt-bold-duotone', tone: 'bg-rose-50 text-price' },
  { to: '/group', label: '拼团活动', desc: '邀好友', icon: 'solar:users-group-rounded-bold-duotone', tone: 'bg-sky-50 text-sky-600' },
]

const orderShortcuts = computed(() => [
  { to: '/orders', label: '待支付', count: pendingPay.value, icon: 'solar:wallet-money-bold-duotone' },
  { to: '/orders', label: '待发货', count: pendingShip.value, icon: 'solar:box-bold-duotone' },
  { to: '/orders', label: '待收货', count: pendingReceive.value, icon: 'solar:delivery-bold-duotone' },
  { to: '/orders', label: '售后', count: aftersale.value, icon: 'solar:restart-bold-duotone' },
])

function syncForm() {
  form.nickname = auth.user?.nickname || ''
  form.avatar = auth.user?.avatar || ''
  form.gender = auth.user?.gender || 0
}

async function onAvatar(event: Event) {
  const file = (event.target as HTMLInputElement).files?.[0]
  ;(event.target as HTMLInputElement).value = ''
  if (!file || !auth.userId) return
  if (file.size > 5 * 1024 * 1024) {
    message.error('图片不能超过 5MB')
    return
  }
  const data = await uploadImage(file)
  if (data?.url) form.avatar = data.url
}

async function save() {
  if (!auth.userId) return
  if (!form.nickname.trim()) {
    message.error('请填写昵称')
    return
  }
  saving.value = true
  try {
    await updateProfile(auth.userId, {
      nickname: form.nickname.trim(),
      avatar: form.avatar,
      gender: form.gender,
    })
    await auth.refreshProfile()
    syncForm()
    editing.value = false
    message.success('资料已保存')
  } finally {
    saving.value = false
  }
}

async function loadSummary() {
  if (!auth.userId) return
  const [orderPage, coupons, seckills, groups, pointAcc] = await Promise.allSettled([
    listOrders(auth.userId, 1, 50),
    listUserCoupons(auth.userId, 0),
    listMySeckills(auth.userId),
    listMyGroups(auth.userId),
    getPoints(auth.userId),
  ])
  if (orderPage.status === 'fulfilled') {
    const records = orderPage.value?.records || []
    pendingPay.value = records.filter((item) => item.status === 0).length
    pendingShip.value = records.filter((item) => item.status === 20).length
    pendingReceive.value = records.filter((item) => item.status === 30).length
    aftersale.value = records.filter((item) => item.status === -20 || item.status === -30).length
  }
  if (coupons.status === 'fulfilled') couponCount.value = (coupons.value || []).length
  if (seckills.status === 'fulfilled') seckillCount.value = (seckills.value || []).length
  if (groups.status === 'fulfilled') groupCount.value = (groups.value || []).length
  if (pointAcc.status === 'fulfilled') points.value = pointAcc.value?.availablePoints || auth.user?.points || 0
}

onMounted(async () => {
  if (auth.isLogin) {
    await auth.refreshProfile()
    syncForm()
    await loadSummary()
  }
})
</script>

<template>
  <div class="space-y-5">
    <section class="overflow-hidden rounded-card bg-gradient-to-br from-sky-500 via-sky-400 to-cyan-300 p-6 text-white shadow-card">
      <div v-if="auth.isLogin" class="flex items-center justify-between gap-4">
        <div class="flex items-center gap-4">
          <div class="h-16 w-16 overflow-hidden rounded-2xl bg-white/20 ring-2 ring-white/40">
            <img v-if="form.avatar" :src="imageUrl(form.avatar)" alt="" class="h-full w-full object-cover" />
            <div v-else class="flex h-full w-full items-center justify-center text-lg font-semibold">
              {{ (auth.user?.nickname || '我').slice(0, 1) }}
            </div>
          </div>
          <div>
            <div class="text-2xl font-semibold">{{ auth.user?.nickname || '你好' }}</div>
            <div class="mt-1 text-sm text-white/80">{{ auth.user?.phone || auth.user?.username }}</div>
          </div>
        </div>
        <button class="h-9 rounded-full bg-white/20 px-4 text-sm backdrop-blur" @click="editing = !editing">
          {{ editing ? '收起资料' : '编辑资料' }}
        </button>
      </div>
      <div v-else class="flex items-center justify-between">
        <div>
          <div class="text-2xl font-semibold">欢迎来到 Stratos</div>
          <div class="mt-1 text-sm text-white/80">登录后查看订单、秒杀和优惠券</div>
        </div>
        <button class="h-11 rounded-full bg-white px-5 font-medium text-sky-600" @click="router.push('/login')">登录 / 注册</button>
      </div>
      <div v-if="auth.isLogin" class="mt-6 grid grid-cols-3 gap-3">
        <div class="rounded-2xl bg-white/15 px-4 py-3 backdrop-blur">
          <div class="text-xs text-white/70">可用积分</div>
          <div class="mt-1 text-xl font-semibold">{{ points }}</div>
        </div>
        <RouterLink to="/coupons" class="rounded-2xl bg-white/15 px-4 py-3 backdrop-blur">
          <div class="text-xs text-white/70">未用优惠券</div>
          <div class="mt-1 text-xl font-semibold">{{ couponCount }}</div>
        </RouterLink>
        <RouterLink to="/orders" class="rounded-2xl bg-white/15 px-4 py-3 backdrop-blur">
          <div class="text-xs text-white/70">待支付</div>
          <div class="mt-1 text-xl font-semibold">{{ pendingPay }}</div>
        </RouterLink>
      </div>
    </section>

    <section v-if="auth.isLogin" class="rounded-card bg-white p-5 shadow-card">
      <div class="mb-4 flex items-center justify-between">
        <h2 class="font-semibold">我的订单</h2>
        <RouterLink to="/orders" class="text-sm text-brand">全部订单</RouterLink>
      </div>
      <div class="grid grid-cols-4 gap-3">
        <RouterLink v-for="item in orderShortcuts" :key="item.label" :to="item.to" class="rounded-2xl bg-page px-3 py-4 text-center">
          <div class="relative inline-flex">
            <Icon :icon="item.icon" width="26" class="text-ink" />
            <span
              v-if="item.count"
              class="absolute -right-3 -top-2 min-w-5 rounded-full bg-price px-1 text-[10px] leading-4 text-white"
            >
              {{ item.count }}
            </span>
          </div>
          <div class="mt-2 text-sm">{{ item.label }}</div>
        </RouterLink>
      </div>
    </section>

    <section v-if="auth.isLogin" class="grid grid-cols-2 gap-4">
      <RouterLink to="/seckill" class="rounded-card bg-gradient-to-br from-rose-50 to-white p-5 shadow-card">
        <div class="text-xs text-price">秒杀</div>
        <div class="mt-2 text-lg font-semibold">我的抢购 {{ seckillCount }}</div>
        <div class="mt-1 text-sm text-mute">已抢到的会直接进入订单</div>
      </RouterLink>
      <RouterLink to="/group" class="rounded-card bg-gradient-to-br from-sky-50 to-white p-5 shadow-card">
        <div class="text-xs text-sky-600">拼团</div>
        <div class="mt-2 text-lg font-semibold">我参加的团 {{ groupCount }}</div>
        <div class="mt-1 text-sm text-mute">成团后即可继续支付</div>
      </RouterLink>
    </section>

    <section class="grid grid-cols-3 gap-3">
      <RouterLink
        v-for="item in menus"
        :key="item.to"
        :to="item.to"
        class="rounded-card bg-white p-4 shadow-card transition hover:-translate-y-0.5"
      >
        <div class="inline-flex h-10 w-10 items-center justify-center rounded-2xl" :class="item.tone">
          <Icon :icon="item.icon" width="20" />
        </div>
        <div class="mt-3 font-medium">{{ item.label }}</div>
        <div class="mt-1 text-xs text-mute">{{ item.desc }}</div>
      </RouterLink>
    </section>

    <form v-if="auth.isLogin && editing" class="space-y-4 rounded-card bg-white p-6 shadow-card" @submit.prevent="save">
      <h2 class="font-semibold">编辑资料</h2>
      <FormField label="头像">
        <div class="flex items-center gap-3">
          <label class="inline-flex h-11 cursor-pointer items-center rounded-full border px-4 text-sm">
            上传头像
            <input type="file" accept="image/*" class="hidden" @change="onAvatar" />
          </label>
          <button v-if="form.avatar" type="button" class="text-sm text-price" @click="form.avatar = ''">移除</button>
        </div>
      </FormField>
      <FormField label="昵称" required>
        <input v-model="form.nickname" class="shop-input" maxlength="20" required />
      </FormField>
      <FormField label="性别">
        <select v-model.number="form.gender" class="shop-select">
          <option :value="0">保密</option>
          <option :value="1">男</option>
          <option :value="2">女</option>
        </select>
      </FormField>
      <button type="submit" class="h-11 w-full rounded-full bg-brand text-white" :disabled="saving">
        {{ saving ? '保存中...' : '保存资料' }}
      </button>
    </form>

    <button
      v-if="auth.isLogin"
      class="h-11 w-full rounded-full border bg-white text-mute"
      @click="auth.logout(); router.push('/')"
    >
      退出登录
    </button>
  </div>
</template>
