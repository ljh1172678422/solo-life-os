<script setup lang="ts">
/**
 * Page 05: 今日总结。
 *
 * 设计稿：designs/solo-life-os-mobile/pages/today-summary.html
 * 文档：designs/solo-life-os-mobile/docs/01-today-module-review.md §Page 05
 *
 * 当前实现：按设计稿 1:1 还原布局，纯静态 mock 展示（暂不接后端）。
 *
 * 信息架构（按设计稿）：
 * - 顶部导航（sticky 毛玻璃）：关闭
 * - 问候区：🌙 + "今天过得怎么样？" + 日期
 * - 完成度统计卡：环形进度（rotate -90deg）+ 完成项/总项 + 4 格进度条
 * - 完成项列表（已完成 Check 打勾、未完成淡化 + "没关系，明天继续"）
 * - 心情快速选择（4 卡，选中 scale(1.15)）
 * - AI 今晚的话（温暖寄语卡，flex items-start 横向布局）
 * - 底部 CTA：晚安，明天见
 *
 * 设计原则（温柔兜底）：
 * - 零完成态温暖文案，绝不"今天什么都没做"
 * - 情感留存：完成度低也要正反馈
 */
import { ref, computed } from 'vue'
import { X, Check, Sparkles, Moon } from 'lucide-vue-next'

/* ---------- 静态 Mock 数据 ---------- */

interface ActivityItem {
  id: number
  title: string
  completed: boolean
}

const mockDate = '7月28日 · 周二'
const mockActivities: ActivityItem[] = [
  { id: 1, title: '去了隐山咖啡', completed: true },
  { id: 2, title: '植物园散步', completed: true },
  { id: 3, title: '看了《死亡诗社》', completed: true },
  { id: 4, title: '阅读30分钟', completed: false },
]

interface MoodOption {
  key: string
  emoji: string
  label: string
}

const MOODS: MoodOption[] = [
  { key: 'great', emoji: '🙂', label: '很棒' },
  { key: 'good', emoji: '😊', label: '不错' },
  { key: 'normal', emoji: '😐', label: '一般' },
  { key: 'tired', emoji: '😔', label: '有点累' },
]

/* ---------- 状态 ---------- */

const selectedMood = ref<string>('')

const total = computed(() => mockActivities.length)
const completedCount = computed(
  () => mockActivities.filter((a) => a.completed).length,
)
const completionRate = computed(() => {
  if (total.value === 0) return 0
  return Math.round((completedCount.value / total.value) * 100)
})

/** 环形进度 SVG 周长（r=34）。设计稿 stroke-dasharray=213.6 */
const CIRCUMFERENCE = 2 * Math.PI * 34 // ≈ 213.628
const strokeOffset = computed(() => {
  const ratio = completionRate.value / 100
  return CIRCUMFERENCE * (1 - ratio)
})

/** 完成度提示（温柔文案）。 */
const completionHint = computed(() => {
  if (total.value === 0) return '今天没有计划，休息也是生活的一部分'
  if (completionRate.value === 0) return '今天主要是休息，明天再开始也不迟'
  if (completionRate.value >= 75) return `完成度 ${completionRate.value}%，很棒！`
  if (completionRate.value >= 25) return `完成度 ${completionRate.value}%，已经不错了`
  return `完成度 ${completionRate.value}%，明天继续`
})

/** AI 温暖寄语（固定模板，TASK-0207 接入后替换为 Planner Agent）。 */
const aiMessage =
  '今天也辛苦了。去了新的咖啡店，走了很多路。不管完成多少，你今天都好好生活了。早点休息，明天又是新的一天。'

/* ---------- 交互 ---------- */

function selectMood(key: string): void {
  selectedMood.value = key
}

function markComplete(activity: ActivityItem): void {
  // 静态 mock：就地标记完成，不发请求
  activity.completed = true
  uni.showToast({ title: '记下这件小事 🌿', icon: 'success' })
}

function goodnight(): void {
  uni.showToast({ title: '晚安，明天见 🌙', icon: 'success' })
  setTimeout(() => {
    uni.reLaunch({ url: '/pages/today/index' })
  }, 800)
}

function goClose(): void {
  uni.navigateBack({
    fail: () => uni.reLaunch({ url: '/pages/today/index' }),
  })
}
</script>

<template>
  <view class="page">
    <!-- 顶部关闭导航（sticky 毛玻璃） -->
    <view class="close-nav">
      <view class="close-nav-inner">
        <view class="close-btn" @click="goClose">
          <X :size="20" :stroke-width="2" :color="'#1C1917'" />
        </view>
      </view>
    </view>

    <view class="content">
      <!-- 问候区 -->
      <view class="greeting">
        <text class="greeting-emoji">🌙</text>
        <text class="greeting-title">今天过得怎么样？</text>
        <text class="greeting-date">{{ mockDate }}</text>
      </view>

      <!-- 完成度统计卡 -->
      <view class="stats-card">
        <view class="stats-row">
          <view class="progress-ring">
            <!-- #ifdef H5 -->
            <svg class="ring-svg" viewBox="0 0 80 80">
              <circle
                cx="40"
                cy="40"
                r="34"
                fill="none"
                :stroke="'#F5F5F4'"
                stroke-width="6"
              />
              <circle
                class="ring-progress"
                cx="40"
                cy="40"
                r="34"
                fill="none"
                :stroke="'#F97316'"
                stroke-width="6"
                stroke-linecap="round"
                :stroke-dasharray="CIRCUMFERENCE"
                :stroke-dashoffset="strokeOffset"
              />
            </svg>
            <!-- #endif -->
            <!-- #ifndef H5 -->
            <view class="ring-fallback">
              <text class="ring-fallback-text">{{ completedCount }}/{{ total }}</text>
            </view>
            <!-- #endif -->
            <view class="ring-center">
              <text class="ring-count">{{ completedCount }}/{{ total }}</text>
            </view>
          </view>

          <view class="stats-info">
            <text class="stats-title">
              今天完成了 {{ completedCount }}/{{ total }} 项安排
            </text>
            <text class="stats-sub">{{ completionHint }}</text>
            <view class="stats-bars">
              <view
                v-for="i in total"
                :key="i"
                class="stats-bar"
                :class="{ filled: i <= completedCount }"
              ></view>
            </view>
          </view>
        </view>
      </view>

      <!-- 完成项列表 -->
      <view class="list-card">
        <template v-for="a in mockActivities" :key="a.id">
          <!-- 已完成 -->
          <view v-if="a.completed" class="list-item">
            <view class="list-check list-check-done">
              <Check :size="14" :stroke-width="3" :color="'#22C55E'" />
            </view>
            <text class="list-text list-text-done">{{ a.title }}</text>
            <text class="list-status list-status-done">已完成</text>
          </view>
          <!-- 未完成 -->
          <view v-else class="list-item">
            <view class="list-check list-check-pending">
              <view class="list-check-dot"></view>
            </view>
            <view class="list-text-wrap">
              <text class="list-text list-text-pending">{{ a.title }}</text>
              <text class="list-text-sub">没关系，明天继续</text>
            </view>
            <text class="list-action" @click="markComplete(a)">补完成</text>
          </view>
        </template>
      </view>

      <!-- 心情选择 -->
      <view class="mood-section">
        <text class="mood-title">此刻的心情是？</text>
        <view class="mood-list">
          <view
            v-for="mood in MOODS"
            :key="mood.key"
            class="mood-btn"
            :class="{ selected: selectedMood === mood.key }"
            @click="selectMood(mood.key)"
          >
            <text class="mood-emoji">{{ mood.emoji }}</text>
            <text class="mood-label">{{ mood.label }}</text>
          </view>
        </view>
      </view>

      <!-- AI 寄语卡（横向布局） -->
      <view class="ai-card">
        <view class="ai-card-body">
          <view class="ai-avatar">
            <Sparkles :size="16" :stroke-width="2" :color="'#C2410C'" />
          </view>
          <view class="ai-text-wrap">
            <text class="ai-message">{{ aiMessage }}</text>
            <text class="ai-emoji">💛</text>
          </view>
        </view>
      </view>

      <!-- 底部 CTA -->
      <view class="bottom-cta">
        <view class="btn-goodnight" @click="goodnight">
          <Moon :size="16" :stroke-width="2" />
          <text class="btn-goodnight-text">晚安，明天见</text>
        </view>
      </view>
    </view>
  </view>
</template>

<style scoped lang="scss">
/* ===== 页面背景：暖色夜色渐变（按设计稿） ===== */
.page {
  min-height: 100vh;
  background: linear-gradient(180deg, $solo-primary-50 0%, $solo-primary-100 30%, $solo-neutral-50 70%);
}

/* ===== 顶部关闭导航（sticky 毛玻璃） ===== */
.close-nav {
  position: sticky;
  top: 0;
  z-index: 30;
  background-color: $solo-summary-glass-bg;
  backdrop-filter: blur(24rpx);
  -webkit-backdrop-filter: blur(24rpx);
  margin: 0 -32rpx;
  padding: 0 32rpx;
}

.close-nav-inner {
  max-width: 896rpx;
  margin: 0 auto;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  height: 96rpx;
}

.close-btn {
  width: 72rpx;
  height: 72rpx;
  margin-right: -16rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background-color 0.15s;
}

/* ===== 内容区 ===== */
.content {
  max-width: 896rpx;
  margin: 0 auto;
  padding: 16rpx 0 64rpx;
  display: flex;
  flex-direction: column;
  gap: 40rpx; /* space-y-5 ≈ 20px = 40rpx */
}

/* ===== 问候区 ===== */
.greeting {
  text-align: center;
  padding-top: 8rpx;
}

.greeting-emoji {
  display: block;
  font-size: 60rpx; /* text-3xl ≈ 30px = 60rpx */
  margin-bottom: 16rpx;
}

.greeting-title {
  display: block;
  font-size: 48rpx; /* text-2xl ≈ 24px = 48rpx */
  font-weight: 700;
  color: $solo-foreground;
}

.greeting-date {
  display: block;
  font-size: 28rpx; /* text-sm ≈ 14px = 28rpx */
  color: $solo-muted-foreground;
  margin-top: 12rpx; /* mt-1.5 */
}

/* ===== 完成度统计卡 ===== */
.stats-card {
  background-color: $solo-card;
  border-radius: $solo-radius-lg; /* rounded-2xl = 16px */
  border: 1rpx solid $solo-border;
  padding: 40rpx; /* p-5 = 20px */
  box-shadow: $solo-shadow-sm;
}

.stats-row {
  display: flex;
  align-items: center;
  gap: 32rpx;
}

.progress-ring {
  position: relative;
  width: 160rpx; /* w-20 = 80px */
  height: 160rpx;
  flex-shrink: 0;
}

.ring-svg {
  width: 160rpx;
  height: 160rpx;
}

/* 设计稿：progress-ring__circle { transform: rotate(-90deg); transform-origin: 50% 50%; } */
.ring-progress {
  transition: stroke-dashoffset 0.5s ease;
  transform: rotate(-90deg);
  transform-origin: 50% 50%;
}

.ring-fallback {
  width: 160rpx;
  height: 160rpx;
  border-radius: 50%;
  background-color: $solo-primary-50;
  display: flex;
  align-items: center;
  justify-content: center;
}

.ring-fallback-text {
  font-size: 28rpx;
  font-weight: 700;
  color: $solo-primary-700;
}

.ring-center {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.ring-count {
  font-size: 32rpx; /* text-lg font-bold */
  font-weight: 700;
  color: $solo-foreground;
}

.stats-info {
  flex: 1;
  min-width: 0;
}

.stats-title {
  display: block;
  font-size: 30rpx; /* text-base font-semibold */
  font-weight: 600;
  color: $solo-foreground;
}

.stats-sub {
  display: block;
  font-size: 26rpx; /* text-sm */
  color: $solo-muted-foreground;
  margin-top: 8rpx; /* mt-1 */
}

.stats-bars {
  display: flex;
  gap: 8rpx; /* gap-1 */
  margin-top: 16rpx; /* mt-2 */
}

.stats-bar {
  height: 12rpx; /* h-1.5 ≈ 6px */
  flex: 1;
  border-radius: $solo-radius-full;
  background-color: $solo-muted;

  &.filled {
    background-color: $solo-primary-500;
  }
}

/* ===== 完成项列表 ===== */
.list-card {
  background-color: $solo-card;
  border-radius: $solo-radius-lg; /* rounded-2xl */
  border: 1rpx solid $solo-border;
  overflow: hidden;
  box-shadow: $solo-shadow-sm;
}

.list-item {
  display: flex;
  align-items: center;
  gap: 24rpx; /* gap-3 = 12px */
  padding: 32rpx; /* p-4 = 16px */
  border-bottom: 1rpx solid $solo-border;

  &:last-child {
    border-bottom: none;
  }
}

.list-check {
  width: 48rpx; /* w-6 = 24px */
  height: 48rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.list-check-done {
  background-color: rgba(34, 197, 94, 0.15); /* bg-state-success/15 */
}

.list-check-pending {
  background-color: $solo-muted;
}

.list-check-dot {
  width: 16rpx; /* w-2 = 8px */
  height: 16rpx;
  border-radius: 50%;
  background-color: rgba(120, 113, 108, 0.4); /* muted-foreground/40 */
}

.list-text-wrap {
  flex: 1;
  min-width: 0;
}

.list-text {
  font-size: 28rpx; /* text-sm */
}

.list-text-done {
  flex: 1;
  color: $solo-foreground;
  text-decoration: line-through;
  text-decoration-color: rgba(120, 113, 108, 0.4); /* decoration-muted-foreground/40 */
}

.list-text-pending {
  color: $solo-muted-foreground;
}

.list-text-sub {
  display: block;
  font-size: 22rpx; /* text-xs */
  color: $solo-neutral-400;
  margin-top: 4rpx; /* mt-0.5 */
}

.list-status-done {
  font-size: 22rpx; /* text-xs */
  color: $solo-state-success;
}

.list-action {
  font-size: 24rpx;
  color: $solo-primary-500;
  padding: 8rpx 16rpx;
}

/* ===== 心情选择 ===== */
.mood-section {
  display: flex;
  flex-direction: column;
  gap: 24rpx; /* space-y-3 */
}

.mood-title {
  font-size: 28rpx; /* text-sm font-semibold */
  font-weight: 600;
  color: $solo-foreground;
  padding: 0 8rpx; /* px-1 */
}

.mood-list {
  display: flex;
  justify-content: space-between;
  gap: 16rpx; /* gap-2 */
}

.mood-btn {
  flex: 1;
  background-color: $solo-card;
  border: 1rpx solid $solo-border;
  border-radius: $solo-radius-md; /* rounded-xl */
  padding: 24rpx 0; /* py-3 */
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
  transition: all 0.2s ease;

  &.selected {
    border-color: $solo-primary-400;
    background-color: $solo-primary-50;
    transform: scale(1.15); /* 设计稿：mood-btn.selected { scale(1.15) } */
  }
}

.mood-emoji {
  font-size: 48rpx; /* text-2xl */
}

.mood-label {
  font-size: 22rpx; /* text-xs */
  color: $solo-muted-foreground;
}

/* ===== AI 寄语卡（横向布局） ===== */
.ai-card {
  border-radius: $solo-radius-lg; /* rounded-2xl */
  padding: 40rpx; /* p-5 */
  background: linear-gradient(135deg, $solo-primary-50 0%, $solo-primary-100 50%, $solo-primary-200 100%);
  border: 1rpx solid $solo-primary-100;
  position: relative;
  overflow: hidden;
}

.ai-card-body {
  display: flex;
  align-items: flex-start; /* items-start */
  gap: 24rpx; /* gap-3 */
  position: relative;
  z-index: 1;
}

.ai-avatar {
  width: 64rpx; /* w-8 = 32px */
  height: 64rpx;
  border-radius: 50%;
  background-color: rgba(249, 115, 22, 0.15);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.ai-text-wrap {
  flex: 1;
  min-width: 0;
}

.ai-message {
  display: block;
  font-size: 28rpx; /* text-sm */
  color: $solo-primary-900;
  line-height: 1.7; /* leading-relaxed */
}

.ai-emoji {
  display: block;
  font-size: 36rpx; /* text-lg */
  margin-top: 16rpx; /* mt-2 */
}

/* ===== 底部 CTA ===== */
.bottom-cta {
  padding-top: 8rpx; /* pt-2 */
}

.btn-goodnight {
  width: 100%;
  height: 96rpx; /* h-12 = 48px */
  background-color: $solo-primary-500;
  color: #fff;
  border-radius: $solo-radius-md; /* rounded-xl */
  font-size: 32rpx; /* text-base */
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  transition: opacity 0.15s;
  /* 设计稿 box-shadow: 0 4px 14px rgba(249,115,22,0.3); → 对应 $solo-shadow-primary-sm */
  box-shadow: $solo-shadow-primary-sm;
}

.btn-goodnight-text {
  font-size: 32rpx;
  font-weight: 600;
  color: inherit;
}
</style>
