<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useMessage } from 'naive-ui'
import { deleteDictData, deleteDictType, dictData, dictTypes, saveDictData, saveDictType } from '@/api'
import AdminPage from '@/components/AdminPage.vue'
import FormField from '@/components/FormField.vue'
import { codeValue, firstError, inputClass, maxLen, required } from '@/utils/validate'

const message = useMessage()
const list = ref<any[]>([])
const items = ref<any[]>([])
const current = ref('')
const typeErrors = reactive({ dictName: '', dictType: '' })
const dataErrors = reactive({ dictLabel: '', dictValue: '' })
const typeForm = reactive({ dictName: '', dictType: '' })
const dataForm = reactive({ dictLabel: '', dictValue: '', dictType: '', status: 1 })

async function load() {
  list.value = (await dictTypes()) || []
}

async function pick(type: string) {
  current.value = type
  dataForm.dictType = type
  items.value = (await dictData(type)) || []
}

async function saveType() {
  Object.assign(typeErrors, {
    dictName: required(typeForm.dictName, '字典名称') || maxLen(typeForm.dictName, 20, '字典名称'),
    dictType: codeValue(typeForm.dictType, '字典类型'),
  })
  const error = firstError(typeErrors)
  if (error) {
    message.error(error)
    return
  }
  await saveDictType({ ...typeForm })
  message.success('字典类型已保存')
  Object.assign(typeForm, { dictName: '', dictType: '' })
  await load()
}

async function saveData() {
  if (!current.value) {
    message.error('请先选择左侧字典类型')
    return
  }
  Object.assign(dataErrors, {
    dictLabel: required(dataForm.dictLabel, '标签') || maxLen(dataForm.dictLabel, 20, '标签'),
    dictValue: required(dataForm.dictValue, '值') || maxLen(dataForm.dictValue, 32, '值'),
  })
  const error = firstError(dataErrors)
  if (error) {
    message.error(error)
    return
  }
  await saveDictData({ ...dataForm, dictType: current.value })
  message.success('字典项已保存')
  Object.assign(dataForm, { dictLabel: '', dictValue: '', dictType: current.value, status: 1 })
  await pick(current.value)
}

async function removeType(item: any) {
  if (!window.confirm(`确定删除字典类型「${item.dictName}」？`)) return
  await deleteDictType(item.id)
  if (current.value === item.dictType) {
    current.value = ''
    items.value = []
  }
  message.success('已删除')
  await load()
}

async function removeData(item: any) {
  await deleteDictData(item.id)
  message.success('已删除')
  if (current.value) await pick(current.value)
}

onMounted(load)
</script>

<template>
  <AdminPage title="字典" extra="先建类型，再维护该类型下的枚举项">
    <div class="grid gap-4 md:grid-cols-2">
      <div class="space-y-3">
        <form class="admin-card grid grid-cols-1 gap-4 p-5" @submit.prevent="saveType">
          <FormField label="类型名称" required :error="typeErrors.dictName">
            <input v-model="typeForm.dictName" :class="inputClass(typeErrors.dictName)" maxlength="20" />
          </FormField>
          <FormField label="类型编码" required :error="typeErrors.dictType">
            <input v-model="typeForm.dictType" :class="inputClass(typeErrors.dictType)" maxlength="32" placeholder="order_status" />
          </FormField>
          <button class="h-11 rounded-full bg-sky-500 text-white">保存类型</button>
        </form>
        <div class="admin-card divide-y divide-slate-100">
          <div v-for="item in list" :key="item.id" class="flex items-center justify-between px-5 py-4">
            <button class="text-left hover:text-sky-600" :class="current === item.dictType ? 'text-sky-600' : ''" @click="pick(item.dictType)">
              <div class="font-medium">{{ item.dictName }}</div>
              <div class="text-xs text-mute">{{ item.dictType }}</div>
            </button>
            <button class="admin-btn-danger" @click="removeType(item)">删除</button>
          </div>
          <div v-if="!list.length" class="px-5 py-8 text-center text-sm text-mute">暂无字典类型</div>
        </div>
      </div>
      <section class="space-y-3">
        <form class="admin-card grid grid-cols-1 gap-4 p-5" @submit.prevent="saveData">
          <FormField label="字典标签" required :error="dataErrors.dictLabel">
            <input v-model="dataForm.dictLabel" :class="inputClass(dataErrors.dictLabel)" maxlength="20" :disabled="!current" />
          </FormField>
          <FormField label="字典值" required :error="dataErrors.dictValue">
            <input v-model="dataForm.dictValue" :class="inputClass(dataErrors.dictValue)" maxlength="32" :disabled="!current" />
          </FormField>
          <button class="h-11 rounded-full bg-sky-500 text-white" :disabled="!current">保存字典项</button>
        </form>
        <div class="admin-card divide-y divide-slate-100">
          <div v-for="item in items" :key="item.id" class="flex items-center justify-between px-5 py-4">
            <span>{{ item.dictLabel }} = {{ item.dictValue }}</span>
            <button class="admin-btn-danger" @click="removeData(item)">删除</button>
          </div>
          <div v-if="!current" class="px-5 py-8 text-center text-sm text-mute">请先点左侧类型</div>
          <div v-else-if="!items.length" class="px-5 py-8 text-center text-sm text-mute">该类型还没有字典项</div>
        </div>
      </section>
    </div>
  </AdminPage>
</template>
