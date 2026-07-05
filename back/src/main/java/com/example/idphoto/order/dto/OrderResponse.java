package com.example.idphoto.order.dto;

import java.time.Instant;

public class OrderResponse {
    private String orderNo;
    private String status;
    private Integer amountCent;
    private String downloadToken;
    private String downloadUrl;
    private String message;
    private Instant createdAt;
    private Instant paidAt;

    public OrderResponse() {}

    public OrderResponse(String orderNo, String status, Integer amountCent,
                         String downloadToken, String downloadUrl,
                         String message, Instant createdAt, Instant paidAt) {
        this.orderNo = orderNo;
        this.status = status;
        this.amountCent = amountCent;
        this.downloadToken = downloadToken;
        this.downloadUrl = downloadUrl;
        this.message = message;
        this.createdAt = createdAt;
        this.paidAt = paidAt;
    }

    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getAmountCent() { return amountCent; }
    public void setAmountCent(Integer amountCent) { this.amountCent = amountCent; }
    public String getDownloadToken() { return downloadToken; }
    public void setDownloadToken(String downloadToken) { this.downloadToken = downloadToken; }
    public String getDownloadUrl() { return downloadUrl; }
    public void setDownloadUrl(String downloadUrl) { this.downloadUrl = downloadUrl; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getPaidAt() { return paidAt; }
    public void setPaidAt(Instant paidAt) { this.paidAt = paidAt; }
}
