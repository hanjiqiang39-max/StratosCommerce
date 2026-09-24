import axios, { type AxiosRequestConfig } from 'axios'
import { createDiscreteApi } from 'naive-ui'

const { message } = createDiscreteApi(['message'])

export interface ApiResult<T> {
  code: number
  message: string
  data: T
}

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || '/api',
  timeout: 20000,
})

request.interceptors.request.use((config) => {
  const token = localStorage.getItem('merchant_token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

function isAuthRoute() {
  return location.pathname === '/login' || location.pathname === '/register'
}

function isAuthApi(url?: string) {
  return Boolean(url && /\/(merchant\/(login|register|sms\/)|user\/(auth\/options|sms\/))/.test(url))
}

function kickToLogin(url?: string) {
  if (isAuthRoute() || isAuthApi(url)) return
  localStorage.removeItem('merchant_token')
  localStorage.removeItem('merchant_session')
  location.href = '/login'
}

request.interceptors.response.use(
  (response) => {
    const payload = response.data as ApiResult<unknown>
    if (payload && typeof payload.code === 'number' && payload.code !== 200) {
      message.error(payload.message || '请求失败')
      if (payload.code === 401) {
        kickToLogin(response.config?.url)
      }
      return Promise.reject(payload)
    }
    return response
  },
  (error) => {
    const payload = error.response?.data as ApiResult<unknown> | undefined
    if (error.response?.status === 401) {
      kickToLogin(error.config?.url)
    }
    message.error(payload?.message || error.message || '网络异常')
    return Promise.reject(error)
  },
)

export async function get<T>(url: string, params?: unknown) {
  const res = await request.get<ApiResult<T>>(url, { params })
  return res.data.data
}

export async function post<T>(url: string, data?: unknown, config?: AxiosRequestConfig) {
  const headers = { ...(config?.headers || {}) }
  if (typeof FormData !== 'undefined' && data instanceof FormData) {
    delete (headers as Record<string, unknown>)['Content-Type']
  }
  const res = await request.post<ApiResult<T>>(url, data, { ...config, headers })
  return res.data.data
}

export async function put<T>(url: string, data?: unknown, config?: AxiosRequestConfig) {
  const res = await request.put<ApiResult<T>>(url, data, config)
  return res.data.data
}

export async function del<T>(url: string, params?: unknown) {
  const res = await request.delete<ApiResult<T>>(url, { params })
  return res.data.data
}
