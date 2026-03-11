package com.example.idphoto.model.request;

public class EditImageRequest {
    private String image;
    private String size;
    private Integer brightness = 50;
    private Integer smoothness = 0;
    private String bgColor = "white";
    private Boolean previewMode = false;
    private Boolean finalMode = false;
    private String quality = "standard"; // quick, standard, high

    // getters and setters
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }
    public Integer getBrightness() { return brightness; }
    public void setBrightness(Integer brightness) { this.brightness = brightness; }
    public Integer getSmoothness() { return smoothness; }
    public void setSmoothness(Integer smoothness) { this.smoothness = smoothness; }
    public String getBgColor() { return bgColor; }
    public void setBgColor(String bgColor) { this.bgColor = bgColor; }
    public Boolean getPreviewMode() { return previewMode; }
    public void setPreviewMode(Boolean previewMode) { this.previewMode = previewMode; }
    public Boolean getFinalMode() { return finalMode; }
    public void setFinalMode(Boolean finalMode) { this.finalMode = finalMode; }
    public String getQuality() { return quality; }
    public void setQuality(String quality) { this.quality = quality; }
}