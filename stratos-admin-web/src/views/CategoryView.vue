<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { NCascader, useMessage } from 'naive-ui'
import { categoryTree, deleteCategory, saveCategory } from '@/api'
import AdminPage from '@/components/AdminPage.vue'
import FormField from '@/components/FormField.vue'
import { firstError, inputClass, maxLen, minLen, required } from '@/utils/validate'

const message = useMessage()
const list = ref<any[]>([])
const name = ref('')
const parentId = ref<number | null>(null)
const errors = reactive({ name: '' })

function flat(nodes: any[] = [], depth = 0): any[] {
  return nodes.flatMap((item) => [{ ...item, depth }, ...flat(item.children || [], depth + 1)])
}

function toOptions(nodes: any[] = []): any[] {
  return nodes.map((item) => ({
    label: item.categoryName,
    value: item.id,
    children: item.children?.length ? toOptions(item.children) : undefined,
  }))
}

const rows = computed(() => flat(list.value))
const options = computed(() => toOptions(list.value))

async function load() {
  list.value = (await categoryTree()) || []
}

async function create() {
  errors.name = required(name.value, '分类名称') || minLen(name.value, 2, '分类名称') || maxLen(name.value, 20, '分类名称')
  const error = firstError(errors)
  if (error) {
    message.error(error)
    return
  }
  await saveCategory({ categoryName: name.value.trim(), parentId: parentId.value || 0 })
  message.success('分类已新增')
  name.value = ''
  parentId.value = null
  await load()
}

async function remove(item: any) {
  if (!window.confirm(`确定删除分类「${item.categoryName}」？`)) return
  await deleteCategory(item.id)
  message.success('已删除')
  await load()
}

onMounted(load)
</script>

<template>
  <AdminPage title="分类" extra="先选父级再新增，不选父级就是一级分类。商城分类页会按这棵树展示。">
    <form class="admin-card grid grid-cols-1 gap-4 p-5 md:grid-cols-[1fr_280px_auto]" @submit.prevent="create">
      <FormField label="分类名称" required :error="errors.name">
        <input v-model="name" :class="inputClass(errors.name)" placeholder="例如：智能手机" maxlength="20" />
      </FormField>
      <FormField label="父级分类" hint="可留空，表示一级分类">
        <NCascader
          v-model:value="parentId"
          :options="options"
          placeholder="父级分类，可留空"
          check-strategy="all"
          :show-path="true"
          clearable
          class="w-full"
        />
      </FormField>
      <button class="mt-7 h-11 shrink-0 rounded-full bg-sky-500 px-6 text-white">新增分类</button>
    </form>
    <div class="admin-card">
      <table class="admin-table">
        <thead>
          <tr>
            <th>类目</th>
            <th>层级</th>
            <th>ID</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in rows" :key="item.id">
            <td>
              <span :style="{ paddingLeft: `${item.depth * 20}px` }">{{ item.categoryName }}</span>
            </td>
            <td>
              <span class="admin-badge admin-badge-mute">{{ item.depth === 0 ? '一级' : item.depth === 1 ? '二级' : '三级' }}</span>
            </td>
            <td class="text-mute">{{ item.id }}</td>
            <td>
              <button class="admin-btn-danger" @click="remove(item)">删除</button>
            </td>
          </tr>
          <tr v-if="!rows.length">
            <td colspan="4" class="py-10 text-center text-mute">暂无分类</td>
          </tr>
        </tbody>
      </table>
    </div>
  </AdminPage>
</template>
