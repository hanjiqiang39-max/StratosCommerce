import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { addCart, deleteCart, listCart, selectAllCart, selectCart, updateCartQty } from '@/api/cart'
import type { CartItem } from '@/api/types'
import { useAuthStore } from './auth'

export const useCartStore = defineStore('cart', () => {
  const items = ref<CartItem[]>([])

  const count = computed(() => items.value.reduce((sum, item) => sum + (item.quantity || 0), 0))

  async function refresh() {
    const auth = useAuthStore()
    if (!auth.userId) {
      items.value = []
      return
    }
    items.value = (await listCart(auth.userId)) || []
  }

  async function add(skuId: number, spuId: number, quantity = 1) {
    const auth = useAuthStore()
    if (!auth.userId) throw new Error('请先登录')
    await addCart({ userId: auth.userId, skuId, spuId, quantity })
    await refresh()
  }

  async function changeQty(cartId: number, quantity: number) {
    const auth = useAuthStore()
    if (!auth.userId) return
    await updateCartQty(cartId, auth.userId, quantity)
    await refresh()
  }

  async function remove(cartId: number) {
    const auth = useAuthStore()
    if (!auth.userId) return
    await deleteCart(cartId, auth.userId)
    await refresh()
  }

  async function removeMany(cartIds: Array<number | string>) {
    const auth = useAuthStore()
    if (!auth.userId || !cartIds.length) return
    await Promise.all(cartIds.map((id) => deleteCart(id, auth.userId!)))
    await refresh()
  }

  async function toggle(cartId: number, selected: number) {
    const auth = useAuthStore()
    if (!auth.userId) return
    await selectCart(cartId, auth.userId, selected)
    await refresh()
  }

  async function toggleAll(selected: number) {
    const auth = useAuthStore()
    if (!auth.userId) return
    await selectAllCart(auth.userId, selected)
    await refresh()
  }

  return { items, count, refresh, add, changeQty, remove, removeMany, toggle, toggleAll }
})
