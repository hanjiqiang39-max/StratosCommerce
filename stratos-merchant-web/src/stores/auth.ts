import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { merchantLogin, merchantSmsLogin, type MerchantSession } from '@/api'

const KEY = 'merchant_session'

export const useAuthStore = defineStore('merchant-auth', () => {
  const session = ref<MerchantSession | null>(JSON.parse(localStorage.getItem(KEY) || 'null'))
  const token = computed(() => session.value?.token || localStorage.getItem('merchant_token') || '')
  const isLogin = computed(() => Boolean(token.value))
  const shopId = computed(() => session.value?.shopId)
  const merchantId = computed(() => session.value?.merchantId)

  async function loginBySession(vo: MerchantSession) {
    if (!vo?.token) {
      throw new Error('登录失败，未返回令牌')
    }
    session.value = vo
    localStorage.setItem(KEY, JSON.stringify(vo))
    localStorage.setItem('merchant_token', vo.token)
  }

  async function login(username: string, password: string) {
    await loginBySession(await merchantLogin(username, password))
  }

  async function smsLogin(phone: string, code: string) {
    await loginBySession(await merchantSmsLogin(phone, code))
  }

  function logout() {
    session.value = null
    localStorage.removeItem(KEY)
    localStorage.removeItem('merchant_token')
  }

  return { session, token, isLogin, shopId, merchantId, login, smsLogin, logout }
})
