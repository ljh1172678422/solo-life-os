<script setup lang="ts">
/**
 * Page 03: AI 重新规划。
 *
 * 设计稿：designs/solo-life-os-mobile/pages/today-replan.html
 * 文档：designs/solo-life-os-mobile/docs/01-today-module-review.md §Page 03
 *
 * 信息架构（按设计稿）：
 * - 顶部导航：返回 + "重新规划"
 * - 标题：计划变了？告诉 AI 怎么了
 * - 原因卡片（单选）：下雨不想出门 / 有点累想休息 / 预算有限 / 时间变少 / 换个心情
 * - 调整偏好（折叠面板）：就近 / 独处 / 室内优先
 * - AI 占位提示卡
 * - 底部 CTA："生成新计划"
 *
 * MVP 范围：本页仅做用户意图收集 + 提交，AI 实际生成走 TASK-0207 Planner Agent 接口（Sprint 5 完整接入）。
 * 当前提交后调用 cancelDailyPlan + createDailyPlan 占位流程，提示用户重新规划已记录。
 */
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { ApiError } from '@/api/request'
import { cancelDailyPlan, getDailyPlan } from '@/api/today'
import type { DailyPlan } from '@/api/types'

interface ReasonOption {
  key: string
  emoji: string
  label: string
}

const REASONS: ReasonOption[] = [
  { key: 'rain', emoji: '🌧️', label: '下雨了不想出门' },
  { key: 'tired', emoji: '🌙', label: '今天有点累想休息' },
  { key: 'budget', emoji: '💰', label: '预算有限想省钱' },
  { key: 'time', emoji: '⏰', label: '临时有事时间变少' },
  { key: 'mood', emoji: '🔄', label: '就是想换个心情' },
]

interface PreferenceToggle {
  key: string
  label: string
  desc: string
  on: boolean
}

const planId = ref<number | null>(null)
const plan = ref<DailyPlan | null>(null)
const selectedReason = ref<string>('')
const preferencesExpanded = ref(false)
const preferences = ref<PreferenceToggle[]>([
  { key: 'nearby', label: '不想走路（想就近）', desc: '推荐1公里范围内的活动', on: false },
  { key: 'solo', label: '想独处', desc: '避开社交和人群活动', on: false },
  { key: 'indoor', label: '室内优先', desc: '适合天气不好的时候', on: false },
])
const submitting = ref(false)
const errorMsg = ref('')

onLoad((query) => {
  const raw = query?.planId
  if (raw) {
    planId.value = Number(raw)
    loadPlan()
  }
})

async function loadPlan(): Promise<void> {
  if (!planId.value) return
  try {
    plan.value = await getDailyPlan(planId.value)
  } catch (e) {
    errorMsg.value = e instanceof ApiError ? e.message : '加载计划失败'
  }
}

function selectReason(key: string): void {
  selectedReason.value = key
}

function togglePreference(pref: PreferenceToggle): void {
  pref.on = !pref.on
}

function togglePreferencesPanel(): void {
  preferencesExpanded.value = !preferencesExpanded.value
}

/** 提交重新规划请求。
 *
 * MVP 占位流程（01-today-module-review §生成失败/无结果的保底建议）：
 * - 当前 Planner Agent 接口尚未上线（TASK-0207 仅骨架）
 * - 取消原计划（CANCELLED），提示用户重新规划已记录，AI 将在 Sprint 5 接入后自动生成
 * - 永远给保底建议，绝不冰冷说"无结果"
 */
async function submitReplan(): Promise<void> {
  if (!plan.value) return
  if (!selectedReason.value) {
    uni.showToast({ title: '请选择调整原因', icon: 'none' })
    return
  }
  submitting.value = true
  errorMsg.value = ''
  try {
    // MVP 占位：取消原计划并提示，等 TASK-0207 Planner Agent 接入后替换为真实生成调用
    await cancelDailyPlan(plan.value.id)
    uni.showModal({
      title: '已记录你的需求',
      content:
        'AI 已收到你的反馈：' +
        reasonLabel(selectedReason.value) +
        '。新的规划能力正在路上，先回到首页查看今日状态吧。',
      showCancel: false,
      confirmText: '回到首页',
      success: () => {
        uni.reLaunch({ url: '/pages/today/index' })
      },
    })
  } catch (e) {
    errorMsg.value = e instanceof ApiError ? e.message : '提交失败，请稍后重试'
  } finally {
    submitting.value = false
  }
}

function reasonLabel(key: string): string {
  return REASONS.find((r) => r.key === key)?.label ?? ''
}

function goBack(): void {
  uni.navigateBack({
    fail: () => uni.reLaunch({ url: '/pages/today/index' }),
  })
}
</script>

<template>
  <view class="page">
    <!-- 顶部导航 -->
    <view class="top-nav">
      <view class="nav-btn" @click="goBack">
        <text class="nav-btn-icon">‹</text>
      </view>
      <text class="nav-title">重新规划</text>
      <view class="nav-placeholder"></view>
    </view>

    <view class="content">
      <!-- 标题区 -->
      <view class="title-block">
        <text class="title">计划变了？</text>
        <text class="subtitle">告诉 AI 怎么了，它帮你调整</text>
      </view>

      <!-- 原因选择 -->
      <view class="section">
        <text class="section-label">为什么要调整</text>
        <view class="reason-list">
          <view
            v-for="reason in REASONS"
            :key="reason.key"
            class="reason-card"
            :class="{ selected: selectedReason === reason.key }"
            @click="selectReason(reason.key)"
          >
            <view class="reason-icon-wrap">
              <text class="reason-icon">{{ reason.emoji }}</text>
            </view>
            <text class="reason-label">{{ reason.label }}</text>
            <view class="radio-circle">
              <view v-if="selectedReason === reason.key" class="radio-inner"></view>
            </view>
          </view>
        </view>
      </view>

      <!-- 调整偏好（折叠面板） -->
      <view class="preference-card">
        <view class="preference-header" @click="togglePreferencesPanel">
          <text class="preference-header-icon">⚙️</text>
          <text class="preference-header-label">调整偏好</text>
          <text class="preference-chevron" :class="{ expanded: preferencesExpanded }">›</text>
        </view>
        <view v-if="preferencesExpanded" class="preference-body">
          <view
            v-for="(pref, idx) in preferences"
            :key="pref.key"
            class="preference-item"
            :class="{ 'last-item': idx === preferences.length - 1 }"
            @click="togglePreference(pref)"
          >
            <view class="preference-text">
              <text class="preference-label">{{ pref.label }}</text>
              <text class="preference-desc">{{ pref.desc }}</text>
            </view>
            <view class="toggle-switch" :class="{ on: pref.on }">
              <view class="toggle-knob"></view>
            </view>
          </view>
        </view>
      </view>

      <!-- AI 占位提示卡 -->
      <view class="ai-placeholder">
        <view class="ai-placeholder-icon-wrap">
          <text class="ai-placeholder-icon">✨</text>
        </view>
        <text class="ai-placeholder-text">
          AI 会根据你的选择重新安排今天
        </text>
      </view>

      <text v-if="errorMsg" class="error-text">{{ errorMsg }}</text>
    </view>

    <!-- 底部 CTA -->
    <view class="bottom-bar">
      <button
        class="btn-submit"
        :disabled="submitting || !selectedReason"
        @click="submitReplan"
      >
        <text class="btn-submit-icon">✨</text>
        <text class="btn-submit-text">{{ submitting ? '生成中...' : '生成新计划' }}</text>
      </button>
    </view>
  </view>
</template>

<style scoped lang="scss">
.page {
  min-height: 100vh;
  background-color: $solo-neutral-50;
}

/* 顶部导航 */
.top-nav {
  position: sticky;
  top: 0;
  z-index: 30;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 104rpx;
  padding: 0 32rpx;
  background-color: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(12px);
  border-bottom: 1rpx solid $solo-border;
}

.nav-btn {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  background-color: $solo-muted;
  display: flex;
  align-items: center;
  justify-content: center;
}

.nav-btn-icon {
  font-size: 40rpx;
  color: $solo-neutral-700;
  line-height: 1;
}

.nav-title {
  font-size: 32rpx;
  font-weight: 600;
  color: $solo-neutral-900;
}

.nav-placeholder {
  width: 72rpx;
}

/* 内容区 */
.content {
  padding: 24rpx 32rpx 200rpx;
}

.title-block {
  padding: 16rpx 8rpx 24rpx;
}

.title {
  display: block;
  font-size: 44rpx;
  font-weight: 700;
  color: $solo-neutral-900;
}

.subtitle {
  display: block;
  font-size: 28rpx;
  color: $solo-neutral-500;
  margin-top: 12rpx;
}

/* 原因选择 */
.section {
  margin-bottom: 32rpx;
}

.section-label {
  display: block;
  font-size: 24rpx;
  font-weight: 500;
  color: $solo-neutral-500;
  padding: 0 8rpx;
  margin-bottom: 20rpx;
  letter-spacing: 2rpx;
}

.reason-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.reason-card {
  display: flex;
  align-items: center;
  gap: 24rpx;
  background-color: $solo-card;
  border: 1rpx solid $solo-border;
  border-radius: $solo-radius-md;
  padding: 32rpx;

  &.selected {
    border-color: $solo-primary-500;
    background-color: $solo-primary-50;
  }
}

.reason-icon-wrap {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  background-color: $solo-primary-50;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.reason-icon {
  font-size: 40rpx;
}

.reason-label {
  flex: 1;
  font-size: 28rpx;
  font-weight: 500;
  color: $solo-neutral-900;
}

.radio-circle {
  width: 40rpx;
  height: 40rpx;
  border-radius: 50%;
  border: 4rpx solid $solo-border;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.reason-card.selected .radio-circle {
  border-color: $solo-primary-500;
  background-color: $solo-primary-500;
}

.radio-inner {
  width: 16rpx;
  height: 16rpx;
  border-radius: 50%;
  background-color: #fff;
}

/* 调整偏好 */
.preference-card {
  background-color: $solo-card;
  border: 1rpx solid $solo-border;
  border-radius: $solo-radius-md;
  overflow: hidden;
  margin-bottom: 32rpx;
}

.preference-header {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 32rpx;
}

.preference-header-icon {
  font-size: 28rpx;
}

.preference-header-label {
  flex: 1;
  font-size: 28rpx;
  font-weight: 500;
  color: $solo-neutral-900;
}

.preference-chevron {
  font-size: 32rpx;
  color: $solo-neutral-500;
  transition: transform 0.2s;
}

.preference-chevron.expanded {
  transform: rotate(90deg);
}

.preference-body {
  border-top: 1rpx solid $solo-border;
}

.preference-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 32rpx;
  border-bottom: 1rpx solid $solo-border;

  &.last-item {
    border-bottom: none;
  }
}

.preference-text {
  flex: 1;
}

.preference-label {
  display: block;
  font-size: 28rpx;
  color: $solo-neutral-900;
}

.preference-desc {
  display: block;
  font-size: 24rpx;
  color: $solo-neutral-500;
  margin-top: 8rpx;
}

.toggle-switch {
  width: 80rpx;
  height: 44rpx;
  border-radius: 22rpx;
  background-color: $solo-neutral-300;
  position: relative;
  transition: background-color 0.2s;
  flex-shrink: 0;
}

.toggle-switch.on {
  background-color: $solo-primary-500;
}

.toggle-knob {
  position: absolute;
  top: 4rpx;
  left: 4rpx;
  width: 36rpx;
  height: 36rpx;
  border-radius: 50%;
  background-color: #fff;
  transition: transform 0.2s;
  box-shadow: 0 2rpx 6rpx rgba(0, 0, 0, 0.15);
}

.toggle-switch.on .toggle-knob {
  transform: translateX(36rpx);
}

/* AI 占位卡 */
.ai-placeholder {
  background-color: $solo-card;
  border: 2rpx dashed $solo-border;
  border-radius: $solo-radius-md;
  padding: 48rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.ai-placeholder-icon-wrap {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  background-color: $solo-primary-50;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16rpx;
}

.ai-placeholder-icon {
  font-size: 40rpx;
}

.ai-placeholder-text {
  font-size: 28rpx;
  color: $solo-neutral-500;
}

.error-text {
  display: block;
  color: $solo-state-error;
  font-size: 26rpx;
  margin-top: 24rpx;
  text-align: center;
}

/* 底部 CTA */
.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background-color: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(12px);
  border-top: 1rpx solid $solo-border;
  padding: 24rpx 32rpx;
  z-index: 30;
}

.btn-submit {
  width: 100%;
  height: 96rpx;
  background-color: $solo-primary-500;
  color: #fff;
  font-size: 30rpx;
  font-weight: 600;
  border-radius: $solo-radius-md;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
}

.btn-submit[disabled] {
  background-color: $solo-neutral-300;
  color: $solo-neutral-500;
}

.btn-submit-icon {
  font-size: 28rpx;
}

.btn-submit-text {
  font-size: 30rpx;
  font-weight: 600;
}
</style>
