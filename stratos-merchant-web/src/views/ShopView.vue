<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useMessage } from 'naive-ui'
import { getShop, updateShop } from '@/api'
import { useAuthStore } from '@/stores/auth'
import FormField from '@/components/FormField.vue'
import MerchantPage from '@/components/MerchantPage.vue'
import ProductImagePicker from '@/components/ProductImagePicker.vue'
import { firstError, inputClass, maxLen, minLen, personName, phone, required, selectClass, textareaClass } from '@/utils/validate'

const auth = useAuthStore()
const message = useMessage()
const saving = ref(false)
const form = reactive({
  merchantId: auth.merchantId,
  shopName: '',
  shopLogo: '',
  shopDesc: '',
  contactName: '',
  contactPhone: '',
  province: '',
  city: '',
  district: '',
  detailAddress: '',
  status: 1,
})
const errors = reactive({
  shopName: '',
  shopDesc: '',
  contactName: '',
  contactPhone: '',
  province: '',
  city: '',
  district: '',
  detailAddress: '',
})

async function load() {
  if (!auth.merchantId) return
  const shop = await getShop(auth.merchantId)
  Object.assign(form, shop, {
    merchantId: auth.merchantId,
    shopLogo: shop?.shopLogo && shop.shopLogo !== '/placeholder.svg' ? shop.shopLogo : '',
  })
}

function validate() {
  errors.shopName = required(form.shopName, '店铺名称') || minLen(form.shopName, 2, '店铺名称') || maxLen(form.shopName, 30, '店铺名称')
  errors.shopDesc = maxLen(form.shopDesc, 200, '店铺简介')
  errors.contactName = personName(form.contactName, '联系人', true)
  errors.contactPhone = phone(form.contactPhone, '联系电话', true)
  const hasAddress = [form.province, form.city, form.district, form.detailAddress].some((item) => String(item || '').trim())
  if (hasAddress) {
    errors.province = required(form.province, '省')
    errors.city = required(form.city, '市')
    errors.district = required(form.district, '区')
    errors.detailAddress = required(form.detailAddress, '详细地址') || maxLen(form.detailAddress, 80, '详细地址')
  } else {
    errors.province = ''
    errors.city = ''
    errors.district = ''
    errors.detailAddress = ''
  }
  const tip = firstError(errors)
  if (tip) message.error(tip)
  return !tip
}

async function save() {
  if (!validate()) return
  saving.value = true
  try {
    await updateShop({ ...form, shopLogo: form.shopLogo || '/placeholder.svg' })
    message.success('店铺已保存')
    await load()
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>

<template>
  <MerchantPage title="店铺资料" extra="买家会在商品详情看到店铺名称、简介和联系方式">
    <form class="admin-card grid grid-cols-2 gap-5 p-6" @submit.prevent="save">
      <div class="col-span-2">
        <ProductImagePicker v-model="form.shopLogo" label="店铺 Logo" />
      </div>
      <FormField label="店铺名称" required :error="errors.shopName">
        <input v-model="form.shopName" :class="inputClass(errors.shopName)" maxlength="30" />
      </FormField>
      <FormField label="营业状态">
        <select v-model.number="form.status" :class="selectClass()">
          <option :value="1">营业中</option>
          <option :value="0">休息</option>
        </select>
      </FormField>
      <FormField label="店铺简介" class="col-span-2" hint="一句话介绍本店，会展示给买家" :error="errors.shopDesc">
        <textarea v-model="form.shopDesc" :class="textareaClass(errors.shopDesc)" maxlength="200" placeholder="例如：正品数码，当日发货" />
      </FormField>
      <FormField label="联系人" :error="errors.contactName">
        <input v-model="form.contactName" :class="inputClass(errors.contactName)" maxlength="20" />
      </FormField>
      <FormField label="联系电话" hint="11 位手机号" :error="errors.contactPhone">
        <input v-model="form.contactPhone" :class="inputClass(errors.contactPhone)" maxlength="11" />
      </FormField>
      <FormField label="省" :error="errors.province">
        <input v-model="form.province" :class="inputClass(errors.province)" maxlength="20" />
      </FormField>
      <FormField label="市" :error="errors.city">
        <input v-model="form.city" :class="inputClass(errors.city)" maxlength="20" />
      </FormField>
      <FormField label="区" :error="errors.district">
        <input v-model="form.district" :class="inputClass(errors.district)" maxlength="20" />
      </FormField>
      <FormField label="详细地址" :error="errors.detailAddress">
        <input v-model="form.detailAddress" :class="inputClass(errors.detailAddress)" maxlength="80" />
      </FormField>
      <div class="col-span-2">
        <button type="submit" class="h-11 rounded-full bg-teal-600 px-6 text-white" :disabled="saving">
          {{ saving ? '保存中...' : '保存店铺' }}
        </button>
      </div>
    </form>
  </MerchantPage>
</template>
