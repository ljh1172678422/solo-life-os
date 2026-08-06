<script setup lang="ts">
import { onLaunch } from '@dcloudio/uni-app'

onLaunch(() => {
  console.log('Solo Life OS launched')
})
</script>

<template>
  <view>
    <!-- uni-app 自动注入页面渲染，无需 router-view -->
  </view>
</template>

<style lang="scss">
/* Global styles — aligned to designs/solo-life-os-mobile/colors_and_type.css */

page {
  background-color: $solo-background;
  color: $solo-foreground;
  font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Display', 'PingFang SC',
    'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
  font-size: $solo-font-base;
  line-height: 1.5;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

/* Hide horizontal scrollbars on horizontal scroll containers (横滑卡片) */
.no-scrollbar {
  -ms-overflow-style: none;
  scrollbar-width: none;
}
.no-scrollbar::-webkit-scrollbar {
  display: none;
}

/* Global button reset — uni-app button adds default borders/outlines */
button {
  margin: 0;
  padding: 0;
  background-color: transparent;
  border: none;
  outline: none;
  line-height: inherit;
  font-size: inherit;
  font-family: inherit;
  color: inherit;
}
button::after {
  border: none;
}

view, text {
  box-sizing: border-box;
}
</style>
