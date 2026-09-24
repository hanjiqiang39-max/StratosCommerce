<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useMessage } from 'naive-ui'
import { deleteNotice, noticeAdminList, publishNotice, saveNotice } from '@/api'
import AdminPage from '@/components/AdminPage.vue'
import FormField from '@/components/FormField.vue'
import { firstError, inputClass, maxLen, minLen, required, textareaClass } from '@/utils/validate'

const message = useMessage()
const list = ref<any[]>([])
const errors = reactive({ title: '', content: '' })
const form = reactive({ id: '' as string | number | '', title: '', content: '', publishStatus: 1 })

async function load() {
  list.value = (await noticeAdminList()) || []
}

function reset() {
  Object.assign(form, { id: '', title: '', content: '', publishStatus: 1 })
  Object.assign(errors, { title: '', content: '' })
}

function edit(item: any) {
  Object.assign(form, {
    id: item.id,
    title: item.title || '',
    content: item.content || '',
    publishStatus: item.publishStatus ?? 1,
  })
}

async function save() {
  Object.assign(errors, {
    title: required(form.title, '标题') || minLen(form.title, 2, '标题') || maxLen(form.title, 40, '标题'),
    content: required(form.content, '内容') || minLen(form.content, 4, '内容') || maxLen(form.content, 500, '内容'),
  })
  const error = firstError(errors)
  if (error) {
    message.error(error)
    return
  }
  await saveNotice({ ...form, id: form.id || undefined })
  message.success('公告已保存')
  reset()
  await load()
}

async function toggle(item: any) {
  await publishNotice(item.id, item.publishStatus === 1 ? 2 : 1)
  message.success(item.publishStatus === 1 ? '已下架' : '已发布')
  await load()
}

async function remove(item: any) {
  if (!window.confirm(`确定删除公告「${item.title}」？`)) return
  await deleteNotice(item.id)
  message.success('已删除')
  await load()
}

onMounted(load)
</script>

<template>
  <AdminPage title="公告" extra="发布后会展示在商城首页和公告页">
    <form class="admin-card space-y-4 p-5" @submit.prevent="save">
      <FormField label="标题" required :error="errors.title">
        <input v-model="form.title" :class="inputClass(errors.title)" maxlength="40" placeholder="例如：支付说明" />
      </FormField>
      <FormField label="内容" required :error="errors.content">
        <textarea v-model="form.content" :class="textareaClass(errors.content)" maxlength="500" placeholder="公告正文" />
      </FormField>
      <div class="flex gap-2">
        <button class="h-11 rounded-full bg-sky-500 px-6 text-white">{{ form.id ? '更新公告' : '保存公告' }}</button>
        <button v-if="form.id" type="button" class="admin-btn h-11" @click="reset">取消编辑</button>
      </div>
    </form>
    <div class="admin-card divide-y divide-slate-100">
      <div v-for="item in list" :key="item.id" class="flex items-center justify-between px-5 py-4">
        <div>
          <div class="font-medium">{{ item.title }}</div>
          <div class="mt-1 line-clamp-1 text-xs text-mute">{{ item.content }}</div>
        </div>
        <div class="flex items-center gap-2">
          <span :class="item.publishStatus === 1 ? 'admin-badge admin-badge-ok' : 'admin-badge admin-badge-mute'">
            {{ item.publishStatus === 1 ? '已发布' : '未发布' }}
          </span>
          <button class="admin-btn" @click="edit(item)">编辑</button>
          <button class="admin-btn" @click="toggle(item)">{{ item.publishStatus === 1 ? '下架' : '发布' }}</button>
          <button class="admin-btn-danger" @click="remove(item)">删除</button>
        </div>
      </div>
      <div v-if="!list.length" class="px-5 py-10 text-center text-sm text-mute">暂无公告</div>
    </div>
  </AdminPage>
</template>
