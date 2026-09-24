<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import { listAddress } from '@/api/address'
import { createFromCart, createOrder } from '@/api/order'
import { getProduct } from '@/api/product'
import { groupActivity, listUserCoupons, quoteCoupon, quoteFullDiscount, seckillDetail } from '@/api/promotion'
import { getPoints } from '@/api/user'
import type { Address, ProductDetail, UserCoupon } from '@/api/types'
import FormField from '@/components/FormField.vue'
import PageHeader from '@/components/PageHeader.vue'
import PriceText from '@/components/PriceText.vue'
import SafeImage from '@/components/SafeImage.vue'
import { useAuthStore } from '@/stores/auth'
import { useCartStore } from '@/stores/cart'
import { couponLabel, fullDiscountLabel, money, productPrice } from '@/utils/format'

const auth = useAuthStore()
const cart = useCartStore()
const route = useRoute()
const router = useRouter()
const message = useMessage()
const addresses = ref<Address[]>([])
const addressId = ref<number | string>()
const coupons = ref<UserCoupon[]>([])
const couponId = ref<string>('')
const usePoints = ref(0)
const available = ref(0)
const remark = ref('')
const couponOff = ref(0)
const fullOff = ref(0)
const fullName = ref('')
const submitting = ref(false)
const extras = ref<Record<string, ProductDetail>>({})
const activityTitle = ref('')
const activityPrice = ref(0)
const activityImage = ref('')

const mode = computed(() => String(route.query.type || 'cart'))
const isActivity = computed(() => mode.value === 'seckill' || mode.value === 'group')
const selectedItems = computed(() => cart.items.filter((item) => item.selected === 1))
const qty = computed(() => Number(route.query.qty || 1) || 1)
const skuId = computed(() => String(route.query.skuId || ''))
const spuId = computed(() => String(route.query.spuId || ''))

const goodsAmount = computed(() => {
  if (isActivity.value) return activityPrice.value * qty.value
  return selectedItems.value.reduce((sum, item) => {
    const detail = extras.value[String(item.spuId)]
    const sku = detail?.skuList?.find((row) => String(row.id) === String(item.skuId))
    return sum + productPrice({ price: sku?.price, minPrice: detail?.minPrice }) * item.quantity
  }, 0)
})

const selectedCount = computed(() => (isActivity.value ? qty.value : selectedItems.value.length))
const checkoutSpuIds = computed(() => {
  if (isActivity.value) return spuId.value ? [spuId.value] : []
  return [...new Set(selectedItems.value.map((item) => item.spuId))]
})
const payAmount = computed(() => Math.max(goodsAmount.value - couponOff.value - fullOff.value, 0))

async function loadCartExtras() {
  const ids = [...new Set(selectedItems.value.map((item) => item.spuId))]
  await Promise.all(
    ids.map(async (id) => {
      try {
        extras.value[String(id)] = await getProduct(id)
      } catch {
        /* keep checkout usable */
      }
    }),
  )
}

async function loadActivity() {
  if (!isActivity.value) return
  try {
    if (mode.value === 'seckill' && route.query.seckillId) {
      const item = await seckillDetail(String(route.query.seckillId))
      activityPrice.value = Number(item.seckillPrice || 0)
      activityTitle.value = item.activityName || '秒杀商品'
    } else if (mode.value === 'group' && route.query.groupBuyingId) {
      const item = await groupActivity(String(route.query.groupBuyingId))
      activityPrice.value = Number(item.groupPrice || 0)
      activityTitle.value = item.activityName || '拼团商品'
    }
    if (spuId.value) {
      const detail = await getProduct(spuId.value)
      extras.value[spuId.value] = detail
      activityTitle.value = detail.title || activityTitle.value
      activityImage.value = detail.mainImage || ''
    }
  } catch {
    /* activity fields stay empty */
  }
}

async function load() {
  if (!auth.userId) return
  try {
    if (!isActivity.value) {
      await cart.refresh()
      await loadCartExtras()
    }
    await loadActivity()
  } catch {
    /* 购物车失败时仍展示地址和优惠 */
  }
  try {
    addresses.value = (await listAddress(auth.userId)) || []
    addressId.value = addresses.value.find((a) => a.isDefault === 1)?.id || addresses.value[0]?.id
    coupons.value = (await listUserCoupons(auth.userId, 0)) || []
    const points = await getPoints(auth.userId)
    available.value = points?.availablePoints || 0
  } catch {
    /* 地址或券失败时仍可提交已加载的数据 */
  }
  await quote()
}

async function quote() {
  if (!couponId.value || !auth.userId) {
    couponOff.value = 0
  } else {
    try {
      couponOff.value = Number(await quoteCoupon(couponId.value, auth.userId, goodsAmount.value || 0)) || 0
    } catch {
      couponOff.value = 0
    }
  }
  if (isActivity.value || !goodsAmount.value) {
    fullOff.value = 0
    fullName.value = ''
    return
  }
  try {
    const vo = await quoteFullDiscount(goodsAmount.value || 0, checkoutSpuIds.value)
    fullOff.value = Number(vo?.discountAmount || 0) || 0
    fullName.value = fullDiscountLabel({
      activityName: vo?.activityName,
      fullAmount: vo?.fullAmount,
      discountAmount: vo?.discountAmount,
    })
  } catch {
    fullOff.value = 0
    fullName.value = ''
  }
}

watch([couponId, goodsAmount], quote)

function pickedAddress() {
  return addresses.value.find((item) => String(item.id) === String(addressId.value))
}

async function submit() {
  if (!auth.userId) return
  const selected = pickedAddress()
  if (!selected) return message.error('请选择收货地址')
  if (!isActivity.value && !selectedCount.value) return message.error('请先在购物车勾选商品')
  if (isActivity.value && (!skuId.value || !activityPrice.value)) return message.error('活动商品信息缺失')
  if (submitting.value) return
  submitting.value = true
  try {
    const address = {
      userId: auth.userId,
      addressId: selected.id,
      receiverName: selected.receiverName,
      receiverPhone: selected.receiverPhone,
      receiverProvince: selected.province,
      receiverCity: selected.city,
      receiverDistrict: selected.district,
      receiverDetailAddress: selected.detailAddress,
      couponId: couponId.value || undefined,
      usePoints: usePoints.value || 0,
      buyerRemark: remark.value,
    }
    const vo = isActivity.value
      ? await createOrder({
          ...address,
          items: [{ skuId: skuId.value, spuId: spuId.value || undefined, quantity: qty.value }],
          seckillId: mode.value === 'seckill' ? route.query.seckillId : undefined,
          seckillRecordId: mode.value === 'seckill' ? route.query.seckillRecordId : undefined,
          groupBuyingId: mode.value === 'group' ? route.query.groupBuyingId : undefined,
          groupRecordId: mode.value === 'group' ? route.query.groupRecordId : undefined,
        })
      : await createFromCart(address)
    router.push(`/pay/${vo.orderId}?orderNo=${vo.orderNo}&amount=${vo.payAmount}${mode.value === 'group' ? '&group=1' : ''}`)
  } catch {
    /* 接口错误已由 request 弹出 */
  } finally {
    submitting.value = false
  }
}

onMounted(load)
</script>

<template>
  <div>
    <PageHeader
      :title="mode === 'seckill' ? '秒杀结算' : mode === 'group' ? '拼团结算' : '确认订单'"
      :extra="mode === 'group' ? '提交后需等成团才能支付' : '核对地址、优惠后再提交'"
    />
    <form class="grid grid-cols-[1fr_320px] gap-6" @submit.prevent="submit">
      <div class="space-y-4">
        <section class="rounded-card bg-white p-5">
          <div class="mb-3 flex justify-between">
            <h2 class="font-semibold">收货地址</h2>
            <RouterLink to="/address" class="text-sm text-brand">管理地址</RouterLink>
          </div>
          <p v-if="!addresses.length" class="text-sm text-mute">还没有地址，先去新增一个。</p>
          <button
            v-for="item in addresses"
            :key="item.id"
            type="button"
            class="mb-2 h-auto w-full rounded-xl border px-4 py-3 text-left"
            :class="String(addressId) === String(item.id) ? 'border-brand bg-sky-50/60' : 'border-slate-200'"
            @click="addressId = item.id"
          >
            <div class="font-medium">{{ item.receiverName }} {{ item.receiverPhone }}</div>
            <div class="text-sm text-mute">{{ item.province }}{{ item.city }}{{ item.district }}{{ item.detailAddress }}</div>
          </button>
        </section>

        <section v-if="isActivity" class="flex items-center gap-4 rounded-card bg-white p-5">
          <div class="h-20 w-20 overflow-hidden rounded-xl bg-slate-100">
            <SafeImage :src="activityImage" class="h-full w-full object-cover" />
          </div>
          <div class="min-w-0 flex-1">
            <div class="font-medium">{{ activityTitle }}</div>
            <div class="mt-1 text-sm text-mute">{{ mode === 'seckill' ? '秒杀价' : '拼团价' }} {{ money(activityPrice) }} × {{ qty }}</div>
          </div>
        </section>

        <section class="space-y-4 rounded-card bg-white p-5">
          <h2 class="font-semibold">优惠与备注</h2>
          <FormField label="优惠券" hint="按当前商品金额试算，未满门槛会提示不可用">
            <select v-model="couponId" class="shop-select">
              <option value="">不使用优惠券</option>
              <option v-for="item in coupons" :key="item.id" :value="String(item.id)">
                {{ item.couponName || couponLabel(item) }} · {{ couponLabel(item) }}
              </option>
            </select>
          </FormField>
          <FormField label="使用积分" :hint="`可用 ${available} 分，100 积分 = 1 元`">
            <input v-model.number="usePoints" type="number" min="0" :max="available" class="shop-input" />
          </FormField>
          <FormField label="订单备注">
            <textarea v-model="remark" class="shop-textarea" placeholder="选填，例如配送时间、门牌号" maxlength="100" />
          </FormField>
        </section>
      </div>
      <aside class="h-fit rounded-card bg-white p-5 shadow-card">
        <div class="text-mute">{{ isActivity ? '活动商品' : `已选 ${selectedCount} 件` }}</div>
        <div class="mt-3 text-sm">商品金额 {{ money(goodsAmount) }}</div>
        <div v-if="fullOff" class="mt-2 text-sm text-price">{{ fullName || '满减' }} -{{ money(fullOff) }}</div>
        <div v-if="couponOff" class="mt-2 text-sm text-price">券抵扣 -{{ money(couponOff) }}</div>
        <div class="mt-4"><PriceText :value="payAmount" size="lg" /></div>
        <button type="submit" :disabled="submitting" class="mt-5 h-11 w-full rounded-full bg-brand text-white">
          {{ submitting ? '提交中...' : '提交订单' }}
        </button>
      </aside>
    </form>
  </div>
</template>
