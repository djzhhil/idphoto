<template>
  <view class="container">
    <!-- 顶部标题 -->
    <view class="header">
      <text class="app-title">底色通 · 智能证件照</text>
      <text class="subtitle">一步上传，快速生成证件照</text>
    </view>

    <!-- 上传卡片 -->
    <view class="card upload-card">
      <!-- 上传照片区域 -->
      <view class="photo-box" :style="{ height: photoHeight + 'px' }" @click="chooseImage">
        <image v-if="imageUrl" :src="imageUrl" class="preview" mode="aspectFit" @load="onImageLoad" @error="onImageError"/>
        <view v-else class="placeholder">
          <view class="upload-icon">+</view>
          <text class="upload-text">点击上传照片</text>
          <text class="upload-tip">支持 JPG、PNG 格式</text>
        </view>
      </view>

      <!-- 去编辑按钮 -->
      <button class="btn-primary" :disabled="!imageUrl || isProcessing" @click="goToEditPage">
        {{ isProcessing ? '处理中...' : '开始编辑' }}
      </button>

      <!-- 使用提示 -->
      <view class="tips-section">
        <text class="tips-title">使用提示：</text>
        <view class="tip-item">• 请上传正面免冠照片</view>
        <view class="tip-item">• 建议使用光线均匀的照片</view>
        <view class="tip-item">• 支持一键更换多种底色</view>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      imageUrl: '',
      bgColor: 'white',
      photoHeight: 280,
      isProcessing: false
    }
  },
  onLoad() {
    // 页面加载时初始化
    this.calculatePhotoHeight();
  },
  onShow() {
    // 页面显示时重新计算高度，确保布局正确
    this.calculatePhotoHeight();
  },
  methods: {
    calculatePhotoHeight() {
      // 根据屏幕宽度计算合适的高度
      const systemInfo = uni.getSystemInfoSync();
      const cardWidth = Math.min(systemInfo.windowWidth - 64, 400); // 与编辑页面保持一致
      this.photoHeight = cardWidth * 0.75; // 4:3 比例
    },

    async chooseImage() {
      try {
        // 显示加载状态
        uni.showLoading({ title: '加载中...', mask: true });
        
        const res = await new Promise((resolve, reject) => {
          uni.chooseImage({
            count: 1,
            sourceType: ['album', 'camera'],
            sizeType: ['compressed'], // 使用压缩图片减少内存占用
            success: resolve,
            fail: reject
          });
        });

        const tempFilePath = res.tempFilePaths[0];
        
        // 验证图片尺寸
        const imageInfo = await new Promise((resolve, reject) => {
          uni.getImageInfo({
            src: tempFilePath,
            success: resolve,
            fail: reject
          });
        });

        // 检查图片最小尺寸
        if (imageInfo.width < 200 || imageInfo.height < 200) {
          uni.hideLoading();
          uni.showToast({ 
            title: '图片尺寸过小，请选择更大尺寸的照片', 
            icon: 'none',
            duration: 3000
          });
          return;
        }

        // 将临时路径转换成 Base64
        let base64Data = ''
        
        // #ifdef MP-WEIXIN
        base64Data = await new Promise((resolve, reject) => {
          uni.getFileSystemManager().readFile({
            filePath: tempFilePath,
            encoding: 'base64',
            success: (res) => resolve(res.data),
            fail: (err) => reject(err)
          })
        })
        this.imageUrl = "data:image/jpeg;base64," + base64Data
        // #endif
        
        // #ifdef H5
        base64Data = await new Promise((resolve, reject) => {
          fetch(tempFilePath)
            .then(res => res.blob())
            .then(blob => {
              const reader = new FileReader()
              reader.onloadend = () => resolve(reader.result.split(',')[1])
              reader.onerror = reject
              reader.readAsDataURL(blob)
            })
            .catch(reject)
        })
        
        this.imageUrl = "data:image/jpeg;base64," + base64Data
        // #endif
        
        // 显示成功提示
        uni.showToast({ 
          title: '上传成功', 
          icon: 'success',
          duration: 1500
        });

      } catch (error) {
        uni.hideLoading();
        console.error('图片选择失败:', error);
        
        let errorMsg = '图片选择失败';
        if (error.errMsg && error.errMsg.includes('cancel')) {
          errorMsg = '已取消选择';
        }
        
        uni.showToast({ 
          title: errorMsg, 
          icon: 'none',
          duration: 2000
        });
      }
    },

    onImageLoad(e) {
      // 根据图片实际比例调整显示高度
      const { width, height } = e.detail;
      const ratio = height / width;
      const maxWidth = Math.min(uni.getSystemInfoSync().windowWidth - 64, 400);
      
      // 限制最大高度，保持合理比例
      this.photoHeight = Math.min(maxWidth * ratio, 500);
    },

    onImageError(err) {
      console.error('图片加载失败:', err);
      uni.showToast({ 
        title: '图片加载失败，请重新上传', 
        icon: 'none',
        duration: 2000
      });
      this.imageUrl = '';
    },

    async goToEditPage() {
      if (!this.imageUrl) {
        uni.showToast({ title: "请先上传照片", icon: "none" });
        return;
      }

      this.isProcessing = true;
      
      try {
        // 添加一个短暂的延迟，让用户看到处理状态
        await new Promise(resolve => setTimeout(resolve, 500));
        
        uni.navigateTo({
          url: `/pages/edit/edit?image=${encodeURIComponent(this.imageUrl)}&bgColor=${encodeURIComponent(this.bgColor)}`
        });
      } catch (error) {
        console.error('导航失败:', error);
        uni.showToast({ 
          title: '跳转失败，请重试', 
          icon: 'none',
          duration: 2000
        });
      } finally {
        this.isProcessing = false;
      }
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
  align-items: center;
}

/* 顶部标题 */
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

/* 卡片样式 */
.upload-card {
  width: 100%;
  max-width: 420px;
  background-color: #fff;
  border-radius: 16px;
  padding: 18px 16px;
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.08);
  display: flex;
  flex-direction: column;
}

/* 上传照片区域 */
.photo-box {
  border: 2px dashed #cfd8dc;
  border-radius: 16px;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 20px;
  width: 100%;
  background-color: #fafafa;
  transition: all 0.3s;
  overflow: hidden;
  position: relative;
}
.photo-box:active {
  opacity: 0.8;
}
.placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px;
}
.upload-icon {
  font-size: 48px;
  color: #1890ff;
  margin-bottom: 8px;
  font-weight: 200;
}
.upload-text {
  font-size: 16px;
  color: #333;
  margin-bottom: 4px;
}
.upload-tip {
  font-size: 12px;
  color: #999;
}
.preview {
  width: 100%;
  height: 100%;
  border-radius: 14px;
}

/* 按钮 */
.btn-primary {
  width: 100%;
  padding: 14px 0;
  border-radius: 12px;
  background-color: #1890ff;
  color: #fff;
  font-size: 16px;
  letter-spacing: 1px;
  box-shadow: 0 3px 10px rgba(24, 144, 255, 0.3);
  margin-bottom: 16px;
  transition: all 0.2s;
}
.btn-primary:active {
  opacity: 0.9;
  transform: translateY(1px);
}
.btn-primary:disabled {
  background-color: #ccc;
  box-shadow: none;
  color: #999;
  transform: none;
}

/* 使用提示 */
.tips-section {
  background-color: #f8f9fa;
  border-radius: 12px;
  padding: 16px;
  border-left: 4px solid #1890ff;
}
.tips-title {
  font-size: 14px;
  color: #333;
  font-weight: 500;
  margin-bottom: 8px;
  display: block;
}
.tip-item {
  font-size: 12px;
  color: #666;
  margin-bottom: 4px;
  line-height: 1.4;
}
.tip-item:last-child {
  margin-bottom: 0;
}
</style>