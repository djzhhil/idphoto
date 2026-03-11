package com.example.idphoto.service;

import com.example.idphoto.model.IdPhotoSize;
import java.util.List;
import java.util.Map;

/**
 * 证件照规格服务接口
 */
public interface IIdPhotoSizeService {

    /**
     * 获取所有证件照规格
     */
    List<IdPhotoSize> getAllSizes();

    /**
     * 根据分类获取证件照规格
     */
    List<IdPhotoSize> getSizesByCategory(String category);

    /**
     * 根据ID获取证件照规格
     */
    IdPhotoSize getSizeById(String id);

    /**
     * 根据ID获取证件照尺寸（宽x高）
     */
    Map<String, Integer> getDimensionById(String id);
}
