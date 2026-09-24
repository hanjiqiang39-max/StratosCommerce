import { get, post } from '@/utils/request'
import type { Coupon, GroupActivity, GroupRecord, SeckillActivity, SeckillRecord, UserCoupon } from './types'

export const listCoupons = () => get<Coupon[]>('/coupon/list')

export const receiveCoupon = (userId: number | string, couponId: number | string) =>
  post<void>('/coupon/receive', { userId, couponId })

export const listUserCoupons = (userId: number | string, status?: number) =>
  get<UserCoupon[]>(`/coupon/user/${userId}`, { status })

export const quoteCoupon = (userCouponId: number | string, userId: number | string, amount: number) =>
  get<number>('/coupon/quote', { userCouponId, userId, amount })

export const listSeckill = () => get<SeckillActivity[]>('/seckill/list')

export const seckillDetail = (id: number | string) => get<SeckillActivity>(`/seckill/detail/${id}`)

export const doSeckill = (payload: Record<string, unknown>) => post<SeckillRecord>('/seckill/do', payload)

export const listMySeckills = (userId: number | string) => get<SeckillRecord[]>(`/seckill/user/${userId}`)

export const mySeckill = (userId: number | string, seckillId: number | string) =>
  get<SeckillRecord | null>(`/seckill/user/${userId}/activity/${seckillId}`)

export const seckillByOrder = (orderId: number | string) => get<SeckillRecord | null>(`/seckill/order/${orderId}`)

export const listFullDiscount = () => get<unknown[]>('/full-discount/list')

export const quoteFullDiscount = (amount: number, spuIds?: Array<number | string>) =>
  post<{ activityId?: number | string; activityName?: string; fullAmount?: number; discountAmount?: number; payAmount?: number }>(
    '/full-discount/calculate',
    { amount, spuIds: (spuIds || []).map((id) => String(id)) },
  )

export const listGroups = () => get<GroupActivity[]>('/group/list')

export const groupActivity = (id: number | string) => get<GroupActivity>(`/group/activity/${id}`)

export const listOpenGroups = (activityId: number | string) =>
  get<GroupRecord[]>(`/group/activity/${activityId}/records`)

export const groupRecord = (groupNo: string) => get<GroupRecord>(`/group/${encodeURIComponent(groupNo)}`)

export const groupMembers = (groupNo: string) => get<any[]>(`/group/${encodeURIComponent(groupNo)}/members`)

export const listMyGroups = (userId: number | string) => get<GroupRecord[]>(`/group/user/${userId}`)

export const myGroup = (userId: number | string, activityId: number | string) =>
  get<GroupRecord | null>(`/group/user/${userId}/activity/${activityId}`)

export const groupByOrder = (orderId: number | string) => get<GroupRecord | null>(`/group/order/${orderId}`)

export const joinGroup = (payload: Record<string, unknown>) => post<GroupRecord>('/group/join', payload)

export const groupPayable = (orderId: number | string) => get<boolean>('/group/payable', { orderId })
