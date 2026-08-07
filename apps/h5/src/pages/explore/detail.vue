<script setup lang="ts">
/**
 * Page 07: 地点详情（Explore）。
 *
 * 设计稿复用 designs/solo-life-os-mobile/pages/activity-detail.html
 * 文档：designs/solo-life-os-mobile/docs/02-explore-module-review.md §Page 07
 *
 * 与 Today 活动详情的区分：
 * - Today Page 04：侧重"为什么此时推荐给你"
 * - Explore Page 07：侧重"客观信息 + 为什么适合你"（当前实现）
 *
 * 信息架构：
 * - Sticky Top Nav：返回 + 标题
 * - Hero 头图：大尺寸渐变氛围图 + 收藏按钮（毛玻璃）
 * - 场所信息：名称 + 评分 + 距离 + 特征 pills
 * - AI 推荐理由卡（暖色背景）
 * - 信息列表：地址 / 营业时间 / 人均消费（三条，Chevron 可点）
 * - Fixed Bottom：导航按钮 + 主 CTA「加入今天」
 */
import { ref } from 'vue'
import {
  ChevronLeft,
  Heart,
  Star,
  Footprints,
  Sparkles,
  MapPin,
  Clock,
  Banknote,
  ChevronRight,
  Navigation,
} from 'lucide-vue-next'

/* ---------- 收藏状态 ---------- */
const isFavorited = ref(false)
function toggleFavorite() {
  isFavorited.value = !isFavorited.value
}

/* ---------- 详情信息 ---------- */
interface InfoItem {
  icon: typeof MapPin
  iconColor: string
  iconBg: string
  label: string
  value: string
  hasChevron: boolean
}

const infoList: InfoItem[] = [
  {
    icon: MapPin,
    iconColor: '#C2410C',
    iconBg: '#F5F5F4',
    label: '地址',
    value: '中山北路32号',
    hasChevron: true,
  },
  {
    icon: Clock,
    iconColor: '#22C55E',
    iconBg: '#F5F5F4',
    label: '营业时间',
    value: '营业中 · 今天到 22:00',
    hasChevron: false,
  },
  {
    icon: Banknote,
    iconColor: '#C2410C',
    iconBg: '#F5F5F4',
    label: '人均消费',
    value: '人均 ¥45',
    hasChevron: false,
  },
]

const featurePills = ['安静', '可办公', '一个人不尴尬']

/* ---------- 导航 ---------- */
function goBack(): void {
  uni.navigateBack({
    fail: () => uni.reLaunch({ url: '/pages/explore/index' }),
  })
}
function addToToday(): void {
  uni.showToast({ title: '已加入今天', icon: 'success' })
}
function goNavigate(): void {
  uni.showToast({ title: '即将调起导航', icon: 'none' })
}
</script>

<template>
  <view class="page">
    <!-- Sticky Top Nav -->
    <view class="top-nav">
      <view class="top-nav-inner">
        <view class="btn-icon" @tap="goBack">
          <ChevronLeft class="nav-icon" />
        </view>
        <text class="nav-title">活动详情</text>
        <view class="w-9" />
      </view>
    </view>

    <!-- Hero 头图 -->
    <view class="hero">
      <view class="hero-gradient">
        <view class="glow-tr" />
        <view class="glow-bl" />
        <text class="hero-emoji">☕</text>
      </view>
      <view class="btn-favorite" @tap="toggleFavorite()">
        <Heart class="fav-icon" :class="{ filled: isFavorited }" />
      </view>
    </view>

    <view class="content">
      <!-- 场所信息 -->
      <view class="info-sec">
        <text class="place-name">隐山咖啡</text>
        <view class="meta-row">
          <view class="stars">
            <Star class="star" fill="currentColor" />
            <text class="rating">4.9</text>
          </view>
          <text class="dot-sep">·</text>
          <view class="distance">
            <Footprints class="mini-icon" />
            <text>步行12分钟</text>
          </view>
        </view>
        <view class="pills">
          <text
            v-for="(p, i) in featurePills"
            :key="i"
            class="pill"
          >{{ p }}</text>
        </view>
      </view>

      <!-- AI 推荐理由 -->
      <view class="ai-card">
        <view class="ai-head">
          <view class="ai-icon-wrap">
            <Sparkles class="ai-icon" />
          </view>
          <text class="ai-title">为什么推荐给你</text>
        </view>
        <text class="ai-text">今天周三，你最近有点累。这里灯光柔和，适合一个人坐一会儿发发呆。手冲耶加雪菲是招牌。</text>
      </view>

      <!-- 信息卡片 -->
      <view class="info-card">
        <view
          v-for="(item, i) in infoList"
          :key="i"
          class="info-row"
          :class="{ 'no-border': i === infoList.length - 1 }"
        >
          <view
            class="row-icon"
            :style="{ background: item.iconBg }"
          >
            <component :is="item.icon" class="row-icon-inner" :style="{ color: item.iconColor }" />
          </view>
          <view class="row-main">
            <text class="row-label">{{ item.label }}</text>
            <text class="row-value">{{ item.value }}</text>
          </view>
          <ChevronRight v-if="item.hasChevron" class="chev" />
        </view>
      </view>

      <view class="bottom-placeholder" />
    </view>

    <!-- Fixed Bottom Action Bar -->
    <view class="bottom-bar">
      <view class="bottom-inner">
        <view class="btn-nav" aria-label="导航" @tap="goNavigate">
          <Navigation class="nav-btn-icon" />
        </view>
        <view class="btn-primary" @tap="addToToday">
          <text>加入今天</text>
        </view>
      </view>
    </view>
  </view>
</template>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: $solo-background;
}

/* ---------- Sticky Top Nav ---------- */
.top-nav {
  position: sticky;
  top: 0;
  z-index: 50;
  background: $solo-glass-bg;
  backdrop-filter: blur(24rpx);
  border-bottom: 1rpx solid $solo-border;
  height: 96rpx;
}
.top-nav-inner {
  height: 100%;
  padding: 0 32rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  max-width: 750rpx;
  margin: 0 auto;
}
.btn-icon {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-left: -16rpx;
  &:active { background: $solo-muted; }
}
.nav-icon {
  width: 40rpx;
  height: 40rpx;
  color: $solo-foreground;
}
.nav-title {
  font-size: $solo-font-base;
  font-weight: 600;
  color: $solo-foreground;
  margin-left: -72rpx; /* 居中对齐（抵消左右按钮宽度） */
}
.w-9 { width: 72rpx; }

/* ---------- Hero 头图 ---------- */
.hero {
  position: relative;
  margin: 0;
}
.hero-gradient {
  position: relative;
  height: 448rpx;
  background: linear-gradient(160deg,
    $solo-primary-200 0%,
    $solo-primary-400 45%,
    $solo-primary-600 80%,
    $solo-primary-800 100%);
  overflow: hidden;
}
.glow-tr, .glow-bl {
  position: absolute;
  border-radius: 50%;
  background: radial-gradient(circle, #FFF7ED 0%, transparent 70%);
}
.glow-tr {
  top: 64rpx; right: 80rpx;
  width: 288rpx; height: 288rpx;
  opacity: 0.3;
}
.glow-bl {
  bottom: 80rpx; left: 96rpx;
  width: 224rpx; height: 224rpx;
  opacity: 0.25;
}
.hero-emoji {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 140rpx;
  opacity: 0.85;
}
.btn-favorite {
  position: absolute;
  top: 32rpx;
  right: 32rpx;
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  background: rgba(255,255,255,0.25);
  backdrop-filter: blur(16rpx);
  display: flex;
  align-items: center;
  justify-content: center;
  &:active { transform: scale(0.95); }
}
.fav-icon {
  width: 40rpx;
  height: 40rpx;
  color: #fff;
  &.filled {
    fill: #fff;
  }
}

/* ---------- Content ---------- */
.content {
  padding: 4rpx 32rpx 0;
  display: flex;
  flex-direction: column;
  gap: 32rpx;
}

/* ---------- 场所信息 ---------- */
.info-sec {
  padding-top: 8rpx;
}
.place-name {
  display: block;
  font-size: 48rpx;
  font-weight: 700;
  color: $solo-foreground;
}
.meta-row {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin-top: 16rpx;
}
.stars {
  display: flex;
  align-items: center;
  gap: 2rpx;
}
.star {
  width: 32rpx;
  height: 32rpx;
  color: $solo-state-warning;
}
.rating {
  font-size: $solo-font-sm;
  font-weight: 600;
  color: $solo-foreground;
  margin-left: 4rpx;
}
.dot-sep {
  font-size: $solo-font-sm;
  color: $solo-neutral-400;
}
.distance {
  display: inline-flex;
  align-items: center;
  gap: 4rpx;
  font-size: $solo-font-sm;
  color: $solo-muted-foreground;
}
.mini-icon {
  width: 32rpx;
  height: 32rpx;
  color: $solo-neutral-500;
}
.pills {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-top: 24rpx;
}
.pill {
  padding: 8rpx 20rpx;
  border-radius: $solo-radius-full;
  background: $solo-primary-50;
  color: $solo-primary-700;
  font-size: $solo-font-xs;
  font-weight: 500;
}

/* ---------- AI 推荐理由卡 ---------- */
.ai-card {
  padding: 32rpx;
  border-radius: $solo-radius-2xl;
  background: $solo-primary-50;
}
.ai-head {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin-bottom: 16rpx;
}
.ai-icon-wrap {
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  background: $solo-primary-100;
  display: flex;
  align-items: center;
  justify-content: center;
}
.ai-icon {
  width: 32rpx;
  height: 32rpx;
  color: $solo-primary-600;
}
.ai-title {
  font-size: $solo-font-base;
  font-weight: 600;
  color: $solo-primary-800;
}
.ai-text {
  display: block;
  font-size: $solo-font-sm;
  line-height: 1.7;
  color: $solo-primary-900;
}

/* ---------- 信息卡片 ---------- */
.info-card {
  border-radius: $solo-radius-2xl;
  border: 1rpx solid $solo-border;
  overflow: hidden;
  background: $solo-card;
}
.info-row {
  display: flex;
  align-items: center;
  gap: 24rpx;
  padding: 28rpx 32rpx;
  border-bottom: 1rpx solid $solo-border;
  &.no-border {
    border-bottom: none;
  }
}
.row-icon {
  flex-shrink: 0;
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}
.row-icon-inner {
  width: 36rpx;
  height: 36rpx;
}
.row-main {
  flex: 1;
  min-width: 0;
}
.row-label {
  display: block;
  font-size: $solo-font-xs;
  color: $solo-muted-foreground;
}
.row-value {
  display: block;
  margin-top: 4rpx;
  font-size: $solo-font-sm;
  font-weight: 500;
  color: $solo-foreground;
}
.chev {
  width: 32rpx;
  height: 32rpx;
  color: $solo-neutral-400;
  flex-shrink: 0;
}

/* ---------- Bottom Placeholder ---------- */
.bottom-placeholder {
  height: 192rpx;
}

/* ---------- Fixed Bottom Action Bar ---------- */
.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 50;
  padding: 24rpx 32rpx;
  background: $solo-glass-bg;
  backdrop-filter: blur(24rpx);
  border-top: 1rpx solid $solo-border;
}
.bottom-inner {
  max-width: 750rpx;
  margin: 0 auto;
  display: flex;
  align-items: center;
  gap: 24rpx;
}
.btn-nav {
  flex-shrink: 0;
  width: 88rpx;
  height: 88rpx;
  border-radius: 50%;
  border: 1rpx solid $solo-border;
  background: $solo-card;
  display: flex;
  align-items: center;
  justify-content: center;
  &:active { background: $solo-muted; }
}
.nav-btn-icon {
  width: 40rpx;
  height: 40rpx;
  color: $solo-neutral-700;
}
.btn-primary {
  flex: 1;
  height: 88rpx;
  border-radius: $solo-radius-full;
  background: $solo-primary;
  box-shadow: $solo-shadow-primary;
  display: flex;
  align-items: center;
  justify-content: center;
  text {
    color: $solo-primary-foreground;
    font-size: $solo-font-sm;
    font-weight: 600;
  }
  &:active { transform: scale(0.98); }
}
</style>
