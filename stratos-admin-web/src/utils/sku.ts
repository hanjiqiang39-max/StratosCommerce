export type SkuLike = {
  id?: string | number
  skuName?: string
  displayName?: string
  productTitle?: string
  title?: string
  stock?: number | string
  lowStockThreshold?: number | string
}

export function skuLabel(item?: SkuLike | null) {
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

export function skuTitle(item?: SkuLike | null) {
  return String(item?.productTitle || item?.title || '').trim()
}

export function skuSpec(item?: SkuLike | null) {
  const spec = String(item?.skuName || '').trim()
  const title = skuTitle(item)
  if (!spec || spec === '默认规格' || spec === title) return '标准款'
  return spec
}

export function chartLabel(text: string, max = 8) {
  const value = String(text || '').trim()
  return value.length > max ? `${value.slice(0, max)}…` : value || '标准款'
}

export function stockOf(item?: SkuLike | null) {
  return Number(item?.stock || 0)
}

export function stockTone(item?: SkuLike | null) {
  const stock = stockOf(item)
  const warn = Number(item?.lowStockThreshold || 10)
  if (stock <= 0) return 'empty'
  if (stock <= warn) return 'warn'
  return 'ok'
}
