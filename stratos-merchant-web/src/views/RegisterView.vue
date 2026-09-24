<script setup lang="ts">
import { onMounted, onUnmounted, reactive, ref } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import { authOptions, merchantRegister, sendSms } from '@/api'
import FormField from '@/components/FormField.vue'
import { firstError, inputClass, maxLen, minLen, password, personName, phone, required, smsCode, username } from '@/utils/validate'

const router = useRouter()
const message = useMessage()
const loading = ref(false)
const seconds = ref(0)
const smsReady = ref(false)
const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  shopName: '',
  realName: '',
  phone: '',
  smsCode: '',
})
const errors = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  shopName: '',
  realName: '',
  phone: '',
  smsCode: '',
})
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
  errors.username = username(form.username)
  errors.password = password(form.password)
  errors.confirmPassword = form.confirmPassword !== form.password ? '两次输入的密码不一致' : required(form.confirmPassword, '确认密码')
  errors.shopName = required(form.shopName, '店铺名称') || minLen(form.shopName, 2, '店铺名称') || maxLen(form.shopName, 30, '店铺名称')
  errors.realName = personName(form.realName, '联系人', true)
  errors.phone = phone(form.phone)
  errors.smsCode = smsCode(form.smsCode)
  const tip = firstError(errors)
  if (tip) message.error(tip)
  return !tip
}

async function send() {
  errors.phone = phone(form.phone)
  if (errors.phone) {
    message.error(errors.phone)
    return
  }
  await sendSms(form.phone)
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
    await merchantRegister({
      username: form.username.trim(),
      password: form.password,
      shopName: form.shopName.trim(),
      realName: form.realName.trim(),
      phone: form.phone.trim(),
      smsCode: form.smsCode.trim(),
    })
    message.success('已提交入驻申请，请等待后台审核')
    router.replace('/login')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="flex min-h-screen items-center justify-center bg-[#042f2e] p-8">
    <form class="w-full max-w-lg space-y-4 rounded-[28px] bg-white p-10 shadow-2xl" @submit.prevent="submit">
      <div class="text-sm text-teal-600">商家入驻</div>
      <h2 class="text-2xl font-semibold">开一家店</h2>
      <p class="text-sm text-mute">提交后需后台审核。演示环境验证码写在通知服务日志。</p>
      <div class="grid grid-cols-2 gap-4">
        <FormField label="登录名" required hint="4-20 位字母数字" :error="errors.username">
          <input v-model="form.username" :class="inputClass(errors.username)" maxlength="20" @blur="errors.username = username(form.username)" />
        </FormField>
        <FormField label="登录密码" required hint="至少 8 位，含字母和数字" :error="errors.password">
          <input v-model="form.password" type="password" :class="inputClass(errors.password)" @blur="errors.password = password(form.password)" />
        </FormField>
        <FormField label="确认密码" required :error="errors.confirmPassword" class="col-span-2">
          <input
            v-model="form.confirmPassword"
            type="password"
            :class="inputClass(errors.confirmPassword)"
            @blur="errors.confirmPassword = form.confirmPassword !== form.password ? '两次输入的密码不一致' : ''"
          />
        </FormField>
        <FormField label="店铺名称" required class="col-span-2" :error="errors.shopName">
          <input v-model="form.shopName" :class="inputClass(errors.shopName)" maxlength="30" @blur="errors.shopName = required(form.shopName, '店铺名称')" />
        </FormField>
        <FormField label="联系人" :error="errors.realName">
          <input v-model="form.realName" :class="inputClass(errors.realName)" maxlength="20" @blur="errors.realName = personName(form.realName, '联系人')" />
        </FormField>
        <FormField label="手机号" required :error="errors.phone">
          <input v-model="form.phone" :class="inputClass(errors.phone)" maxlength="11" @blur="errors.phone = phone(form.phone)" />
        </FormField>
        <FormField label="短信验证码" required class="col-span-2" :error="errors.smsCode" :hint="smsReady ? '验证码发到上面的手机号' : '未配置阿里云时请看通知服务日志'">
          <div class="flex gap-2">
            <input v-model="form.smsCode" :class="inputClass(errors.smsCode)" maxlength="8" />
            <button type="button" class="admin-btn h-11" :disabled="seconds > 0" @click="send">
              {{ seconds > 0 ? `${seconds}s` : '获取验证码' }}
            </button>
          </div>
        </FormField>
      </div>
      <button type="submit" class="h-11 w-full rounded-full bg-teal-600 text-white" :disabled="loading">
        {{ loading ? '提交中...' : '提交入驻申请' }}
      </button>
      <div class="text-center text-sm text-mute">
        已有账号？
        <RouterLink to="/login" class="text-teal-700">去登录</RouterLink>
      </div>
    </form>
  </div>
</template>
