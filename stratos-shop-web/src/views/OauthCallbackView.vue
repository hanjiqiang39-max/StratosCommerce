<script setup lang="ts">
import { onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const message = useMessage()
const auth = useAuthStore()

onMounted(() => {
  const error = String(route.query.error || '')
  if (error) {
    message.error(error)
    router.replace('/login')
    return
  }
  const token = String(route.query.token || '')
  if (!token) {
    message.error('第三方登录未返回令牌')
    router.replace('/login')
    return
  }
  auth.loginByOauth({
    token,
    userId: String(route.query.userId || ''),
    username: String(route.query.username || ''),
    nickname: String(route.query.nickname || ''),
  })
  router.replace(String(route.query.redirect || '/'))
})
</script>

<template>
  <div class="flex min-h-screen items-center justify-center text-mute">正在完成登录...</div>
</template>
