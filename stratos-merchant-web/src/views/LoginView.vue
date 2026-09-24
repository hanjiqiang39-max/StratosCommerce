<script setup lang="ts">
import { onMounted, onUnmounted, reactive, ref } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import { authOptions, sendSms } from '@/api'
import { useAuthStore } from '@/stores/auth'
import FormField from '@/components/FormField.vue'
import { firstError, inputClass, minLen, phone, required, smsCode } from '@/utils/validate'

const auth = useAuthStore()
const router = useRouter()
const message = useMessage()
const mode = ref<'pwd' | 'sms'>('pwd')
const username = ref('merchant')
const password = ref('merchant123')
const phoneNo = ref('')
const code = ref('')
const loading = ref(false)
const seconds = ref(0)
const smsReady = ref(false)
const errors = reactive({ username: '', password: '', phone: '', code: '' })
let timer: number | undefined

onMounted(async () => {
  try {
    smsReady.value = Boolean((await authOptions())?.smsReady)
  } catch {
    smsReady.value = false
  }
})

onUnmounted(() => {
  if (timer) window.clearInterval(timer)
})

function validate() {
  if (mode.value === 'pwd') {
    errors.username = required(username.value, '用户名') || minLen(username.value, 3, '用户名')
    errors.password = required(password.value, '密码') || minLen(password.value, 6, '密码')
    errors.phone = ''
    errors.code = ''
  } else {
    errors.phone = phone(phoneNo.value, '入驻手机号')
    errors.code = smsCode(code.value)
    errors.username = ''
    errors.password = ''
  }
  const tip = firstError(errors)
  if (tip) message.error(tip)
  return !tip
}

async function send() {
  errors.phone = phone(phoneNo.value, '入驻手机号')
  if (errors.phone) {
    message.error(errors.phone)
    return
  }
  await sendSms(phoneNo.value)
  seconds.value = 60
  timer = window.setInterval(() => {
    seconds.value -= 1
    if (seconds.value <= 0 && timer) window.clearInterval(timer)
  }, 1000)
  message.success(smsReady.value ? '验证码已发送到手机' : '验证码已生成，未配置阿里云时请看通知服务日志')
}

async function submit() {
  if (!validate()) return
  loading.value = true
  try {
    if (mode.value === 'pwd') {
      await auth.login(username.value.trim(), password.value)
    } else {
      await auth.smsLogin(phoneNo.value, code.value)
    }
    if (!auth.isLogin) return
    await router.replace('/dashboard')
  } catch {
    // 错误已由请求拦截器提示
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="flex min-h-screen bg-[#042f2e]">
    <div class="hidden w-[46%] flex-col justify-between bg-[radial-gradient(circle_at_top,_#0f766e_0%,_#042f2e_55%)] p-12 text-white lg:flex">
      <div class="text-sm text-teal-200">StratosCommerce</div>
      <div>
        <h1 class="text-4xl font-semibold leading-tight">商家工作台</h1>
        <p class="mt-4 max-w-sm text-sm leading-7 text-slate-300">演示账号用密码登录。短信登录需要入驻时填写的手机号。</p>
      </div>
      <div class="text-xs text-slate-500">演示账号 merchant / merchant123</div>
    </div>
    <div class="flex flex-1 items-center justify-center p-8">
      <form class="w-full max-w-md space-y-4 rounded-[28px] bg-white p-10 shadow-2xl" @submit.prevent="submit">
        <div class="text-sm text-teal-600">商家登录</div>
        <h2 class="text-2xl font-semibold">进入店铺</h2>
        <div class="flex gap-4 text-sm">
          <button type="button" :class="mode === 'pwd' ? 'text-teal-700' : 'text-mute'" @click="mode = 'pwd'">密码登录</button>
          <button type="button" :class="mode === 'sms' ? 'text-teal-700' : 'text-mute'" @click="mode = 'sms'">短信登录</button>
        </div>
        <template v-if="mode === 'pwd'">
          <FormField label="用户名" required :error="errors.username">
            <input v-model="username" :class="inputClass(errors.username)" autocomplete="username" maxlength="20" @blur="errors.username = required(username, '用户名')" />
          </FormField>
          <FormField label="密码" required :error="errors.password">
            <input v-model="password" type="password" :class="inputClass(errors.password)" autocomplete="current-password" @blur="errors.password = required(password, '密码')" />
          </FormField>
        </template>
        <template v-else>
          <FormField label="入驻手机号" required :error="errors.phone">
            <input v-model="phoneNo" :class="inputClass(errors.phone)" maxlength="11" @blur="errors.phone = phone(phoneNo, '入驻手机号')" />
          </FormField>
          <FormField label="验证码" required :error="errors.code" :hint="smsReady ? '验证码发到该手机号' : '未配置阿里云时请看通知服务日志'">
            <div class="flex gap-2">
              <input v-model="code" :class="inputClass(errors.code)" maxlength="8" />
              <button type="button" class="admin-btn h-11" :disabled="seconds > 0" @click="send">
                {{ seconds > 0 ? `${seconds}s` : '获取验证码' }}
              </button>
            </div>
          </FormField>
        </template>
        <button type="submit" class="h-11 w-full rounded-full bg-teal-600 text-white" :disabled="loading">
          {{ loading ? '登录中...' : '登录' }}
        </button>
        <div class="text-center text-sm text-mute">
          还没有店铺？
          <RouterLink to="/register" class="text-teal-700">申请入驻</RouterLink>
        </div>
      </form>
    </div>
  </div>
</template>
