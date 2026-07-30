<script setup lang="ts">
import { ref } from 'vue'
import { login } from '@/api/user'
import { ApiError } from '@/api/request'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const account = ref('')
const password = ref('')
const loading = ref(false)
const errorMsg = ref('')

async function handleLogin(): Promise<void> {
  if (!account.value || !password.value) {
    errorMsg.value = '请填写账号和密码'
    return
  }
  loading.value = true
  errorMsg.value = ''
  try {
    const res = await login({ account: account.value.trim(), password: password.value })
    userStore.setAuth(res)
    // 登录成功跳转资料页（reLaunch 关闭登录页，避免返回）
    uni.reLaunch({ url: '/pages/profile/index' })
  } catch (e) {
    errorMsg.value = e instanceof ApiError ? e.message : '登录失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

function goRegister(): void {
  uni.navigateTo({ url: '/pages/register/index' })
}
</script>

<template>
  <view class="page">
    <view class="header">
      <text class="title">Solo Life OS</text>
      <text class="subtitle">登录</text>
    </view>

    <view class="form">
      <view class="form-item">
        <text class="label">账号</text>
        <input
          v-model="account"
          class="input"
          type="text"
          placeholder="邮箱或手机号"
          maxlength="100"
        />
      </view>

      <view class="form-item">
        <text class="label">密码</text>
        <input
          v-model="password"
          class="input"
          type="password"
          placeholder="6-100 位密码"
          maxlength="100"
        />
      </view>

      <text v-if="errorMsg" class="error">{{ errorMsg }}</text>

      <button class="btn-primary" :disabled="loading" @click="handleLogin">
        {{ loading ? '登录中...' : '登录' }}
      </button>

      <view class="footer">
        <text class="footer-text">还没有账号？</text>
        <text class="link" @click="goRegister">立即注册</text>
      </view>
    </view>
  </view>
</template>

<style scoped>
.page {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  padding: 60rpx 40rpx;
  background-color: #f7f8fa;
}

.header {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 60rpx;
}

.title {
  font-size: 56rpx;
  font-weight: bold;
  color: #1a1a1a;
  margin-bottom: 12rpx;
}

.subtitle {
  font-size: 32rpx;
  color: #666;
}

.form {
  background-color: #fff;
  border-radius: 16rpx;
  padding: 40rpx;
}

.form-item {
  margin-bottom: 32rpx;
}

.label {
  display: block;
  font-size: 28rpx;
  color: #333;
  margin-bottom: 12rpx;
}

.input {
  width: 100%;
  height: 80rpx;
  padding: 0 24rpx;
  border: 1rpx solid #e0e0e0;
  border-radius: 8rpx;
  font-size: 28rpx;
  box-sizing: border-box;
}

.error {
  display: block;
  color: #e53935;
  font-size: 26rpx;
  margin-bottom: 24rpx;
}

.btn-primary {
  width: 100%;
  height: 88rpx;
  line-height: 88rpx;
  background-color: #4a90d9;
  color: #fff;
  font-size: 32rpx;
  border-radius: 8rpx;
  border: none;
  margin-top: 16rpx;
}

.btn-primary[disabled] {
  background-color: #a0c4eb;
}

.footer {
  display: flex;
  justify-content: center;
  margin-top: 40rpx;
}

.footer-text {
  font-size: 28rpx;
  color: #666;
}

.link {
  font-size: 28rpx;
  color: #4a90d9;
  margin-left: 8rpx;
}
</style>
