<script setup lang="ts">
import { onLaunch } from '@dcloudio/uni-app'

onLaunch(() => {
  console.log('Solo Life OS launched')
})
</script>

<template>
  <view>
    <router-view />
  </view>
</template>

<style>
/* Global styles */
</style>
