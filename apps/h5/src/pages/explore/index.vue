<script setup lang="ts">
/**
 * Page 06: 探索首页（Explore Tab 入口）。
 *
 * 设计稿：designs/solo-life-os-mobile/pages/explore.html
 * 文档：designs/solo-life-os-mobile/docs/02-explore-module-review.md §Page 06
 *
 * 信息架构（按设计稿 1:1）：
 * - Header：标题"探索"+ 副标题"今晚适合去哪里"
 * - 分类筛选 pills 横向滚动（全部 / 咖啡 / 书店 / 公园 / 展览 / 运动 / 夜骑 / 电影）
 * - AI 今晚推荐 Hero 卡（渐变背景 + 装饰光晕 + 5 星评分 + 步行距离 + 特征标签）
 * - Section：为你推荐 3 张列表卡（方所书店 / 西湖夜骑 / 植物园）
 *
 * 当前为纯静态 mock 展示，MVP 阶段地图视图用卡片流替代（对齐 Explore 文档 §产品逻辑断裂点）。
 * 真实地图 SDK 接入推迟到 Sprint 5+。
 */
import { ref } from 'vue'
import {
  Sparkles,
  CloudMoon,
  Star,
  MapPin,
  Volume2,
  Laptop,
  User,
  BookOpen,
  Clock,
  Bike,
  Route,
  TreePine,
  Footprints,
} from 'lucide-vue-next'

/* ---------- 分类筛选 ---------- */

interface Category {
  key: string
  label: string
  icon?: typeof BookOpen
}

const categories: Category[] = [
  { key: 'all', label: '全部' },
  { key: 'cafe', label: '咖啡店', icon: Star /* placeholder, not lucide coffee */ },
  { key: 'bookstore', label: '书店', icon: BookOpen },
  { key: 'park', label: '公园', icon: TreePine },
  { key: 'exhibition', label: '展览', icon: Star },
  { key: 'sports', label: '运动', icon: Bike },
  { key: 'nightRide', label: '夜骑', icon: Bike },
  { key: 'movie', label: '电影', icon: Star },
]

const activeCategory = ref<string>('all')
function selectCategory(key: string) {
  activeCategory.value = key
}

/* ---------- 为你推荐列表 ---------- */

interface RecTag {
  label: string
  primary?: boolean
}

interface RecommendCard {
  title: string
  subtitle: string
  icon: typeof BookOpen
  rightLabel: string
  rightIcon: typeof Clock
  tags: RecTag[]
}

const recommendList: RecommendCard[] = [
  {
    title: '方所书店',
    subtitle: '今天营业到 22:00',
    icon: BookOpen,
    rightLabel: '18分钟',
    rightIcon: Clock,
    tags: [
      { label: '有新书上架' },
      { label: '适合发呆', primary: true },
    ],
  },
  {
    title: '西湖夜骑',
    subtitle: '今晚有风，体感舒适',
    icon: Bike,
    rightLabel: '8km 环线',
    rightIcon: Route,
    tags: [
      { label: '路灯充足' },
      { label: '微风 24°C', primary: true },
    ],
  },
  {
    title: '植物园',
    subtitle: '今晚有晚霞，适合散步',
    icon: TreePine,
    rightLabel: '25分钟',
    rightIcon: Footprints,
    tags: [
      { label: '人少安静' },
      { label: '日落绝佳', primary: true },
    ],
  },
]
</script>

<template>
  <view class="page">
    <!-- Header -->
    <view class="header">
      <text class="title">探索</text>
      <text class="subtitle">今晚适合去哪里</text>
    </view>

    <!-- Category Pills -->
    <scroll-view class="cats" scroll-x enable-flex show-scrollbar="false">
      <view
        v-for="c in categories"
        :key="c.key"
        class="cat-btn"
        :class="{ active: activeCategory === c.key }"
        @tap="selectCategory(c.key)"
      >
        <component v-if="c.icon && activeCategory !== c.key" :is="c.icon" class="cat-icon" />
        <text class="cat-label">{{ c.label }}</text>
      </view>
    </scroll-view>

    <view class="content">
      <!-- Hero Recommendation Card -->
      <view class="hero">
        <view class="hero-img">
          <view class="glow glow-tr" />
          <view class="glow glow-bl" />
          <view class="badge badge-ai">
            <Sparkles class="badge-icon" />
            <text>AI 今晚推荐</text>
          </view>
          <view class="badge badge-reason">
            <CloudMoon class="badge-icon" />
            <text>今晚适合去</text>
          </view>
        </view>
        <view class="hero-body">
          <view class="hero-head">
            <view class="hero-left">
              <text class="hero-title">隐山咖啡</text>
              <view class="hero-stars">
                <Star v-for="i in 5" :key="i" class="star" fill="currentColor" />
                <text class="hero-rating">4.9</text>
              </view>
            </view>
            <view class="hero-distance">
              <MapPin class="mini-icon" />
              <text>步行12分钟</text>
            </view>
          </view>
          <view class="hero-tags">
            <view class="hero-tag hero-tag-primary">
              <Volume2 class="tag-icon" />
              <text>安静</text>
            </view>
            <view class="hero-tag">
              <Laptop class="tag-icon" />
              <text>可办公</text>
            </view>
            <view class="hero-tag">
              <User class="tag-icon" />
              <text>一个人不尴尬</text>
            </view>
          </view>
          <text class="hero-desc">暖黄灯光、木质桌椅，适合下班后坐一会儿，点一杯手冲慢慢喝。</text>
        </view>
      </view>

      <!-- Section Header -->
      <view class="section-head">
        <text class="section-title">为你推荐</text>
        <text class="section-sub">基于此刻心情</text>
      </view>

      <!-- Recommend List (3 cards) -->
      <view
        v-for="(item, idx) in recommendList"
        :key="idx"
        class="rec-card"
      >
        <view class="rec-icon">
          <component :is="item.icon" class="rec-icon-inner" />
        </view>
        <view class="rec-main">
          <view class="rec-top">
            <text class="rec-title">{{ item.title }}</text>
            <view class="rec-right">
              <component :is="item.rightIcon" class="mini-icon" />
              <text>{{ item.rightLabel }}</text>
            </view>
          </view>
          <text class="rec-sub">{{ item.subtitle }}</text>
          <view class="rec-tags">
            <text
              v-for="(t, i) in item.tags"
              :key="i"
              class="rec-tag"
              :class="{ primary: t.primary }"
            >{{ t.label }}</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: $solo-background;
  padding-bottom: 120rpx; /* 预留 TabBar + 安全区 */
}

/* ---------- Header ---------- */
.header {
  padding: 12rpx 32rpx 8rpx;
}
.title {
  font-size: 56rpx;
  font-weight: 700;
  letter-spacing: -1rpx;
  line-height: 1.2;
  color: $solo-foreground;
}
.subtitle {
  display: block;
  margin-top: 8rpx;
  font-size: $solo-font-sm;
  color: $solo-muted-foreground;
}

/* ---------- Category Pills ---------- */
.cats {
  margin-top: 32rpx;
  white-space: nowrap;
  padding: 0 24rpx;
}
.cat-btn {
  display: inline-flex;
  align-items: center;
  gap: 6rpx;
  flex-shrink: 0;
  margin: 0 8rpx;
  padding: 12rpx 28rpx;
  border-radius: $solo-radius-md;
  background: $solo-card;
  border: 1rpx solid $solo-border;
  font-size: $solo-font-sm;
  color: $solo-muted-foreground;
  transition: all 0.2s;
  &.active {
    background: $solo-primary;
    border-color: $solo-primary;
    color: $solo-primary-foreground;
    font-weight: 500;
  }
}
.cat-icon {
  width: 28rpx;
  height: 28rpx;
}

/* ---------- Content ---------- */
.content {
  margin-top: 32rpx;
  padding: 0 32rpx;
  display: flex;
  flex-direction: column;
  gap: 32rpx;
}

/* ---------- Hero Card ---------- */
.hero {
  border-radius: $solo-radius-lg;
  border: 1rpx solid $solo-border;
  overflow: hidden;
  background: $solo-card;
  box-shadow: $solo-shadow-sm;
}
.hero-img {
  position: relative;
  height: 352rpx; /* 176 * 2 */
  background: linear-gradient(155deg,
    $solo-primary-100 0%,
    $solo-primary-300 30%,
    $solo-primary-500 65%,
    $solo-primary-800 100%);
}
.glow {
  position: absolute;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(255,247,237,0.45) 0%, transparent 65%);
  &.glow-tr {
    top: 40rpx; right: 48rpx;
    width: 192rpx; height: 192rpx;
  }
  &.glow-bl {
    bottom: 48rpx; left: 64rpx;
    width: 128rpx; height: 128rpx;
    background: radial-gradient(circle, rgba(255,247,237,0.3) 0%, transparent 65%);
  }
}
.badge {
  position: absolute;
  display: inline-flex;
  align-items: center;
  gap: 4rpx;
  padding: 8rpx 20rpx;
  border-radius: $solo-radius-full;
  font-size: 22rpx;
  font-weight: 500;
  color: #fff;
  backdrop-filter: blur(8rpx);
  &.badge-ai {
    top: 24rpx; left: 24rpx;
    background: rgba(255,255,255,0.25);
  }
  &.badge-reason {
    bottom: 24rpx; right: 24rpx;
    font-size: 22rpx;
    background: rgba(255,255,255,0.2);
    color: rgba(255,255,255,0.95);
  }
}
.badge-icon {
  width: 24rpx;
  height: 24rpx;
}
.hero-body {
  padding: 32rpx;
}
.hero-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 24rpx;
}
.hero-left { min-width: 0; flex: 1; }
.hero-title {
  display: block;
  font-size: 36rpx;
  font-weight: 600;
  color: $solo-foreground;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.hero-stars {
  display: flex;
  align-items: center;
  gap: 2rpx;
  margin-top: 8rpx;
}
.star {
  width: 28rpx;
  height: 28rpx;
  color: $solo-state-warning;
}
.hero-rating {
  margin-left: 8rpx;
  font-size: $solo-font-xs;
  color: $solo-muted-foreground;
}
.hero-distance {
  flex-shrink: 0;
  display: inline-flex;
  align-items: center;
  gap: 4rpx;
  padding-top: 2rpx;
  font-size: $solo-font-sm;
  color: $solo-muted-foreground;
}
.mini-icon {
  width: 28rpx;
  height: 28rpx;
  flex-shrink: 0;
}
.hero-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-top: 24rpx;
}
.hero-tag {
  display: inline-flex;
  align-items: center;
  gap: 4rpx;
  padding: 4rpx 16rpx;
  border-radius: $solo-radius-sm;
  background: $solo-muted;
  color: $solo-muted-foreground;
  font-size: $solo-font-xs;
  white-space: nowrap;
  &-primary {
    background: $solo-primary-50;
    color: $solo-primary-700;
  }
}
.tag-icon {
  width: 24rpx;
  height: 24rpx;
}
.hero-desc {
  display: block;
  margin-top: 24rpx;
  font-size: $solo-font-xs;
  line-height: 1.7;
  color: $solo-muted-foreground;
}

/* ---------- Section Header ---------- */
.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 4rpx;
}
.section-title {
  font-size: $solo-font-lg;
  font-weight: 600;
  color: $solo-foreground;
}
.section-sub {
  font-size: $solo-font-xs;
  color: $solo-muted-foreground;
}

/* ---------- Recommend List Cards ---------- */
.rec-card {
  display: flex;
  gap: 24rpx;
  padding: 32rpx;
  border-radius: $solo-radius-lg;
  background: $solo-card;
  border: 1rpx solid $solo-border;
  transition: transform 0.15s;
  &:active { transform: scale(0.99); }
}
.rec-icon {
  flex-shrink: 0;
  width: 96rpx;
  height: 96rpx;
  border-radius: $solo-radius-md;
  background: $solo-primary-50;
  color: $solo-primary;
  display: flex;
  align-items: center;
  justify-content: center;
}
.rec-icon-inner {
  width: 40rpx;
  height: 40rpx;
}
.rec-main {
  flex: 1;
  min-width: 0;
}
.rec-top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16rpx;
}
.rec-title {
  font-size: $solo-font-base;
  font-weight: 600;
  color: $solo-foreground;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.rec-right {
  flex-shrink: 0;
  display: inline-flex;
  align-items: center;
  gap: 2rpx;
  padding-top: 2rpx;
  font-size: $solo-font-xs;
  color: $solo-muted-foreground;
  white-space: nowrap;
  .mini-icon { width: 24rpx; height: 24rpx; }
}
.rec-sub {
  display: block;
  margin-top: 4rpx;
  font-size: $solo-font-xs;
  color: $solo-muted-foreground;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.rec-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-top: 16rpx;
}
.rec-tag {
  padding: 2rpx 16rpx;
  border-radius: $solo-radius-sm;
  background: $solo-muted;
  color: $solo-muted-foreground;
  font-size: 22rpx;
  white-space: nowrap;
  &.primary {
    background: $solo-primary-50;
    color: $solo-primary-700;
  }
}
</style>
