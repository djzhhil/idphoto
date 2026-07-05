import request from "./request.js";

const BASE = "/api/orders";

export default {
  createOrder(data) {
    return request.post(BASE + "/create", data);
  },

  getOrder(orderNo) {
    return request.get(BASE + "/" + orderNo);
  },

  // Admin-only helper. Do not call this from user-facing pages.
  manualPay(orderNo, adminToken) {
    return request.post(BASE + "/" + orderNo + "/manual-pay", {
      adminToken: adminToken
    });
  },

  getMyOrders(openid) {
    const query = openid ? "?openid=" + encodeURIComponent(openid) : "";
    return request.get(BASE + "/my" + query);
  },

  getDownloadUrl(orderNo, token) {
    // Returns the full download URL with the base URL
    const ENV = {
      local: { baseURL: "http://localhost:8081" },
      prod: { baseURL: "https://idphoto.scserver.store:56465" }
    };
    const BASE_URL = ENV.prod.baseURL;
    return BASE_URL + BASE + "/" + orderNo + "/download?token=" + encodeURIComponent(token);
  }
};
