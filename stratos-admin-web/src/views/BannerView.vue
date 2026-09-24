<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useMessage } from 'naive-ui'
import { bannerAdminList, deleteBanner, saveBanner } from '@/api'
import AdminPage from '@/components/AdminPage.vue'
import FormField from '@/components/FormField.vue'
import ProductImagePicker from '@/components/ProductImagePicker.vue'
import { imageUrl } from '@/utils/image'
import { firstError, inputClass, maxLen, required } from '@/utils/validate'

const message = useMessage()
const list = ref<any[]>([])
const errors = reactive({ title: '', imageUrl: '', linkUrl: '' })
const form = reactive({ id: '' as string | number | '', title: '', imageUrl: '', linkUrl: '/', status: 1, position: 1 })

async function load() {
  list.value = (await bannerAdminList()) || []
}

function reset() {
  Object.assign(form, { id: '', title: '', imageUrl: '', linkUrl: '/', status: 1, position: 1 })
  Object.assign(errors, { title: '', imageUrl: '', linkUrl: '' })
}

function edit(item: any) {
  Object.assign(form, {
    id: item.id,
    title: item.title || '',
    imageUrl: item.imageUrl || '',
    linkUrl: item.linkUrl || '/',
    status: item.status ?? 1,
    position: item.position ?? 1,
  })
}

async function save() {
  Object.assign(errors, {
    title: required(form.title, '标题') || maxLen(form.title, 30, '标题'),
    imageUrl: required(form.imageUrl, '轮播图'),
    linkUrl: required(form.linkUrl, '跳转地址'),
  })
  const error = firstError(errors)
  if (error) {
    message.error(error)
    return
  }
  await saveBanner({ ...form, id: form.id || undefined })
  message.success('轮播已保存')
  reset()
  await load()
}

async function remove(item: any) {
  if (!window.confirm(`确定删除轮播「${item.title}」？`)) return
  await deleteBanner(item.id)
  message.success('已删除')
  await load()
}

onMounted(load)
</script>

<template>
  <AdminPage title="轮播" extra="首页顶部大图。建议上传本地图片，避免外链失效。">
    <form class="admin-card grid grid-cols-2 gap-4 p-5" @submit.prevent="save">
      <div class="col-span-2">
        <ProductImagePicker v-model="form.imageUrl" label="轮播图" />
        <p v-if="errors.imageUrl" class="mt-1 text-xs text-rose-500">{{ errors.imageUrl }}</p>
      </div>
      <FormField label="标题" required :error="errors.title">
        <input v-model="form.title" :class="inputClass(errors.title)" maxlength="30" placeholder="例如：秋季上新" />
      </FormField>
      <FormField label="跳转" required hint="商城路由，如 /seckill" :error="errors.linkUrl">
        <input v-model="form.linkUrl" :class="inputClass(errors.linkUrl)" placeholder="/category" />
      </FormField>
      <div class="col-span-2 flex gap-2">
        <button class="h-11 rounded-full bg-sky-500 px-6 text-white">{{ form.id ? '更新轮播' : '保存轮播' }}</button>
        <button v-if="form.id" type="button" class="admin-btn h-11" @click="reset">取消编辑</button>
      </div>
    </form>
    <div class="grid grid-cols-1 gap-4 md:grid-cols-2">
      <div v-for="item in list" :key="item.id" class="admin-card overflow-hidden">
        <div class="h-36 bg-slate-100">
          <img v-if="item.imageUrl" :src="imageUrl(item.imageUrl)" alt="" class="h-full w-full object-cover" />
        </div>
        <div class="flex items-center justify-between p-5">
          <div>
            <div class="font-medium">{{ item.title }}</div>
            <div class="mt-1 text-sm text-mute">{{ item.linkUrl }}</div>
          </div>
          <div class="space-x-2">
            <button class="admin-btn" @click="edit(item)">编辑</button>
            <button class="admin-btn-danger" @click="remove(item)">删除</button>
          </div>
        </div>
      </div>
      <div v-if="!list.length" class="admin-card py-10 text-center text-sm text-mute">暂无轮播</div>
    </div>
  </AdminPage>
</template>
