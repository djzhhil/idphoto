package com.example.idphoto.model.response;

public class ImageResponse {
    private String resultImageBase64;
    private String message;

    public ImageResponse(String resultImageBase64, String message) {
        this.resultImageBase64 = resultImageBase64;
        this.message = message;
    }

    public String getResultImageBase64() { return resultImageBase64; }
    public void setResultImageBase64(String resultImageBase64) { this.resultImageBase64 = resultImageBase64; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}