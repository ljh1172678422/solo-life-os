<script setup lang="ts">
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'

const title = ref('Solo Life OS')

onLoad(() => {
  console.log('Index page loaded')
})
</script>

<template>
  <view class="container">
    <text class="title">{{ title }}</text>
    <text class="subtitle">AI Native Solo Living Assistant</text>
  </view>
</template>

<style scoped>
.container {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40rpx;
}

.title {
  font-size: 48rpx;
  font-weight: bold;
  margin-bottom: 20rpx;
}

.subtitle {
  font-size: 28rpx;
  color: #666;
}
</style>
