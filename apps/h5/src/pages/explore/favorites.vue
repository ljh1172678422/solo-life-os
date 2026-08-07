<script setup lang="ts">
/**
 * Page 09: 收藏夹。
 *
 * 设计稿：designs/solo-life-os-mobile/pages/favorites.html
 * 文档：designs/solo-life-os-mobile/docs/02-explore-module-review.md §Page 09
 *
 * 信息架构：
 * - Sticky Header：返回 + "我的收藏" 居中 + 搜索/编辑
 * - 分类 Tab 横向带下划线（全部/地点/路线/活动/电影）
 * - 2 列卡片网格：4 张收藏卡 + 创建收藏夹入口（虚线边框，跨 2 列）
 * - 每张卡：渐变色封面 + Emoji 图标 + 书签角标 + 分类/时间/去过次数
 *
 * 收藏数据与 Today 活动详情、Explore 地点详情共用同一个数据池（后端 favorite 表）。
 */
import { ref } from 'vue'
import {
  ChevronLeft,
  Search,
  SlidersHorizontal,
  BookmarkCheck,
  Plus,
} from 'lucide-vue-next'

/* ---------- Tab 切换 ---------- */
interface TabItem {
  key: 'all' | 'place' | 'route' | 'activity' | 'movie'
  label: string
}

const tabs: TabItem[] = [
  { key: 'all', label: '全部' },
  { key: 'place', label: '地点' },
  { key: 'route', label: '路线' },
  { key: 'activity', label: '活动' },
  { key: 'movie', label: '电影' },
]

const activeTab = ref<string>('all')
function switchTab(key: string) {
  activeTab.value = key
}

/* ---------- 收藏卡片数据 ---------- */
interface FavCard {
  title: string
  category: string
  timeLabel: string
  bottomLabel?: string
  badge?: string
  badgeBg?: string
  emoji: string
  gradientFrom: string
  gradientVia: string
  gradientTo: string
}

const favList: FavCard[] = [
  {
    title: '隐山咖啡',
    category: '咖啡',
    timeLabel: '3月收藏',
    bottomLabel: '去过 1 次',
    emoji: '☕',
    gradientFrom: '#FDBA74',
    gradientVia: '#F97316',
    gradientTo: '#C2410C',
  },
  {
    title: '西湖黄昏漫步',
    category: 'City Walk 路线',
    timeLabel: '上周收藏',
    badge: '7km',
    badgeBg: 'rgba(0,0,0,0.25)',
    emoji: '🚶',
    gradientFrom: '#FED7AA',
    gradientVia: '#FB923C',
    gradientTo: '#EA580C',
  },
  {
    title: '植物园',
    category: '公园',
    timeLabel: '4月收藏',
    emoji: '🌿',
    gradientFrom: '#FFEDD5',
    gradientVia: '#FDBA74',
    gradientTo: '#F97316',
  },
  {
    title: '《机器人之梦》',
    category: '电影',
    timeLabel: '待看清单',
    badge: '待看',
    badgeBg: 'rgba(249, 115, 22, 0.8)',
    emoji: '🎬',
    gradientFrom: '#FFEDD5',
    gradientVia: '#F59E0B',
    gradientTo: '#9A3412',
  },
]

/* ---------- 导航 ---------- */
function goBack(): void {
  uni.navigateBack({
    fail: () => uni.reLaunch({ url: '/pages/explore/index' }),
  })
}
function createFolder(): void {
  uni.showToast({ title: '功能即将上线', icon: 'none' })
}
</script>

<template>
  <view class="page">
    <!-- Sticky Header -->
    <view class="header">
      <view class="header-inner">
        <view class="btn-icon" @tap="goBack">
          <ChevronLeft class="hdr-icon" />
        </view>
        <text class="hdr-title">我的收藏</text>
        <view class="hdr-actions">
          <view class="btn-icon">
            <Search class="hdr-icon sm" />
          </view>
          <view class="btn-icon">
            <SlidersHorizontal class="hdr-icon sm" />
          </view>
        </view>
      </view>

      <!-- Tabs -->
      <scroll-view class="tabs" scroll-x enable-flex show-scrollbar="false">
        <view
          v-for="t in tabs"
          :key="t.key"
          class="tab"
          :class="{ active: activeTab === t.key }"
          @tap="switchTab(t.key)"
        >
          {{ t.label }}
        </view>
      </scroll-view>
    </view>

    <!-- Grid -->
    <view class="grid">
      <view
        v-for="(item, i) in favList"
        :key="i"
        class="fav-card"
      >
        <view
          class="cover"
          :style="{ background: `linear-gradient(135deg, ${item.gradientFrom} 0%, ${item.gradientVia} 50%, ${item.gradientTo} 100%)` }"
        >
          <text class="cover-emoji">{{ item.emoji }}</text>
          <view class="bookmark">
            <BookmarkCheck class="bookmark-icon" fill="currentColor" />
          </view>
          <view
            v-if="item.badge"
            class="cover-badge"
            :style="item.badgeBg ? { background: item.badgeBg } : {}"
          >
            {{ item.badge }}
          </view>
        </view>
        <view class="card-body">
          <text class="card-title">{{ item.title }}</text>
          <view class="card-meta">
            <text>{{ item.category }}</text>
            <text class="dot">·</text>
            <text>{{ item.timeLabel }}</text>
          </view>
          <text v-if="item.bottomLabel" class="card-bottom">{{ item.bottomLabel }}</text>
        </view>
      </view>

      <!-- 创建收藏夹入口 -->
      <view class="create-cta" @tap="createFolder">
        <view class="create-icon">
          <Plus class="plus-icon" />
        </view>
        <text class="create-title">创建收藏夹</text>
        <text class="create-sub">将喜欢的内容归类整理</text>
      </view>
    </view>
  </view>
</template>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: $solo-background;
}

/* ---------- Sticky Header ---------- */
.header {
  position: sticky;
  top: 0;
  z-index: 50;
  background: $solo-background;
}
.header-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 96rpx;
  padding: 0 32rpx;
  position: relative;
}
.btn-icon {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-left: -16rpx;
  &:active { background: rgba(231, 229, 228, 0.7); }
}
.hdr-icon {
  width: 48rpx;
  height: 48rpx;
  color: $solo-neutral-800;
  &.sm {
    width: 40rpx;
    height: 40rpx;
    color: $solo-neutral-700;
  }
}
.hdr-title {
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
  font-size: $solo-font-base;
  font-weight: 600;
  color: $solo-neutral-900;
}
.hdr-actions {
  display: flex;
  align-items: center;
  gap: 4rpx;
  .btn-icon { margin-left: 0; margin-right: -16rpx; }
}

/* ---------- Tabs ---------- */
.tabs {
  display: flex;
  padding: 0 32rpx;
  border-bottom: 1rpx solid rgba(231, 229, 228, 0.7);
  white-space: nowrap;
}
.tab {
  flex-shrink: 0;
  padding: 0 8rpx 16rpx;
  margin-right: 40rpx;
  font-size: $solo-font-sm;
  color: $solo-muted-foreground;
  border-bottom: 4rpx solid transparent;
  margin-bottom: -1rpx;
  transition: all 0.2s;
  &:last-child { margin-right: 0; }
  &.active {
    color: $solo-primary;
    border-bottom-color: $solo-primary;
    font-weight: 500;
  }
}

/* ---------- Grid ---------- */
.grid {
  display: flex;
  flex-wrap: wrap;
  gap: 24rpx;
  padding: 32rpx;
}
.fav-card {
  width: calc((100% - 24rpx) / 2);
  background: $solo-card;
  border: 1rpx solid rgba(231, 229, 228, 0.8);
  border-radius: $solo-radius-2xl;
  overflow: hidden;
  box-shadow: $solo-shadow-sm;
  &:active { transform: scale(0.98); }
}
.cover {
  position: relative;
  aspect-ratio: 4 / 3;
  display: flex;
  align-items: center;
  justify-content: center;
}
.cover-emoji {
  font-size: 80rpx;
  filter: drop-shadow(0 2rpx 4rpx rgba(0,0,0,0.1));
}
.bookmark {
  position: absolute;
  top: 16rpx;
  right: 16rpx;
  width: 56rpx;
  height: 56rpx;
  border-radius: 50%;
  background: rgba(255,255,255,0.25);
  backdrop-filter: blur(8rpx);
  display: flex;
  align-items: center;
  justify-content: center;
}
.bookmark-icon {
  width: 32rpx;
  height: 32rpx;
  color: #fff;
}
.cover-badge {
  position: absolute;
  left: 16rpx;
  bottom: 16rpx;
  padding: 4rpx 12rpx;
  border-radius: $solo-radius-sm;
  font-size: 20rpx;
  font-weight: 500;
  color: #fff;
}
.card-body {
  padding: 20rpx;
}
.card-title {
  display: block;
  font-size: $solo-font-sm;
  font-weight: 500;
  color: $solo-neutral-900;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.card-meta {
  display: flex;
  align-items: center;
  gap: 8rpx;
  margin-top: 4rpx;
  font-size: 22rpx;
  color: $solo-muted-foreground;
  flex-wrap: nowrap;
}
.dot {
  color: $solo-neutral-300;
}
.card-bottom {
  display: block;
  margin-top: 4rpx;
  font-size: 22rpx;
  color: $solo-muted-foreground;
}

/* ---------- Create CTA ---------- */
.create-cta {
  width: 100%;
  margin-top: 4rpx;
  border: 2rpx dashed $solo-neutral-300;
  border-radius: $solo-radius-2xl;
  padding: 40rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  background: rgba(255,255,255,0.4);
  color: $solo-muted-foreground;
  &:active { background: rgba(245, 245, 244, 0.6); }
}
.create-icon {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  background: $solo-neutral-100;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 8rpx;
}
.plus-icon {
  width: 40rpx;
  height: 40rpx;
  color: $solo-neutral-500;
}
.create-title {
  font-size: $solo-font-sm;
  font-weight: 500;
  color: $solo-muted-foreground;
}
.create-sub {
  font-size: 22rpx;
  color: $solo-neutral-400;
  margin-top: -4rpx;
}
</style>
