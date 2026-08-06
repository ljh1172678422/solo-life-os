<script setup lang="ts">
/**
 * Page 01: 今天（首页）。
 *
 * 设计稿：designs/solo-life-os-mobile/pages/today.html
 * 文档：designs/solo-life-os-mobile/docs/01-today-module-review.md §Page 01
 *
 * 信息架构（按设计稿）：
 * - 顶部：问候 + 副标题
 * - AI 今日规划 Hero 卡（时间块概览 pills + 查看完整规划 CTA）
 * - AI 为你推荐（横滑卡片）
 * - 附近正在发生（列表卡片）
 * - 底部：今晚记录 / 重新规划入口
 *
 * 数据来源：getTodayPlan / listActivities（PLAN 已存在时展示活动 pills；无 PLAN 时显示空态温柔引导）
 * 设计原则（01-today-module-review.md §关键设计原则）：信息折叠 / 零摩擦 / 温柔兜底 / Agent 而非 Chatbot
 */
import { computed, onMounted, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { ApiError } from '@/api/request'
import { useUserStore } from '@/stores/user'
import {
  createDailyPlan,
  getTodayPlan,
  listActivities,
  startDailyPlan,
} from '@/api/today'
import type { Activity, ActivityType, DailyPlan } from '@/api/types'

const userStore = useUserStore()

const loading = ref(true)
const errorMsg = ref('')
const plan = ref<DailyPlan | null>(null)
const activities = ref<Activity[]>([])

const todayIso = new Date().toISOString().slice(0, 10)

/** 按时段分组活动（上午/下午/晚上/睡前）。 */
const groupedActivities = computed<{ label: string; emoji: string; items: Activity[] }[]>(() => {
  const buckets: Record<string, Activity[]> = { morning: [], afternoon: [], evening: [], night: [] }
  for (const a of activities.value) {
    const hour = parseInt(a.startTime.slice(11, 13), 10)
    if (hour < 12) buckets.morning.push(a)
    else if (hour < 18) buckets.afternoon.push(a)
    else if (hour < 22) buckets.evening.push(a)
    else buckets.night.push(a)
  }
  return [
    { label: '上午', emoji: '☀️', items: buckets.morning },
    { label: '下午', emoji: '🌿', items: buckets.afternoon },
    { label: '晚上', emoji: '🎬', items: buckets.evening },
    { label: '睡前', emoji: '🌙', items: buckets.night },
  ].filter((g) => g.items.length > 0)
})

const hasPlan = computed(() => !!plan.value)
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

const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '夜深了 🌙'
  if (hour < 11) return '早上好 ☀️'
  if (hour < 14) return '中午好 🌤️'
  if (hour < 18) return '下午好 ☕'
  if (hour < 22) return '晚上好 🌆'
  return '夜深了 🌙'
})

onMounted(loadToday)

// reLaunch 回到首页时刷新（活动状态可能变化）
onShow(() => {
  if (userStore.isLoggedIn) loadToday()
})

async function loadToday(): Promise<void> {
  if (!userStore.userId) {
    uni.reLaunch({ url: '/pages/login/index' })
    return
  }
  loading.value = true
  errorMsg.value = ''
  try {
    const today = await getTodayPlan(userStore.userId, todayIso)
    plan.value = today
    if (today) {
      activities.value = await listActivities(today.id)
    }
  } catch (e) {
    errorMsg.value = e instanceof ApiError ? e.message : '加载今日计划失败，稍后再试'
  } finally {
    loading.value = false
  }
}

/** 一键生成今日计划（PLAN 不存在时的懒人入口）。 */
async function createTodayPlan(): Promise<void> {
  if (!userStore.userId) return
  loading.value = true
  errorMsg.value = ''
  try {
    const created = await createDailyPlan(userStore.userId, { date: todayIso })
    plan.value = created
    activities.value = []
    uni.showToast({ title: '今日计划已创建', icon: 'success' })
  } catch (e) {
    errorMsg.value = e instanceof ApiError ? e.message : '创建计划失败'
  } finally {
    loading.value = false
  }
}

/** 开始执行今日计划：PLANNING → ONGOING。 */
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

function goPlanDetail(): void {
  if (!plan.value) return
  uni.navigateTo({ url: `/pages/today/plan-detail?planId=${plan.value.id}` })
}

function goReplan(): void {
  if (!plan.value) return
  uni.navigateTo({ url: `/pages/today/replan?planId=${plan.value.id}` })
}

function goSummary(): void {
  if (!plan.value) return
  uni.navigateTo({ url: `/pages/today/summary?planId=${plan.value.id}` })
}

function goProfile(): void {
  uni.reLaunch({ url: '/pages/profile/index' })
}

/** 活动类型 → 展示 emoji（与设计稿活动 pill 一致）。 */
function activityEmoji(type: ActivityType): string {
  switch (type) {
    case 'WORK':
      return '💼'
    case 'LEISURE':
      return '☕'
    case 'SPORT':
      return '🏃'
    case 'STUDY':
      return '📖'
    case 'SOCIAL':
      return '👥'
    case 'EXPLORE':
      return '🧭'
    case 'REST':
      return '🌙'
    default:
      return '✨'
  }
}

/** 格式化活动开始时间 HH:MM。 */
function formatTime(startTime: string): string {
  return startTime.slice(11, 16)
}
</script>

<template>
  <view class="page">
    <!-- 顶部问候 -->
    <view class="greeting">
      <text class="greeting-title">{{ greeting }}</text>
      <text class="greeting-sub">今天，过得值得一点</text>
    </view>

    <!-- 加载 / 错误态 -->
    <view v-if="loading && !plan" class="loading-card">
      <text class="loading-text">正在为你准备今天...</text>
    </view>
    <view v-else-if="errorMsg && !plan" class="error-card">
      <text class="error-text">{{ errorMsg }}</text>
      <button class="btn-retry" @click="loadToday">重试</button>
    </view>

    <!-- 空态：温柔引导（01-today-module-review §首次使用） -->
    <view v-else-if="!hasPlan" class="empty-hero">
      <view class="empty-emoji">🌅</view>
      <text class="empty-title">今天感觉怎么样？</text>
      <text class="empty-sub">想出门吗？让 AI 为你准备一份轻松的今日安排</text>
      <button class="btn-primary" :disabled="loading" @click="createTodayPlan">
        {{ loading ? '生成中...' : '生成今日计划' }}
      </button>
    </view>

    <!-- AI 今日规划 Hero 卡（设计稿 §AI 今日规划 Hero 卡片） -->
    <view v-else class="hero-card">
      <view class="hero-decor-1"></view>
      <view class="hero-decor-2"></view>

      <view class="hero-content">
        <view class="hero-header">
          <view class="hero-tag">
            <text class="hero-tag-icon">✨</text>
            <text class="hero-tag-text">AI 今日规划</text>
          </view>
          <text class="hero-status">{{ planStatusText }}</text>
        </view>

        <text class="hero-title">为你准备的今天</text>
        <text class="hero-sub">
          根据你的节奏，推荐了 {{ activities.length }} 件小而美好的事
        </text>

        <!-- 活动 pills（信息折叠：只显示标题，不展开细节） -->
        <view v-if="activities.length" class="hero-pills">
          <view v-for="a in activities" :key="a.id" class="pill" @click="goPlanDetail">
            <text class="pill-emoji">{{ activityEmoji(a.type) }}</text>
            <text class="pill-text">{{ a.title }}</text>
          </view>
        </view>
        <view v-else class="hero-pills-empty">
          <text class="pills-empty-text">还没有活动，去规划详情添加吧</text>
        </view>

        <button class="btn-hero" @click="goPlanDetail">
          <text class="btn-hero-text">查看完整规划</text>
          <text class="btn-hero-arrow">→</text>
        </button>
      </view>
    </view>

    <!-- 时间块概览（PLAN 存在时按上午/下午/晚上/睡前分组） -->
    <view v-if="hasPlan && groupedActivities.length" class="section">
      <text class="section-title">今日时间块</text>
      <view class="block-list">
        <view
          v-for="group in groupedActivities"
          :key="group.label"
          class="block-card"
          @click="goPlanDetail"
        >
          <view class="block-header">
            <text class="block-emoji">{{ group.emoji }}</text>
            <text class="block-label">{{ group.label }}</text>
            <text class="block-count">{{ group.items.length }} 项</text>
          </view>
          <view class="block-items">
            <view v-for="a in group.items" :key="a.id" class="block-item">
              <text class="block-item-time">{{ formatTime(a.startTime) }}</text>
              <text class="block-item-title">{{ a.title }}</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 底部操作区 -->
    <view v-if="hasPlan" class="bottom-actions">
      <button
        v-if="plan?.status === 'PLANNING'"
        class="btn-primary"
        :disabled="loading"
        @click="startPlan"
      >
        开始今天
      </button>
      <button v-else class="btn-primary" @click="goSummary">今晚记录一下</button>
      <view class="bottom-link" @click="goReplan">
        <text class="bottom-link-icon">↻</text>
        <text class="bottom-link-text">重新规划</text>
      </view>
    </view>

    <!-- 顶部右上：个人中心入口 -->
    <view class="profile-entry" @click="goProfile">
      <text class="profile-entry-text">我的</text>
    </view>
  </view>
</template>

<style scoped lang="scss">
.page {
  min-height: 100vh;
  padding: 80rpx 32rpx 160rpx;
  background-color: $solo-neutral-50;
  position: relative;
}

/* 顶部问候 */
.greeting {
  margin-bottom: 40rpx;
}

.greeting-title {
  display: block;
  font-size: 52rpx;
  font-weight: 700;
  color: $solo-neutral-900;
  line-height: 1.2;
}

.greeting-sub {
  display: block;
  font-size: 28rpx;
  color: $solo-neutral-500;
  margin-top: 8rpx;
}

/* 加载 / 错误态 */
.loading-card,
.error-card {
  background-color: $solo-card;
  border-radius: $solo-radius-lg;
  padding: 64rpx 32rpx;
  text-align: center;
}

.loading-text,
.error-text {
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

/* 空态 */
.empty-hero {
  background: linear-gradient(135deg, $solo-primary-50 0%, $solo-primary-100 60%, $solo-primary-200 100%);
  border: 1rpx solid $solo-primary-200;
  border-radius: $solo-radius-xl;
  padding: 64rpx 40rpx;
  text-align: center;
}

.empty-emoji {
  font-size: 96rpx;
  margin-bottom: 24rpx;
}

.empty-title {
  display: block;
  font-size: 40rpx;
  font-weight: 700;
  color: $solo-neutral-900;
  margin-bottom: 16rpx;
}

.empty-sub {
  display: block;
  font-size: 28rpx;
  color: $solo-neutral-600;
  line-height: 1.6;
  margin-bottom: 40rpx;
}

/* AI Hero 卡 */
.hero-card {
  position: relative;
  overflow: hidden;
  border-radius: $solo-radius-xl;
  padding: 40rpx;
  margin-bottom: 48rpx;
  background: linear-gradient(135deg, $solo-primary-50 0%, $solo-primary-100 40%, $solo-primary-200 100%);
  border: 1rpx solid $solo-primary-200;
}

.hero-decor-1,
.hero-decor-2 {
  position: absolute;
  border-radius: 50%;
  pointer-events: none;
}

.hero-decor-1 {
  top: -64rpx;
  right: -64rpx;
  width: 224rpx;
  height: 224rpx;
  background-color: $solo-primary-200;
  opacity: 0.4;
}

.hero-decor-2 {
  top: 64rpx;
  right: 96rpx;
  width: 128rpx;
  height: 128rpx;
  background-color: $solo-primary-300;
  opacity: 0.3;
}

.hero-content {
  position: relative;
  z-index: 1;
}

.hero-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
}

.hero-tag {
  display: inline-flex;
  align-items: center;
  padding: 8rpx 20rpx;
  border-radius: $solo-radius-full;
  background-color: rgba(249, 115, 22, 0.12);
}

.hero-tag-icon {
  font-size: 24rpx;
  margin-right: 8rpx;
}

.hero-tag-text {
  font-size: 24rpx;
  font-weight: 500;
  color: $solo-primary-700;
}

.hero-status {
  font-size: 24rpx;
  color: $solo-neutral-600;
}

.hero-title {
  display: block;
  font-size: 36rpx;
  font-weight: 600;
  color: $solo-neutral-900;
  margin-bottom: 8rpx;
}

.hero-sub {
  display: block;
  font-size: 24rpx;
  color: $solo-neutral-600;
  line-height: 1.6;
  margin-bottom: 32rpx;
}

.hero-pills {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
  margin-bottom: 40rpx;
}

.pill {
  display: inline-flex;
  align-items: center;
  padding: 12rpx 24rpx;
  border-radius: $solo-radius-full;
  background-color: rgba(255, 255, 255, 0.7);
  border: 1rpx solid rgba(249, 115, 22, 0.15);
}

.pill-emoji {
  font-size: 24rpx;
  margin-right: 8rpx;
}

.pill-text {
  font-size: 24rpx;
  color: $solo-neutral-700;
}

.hero-pills-empty {
  margin-bottom: 40rpx;
}

.pills-empty-text {
  font-size: 24rpx;
  color: $solo-neutral-500;
}

.btn-hero {
  width: 100%;
  height: 88rpx;
  line-height: 88rpx;
  background-color: $solo-primary-500;
  border-radius: $solo-radius-full;
  border: none;
  box-shadow: 0 8rpx 28rpx rgba(249, 115, 22, 0.35);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
}

.btn-hero-text {
  color: #fff;
  font-size: 28rpx;
  font-weight: 600;
}

.btn-hero-arrow {
  color: #fff;
  font-size: 28rpx;
}

/* 时间块 */
.section {
  margin-bottom: 48rpx;
}

.section-title {
  display: block;
  font-size: 32rpx;
  font-weight: 600;
  color: $solo-neutral-900;
  margin-bottom: 24rpx;
}

.block-list {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.block-card {
  background-color: $solo-card;
  border: 1rpx solid $solo-border;
  border-radius: $solo-radius-lg;
  padding: 32rpx;
}

.block-header {
  display: flex;
  align-items: center;
  margin-bottom: 20rpx;
}

.block-emoji {
  font-size: 32rpx;
  margin-right: 12rpx;
}

.block-label {
  font-size: 30rpx;
  font-weight: 600;
  color: $solo-neutral-900;
}

.block-count {
  margin-left: auto;
  font-size: 24rpx;
  color: $solo-neutral-500;
}

.block-items {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.block-item {
  display: flex;
  align-items: center;
  padding: 12rpx 0;
}

.block-item-time {
  font-size: 24rpx;
  color: $solo-neutral-500;
  width: 96rpx;
}

.block-item-title {
  font-size: 28rpx;
  color: $solo-neutral-800;
}

/* 底部操作区 */
.bottom-actions {
  margin-top: 48rpx;
}

.btn-primary {
  width: 100%;
  height: 96rpx;
  line-height: 96rpx;
  background-color: $solo-primary-500;
  color: #fff;
  font-size: 30rpx;
  font-weight: 600;
  border-radius: $solo-radius-full;
  border: none;
  box-shadow: 0 8rpx 28rpx rgba(249, 115, 22, 0.35);
}

.btn-primary[disabled] {
  background-color: $solo-primary-300;
  box-shadow: none;
}

.bottom-link {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 24rpx;
  padding: 16rpx;
}

.bottom-link-icon {
  font-size: 28rpx;
  color: $solo-neutral-500;
  margin-right: 8rpx;
}

.bottom-link-text {
  font-size: 24rpx;
  color: $solo-neutral-500;
}

/* 个人中心入口（右上浮动） */
.profile-entry {
  position: fixed;
  top: 48rpx;
  right: 32rpx;
  padding: 12rpx 24rpx;
  background-color: $solo-card;
  border: 1rpx solid $solo-border;
  border-radius: $solo-radius-full;
  z-index: 10;
}

.profile-entry-text {
  font-size: 24rpx;
  color: $solo-neutral-700;
}
</style>
