<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { getUserPreference, updateUserPreference } from '@/api/user'
import { ApiError } from '@/api/request'
import { useUserStore } from '@/stores/user'
import type { BudgetLevel, UserPreference } from '@/api/types'

const userStore = useUserStore()

const preference = ref<UserPreference | null>(null)
const editInterest = ref('')
const editBudget = ref<BudgetLevel | ''>('')
const editLifestyle = ref('')
const loading = ref(false)
const saving = ref(false)
const errorMsg = ref('')
const successMsg = ref('')

const budgetOptions: BudgetLevel[] = ['LOW', 'MEDIUM', 'HIGH']

onMounted(async () => {
  await loadPreference()
})

async function loadPreference(): Promise<void> {
  if (!userStore.userId) {
    uni.reLaunch({ url: '/pages/login/index' })
    return
  }
  loading.value = true
  try {
    const data = await getUserPreference(userStore.userId)
    preference.value = data
    editInterest.value = data.interest ?? ''
    editBudget.value = data.budget ?? ''
    editLifestyle.value = data.lifestyle ?? ''
  } catch (e) {
    errorMsg.value = e instanceof ApiError ? e.message : '加载偏好失败'
  } finally {
    loading.value = false
  }
}

async function save(): Promise<void> {
  if (!userStore.userId) return
  saving.value = true
  errorMsg.value = ''
  successMsg.value = ''
  try {
    const updated = await updateUserPreference(userStore.userId, {
      interest: editInterest.value.trim() || null,
      budget: editBudget.value || null,
      lifestyle: editLifestyle.value.trim() || null,
    })
    preference.value = updated
    successMsg.value = '保存成功'
  } catch (e) {
    errorMsg.value = e instanceof ApiError ? e.message : '保存失败'
  } finally {
    saving.value = false
  }
}

function goBack(): void {
  uni.navigateBack()
}
</script>

<template>
  <view class="page">
    <view class="header">
      <text class="title">偏好设置</text>
      <text class="subtitle">个性化你的 Solo Life 体验</text>
    </view>

    <text v-if="loading" class="loading">加载中...</text>
    <text v-if="errorMsg" class="error">{{ errorMsg }}</text>
    <text v-if="successMsg" class="success">{{ successMsg }}</text>

    <view class="card">
      <view class="form-item">
        <text class="label">兴趣</text>
        <textarea
          v-model="editInterest"
          class="textarea"
          placeholder="例如：阅读、徒步、咖啡、摄影"
          maxlength="500"
        />
      </view>

      <view class="form-item">
        <text class="label">预算等级</text>
        <view class="budget-options">
          <view
            v-for="opt in budgetOptions"
            :key="opt"
            class="budget-item"
            :class="{ active: editBudget === opt }"
            @click="editBudget = opt"
          >
            <text>{{ opt === 'LOW' ? '低' : opt === 'MEDIUM' ? '中' : '高' }}</text>
          </view>
        </view>
      </view>

      <view class="form-item">
        <text class="label">生活方式</text>
        <textarea
          v-model="editLifestyle"
          class="textarea"
          placeholder="例如：早睡早起、素食、独居"
          maxlength="500"
        />
      </view>

      <view class="actions">
        <button class="btn-primary" :disabled="saving" @click="save">
          {{ saving ? '保存中...' : '保存偏好' }}
        </button>
        <button class="btn-link" @click="goBack">返回</button>
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

.subtitle {
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

.success {
  display: block;
  color: #4caf50;
  font-size: 26rpx;
  margin-bottom: 24rpx;
}

.card {
  background-color: #fff;
  border-radius: 16rpx;
  padding: 40rpx;
}

.form-item {
  margin-bottom: 36rpx;
}

.label {
  display: block;
  font-size: 28rpx;
  color: #333;
  margin-bottom: 12rpx;
}

.textarea {
  width: 100%;
  min-height: 120rpx;
  padding: 20rpx 24rpx;
  border: 1rpx solid #e0e0e0;
  border-radius: 8rpx;
  font-size: 28rpx;
  box-sizing: border-box;
}

.budget-options {
  display: flex;
  gap: 16rpx;
}

.budget-item {
  flex: 1;
  height: 72rpx;
  line-height: 72rpx;
  text-align: center;
  border: 1rpx solid #e0e0e0;
  border-radius: 8rpx;
  font-size: 28rpx;
  color: #666;
}

.budget-item.active {
  background-color: #4a90d9;
  color: #fff;
  border-color: #4a90d9;
}

.actions {
  display: flex;
  flex-direction: column;
  margin-top: 24rpx;
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

.btn-link {
  height: 80rpx;
  line-height: 80rpx;
  background-color: transparent;
  color: #666;
  font-size: 28rpx;
  border: none;
}
</style>
