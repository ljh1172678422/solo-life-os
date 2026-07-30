<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { getUser, updateUserProfile } from '@/api/user'
import { ApiError } from '@/api/request'
import { useUserStore } from '@/stores/user'
import type { UserProfile } from '@/api/types'

const userStore = useUserStore()

const profile = ref<UserProfile | null>(null)
const editNickname = ref('')
const editAvatar = ref('')
const editCity = ref('')
const editing = ref(false)
const loading = ref(false)
const errorMsg = ref('')

onMounted(async () => {
  await loadProfile()
})

async function loadProfile(): Promise<void> {
  if (!userStore.userId) {
    uni.reLaunch({ url: '/pages/login/index' })
    return
  }
  loading.value = true
  try {
    const data = await getUser(userStore.userId)
    profile.value = data
    userStore.setUser(data)
    editNickname.value = data.nickname
    editAvatar.value = data.avatar ?? ''
    editCity.value = data.city ?? ''
  } catch (e) {
    errorMsg.value = e instanceof ApiError ? e.message : '加载资料失败'
  } finally {
    loading.value = false
  }
}

function startEdit(): void {
  editing.value = true
  errorMsg.value = ''
}

function cancelEdit(): void {
  editing.value = false
  if (profile.value) {
    editNickname.value = profile.value.nickname
    editAvatar.value = profile.value.avatar ?? ''
    editCity.value = profile.value.city ?? ''
  }
}

async function saveProfile(): Promise<void> {
  if (!userStore.userId || !editNickname.value.trim()) {
    errorMsg.value = '昵称不可为空'
    return
  }
  loading.value = true
  errorMsg.value = ''
  try {
    const updated = await updateUserProfile(userStore.userId, {
      nickname: editNickname.value.trim(),
      avatar: editAvatar.value.trim() || null,
      city: editCity.value.trim() || null,
    })
    profile.value = updated
    userStore.setUser(updated)
    editing.value = false
  } catch (e) {
    errorMsg.value = e instanceof ApiError ? e.message : '保存失败'
  } finally {
    loading.value = false
  }
}

function goPreference(): void {
  uni.navigateTo({ url: '/pages/preference/index' })
}

function logout(): void {
  userStore.clearAuth()
  uni.reLaunch({ url: '/pages/login/index' })
}

function goIndex(): void {
  uni.reLaunch({ url: '/pages/index/index' })
}
</script>

<template>
  <view class="page">
    <view class="header">
      <text class="title">个人资料</text>
      <text v-if="profile" class="welcome">你好，{{ profile.nickname }}</text>
    </view>

    <text v-if="loading && !profile" class="loading">加载中...</text>
    <text v-if="errorMsg" class="error">{{ errorMsg }}</text>

    <view v-if="profile && !editing" class="card">
      <view class="row">
        <text class="row-label">昵称</text>
        <text class="row-value">{{ profile.nickname }}</text>
      </view>
      <view class="row">
        <text class="row-label">邮箱</text>
        <text class="row-value">{{ profile.email || '未设置' }}</text>
      </view>
      <view class="row">
        <text class="row-label">手机号</text>
        <text class="row-value">{{ profile.phone || '未设置' }}</text>
      </view>
      <view class="row">
        <text class="row-label">城市</text>
        <text class="row-value">{{ profile.city || '未设置' }}</text>
      </view>
      <view class="row">
        <text class="row-label">头像</text>
        <text class="row-value">{{ profile.avatar || '未设置' }}</text>
      </view>
      <view class="row">
        <text class="row-label">状态</text>
        <text class="row-value">{{ profile.status }}</text>
      </view>

      <view class="actions">
        <button class="btn-primary" @click="startEdit">编辑资料</button>
        <button class="btn-secondary" @click="goPreference">偏好设置</button>
        <button class="btn-link" @click="goIndex">返回首页</button>
        <button class="btn-link danger" @click="logout">退出登录</button>
      </view>
    </view>

    <view v-if="editing" class="card">
      <view class="form-item">
        <text class="label">昵称 *</text>
        <input v-model="editNickname" class="input" maxlength="50" />
      </view>
      <view class="form-item">
        <text class="label">头像 URL</text>
        <input v-model="editAvatar" class="input" maxlength="500" />
      </view>
      <view class="form-item">
        <text class="label">城市</text>
        <input v-model="editCity" class="input" maxlength="100" />
      </view>

      <view class="actions">
        <button class="btn-primary" :disabled="loading" @click="saveProfile">保存</button>
        <button class="btn-secondary" @click="cancelEdit">取消</button>
      </view>
    </view>
  </view>
</template>

<style scoped>
.page {
  min-height: 100vh;
  padding: 40rpx;
  background-color: #f7f8fa;
}

.header {
  margin-bottom: 40rpx;
}

.title {
  display: block;
  font-size: 48rpx;
  font-weight: bold;
  color: #1a1a1a;
  margin-bottom: 12rpx;
}

.welcome {
  font-size: 28rpx;
  color: #666;
}

.loading {
  font-size: 28rpx;
  color: #999;
  text-align: center;
}

.error {
  display: block;
  color: #e53935;
  font-size: 26rpx;
  margin-bottom: 24rpx;
}

.card {
  background-color: #fff;
  border-radius: 16rpx;
  padding: 40rpx;
  margin-bottom: 24rpx;
}

.row {
  display: flex;
  justify-content: space-between;
  padding: 24rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
}

.row:last-of-type {
  border-bottom: none;
}

.row-label {
  font-size: 28rpx;
  color: #666;
}

.row-value {
  font-size: 28rpx;
  color: #1a1a1a;
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

.actions {
  display: flex;
  flex-direction: column;
  margin-top: 32rpx;
}

.btn-primary {
  height: 80rpx;
  line-height: 80rpx;
  background-color: #4a90d9;
  color: #fff;
  font-size: 30rpx;
  border-radius: 8rpx;
  border: none;
  margin-bottom: 16rpx;
}

.btn-primary[disabled] {
  background-color: #a0c4eb;
}

.btn-secondary {
  height: 80rpx;
  line-height: 80rpx;
  background-color: #fff;
  color: #4a90d9;
  font-size: 30rpx;
  border-radius: 8rpx;
  border: 1rpx solid #4a90d9;
  margin-bottom: 16rpx;
}

.btn-link {
  height: 80rpx;
  line-height: 80rpx;
  background-color: transparent;
  color: #666;
  font-size: 28rpx;
  border: none;
}

.btn-link.danger {
  color: #e53935;
}
</style>
