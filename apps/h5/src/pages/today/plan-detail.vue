<script setup lang="ts">
/**
 * Page 02: 今日规划详情。
 *
 * 设计稿：designs/solo-life-os-mobile/pages/today-plan-detail.html
 * 文档：designs/solo-life-os-mobile/docs/01-today-module-review.md §Page 02
 *
 * 信息架构（按设计稿）：
 * - 顶部导航：返回 + "今日规划" + 重新规划
 * - 今日概览卡：日期 + 状态 + 活动数
 * - 时间轴：左侧时间+圆点+竖线，右侧活动卡片（标题/类型标签/时间/操作）
 * - 底部操作栏："AI帮我调整" + "开始今天"
 *
 * 数据来源：getDailyPlan / listActivities / endActivity（标记完成）
 */
import { computed, onMounted, ref } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { ApiError } from '@/api/request'
import {
  completeDailyPlan,
  endActivity,
  getDailyPlan,
  listActivities,
  startDailyPlan,
} from '@/api/today'
import type { Activity, ActivityType, DailyPlan } from '@/api/types'

const planId = ref<number | null>(null)
const loading = ref(true)
const errorMsg = ref('')
const plan = ref<DailyPlan | null>(null)
const activities = ref<Activity[]>([])

const planDateText = computed(() => {
  if (!plan.value) return ''
  const d = plan.value.date
  return `${d.slice(5)} · ${weekdayText(d)}`
})

const planStatusText = computed(() => {
  switch (plan.value?.status) {
    case 'PLANNING':
      return '规划中'
    case 'ONGOING':
      return '进行中'
    case 'COMPLETED':
      return '已完成'
    case 'CANCELLED':
      return '已取消'
    default:
      return ''
  }
})

const canStart = computed(() => plan.value?.status === 'PLANNING')
const canComplete = computed(() => plan.value?.status === 'ONGOING')

onLoad((query) => {
  const raw = query?.planId
  if (raw) planId.value = Number(raw)
})

onMounted(() => {
  if (planId.value) loadDetail()
})

onShow(() => {
  if (planId.value && plan.value) loadDetail()
})

async function loadDetail(): Promise<void> {
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
    errorMsg.value = e instanceof ApiError ? e.message : '加载规划失败'
  } finally {
    loading.value = false
  }
}

/** 标记活动完成（设置 endTime = 当前时间）。 */
async function markComplete(activity: Activity): Promise<void> {
  const now = new Date()
  const isoLocal = toLocalDateTimeIso(now)
  try {
    const updated = await endActivity(activity.id, { endTime: isoLocal })
    const idx = activities.value.findIndex((a) => a.id === activity.id)
    if (idx >= 0) activities.value[idx] = updated
    uni.showToast({ title: '完成一件事 🎉', icon: 'success' })
  } catch (e) {
    uni.showToast({
      title: e instanceof ApiError ? e.message : '操作失败',
      icon: 'none',
    })
  }
}

async function startPlan(): Promise<void> {
  if (!plan.value) return
  try {
    plan.value = await startDailyPlan(plan.value.id)
    uni.showToast({ title: '今天开始啦', icon: 'success' })
  } catch (e) {
    uni.showToast({
      title: e instanceof ApiError ? e.message : '操作失败',
      icon: 'none',
    })
  }
}

async function completePlan(): Promise<void> {
  if (!plan.value) return
  try {
    plan.value = await completeDailyPlan(plan.value.id)
    uni.showToast({ title: '今天辛苦了', icon: 'success' })
  } catch (e) {
    uni.showToast({
      title: e instanceof ApiError ? e.message : '操作失败',
      icon: 'none',
    })
  }
}

function goReplan(): void {
  if (!plan.value) return
  uni.navigateTo({ url: `/pages/today/replan?planId=${plan.value.id}` })
}

function goBack(): void {
  uni.navigateBack({
    fail: () => uni.reLaunch({ url: '/pages/today/index' }),
  })
}

/** 活动类型 → 中文标签 + emoji。 */
function activityTypeMeta(type: ActivityType): { label: string; emoji: string } {
  switch (type) {
    case 'WORK':
      return { label: '工作', emoji: '💼' }
    case 'LEISURE':
      return { label: '休闲', emoji: '☕' }
    case 'SPORT':
      return { label: '运动', emoji: '🏃' }
    case 'STUDY':
      return { label: '学习', emoji: '📖' }
    case 'SOCIAL':
      return { label: '社交', emoji: '👥' }
    case 'EXPLORE':
      return { label: '探索', emoji: '🧭' }
    case 'REST':
      return { label: '休息', emoji: '🌙' }
    default:
      return { label: '其他', emoji: '✨' }
  }
}

function formatTime(startTime: string): string {
  return startTime.slice(11, 16)
}

function weekdayText(iso: string): string {
  const d = new Date(`${iso}T00:00:00`)
  const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  return weekdays[d.getDay()]
}

function toLocalDateTimeIso(d: Date): string {
  // 后端 LocalDateTime 不带时区，前端按本地时间拼 ISO 字符串（YYYY-MM-DDTHH:MM:SS）
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}
</script>

<template>
  <view class="page">
    <!-- 顶部导航 -->
    <view class="top-nav">
      <view class="nav-btn" @click="goBack">
        <text class="nav-btn-icon">‹</text>
      </view>
      <text class="nav-title">今日规划</text>
      <view class="nav-text-btn" @click="goReplan">
        <text>重新规划</text>
      </view>
    </view>

    <!-- 内容区 -->
    <view class="content">
      <view v-if="loading && !plan" class="loading">
        <text class="loading-text">加载中...</text>
      </view>
      <view v-else-if="errorMsg" class="error">
        <text class="error-text">{{ errorMsg }}</text>
        <button class="btn-retry" @click="loadDetail">重试</button>
      </view>
      <template v-else-if="plan">
        <!-- 概览卡 -->
        <view class="overview-card">
          <text class="overview-date">{{ planDateText }}</text>
          <view class="overview-row">
            <text class="overview-emoji">🌤️</text>
            <text class="overview-text">{{ planStatusText }}</text>
            <text class="overview-dot">·</text>
            <text class="overview-text">{{ activities.length }} 项活动</text>
          </view>
        </view>

        <!-- 时间轴 -->
        <view v-if="activities.length" class="timeline">
          <view
            v-for="(a, idx) in activities"
            :key="a.id"
            class="timeline-item"
          >
            <view class="timeline-left">
              <text class="timeline-time">{{ formatTime(a.startTime) }}</text>
              <view class="timeline-dot"></view>
              <view v-if="idx < activities.length - 1" class="timeline-line"></view>
            </view>

            <view class="timeline-card">
              <view class="card-header">
                <view class="card-header-left">
                  <view class="card-tag">
                    <text class="card-tag-emoji">{{ activityTypeMeta(a.type).emoji }}</text>
                    <text class="card-tag-text">{{ activityTypeMeta(a.type).label }}</text>
                  </view>
                  <text class="card-title">{{ a.title }}</text>
                </view>
              </view>

              <view class="card-meta">
                <view class="card-meta-item">
                  <text class="meta-icon">⏰</text>
                  <text class="meta-text">{{ formatTime(a.startTime) }} 开始</text>
                </view>
                <view v-if="a.endTime" class="card-meta-item">
                  <text class="meta-icon">✅</text>
                  <text class="meta-text">{{ formatTime(a.endTime) }} 完成</text>
                </view>
              </view>

              <view class="card-actions">
                <button
                  v-if="!a.endTime"
                  class="btn-complete"
                  @click="markComplete(a)"
                >
                  标记完成
                </button>
                <text v-else class="completed-badge">已完成</text>
              </view>
            </view>
          </view>
        </view>

        <!-- 空活动态 -->
        <view v-else class="empty-activities">
          <text class="empty-emoji">📝</text>
          <text class="empty-text">还没有活动，开始今天后可逐步添加</text>
        </view>
      </template>
    </view>

    <!-- 底部操作栏 -->
    <view v-if="plan" class="bottom-bar">
      <view class="bottom-bar-inner">
        <view class="btn-ai-adjust" @click="goReplan">
          <text class="btn-ai-icon">✨</text>
          <text class="btn-ai-text">AI帮我调整</text>
        </view>
        <button
          v-if="canStart"
          class="btn-start"
          :disabled="loading"
          @click="startPlan"
        >
          开始今天
        </button>
        <button
          v-else-if="canComplete"
          class="btn-start"
          :disabled="loading"
          @click="completePlan"
        >
          完成今天
        </button>
        <button v-else class="btn-start" disabled>已结束</button>
      </view>
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
  font-size: 34rpx;
  font-weight: 600;
  color: $solo-neutral-900;
}

.nav-text-btn {
  padding: 12rpx 8rpx;
}

.nav-text-btn text {
  font-size: 28rpx;
  color: $solo-primary-500;
}

/* 内容区 */
.content {
  padding: 16rpx 32rpx 200rpx;
}

.loading,
.error,
.empty-activities {
  background-color: $solo-card;
  border-radius: $solo-radius-md;
  padding: 64rpx 32rpx;
  text-align: center;
  margin-top: 24rpx;
}

.loading-text,
.error-text,
.empty-text {
  display: block;
  font-size: 28rpx;
  color: $solo-neutral-500;
  margin-bottom: 16rpx;
}

.empty-emoji {
  font-size: 80rpx;
  margin-bottom: 16rpx;
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

/* 概览卡 */
.overview-card {
  background-color: $solo-primary-50;
  border-radius: $solo-radius-md;
  padding: 32rpx;
  margin: 24rpx 0 40rpx;
}

.overview-date {
  display: block;
  font-size: 36rpx;
  font-weight: 600;
  color: $solo-neutral-900;
  margin-bottom: 12rpx;
}

.overview-row {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.overview-emoji {
  font-size: 24rpx;
  margin-right: 4rpx;
}

.overview-text {
  font-size: 26rpx;
  color: $solo-neutral-600;
}

.overview-dot {
  color: $solo-neutral-300;
  margin: 0 4rpx;
}

/* 时间轴 */
.timeline {
  display: flex;
  flex-direction: column;
}

.timeline-item {
  display: flex;
  gap: 24rpx;
}

.timeline-item + .timeline-item {
  margin-top: 32rpx;
}

.timeline-left {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 112rpx;
  flex-shrink: 0;
  padding-top: 8rpx;
}

.timeline-time {
  font-size: 24rpx;
  color: $solo-neutral-500;
  white-space: nowrap;
  margin-bottom: 16rpx;
}

.timeline-dot {
  width: 24rpx;
  height: 24rpx;
  border-radius: 50%;
  background-color: $solo-primary-500;
  border: 4rpx solid #fff;
  box-shadow: 0 0 0 2rpx $solo-primary-100;
  flex-shrink: 0;
  z-index: 1;
}

.timeline-line {
  width: 4rpx;
  flex: 1;
  background-color: $solo-primary-100;
  margin-top: 8rpx;
  min-height: 40rpx;
}

.timeline-card {
  flex: 1;
  min-width: 0;
  background-color: #fff;
  border-radius: $solo-radius-md;
  border: 1rpx solid $solo-border;
  padding: 32rpx;
}

.card-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16rpx;
  margin-bottom: 16rpx;
}

.card-header-left {
  flex: 1;
  min-width: 0;
}

.card-tag {
  display: inline-flex;
  align-items: center;
  height: 44rpx;
  padding: 0 16rpx;
  border-radius: $solo-radius-sm;
  background-color: $solo-primary-50;
  margin-bottom: 12rpx;
}

.card-tag-emoji {
  font-size: 22rpx;
  margin-right: 6rpx;
}

.card-tag-text {
  font-size: 22rpx;
  color: $solo-primary-600;
}

.card-title {
  display: block;
  font-size: 32rpx;
  font-weight: 600;
  color: $solo-neutral-800;
  line-height: 1.3;
}

.card-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx 24rpx;
  margin-bottom: 16rpx;
}

.card-meta-item {
  display: inline-flex;
  align-items: center;
  gap: 8rpx;
}

.meta-icon {
  font-size: 24rpx;
}

.meta-text {
  font-size: 26rpx;
  color: $solo-neutral-600;
}

.card-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 8rpx;
}

.btn-complete {
  height: 64rpx;
  line-height: 64rpx;
  padding: 0 28rpx;
  background-color: $solo-primary-50;
  color: $solo-primary-600;
  font-size: 26rpx;
  border-radius: $solo-radius-full;
  border: none;
}

.completed-badge {
  font-size: 24rpx;
  color: $solo-state-success;
}

/* 底部操作栏 */
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

.bottom-bar-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24rpx;
}

.btn-ai-adjust {
  display: inline-flex;
  align-items: center;
  gap: 8rpx;
  padding: 16rpx 8rpx;
}

.btn-ai-icon {
  font-size: 28rpx;
}

.btn-ai-text {
  font-size: 28rpx;
  color: $solo-neutral-500;
}

.btn-start {
  background-color: $solo-primary-500;
  color: #fff;
  border-radius: $solo-radius-full;
  padding: 20rpx 48rpx;
  font-size: 28rpx;
  font-weight: 500;
  border: none;
  white-space: nowrap;
  flex-shrink: 0;
}

.btn-start[disabled] {
  background-color: $solo-neutral-300;
  color: $solo-neutral-500;
}
</style>
