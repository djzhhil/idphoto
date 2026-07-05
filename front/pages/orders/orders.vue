<template>
  <view class="container">
    <view class="header">
      <text class="app-title">订单列表</text>
      <text class="subtitle">查看历史订单</text>
    </view>

    <view v-if="orders.length === 0" class="empty-state">
      <text class="empty-text">暂无订单记录</text>
    </view>

    <view v-else class="orders-list">
      <view v-for="order in orders" :key="order.orderNo" class="order-card">
        <view class="order-header">
          <text class="order-no">{{ order.orderNo }}</text>
          <text :class="['order-status', 'status-' + order.status]">{{ statusText(order.status) }}</text>
        </view>
        <view class="order-body">
          <text class="order-amount">¥{{ (order.amountCent / 100).toFixed(2) }}</text>
          <text class="order-time">{{ formatTime(order.createdAt) }}</text>
        </view>
        <view class="order-actions">
          <button
            v-if="order.status === 'DONE'"
            class="btn-sm primary"
            @click="goToResult(order)"
          >
            查看证件照
          </button>
          <button
            v-if="order.status === 'UNPAID'"
            class="btn-sm warning"
            @click="goToPay(order)"
          >
            去支付
          </button>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import orderApi from "@/utils/orderApi.js";

export default {
  data() {
    return {
      orders: [],
      openid: ""
    };
  },
  onLoad() {
    this.openid = uni.getStorageSync("openid") || "guest";
    this.loadOrders();
  },
  methods: {
    async loadOrders() {
      try {
        const res = await orderApi.getMyOrders(this.openid);
        this.orders = res || [];
      } catch (err) {
        console.error("加载订单列表失败:", err);
        uni.showToast({ title: "加载失败", icon: "none" });
      }
    },

    statusText(status) {
      const map = {
        UNPAID: "待支付",
        PAID: "已支付",
        DONE: "已完成",
        FAILED: "失败",
        CLOSED: "已关闭"
      };
      return map[status] || status;
    },

    formatTime(instant) {
      if (!instant) return "";
      try {
        const d = new Date(instant);
        const pad = (n) => String(n).padStart(2, "0");
        return d.getFullYear() + "-" + pad(d.getMonth() + 1) + "-" + pad(d.getDate())
          + " " + pad(d.getHours()) + ":" + pad(d.getMinutes());
      } catch (e) {
        return instant;
      }
    },

    goToResult(order) {
      if (!order.downloadToken) {
        uni.showToast({ title: "订单暂不可下载", icon: "none" });
        return;
      }
      uni.navigateTo({
        url: "/pages/result/result?orderNo=" + order.orderNo + "&token=" + order.downloadToken
      });
    },

    goToPay(order) {
      uni.navigateTo({
        url: "/pages/pay/pay?orderNo=" + order.orderNo
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
.empty-state {
  margin-top: 60px;
  text-align: center;
}
.empty-text {
  font-size: 14px;
  color: #bbb;
}
.orders-list {
  width: 100%;
  max-width: 420px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.order-card {
  background-color: #fff;
  border-radius: 12px;
  padding: 14px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.06);
}
.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.order-no {
  font-size: 13px;
  color: #666;
  font-family: monospace;
}
.order-status {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 8px;
  font-weight: 500;
}
.status-UNPAID { background-color: #fff7e6; color: #fa8c16; }
.status-DONE { background-color: #f6ffed; color: #52c41a; }
.status-PAID { background-color: #e6f7ff; color: #1890ff; }
.status-FAILED { background-color: #fff2f0; color: #ff4d4f; }
.status-CLOSED { background-color: #f5f5f5; color: #999; }
.order-body {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}
.order-amount {
  font-size: 16px;
  font-weight: 600;
  color: #ff4d4f;
}
.order-time {
  font-size: 12px;
  color: #999;
}
.order-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}
.btn-sm {
  padding: 6px 16px;
  border-radius: 8px;
  font-size: 12px;
  border: none;
}
.btn-sm.primary {
  background-color: #1890ff;
  color: #fff;
}
.btn-sm.warning {
  background-color: #fa8c16;
  color: #fff;
}
</style>
