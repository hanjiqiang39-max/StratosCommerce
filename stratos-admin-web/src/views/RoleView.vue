<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useMessage } from 'naive-ui'
import { assignMenus, deleteRole, menuList, roleList, roleMenus, saveRole } from '@/api'
import AdminPage from '@/components/AdminPage.vue'
import FormField from '@/components/FormField.vue'
import { codeValue, firstError, inputClass, maxLen, minLen, required } from '@/utils/validate'

const message = useMessage()
const roles = ref<any[]>([])
const menus = ref<any[]>([])
const errors = reactive({ roleName: '', roleCode: '' })
const form = reactive({ roleName: '', roleCode: '' })
const current = ref<number>()
const checked = ref<number[]>([])

async function load() {
  roles.value = (await roleList()) || []
  menus.value = (await menuList()) || []
}

async function save() {
  Object.assign(errors, {
    roleName: required(form.roleName, '角色名称') || minLen(form.roleName, 2, '角色名称') || maxLen(form.roleName, 20, '角色名称'),
    roleCode: codeValue(form.roleCode, '角色编码'),
  })
  const error = firstError(errors)
  if (error) {
    message.error(error)
    return
  }
  await saveRole({ ...form, roleName: form.roleName.trim(), roleCode: form.roleCode.trim() })
  message.success('角色已保存')
  Object.assign(form, { roleName: '', roleCode: '' })
  await load()
}

async function pick(id: number) {
  current.value = id
  checked.value = (await roleMenus(id)) || []
}

async function saveMenus() {
  if (!current.value) return
  await assignMenus(current.value, checked.value)
  message.success('菜单权限已保存')
}

async function remove(item: any) {
  if (!window.confirm(`确定删除角色「${item.roleName}」？`)) return
  await deleteRole(item.id)
  if (current.value === item.id) {
    current.value = undefined
    checked.value = []
  }
  message.success('已删除')
  await load()
}

onMounted(load)
</script>

<template>
  <AdminPage title="角色" extra="先选角色，再勾选菜单权限。保存后该角色下的管理员即生效。">
    <div class="grid gap-4 md:grid-cols-2">
      <div class="space-y-3">
        <form class="admin-card grid grid-cols-1 gap-4 p-5" @submit.prevent="save">
          <FormField label="角色名称" required :error="errors.roleName">
            <input v-model="form.roleName" :class="inputClass(errors.roleName)" maxlength="20" />
          </FormField>
          <FormField label="角色编码" required :error="errors.roleCode">
            <input v-model="form.roleCode" :class="inputClass(errors.roleCode)" maxlength="32" placeholder="如 ROLE_OPS" />
          </FormField>
          <button class="h-11 rounded-full bg-sky-500 text-white">保存角色</button>
        </form>
        <div class="admin-card divide-y divide-slate-100">
          <div v-for="item in roles" :key="item.id" class="flex items-center justify-between px-5 py-4">
            <button class="text-left hover:text-sky-600" :class="current === item.id ? 'text-sky-600' : ''" @click="pick(item.id)">
              {{ item.roleName }}
              <span class="ml-2 text-xs text-mute">{{ item.roleCode }}</span>
            </button>
            <button class="admin-btn-danger" @click="remove(item)">删除</button>
          </div>
          <div v-if="!roles.length" class="px-5 py-8 text-center text-sm text-mute">暂无角色</div>
        </div>
      </div>
      <section class="admin-card p-5">
        <h2 class="mb-3 font-medium">分配菜单</h2>
        <label v-for="item in menus" :key="item.id" class="flex items-center gap-2 py-1.5 text-sm">
          <input v-model="checked" type="checkbox" :value="item.id" />
          {{ item.menuName }}
        </label>
        <button v-if="current" class="mt-4 h-11 rounded-full bg-slate-900 px-5 text-white" @click="saveMenus">保存权限</button>
        <p v-else class="mt-4 text-sm text-mute">请先点左侧角色</p>
      </section>
    </div>
  </AdminPage>
</template>
