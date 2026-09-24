<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useMessage } from 'naive-ui'
import { brandList, deleteBrand, saveBrand } from '@/api'
import AdminPage from '@/components/AdminPage.vue'
import FormField from '@/components/FormField.vue'
import { firstError, inputClass, maxLen, minLen, required } from '@/utils/validate'

const message = useMessage()
const list = ref<any[]>([])
const name = ref('')
const errors = reactive({ name: '' })

async function load() {
  list.value = (await brandList()) || []
}

async function create() {
  errors.name = required(name.value, '品牌名') || minLen(name.value, 2, '品牌名') || maxLen(name.value, 20, '品牌名')
  const error = firstError(errors)
  if (error) {
    message.error(error)
    return
  }
  await saveBrand({ brandName: name.value.trim() })
  message.success('品牌已新增')
  name.value = ''
  await load()
}

async function remove(item: any) {
  if (!window.confirm(`确定删除品牌「${item.brandName}」？`)) return
  await deleteBrand(item.id)
  message.success('已删除')
  await load()
}

onMounted(load)
</script>

<template>
  <AdminPage title="品牌" extra="品牌会出现在商品筛选和详情中">
    <form class="admin-card flex flex-wrap items-end gap-3 p-5" @submit.prevent="create">
      <FormField class="min-w-[240px] flex-1" label="品牌名" required :error="errors.name">
        <input v-model="name" :class="inputClass(errors.name)" placeholder="例如：Stratos" maxlength="20" />
      </FormField>
      <button class="h-11 shrink-0 rounded-full bg-sky-500 px-6 text-white">新增</button>
    </form>
    <div class="admin-card divide-y divide-slate-100">
      <div v-for="item in list" :key="item.id" class="flex items-center justify-between px-5 py-4">
        <span class="font-medium">{{ item.brandName }}</span>
        <button class="admin-btn-danger" @click="remove(item)">删除</button>
      </div>
      <div v-if="!list.length" class="px-5 py-10 text-center text-sm text-mute">暂无品牌</div>
    </div>
  </AdminPage>
</template>
