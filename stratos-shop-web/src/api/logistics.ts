import { get } from '@/utils/request'

export const traceByOrder = (orderNo: string) => get<unknown[]>(`/logistics/trace/order/${orderNo}`)
