package com.example.idphoto.dto;

public class ImageResponse {
    private String resultImageBase64;

    public ImageResponse(String resultImageBase64) {
        this.resultImageBase64 = resultImageBase64;
    }

    public String getResultImageBase64() {
        return resultImageBase64;
    }

    public void setResultImageBase64(String resultImageBase64) {
        this.resultImageBase64 = resultImageBase64;
    }
}

