<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import { useAuthStore } from '@/stores/auth'
import FormField from '@/components/FormField.vue'
import { firstError, inputClass, required } from '@/utils/validate'

const auth = useAuthStore()
const router = useRouter()
const message = useMessage()
const username = ref('admin')
const password = ref('admin123')
const loading = ref(false)
const errors = reactive({ username: '', password: '' })

function validate() {
  errors.username = required(username.value, '用户名')
  errors.password = required(password.value, '密码')
  const error = firstError(errors)
  if (error) message.error(error)
  return !error
}

async function submit() {
  if (!validate()) return
  loading.value = true
  try {
    await auth.login(username.value.trim(), password.value)
    router.replace('/dashboard')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="flex min-h-screen bg-[#0B1220]">
    <div class="hidden w-[46%] flex-col justify-between bg-[radial-gradient(circle_at_top,_#1d4ed8_0%,_#0b1220_55%)] p-12 text-white lg:flex">
      <div class="text-sm text-sky-200">StratosCommerce</div>
      <div>
        <h1 class="text-4xl font-semibold leading-tight">零售运营后台</h1>
        <p class="mt-4 max-w-sm text-sm leading-7 text-slate-300">管理商品、订单、售后、营销和系统配置，配合商城前台走完下单支付。</p>
      </div>
      <div class="text-xs text-slate-500">默认账号 admin / admin123</div>
    </div>
    <div class="flex flex-1 items-center justify-center p-8">
      <form class="w-full max-w-md space-y-4 rounded-[28px] bg-white p-10 shadow-2xl" @submit.prevent="submit">
        <div class="text-sm text-sky-600">管理员登录</div>
        <h2 class="text-2xl font-semibold">进入控制台</h2>
        <FormField label="用户名" required :error="errors.username">
          <input
            v-model="username"
            :class="inputClass(errors.username)"
            autocomplete="username"
            maxlength="20"
            @blur="errors.username = required(username, '用户名')"
          />
        </FormField>
        <FormField label="密码" required :error="errors.password">
          <input
            v-model="password"
            type="password"
            :class="inputClass(errors.password)"
            autocomplete="current-password"
            @blur="errors.password = required(password, '密码')"
          />
        </FormField>
        <button type="submit" class="h-11 w-full rounded-full bg-sky-500 text-white" :disabled="loading">
          {{ loading ? '登录中...' : '登录' }}
        </button>
      </form>
    </div>
  </div>
</template>
