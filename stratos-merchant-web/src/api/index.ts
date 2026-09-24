import { del, get, post, put } from '@/utils/request'

export interface MerchantSession {
  token: string
  merchantId: string | number
  shopId: string | number
  username: string
  nickname?: string
  realName?: string
  shopName?: string
  status?: number
  auditStatus?: number
}

export const merchantLogin = (username: string, password: string) =>
  post<MerchantSession>('/merchant/login', { username, password })

export const merchantSmsLogin = (phone: string, code: string) =>
  post<MerchantSession>('/merchant/sms/login', { phone, code })

export const merchantRegister = (payload: Record<string, unknown>) =>
  post<string>('/merchant/register', payload)

export const sendSms = (phone: string) => post<void>('/user/sms/send', { phone })

export const authOptions = () =>
  get<{ smsReady: boolean; smsVendor: string; wechatReady: boolean; alipayReady: boolean }>('/user/auth/options')

export const merchantProfile = (merchantId: string | number) =>
  get<{ merchant: any; shop: any }>('/merchant/profile', { merchantId })

export const getShop = (merchantId: string | number) => get<any>('/merchant/shop', { merchantId })

export const updateShop = (payload: Record<string, unknown>) => put<string>('/merchant/shop', payload)

export const orderReport = (shopId: string | number, params?: Record<string, string>) =>
  get<Record<string, number>>('/merchant/order/report', { shopId, ...params })

export const merchantOrders = (params: Record<string, unknown>) => get<any>('/merchant/order/list', params)

export const merchantOrderDetail = (orderId: string | number, shopId: string | number) =>
  get<any>(`/merchant/order/${orderId}`, { shopId })

export const remarkOrder = (orderId: string | number, shopId: string | number, sellerRemark: string) =>
  put('/merchant/order/remark', { orderId, shopId, sellerRemark })

export const shipOrder = (orderId: string | number, shopId: string | number, companyCode: string) =>
  post<string>('/merchant/order/ship', { orderId, shopId, companyCode })

export const refundList = (shopId: string | number) => get<any[]>('/merchant/refund/list', { shopId })

export const auditRefund = (payload: Record<string, unknown>) => put('/merchant/refund/audit', payload)

export const uploadImage = (file: File) => {
  const data = new FormData()
  data.append('file', file)
  return post<{ url: string; filename: string }>('/upload', data)
}

export const productList = (params: Record<string, unknown>) => get<any>('/merchant/product/list', params)

export const saveProduct = (payload: Record<string, unknown>) => post<string>('/merchant/product', payload)

export const updateProductStatus = (id: string | number, status: number, shopId: string | number) =>
  put(`/merchant/product/${id}/status/${status}`, null, { params: { shopId } })

export const deleteProduct = (id: string | number, shopId: string | number) =>
  del(`/merchant/product/${id}`, { shopId })

export const productSkus = (shopId: string | number) => get<any[]>('/merchant/product/skus', { shopId })

export const updateSkuStock = (skuId: string | number, stock: number, shopId: string | number) =>
  put(`/merchant/product/sku/${skuId}/stock/${stock}`, null, { params: { shopId } })

export const categoryTree = () => get<any[]>('/product/categories')

export const promoSeckill = (skuIds?: string) => get<any[]>('/merchant/promotion/seckill', skuIds ? { skuIds } : undefined)
export const saveSeckill = (payload: Record<string, unknown>) => post('/merchant/promotion/seckill', payload)
export const seckillStatus = (id: number | string, status: number) =>
  put(`/merchant/promotion/seckill/${id}/status/${status}`)
export const promoCoupon = () => get<any[]>('/merchant/promotion/coupon')
export const saveCoupon = (payload: Record<string, unknown>) => post('/merchant/promotion/coupon', payload)
export const couponStatus = (id: number | string, status: number) =>
  put(`/merchant/promotion/coupon/${id}/status/${status}`)
export const promoGroup = (skuIds?: string) => get<any[]>('/merchant/promotion/group', skuIds ? { skuIds } : undefined)
export const saveGroup = (payload: Record<string, unknown>) => post('/merchant/promotion/group', payload)
export const groupStatus = (id: number | string, status: number) =>
  put(`/merchant/promotion/group/${id}/status/${status}`)

export const commentList = (shopId: string | number) => get<any[]>('/merchant/comment/list', { shopId })

export const replyComment = (id: string | number, shopId: string | number, replyContent: string) =>
  put(`/merchant/comment/${id}/reply`, null, { params: { shopId, replyContent } })
