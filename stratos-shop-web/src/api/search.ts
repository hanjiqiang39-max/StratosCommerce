import { get } from '@/utils/request'

export const searchProducts = (params: Record<string, unknown>) =>
  get<{ records?: unknown[]; list?: unknown[]; total?: number; items?: unknown[] }>('/search/products', params)

export const suggest = (keyword: string) => get<string[]>('/search/suggest', { keyword })

export const hotWords = () => get<string[]>('/search/hot')
