<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useMessage } from 'naive-ui'
import { adminList, adminRoles, assignRoles, deleteAdmin, roleList, saveAdmin } from '@/api'
import AdminPage from '@/components/AdminPage.vue'
import FormField from '@/components/FormField.vue'
import { firstError, inputClass, maxLen, minLen, password, required, username } from '@/utils/validate'

const message = useMessage()
const records = ref<any[]>([])
const roles = ref<any[]>([])
const errors = reactive({ username: '', nickname: '', password: '' })
const form = reactive({ username: '', password: 'admin123', nickname: '', status: 1 })
const current = ref<string | number>()
const checked = ref<Array<number | string>>([])

async function load() {
  const page = await adminList({ pageNum: 1, pageSize: 50 })
  records.value = page.records || []
  roles.value = (await roleList()) || []
}

async function save() {
  Object.assign(errors, {
    username: username(form.username),
    nickname: required(form.nickname, '昵称') || minLen(form.nickname, 2, '昵称') || maxLen(form.nickname, 20, '昵称'),
    password: password(form.password),
  })
  const error = firstError(errors)
  if (error) {
    message.error(error)
    return
  }
  await saveAdmin({ ...form, username: form.username.trim(), nickname: form.nickname.trim() })
  message.success('管理员已保存')
  Object.assign(form, { username: '', password: 'admin123', nickname: '', status: 1 })
  await load()
}

async function remove(item: any) {
  if (!window.confirm(`确定删除管理员 ${item.username}？`)) return
  await deleteAdmin(item.id)
  message.success('已删除')
  await load()
}

async function pick(id: string | number) {
  current.value = id
  const rows = (await adminRoles(id)) || []
  checked.value = rows.map((item: any) => item.id)
}

async function saveRoles() {
  if (!current.value) {
    message.error('请先选择管理员')
    return
  }
  await assignRoles(current.value, checked.value)
  message.success('角色已分配')
}

onMounted(load)
</script>

<template>
  <AdminPage title="管理员" extra="新建账号后可在右侧分配角色。默认密码 admin123，保存前可改。">
    <div class="grid gap-4 md:grid-cols-2">
      <div class="space-y-4">
        <form class="admin-card grid grid-cols-1 gap-4 p-5" @submit.prevent="save">
          <FormField label="登录名" required :error="errors.username">
            <input v-model="form.username" :class="inputClass(errors.username)" maxlength="20" />
          </FormField>
          <FormField label="昵称" required :error="errors.nickname">
            <input v-model="form.nickname" :class="inputClass(errors.nickname)" maxlength="20" />
          </FormField>
          <FormField label="密码" required hint="至少 8 位，含字母和数字" :error="errors.password">
            <input v-model="form.password" type="password" :class="inputClass(errors.password)" />
          </FormField>
          <button class="h-11 rounded-full bg-sky-500 text-white">保存管理员</button>
        </form>
        <div class="admin-card divide-y divide-slate-100">
          <div v-for="item in records" :key="item.id" class="flex items-center justify-between px-5 py-4">
            <button class="text-left hover:text-sky-600" :class="String(current) === String(item.id) ? 'text-sky-600' : ''" @click="pick(item.id)">
              <div class="font-medium">{{ item.username }}</div>
              <div class="text-xs text-mute">{{ item.nickname || '-' }}</div>
            </button>
            <button class="admin-btn-danger" @click="remove(item)">删除</button>
          </div>
        </div>
      </div>
      <section class="admin-card p-5">
        <h2 class="mb-3 font-medium">分配角色</h2>
        <p class="mb-4 text-sm text-mute">{{ current ? `当前管理员 #${current}` : '请先点左侧管理员' }}</p>
        <label v-for="item in roles" :key="item.id" class="flex items-center gap-2 py-1.5 text-sm">
          <input v-model="checked" type="checkbox" :value="item.id" />
          {{ item.roleName }}
        </label>
        <button v-if="current" class="mt-4 h-11 rounded-full bg-slate-900 px-5 text-white" @click="saveRoles">保存角色</button>
      </section>
    </div>
  </AdminPage>
</template>
