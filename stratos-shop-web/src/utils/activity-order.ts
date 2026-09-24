import type { Router } from 'vue-router'
import { getOrder } from '@/api/order'

function hasOrder(id?: number | string | null) {
  if (id == null || id === '') return false
  const value = String(id)
  return value !== '0' && value !== 'null' && value !== 'undefined'
}

export async function goExistingOrder(router: Router, orderId?: number | string | null, extra = '') {
  if (!hasOrder(orderId)) return false
  try {
    const order = await getOrder(orderId as string)
    if (order.status === 0) {
      router.push(`/pay/${order.id}?orderNo=${order.orderNo}&amount=${order.payAmount}${extra}`)
    } else {
      router.push(`/order/${order.id}?orderNo=${order.orderNo}`)
    }
    return true
  } catch {
    return false
  }
}

export async function goSeckillFlow(
  router: Router,
  payload: {
    seckillId?: number | string
    recordId?: number | string
    skuId?: number | string
    spuId?: number | string
    orderId?: number | string
    orderNo?: string
    qty?: number | string
  },
) {
  if (await goExistingOrder(router, payload.orderId)) return
  router.push({
    path: '/checkout',
    query: {
      type: 'seckill',
      seckillId: String(payload.seckillId || ''),
      seckillRecordId: String(payload.recordId || ''),
      skuId: String(payload.skuId || ''),
      spuId: String(payload.spuId || ''),
      qty: String(payload.qty || 1),
    },
  })
}

export async function goGroupFlow(
  router: Router,
  payload: {
    groupBuyingId?: number | string
    groupRecordId?: number | string
    groupNo?: string
    skuId?: number | string
    spuId?: number | string
    orderId?: number | string
    qty?: number | string
  },
) {
  if (await goExistingOrder(router, payload.orderId, '&group=1')) return
  router.push({
    path: '/checkout',
    query: {
      type: 'group',
      groupBuyingId: String(payload.groupBuyingId || ''),
      groupRecordId: String(payload.groupRecordId || ''),
      groupNo: payload.groupNo || '',
      skuId: String(payload.skuId || ''),
      spuId: String(payload.spuId || ''),
      qty: String(payload.qty || 1),
    },
  })
}
