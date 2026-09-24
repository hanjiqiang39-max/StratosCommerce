<script setup lang="ts">
import { onMounted, onUnmounted, reactive, ref } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import { authOptions, sendSms, type AuthOptions } from '@/api/user'
import { useAuthStore } from '@/stores/auth'
import FormField from '@/components/FormField.vue'

const auth = useAuthStore()
const router = useRouter()
const message = useMessage()
const mode = ref<'account' | 'sms'>('account')
const submitting = ref(false)
const seconds = ref(0)
const options = ref<AuthOptions>({ smsReady: false, smsVendor: 'mock', wechatReady: false, alipayReady: false })
const account = reactive({
  username: '',
  password: '',
  nickname: '',
  phone: '',
})
const sms = reactive({
  phone: '',
  code: '',
  password: '',
  nickname: '',
})
let timer: number | undefined

onMounted(async () => {
  try {
    options.value = await authOptions()
  } catch {
    /* ignore */
  }
})

onUnmounted(() => {
  if (timer) window.clearInterval(timer)
})

function validPassword(value: string) {
  return /^(?=.*[A-Za-z])(?=.*\d).{8,32}$/.test(value)
}

async function send() {
  if (!/^1[3-9]\d{9}$/.test(sms.phone)) {
    message.error('请输入正确手机号')
    return
  }
  await sendSms(sms.phone)
  seconds.value = 60
  timer = window.setInterval(() => {
    seconds.value -= 1
    if (seconds.value <= 0 && timer) window.clearInterval(timer)
  }, 1000)
  message.success(options.value.smsReady ? '验证码已发送到手机' : '验证码已生成，当前未配置阿里云，请看通知服务日志')
}

async function submit() {
  submitting.value = true
  try {
    if (mode.value === 'account') {
      if (!/^[A-Za-z0-9_]{4,20}$/.test(account.username)) {
        message.error('用户名 4-20 位，字母数字或下划线')
        return
      }
      if (!validPassword(account.password)) {
        message.error('密码至少 8 位，需同时包含字母和数字')
        return
      }
      await auth.register({
        username: account.username,
        password: account.password,
        nickname: account.nickname.trim() || undefined,
        phone: account.phone || undefined,
      })
    } else {
      if (!/^1[3-9]\d{9}$/.test(sms.phone)) {
        message.error('请输入正确手机号')
        return
      }
      if (!sms.code) {
        message.error('请填写短信验证码')
        return
      }
      if (!validPassword(sms.password)) {
        message.error('密码至少 8 位，需同时包含字母和数字')
        return
      }
      await auth.smsRegister({
        phone: sms.phone,
        code: sms.code,
        password: sms.password,
        nickname: sms.nickname.trim() || undefined,
      })
    }
    router.replace('/')
  } catch {
    /* 接口错误已由 request 弹出 */
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="flex min-h-screen items-center justify-center bg-page px-6">
    <form class="w-full max-w-md space-y-4 rounded-card bg-white p-10 shadow-card" @submit.prevent="submit">
      <h1 class="text-2xl font-semibold">注册账号</h1>
      <p class="text-sm text-mute">演示环境推荐账号注册。短信通道未配置时，验证码只写在服务器日志。</p>
      <div class="flex gap-4 text-sm">
        <button type="button" :class="mode === 'account' ? 'text-brand' : 'text-mute'" @click="mode = 'account'">账号注册</button>
        <button type="button" :class="mode === 'sms' ? 'text-brand' : 'text-mute'" @click="mode = 'sms'">手机号注册</button>
      </div>
      <template v-if="mode === 'account'">
        <FormField label="用户名" required hint="登录用，4-20 位字母数字">
          <input v-model="account.username" class="shop-input" maxlength="20" required />
        </FormField>
        <FormField label="密码" required hint="至少 8 位，需同时包含字母和数字">
          <input v-model="account.password" type="password" class="shop-input" required />
        </FormField>
        <FormField label="昵称">
          <input v-model="account.nickname" class="shop-input" placeholder="选填，展示在个人中心" maxlength="20" />
        </FormField>
        <FormField label="手机号" hint="选填，方便以后绑定">
          <input v-model="account.phone" class="shop-input" maxlength="11" />
        </FormField>
      </template>
      <template v-else>
        <FormField label="手机号" required>
          <input v-model="sms.phone" class="shop-input" maxlength="11" required />
        </FormField>
        <FormField label="验证码" required :hint="options.smsReady ? '验证码会发到手机' : '未配置阿里云时请看通知服务日志'">
          <div class="flex gap-2">
            <input v-model="sms.code" class="shop-input flex-1" />
            <button type="button" class="h-11 shrink-0 rounded-xl border px-3" :disabled="seconds > 0" @click="send">
              {{ seconds > 0 ? `${seconds}s` : '获取验证码' }}
            </button>
          </div>
        </FormField>
        <FormField label="密码" required>
          <input v-model="sms.password" type="password" class="shop-input" required />
        </FormField>
        <FormField label="昵称">
          <input v-model="sms.nickname" class="shop-input" maxlength="20" />
        </FormField>
      </template>
      <button type="submit" class="h-11 w-full rounded-full bg-brand text-white" :disabled="submitting">
        {{ submitting ? '注册中...' : '注册并登录' }}
      </button>
      <RouterLink to="/login" class="block text-center text-sm text-mute">已有账号？去登录</RouterLink>
    </form>
  </div>
</template>
