<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const title = ref('Solo Life OS')

onMounted(() => {
  // 未登录跳登录页，已登录跳今日首页（Sprint 2 起 Today 是用户登录后的第一眼）
  if (userStore.isLoggedIn) {
    uni.reLaunch({ url: '/pages/today/index' })
  } else {
    uni.reLaunch({ url: '/pages/login/index' })
  }
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
