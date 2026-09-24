<script setup lang="ts">
import { ref, watch } from 'vue'
import { PLACEHOLDER_IMAGE, imageUrl } from '@/utils/format'

const props = defineProps<{
  src?: string | null
  alt?: string
  fallback?: string
}>()

const failed = ref(false)
const resolved = ref(imageUrl(props.src) || props.fallback || PLACEHOLDER_IMAGE)

watch(
  () => props.src,
  (value) => {
    failed.value = false
    resolved.value = imageUrl(value) || props.fallback || PLACEHOLDER_IMAGE
  },
)

function onError() {
  if (resolved.value !== (props.fallback || PLACEHOLDER_IMAGE)) {
    resolved.value = props.fallback || PLACEHOLDER_IMAGE
    return
  }
  failed.value = true
}
</script>

<template>
  <img v-if="!failed" :src="resolved" :alt="alt || ''" @error="onError" />
  <div v-else class="flex h-full w-full items-center justify-center bg-slate-100 text-sm text-mute">暂无图片</div>
</template>
