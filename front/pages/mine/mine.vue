<template>
  <view class="container">
    <!-- 顶部标题 -->
    <view class="header">
      <text class="app-title">我的</text>
      <text class="subtitle">个人中心</text>
    </view>

    <!-- 功能卡片区域 -->
    <view class="cards-container">
      <!-- 历史记录卡片 -->
      <view class="feature-card" @click="viewHistory">
        <view class="card-icon">
          <text class="icon-text">📋</text>
        </view>
        <view class="card-content">
          <text class="card-title">历史记录</text>
          <text class="card-desc">查看您的证件照制作历史</text>
        </view>
        <view class="card-arrow">›</view>
      </view>
    </view>

    <!-- 底部提示 -->
    <view class="footer-tips">
      <text class="tip-text">💡 暂无历史记录</text>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {}
  },
  methods: {
    viewHistory() {
      uni.navigateTo({
        url: '/pages/orders/orders'
      });
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
