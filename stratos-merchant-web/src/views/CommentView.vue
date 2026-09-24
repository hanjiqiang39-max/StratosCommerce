<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useMessage } from 'naive-ui'
import { commentList, replyComment } from '@/api'
import { useAuthStore } from '@/stores/auth'
import FormField from '@/components/FormField.vue'
import MerchantPage from '@/components/MerchantPage.vue'
import { maxLen, minLen, required, textareaClass } from '@/utils/validate'

const auth = useAuthStore()
const message = useMessage()
const list = ref<any[]>([])
const drafts = ref<Record<string, string>>({})
const draftErrors = ref<Record<string, string>>({})

async function load() {
  if (!auth.shopId) return
  list.value = (await commentList(auth.shopId)) || []
}

async function reply(item: any) {
  const key = String(item.id)
  const content = (drafts.value[key] || '').trim()
  const error = required(content, '回复内容') || minLen(content, 2, '回复内容') || maxLen(content, 200, '回复内容')
  draftErrors.value[key] = error
  if (error) {
    message.error(error)
    return
  }
  await replyComment(item.id, auth.shopId!, content)
  message.success('已回复')
  drafts.value[String(item.id)] = ''
  await load()
}

onMounted(load)
</script>

<template>
  <MerchantPage title="评价" extra="回复会出现在商城商品详情">
    <div class="space-y-3">
      <div v-for="item in list" :key="item.id" class="admin-card space-y-3 p-5">
        <div class="font-medium">{{ item.nickname }} · {{ item.starRating }} 星</div>
        <div class="text-sm text-mute">{{ item.content }}</div>
        <div v-if="item.replyContent" class="rounded-xl bg-teal-50 px-3 py-2 text-sm">已回复：{{ item.replyContent }}</div>
        <form class="space-y-2" @submit.prevent="reply(item)">
          <FormField :label="item.replyContent ? '追加 / 修改回复' : '回复买家'" :error="draftErrors[String(item.id)]">
            <textarea
              v-model="drafts[String(item.id)]"
              :class="textareaClass(draftErrors[String(item.id)])"
              placeholder="感谢支持，欢迎再次光临"
              maxlength="200"
            />
          </FormField>
          <button type="submit" class="admin-btn h-10">发送回复</button>
        </form>
      </div>
      <div v-if="!list.length" class="admin-card py-10 text-center text-sm text-mute">暂无评价</div>
    </div>
  </MerchantPage>
</template>
