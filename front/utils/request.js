const ENV = {
  local: {
    baseURL: "http://localhost:8081", // 本地测试
  },
  prod: {
    baseURL: "https://idphoto-kqffccmbmb.cn-hangzhou.fcapp.run", // 线上
  },
};

// 强制使用生产环境
const BASE_URL = ENV.local.baseURL;

function handleResponse(res) {
  if (res.statusCode >= 200 && res.statusCode < 300) {
    return res.data; // 返回后端 JSON
  } else {
    return Promise.reject(res);
  }
}

export default {
  get(url, data = {}, header = {}) {
    return new Promise((resolve, reject) => {
      uni.request({
        url: BASE_URL + url,
        method: "GET",
        data,
        header: {
          "Content-Type": "application/json",
          ...header,
        },
        success(res) {
          try {
            resolve(handleResponse(res));
          } catch (err) {
            reject(err);
          }
        },
        fail(err) {
          reject(err);
        },
      });
    });
  },

  post(url, data = {}, header = {}) {
    return new Promise((resolve, reject) => {
      uni.request({
        url: BASE_URL + url,
        method: "POST",
        data,
        header: {
          "Content-Type": "application/json",
          ...header,
        },
        success(res) {
          try {
            resolve(handleResponse(res));
          } catch (err) {
            reject(err);
          }
        },
        fail(err) {
          reject(err);
        },
      });
    });
  },
};