<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useMessage } from 'naive-ui'
import { deleteAddress, listAddress, saveAddress, setDefaultAddress, updateAddress } from '@/api/address'
import type { Address } from '@/api/types'
import { useAuthStore } from '@/stores/auth'
import EmptyState from '@/components/EmptyState.vue'
import FormField from '@/components/FormField.vue'
import PageHeader from '@/components/PageHeader.vue'

const auth = useAuthStore()
const message = useMessage()
const list = ref<Address[]>([])
const editingId = ref<number | string | null>(null)
const saving = ref(false)
const form = reactive({
  receiverName: '',
  receiverPhone: '',
  province: '',
  city: '',
  district: '',
  detailAddress: '',
  isDefault: 0,
})

function resetForm() {
  editingId.value = null
  form.receiverName = ''
  form.receiverPhone = ''
  form.province = ''
  form.city = ''
  form.district = ''
  form.detailAddress = ''
  form.isDefault = 0
}

async function load() {
  if (!auth.userId) return
  list.value = (await listAddress(auth.userId)) || []
}

function edit(item: Address) {
  editingId.value = item.id
  form.receiverName = item.receiverName
  form.receiverPhone = item.receiverPhone
  form.province = item.province
  form.city = item.city
  form.district = item.district
  form.detailAddress = item.detailAddress
  form.isDefault = item.isDefault || 0
}

async function submit() {
  if (!auth.userId) return
  if (!/^1[3-9]\d{9}$/.test(form.receiverPhone)) {
    message.error('请填写正确的手机号')
    return
  }
  saving.value = true
  try {
    if (editingId.value) {
      await updateAddress(auth.userId, editingId.value, form)
      message.success('地址已更新')
    } else {
      await saveAddress(auth.userId, form)
      message.success('地址已保存')
    }
    resetForm()
    await load()
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>

<template>
  <div>
    <PageHeader title="收货地址" extra="结算时会默认选中默认地址，也可以在这里改已有地址" />
    <div class="grid grid-cols-2 gap-6">
      <div class="space-y-3">
        <EmptyState v-if="!list.length" title="还没有地址" extra="右侧表单填写后保存" />
        <article v-for="item in list" :key="item.id" class="rounded-card bg-white p-5 shadow-card">
          <div class="flex items-start justify-between gap-3">
            <div>
              <div class="font-medium">{{ item.receiverName }} {{ item.receiverPhone }}</div>
              <div class="mt-1 text-sm text-mute">
                {{ item.province }}{{ item.city }}{{ item.district }}{{ item.detailAddress }}
              </div>
            </div>
            <span v-if="item.isDefault === 1" class="rounded-full bg-sky-50 px-2 py-0.5 text-xs text-brand">默认</span>
          </div>
          <div class="mt-4 flex flex-wrap gap-2 text-sm">
            <button class="shop-btn" @click="edit(item)">编辑</button>
            <button
              class="shop-btn"
              @click="auth.userId && setDefaultAddress(auth.userId, item.id).then(() => { message.success('已设为默认'); load() })"
            >
              设为默认
            </button>
            <button
              class="text-price"
              @click="auth.userId && deleteAddress(auth.userId, item.id).then(() => { message.success('已删除'); if (editingId === item.id) resetForm(); load() })"
            >
              删除
            </button>
          </div>
        </article>
      </div>
      <form class="h-fit space-y-4 rounded-card bg-white p-6 shadow-card" @submit.prevent="submit">
        <h2 class="text-lg font-semibold">{{ editingId ? '编辑地址' : '新增地址' }}</h2>
        <FormField label="收货人" required>
          <input v-model="form.receiverName" class="shop-input" placeholder="姓名" required maxlength="20" />
        </FormField>
        <FormField label="手机号" required hint="用于快递联系">
          <input v-model="form.receiverPhone" class="shop-input" placeholder="11 位手机号" required maxlength="11" />
        </FormField>
        <div class="grid grid-cols-3 gap-2">
          <FormField label="省" required>
            <input v-model="form.province" class="shop-input" placeholder="省" required />
          </FormField>
          <FormField label="市" required>
            <input v-model="form.city" class="shop-input" placeholder="市" required />
          </FormField>
          <FormField label="区" required>
            <input v-model="form.district" class="shop-input" placeholder="区" required />
          </FormField>
        </div>
        <FormField label="详细地址" required>
          <textarea v-model="form.detailAddress" class="shop-textarea" placeholder="街道、门牌号" required />
        </FormField>
        <label class="flex items-center gap-2 text-sm">
          <input v-model="form.isDefault" type="checkbox" :true-value="1" :false-value="0" />
          设为默认收货地址
        </label>
        <div class="flex gap-2">
          <button type="submit" class="h-11 flex-1 rounded-full bg-brand text-white" :disabled="saving">
            {{ saving ? '保存中...' : editingId ? '保存修改' : '保存地址' }}
          </button>
          <button v-if="editingId" type="button" class="h-11 rounded-full border px-4" @click="resetForm">取消</button>
        </div>
      </form>
    </div>
  </div>
</template>
