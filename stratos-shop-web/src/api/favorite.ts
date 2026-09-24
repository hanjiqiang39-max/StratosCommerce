import { del, get, post } from '@/utils/request'

export const addFavorite = (userId: number, targetId: number, targetType = 1) =>
  post<number>('/favorite', null, { params: { userId, targetId, targetType } })

export const removeFavorite = (userId: number, targetId: number, targetType = 1) =>
  del<void>('/favorite', { userId, targetId, targetType })

export const listFavorites = (userId: number) =>
  get<Array<{ id: number; targetId: number; targetType: number }>>('/favorite/list', { userId })

export const favoriteExists = (userId: number, targetId: number) =>
  get<boolean>('/favorite/exists', { userId, targetId, targetType: 1 })
