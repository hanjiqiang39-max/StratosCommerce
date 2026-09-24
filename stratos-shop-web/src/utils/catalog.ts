import type { Category, ProductSpu } from '@/api/types'
import { getProduct } from '@/api/product'
import { productPrice } from './format'

export interface FlatCategory {
  id: number
  categoryName: string
  parentId?: number
  depth: number
  children?: Category[]
}

const CATEGORY_ICONS = [
  'solar:smartphone-bold-duotone',
  'solar:hanger-2-bold-duotone',
  'solar:chef-hat-bold-duotone',
  'solar:home-2-bold-duotone',
  'solar:gamepad-bold-duotone',
  'solar:heart-bold-duotone',
  'solar:leaf-bold-duotone',
  'solar:box-bold-duotone',
]

export function categoryIcon(index = 0) {
  return CATEGORY_ICONS[Math.abs(index) % CATEGORY_ICONS.length]
}

export function flattenCategories(list: Category[] = [], depth = 0): FlatCategory[] {
  return list.flatMap((item) => [
    { id: item.id, categoryName: item.categoryName, parentId: item.parentId, depth, children: item.children },
    ...flattenCategories(item.children || [], depth + 1),
  ])
}

export function sameId(a?: number | string | null, b?: number | string | null) {
  return a != null && b != null && String(a) === String(b)
}

export function collectCategoryIds(item?: Category | FlatCategory | null): Array<number | string> {
  if (!item) return []
  return [item.id, ...(item.children || []).flatMap((child) => collectCategoryIds(child))]
}

export function findCategory(list: Category[] = [], id?: number | string | null): Category | undefined {
  if (id == null || id === '') return undefined
  for (const item of list) {
    if (sameId(item.id, id)) return item
    const child = findCategory(item.children || [], id)
    if (child) return child
  }
  return undefined
}

export async function hydrateProducts(list: ProductSpu[]) {
  await Promise.all(
    list.map(async (item) => {
      if (productPrice(item) > 0 && item.mainImage) return
      try {
        const detail = await getProduct(item.id)
        item.minPrice = productPrice(detail)
        item.mainImage = item.mainImage || detail.mainImage
        item.sellingPoint = item.sellingPoint || detail.sellingPoint
        item.title = item.title || detail.title
      } catch {
        /* keep list item */
      }
    }),
  )
  return list
}
