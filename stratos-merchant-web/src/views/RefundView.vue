<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useMessage } from 'naive-ui'
import { auditRefund, refundList } from '@/api'
import { useAuthStore } from '@/stores/auth'
import FormField from '@/components/FormField.vue'
import MerchantPage from '@/components/MerchantPage.vue'
import { minLen, textareaClass } from '@/utils/validate'

const statusText: Record<number, string> = {
  0: '待审核',
  1: '已通过',
  2: '已拒绝',
  10: '退款中',
  20: '退款成功',
  [-10]: '已取消',
}

const auth = useAuthStore()
const message = useMessage()
const list = ref<any[]>([])
const remarks = ref<Record<string, string>>({})
const remarkErrors = ref<Record<string, string>>({})

async function load() {
  if (!auth.shopId) return
  list.value = (await refundList(auth.shopId)) || []
}

async function decide(id: string | number, approved: boolean) {
  const key = String(id)
  const remark = (remarks.value[key] || '').trim()
  if (!approved) {
    const error = minLen(remark, 4, '拒绝原因')
    remarkErrors.value[key] = error
    if (error) {
      message.error(error)
      return
    }
  } else {
    remarkErrors.value[key] = ''
  }
  await auditRefund({ refundId: id, shopId: auth.shopId, approved, auditRemark: remark || (approved ? '商家同意退款' : '商家拒绝退款') })
  message.success(approved ? '已同意' : '已拒绝')
  await load()
}

onMounted(load)
</script>

<template>
  <MerchantPage title="售后" extra="买家申请退款后，在这里填写审核意见并处理">
    <div class="space-y-3">
      <div v-for="item in list" :key="item.id" class="admin-card space-y-3 p-5">
        <div class="flex items-center justify-between">
          <div class="font-medium">{{ item.refundNo }}</div>
          <span class="admin-badge" :class="item.status === 0 ? 'admin-badge-warn' : 'admin-badge-ok'">
            {{ statusText[item.status] || item.status }}
          </span>
        </div>
        <div class="text-sm text-mute">订单 {{ item.orderNo }} · ¥{{ Number(item.refundAmount || 0).toFixed(2) }}</div>
        <div class="text-sm">{{ item.refundDesc || '买家未填写说明' }}</div>
        <form v-if="item.status === 0" class="space-y-3" @submit.prevent>
          <FormField label="审核意见" :error="remarkErrors[String(item.id)]" hint="拒绝时必须填写至少 4 个字">
            <textarea
              v-model="remarks[String(item.id)]"
              :class="textareaClass(remarkErrors[String(item.id)])"
              placeholder="同意或拒绝时会带上这段说明"
              maxlength="120"
            />
          </FormField>
          <div class="space-x-2">
            <button type="button" class="admin-btn" @click="decide(item.id, true)">同意退款</button>
            <button type="button" class="admin-btn-danger" @click="decide(item.id, false)">拒绝</button>
          </div>
        </form>
      </div>
      <div v-if="!list.length" class="admin-card py-10 text-center text-sm text-mute">暂无售后单</div>
    </div>
  </MerchantPage>
</template>
