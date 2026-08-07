<script setup lang="ts">
/**
 * Page 08: 路线规划（Route）。
 *
 * 设计稿：designs/solo-life-os-mobile/pages/route.html
 * 文档：designs/solo-life-os-mobile/docs/02-explore-module-review.md §Page 08
 *
 * 信息架构：
 * - Sticky Top Nav：返回 + 标题 + 收藏
 * - 路线概览卡：起点→终点连线 + 距离/时间
 * - 地图占位：SVG 网格 + 虚线路径 + 起终点 pin + 起终点标签
 * - AI 建议卡（暖色背景）
 * - 步行路线时间轴：4 步（编号→终点 MapPin），连线自动延伸
 * - Fixed Bottom：次按钮"加入今天" + 主按钮"开始行走"
 */
import { ref } from 'vue'
import {
  ChevronLeft,
  Heart,
  Footprints,
  Clock,
  Sparkles,
  MapPin,
  Navigation,
} from 'lucide-vue-next'

/* ---------- 收藏状态 ---------- */
const isFavorited = ref(false)
function toggleFavorite() {
  isFavorited.value = !isFavorited.value
}

/* ---------- 路线步骤 ---------- */
interface RouteStep {
  index: number | 'end'
  title: string
  desc: string
  primary?: boolean
}

const steps: RouteStep[] = [
  { index: 1, title: '从当前位置出发', desc: '面向中山北路方向' },
  { index: 2, title: '沿中山北路走800米', desc: '约10分钟，经过梧桐小道' },
  { index: 3, title: '左转进入曙光路', desc: '继续步行约600米' },
  { index: 'end', title: '到达目的地', desc: '植物园南门', primary: true },
]

/* ---------- 导航 ---------- */
function goBack(): void {
  uni.navigateBack({
    fail: () => uni.reLaunch({ url: '/pages/explore/index' }),
  })
}
function addToToday(): void {
  uni.showToast({ title: '已加入今天', icon: 'success' })
}
function startWalk(): void {
  uni.showToast({ title: '开始行走', icon: 'success' })
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
        <text class="nav-title">路线规划</text>
        <view class="btn-icon" @tap="toggleFavorite()">
          <Heart class="nav-icon" :class="{ filled: isFavorited }" />
        </view>
      </view>
    </view>

    <view class="content">
      <!-- Route Summary Card -->
      <view class="summary">
        <view class="summary-row">
          <view class="endpoint start">
            <view class="dot dot-start" />
            <text class="endpoint-label">隐山咖啡</text>
          </view>
          <view class="connector">
            <view class="conn-line" />
          </view>
          <view class="endpoint end">
            <text class="endpoint-label">植物园</text>
            <view class="dot dot-end" />
          </view>
        </view>
        <view class="summary-meta">
          <view class="meta-item">
            <Footprints class="meta-icon" />
            <text>2.3km</text>
          </view>
          <text class="dot-sep">·</text>
          <view class="meta-item">
            <Clock class="meta-icon" />
            <text>约30分钟步行</text>
          </view>
        </view>
      </view>

      <!-- Map Placeholder -->
      <view class="map-wrap">
        <!-- Grid pattern -->
        <view class="map-grid" />
        <!-- Route Path SVG -->
        <svg class="route-svg" viewBox="0 0 320 256" preserveAspectRatio="none">
          <path
            d="M 40 200 Q 80 180 100 150 T 160 120 Q 200 90 240 70 T 280 50"
            fill="none"
            stroke="#F97316"
            stroke-width="3"
            stroke-linecap="round"
            stroke-dasharray="6 4"
            opacity="0.85"
          />
          <!-- Start pin -->
          <circle cx="40" cy="200" r="8" fill="#F97316" />
          <circle cx="40" cy="200" r="4" fill="#FFFFFF" />
          <!-- End pin -->
          <circle cx="280" cy="50" r="8" fill="#22C55E" />
          <circle cx="280" cy="50" r="4" fill="#FFFFFF" />
        </svg>
        <!-- Labels -->
        <view class="label label-start">
          <view class="label-dot label-dot-start" />
          <text>起点</text>
        </view>
        <view class="label label-end">
          <view class="label-dot label-dot-end" />
          <text>终点</text>
        </view>
      </view>

      <!-- AI Suggestion -->
      <view class="ai-card">
        <view class="ai-icon-wrap">
          <Sparkles class="ai-icon" />
        </view>
        <text class="ai-text">这条路线会经过一条梧桐小道，树荫很密，现在走正好不晒。</text>
      </view>

      <!-- Steps -->
      <view class="steps-wrap">
        <text class="steps-title">步行路线</text>
        <view
          v-for="(s, i) in steps"
          :key="i"
          class="step-row"
        >
          <view class="step-col">
            <view
              class="step-badge"
              :class="{ end: s.index === 'end' }"
            >
              <MapPin v-if="s.index === 'end'" class="step-badge-icon" />
              <text v-else class="step-index">{{ s.index }}</text>
            </view>
            <view v-if="i !== steps.length - 1" class="step-line" />
          </view>
          <view class="step-main" :class="{ 'pb-0': i === steps.length - 1 }">
            <text class="step-title">{{ s.title }}</text>
            <text class="step-desc">{{ s.desc }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- Fixed Bottom Action Bar -->
    <view class="bottom-bar">
      <view class="bottom-inner">
        <view class="btn-secondary" @tap="addToToday">
          <text>加入今天</text>
        </view>
        <view class="btn-primary" @tap="startWalk">
          <Navigation class="btn-icon-inner" />
          <text>开始行走</text>
        </view>
      </view>
    </view>
  </view>
</template>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: $solo-background;
  padding-bottom: 192rpx;
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
  & + .btn-icon { margin-left: 0; margin-right: -16rpx; }
}
.nav-icon {
  width: 40rpx;
  height: 40rpx;
  color: $solo-foreground;
  &.filled { fill: currentColor; }
}
.nav-title {
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
  font-size: $solo-font-base;
  font-weight: 600;
  color: $solo-foreground;
}

/* ---------- Content ---------- */
.content {
  padding: 32rpx 32rpx 0;
  display: flex;
  flex-direction: column;
  gap: 32rpx;
}

/* ---------- Summary ---------- */
.summary {
  padding: 32rpx;
  border-radius: $solo-radius-xl;
  border: 1rpx solid $solo-border;
  background: $solo-card;
}
.summary-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12rpx;
}
.endpoint {
  display: flex;
  align-items: center;
  gap: 16rpx;
  flex-shrink: 0;
  &.end { flex-direction: row-reverse; }
}
.dot {
  width: 20rpx;
  height: 20rpx;
  border-radius: 50%;
  flex-shrink: 0;
  &-start { background: $solo-primary; }
  &-end { background: $solo-state-success; }
}
.endpoint-label {
  font-size: $solo-font-sm;
  font-weight: 500;
  color: $solo-foreground;
}
.connector {
  flex: 1;
  display: flex;
  align-items: center;
  min-width: 40rpx;
}
.conn-line {
  width: 100%;
  height: 2rpx;
  background: $solo-border;
  position: relative;
  &::before {
    content: '';
    position: absolute;
    inset: 0;
    background: $solo-primary;
  }
}
.summary-meta {
  margin-top: 24rpx;
  display: flex;
  align-items: center;
  gap: 16rpx;
  font-size: $solo-font-xs;
  color: $solo-muted-foreground;
}
.meta-item {
  display: inline-flex;
  align-items: center;
  gap: 8rpx;
}
.meta-icon {
  width: 28rpx;
  height: 28rpx;
}
.dot-sep { color: $solo-neutral-300; }

/* ---------- Map Placeholder ---------- */
.map-wrap {
  position: relative;
  height: 512rpx;
  border-radius: $solo-radius-lg;
  overflow: hidden;
  background: linear-gradient(135deg, #F5F5F4 0%, #E7E5E4 100%);
}
.map-grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(to right, rgba(214, 211, 209, 0.5) 1rpx, transparent 1rpx),
    linear-gradient(to bottom, rgba(214, 211, 209, 0.5) 1rpx, transparent 1rpx);
  background-size: 48rpx 48rpx;
  opacity: 0.6;
}
.route-svg {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
}
.label {
  position: absolute;
  display: inline-flex;
  align-items: center;
  gap: 8rpx;
  padding: 8rpx 16rpx;
  background: #fff;
  border-radius: $solo-radius-sm;
  box-shadow: $solo-shadow-sm;
  font-size: $solo-font-xs;
  font-weight: 500;
  color: $solo-foreground;
  &-start { left: 48rpx; bottom: 48rpx; }
  &-end { right: 48rpx; top: 48rpx; }
}
.label-dot {
  width: 16rpx;
  height: 16rpx;
  border-radius: 50%;
  &-start { background: $solo-primary; }
  &-end { background: $solo-state-success; }
}

/* ---------- AI Suggestion ---------- */
.ai-card {
  padding: 32rpx;
  border-radius: $solo-radius-xl;
  background: $solo-primary-50;
  border: 1rpx solid $solo-primary-100;
  display: flex;
  align-items: flex-start;
  gap: 24rpx;
}
.ai-icon-wrap {
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  background: $solo-primary-100;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.ai-icon {
  width: 32rpx;
  height: 32rpx;
  color: $solo-primary-600;
}
.ai-text {
  flex: 1;
  font-size: $solo-font-sm;
  line-height: 1.7;
  color: $solo-primary-900;
}

/* ---------- Steps ---------- */
.steps-wrap {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}
.steps-title {
  padding: 0 4rpx;
  font-size: $solo-font-sm;
  font-weight: 600;
  color: $solo-foreground;
}
.step-row {
  display: flex;
  gap: 24rpx;
  background: $solo-card;
  border: 1rpx solid $solo-border;
  border-radius: $solo-radius-xl;
  padding: 32rpx;
}
.step-col {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 56rpx;
  flex-shrink: 0;
}
.step-badge {
  width: 56rpx;
  height: 56rpx;
  border-radius: 50%;
  background: $solo-primary;
  color: $solo-primary-foreground;
  font-size: $solo-font-xs;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  &.end {
    background: $solo-state-success;
    color: #fff;
  }
}
.step-badge-icon {
  width: 28rpx;
  height: 28rpx;
}
.step-line {
  width: 2rpx;
  flex: 1;
  background: $solo-border;
  margin-top: 8rpx;
}
.step-main {
  flex: 1;
  padding-bottom: 16rpx;
  &.pb-0 { padding-bottom: 0; }
}
.step-title {
  display: block;
  font-size: $solo-font-sm;
  font-weight: 500;
  color: $solo-foreground;
}
.step-desc {
  display: block;
  margin-top: 8rpx;
  font-size: $solo-font-xs;
  color: $solo-muted-foreground;
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
  gap: 24rpx;
}
.btn-secondary {
  flex: 1;
  height: 88rpx;
  border-radius: $solo-radius-xl;
  border: 1rpx solid $solo-border;
  background: $solo-card;
  display: flex;
  align-items: center;
  justify-content: center;
  text {
    font-size: $solo-font-sm;
    font-weight: 500;
    color: $solo-foreground;
  }
  &:active { background: $solo-muted; }
}
.btn-primary {
  flex: 1;
  height: 88rpx;
  border-radius: $solo-radius-xl;
  background: $solo-primary;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  text {
    font-size: $solo-font-sm;
    font-weight: 600;
    color: $solo-primary-foreground;
  }
  &:active { opacity: 0.9; }
}
.btn-icon-inner {
  width: 32rpx;
  height: 32rpx;
  color: $solo-primary-foreground;
}
</style>
