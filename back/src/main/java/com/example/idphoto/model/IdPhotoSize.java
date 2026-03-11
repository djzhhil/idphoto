package com.example.idphoto.model;

/**
 * 证件照规格实体类
 */
public class IdPhotoSize {
    private String id;
    private String name;
    private Integer width;
    private Integer height;
    private String description;
    private String category;
    private Boolean isActive;

    public IdPhotoSize() {}

    public IdPhotoSize(String id, String name, Integer width, Integer height, String description, String category, Boolean isActive) {
        this.id = id;
        this.name = name;
        this.width = width;
        this.height = height;
        this.description = description;
        this.category = category;
        this.isActive = isActive;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getWidth() { return width; }
    public void setWidth(Integer width) { this.width = width; }

    public Integer getHeight() { return height; }
    public void setHeight(Integer height) { this.height = height; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}
