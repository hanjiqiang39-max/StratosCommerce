export const PLACEHOLDER_IMAGE = '/placeholder.svg'
export const FALLBACK_BANNER = '/banner.svg'

export function money(value?: number | string | null) {
  const num = Number(value || 0)
  return `¥${num.toFixed(2)}`
}

function isRemotePlaceholder(path: string) {
  return /placeholder\.com|placehold\.co|demo-banner/i.test(path)
}

export function imageUrl(path?: string | null) {
  if (!path) return ''
  if (path.includes('demo-banner')) return FALLBACK_BANNER
  if (isRemotePlaceholder(path)) return PLACEHOLDER_IMAGE
  if (path.startsWith('http') || path.startsWith('data:')) return path
  if (path.startsWith('/api/')) return path
  if (path.startsWith('/files/')) return `/api${path}`
  return path
}

export function coverUrl(path?: string | null) {
  return imageUrl(path) || PLACEHOLDER_IMAGE
}

export function skuLabel(item?: { skuName?: string | null; displayName?: string | null; productTitle?: string | null; title?: string | null } | null) {
  if (!item) return '标准款'
  const display = String(item.displayName || '').trim()
  if (display && display !== '默认规格') return display
  const title = String(item.productTitle || item.title || '').trim()
  const spec = String(item.skuName || '').trim()
  if (spec && spec !== '默认规格') {
    return title && title !== spec ? `${title} · ${spec}` : spec
  }
  return title || '标准款'
}

export function productPrice(item?: { minPrice?: number | string | null; price?: number | string | null; skuList?: Array<{ price?: number | string | null }> } | null) {
  if (!item) return 0
  const candidates = [item.minPrice, item.price, ...(item.skuList || []).map((sku) => sku.price)]
    .map((value) => Number(value))
    .filter((value) => Number.isFinite(value) && value > 0)
  return candidates.length ? Math.min(...candidates) : Number(item.minPrice || item.price || 0)
}

export function orderStatusText(status?: number) {
  const map: Record<number, string> = {
    0: '待支付',
    10: '已支付',
    20: '待发货',
    30: '已发货',
    40: '已收货',
    50: '已完成',
    [-10]: '已取消',
    [-20]: '退款中',
    [-30]: '已退款',
  }
  return status == null ? '未知' : map[status] || '未知'
}

export function couponStatusText(status?: number) {
  const map: Record<number, string> = {
    0: '未使用',
    1: '已使用',
    2: '已过期',
  }
  return status == null ? '未知' : map[status] || '未知'
}

function amountText(value: number) {
  return Number.isInteger(value) ? String(value) : value.toFixed(2)
}

function readableText(value?: string | null) {
  const text = String(value || '').trim()
  if (!text) return ''
  if (/[?？�]/.test(text)) return ''
  return text
}

export function fullDiscountLabel(item?: { activityName?: string | null; fullAmount?: number | string | null; discountAmount?: number | string | null } | null) {
  const name = readableText(item?.activityName)
  if (name) return name
  const full = Number(item?.fullAmount || 0)
  const off = Number(item?.discountAmount || 0)
  if (full > 0 && off > 0) return `满${amountText(full)}减${amountText(off)}`
  return '满减'
}

export function couponLabel(item?: { discountType?: number; discountValue?: number | string | null; minAmount?: number | string | null } | null) {
  if (!item) return '优惠券'
  const value = Number(item.discountValue || 0)
  const min = Number(item.minAmount || 0)
  const offer = item.discountType === 2 ? `${value}折` : `减${value.toFixed(0)}`
  return min > 0 ? `满${min.toFixed(0)}${offer}` : offer
}

export function remainMs(end?: string | null, now = Date.now()) {
  if (!end) return 0
  const stamp = Date.parse(String(end).replace(' ', 'T'))
  if (Number.isNaN(stamp)) return 0
  return stamp - now
}

export function remainText(end?: string | null, now = Date.now()) {
  const ms = remainMs(end, now)
  if (ms <= 0) return '已结束'
  const total = Math.floor(ms / 1000)
  const d = Math.floor(total / 86400)
  const h = Math.floor((total % 86400) / 3600)
  const m = Math.floor((total % 3600) / 60)
  const s = total % 60
  if (d > 0) return `${d}天${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
  return `${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
}

export function stockPercent(sold?: number | null, stock?: number | null) {
  const taken = Number(sold || 0)
  const left = Number(stock || 0)
  const total = taken + left
  if (total <= 0) return 0
  return Math.min(100, Math.round((taken / total) * 100))
}

export function activityPhase(item?: { startTime?: string; endTime?: string; status?: number } | null, now = Date.now()) {
  if (!item) return '未知'
  if (item.status === 3) return '已下架'
  if (item.status === 2) return '已结束'
  const start = item.startTime ? Date.parse(String(item.startTime).replace(' ', 'T')) : 0
  const end = item.endTime ? Date.parse(String(item.endTime).replace(' ', 'T')) : 0
  if (start && now < start) return '未开始'
  if (end && now > end) return '已结束'
  return '进行中'
}

export function groupStatusText(status?: number) {
  const map: Record<number, string> = {
    0: '拼团中',
    1: '已成团',
    2: '已失败',
  }
  return status == null ? '未知' : map[status] || '未知'
}
