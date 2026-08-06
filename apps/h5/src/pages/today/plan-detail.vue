<script setup lang="ts">
/**
 * Page 02: 今日规划详情。
 *
 * 设计稿：designs/solo-life-os-mobile/pages/today-plan-detail.html
 * 文档：designs/solo-life-os-mobile/docs/01-today-module-review.md §Page 02
 *
 * 当前实现：按设计稿 1:1 还原布局，纯静态 mock 展示。
 *
 * 信息架构：
 * - 顶部 sticky 导航（毛玻璃）：返回 + "今日规划" + 重新规划
 * - Overview 卡片：日期 + 天气·预算·星级 + 外出·步数
 * - 时间轴（4 个活动）：左侧时间+圆点+连线，右侧卡片（tag-chip · tags · meta · reason引用）
 * - 底部固定操作栏：AI帮我调整 + 开始今天
 */
import {
  ChevronLeft,
  Sun,
  MapPin,
  Footprints,
  Wallet,
  Clock,
  ChevronRight,
  Sparkles,
  BookOpen,
} from 'lucide-vue-next'

/* ---------- 静态 Mock 数据（与设计稿 today-plan-detail.html 一致） ---------- */

interface TimelineMeta {
  icon: 'wallet' | 'footprints' | 'clock' | 'mapPin' | 'bookOpen'
  text: string
}

interface TimelineActivity {
  time: string
  chipLabel: string
  chipEmoji: string
  title: string
  tags: string[]
  meta: TimelineMeta[]
  reason: string
}

const timeline: TimelineActivity[] = [
  {
    time: '9:30',
    chipLabel: '上午',
    chipEmoji: '☀',
    title: '隐山咖啡',
    tags: ['新开业', '安静', '适合一个人'],
    meta: [
      { icon: 'wallet', text: '预算 ¥35' },
      { icon: 'footprints', text: '步行12分钟' },
    ],
    reason: '新开的精品咖啡店，装修很有格调，适合坐一上午',
  },
  {
    time: '14:00',
    chipLabel: '下午',
    chipEmoji: '🌿',
    title: '植物园散步',
    tags: ['户外', '免费', '今晚有晚霞'],
    meta: [
      { icon: 'clock', text: '预计40分钟' },
      { icon: 'mapPin', text: '步行20分钟/地铁1站' },
    ],
    reason: '今天天气很好，植物园的荷花开了，适合走走',
  },
  {
    time: '19:30',
    chipLabel: '晚上',
    chipEmoji: '🎬',
    title: '《机器人之梦》',
    tags: ['电影', '动画', '高分'],
    meta: [
      { icon: 'wallet', text: '预算 ¥45' },
      { icon: 'mapPin', text: '百老汇影城（步行15分钟）' },
      { icon: 'clock', text: '场次 19:30' },
    ],
    reason: '这部电影评价很好，一个人看也会很感动',
  },
  {
    time: '22:00',
    chipLabel: '睡前',
    chipEmoji: '🌙',
    title: '阅读30分钟',
    tags: ['在家', '成长习惯'],
    meta: [
      { icon: 'bookOpen', text: '《活着》— 你已经读到第8章' },
    ],
    reason: '睡前阅读帮助放松，也能让你慢慢进步',
  },
]

function getMetaIcon(icon: TimelineMeta['icon']) {
  switch (icon) {
    case 'wallet':
      return Wallet
    case 'footprints':
      return Footprints
    case 'clock':
      return Clock
    case 'mapPin':
      return MapPin
    case 'bookOpen':
      return BookOpen
    default:
      return Clock
  }
}

/* ---------- 导航 ---------- */

function goBack(): void {
  uni.navigateBack({
    fail: () => uni.reLaunch({ url: '/pages/today/index' }),
  })
}

function goReplan(): void {
  uni.navigateTo({ url: '/pages/today/replan?planId=1' })
}

function startPlan(): void {
  uni.showToast({ title: '今天开始啦', icon: 'success' })
}
</script>

<template>
  <view class="page">
    <!-- ===== 顶部导航（sticky 毛玻璃） ===== -->
    <view class="top-nav">
      <view class="top-nav-inner">
        <view class="nav-btn" @click="goBack">
          <ChevronLeft :size="18" :stroke-width="2" color="#44403C" />
        </view>
        <text class="nav-title">今日规划</text>
        <view class="nav-text-btn" @click="goReplan">
          <text class="nav-text-btn-inner">重新规划</text>
        </view>
      </view>
    </view>

    <!-- ===== 内容区 ===== -->
    <view class="content">
      <!-- Overview 卡片 -->
      <view class="overview-card">
        <text class="overview-date">7月28日 周六</text>
        <view class="overview-row">
          <Sun :size="14" :stroke-width="2" color="#FB923C" />
          <text class="overview-text">晴 28°C</text>
          <text class="overview-dot">·</text>
          <text class="overview-text">预算 ¥150</text>
          <text class="overview-dot">·</text>
          <text class="overview-stars">★★★★★</text>
        </view>
        <view class="overview-row">
          <MapPin :size="14" :stroke-width="2" color="#FB923C" />
          <text class="overview-text">预计外出3次</text>
          <text class="overview-dot">·</text>
          <Footprints :size="14" :stroke-width="2" color="#FB923C" />
          <text class="overview-text">步行约6000步</text>
        </view>
      </view>

      <!-- 时间轴 -->
      <view class="timeline">
        <view
          v-for="(item, idx) in timeline"
          :key="idx"
          class="timeline-item"
        >
          <!-- 左侧：时间 + 圆点 + 连线 -->
          <view class="timeline-left">
            <text class="timeline-time">{{ item.time }}</text>
            <view class="timeline-dot"></view>
            <view
              v-if="idx < timeline.length - 1"
              class="timeline-line"
            ></view>
          </view>

          <!-- 右侧卡片 -->
          <view class="timeline-card">
            <view class="card-header">
              <view class="card-header-left">
                <view class="tag-chip">
                  <text class="tag-chip-emoji">{{ item.chipEmoji }}</text>
                  <text class="tag-chip-text">{{ item.chipLabel }}</text>
                </view>
                <text class="card-title">{{ item.title }}</text>
              </view>
              <ChevronRight class="card-arrow" :size="18" :stroke-width="2" color="#78716C" />
            </view>

            <!-- 标签（带分隔圆点） -->
            <view class="card-tags">
              <template v-for="(tag, tIdx) in item.tags" :key="tIdx">
                <text v-if="tIdx > 0" class="card-tag-sep">·</text>
                <text class="card-tag">{{ tag }}</text>
              </template>
            </view>

            <!-- meta -->
            <view class="card-meta">
              <view
                v-for="(m, mIdx) in item.meta"
                :key="mIdx"
                class="card-meta-item"
              >
                <component
                  :is="getMetaIcon(m.icon)"
                  :size="13"
                  :stroke-width="2"
                  color="#57534E"
                />
                <text class="card-meta-text">{{ m.text }}</text>
              </view>
            </view>

            <!-- reason 引用（左边框） -->
            <text class="card-reason">{{ item.reason }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- ===== 底部操作栏（固定） ===== -->
    <view class="bottom-bar">
      <view class="bottom-bar-inner">
        <view class="btn-ai-adjust" @click="goReplan">
          <Sparkles :size="16" :stroke-width="2" color="#78716C" />
          <text class="btn-ai-text">AI帮我调整</text>
        </view>
        <view class="btn-start" @click="startPlan">
          <text class="btn-start-text">开始今天</text>
        </view>
      </view>
    </view>
  </view>
</template>

<style scoped lang="scss">
.page {
  min-height: 100vh;
  background-color: $solo-background;
  position: relative;
}

/* ===== 顶部导航（sticky + 毛玻璃） ===== */
.top-nav {
  position: sticky;
  top: 0;
  z-index: 30;
  background-color: $solo-glass-bg;
  backdrop-filter: blur(24rpx);
  -webkit-backdrop-filter: blur(24rpx);
  border-bottom: 1rpx solid $solo-border;
}

.top-nav-inner {
  max-width: 896rpx;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 104rpx;
  padding: 0 32rpx;
  position: relative;
}

.nav-btn {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  background-color: $solo-muted;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.nav-title {
  font-size: 34rpx;
  font-weight: 600;
  color: $solo-foreground;
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
  white-space: nowrap;
}

.nav-text-btn {
  padding: 12rpx 8rpx;
  flex-shrink: 0;
}
.nav-text-btn-inner {
  font-size: 28rpx;
  color: $solo-primary-500;
}

/* ===== 内容区 ===== */
.content {
  max-width: 896rpx;
  margin: 0 auto;
  padding: 16rpx 32rpx 200rpx;
}

/* ===== Overview 卡片 ===== */
.overview-card {
  background-color: $solo-primary-50;
  border-radius: $solo-radius-md;
  padding: 32rpx;
  margin: 16rpx 0 40rpx;
}

.overview-date {
  display: block;
  font-size: 34rpx;
  font-weight: 600;
  color: $solo-foreground;
  margin-bottom: 12rpx;
}

.overview-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
  font-size: 26rpx;
  color: $solo-neutral-600;
  line-height: 1.6;

  & + & {
    margin-top: 8rpx;
  }
}

.overview-dot {
  color: $solo-neutral-300;
}

.overview-text {
  font-size: 26rpx;
  color: $solo-neutral-600;
}

.overview-stars {
  color: $solo-primary-500;
  letter-spacing: 2rpx;
}

/* ===== 时间轴 ===== */
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
  color: $solo-muted-foreground;
  white-space: nowrap;
  margin-bottom: 16rpx;
  line-height: 1;
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

/* ===== Timeline Card ===== */
.timeline-card {
  flex: 1;
  min-width: 0;
  background-color: $solo-card;
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

/* tag-chip（时段 badge：☀ 上午 等） */
.tag-chip {
  display: inline-flex;
  align-items: center;
  gap: 4rpx;
  height: 44rpx;
  padding: 0 16rpx;
  border-radius: $solo-radius-sm;
  background-color: $solo-primary-50;
  margin-bottom: 12rpx;
}
.tag-chip-emoji {
  font-size: 20rpx;
  line-height: 1;
}
.tag-chip-text {
  font-size: 22rpx;
  color: $solo-primary-600;
}

.card-title {
  display: block;
  font-size: 32rpx;
  font-weight: 600;
  color: $solo-card-foreground;
  line-height: 1.3;
}

.card-arrow {
  flex-shrink: 0;
  margin-top: 4rpx;
}

/* 标签（带分隔圆点） */
.card-tags {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 4rpx 16rpx;
  margin-bottom: 20rpx;
}

.card-tag {
  font-size: 24rpx;
  color: $solo-muted-foreground;
  white-space: nowrap;
}

.card-tag-sep {
  font-size: 24rpx;
  color: $solo-neutral-300;
}

/* meta 信息行 */
.card-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8rpx 24rpx;
  margin-bottom: 20rpx;
}

.card-meta-item {
  display: inline-flex;
  align-items: center;
  gap: 8rpx;
  white-space: nowrap;
}

.card-meta-text {
  font-size: 26rpx;
  color: $solo-neutral-600;
}

/* reason 引用（左边框） */
.card-reason {
  display: block;
  font-size: 26rpx;
  color: $solo-muted-foreground;
  line-height: 1.6;
  border-left: 4rpx solid $solo-primary-100;
  padding-left: 20rpx;
  margin-top: 8rpx;
}

/* ===== 底部固定操作栏（毛玻璃） ===== */
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
.btn-ai-text {
  font-size: 28rpx;
  color: $solo-muted-foreground;
}

.btn-start {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background-color: $solo-primary-500;
  color: #fff;
  border-radius: $solo-radius-full;
  padding: 20rpx 48rpx;
  flex-shrink: 0;
}
.btn-start-text {
  font-size: 28rpx;
  font-weight: 500;
  color: #fff;
  white-space: nowrap;
}
</style>
