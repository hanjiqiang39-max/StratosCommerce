<script setup lang="ts">
import { computed, ref } from 'vue'
import { useMessage } from 'naive-ui'
import { imageUrl } from '@/utils/image'

const props = defineProps<{
  modelValue?: string
  label?: string
  compact?: boolean
}>()

const emit = defineEmits<{
  'update:modelValue': [value: string]
}>()

const message = useMessage()
const input = ref<HTMLInputElement>()
const busy = ref(false)
const src = computed(() => imageUrl(props.modelValue))

function pick() {
  input.value?.click()
}

async function onChange(event: Event) {
  const file = (event.target as HTMLInputElement).files?.[0]
  ;(event.target as HTMLInputElement).value = ''
  if (!file) return
  if (!file.type.startsWith('image/') && !/\.(jpe?g|png|gif|webp|svg)$/i.test(file.name)) {
    message.error('请选择图片文件')
    return
  }
  if (file.size > 5 * 1024 * 1024) {
    message.error('图片不能超过 5MB')
    return
  }
  busy.value = true
  try {
    const { uploadImage } = await import('@/api')
    const data = await uploadImage(file)
    if (!data?.url) {
      message.error('上传失败')
      return
    }
    emit('update:modelValue', data.url)
    message.success('图片已上传')
  } catch {
    // 错误已由请求拦截器提示
  } finally {
    busy.value = false
  }
}
</script>

<template>
  <div class="flex items-center gap-3">
    <button
      type="button"
      class="relative overflow-hidden rounded-2xl border border-dashed border-slate-200 bg-slate-50 text-left"
      :class="compact ? 'h-16 w-16' : 'h-24 w-24'"
      :disabled="busy"
      @click="pick"
    >
      <img v-if="src" :src="src" alt="" class="h-full w-full object-cover" />
      <div v-else class="flex h-full w-full flex-col items-center justify-center px-1 text-center text-[11px] leading-4 text-mute">
        {{ busy ? '上传中' : '上传图片' }}
      </div>
    </button>
    <div v-if="!compact" class="min-w-0 space-y-1.5">
      <div class="text-sm font-medium">{{ label || '商品主图' }}</div>
      <div class="text-xs text-mute">支持 jpg / png / webp，不超过 5MB。无图时商城会显示占位。</div>
      <div class="flex gap-2">
        <button type="button" class="admin-btn" :disabled="busy" @click="pick">
          {{ src ? '更换图片' : '选择图片' }}
        </button>
        <button v-if="src" type="button" class="admin-btn-danger" @click="emit('update:modelValue', '')">移除</button>
      </div>
    </div>
    <input ref="input" type="file" accept="image/*" class="hidden" @change="onChange" />
  </div>
</template>
