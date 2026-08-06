<script setup lang="ts">
/**
 * Page 03: AI 重新规划。
 *
 * 设计稿：designs/solo-life-os-mobile/pages/today-replan.html
 * 文档：designs/solo-life-os-mobile/docs/01-today-module-review.md §Page 03
 *
 * 当前实现：按设计稿 1:1 还原布局，纯静态 mock 展示（暂不接 Planner Agent）。
 *
 * 信息架构：
 * - 顶部 sticky 导航（毛玻璃）：返回 + "重新规划"
 * - 标题：计划变了？ + 副标题
 * - 原因选择 5 卡（单选）：下雨/累/预算/时间/换心情
 * - 调整偏好折叠面板：3 个 toggle（就近 / 独处 / 室内优先）
 * - AI 虚线占位提示卡
 * - 底部固定 CTA："生成新计划"
 */
import { ref } from 'vue'
import {
  ChevronLeft,
  CloudRain,
  Moon,
  Wallet,
  Clock,
  Shuffle,
  SlidersHorizontal,
  ChevronDown,
  Sparkles,
} from 'lucide-vue-next'

/* ---------- 静态 Mock 数据 ---------- */

interface ReasonOption {
  key: string
  label: string
}

const REASONS: ReasonOption[] = [
  { key: 'rain', label: '下雨了不想出门' },
  { key: 'tired', label: '今天有点累想休息' },
  { key: 'budget', label: '预算有限想省钱' },
  { key: 'time', label: '临时有事时间变少' },
  { key: 'mood', label: '就是想换个心情' },
]

function getReasonIcon(key: string) {
  switch (key) {
    case 'rain':
      return CloudRain
    case 'tired':
      return Moon
    case 'budget':
      return Wallet
    case 'time':
      return Clock
    case 'mood':
      return Shuffle
    default:
      return Sparkles
  }
}

interface PreferenceToggle {
  key: string
  label: string
  desc: string
  on: boolean
}

/* ---------- 状态 ---------- */

const selectedReason = ref<string>('')
const preferencesExpanded = ref(false)
const preferences = ref<PreferenceToggle[]>([
  { key: 'nearby', label: '不想走路（想就近）', desc: '推荐1公里范围内的活动', on: false },
  { key: 'solo', label: '想独处', desc: '避开社交和人群活动', on: false },
  { key: 'indoor', label: '室内优先', desc: '适合天气不好的时候', on: false },
])
const submitting = ref(false)

/* ---------- 交互 ---------- */

function selectReason(key: string): void {
  selectedReason.value = key
}

function togglePreference(pref: PreferenceToggle): void {
  pref.on = !pref.on
}

function togglePreferencesPanel(): void {
  preferencesExpanded.value = !preferencesExpanded.value
}

function submitReplan(): void {
  if (!selectedReason.value) {
    uni.showToast({ title: '请选择调整原因', icon: 'none' })
    return
  }
  submitting.value = true
  // 静态 mock：提交后提示，后续 Sprint 5 接 Planner Agent
  setTimeout(() => {
    submitting.value = false
    uni.showModal({
      title: '已记录你的需求',
      content: `AI 已收到你的反馈。新规划能力正在路上，先回到首页吧。`,
      showCancel: false,
      confirmText: '回到首页',
      success: () => uni.reLaunch({ url: '/pages/today/index' }),
    })
  }, 600)
}

function goBack(): void {
  uni.navigateBack({
    fail: () => uni.reLaunch({ url: '/pages/today/index' }),
  })
}
</script>

<template>
  <view class="page">
    <!-- ===== 顶部导航 ===== -->
    <view class="top-nav">
      <view class="top-nav-inner">
        <view class="nav-btn" @click="goBack">
          <ChevronLeft :size="20" :stroke-width="2" color="#1C1917" />
        </view>
        <text class="nav-title">重新规划</text>
        <view class="nav-placeholder"></view>
      </view>
    </view>

    <!-- ===== 内容区 ===== -->
    <view class="content">
      <!-- 标题区 -->
      <view class="title-block">
        <text class="title">计划变了？</text>
        <text class="subtitle">告诉AI怎么了，它帮你调整</text>
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
              <component
                :is="getReasonIcon(reason.key)"
                :size="20"
                :stroke-width="2"
                color="#C2410C"
              />
            </view>
            <text class="reason-label">{{ reason.label }}</text>
            <view
              class="radio-circle"
              :class="{ selected: selectedReason === reason.key }"
            >
              <view v-if="selectedReason === reason.key" class="radio-inner"></view>
            </view>
          </view>
        </view>
      </view>

      <!-- 调整偏好（折叠面板） -->
      <view class="preference-card">
        <view class="preference-header" @click="togglePreferencesPanel">
          <SlidersHorizontal :size="16" :stroke-width="2" color="#78716C" />
          <text class="preference-header-label">调整偏好</text>
          <ChevronDown
            class="preference-chevron"
            :class="{ expanded: preferencesExpanded }"
            :size="16"
            :stroke-width="2"
            color="#78716C"
          />
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

      <!-- AI 虚线占位卡 -->
      <view class="ai-placeholder">
        <view class="ai-placeholder-icon-wrap">
          <Sparkles :size="20" :stroke-width="2" color="#F97316" />
        </view>
        <text class="ai-placeholder-text">AI 会根据你的选择重新安排今天</text>
      </view>
    </view>

    <!-- ===== 底部固定 CTA ===== -->
    <view class="bottom-bar">
      <view class="bottom-bar-inner">
        <view
          class="btn-submit"
          :class="{ disabled: submitting || !selectedReason }"
          @click="submitReplan"
        >
          <Sparkles :size="16" :stroke-width="2" />
          <text class="btn-submit-text">
            {{ submitting ? '生成中...' : '生成新计划' }}
          </text>
        </view>
      </view>
    </view>
  </view>
</template>

<style scoped lang="scss">
.page {
  min-height: 100vh;
  background-color: $solo-background;
  padding-bottom: 200rpx;
  position: relative;
}

/* ===== 顶部导航 ===== */
.top-nav {
  position: sticky;
  top: 0;
  z-index: 30;
  background-color: $solo-glass-bg;
  backdrop-filter: blur(24rpx);
  -webkit-backdrop-filter: blur(24rpx);
  border-bottom: 1rpx solid $solo-border;
  margin: 0 -32rpx; /* 与内容 margin 对齐 */
  padding: 0 32rpx;
}

.top-nav-inner {
  max-width: 896rpx;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 96rpx;
  position: relative;
}

.nav-btn {
  width: 72rpx;
  height: 72rpx;
  margin-left: -16rpx;
  border-radius: 50%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.nav-title {
  font-size: 32rpx;
  font-weight: 600;
  color: $solo-foreground;
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
}

.nav-placeholder {
  width: 72rpx;
}

/* ===== 内容区 ===== */
.content {
  max-width: 896rpx;
  margin: 0 auto;
  padding: 24rpx 0 0; /* 内部不做左右 padding，section 自带 */
}

/* ===== 标题区 ===== */
.title-block {
  padding: 16rpx 8rpx 24rpx;
}

.title {
  display: block;
  font-size: 44rpx;
  font-weight: 700;
  color: $solo-foreground;
}

.subtitle {
  display: block;
  font-size: 28rpx;
  color: $solo-muted-foreground;
  margin-top: 12rpx;
}

/* ===== 原因选择 ===== */
.section {
  margin-bottom: 32rpx;
}

.section-label {
  display: block;
  font-size: 22rpx;
  font-weight: 500;
  color: $solo-muted-foreground;
  padding: 0 8rpx;
  margin-bottom: 20rpx;
  letter-spacing: 2rpx;
  text-transform: uppercase;
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
  border: 2rpx solid $solo-border;
  border-radius: $solo-radius-md;
  padding: 32rpx;
  transition: all 0.2s ease;

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

.reason-label {
  flex: 1;
  font-size: 28rpx;
  font-weight: 500;
  color: $solo-foreground;
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
  transition: all 0.2s;

  &.selected {
    border-color: $solo-primary-500;
    background-color: $solo-primary-500;
  }
}

.radio-inner {
  width: 16rpx;
  height: 16rpx;
  border-radius: 50%;
  background-color: #fff;
}

/* ===== 调整偏好折叠面板 ===== */
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

.preference-header-label {
  flex: 1;
  font-size: 28rpx;
  font-weight: 500;
  color: $solo-foreground;
}

.preference-chevron {
  transition: transform 0.2s;
  flex-shrink: 0;

  &.expanded {
    transform: rotate(180deg);
  }
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
  padding-right: 24rpx;
}

.preference-label {
  display: block;
  font-size: 28rpx;
  color: $solo-foreground;
}

.preference-desc {
  display: block;
  font-size: 24rpx;
  color: $solo-muted-foreground;
  margin-top: 8rpx;
  line-height: 1.4;
}

/* toggle switch */
.toggle-switch {
  width: 88rpx;
  height: 48rpx;
  border-radius: 9999rpx;
  background-color: $solo-neutral-300;
  position: relative;
  transition: background-color 0.2s;
  flex-shrink: 0;

  &.on {
    background-color: $solo-primary-500;
  }
}

.toggle-knob {
  position: absolute;
  top: 4rpx;
  left: 4rpx;
  width: 40rpx;
  height: 40rpx;
  border-radius: 50%;
  background-color: #fff;
  transition: transform 0.2s;
  box-shadow: 0 2rpx 6rpx rgba(0, 0, 0, 0.15);
}

.toggle-switch.on .toggle-knob {
  transform: translateX(40rpx);
}

/* ===== AI 虚线占位卡 ===== */
.ai-placeholder {
  background-color: $solo-card;
  border: 2rpx dashed $solo-border;
  border-radius: $solo-radius-md;
  padding: 48rpx 32rpx;
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

.ai-placeholder-text {
  font-size: 28rpx;
  color: $solo-muted-foreground;
}

/* ===== 底部固定 CTA ===== */
.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background-color: $solo-glass-bg;
  backdrop-filter: blur(24rpx);
  -webkit-backdrop-filter: blur(24rpx);
  border-top: 1rpx solid $solo-border;
  padding: 24rpx 32rpx;
  z-index: 30;
}

.bottom-bar-inner {
  max-width: 896rpx;
  margin: 0 auto;
}

.btn-submit {
  width: 100%;
  height: 96rpx;
  background-color: $solo-primary-500;
  color: #fff;
  border-radius: $solo-radius-md;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  transition: opacity 0.15s;

  &.disabled {
    background-color: $solo-neutral-300;
    color: $solo-neutral-500;
    pointer-events: none;
  }
}

.btn-submit-text {
  font-size: 28rpx;
  font-weight: 600;
  color: inherit;
}
</style>
