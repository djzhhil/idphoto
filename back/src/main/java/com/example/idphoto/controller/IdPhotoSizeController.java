package com.example.idphoto.controller;

import com.example.idphoto.model.IdPhotoSize;
import com.example.idphoto.service.IIdPhotoSizeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 证件照规格控制器 - 提供规格查询接口
 */
@RestController
@RequestMapping("/api/idphoto/sizes")
public class IdPhotoSizeController {

    @Autowired
    private IIdPhotoSizeService idPhotoSizeService;

    /**
     * 获取所有证件照规格
     */
    @GetMapping
    public List<IdPhotoSize> getAllSizes() {
        return idPhotoSizeService.getAllSizes();
    }

    /**
     * 根据分类获取证件照规格
     */
    @GetMapping("/category/{category}")
    public List<IdPhotoSize> getSizesByCategory(@PathVariable String category) {
        return idPhotoSizeService.getSizesByCategory(category);
    }
}
