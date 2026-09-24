<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useMessage } from 'naive-ui'
import {
  couponStatus,
  groupStatus,
  productSkus,
  promoCoupon,
  promoGroup,
  promoSeckill,
  saveCoupon,
  saveGroup,
  saveSeckill,
  seckillStatus,
} from '@/api'
import FormField from '@/components/FormField.vue'
import MerchantPage from '@/components/MerchantPage.vue'
import { useAuthStore } from '@/stores/auth'
import { dateOrder, firstError, inputClass, maxLen, minLen, numberRange, required, selectClass } from '@/utils/validate'

const auth = useAuthStore()
const message = useMessage()
const tab = ref<'seckill' | 'group' | 'coupon'>('seckill')
const skus = ref<any[]>([])
const seckills = ref<any[]>([])
const groups = ref<any[]>([])
const coupons = ref<any[]>([])

const seckillForm = reactive(emptySeckill())
const groupForm = reactive(emptyGroup())
const couponForm = reactive(emptyCoupon())
const seckillErrors = reactive({ activityName: '', skuId: '', seckillPrice: '', seckillStock: '', time: '' })
const groupErrors = reactive({ activityName: '', skuId: '', groupPrice: '', requireNum: '', time: '' })
const couponErrors = reactive({ couponName: '', discountValue: '', minAmount: '', time: '' })

const skuOptions = computed(() =>
  skus.value.map((item) => ({
    id: item.id,
    spuId: item.spuId,
    label: `${item.skuName || item.id} · ¥${item.price ?? 0}`,
    price: Number(item.price || 0),
    originalPrice: Number(item.originalPrice || item.price || 0),
  })),
)

function emptySeckill() {
  return {
    id: '' as string | number | '',
    activityName: '',
    skuId: '',
    spuId: '',
    originalPrice: 99,
    seckillPrice: 9.9,
    seckillStock: 20,
    limitPerUser: 1,
    startTime: '',
    endTime: '',
    status: 1,
  }
}

function emptyGroup() {
  return {
    id: '' as string | number | '',
    activityName: '',
    skuId: '',
    spuId: '',
    originalPrice: 99,
    groupPrice: 79,
    requireNum: 2,
    limitHours: 24,
    limitPerUser: 1,
    startTime: '',
    endTime: '',
    status: 1,
  }
}

function emptyCoupon() {
  return {
    id: '' as string | number | '',
    couponName: '',
    discountType: 1,
    discountValue: 10,
    minAmount: 50,
    publishCount: 200,
    limitPerUser: 1,
    validDays: 7,
    startTime: '',
    endTime: '',
    status: 1,
  }
}

function toInput(value?: string) {
  if (!value) return ''
  return String(value).replace(' ', 'T').slice(0, 16)
}

function fromInput(value?: string) {
  if (!value) return undefined
  return value.length === 16 ? `${value.replace('T', ' ')}:00` : String(value).replace('T', ' ')
}

function syncSku(kind: 'seckill' | 'group') {
  const form = kind === 'seckill' ? seckillForm : groupForm
  const hit = skuOptions.value.find((item) => String(item.id) === String(form.skuId))
  if (!hit) return
  form.spuId = hit.spuId
  form.originalPrice = hit.originalPrice || hit.price
}

function fill(target: Record<string, any>, source: Record<string, any>) {
  Object.keys(target).forEach((key) => {
    if (key.endsWith('Time')) target[key] = toInput(source[key])
    else target[key] = source[key] ?? target[key]
  })
}

async function load() {
  if (!auth.shopId) return
  skus.value = (await productSkus(auth.shopId)) || []
  const skuIds = skus.value.map((item) => item.id).join(',')
  seckills.value = (await promoSeckill(skuIds || undefined)) || []
  groups.value = (await promoGroup(skuIds || undefined)) || []
  coupons.value = (await promoCoupon()) || []
}

function validateSeckill() {
  syncSku('seckill')
  seckillErrors.activityName = required(seckillForm.activityName, '活动名称') || minLen(seckillForm.activityName, 2, '活动名称') || maxLen(seckillForm.activityName, 40, '活动名称')
  seckillErrors.skuId = seckillForm.skuId ? '' : '请选择本店 SKU'
  seckillErrors.seckillPrice = numberRange(seckillForm.seckillPrice, 0.01, 999999, '秒杀价')
  if (!seckillErrors.seckillPrice && Number(seckillForm.seckillPrice) >= Number(seckillForm.originalPrice || 0) && Number(seckillForm.originalPrice) > 0) {
    seckillErrors.seckillPrice = '秒杀价必须低于原价'
  }
  seckillErrors.seckillStock = numberRange(seckillForm.seckillStock, 1, 999999, '秒杀库存', true)
  seckillErrors.time = required(seckillForm.startTime, '开始时间') || required(seckillForm.endTime, '结束时间') || dateOrder(seckillForm.startTime, seckillForm.endTime)
  const tip = firstError(seckillErrors)
  if (tip) message.error(tip)
  return !tip
}

async function submitSeckill() {
  if (!validateSeckill()) return
  await saveSeckill({
    ...seckillForm,
    id: seckillForm.id || undefined,
    startTime: fromInput(seckillForm.startTime),
    endTime: fromInput(seckillForm.endTime),
  })
  message.success('秒杀已保存，商城可立即抢购')
  Object.assign(seckillForm, emptySeckill())
  await load()
}

function validateGroup() {
  syncSku('group')
  groupErrors.activityName = required(groupForm.activityName, '活动名称') || minLen(groupForm.activityName, 2, '活动名称') || maxLen(groupForm.activityName, 40, '活动名称')
  groupErrors.skuId = groupForm.skuId ? '' : '请选择本店 SKU'
  groupErrors.groupPrice = numberRange(groupForm.groupPrice, 0.01, 999999, '拼团价')
  if (!groupErrors.groupPrice && Number(groupForm.groupPrice) >= Number(groupForm.originalPrice || 0) && Number(groupForm.originalPrice) > 0) {
    groupErrors.groupPrice = '拼团价必须低于原价'
  }
  groupErrors.requireNum = numberRange(groupForm.requireNum, 2, 20, '成团人数', true)
  groupErrors.time = required(groupForm.startTime, '开始时间') || required(groupForm.endTime, '结束时间') || dateOrder(groupForm.startTime, groupForm.endTime)
  const tip = firstError(groupErrors)
  if (tip) message.error(tip)
  return !tip
}

async function submitGroup() {
  if (!validateGroup()) return
  await saveGroup({
    ...groupForm,
    id: groupForm.id || undefined,
    startTime: fromInput(groupForm.startTime),
    endTime: fromInput(groupForm.endTime),
  })
  message.success('拼团已保存')
  Object.assign(groupForm, emptyGroup())
  await load()
}

function validateCoupon() {
  couponErrors.couponName = required(couponForm.couponName, '券名称') || minLen(couponForm.couponName, 2, '券名称') || maxLen(couponForm.couponName, 30, '券名称')
  couponErrors.discountValue =
    couponForm.discountType === 2
      ? numberRange(couponForm.discountValue, 1, 99, '折扣')
      : numberRange(couponForm.discountValue, 0.01, 99999, '减免金额')
  couponErrors.minAmount = numberRange(couponForm.minAmount, 0, 999999, '使用门槛')
  if (!couponErrors.discountValue && couponForm.discountType === 1 && Number(couponForm.discountValue) >= Number(couponForm.minAmount || 0) && Number(couponForm.minAmount) > 0) {
    couponErrors.discountValue = '减免金额必须小于使用门槛'
  }
  couponErrors.time = required(couponForm.startTime, '开始领取') || required(couponForm.endTime, '结束领取') || dateOrder(couponForm.startTime, couponForm.endTime, '开始领取', '结束领取')
  const tip = firstError(couponErrors)
  if (tip) message.error(tip)
  return !tip
}

async function submitCoupon() {
  if (!validateCoupon()) return
  await saveCoupon({
    ...couponForm,
    id: couponForm.id || undefined,
    couponType: couponForm.discountType === 2 ? 2 : 1,
    startTime: fromInput(couponForm.startTime),
    endTime: fromInput(couponForm.endTime),
  })
  message.success('优惠券已保存，买家可在领券中心领取')
  Object.assign(couponForm, emptyCoupon())
  await load()
}

onMounted(load)
</script>

<template>
  <MerchantPage title="营销活动" extra="为本店商品创建秒杀、拼团，或发放优惠券">
    <div class="mb-4 flex gap-2">
      <button
        v-for="item in [
          { id: 'seckill', label: '秒杀' },
          { id: 'group', label: '拼团' },
          { id: 'coupon', label: '优惠券' },
        ]"
        :key="item.id"
        class="h-9 rounded-full px-4 text-sm"
        :class="tab === item.id ? 'bg-teal-600 text-white' : 'bg-white'"
        @click="tab = item.id as any"
      >
        {{ item.label }}
      </button>
    </div>

    <section v-if="tab === 'seckill'" class="space-y-4">
      <form class="admin-card grid grid-cols-2 gap-4 p-5" @submit.prevent="submitSeckill">
        <FormField label="活动名称" required :error="seckillErrors.activityName">
          <input v-model="seckillForm.activityName" :class="inputClass(seckillErrors.activityName)" maxlength="40" />
        </FormField>
        <FormField label="本店 SKU" required :error="seckillErrors.skuId">
          <select v-model="seckillForm.skuId" :class="selectClass(seckillErrors.skuId)" @change="syncSku('seckill')">
            <option value="">选择商品规格</option>
            <option v-for="item in skuOptions" :key="item.id" :value="item.id">{{ item.label }}</option>
          </select>
        </FormField>
        <FormField label="秒杀价" required :error="seckillErrors.seckillPrice">
          <input v-model.number="seckillForm.seckillPrice" type="number" step="0.01" :class="inputClass(seckillErrors.seckillPrice)" />
        </FormField>
        <FormField label="秒杀库存" required :error="seckillErrors.seckillStock">
          <input v-model.number="seckillForm.seckillStock" type="number" min="1" :class="inputClass(seckillErrors.seckillStock)" />
        </FormField>
        <FormField label="开始时间" required :error="seckillErrors.time">
          <input v-model="seckillForm.startTime" type="datetime-local" :class="inputClass(seckillErrors.time)" />
        </FormField>
        <FormField label="结束时间" required :error="seckillErrors.time">
          <input v-model="seckillForm.endTime" type="datetime-local" :class="inputClass(seckillErrors.time)" />
        </FormField>
        <button class="h-11 rounded-full bg-teal-600 px-6 text-white">保存秒杀</button>
      </form>
      <div class="admin-card divide-y divide-slate-100">
        <div v-for="item in seckills" :key="item.id" class="flex items-center justify-between px-5 py-4">
          <div>
            <div class="font-medium">{{ item.activityName }} · ¥{{ item.seckillPrice }}</div>
            <div class="text-xs text-mute">库存 {{ item.seckillStock }} · {{ item.startTime }} ~ {{ item.endTime }}</div>
          </div>
          <div class="space-x-2">
            <button class="admin-btn" @click="fill(seckillForm, item)">编辑</button>
            <button class="admin-btn" @click="seckillStatus(item.id, item.status === 1 ? 3 : 1).then(load)">
              {{ item.status === 1 ? '下线' : '上线' }}
            </button>
          </div>
        </div>
        <div v-if="!seckills.length" class="px-5 py-8 text-center text-sm text-mute">还没有本店秒杀，先选一个 SKU 创建</div>
      </div>
    </section>

    <section v-else-if="tab === 'group'" class="space-y-4">
      <form class="admin-card grid grid-cols-2 gap-4 p-5" @submit.prevent="submitGroup">
        <FormField label="活动名称" required :error="groupErrors.activityName">
          <input v-model="groupForm.activityName" :class="inputClass(groupErrors.activityName)" maxlength="40" />
        </FormField>
        <FormField label="本店 SKU" required :error="groupErrors.skuId">
          <select v-model="groupForm.skuId" :class="selectClass(groupErrors.skuId)" @change="syncSku('group')">
            <option value="">选择商品规格</option>
            <option v-for="item in skuOptions" :key="item.id" :value="item.id">{{ item.label }}</option>
          </select>
        </FormField>
        <FormField label="拼团价" required :error="groupErrors.groupPrice">
          <input v-model.number="groupForm.groupPrice" type="number" step="0.01" :class="inputClass(groupErrors.groupPrice)" />
        </FormField>
        <FormField label="成团人数" required :error="groupErrors.requireNum">
          <input v-model.number="groupForm.requireNum" type="number" min="2" :class="inputClass(groupErrors.requireNum)" />
        </FormField>
        <FormField label="开始时间" required :error="groupErrors.time">
          <input v-model="groupForm.startTime" type="datetime-local" :class="inputClass(groupErrors.time)" />
        </FormField>
        <FormField label="结束时间" required :error="groupErrors.time">
          <input v-model="groupForm.endTime" type="datetime-local" :class="inputClass(groupErrors.time)" />
        </FormField>
        <button class="h-11 rounded-full bg-teal-600 px-6 text-white">保存拼团</button>
      </form>
      <div class="admin-card divide-y divide-slate-100">
        <div v-for="item in groups" :key="item.id" class="flex items-center justify-between px-5 py-4">
          <div>
            <div class="font-medium">{{ item.activityName }} · {{ item.requireNum }} 人 ¥{{ item.groupPrice }}</div>
            <div class="text-xs text-mute">{{ item.startTime }} ~ {{ item.endTime }}</div>
          </div>
          <div class="space-x-2">
            <button class="admin-btn" @click="fill(groupForm, item)">编辑</button>
            <button class="admin-btn" @click="groupStatus(item.id, item.status === 1 ? 0 : 1).then(load)">
              {{ item.status === 1 ? '下线' : '上线' }}
            </button>
          </div>
        </div>
        <div v-if="!groups.length" class="px-5 py-8 text-center text-sm text-mute">还没有本店拼团</div>
      </div>
    </section>

    <section v-else class="space-y-4">
      <form class="admin-card grid grid-cols-2 gap-4 p-5" @submit.prevent="submitCoupon">
        <FormField label="券名称" required :error="couponErrors.couponName">
          <input v-model="couponForm.couponName" :class="inputClass(couponErrors.couponName)" maxlength="30" />
        </FormField>
        <FormField label="优惠方式">
          <select v-model.number="couponForm.discountType" class="admin-select">
            <option :value="1">满减金额</option>
            <option :value="2">折扣</option>
          </select>
        </FormField>
        <FormField :label="couponForm.discountType === 2 ? '折扣（80=8折）' : '减免金额'" required :error="couponErrors.discountValue">
          <input v-model.number="couponForm.discountValue" type="number" step="0.01" :class="inputClass(couponErrors.discountValue)" />
        </FormField>
        <FormField label="使用门槛" :error="couponErrors.minAmount">
          <input v-model.number="couponForm.minAmount" type="number" step="0.01" :class="inputClass(couponErrors.minAmount)" />
        </FormField>
        <FormField label="开始领取" required :error="couponErrors.time">
          <input v-model="couponForm.startTime" type="datetime-local" :class="inputClass(couponErrors.time)" />
        </FormField>
        <FormField label="结束领取" required :error="couponErrors.time">
          <input v-model="couponForm.endTime" type="datetime-local" :class="inputClass(couponErrors.time)" />
        </FormField>
        <button class="h-11 rounded-full bg-teal-600 px-6 text-white">保存优惠券</button>
      </form>
      <div class="admin-card divide-y divide-slate-100">
        <div v-for="item in coupons" :key="item.id" class="flex items-center justify-between px-5 py-4">
          <div>
            <div class="font-medium">{{ item.couponName }}</div>
            <div class="text-xs text-mute">门槛 {{ item.minAmount || 0 }} · 已领 {{ item.receivedCount || 0 }}</div>
          </div>
          <div class="space-x-2">
            <button class="admin-btn" @click="fill(couponForm, item)">编辑</button>
            <button class="admin-btn" @click="couponStatus(item.id, item.status === 1 ? 0 : 1).then(load)">
              {{ item.status === 1 ? '停用' : '启用' }}
            </button>
          </div>
        </div>
      </div>
    </section>
  </MerchantPage>
</template>
