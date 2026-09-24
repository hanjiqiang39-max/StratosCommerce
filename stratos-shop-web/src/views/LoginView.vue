<script setup lang="ts">
import { onMounted, onUnmounted, ref } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import { authOptions, oauthAuthorizeUrl, sendSms, type AuthOptions } from '@/api/user'
import { useAuthStore } from '@/stores/auth'
import FormField from '@/components/FormField.vue'

const auth = useAuthStore()
const router = useRouter()
const route = useRoute()
const message = useMessage()
const mode = ref<'pwd' | 'sms'>('pwd')
const username = ref('')
const password = ref('')
const phone = ref('')
const code = ref('')
const seconds = ref(0)
const submitting = ref(false)
const options = ref<AuthOptions>({ smsReady: false, smsVendor: 'mock', wechatReady: false, alipayReady: false })
let timer: number | undefined

onMounted(async () => {
  try {
    options.value = await authOptions()
  } catch {
    /* 选项失败时仍可用密码登录 */
  }
})

onUnmounted(() => {
  if (timer) window.clearInterval(timer)
})

async function submit() {
  submitting.value = true
  try {
    if (mode.value === 'pwd') {
      if (!username.value.trim() || !password.value) {
        message.error('请填写用户名和密码')
        return
      }
      await auth.login(username.value.trim(), password.value)
    } else {
      if (!/^1[3-9]\d{9}$/.test(phone.value) || !code.value) {
        message.error('请填写手机号和验证码')
        return
      }
      await auth.smsLogin(phone.value, code.value)
    }
    router.replace(String(route.query.redirect || '/'))
  } catch {
    /* 接口错误已由 request 弹出 */
  } finally {
    submitting.value = false
  }
}

async function send() {
  if (!/^1[3-9]\d{9}$/.test(phone.value)) {
    message.error('请输入正确手机号')
    return
  }
  await sendSms(phone.value)
  seconds.value = 60
  timer = window.setInterval(() => {
    seconds.value -= 1
    if (seconds.value <= 0 && timer) window.clearInterval(timer)
  }, 1000)
  message.success(options.value.smsReady ? '验证码已发送到手机' : '验证码已生成，当前未配置阿里云，请看通知服务日志')
}

function startOauth(provider: 'wechat' | 'alipay') {
  const ready = provider === 'wechat' ? options.value.wechatReady : options.value.alipayReady
  if (!ready) {
    message.error(provider === 'wechat'
      ? '未配置微信开放平台，演示环境请用账号密码登录'
      : '未配置支付宝开放平台，演示环境请用账号密码登录')
    return
  }
  window.location.href = oauthAuthorizeUrl(provider, String(route.query.redirect || '/'))
}
</script>

<template>
  <div class="flex min-h-screen items-center justify-center bg-page px-6">
    <div class="grid w-full max-w-4xl grid-cols-2 overflow-hidden rounded-card bg-white shadow-card">
      <div class="bg-gradient-to-br from-sky-100 to-white p-10">
        <div class="text-sm text-brand">StratosCommerce</div>
        <h2 class="mt-4 text-3xl font-semibold">欢迎回来</h2>
        <p class="mt-3 text-mute">演示站用账号密码登录即可。短信和第三方登录需要企业资质后才会开通。</p>
      </div>
      <form class="p-10" @submit.prevent="submit">
        <h1 class="text-2xl font-semibold">登录</h1>
        <div class="mt-4 flex gap-4 text-sm">
          <button type="button" :class="mode === 'pwd' ? 'text-brand' : 'text-mute'" @click="mode = 'pwd'">密码登录</button>
          <button type="button" :class="mode === 'sms' ? 'text-brand' : 'text-mute'" @click="mode = 'sms'">短信登录</button>
        </div>
        <div v-if="mode === 'pwd'" class="mt-5 space-y-4">
          <FormField label="用户名" required>
            <input v-model="username" class="shop-input" placeholder="注册时的用户名" autocomplete="username" />
          </FormField>
          <FormField label="密码" required>
            <input v-model="password" type="password" class="shop-input" placeholder="登录密码" autocomplete="current-password" />
          </FormField>
        </div>
        <div v-else class="mt-5 space-y-4">
          <FormField label="手机号" required>
            <input v-model="phone" class="shop-input" placeholder="11 位手机号" maxlength="11" />
          </FormField>
          <FormField label="验证码" required :hint="options.smsReady ? '将发到该手机号' : '未配置阿里云时，验证码在通知服务日志'">
            <div class="flex gap-2">
              <input v-model="code" class="shop-input flex-1" placeholder="6 位验证码" />
              <button type="button" class="h-11 shrink-0 rounded-xl border px-3" :disabled="seconds > 0" @click="send">
                {{ seconds > 0 ? `${seconds}s` : '获取验证码' }}
              </button>
            </div>
          </FormField>
        </div>
        <button type="submit" class="mt-6 h-11 w-full rounded-full bg-brand text-white" :disabled="submitting">
          {{ submitting ? '登录中...' : '登录' }}
        </button>
        <div class="mt-5 flex items-center gap-3 text-xs text-mute">
          <span class="h-px flex-1 bg-slate-200" />
          其他登录方式
          <span class="h-px flex-1 bg-slate-200" />
        </div>
        <div class="mt-4 grid grid-cols-2 gap-3">
          <button type="button" class="h-11 rounded-full border text-sm text-mute" @click="startOauth('wechat')">微信登录</button>
          <button type="button" class="h-11 rounded-full border text-sm text-mute" @click="startOauth('alipay')">支付宝登录</button>
        </div>
        <RouterLink to="/register" class="mt-4 block text-center text-sm text-mute">没有账号？去注册</RouterLink>
      </form>
    </div>
  </div>
</template>
