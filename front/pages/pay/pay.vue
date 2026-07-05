<template>
  <view class="container">
    <view class="header">
      <text class="app-title">订单支付</text>
      <text class="subtitle">确认订单信息并完成支付</text>
    </view>

    <view class="card order-card">
      <!-- 订单信息 -->
      <view class="info-section">
        <view class="info-row">
          <text class="info-label">订单编号</text>
          <text class="info-value">{{ orderNo }}</text>
        </view>
        <view class="info-row">
          <text class="info-label">支付金额</text>
          <text class="info-value amount">¥{{ amountYuan }}</text>
        </view>
        <view class="info-row">
          <text class="info-label">订单状态</text>
          <text :class="['info-value', 'status-' + status]">{{ statusText }}</text>
        </view>
      </view>

      <!-- 支付说明 -->
      <view v-if="status === 'UNPAID'" class="pay-instructions">
        <text class="section-title">支付说明</text>
        <view class="instructions-box">
          <text class="instruction-text">请通过以下方式完成支付：</text>
          <text class="instruction-text">1. 扫描下方收款码或转账至指定账户</text>
          <text class="instruction-text">2. 支付金额：¥{{ amountYuan }}</text>
          <text class="instruction-text">3. 备注订单号：{{ orderNo }}</text>
          <text class="instruction-text">4. 支付完成后点击下方"我已支付"按钮</text>
        </view>
      </view>

      <!-- 已完成提示 -->
      <view v-if="status === 'DONE'" class="done-section">
        <view class="done-badge">✓ 支付已完成</view>
        <button class="btn-primary" @click="goToResult">查看证件照</button>
      </view>

      <!-- 操作按钮 -->
      <view class="btn-area">
        <button v-if="status === 'UNPAID'" class="btn-primary" @click="checkStatus">
          我已支付，查询状态
        </button>

        <button class="btn-refresh" @click="refreshOrder">
          刷新订单状态
        </button>
      </view>
    </view>
  </view>
</template>

<script>
import orderApi from "@/utils/orderApi.js";

export default {
  data() {
    return {
      orderNo: "",
      status: "UNPAID",
      amountCent: 0,
      downloadToken: "",
      createdAt: null
    };
  },
  computed: {
    amountYuan() {
      return (this.amountCent / 100).toFixed(2);
    },
    statusText() {
      const map = {
        UNPAID: "待支付",
        PAID: "已支付",
        DONE: "已完成",
        FAILED: "失败",
        CLOSED: "已关闭"
      };
      return map[this.status] || this.status;
    }
  },
  onLoad(options) {
    if (options.orderNo) {
      this.orderNo = options.orderNo;
      this.loadOrder();
    } else {
      uni.showToast({ title: "缺少订单号", icon: "none" });
    }
  },
  methods: {
    async loadOrder() {
      try {
        const res = await orderApi.getOrder(this.orderNo);
        if (res) {
          this.status = res.status;
          this.amountCent = res.amountCent;
          this.downloadToken = res.downloadToken;
          this.createdAt = res.createdAt;
        }
      } catch (err) {
        console.error("加载订单失败:", err);
        uni.showToast({ title: "加载订单失败", icon: "none" });
      }
    },

    async refreshOrder() {
      uni.showLoading({ title: "刷新中..." });
      await this.loadOrder();
      uni.hideLoading();
      if (this.status === "DONE") {
        uni.showToast({ title: "订单已完成支付！", icon: "success" });
      }
    },

    async checkStatus() {
      uni.showLoading({ title: "查询中..." });
      await this.loadOrder();
      uni.hideLoading();
      if (this.status === "DONE") {
        uni.showToast({ title: "支付已完成！", icon: "success" });
      } else {
        uni.showToast({ title: "暂未收到支付，请确认后重试", icon: "none" });
      }
    },

    goToResult() {
      uni.redirectTo({
        url: "/pages/result/result?orderNo=" + this.orderNo + "&token=" + this.downloadToken
      });
    }
  }
};
</script>

<style scoped>
.container {
  background-color: #f6f8fb;
  min-height: 100vh;
  padding: 20px 16px 40px;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.header {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 20px;
}
.app-title {
  font-size: 20px;
  font-weight: 600;
  color: #1890ff;
}
.subtitle {
  font-size: 13px;
  color: #666;
  margin-top: 4px;
}
.order-card {
  width: 100%;
  max-width: 420px;
  background-color: #fff;
  border-radius: 16px;
  padding: 18px 16px;
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.08);
}
.info-section {
  margin-bottom: 18px;
}
.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
}
.info-label {
  font-size: 14px;
  color: #666;
}
.info-value {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}
.amount {
  color: #ff4d4f;
  font-size: 18px;
  font-weight: 600;
}
.status-UNPAID { color: #faad14; }
.status-DONE { color: #52c41a; }
.status-FAILED, .status-CLOSED { color: #999; }

.section-title {
  font-size: 15px;
  font-weight: 500;
  color: #333;
  margin-bottom: 10px;
  display: block;
}
.pay-instructions {
  margin-bottom: 20px;
}
.instructions-box {
  background-color: #fffbe6;
  border: 1px solid #ffe58f;
  border-radius: 10px;
  padding: 14px;
}
.instruction-text {
  font-size: 13px;
  color: #666;
  line-height: 1.8;
  display: block;
}
.done-section {
  text-align: center;
  margin-bottom: 20px;
}
.done-badge {
  display: inline-block;
  background-color: #f6ffed;
  border: 1px solid #b7eb8f;
  color: #52c41a;
  font-size: 16px;
  padding: 8px 24px;
  border-radius: 20px;
  margin-bottom: 16px;
}
.btn-area {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.btn-primary {
  width: 100%;
  padding: 14px 0;
  border-radius: 12px;
  background-color: #1890ff;
  color: #fff;
  font-size: 16px;
  letter-spacing: 1px;
  box-shadow: 0 3px 10px rgba(24, 144, 255, 0.3);
  border: none;
}
.btn-primary:active {
  opacity: 0.85;
}
.btn-secondary {
  width: 100%;
  padding: 12px 0;
  border-radius: 12px;
  background-color: #f0f0f0;
  color: #999;
  font-size: 13px;
  border: 1px dashed #d9d9d9;
}
.btn-refresh {
  width: 100%;
  padding: 12px 0;
  border-radius: 12px;
  background-color: #fafafa;
  color: #666;
  font-size: 13px;
  border: 1px solid #d9d9d9;
}
</style>
