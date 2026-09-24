import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { getUser, login as loginApi, register as registerApi, smsLogin as smsLoginApi, smsRegister as smsRegisterApi } from '@/api/user'
import type { LoginVO, UserVO } from '@/api/types'

const TOKEN_KEY = 'shop_token'
const USER_KEY = 'shop_user'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem(TOKEN_KEY) || '')
  const user = ref<UserVO | null>(JSON.parse(localStorage.getItem(USER_KEY) || 'null'))

  const isLogin = computed(() => Boolean(token.value && user.value?.id))
  const userId = computed(() => user.value?.id)

  function persist(vo: LoginVO) {
    token.value = vo.token
    user.value = {
      id: vo.userId,
      username: vo.username,
      nickname: vo.nickname,
    }
    localStorage.setItem(TOKEN_KEY, vo.token)
    localStorage.setItem(USER_KEY, JSON.stringify(user.value))
  }

  async function login(username: string, password: string) {
    persist(await loginApi(username, password))
    if (user.value?.id) {
      user.value = await getUser(user.value.id)
      localStorage.setItem(USER_KEY, JSON.stringify(user.value))
    }
  }

  async function smsLogin(phone: string, code: string) {
    persist(await smsLoginApi(phone, code))
  }

  async function smsRegister(payload: { phone: string; code: string; password: string; nickname?: string }) {
    persist(await smsRegisterApi(payload))
  }

  function loginByOauth(vo: LoginVO) {
    persist(vo)
  }

  async function register(payload: { username: string; password: string; nickname?: string; phone?: string }) {
    await registerApi(payload)
    await login(payload.username, payload.password)
  }

  async function refreshProfile() {
    if (!user.value?.id) return
    user.value = await getUser(user.value.id)
    localStorage.setItem(USER_KEY, JSON.stringify(user.value))
  }

  function logout() {
    token.value = ''
    user.value = null
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_KEY)
  }

  return { token, user, isLogin, userId, login, smsLogin, smsRegister, loginByOauth, register, refreshProfile, logout }
})
