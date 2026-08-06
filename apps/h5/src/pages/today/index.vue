<script setup lang="ts">
/**
 * Page 01: 今天（首页）。
 *
 * 设计稿：designs/solo-life-os-mobile/pages/today.html
 * 文档：designs/solo-life-os-mobile/docs/01-today-module-review.md §Page 01
 *
 * 当前实现：按设计稿 1:1 还原布局，纯静态 mock 展示（暂不接后端接口，Sprint 5 接入 Planner Agent 后再联调）。
 *
 * 信息架构：
 * - 顶部问候区（时间感知问候 + slogan）
 * - AI 今日规划 Hero 卡（渐变背景 + 装饰圆形 + 活动 pills + CTA）
 * - AI 为你推荐（横滑 4 张推荐卡，隐藏滚动条）
 * - 附近正在发生（2 张列表卡，第二张带 3缺2 红色角标）
 * - 底部 2 CTA：主按钮"今晚记录一下" + 次级"重新规划"链接
 */
import { computed } from 'vue'
import {
  Sparkles,
  Sun,
  Coffee,
  BookOpen,
  TreePine,
  Film,
  ArrowRight,
  MapPin,
  Brain,
  Star,
  Heart,
  Palette,
  Users,
  Footprints,
  Clock,
  ChevronRight,
  MoonStar,
  RefreshCw,
} from 'lucide-vue-next'

/* ---------- 静态 Mock 数据（与设计稿 today.html 一致） ---------- */

interface HeroPill {
  label: string
  iconColor: string
}

const heroPills: HeroPill[] = [
  { label: '晨间咖啡', iconColor: '#F97316' },
  { label: '书店阅读', iconColor: '#F97316' },
  { label: '公园散步', iconColor: '#22C55E' },
  { label: '晚间电影', iconColor: '#3B82F6' },
]

interface RecommendCard {
  title: string
  distance: string
  tagLabel: string
  tagBg: string
  tagColor: string
  gradientFrom: string
  gradientTo: string
  iconColor: string
}

const recommendCards: RecommendCard[] = [
  {
    title: '安静咖啡馆',
    distance: '0.8km',
    tagLabel: '适合独处',
    tagBg: '#FFF7ED',
    tagColor: '#C2410C',
    gradientFrom: '#FFF7ED',
    gradientTo: '#FED7AA',
    iconColor: '#EA580C',
  },
  {
    title: '城市公园',
    distance: '1.2km',
    tagLabel: '天气正好',
    tagBg: '#F0FDF4',
    tagColor: '#15803D',
    gradientFrom: '#F0FDF4',
    gradientTo: '#BBF7D0',
    iconColor: '#22C55E',
  },
  {
    title: '一个人看电影',
    distance: '2.1km',
    tagLabel: '高分治愈',
    tagBg: '#EFF6FF',
    tagColor: '#1D4ED8',
    gradientFrom: '#EFF6FF',
    gradientTo: '#BFDBFE',
    iconColor: '#3B82F6',
  },
  {
    title: '书店阅读',
    distance: '0.5km',
    tagLabel: '你常去',
    tagBg: '#FEF3C7',
    tagColor: '#92400E',
    gradientFrom: '#FEF3C7',
    gradientTo: '#FDE68A',
    iconColor: '#B45309',
  },
]

interface NearbyCard {
  title: string
  subDistance: string
  subMeta: string
  badge?: string
  bgColor: string
  iconColor: string
}

const nearbyCards: NearbyCard[] = [
  {
    title: '一个人看展 · 城市光影摄影展',
    subDistance: '1.5km',
    subMeta: '8人正在看',
    bgColor: '#FFF7ED',
    iconColor: '#F97316',
  },
  {
    title: '今晚 City Walk · 西湖夜游',
    subDistance: '19:30 出发',
    subMeta: '3/5人',
    badge: '3缺2',
    bgColor: '#EFF6FF',
    iconColor: '#3B82F6',
  },
]

/* ---------- 状态 ---------- */

const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return { text: '夜深了', emoji: '🌙' }
  if (hour < 11) return { text: '早上好', emoji: '☀️' }
  if (hour < 14) return { text: '中午好', emoji: '🌤️' }
  if (hour < 18) return { text: '下午好', emoji: '☀️' }
  if (hour < 22) return { text: '晚上好', emoji: '🌆' }
  return { text: '夜深了', emoji: '🌙' }
})

/* ---------- 导航 ---------- */

function goPlanDetail(): void {
  uni.navigateTo({ url: '/pages/today/plan-detail?planId=1' })
}

function goReplan(): void {
  uni.navigateTo({ url: '/pages/today/replan?planId=1' })
}

function goSummary(): void {
  uni.navigateTo({ url: '/pages/today/summary?planId=1' })
}

/* 按索引返回 pill 对应图标（与设计稿一致） */
function getPillIcon(idx: number): typeof Coffee {
  return [Coffee, BookOpen, TreePine, Film][idx] ?? Coffee
}

/* 按索引返回推荐卡对应图标 */
function getRecIcon(idx: number): typeof Coffee {
  return [Coffee, TreePine, Film, BookOpen][idx] ?? Coffee
}

/* 按索引返回推荐卡 tag 图标（brain / sun / star / heart） */
function getRecTagIcon(idx: number): typeof Brain {
  return [Brain, Sun, Star, Heart][idx] ?? Brain
}

/* 按索引返回附近卡图标 */
function getNearbyIcon(idx: number): typeof Palette {
  return [Palette, Footprints][idx] ?? Palette
}

/* 按索引返回附近卡 sub 左侧图标（map-pin / clock） */
function getNearbySubIcon(idx: number): typeof MapPin {
  return [MapPin, Clock][idx] ?? MapPin
}
</script>

<template>
  <view class="page">
    <!-- 顶部问候区 -->
    <view class="greeting">
      <text class="greeting-title">{{ greeting.text }} {{ greeting.emoji }}</text>
      <text class="greeting-sub">今天，过得值得一点</text>
    </view>

    <!-- AI 今日规划 Hero 卡片 -->
    <view class="hero-card">
      <!-- 装饰圆形 -->
      <view class="hero-decor hero-decor-1"></view>
      <view class="hero-decor hero-decor-2"></view>

      <view class="hero-content">
        <!-- 头部：标签 + 天气 -->
        <view class="hero-header">
          <view class="hero-tag">
            <Sparkles class="hero-tag-icon" :size="14" :stroke-width="2" />
            <text class="hero-tag-text">AI 今日规划</text>
          </view>
          <view class="hero-weather">
            <Sun class="hero-weather-icon" :size="16" :stroke-width="2" />
            <text class="hero-weather-text">26°C · 晴</text>
          </view>
        </view>

        <!-- 主标题 -->
        <text class="hero-title">为你准备的今天</text>
        <text class="hero-sub">根据你的心情和节奏，推荐了 4 件小而美好的事</text>

        <!-- 活动预览 pills -->
        <view class="hero-pills">
          <view
            v-for="(pill, idx) in heroPills"
            :key="idx"
            class="pill"
            @click="goPlanDetail"
          >
            <component
              :is="getPillIcon(idx)"
              class="pill-icon"
              :size="14"
              :stroke-width="2"
              :color="pill.iconColor"
            />
            <text class="pill-text">{{ pill.label }}</text>
          </view>
        </view>

        <!-- 查看完整规划按钮 -->
        <view class="btn-hero" @click="goPlanDetail">
          <text class="btn-hero-text">查看完整规划</text>
          <ArrowRight class="btn-hero-arrow" :size="16" :stroke-width="2" />
        </view>
      </view>
    </view>

    <!-- AI 为你推荐 -->
    <view class="section">
      <view class="section-header">
        <view class="section-title-wrap">
          <Sparkles class="section-title-icon" :size="16" :stroke-width="2" color="#F97316" />
          <text class="section-title">AI 为你推荐</text>
        </view>
        <view class="section-more">
          <text class="section-more-text">更多</text>
          <ChevronRight class="section-more-icon" :size="14" :stroke-width="2" />
        </view>
      </view>

      <scroll-view scroll-x class="rec-scroll no-scrollbar" :show-scrollbar="false">
        <view class="rec-list">
          <view
            v-for="(card, idx) in recommendCards"
            :key="idx"
            class="rec-card"
          >
            <view
              class="rec-cover"
              :style="{ background: `linear-gradient(135deg, ${card.gradientFrom}, ${card.gradientTo})` }"
            >
              <component
                :is="getRecIcon(idx)"
                :size="36"
                :stroke-width="2"
                :color="card.iconColor"
              />
            </view>
            <view class="rec-body">
              <text class="rec-title">{{ card.title }}</text>
              <view class="rec-distance">
                <MapPin :size="12" :stroke-width="2" color="#78716C" />
                <text class="rec-distance-text">{{ card.distance }}</text>
              </view>
              <view
                class="rec-tag"
                :style="{ backgroundColor: card.tagBg, color: card.tagColor }"
              >
                <component :is="getRecTagIcon(idx)" :size="12" :stroke-width="2" />
                <text class="rec-tag-text">{{ card.tagLabel }}</text>
              </view>
            </view>
          </view>
        </view>
      </scroll-view>
    </view>

    <!-- 附近正在发生 -->
    <view class="section">
      <view class="section-header">
        <view class="section-title-wrap">
          <MapPin class="section-title-icon" :size="16" :stroke-width="2" color="#F97316" />
          <text class="section-title">附近正在发生</text>
        </view>
      </view>

      <view class="nearby-list">
        <view
          v-for="(card, idx) in nearbyCards"
          :key="idx"
          class="nearby-card"
        >
          <!-- 3缺2 角标（仅第二张） -->
          <view v-if="card.badge" class="nearby-badge">
            <text class="nearby-badge-text">{{ card.badge }}</text>
          </view>

          <view
            class="nearby-icon"
            :style="{ backgroundColor: card.bgColor }"
          >
            <component
              :is="getNearbyIcon(idx)"
              :size="24"
              :stroke-width="2"
              :color="card.iconColor"
            />
          </view>

          <view class="nearby-info">
            <text class="nearby-title">{{ card.title }}</text>
            <view class="nearby-sub">
              <view class="nearby-sub-item">
                <component :is="getNearbySubIcon(idx)" :size="12" :stroke-width="2" color="#78716C" />
                <text class="nearby-sub-text">{{ card.subDistance }}</text>
              </view>
              <view class="nearby-sub-item">
                <Users :size="12" :stroke-width="2" color="#78716C" />
                <text class="nearby-sub-text">{{ card.subMeta }}</text>
              </view>
            </view>
          </view>

          <ChevronRight class="nearby-arrow" :size="16" :stroke-width="2" color="#A8A29E" />
        </view>
      </view>
    </view>

    <!-- 底部操作区 -->
    <view class="bottom-actions">
      <view class="btn-primary" @click="goSummary">
        <MoonStar :size="16" :stroke-width="2" />
        <text class="btn-primary-text">今晚记录一下</text>
      </view>
      <view class="bottom-link" @click="goReplan">
        <RefreshCw class="bottom-link-icon" :size="14" :stroke-width="2" />
        <text class="bottom-link-text">重新规划</text>
      </view>
    </view>
  </view>
</template>

<style scoped lang="scss">
.page {
  min-height: 100vh;
  max-width: 896rpx; /* max-w-md 448px → 896rpx */
  margin: 0 auto;
  padding: 96rpx 32rpx 160rpx;
  background-color: $solo-background;
  position: relative;
}

/* ===== 顶部问候 ===== */
.greeting {
  padding-bottom: 40rpx;
}

.greeting-title {
  display: block;
  font-size: 52rpx;
  font-weight: 700;
  line-height: 1.2;
  color: $solo-foreground;
}

.greeting-sub {
  display: block;
  font-size: 28rpx;
  color: $solo-neutral-500;
  margin-top: 8rpx;
}

/* ===== Hero 卡 ===== */
.hero-card {
  position: relative;
  overflow: hidden;
  border-radius: 40rpx; /* rounded-[20px] → 40rpx */
  padding: 40rpx;
  margin-bottom: 48rpx;
  background: $solo-hero-gradient;
  border: 1rpx solid $solo-primary-200;
}

.hero-decor {
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
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24rpx;
}

.hero-tag {
  display: inline-flex;
  align-items: center;
  gap: 6rpx;
  padding: 8rpx 20rpx;
  border-radius: $solo-radius-full;
  background-color: rgba(249, 115, 22, 0.12);
}
.hero-tag-icon {
  color: $solo-primary-700;
  flex-shrink: 0;
}
.hero-tag-text {
  font-size: 24rpx;
  font-weight: 500;
  color: $solo-primary-700;
}

.hero-weather {
  display: inline-flex;
  align-items: center;
  gap: 4rpx;
}
.hero-weather-icon {
  color: $solo-state-warning;
  flex-shrink: 0;
}
.hero-weather-text {
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
  gap: 6rpx;
  padding: 12rpx 24rpx;
  border-radius: $solo-radius-full;
  background-color: rgba(255, 255, 255, 0.7);
  border: 1rpx solid rgba(249, 115, 22, 0.15);
}
.pill-icon { flex-shrink: 0; }
.pill-text {
  font-size: 24rpx;
  color: $solo-neutral-700;
}

.btn-hero {
  width: 100%;
  height: 88rpx;
  border-radius: $solo-radius-full;
  background-color: $solo-primary-500;
  box-shadow: $solo-shadow-primary-sm;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
}
.btn-hero-text {
  font-size: 28rpx;
  font-weight: 600;
  color: #fff;
}
.btn-hero-arrow {
  color: #fff;
  flex-shrink: 0;
}

/* ===== 通用 section ===== */
.section {
  margin-bottom: 48rpx;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24rpx;
}
.section-title-wrap {
  display: inline-flex;
  align-items: center;
  gap: 6rpx;
}
.section-title-icon { flex-shrink: 0; }
.section-title {
  font-size: 32rpx;
  font-weight: 600;
  color: $solo-foreground;
}

.section-more {
  display: inline-flex;
  align-items: center;
  gap: 2rpx;
}
.section-more-text {
  font-size: 24rpx;
  color: $solo-neutral-500;
}
.section-more-icon {
  color: $solo-neutral-500;
  flex-shrink: 0;
}

/* ===== AI 为你推荐 横滑 ===== */
.rec-scroll {
  width: 100%;
  white-space: nowrap;
  margin: 0 -32rpx;
  padding: 0 32rpx 4rpx;
}

.rec-list {
  display: inline-flex;
  gap: 24rpx;
}

.rec-card {
  flex-shrink: 0;
  width: 300rpx; /* 150px → 300rpx */
  overflow: hidden;
  background-color: $solo-card;
  border: 1rpx solid $solo-border;
  border-radius: $solo-radius-md; /* rounded-xl 12px → 24rpx, 这里用设计稿的 12px */
}

.rec-cover {
  height: 180rpx; /* 90px → 180rpx */
  display: flex;
  align-items: center;
  justify-content: center;
}

.rec-body {
  padding: 24rpx;
}

.rec-title {
  display: block;
  font-size: 28rpx;
  font-weight: 500;
  color: $solo-foreground;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.rec-distance {
  display: inline-flex;
  align-items: center;
  gap: 4rpx;
  margin-top: 8rpx;
}
.rec-distance-text {
  font-size: 22rpx;
  color: $solo-neutral-500;
}

.rec-tag {
  margin-top: 16rpx;
  display: inline-flex;
  align-items: center;
  gap: 4rpx;
  padding: 4rpx 16rpx;
  border-radius: $solo-radius-full;
}
.rec-tag-text {
  font-size: 20rpx;
}

/* ===== 附近正在发生 ===== */
.nearby-list {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.nearby-card {
  position: relative;
  overflow: hidden;
  background-color: $solo-card;
  border: 1rpx solid $solo-border;
  border-radius: $solo-radius-md;
  padding: 32rpx;
  display: flex;
  align-items: center;
  gap: 24rpx;
}

.nearby-badge {
  position: absolute;
  top: 0;
  right: 0;
  padding: 4rpx 16rpx;
  border-bottom-left-radius: $solo-radius-sm;
  background-color: $solo-state-error;
}
.nearby-badge-text {
  font-size: 20rpx;
  font-weight: 500;
  color: #fff;
}

.nearby-icon {
  width: 96rpx;
  height: 96rpx;
  border-radius: $solo-radius-sm;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.nearby-info {
  flex: 1;
  min-width: 0;
}

.nearby-title {
  display: block;
  font-size: 28rpx;
  font-weight: 500;
  color: $solo-foreground;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.nearby-sub {
  margin-top: 8rpx;
  display: flex;
  align-items: center;
  gap: 16rpx;
}
.nearby-sub-item {
  display: inline-flex;
  align-items: center;
  gap: 2rpx;
}
.nearby-sub-text {
  font-size: 24rpx;
  color: $solo-neutral-500;
}

.nearby-arrow {
  flex-shrink: 0;
}

/* ===== 底部操作区 ===== */
.bottom-actions {
  padding-top: 32rpx;
  padding-bottom: 16rpx;
}

.btn-primary {
  width: 100%;
  height: 96rpx;
  border-radius: $solo-radius-full;
  background-color: $solo-primary-500;
  box-shadow: $solo-shadow-primary-sm;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  color: #fff;
}
.btn-primary-text {
  font-size: 28rpx;
  font-weight: 600;
  color: #fff;
}

.bottom-link {
  margin-top: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4rpx;
  padding: 8rpx;
}
.bottom-link-icon {
  color: $solo-neutral-500;
  flex-shrink: 0;
}
.bottom-link-text {
  font-size: 24rpx;
  color: $solo-neutral-500;
}
</style>
