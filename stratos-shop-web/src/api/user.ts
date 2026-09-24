import { get, post, put } from '@/utils/request'
import type { LoginVO, PointsAccount, UserVO } from './types'

export interface AuthOptions {
  smsReady: boolean
  smsVendor: string
  wechatReady: boolean
  alipayReady: boolean
}

export const login = (username: string, password: string) =>
  post<LoginVO>('/user/login', { username, password })

export const register = (payload: { username: string; password: string; nickname?: string; phone?: string }) =>
  post<number>('/user/register', payload)

export const sendSms = (phone: string) => post<void>('/user/sms/send', { phone })

export const smsLogin = (phone: string, code: string) =>
  post<LoginVO>('/user/sms/login', { phone, code })

export const smsRegister = (payload: { phone: string; code: string; password: string; nickname?: string }) =>
  post<LoginVO>('/user/sms/register', payload)

export const authOptions = () => get<AuthOptions>('/user/auth/options')

export const oauthAuthorizeUrl = (provider: 'wechat' | 'alipay', redirect = '') =>
  `/api/user/oauth/${provider}/authorize?redirect=${encodeURIComponent(redirect)}`

export const getUser = (userId: number | string) => get<UserVO>(`/user/${userId}`)

export const updateProfile = (userId: number | string, payload: Partial<UserVO>) =>
  put<void>(`/user/${userId}/profile`, payload)

export const getPoints = (userId: number | string) => get<PointsAccount>(`/user/${userId}/points`)

export const uploadImage = (file: File) => {
  const data = new FormData()
  data.append('file', file)
  return post<{ url: string; filename: string }>('/upload', data)
}
