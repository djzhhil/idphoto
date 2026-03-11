// 导入 request
import request from "@/utils/request.js";

// 缓存键前缀
const CACHE_KEY_PREFIX = 'idphoto_sizes_';
const CACHE_EXPIRY = 30 * 60 * 1000; // 30分钟

export default {
  // 获取所有规格（带缓存）
  async getAllSizes() {
    return await this._fetchWithCache('/api/idphoto/sizes', 'all');
  },
  
  // 按分类获取规格（带缓存）
  async getSizesByCategory(category) {
    return await this._fetchWithCache('/api/idphoto/sizes/category/' + category, category);
  },
  
  // 请求并缓存
  async _fetchWithCache(url, cacheKey) {
    const fullCacheKey = CACHE_KEY_PREFIX + cacheKey;
    
    // 尝试从缓存读取
    const cached = this._getCached(fullCacheKey);
    if (cached) {
      console.log('从缓存读取规格:', cacheKey);
      return cached;
    }
    
    // 请求 API
    try {
      const res = await request.get(url);
      
      // 缓存结果
      this._setCached(fullCacheKey, res);
      
      return res || [];
    } catch (err) {
      console.error('获取规格失败:', err);
      return [];
    }
  },
  
  // 获取缓存
  _getCached(key) {
    // #ifdef H5
    const cached = localStorage.getItem(key);
    if (cached) {
      const data = JSON.parse(cached);
      if (Date.now() - data.timestamp < CACHE_EXPIRY) {
        return data.value;
      }
    }
    // #endif
    
    // #ifdef MP-WEIXIN
    const cached = uni.getStorageSync(key);
    if (cached) {
      const data = JSON.parse(cached);
      if (Date.now() - data.timestamp < CACHE_EXPIRY) {
        return data.value;
      }
    }
    // #endif
    
    return null;
  },
  
  // 设置缓存
  _setCached(key, value) {
    const data = {
      timestamp: Date.now(),
      value: value
    };
    
    // #ifdef H5
    localStorage.setItem(key, JSON.stringify(data));
    // #endif
    
    // #ifdef MP-WEIXIN
    uni.setStorageSync(key, JSON.stringify(data));
    // #endif
  }
};
