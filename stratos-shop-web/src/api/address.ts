import { del, get, post, put } from '@/utils/request'
import type { Address } from './types'

export const listAddress = (userId: number | string) => get<Address[]>(`/address/list/${userId}`)

export const saveAddress = (userId: number | string, payload: Partial<Address>) =>
  post<number>(`/address/${userId}`, payload)

export const updateAddress = (userId: number | string, addressId: number | string, payload: Partial<Address>) =>
  put<void>(`/address/${userId}/${addressId}`, payload)

export const deleteAddress = (userId: number | string, addressId: number | string) =>
  del<void>(`/address/${userId}/${addressId}`)

export const setDefaultAddress = (userId: number | string, addressId: number | string) =>
  put<void>(`/address/${userId}/${addressId}/default`)
