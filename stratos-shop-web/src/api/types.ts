export interface PageResult<T> {
  records: T[]
  total: number
  pageNum: number
  pageSize: number
  pages: number
}

export interface LoginVO {
  token: string
  refreshToken?: string
  expireTime?: string
  userId: number | string
  username: string
  nickname: string
}

export interface UserVO {
  id: number | string
  username: string
  nickname: string
  avatar?: string
  gender?: number
  birthday?: string
  phone?: string
  email?: string
  status?: number
  levelId?: number
  points?: number
  growthValue?: number
}

export interface ProductSpu {
  id: number
  title?: string
  spuName?: string
  mainImage?: string
  sellingPoint?: string
  categoryId?: number
  brandId?: number
  status?: number
  saleCount?: number
  minPrice?: number
  price?: number
  skuList?: ProductSku[]
}

export interface ProductSku {
  id: number
  skuName?: string
  skuImage?: string
  specJson?: string
  price?: number
  originalPrice?: number
  stock?: number
}

export interface ProductDetail {
  id: number
  shopId?: number | string
  shopName?: string
  title?: string
  subTitle?: string
  mainImage?: string
  imageList?: string
  detailHtml?: string
  sellingPoint?: string
  status?: number
  saleCount?: number
  minPrice?: number
  maxPrice?: number
  skuList?: ProductSku[]
}

export interface Category {
  id: number
  parentId?: number
  categoryName: string
  icon?: string
  image?: string
  children?: Category[]
}

export interface CartItem {
  id: number
  userId: number
  skuId: number
  spuId: number
  quantity: number
  selected: number
}

export interface Address {
  id: number
  userId: number
  receiverName: string
  receiverPhone: string
  province: string
  city: string
  district: string
  detailAddress: string
  isDefault?: number
}

export interface OrderInfo {
  id: string | number
  orderNo: string
  userId: string | number
  status: number
  totalAmount: number
  payAmount: number
  freightAmount?: number
  discountAmount?: number
  couponAmount?: number
  pointsAmount?: number
  receiverName?: string
  receiverPhone?: string
  receiverProvince?: string
  receiverCity?: string
  receiverDistrict?: string
  receiverDetailAddress?: string
  createTime?: string
  buyerRemark?: string
}

export interface OrderDetail extends OrderInfo {
  statusDesc?: string
  receiverAddress?: string
  items?: Array<{
    skuId: number
    skuName?: string
    skuImage?: string
    price?: number
    quantity?: number
  }>
}

export interface OrderCreateVO {
  orderId: string | number
  orderNo: string
  userId: string | number
  totalAmount: number
  freightAmount: number
  payAmount: number
  status: number
}

export interface PayOrderVO {
  payNo: string
  outTradeNo?: string
  status?: number
  payAmount?: number
  payForm?: string
}

export interface Banner {
  id: number
  title: string
  imageUrl: string
  linkUrl?: string
}

export interface Notice {
  id: number
  title: string
  content: string
}

export interface Coupon {
  id: number | string
  couponName: string
  couponCode?: string
  couponType?: number
  discountType?: number
  discountValue?: number
  minAmount?: number
  maxDiscount?: number
  publishCount?: number
  receivedCount?: number
  limitPerUser?: number
  validDays?: number
  startTime?: string
  endTime?: string
  status?: number
}

export interface UserCoupon {
  id: number | string
  couponId: number | string
  couponCode?: string
  couponName?: string
  couponType?: number
  discountType?: number
  discountValue?: number
  minAmount?: number
  maxDiscount?: number
  status: number
  startTime?: string
  endTime?: string
  orderNo?: string
}

export interface SeckillActivity {
  id: number | string
  activityName?: string
  spuId?: number | string
  skuId?: number | string
  originalPrice?: number
  seckillPrice?: number
  seckillStock?: number
  limitPerUser?: number
  startTime?: string
  endTime?: string
  status?: number
  soldCount?: number
}

export interface GroupActivity {
  id: number | string
  activityName?: string
  spuId?: number | string
  skuId?: number | string
  originalPrice?: number
  groupPrice?: number
  requireNum?: number
  limitHours?: number
  limitPerUser?: number
  startTime?: string
  endTime?: string
  status?: number
}

export interface GroupRecord {
  id: number | string
  groupBuyingId?: number | string
  groupNo: string
  leaderUserId?: number | string
  requireNum?: number
  currentNum?: number
  status?: number
  expireTime?: string
  successTime?: string
  orderId?: number | string
  orderNo?: string
  isLeader?: number
}

export interface SeckillRecord {
  id: number | string
  recordId?: number | string
  seckillId?: number | string
  skuId?: number | string
  userId?: number | string
  orderId?: number | string
  orderNo?: string
  quantity?: number
  seckillPrice?: number
  status?: number
  reused?: boolean
}

export interface PointsAccount {
  availablePoints?: number
  totalPoints?: number
  usedPoints?: number
}

export interface Comment {
  id: number
  nickname?: string
  starRating?: number
  content?: string
  images?: string
  replyContent?: string
  createTime?: string
}
