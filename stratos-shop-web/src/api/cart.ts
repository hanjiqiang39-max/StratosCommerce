import { del, get, post, put } from '@/utils/request'
import type { CartItem } from './types'

export const listCart = (userId: number) => get<CartItem[]>(`/cart/list/${userId}`)

export const addCart = (payload: { userId: number; skuId: number; spuId: number; quantity: number }) =>
  post<number>('/cart', payload)

export const updateCartQty = (cartId: number, userId: number, quantity: number) =>
  put<void>(`/cart/${cartId}`, null, { params: { userId, quantity } })

export const deleteCart = (cartId: number, userId: number) => del<void>(`/cart/${cartId}`, { userId })

export const selectCart = (cartId: number, userId: number, selected: number) =>
  put<void>(`/cart/${cartId}/selected`, null, { params: { userId, selected } })

export const selectAllCart = (userId: number, selected: number) =>
  put<void>('/cart/selected/all', null, { params: { userId, selected } })
