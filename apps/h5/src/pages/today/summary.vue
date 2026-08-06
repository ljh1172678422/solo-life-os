<script setup lang="ts">
/**
 * Page 05: 今日总结。
 *
 * 设计稿：designs/solo-life-os-mobile/pages/today-summary.html
 * 文档：designs/solo-life-os-mobile/docs/01-today-module-review.md §Page 05
 *
 * 信息架构（按设计稿）：
 * - 顶部导航：关闭
 * - 问候区：🌙 + "今天过得怎么样？" + 日期
 * - 完成度统计卡：环形进度 + 完成项/总项
 * - 完成项列表（已完成打勾、未完成淡化）
 * - 心情快速选择
 * - AI 今晚的话（温暖寄语卡）
 * - 底部 CTA：晚安，明天见
 *
 * 设计原则（01-today-module-review §关键设计原则 §3, §5, §7）：
 * - 温柔兜底：零完成态文案温暖，绝不"今天什么都没做"
 * - AI 寄语可编辑（MVP 先展示固定模板，AI 接入后替换）
 * - 情感留存：完成度低也要正反馈
 */
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { ApiError } from '@/api/request'
import { completeDailyPlan, endActivity, getDailyPlan, listActivities } from '@/api/today'
import type { Activity, DailyPlan } from '@/api/types'

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

const planId = ref<number | null>(null)
const loading = ref(true)
const errorMsg = ref('')
const plan = ref<DailyPlan | null>(null)
const activities = ref<Activity[]>([])
const selectedMood = ref<string>('')
const submitting = ref(false)

const todayText = computed(() => {
  if (!plan.value) return ''
  const d = plan.value.date
  return `${d.slice(5)} · ${weekdayText(d)}`
})

const completedActivities = computed(() => activities.value.filter((a) => !!a.endTime))
const pendingActivities = computed(() => activities.value.filter((a) => !a.endTime))
const total = computed(() => activities.value.length)
const completedCount = computed(() => completedActivities.value.length)
const completionRate = computed(() => {
  if (total.value === 0) return 0
  return Math.round((completedCount.value / total.value) * 100)
})

/** 环形进度 SVG 周长（r=34，stroke-dasharray=213.6，对应设计稿）。 */
const CIRCUMFERENCE = 2 * Math.PI * 34
const strokeOffset = computed(() => {
  const ratio = completionRate.value / 100
  return CIRCUMFERENCE * (1 - ratio)
})

/** AI 寄语（MVP 模板，TASK-0207 接入后替换为 Planner Agent 生成内容）。 */
const aiMessage = computed(() => {
  if (total.value === 0) {
    return '今天或许只是需要休息，也是一件重要的事。早点睡，明天又是新的一天。'
  }
  if (completionRate.value >= 75) {
    return `今天也辛苦了。完成了 ${completedCount.value} 件事，每一件都是好好生活的证明。早点休息，明天又是新的一天。`
  }
  if (completionRate.value >= 25) {
    return `今天完成了 ${completedCount.value} 件事，已经很不错了。没做完的明天再说，先好好休息。`
  }
  return '今天辛苦了。不管完成多少，你今天都好好生活了。早点休息，明天又是新的一天。'
})

/** 完成度提示文案（温柔兜底，零完成态不说"什么都没做"）。 */
const completionHint = computed(() => {
  if (total.value === 0) return '今天没有计划，休息也是生活的一部分'
  if (completionRate.value === 0) return '今天主要是休息，明天再开始也不迟'
  if (completionRate.value >= 75) return `完成度 ${completionRate.value}%，很棒！`
  if (completionRate.value >= 25) return `完成度 ${completionRate.value}%，已经不错了`
  return `完成度 ${completionRate.value}%，明天继续`
})

onLoad((query) => {
  const raw = query?.planId
  if (raw) {
    planId.value = Number(raw)
    loadSummary()
  }
})

async function loadSummary(): Promise<void> {
  if (!planId.value) return
  loading.value = true
  errorMsg.value = ''
  try {
    const [p, list] = await Promise.all([
      getDailyPlan(planId.value),
      listActivities(planId.value),
    ])
    plan.value = p
    activities.value = list
  } catch (e) {
    errorMsg.value = e instanceof ApiError ? e.message : '加载总结失败'
  } finally {
    loading.value = false
  }
}

function selectMood(key: string): void {
  selectedMood.value = key
}

/** 手动补完一项活动（设计稿 §只完成部分：允许用户手动补充完成）。 */
async function markComplete(activity: Activity): Promise<void> {
  const now = new Date()
  const isoLocal = toLocalDateTimeIso(now)
  try {
    const updated = await endActivity(activity.id, { endTime: isoLocal })
    const idx = activities.value.findIndex((a) => a.id === activity.id)
    if (idx >= 0) activities.value[idx] = updated
    uni.showToast({ title: '记下这件小事 🌿', icon: 'success' })
  } catch (e) {
    uni.showToast({
      title: e instanceof ApiError ? e.message : '操作失败',
      icon: 'none',
    })
  }
}

/** 完成今日计划：ONGOING → COMPLETED，并回到首页。 */
async function goodnight(): Promise<void> {
  if (!plan.value) return
  submitting.value = true
  try {
    if (plan.value.status === 'ONGOING') {
      await completeDailyPlan(plan.value.id)
    }
    uni.showToast({ title: '晚安，明天见 🌙', icon: 'success' })
    setTimeout(() => {
      uni.reLaunch({ url: '/pages/today/index' })
    }, 800)
  } catch (e) {
    uni.showToast({
      title: e instanceof ApiError ? e.message : '操作失败',
      icon: 'none',
    })
  } finally {
    submitting.value = false
  }
}

function goClose(): void {
  uni.navigateBack({
    fail: () => uni.reLaunch({ url: '/pages/today/index' }),
  })
}

function weekdayText(iso: string): string {
  const d = new Date(`${iso}T00:00:00`)
  const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  return weekdays[d.getDay()]
}

function toLocalDateTimeIso(d: Date): string {
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}
</script>

<template>
  <view class="page">
    <!-- 关闭导航 -->
    <view class="close-nav">
      <view class="close-btn" @click="goClose">
        <text class="close-icon">✕</text>
      </view>
    </view>

    <view class="content">
      <!-- 问候区 -->
      <view class="greeting">
        <text class="greeting-emoji">🌙</text>
        <text class="greeting-title">今天过得怎么样？</text>
        <text class="greeting-date">{{ todayText }}</text>
      </view>

      <!-- 加载 / 错误态 -->
      <view v-if="loading" class="state-card">
        <text class="state-text">正在为你整理今天...</text>
      </view>
      <view v-else-if="errorMsg" class="state-card">
        <text class="state-text">{{ errorMsg }}</text>
        <button class="btn-retry" @click="loadSummary">重试</button>
      </view>

      <template v-else>
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
        <view v-if="total" class="list-card">
          <view
            v-for="a in completedActivities"
            :key="a.id"
            class="list-item"
          >
            <view class="list-check list-check-done">
              <text class="list-check-icon">✓</text>
            </view>
            <text class="list-text list-text-done">{{ a.title }}</text>
            <text class="list-status list-status-done">已完成</text>
          </view>
          <view
            v-for="a in pendingActivities"
            :key="a.id"
            class="list-item"
          >
            <view class="list-check list-check-pending">
              <view class="list-check-dot"></view>
            </view>
            <view class="list-text-wrap">
              <text class="list-text list-text-pending">{{ a.title }}</text>
              <text class="list-text-sub">没关系，明天继续</text>
            </view>
            <text class="list-action" @click="markComplete(a)">补完成</text>
          </view>
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

        <!-- AI 寄语卡 -->
        <view class="ai-card">
          <view class="ai-card-decor"></view>
          <view class="ai-card-content">
            <view class="ai-card-header">
              <view class="ai-avatar">
                <text class="ai-avatar-icon">✨</text>
              </view>
            </view>
            <text class="ai-message">{{ aiMessage }}</text>
            <text class="ai-emoji">💛</text>
          </view>
        </view>

        <!-- 底部 CTA -->
        <view class="bottom-cta">
          <button
            class="btn-goodnight"
            :disabled="submitting"
            @click="goodnight"
          >
            <text class="btn-goodnight-icon">🌙</text>
            <text class="btn-goodnight-text">晚安，明天见</text>
          </button>
        </view>
      </template>
    </view>
  </view>
</template>

<style scoped lang="scss">
.page {
  min-height: 100vh;
  background: linear-gradient(180deg, $solo-primary-50 0%, $solo-primary-100 30%, $solo-neutral-50 70%);
}

/* 关闭导航 */
.close-nav {
  display: flex;
  justify-content: flex-end;
  padding: 24rpx 32rpx 0;
}

.close-btn {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: rgba(255, 255, 255, 0.6);
}

.close-icon {
  font-size: 32rpx;
  color: $solo-neutral-700;
}

/* 内容区 */
.content {
  padding: 24rpx 32rpx 64rpx;
}

/* 问候区 */
.greeting {
  text-align: center;
  padding: 24rpx 0 40rpx;
}

.greeting-emoji {
  display: block;
  font-size: 80rpx;
  margin-bottom: 16rpx;
}

.greeting-title {
  display: block;
  font-size: 48rpx;
  font-weight: 700;
  color: $solo-neutral-900;
}

.greeting-date {
  display: block;
  font-size: 26rpx;
  color: $solo-neutral-500;
  margin-top: 12rpx;
}

/* 状态卡 */
.state-card {
  background-color: $solo-card;
  border-radius: $solo-radius-lg;
  padding: 64rpx 32rpx;
  text-align: center;
}

.state-text {
  display: block;
  font-size: 28rpx;
  color: $solo-neutral-500;
  margin-bottom: 24rpx;
}

.btn-retry {
  display: inline-block;
  height: 64rpx;
  line-height: 64rpx;
  padding: 0 40rpx;
  background-color: $solo-primary-500;
  color: #fff;
  font-size: 26rpx;
  border-radius: $solo-radius-full;
  border: none;
}

/* 完成度统计卡 */
.stats-card {
  background-color: $solo-card;
  border-radius: $solo-radius-lg;
  border: 1rpx solid $solo-border;
  padding: 40rpx;
  margin-bottom: 32rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
}

.stats-row {
  display: flex;
  align-items: center;
  gap: 32rpx;
}

.progress-ring {
  position: relative;
  width: 160rpx;
  height: 160rpx;
  flex-shrink: 0;
}

.ring-svg {
  width: 160rpx;
  height: 160rpx;
}

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
  font-size: 32rpx;
  font-weight: 700;
  color: $solo-neutral-900;
}

.stats-info {
  flex: 1;
  min-width: 0;
}

.stats-title {
  display: block;
  font-size: 30rpx;
  font-weight: 600;
  color: $solo-neutral-900;
}

.stats-sub {
  display: block;
  font-size: 26rpx;
  color: $solo-neutral-500;
  margin-top: 8rpx;
}

.stats-bars {
  display: flex;
  gap: 8rpx;
  margin-top: 16rpx;
}

.stats-bar {
  height: 6rpx;
  flex: 1;
  border-radius: 3rpx;
  background-color: $solo-muted;

  &.filled {
    background-color: $solo-primary-500;
  }
}

/* 完成项列表 */
.list-card {
  background-color: $solo-card;
  border-radius: $solo-radius-lg;
  border: 1rpx solid $solo-border;
  overflow: hidden;
  margin-bottom: 32rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
}

.list-item {
  display: flex;
  align-items: center;
  gap: 24rpx;
  padding: 32rpx;
  border-bottom: 1rpx solid $solo-border;

  &:last-child {
    border-bottom: none;
  }
}

.list-check {
  width: 48rpx;
  height: 48rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.list-check-done {
  background-color: rgba(34, 197, 94, 0.15);
}

.list-check-icon {
  font-size: 24rpx;
  color: $solo-state-success;
}

.list-check-pending {
  background-color: $solo-muted;
}

.list-check-dot {
  width: 16rpx;
  height: 16rpx;
  border-radius: 50%;
  background-color: rgba(120, 113, 108, 0.4);
}

.list-text-wrap {
  flex: 1;
  min-width: 0;
}

.list-text {
  font-size: 28rpx;
}

.list-text-done {
  color: $solo-neutral-800;
  text-decoration: line-through;
  text-decoration-color: rgba(120, 113, 108, 0.4);
}

.list-text-pending {
  color: $solo-neutral-500;
}

.list-text-sub {
  display: block;
  font-size: 22rpx;
  color: $solo-neutral-400;
  margin-top: 4rpx;
}

.list-status-done {
  font-size: 22rpx;
  color: $solo-state-success;
}

.list-action {
  font-size: 24rpx;
  color: $solo-primary-500;
  padding: 8rpx 16rpx;
}

/* 心情选择 */
.mood-section {
  margin-bottom: 32rpx;
}

.mood-title {
  display: block;
  font-size: 28rpx;
  font-weight: 600;
  color: $solo-neutral-900;
  padding: 0 8rpx;
  margin-bottom: 20rpx;
}

.mood-list {
  display: flex;
  gap: 16rpx;
}

.mood-btn {
  flex: 1;
  background-color: $solo-card;
  border: 1rpx solid $solo-border;
  border-radius: $solo-radius-md;
  padding: 24rpx 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
  transition: all 0.2s;

  &.selected {
    border-color: $solo-primary-400;
    background-color: $solo-primary-50;
    transform: scale(1.05);
  }
}

.mood-emoji {
  font-size: 48rpx;
}

.mood-label {
  font-size: 22rpx;
  color: $solo-neutral-500;
}

/* AI 寄语卡 */
.ai-card {
  position: relative;
  overflow: hidden;
  border-radius: $solo-radius-lg;
  padding: 40rpx;
  margin-bottom: 40rpx;
  background: linear-gradient(135deg, $solo-primary-50 0%, $solo-primary-100 50%, $solo-primary-200 100%);
  border: 1rpx solid $solo-primary-100;
}

.ai-card-decor {
  position: absolute;
  top: -40rpx;
  right: -40rpx;
  width: 160rpx;
  height: 160rpx;
  border-radius: 50%;
  background-color: $solo-primary-200;
  opacity: 0.3;
  pointer-events: none;
}

.ai-card-content {
  position: relative;
  z-index: 1;
}

.ai-card-header {
  margin-bottom: 16rpx;
}

.ai-avatar {
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  background-color: rgba(249, 115, 22, 0.15);
  display: flex;
  align-items: center;
  justify-content: center;
}

.ai-avatar-icon {
  font-size: 32rpx;
}

.ai-message {
  display: block;
  font-size: 28rpx;
  color: $solo-primary-900;
  line-height: 1.7;
}

.ai-emoji {
  display: block;
  font-size: 36rpx;
  margin-top: 16rpx;
}

/* 底部 CTA */
.bottom-cta {
  margin-top: 16rpx;
}

.btn-goodnight {
  width: 100%;
  height: 96rpx;
  background-color: $solo-primary-500;
  color: #fff;
  font-size: 32rpx;
  font-weight: 600;
  border-radius: $solo-radius-md;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  box-shadow: 0 8rpx 28rpx rgba(249, 115, 22, 0.3);
}

.btn-goodnight[disabled] {
  background-color: $solo-primary-300;
  box-shadow: none;
}

.btn-goodnight-icon {
  font-size: 32rpx;
}

.btn-goodnight-text {
  font-size: 32rpx;
  font-weight: 600;
}
</style>
