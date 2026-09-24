<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useMessage } from 'naive-ui'
import { configList, deleteConfig, saveConfig } from '@/api'
import AdminPage from '@/components/AdminPage.vue'
import FormField from '@/components/FormField.vue'
import { codeValue, firstError, inputClass, maxLen, required } from '@/utils/validate'

const message = useMessage()
const list = ref<any[]>([])
const errors = reactive({ configName: '', configKey: '', configValue: '' })
const form = reactive({ id: '' as string | number | '', configName: '', configKey: '', configValue: '' })

async function load() {
  list.value = (await configList()) || []
}

function reset() {
  Object.assign(form, { id: '', configName: '', configKey: '', configValue: '' })
  Object.assign(errors, { configName: '', configKey: '', configValue: '' })
}

function edit(item: any) {
  Object.assign(form, {
    id: item.id,
    configName: item.configName || '',
    configKey: item.configKey || '',
    configValue: item.configValue || '',
  })
}

async function save() {
  Object.assign(errors, {
    configName: required(form.configName, '名称') || maxLen(form.configName, 30, '名称'),
    configKey: codeValue(form.configKey, '配置键'),
    configValue: required(form.configValue, '配置值') || maxLen(form.configValue, 200, '配置值'),
  })
  const error = firstError(errors)
  if (error) {
    message.error(error)
    return
  }
  await saveConfig({ ...form, id: form.id || undefined })
  message.success('配置已保存')
  reset()
  await load()
}

async function remove(item: any) {
  if (!window.confirm(`确定删除配置 ${item.configKey}？`)) return
  await deleteConfig(item.id)
  message.success('已删除')
  await load()
}

onMounted(load)
</script>

<template>
  <AdminPage title="系统配置" extra="键值配置，供业务开关使用。例如支付、短信开关。">
    <form class="admin-card grid grid-cols-1 gap-4 p-5 xl:grid-cols-4" @submit.prevent="save">
      <FormField label="名称" required :error="errors.configName">
        <input v-model="form.configName" :class="inputClass(errors.configName)" maxlength="30" />
      </FormField>
      <FormField label="键" required :error="errors.configKey">
        <input v-model="form.configKey" :class="inputClass(errors.configKey)" maxlength="32" placeholder="alipay.enabled" />
      </FormField>
      <FormField label="值" required :error="errors.configValue">
        <input v-model="form.configValue" :class="inputClass(errors.configValue)" maxlength="200" />
      </FormField>
      <button class="mt-7 h-11 rounded-full bg-sky-500 text-white">{{ form.id ? '更新' : '保存' }}</button>
    </form>
    <div class="admin-card divide-y divide-slate-100">
      <div v-for="item in list" :key="item.id" class="flex items-center justify-between px-5 py-4">
        <div>
          <div class="font-medium">{{ item.configName }}</div>
          <div class="text-xs text-mute">{{ item.configKey }} = {{ item.configValue }}</div>
        </div>
        <div class="space-x-2">
          <button class="admin-btn" @click="edit(item)">编辑</button>
          <button class="admin-btn-danger" @click="remove(item)">删除</button>
        </div>
      </div>
      <div v-if="!list.length" class="px-5 py-10 text-center text-sm text-mute">暂无配置</div>
    </div>
  </AdminPage>
</template>
