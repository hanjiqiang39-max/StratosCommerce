import { get, post, put } from '@/utils/request'
import type { OrderCreateVO, OrderDetail, OrderInfo, PageResult } from './types'

export const createFromCart = (payload: Record<string, unknown>) =>
  post<OrderCreateVO>('/order/create-from-cart', payload)

export const createOrder = (payload: Record<string, unknown>) =>
  post<OrderCreateVO>('/order/create', payload)

export const listOrders = (userId: number | string, pageNum = 1, pageSize = 10) =>
  get<PageResult<OrderInfo>>(`/order/user/${userId}`, { pageNum, pageSize })

export const getOrder = (orderId: string | number) => get<OrderDetail>(`/order/${orderId}`)

export const getOrderByNo = (orderNo: string) => get<OrderDetail>(`/order/no/${encodeURIComponent(orderNo)}`)

export const cancelOrder = (orderId: string | number) => put<void>(`/order/${orderId}/cancel`)

export const confirmOrder = (orderId: string | number) => put<void>(`/order/${orderId}/confirm`)

export const applyRefund = (payload: Record<string, unknown>) => post<number>('/order/refund', payload)

export const listRefunds = (userId: number) => get<unknown[]>(`/order/refund/user/${userId}`)
