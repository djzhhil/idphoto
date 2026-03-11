<template>
  <view class="container">
    <view class="header">
      <text class="app-title">底色通 · 智能证件照</text>
      <text class="subtitle">调整亮度与磨皮，预览效果</text>
    </view>

    <view class="card edit-card">
      <!-- 预览区域 -->
      <view class="preview-wrapper">
        <image
          v-if="serverPreviewUrl"
          :src="serverPreviewUrl"
          class="photo-box"
          mode="widthFix"
          @load="onServerPreviewLoad"
          @error="onServerPreviewError"
        />
        <view v-else class="preview-placeholder">
          <text class="placeholder-text">预览加载中...</text>
        </view>
        <view v-if="isProcessing" class="processing-overlay">
          <text>处理中...</text>
        </view>
      </view>

      <!-- 预览状态指示器 -->
      <view class="preview-status">
        <text class="status-text" v-if="isProcessing">生成预览中...</text>
        <text class="status-text" v-else-if="!serverPreviewUrl">准备生成预览</text>
        <text class="status-text" v-else>预览已更新</text>
      </view>

      <!-- 背景色选择 -->
      <view class="section">
        <text class="section-title">背景颜色</text>
        <view class="color-options">
          <view
            v-for="color in bgColors"
            :key="color.value"
            :class="['color-btn', bgColor === color.value ? 'active' : '']"
            @click="selectBgColor(color.value)"
          >
            <view class="color-preview" :style="{ backgroundColor: color.hex }"></view>
            <text class="color-label">{{ color.label }}</text>
          </view>
        </view>
      </view>

      <!-- 尺寸选择 -->
      <view class="section">
        <text class="section-title">选择尺寸</text>
        <view class="size-options">
          <view
            v-for="size in sizes"
            :key="size.value"
            :class="['size-btn', selectedSize === size.value ? 'active' : '']"
            @click="selectSize(size)"
          >
            {{ size.label }}
            <text class="size-dimension">{{ size.width }}×{{ size.height }}</text>
          </view>
        </view>
      </view>

      <!-- 亮度调整 -->
      <view class="section">
        <view class="label-row">
          <text class="section-title">亮度调整</text>
          <text class="slider-value">{{ brightness }}%</text>
        </view>
        <slider
          min="0"
          max="100"
          step="1"
          :value="brightness"
          activeColor="#1890ff"
          @changing="onBrightnessChanging"
          @change="onBrightnessChange"
        />
      </view>

      <!-- 磨皮调整 -->
      <view class="section">
        <view class="label-row">
          <text class="section-title">磨皮程度</text>
          <text class="slider-value">{{ smoothness }}</text>
        </view>
        <slider
          min="0"
          max="20"
          step="1"
          :value="smoothness"
          activeColor="#1890ff"
          @changing="onSmoothChanging"
          @change="onSmoothChange"
        />
      </view>

      <!-- 底部按钮 -->
      <view class="btn-area">
        <button class="btn-primary" @click="generateFinalImage" :disabled="isProcessing">
          {{ isProcessing ? '生成中...' : '生成证件照' }}
        </button>
      </view>
    </view>
  </view>
</template>

<script>
import request from "@/utils/request.js";

export default {
  data() {
    return {
      imageUrl: "",
      serverPreviewUrl: "",
      isProcessing: false,
      // 背景颜色选项
      bgColors: [
        { label: "白色", value: "white", hex: "#ffffff" },
        { label: "蓝色", value: "blue", hex: "#0066cc" },
        { label: "红色", value: "red", hex: "#dc143c" },
        { label: "灰色", value: "gray", hex: "#808080" }
      ],
      sizes: [
        { label: "一寸", value: "1inch", width: 295, height: 413, aspectRatio: 413/295 },
        { label: "二寸", value: "2inch", width: 413, height: 579, aspectRatio: 579/413 },
        { label: "小一寸", value: "small1inch", width: 260, height: 378, aspectRatio: 378/260 }
      ],
      selectedSize: "1inch",
      brightness: 50,
      smoothness: 0,
      bgColor: "white",
      debounceTimer: null,
      lastRequestKey: ""
    };
  },
  onLoad(options) {
    console.log('页面加载，参数:', options);
    
    if (options.image) {
      this.imageUrl = decodeURIComponent(options.image);
      this.serverPreviewUrl = this.imageUrl; // 初始显示原图
      console.log('图片URL已设置');
    } else {
      console.error('没有接收到图片参数');
      uni.showToast({ 
        title: "未接收到图片，请重新上传", 
        icon: "none",
        duration: 3000
      });
      setTimeout(() => {
        uni.navigateBack();
      }, 2000);
      return;
    }
    
    if (options.bgColor) {
      this.bgColor = decodeURIComponent(options.bgColor);
    }
    
    // 页面加载后立即请求一次预览
    setTimeout(() => {
      this.requestServerPreview();
    }, 500);
  },
  methods: {
    selectSize(size) {
      this.selectedSize = size.value;
      this.debouncePreview();
    },

    selectBgColor(color) {
      this.bgColor = color;
      this.debouncePreview();
    },

    onBrightnessChanging(e) {
      this.brightness = e.detail.value;
      // 实时调整时不立即请求，只在停止时请求
    },

    onSmoothChanging(e) {
      this.smoothness = e.detail.value;
      // 实时调整时不立即请求，只在停止时请求
    },

    onBrightnessChange(e) {
      this.brightness = e.detail.value;
      this.debouncePreview();
    },

    onSmoothChange(e) {
      this.smoothness = e.detail.value;
      this.debouncePreview();
    },

    debouncePreview() {
      clearTimeout(this.debounceTimer);
      this.debounceTimer = setTimeout(() => {
        this.requestServerPreview();
      }, 800);
    },

    generateRequestKey() {
      return `${this.selectedSize}_${this.brightness}_${this.smoothness}_${this.bgColor}`;
    },

    async requestServerPreview() {
      if (!this.imageUrl) return;
      
      const currentRequestKey = this.generateRequestKey();
      if (currentRequestKey === this.lastRequestKey) {
        return;
      }
      
      this.lastRequestKey = currentRequestKey;
      this.isProcessing = true;
      
      try {
        console.log('请求预览...');
        const res = await request.post("/api/idphoto/preview-idphoto", {
          image: this.imageUrl,
          size: this.selectedSize,
          brightness: this.brightness,
          smoothness: this.smoothness,
          bgColor: this.bgColor,
          previewMode: true,
          quality: "standard"
        });
        
        if (res && res.imageBase64) {
          this.serverPreviewUrl = res.imageBase64;
          console.log('预览加载成功');
        } else if (res && res.resultImageBase64) {
          this.serverPreviewUrl = res.resultImageBase64;
          console.log('预览加载成功(兼容字段)');
        } else {
          console.warn("预览生成失败", res);
          uni.showToast({ 
            title: res?.message || "预览生成失败", 
            icon: "none",
            duration: 2000
          });
        }
      } catch (err) {
        console.error("预览请求失败", err);
        uni.showToast({ 
          title: "预览请求失败", 
          icon: "none",
          duration: 2000
        });
      } finally {
        this.isProcessing = false;
      }
    },

    onServerPreviewLoad() {
      console.log('预览图片加载成功');
    },

    onServerPreviewError(err) {
      console.error('预览图片加载失败:', err);
      uni.showToast({ 
        title: "预览图片加载失败", 
        icon: "none",
        duration: 2000
      });
    },

    async generateFinalImage() {
      if (!this.imageUrl) {
        uni.showToast({ title: "请先上传照片", icon: "none" });
        return;
      }
      
      this.isProcessing = true;
      uni.showLoading({ title: '生成中...', mask: true });
      
      try {
        console.log('生成最终图片...');
        const res = await request.post("/api/idphoto/generate-idphoto", {
          image: this.imageUrl,
          size: this.selectedSize,
          brightness: this.brightness,
          smoothness: this.smoothness,
          bgColor: this.bgColor,
          quality: "high"
        });
        
        let img = null;
        if (res && res.imageBase64) {
          img = res.imageBase64;
        } else if (res && res.resultImageBase64) {
          img = res.resultImageBase64;
        }
        
        if (img) {
          uni.hideLoading();
          // 使用 redirectTo 跳转到结果页面，关闭当前编辑页面
          uni.redirectTo({
            url: "/pages/result/result?image=" + encodeURIComponent(img),
          });
        } else {
          uni.hideLoading();
          uni.showToast({ 
            title: res?.message || "生成失败，请重试", 
            icon: "none",
            duration: 3000
          });
        }
      } catch (err) {
        uni.hideLoading();
        console.error("生成请求失败", err);
        uni.showToast({ 
          title: "生成失败，请检查网络后重试", 
          icon: "none",
          duration: 3000
        });
      } finally {
        this.isProcessing = false;
      }
    }
  },
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
  color: #777;
  margin-top: 4px;
}
.edit-card {
  width: 100%;
  max-width: 420px;
  background-color: #fff;
  border-radius: 16px;
  padding: 18px 16px;
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.08);
  display: flex;
  flex-direction: column;
}
.preview-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  position: relative;
  min-height: 200px;
}
.photo-box {
  width: 100%;
  border: 2px dashed #cfd8dc;
  border-radius: 16px;
  background-color: #fafafa;
  margin-bottom: 20px;
  transition: all 0.3s;
}
.preview-placeholder {
  width: 100%;
  height: 200px;
  border: 2px dashed #cfd8dc;
  border-radius: 16px;
  background-color: #fafafa;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 20px;
}
.placeholder-text {
  color: #999;
  font-size: 14px;
}
.processing-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  border-radius: 16px;
  color: white;
  font-size: 14px;
}
.preview-status {
  text-align: center;
  margin-bottom: 16px;
}
.status-text {
  font-size: 12px;
  color: #666;
  padding: 4px 12px;
  background-color: #f0f2f5;
  border-radius: 12px;
  display: inline-block;
}
.section {
  margin-bottom: 18px;
}
.section-title {
  font-size: 14px;
  color: #333;
  font-weight: 500;
  margin-bottom: 8px;
  display: block;
}
.label-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}
.slider-value {
  font-size: 13px;
  color: #1890ff;
  font-weight: 500;
}
.color-options {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  margin-top: 8px;
}
.color-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 8px;
  border-radius: 12px;
  background-color: #f8f9fa;
  transition: all 0.2s;
  min-width: 60px;
}
.color-btn.active {
  background-color: #1890ff;
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.3);
}
.color-preview {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  border: 2px solid #e0e0e0;
  margin-bottom: 4px;
}
.color-btn.active .color-preview {
  border-color: #fff;
}
.color-label {
  font-size: 11px;
  color: #666;
}
.color-btn.active .color-label {
  color: #fff;
}
.size-options {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin-top: 8px;
}
.size-btn {
  padding: 8px 12px;
  border-radius: 12px;
  background-color: #f0f2f5;
  color: #333;
  font-size: 13px;
  transition: all 0.2s;
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 80px;
  flex: 1;
}
.size-btn.active {
  background-color: #1890ff;
  color: #fff;
  font-weight: 500;
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.3);
}
.size-dimension {
  font-size: 11px;
  color: #666;
  margin-top: 2px;
}
.size-btn.active .size-dimension {
  color: rgba(255, 255, 255, 0.8);
}
.btn-area {
  margin-top: 12px;
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
}
.btn-primary:active {
  opacity: 0.9;
}
.btn-primary[disabled] {
  background-color: #ccc;
  box-shadow: none;
  color: #999;
}
</style>