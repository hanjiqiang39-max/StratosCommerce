import { get, post } from '@/utils/request'
import type { Comment } from './types'

export const listComments = (spuId: number) => get<Comment[]>(`/comment/product/${spuId}`)

export const addComment = (payload: Record<string, unknown>) => post<number>('/comment', payload)
