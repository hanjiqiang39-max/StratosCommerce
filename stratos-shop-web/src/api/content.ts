import { del, get } from '@/utils/request'
import type { Banner, Notice } from './types'

export const listBanners = () => get<Banner[]>('/banner/list')

export const listNotices = () => get<Notice[]>('/notice/list')

export const getNotice = (id: number) => get<Notice>(`/notice/${id}`)

export const listNotifications = (userId: number) =>
  get<Array<{ id: number; title: string; content: string; isRead: number }>>('/notification/list', { userId })

export const deleteNotification = (id: number, userId: number) =>
  del<void>(`/notification/${id}`, { userId })
