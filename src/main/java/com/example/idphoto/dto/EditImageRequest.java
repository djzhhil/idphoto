package com.example.idphoto.dto;

public class EditImageRequest {
    private String image;       // base64 或临时路径
    private String size;        // 尺寸类型: 1inch, 2inch, small1inch
    private int brightness;     // 亮度 0~100
    private int smoothness;     // 磨皮 0~100
    private String bgColor;     // 背景色: white, blue, red

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }

    public int getBrightness() { return brightness; }
    public void setBrightness(int brightness) { this.brightness = brightness; }

    public int getSmoothness() { return smoothness; }
    public void setSmoothness(int smoothness) { this.smoothness = smoothness; }

    public String getBgColor() { return bgColor; }
    public void setBgColor(String bgColor) { this.bgColor = bgColor; }
}