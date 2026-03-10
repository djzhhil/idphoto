package com.example.idphoto.dto;

public class ImageRequest {
    private String image;      // base64编码图像
    private String background; // 目标背景色（white、blue、red）

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getBackground() {
        return background;
    }

    public void setBackground(String background) {
        this.background = background;
    }
}
