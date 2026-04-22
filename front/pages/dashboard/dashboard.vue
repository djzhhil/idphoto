<template>
  <view class="container">
    <!-- 顶部标题 -->
    <view class="header">
      <text class="app-title">底色通</text>
      <text class="subtitle">智能图像处理工具集</text>
    </view>

    <!-- 功能卡片区域 -->
    <view class="cards-container">
      <!-- 卡片1：证件照换底 -->
      <view class="feature-card" @click="navigateToUpload">
        <view class="card-icon">
          <text class="icon-text">📷</text>
        </view>
        <view class="card-content">
          <text class="card-title">证件照换底</text>
          <text class="card-desc">一键更换证件照底色，支持白/蓝/红/灰</text>
        </view>
        <view class="card-arrow">›</view>
      </view>

      <!-- 卡片2：图片尺寸裁剪 -->
      <view class="feature-card coming-soon" @click="showComingSoon">
        <view class="card-icon">
          <text class="icon-text">✂️</text>
        </view>
        <view class="card-content">
          <text class="card-title">图片尺寸裁剪</text>
          <text class="card-desc">智能裁剪为标准证件照尺寸</text>
        </view>
        <view class="card-badge">开发中</view>
      </view>

      <!-- 卡片3：更多 AI 效率工具 -->
      <view class="feature-card coming-soon" @click="showComingSoon">
        <view class="card-icon">
          <text class="icon-text">🤖</text>
        </view>
        <view class="card-content">
          <text class="card-title">更多 AI 效率工具</text>
          <text class="card-desc">智能抠图、背景虚化等功能</text>
        </view>
        <view class="card-badge">敬请期待</view>
      </view>
    </view>

    <!-- 底部提示 -->
    <view class="footer-tips">
      <text class="tip-text">💡 点击上方卡片开始使用功能</text>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {}
  },
  methods: {
    navigateToUpload() {
      uni.navigateTo({
        url: '/pages/upload/upload'
      })
    },
    showComingSoon() {
      uni.showToast({
        title: '功能开发中，敬请期待',
        icon: 'none',
        duration: 2000
      })
    }
  }
}
</script>

<style scoped>
.container {
  background-color: #f6f8fb;
  min-height: 100vh;
  padding: 20px 16px 40px;
  display: flex;
  flex-direction: column;
}

/* 顶部标题 */
.header {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 30px;
  padding-top: 10px;
}
.app-title {
  font-size: 24px;
  font-weight: 600;
  color: #1890ff;
}
.subtitle {
  font-size: 14px;
  color: #777;
  margin-top: 6px;
}

/* 卡片容器 */
.cards-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
  width: 100%;
  max-width: 420px;
  margin: 0 auto;
}

/* 功能卡片 */
.feature-card {
  background-color: #fff;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.08);
  display: flex;
  align-items: center;
  transition: all 0.3s;
  position: relative;
  overflow: hidden;
}
.feature-card:active {
  transform: translateY(2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
}
.feature-card.coming-soon {
  opacity: 0.7;
}

/* 卡片图标 */
.card-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
  display: flex;
  justify-content: center;
  align-items: center;
  margin-right: 16px;
  flex-shrink: 0;
}
.icon-text {
  font-size: 28px;
}

/* 卡片内容 */
.card-content {
  flex: 1;
  display: flex;
  flex-direction: column;
}
.card-title {
  font-size: 17px;
  font-weight: 600;
  color: #333;
  margin-bottom: 6px;
}
.card-desc {
  font-size: 13px;
  color: #999;
  line-height: 1.4;
}

/* 卡片箭头 */
.card-arrow {
  font-size: 24px;
  color: #d9d9d9;
  margin-left: 8px;
}

/* 开发中标签 */
.card-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  padding: 4px 10px;
  background-color: #f0f0f0;
  color: #999;
  font-size: 11px;
  border-radius: 12px;
}

/* 底部提示 */
.footer-tips {
  margin-top: 40px;
  text-align: center;
}
.tip-text {
  font-size: 13px;
  color: #bbb;
}
</style>
