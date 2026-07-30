<script setup lang="ts">
import { ref } from 'vue'
import { registerUser } from '@/api/user'
import { ApiError } from '@/api/request'

const nickname = ref('')
const email = ref('')
const phone = ref('')
const password = ref('')
const loading = ref(false)
const errorMsg = ref('')

async function handleRegister(): Promise<void> {
  if (!nickname.value.trim()) {
    errorMsg.value = '请填写昵称'
    return
  }
  if (!password.value || password.value.length < 6) {
    errorMsg.value = '密码至少 6 位'
    return
  }
  if (!email.value && !phone.value) {
    errorMsg.value = '邮箱或手机号至少填写一项'
    return
  }
  loading.value = true
  errorMsg.value = ''
  try {
    await registerUser({
      nickname: nickname.value.trim(),
      email: email.value.trim() || null,
      phone: phone.value.trim() || null,
      password: password.value,
    })
    // 注册成功跳转登录
    uni.redirectTo({ url: '/pages/login/index' })
  } catch (e) {
    errorMsg.value = e instanceof ApiError ? e.message : '注册失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

function goLogin(): void {
  uni.navigateBack()
}
</script>

<template>
  <view class="page">
    <view class="header">
      <text class="title">创建账号</text>
      <text class="subtitle">开启你的 Solo Life</text>
    </view>

    <view class="form">
      <view class="form-item">
        <text class="label">昵称 *</text>
        <input v-model="nickname" class="input" placeholder="最长 50 字符" maxlength="50" />
      </view>

      <view class="form-item">
        <text class="label">邮箱</text>
        <input v-model="email" class="input" placeholder="选填，与手机号二选一" maxlength="100" />
      </view>

      <view class="form-item">
        <text class="label">手机号</text>
        <input v-model="phone" class="input" placeholder="选填，与邮箱二选一" maxlength="20" />
      </view>

      <view class="form-item">
        <text class="label">密码 *</text>
        <input
          v-model="password"
          class="input"
          type="password"
          placeholder="6-100 位密码"
          maxlength="100"
        />
      </view>

      <text v-if="errorMsg" class="error">{{ errorMsg }}</text>

      <button class="btn-primary" :disabled="loading" @click="handleRegister">
        {{ loading ? '注册中...' : '注册' }}
      </button>

      <view class="footer">
        <text class="footer-text">已有账号？</text>
        <text class="link" @click="goLogin">返回登录</text>
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
  font-size: 48rpx;
  font-weight: bold;
  color: #1a1a1a;
  margin-bottom: 12rpx;
}

.subtitle {
  font-size: 28rpx;
  color: #666;
}

.form {
  background-color: #fff;
  border-radius: 16rpx;
  padding: 40rpx;
}

.form-item {
  margin-bottom: 28rpx;
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
