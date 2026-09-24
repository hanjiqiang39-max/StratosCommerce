import { get } from '@/utils/request'
import type { Category, PageResult, ProductDetail, ProductSpu } from './types'

export const listProducts = (params: Record<string, unknown>) =>
  get<PageResult<ProductSpu>>('/product/list', params)

export const getCategories = () => get<Category[]>('/product/categories')

export const getProduct = (id: number | string) => get<ProductDetail>(`/product/${id}`)

export const getShop = (shopId: number | string) => get<{ shopName?: string; shopDesc?: string }>(`/merchant/shop/public/${shopId}`)

export const getProductsByCategory = (categoryId: number, pageNum = 1, pageSize = 20) =>
  get<PageResult<ProductSpu>>(`/product/category/${categoryId}`, { pageNum, pageSize })
