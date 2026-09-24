<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useMessage } from 'naive-ui'
import { companyAdminList, deleteCompany, saveCompany } from '@/api'
import AdminPage from '@/components/AdminPage.vue'
import FormField from '@/components/FormField.vue'
import { codeValue, firstError, inputClass, maxLen, minLen, required } from '@/utils/validate'

const message = useMessage()
const list = ref<any[]>([])
const errors = reactive({ companyName: '', companyCode: '' })
const form = reactive({ id: '' as string | number | '', companyName: '', companyCode: '', status: 1 })

async function load() {
  list.value = (await companyAdminList()) || []
}

function reset() {
  Object.assign(form, { id: '', companyName: '', companyCode: '', status: 1 })
  Object.assign(errors, { companyName: '', companyCode: '' })
}

function edit(item: any) {
  Object.assign(form, {
    id: item.id,
    companyName: item.companyName || '',
    companyCode: item.companyCode || '',
    status: item.status ?? 1,
  })
}

async function save() {
  Object.assign(errors, {
    companyName: required(form.companyName, '公司名称') || minLen(form.companyName, 2, '公司名称') || maxLen(form.companyName, 20, '公司名称'),
    companyCode: codeValue(form.companyCode, '公司编码'),
  })
  const error = firstError(errors)
  if (error) {
    message.error(error)
    return
  }
  await saveCompany({ ...form, id: form.id || undefined, companyCode: String(form.companyCode).toUpperCase() })
  message.success('物流公司已保存')
  reset()
  await load()
}

async function remove(item: any) {
  if (!window.confirm(`确定删除「${item.companyName}」？`)) return
  await deleteCompany(item.id)
  message.success('已删除')
  await load()
}

onMounted(load)
</script>

<template>
  <AdminPage title="物流公司" extra="发货时会用到这些公司编码，请和快递 100 等查询接口保持一致">
    <form class="admin-card grid grid-cols-1 gap-4 p-5 md:grid-cols-[1fr_1fr_auto]" @submit.prevent="save">
      <FormField label="公司名称" required :error="errors.companyName">
        <input v-model="form.companyName" :class="inputClass(errors.companyName)" maxlength="20" placeholder="例如：顺丰" />
      </FormField>
      <FormField label="公司编码" required hint="如 SF、YTO" :error="errors.companyCode">
        <input v-model="form.companyCode" :class="inputClass(errors.companyCode)" maxlength="16" placeholder="SF" />
      </FormField>
      <button class="mt-7 h-11 rounded-full bg-sky-500 px-6 text-white">{{ form.id ? '更新' : '保存' }}</button>
    </form>
    <div class="admin-card divide-y divide-slate-100">
      <div v-for="item in list" :key="item.id" class="flex items-center justify-between px-5 py-4">
        <div>
          <div class="font-medium">{{ item.companyName }}</div>
          <div class="text-xs text-mute">{{ item.companyCode }}</div>
        </div>
        <div class="space-x-2">
          <button class="admin-btn" @click="edit(item)">编辑</button>
          <button class="admin-btn-danger" @click="remove(item)">删除</button>
        </div>
      </div>
      <div v-if="!list.length" class="px-5 py-10 text-center text-sm text-mute">暂无物流公司</div>
    </div>
  </AdminPage>
</template>
