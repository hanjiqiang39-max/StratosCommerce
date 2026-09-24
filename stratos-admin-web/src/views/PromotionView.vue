<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useMessage } from 'naive-ui'
import type { EChartsOption } from 'echarts'
import {
  couponStatus,
  discountStatus,
  getProduct,
  groupStatus,
  productList,
  promoCoupon,
  promoDiscount,
  promoGroup,
  promoSeckill,
  saveCoupon,
  saveDiscount,
  saveGroup,
  saveSeckill,
  seckillStatus,
} from '@/api'
import AdminPage from '@/components/AdminPage.vue'
import ChartBox from '@/components/ChartBox.vue'
import FormField from '@/components/FormField.vue'
import { dateOrder, firstError, inputClass, maxLen, minLen, numberRange, required, selectClass } from '@/utils/validate'

const message = useMessage()
const tab = ref<'seckill' | 'coupon' | 'group' | 'discount'>('seckill')
const seckills = ref<any[]>([])
const coupons = ref<any[]>([])
const groups = ref<any[]>([])
const discounts = ref<any[]>([])
const products = ref<any[]>([])
const skus = ref<any[]>([])
const seckillErrors = reactive<Record<string, string>>({})
const couponErrors = reactive<Record<string, string>>({})
const groupErrors = reactive<Record<string, string>>({})
const discountErrors = reactive<Record<string, string>>({})

const seckillForm = reactive(emptySeckill())
const couponForm = reactive(emptyCoupon())
const groupForm = reactive(emptyGroup())
const discountForm = reactive(emptyDiscount())

function emptySeckill() {
  return {
    id: '' as string | number | '',
    activityName: '',
    spuId: '',
    skuId: '',
    originalPrice: 129,
    seckillPrice: 9.9,
    seckillStock: 50,
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
    publishCount: 1000,
    limitPerUser: 1,
    validDays: 7,
    startTime: '',
    endTime: '',
    status: 1,
  }
}

function emptyGroup() {
  return {
    id: '' as string | number | '',
    activityName: '',
    spuId: '',
    skuId: '',
    originalPrice: 129,
    groupPrice: 79,
    requireNum: 2,
    limitHours: 24,
    limitPerUser: 1,
    startTime: '',
    endTime: '',
    status: 1,
  }
}

function emptyDiscount() {
  return {
    id: '' as string | number | '',
    activityName: '',
    fullAmount: 99,
    discountAmount: 10,
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

function assign(target: Record<string, any>, source: Record<string, any>) {
  Object.keys(target).forEach((key) => {
    if (key.endsWith('Time')) target[key] = toInput(source[key])
    else target[key] = source[key] ?? target[key]
  })
}

const option = computed<EChartsOption>(() => ({
  color: ['#f43f5e', '#0ea5e9', '#f59e0b', '#8b5cf6'],
  tooltip: { trigger: 'axis' },
  grid: { left: 36, right: 16, top: 16, bottom: 28 },
  xAxis: { type: 'category', data: ['秒杀', '拼团', '优惠券', '满减'] },
  yAxis: { type: 'value', minInterval: 1 },
  series: [{ type: 'bar', barWidth: 28, data: [seckills.value.length, groups.value.length, coupons.value.length, discounts.value.length] }],
}))

async function load() {
  seckills.value = (await promoSeckill()) || []
  coupons.value = (await promoCoupon()) || []
  groups.value = (await promoGroup()) || []
  discounts.value = (await promoDiscount()) || []
  const page = await productList({ pageNum: 1, pageSize: 50, status: 1 })
  products.value = page?.records || []
}

async function pickProduct(spuId: string | number, kind: 'seckill' | 'group') {
  if (!spuId) {
    skus.value = []
    return
  }
  const detail = await getProduct(spuId)
  skus.value = detail?.skuList || []
  const first = skus.value[0]
  if (kind === 'seckill') {
    seckillForm.spuId = String(spuId)
    if (first) {
      seckillForm.skuId = String(first.id)
      seckillForm.originalPrice = Number(first.originalPrice || first.price || seckillForm.originalPrice)
    }
  } else {
    groupForm.spuId = String(spuId)
    if (first) {
      groupForm.skuId = String(first.id)
      groupForm.originalPrice = Number(first.originalPrice || first.price || groupForm.originalPrice)
    }
  }
}

async function submitSeckill() {
  Object.assign(seckillErrors, {
    activityName: required(seckillForm.activityName, '活动名称') || minLen(seckillForm.activityName, 2, '活动名称') || maxLen(seckillForm.activityName, 40, '活动名称'),
    skuId: required(seckillForm.skuId, 'SKU'),
    seckillPrice: numberRange(seckillForm.seckillPrice, 0.01, 999999, '秒杀价'),
    seckillStock: numberRange(seckillForm.seckillStock, 1, 999999, '秒杀库存', true),
    time: dateOrder(seckillForm.startTime, seckillForm.endTime),
  })
  if (seckillForm.seckillPrice >= seckillForm.originalPrice) seckillErrors.seckillPrice = '秒杀价必须低于原价'
  const error = firstError(seckillErrors)
  if (error) {
    message.error(error)
    return
  }
  await saveSeckill({
    ...seckillForm,
    id: seckillForm.id || undefined,
    startTime: fromInput(seckillForm.startTime),
    endTime: fromInput(seckillForm.endTime),
  })
  message.success('秒杀已保存')
  Object.assign(seckillForm, emptySeckill())
  await load()
}

async function submitCoupon() {
  Object.assign(couponErrors, {
    couponName: required(couponForm.couponName, '券名称') || minLen(couponForm.couponName, 2, '券名称') || maxLen(couponForm.couponName, 30, '券名称'),
    discountValue: couponForm.discountType === 2
      ? numberRange(couponForm.discountValue, 1, 99, '折扣')
      : numberRange(couponForm.discountValue, 0.01, 999999, '减免金额'),
    minAmount: numberRange(couponForm.minAmount, 0, 999999, '使用门槛'),
    time: dateOrder(couponForm.startTime, couponForm.endTime, '开始领取', '结束领取'),
  })
  if (couponForm.discountType === 1 && couponForm.discountValue >= couponForm.minAmount && couponForm.minAmount > 0) {
    couponErrors.discountValue = '减免金额必须小于使用门槛'
  }
  const error = firstError(couponErrors)
  if (error) {
    message.error(error)
    return
  }
  await saveCoupon({
    ...couponForm,
    id: couponForm.id || undefined,
    couponType: couponForm.discountType === 2 ? 2 : 1,
    startTime: fromInput(couponForm.startTime),
    endTime: fromInput(couponForm.endTime),
  })
  message.success('优惠券已保存')
  Object.assign(couponForm, emptyCoupon())
  await load()
}

async function submitGroup() {
  Object.assign(groupErrors, {
    activityName: required(groupForm.activityName, '活动名称') || minLen(groupForm.activityName, 2, '活动名称'),
    skuId: required(groupForm.skuId, 'SKU'),
    groupPrice: numberRange(groupForm.groupPrice, 0.01, 999999, '拼团价'),
    requireNum: numberRange(groupForm.requireNum, 2, 50, '成团人数', true),
    time: dateOrder(groupForm.startTime, groupForm.endTime),
  })
  if (groupForm.groupPrice >= groupForm.originalPrice) groupErrors.groupPrice = '拼团价必须低于原价'
  const error = firstError(groupErrors)
  if (error) {
    message.error(error)
    return
  }
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

async function submitDiscount() {
  Object.assign(discountErrors, {
    activityName: required(discountForm.activityName, '满减名称'),
    fullAmount: numberRange(discountForm.fullAmount, 0.01, 999999, '满额'),
    discountAmount: numberRange(discountForm.discountAmount, 0.01, 999999, '减免金额'),
    time: dateOrder(discountForm.startTime, discountForm.endTime),
  })
  if (discountForm.discountAmount >= discountForm.fullAmount) discountErrors.discountAmount = '减免必须小于满额'
  const error = firstError(discountErrors)
  if (error) {
    message.error(error)
    return
  }
  await saveDiscount({
    ...discountForm,
    id: discountForm.id || undefined,
    startTime: fromInput(discountForm.startTime),
    endTime: fromInput(discountForm.endTime),
  })
  message.success('满减已保存')
  Object.assign(discountForm, emptyDiscount())
  await load()
}

onMounted(load)
</script>

<template>
  <AdminPage title="营销活动" extra="创建秒杀、拼团、优惠券后，商城即可领取和下单">
    <div class="admin-card p-5">
      <div class="mb-2 font-medium">活动数量</div>
      <ChartBox :option="option" height="220px" />
    </div>
    <div class="flex gap-2">
      <button
        v-for="item in [
          { id: 'seckill', label: `秒杀 ${seckills.length}` },
          { id: 'coupon', label: `优惠券 ${coupons.length}` },
          { id: 'group', label: `拼团 ${groups.length}` },
          { id: 'discount', label: `满减 ${discounts.length}` },
        ]"
        :key="item.id"
        class="h-9 rounded-full px-4 text-sm"
        :class="tab === item.id ? 'bg-sky-500 text-white' : 'bg-white'"
        @click="tab = item.id as any"
      >
        {{ item.label }}
      </button>
    </div>

    <section v-if="tab === 'seckill'" class="space-y-4">
      <form class="admin-card grid grid-cols-2 gap-4 p-5 xl:grid-cols-3" @submit.prevent="submitSeckill">
        <FormField label="活动名称" required :error="seckillErrors.activityName">
          <input v-model="seckillForm.activityName" :class="inputClass(seckillErrors.activityName)" maxlength="40" />
        </FormField>
        <FormField label="商品" required>
          <select class="admin-select" :value="seckillForm.spuId" @change="pickProduct(($event.target as HTMLSelectElement).value, 'seckill')">
            <option value="">选择商品</option>
            <option v-for="item in products" :key="item.id" :value="item.id">{{ item.title || item.spuName }}</option>
          </select>
        </FormField>
        <FormField label="SKU" required :error="seckillErrors.skuId">
          <select v-model="seckillForm.skuId" :class="selectClass(seckillErrors.skuId)">
            <option value="">选择 SKU</option>
            <option v-for="item in skus" :key="item.id" :value="item.id">{{ item.skuName || item.id }} / {{ item.price }}</option>
          </select>
        </FormField>
        <FormField label="秒杀价" required :error="seckillErrors.seckillPrice">
          <input v-model.number="seckillForm.seckillPrice" type="number" step="0.01" :class="inputClass(seckillErrors.seckillPrice)" />
        </FormField>
        <FormField label="秒杀库存" required :error="seckillErrors.seckillStock">
          <input v-model.number="seckillForm.seckillStock" type="number" :class="inputClass(seckillErrors.seckillStock)" />
        </FormField>
        <FormField label="每人限购">
          <input v-model.number="seckillForm.limitPerUser" type="number" class="admin-input" />
        </FormField>
        <FormField label="开始时间" required :error="seckillErrors.time">
          <input v-model="seckillForm.startTime" type="datetime-local" :class="inputClass(seckillErrors.time)" />
        </FormField>
        <FormField label="结束时间" required :error="seckillErrors.time">
          <input v-model="seckillForm.endTime" type="datetime-local" :class="inputClass(seckillErrors.time)" />
        </FormField>
        <button class="h-11 rounded-full bg-sky-500 px-6 text-white">{{ seckillForm.id ? '更新秒杀' : '创建秒杀' }}</button>
      </form>
      <div class="admin-card">
        <table class="admin-table">
          <thead>
            <tr><th>活动</th><th>价格</th><th>库存</th><th>时间</th><th></th></tr>
          </thead>
          <tbody>
            <tr v-for="item in seckills" :key="item.id">
              <td>{{ item.activityName }}</td>
              <td>{{ item.seckillPrice }} / {{ item.originalPrice }}</td>
              <td>{{ item.seckillStock }} / 已售 {{ item.soldCount || 0 }}</td>
              <td class="text-xs text-mute">{{ item.startTime }} ~ {{ item.endTime }}</td>
              <td class="space-x-2">
                <button class="admin-btn" @click="assign(seckillForm, item); pickProduct(item.spuId, 'seckill')">编辑</button>
                <button class="admin-btn" @click="seckillStatus(item.id, item.status === 1 ? 3 : 1).then(load)">
                  {{ item.status === 1 ? '下线' : '上线' }}
                </button>
              </td>
            </tr>
            <tr v-if="!seckills.length">
              <td colspan="5" class="py-10 text-center text-mute">还没有秒杀活动</td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>

    <section v-else-if="tab === 'coupon'" class="space-y-4">
      <form class="admin-card grid grid-cols-2 gap-4 p-5 xl:grid-cols-3" @submit.prevent="submitCoupon">
        <FormField label="券名称" required :error="couponErrors.couponName">
          <input v-model="couponForm.couponName" :class="inputClass(couponErrors.couponName)" maxlength="30" />
        </FormField>
        <FormField label="优惠方式">
          <select v-model.number="couponForm.discountType" class="admin-select">
            <option :value="1">满减/代金</option>
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
        <button class="h-11 rounded-full bg-sky-500 px-6 text-white">{{ couponForm.id ? '更新优惠券' : '创建优惠券' }}</button>
      </form>
      <div class="admin-card divide-y divide-slate-100">
        <div v-for="item in coupons" :key="item.id" class="flex items-center justify-between px-5 py-4">
          <div>
            <div class="font-medium">{{ item.couponName }}</div>
            <div class="text-xs text-mute">门槛 {{ item.minAmount || 0 }} · 已领 {{ item.receivedCount || 0 }} / {{ item.publishCount }}</div>
          </div>
          <div class="space-x-2">
            <button class="admin-btn" @click="assign(couponForm, item)">编辑</button>
            <button class="admin-btn" @click="couponStatus(item.id, item.status === 1 ? 0 : 1).then(load)">
              {{ item.status === 1 ? '停用' : '启用' }}
            </button>
          </div>
        </div>
        <div v-if="!coupons.length" class="px-5 py-8 text-center text-sm text-mute">还没有优惠券</div>
      </div>
    </section>

    <section v-else-if="tab === 'group'" class="space-y-4">
      <form class="admin-card grid grid-cols-2 gap-4 p-5 xl:grid-cols-3" @submit.prevent="submitGroup">
        <FormField label="活动名称" required :error="groupErrors.activityName">
          <input v-model="groupForm.activityName" :class="inputClass(groupErrors.activityName)" maxlength="40" />
        </FormField>
        <FormField label="商品" required>
          <select class="admin-select" :value="groupForm.spuId" @change="pickProduct(($event.target as HTMLSelectElement).value, 'group')">
            <option value="">选择商品</option>
            <option v-for="item in products" :key="item.id" :value="item.id">{{ item.title || item.spuName }}</option>
          </select>
        </FormField>
        <FormField label="SKU" required :error="groupErrors.skuId">
          <select v-model="groupForm.skuId" :class="selectClass(groupErrors.skuId)">
            <option value="">选择 SKU</option>
            <option v-for="item in skus" :key="item.id" :value="item.id">{{ item.skuName || item.id }} / {{ item.price }}</option>
          </select>
        </FormField>
        <FormField label="拼团价" required :error="groupErrors.groupPrice">
          <input v-model.number="groupForm.groupPrice" type="number" step="0.01" :class="inputClass(groupErrors.groupPrice)" />
        </FormField>
        <FormField label="成团人数" required :error="groupErrors.requireNum">
          <input v-model.number="groupForm.requireNum" type="number" :class="inputClass(groupErrors.requireNum)" />
        </FormField>
        <FormField label="开始时间" required :error="groupErrors.time">
          <input v-model="groupForm.startTime" type="datetime-local" :class="inputClass(groupErrors.time)" />
        </FormField>
        <FormField label="结束时间" required :error="groupErrors.time">
          <input v-model="groupForm.endTime" type="datetime-local" :class="inputClass(groupErrors.time)" />
        </FormField>
        <button class="h-11 rounded-full bg-sky-500 px-6 text-white">{{ groupForm.id ? '更新拼团' : '创建拼团' }}</button>
      </form>
      <div class="admin-card divide-y divide-slate-100">
        <div v-for="item in groups" :key="item.id" class="flex items-center justify-between px-5 py-4">
          <div>
            <div class="font-medium">{{ item.activityName }} · {{ item.requireNum }} 人 {{ item.groupPrice }} 元</div>
            <div class="text-xs text-mute">{{ item.startTime }} ~ {{ item.endTime }}</div>
          </div>
          <div class="space-x-2">
            <button class="admin-btn" @click="assign(groupForm, item); pickProduct(item.spuId, 'group')">编辑</button>
            <button class="admin-btn" @click="groupStatus(item.id, item.status === 1 ? 0 : 1).then(load)">
              {{ item.status === 1 ? '下线' : '上线' }}
            </button>
          </div>
        </div>
        <div v-if="!groups.length" class="px-5 py-8 text-center text-sm text-mute">还没有拼团活动</div>
      </div>
    </section>

    <section v-else class="space-y-4">
      <form class="admin-card grid grid-cols-2 gap-4 p-5 xl:grid-cols-3" @submit.prevent="submitDiscount">
        <FormField label="满减名称" required :error="discountErrors.activityName">
          <input v-model="discountForm.activityName" :class="inputClass(discountErrors.activityName)" />
        </FormField>
        <FormField label="满多少" required :error="discountErrors.fullAmount">
          <input v-model.number="discountForm.fullAmount" type="number" step="0.01" :class="inputClass(discountErrors.fullAmount)" />
        </FormField>
        <FormField label="减多少" required :error="discountErrors.discountAmount">
          <input v-model.number="discountForm.discountAmount" type="number" step="0.01" :class="inputClass(discountErrors.discountAmount)" />
        </FormField>
        <FormField label="开始时间" required :error="discountErrors.time">
          <input v-model="discountForm.startTime" type="datetime-local" :class="inputClass(discountErrors.time)" />
        </FormField>
        <FormField label="结束时间" required :error="discountErrors.time">
          <input v-model="discountForm.endTime" type="datetime-local" :class="inputClass(discountErrors.time)" />
        </FormField>
        <button class="h-11 rounded-full bg-sky-500 px-6 text-white">保存满减</button>
      </form>
      <div class="admin-card divide-y divide-slate-100">
        <div v-for="item in discounts" :key="item.id" class="flex items-center justify-between px-5 py-4 text-sm">
          <span>{{ item.activityName }} · 满 {{ item.fullAmount }} 减 {{ item.discountAmount }}</span>
          <button class="admin-btn" @click="discountStatus(item.id, item.status === 1 ? 0 : 1).then(load)">
            {{ item.status === 1 ? '停用' : '启用' }}
          </button>
        </div>
        <div v-if="!discounts.length" class="px-5 py-8 text-center text-sm text-mute">还没有满减活动</div>
      </div>
    </section>
  </AdminPage>
</template>
