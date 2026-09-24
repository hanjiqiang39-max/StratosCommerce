<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useMessage } from 'naive-ui'
import { deleteMenu, menuList, saveMenu } from '@/api'
import AdminPage from '@/components/AdminPage.vue'
import FormField from '@/components/FormField.vue'
import { firstError, inputClass, maxLen, required } from '@/utils/validate'

const message = useMessage()
const list = ref<any[]>([])
const errors = reactive({ menuName: '' })
const form = reactive({ id: '' as string | number | '', menuName: '', path: '', permission: '', parentId: 0, menuType: 1, status: 1 })

async function load() {
  list.value = (await menuList()) || []
}

function reset() {
  Object.assign(form, { id: '', menuName: '', path: '', permission: '', parentId: 0, menuType: 1, status: 1 })
  errors.menuName = ''
}

function edit(item: any) {
  Object.assign(form, {
    id: item.id,
    menuName: item.menuName || '',
    path: item.path || '',
    permission: item.permission || '',
    parentId: item.parentId || 0,
    menuType: item.menuType ?? 1,
    status: item.status ?? 1,
  })
}

async function save() {
  errors.menuName = required(form.menuName, '菜单名称') || maxLen(form.menuName, 20, '菜单名称')
  const error = firstError(errors)
  if (error) {
    message.error(error)
    return
  }
  await saveMenu({ ...form, id: form.id || undefined })
  message.success('菜单已保存')
  reset()
  await load()
}

async function remove(item: any) {
  if (!window.confirm(`确定删除菜单「${item.menuName}」？`)) return
  await deleteMenu(item.id)
  message.success('已删除')
  await load()
}

onMounted(load)
</script>

<template>
  <AdminPage title="菜单" extra="后台菜单和权限标识，供角色勾选">
    <form class="admin-card grid grid-cols-2 gap-4 p-5 xl:grid-cols-4" @submit.prevent="save">
      <FormField label="名称" required :error="errors.menuName">
        <input v-model="form.menuName" :class="inputClass(errors.menuName)" maxlength="20" />
      </FormField>
      <FormField label="路径">
        <input v-model="form.path" class="admin-input" placeholder="/orders" />
      </FormField>
      <FormField label="权限标识">
        <input v-model="form.permission" class="admin-input" placeholder="order:list" />
      </FormField>
      <button class="mt-7 h-11 rounded-full bg-sky-500 text-white">{{ form.id ? '更新' : '保存' }}</button>
    </form>
    <div class="admin-card divide-y divide-slate-100">
      <div v-for="item in list" :key="item.id" class="flex items-center justify-between px-5 py-4">
        <div>
          <div class="font-medium">{{ item.menuName }}</div>
          <div class="text-xs text-mute">{{ item.path || '-' }} · {{ item.permission || '无权限标识' }}</div>
        </div>
        <div class="space-x-2">
          <button class="admin-btn" @click="edit(item)">编辑</button>
          <button class="admin-btn-danger" @click="remove(item)">删除</button>
        </div>
      </div>
      <div v-if="!list.length" class="px-5 py-10 text-center text-sm text-mute">暂无菜单</div>
    </div>
  </AdminPage>
</template>
