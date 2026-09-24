import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { adminLogin } from '@/api'

const KEY = 'admin_session'

export const useAuthStore = defineStore('admin-auth', () => {
  const session = ref(JSON.parse(localStorage.getItem(KEY) || 'null'))
  const token = computed(() => session.value?.token || localStorage.getItem('admin_token') || '')
  const isLogin = computed(() => Boolean(token.value))
  const isSuper = computed(() => session.value?.isSuperAdmin === 1 || session.value?.permissions?.includes('*:*:*'))

  function can(code?: string) {
    if (!code || isSuper.value) return true
    return (session.value?.permissions || []).includes(code)
  }

  async function login(username: string, password: string) {
    const vo = await adminLogin(username, password)
    session.value = vo
    localStorage.setItem(KEY, JSON.stringify(vo))
    localStorage.setItem('admin_token', vo.token)
  }

  function logout() {
    session.value = null
    localStorage.removeItem(KEY)
    localStorage.removeItem('admin_token')
  }

  return { session, token, isLogin, isSuper, can, login, logout }
})
