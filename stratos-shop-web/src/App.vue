<script setup lang="ts">
import { NConfigProvider, NMessageProvider, dateZhCN, zhCN } from 'naive-ui'

const themeOverrides = {
  common: {
    fontFamily: '"Microsoft YaHei UI", "Microsoft YaHei", "PingFang SC", "Noto Sans SC", sans-serif',
  },
}
</script>

<template>
  <NConfigProvider
    preflight-style-disabled
    :locale="zhCN"
    :date-locale="dateZhCN"
    :theme-overrides="themeOverrides"
  >
    <NMessageProvider>
      <RouterView />
    </NMessageProvider>
  </NConfigProvider>
</template>
