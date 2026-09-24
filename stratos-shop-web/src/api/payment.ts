import { get, post } from '@/utils/request'
import type { PayOrderVO } from './types'

export const createPay = (payload: {
  orderId: string | number
  orderNo: string
  userId: string | number
  payChannel: number
  payAmount: number
  subject?: string
}) => post<PayOrderVO>('/payment/create', payload)

export const simulatePay = (payNo: string) => post<void>(`/payment/simulate/${payNo}`)

export const queryPayByOrder = (orderNo: string) => get<Record<string, unknown>>(`/payment/order/${orderNo}`)
