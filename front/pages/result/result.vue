<template>
  <view class="container">
    <!-- 顶部标题区 -->
    <view class="header">
      <text class="app-title">底色通 · 智能证件照</text>
      <text class="subtitle">请确认照片效果并保存</text>
    </view>

    <view class="card result-card">
      <text class="card-title">生成成功 🎉</text>

      <view class="photo-box" :style="{ height: photoHeight + 'px' }">
        <image v-if="resultImage" :src="resultImage" class="preview" mode="widthFix" @load="onImageLoad" />
        <view v-else class="placeholder"><text>图片加载中...</text></view>
      </view>

      <view class="button-group">
        <button class="btn-action" @click="downloadImage" :disabled="isDownloading">
          {{ isDownloading ? '保存中...' : '保存到相册' }}
        </button>
        <button class="btn-action secondary" @click="createNew">重新制作</button>
      </view>

      <!-- 分享提示 -->
      <view class="share-tips">
        <text class="tips-text">提示：保存后可通过相册分享给朋友</text>
      </view>
    </view>

    <!-- 保存成功提示 -->
    <view v-if="showSuccess" class="success-toast">
      <text>保存成功！</text>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      resultImage: '',
      photoHeight: 200,
      isDownloading: false,
      showSuccess: false
    };
  },
  onLoad(options) {
  
    const cacheImage = uni.getStorageSync('resultImage')
  
    if (cacheImage) {
      this.resultImage = cacheImage
      uni.removeStorageSync('resultImage')
    }
    else if (options.image) {
      this.resultImage = decodeURIComponent(options.image)
    }
  
    console.log('结果页面加载，图片地址:', this.resultImage ? '有图片' : '无图片')
  },
  methods: {
    onImageLoad(e) {
      const ratio = e.detail.height / e.detail.width;
      const maxWidth = uni.getSystemInfoSync().windowWidth - 40;
      this.photoHeight = Math.min(maxWidth * ratio, 600);
    },

    async downloadImage() {
    
      if (!this.resultImage) {
        uni.showToast({ title: '图片未加载', icon: 'none' })
        return
      }
    
      this.isDownloading = true
    
      try {
    
        // ======================
        // 微信小程序
        // ======================
        // #ifdef MP-WEIXIN
    
        let tempFilePath = this.resultImage
    
        if (this.resultImage.startsWith('http')) {
    
          const downloadRes = await uni.downloadFile({
            url: this.resultImage
          })
    
          if (downloadRes.statusCode !== 200) {
            throw new Error('下载失败')
          }
    
          tempFilePath = downloadRes.tempFilePath
    
        } 
        else if (this.resultImage.startsWith('data:')) {
    
          tempFilePath = await this.base64ToTempFile(this.resultImage)
    
        }
    
        await this.saveToAlbum(tempFilePath)
    
        this.showSuccess = true
        setTimeout(() => {
          this.showSuccess = false
        }, 2000)
    
        // #endif
    
    
        // ======================
        // H5
        // ======================
        // #ifdef H5
    
        const link = document.createElement('a')
    
        link.href = this.resultImage
        link.download = 'idphoto.jpg'
    
        document.body.appendChild(link)
    
        link.click()
    
        document.body.removeChild(link)
    
        uni.showToast({
          title: '下载成功',
          icon: 'success'
        })
    
        // #endif
    
    
      } catch (error) {
    
        console.error('保存失败:', error)
    
        // 小程序权限处理
        if (error.errMsg && error.errMsg.includes('auth deny')) {
    
          uni.showModal({
            title: '需要相册权限',
            content: '请允许访问相册以保存图片',
            success: (res) => {
              if (res.confirm) {
                this.openSetting()
              }
            }
          })
    
        } else {
    
          uni.showToast({
            title: '保存失败，请重试',
            icon: 'none'
          })
    
        }
    
      } finally {
    
        this.isDownloading = false
    
      }
    
    },

    // Base64转临时文件
    base64ToTempFile(base64) {
    
      // #ifdef MP-WEIXIN
    
      return new Promise((resolve, reject) => {
    
        const fs = uni.getFileSystemManager()
    
        const filePath = `${wx.env.USER_DATA_PATH}/temp_${Date.now()}.jpg`
    
        const base64Data = base64.replace(/^data:image\/\w+;base64,/, '')
    
        fs.writeFile({
          filePath: filePath,
          data: base64Data,
          encoding: 'base64',
          success: () => resolve(filePath),
          fail: reject
        })
    
      })
    
      // #endif
    
    },

    // 保存到相册
    saveToAlbum(tempFilePath) {
      return new Promise((resolve, reject) => {
        uni.saveImageToPhotosAlbum({
          filePath: tempFilePath,
          success: resolve,
          fail: reject
        });
      });
    },

    // 打开设置页面
    openSetting() {
      uni.openSetting({
        success: (res) => {
          console.log('设置页面打开成功', res);
        }
      });
    },

    // 重新制作
    createNew() {
      uni.navigateBack({
        delta: 2, // 返回两级，回到上传页面
        fail: () => {
          // 如果返回失败，直接跳转到首页
          uni.reLaunch({
            url: '/pages/index/index'
          });
        }
      });
    },

    // 分享功能（在小程序中分享的是页面，不是图片）
    onShareAppMessage() {
      return {
        title: '智能证件照 - 一键生成专业证件照',
        path: '/pages/index/index',
        imageUrl: this.resultImage
      };
    },

    // 分享到朋友圈
    onShareTimeline() {
      return {
        title: '智能证件照 - 一键生成专业证件照',
        imageUrl: this.resultImage
      };
    }
  }
};
</script>

<style scoped>
.container {
  background-color: #f5f7fa;
  min-height: 100vh;
  padding: 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

/* 顶部标题区 */
.header {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 16px;
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

/* 卡片 */
.result-card {
  width: 100%;
  max-width: 420px;
  background-color: #fff;
  border-radius: 16px;
  padding: 16px;
  box-shadow: 0 6px 16px rgba(0,0,0,0.08);
  text-align: center;
}
.card-title {
  font-size: 18px;
  font-weight: 500;
  color: #111;
  margin-bottom: 12px;
}

/* 照片区域 */
.photo-box {
  border: 2px dashed #e0e0e0;
  border-radius: 16px;
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  overflow: hidden;
  margin-bottom: 20px;
  background-color: #fafafa;
}
.preview {
  width: 100%;
  height: auto;
}
.placeholder {
  color: #aaa;
  padding: 40px;
}

/* 按钮组 */
.button-group {
  display: flex;
  gap: 12px;
  justify-content: center;
  margin-bottom: 16px;
}
.btn-action {
  flex: 1;
  padding: 12px 0;
  border-radius: 12px;
  font-size: 16px;
  color: #fff;
  background-color: #1890ff;
  border: none;
  transition: all 0.2s;
}
.btn-action:active {
  opacity: 0.85;
  transform: translateY(1px);
}
.btn-action:disabled {
  background-color: #ccc;
  color: #999;
}
.btn-action.secondary {
  background-color: #f0f0f0;
  color: #666;
}

/* 分享提示 */
.share-tips {
  background-color: #f8f9fa;
  border-radius: 8px;
  padding: 12px;
  border-left: 4px solid #1890ff;
}
.tips-text {
  font-size: 12px;
  color: #666;
  line-height: 1.4;
}

/* 成功提示 */
.success-toast {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background-color: rgba(0, 0, 0, 0.8);
  color: white;
  padding: 16px 24px;
  border-radius: 8px;
  z-index: 9999;
}
</style>