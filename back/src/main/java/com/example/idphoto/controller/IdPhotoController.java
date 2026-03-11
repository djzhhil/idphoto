package com.example.idphoto.controller;

import com.example.idphoto.model.request.EditImageRequest;
import com.example.idphoto.model.response.ImageResponse;
import com.example.idphoto.service.IIdPhotoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 证件照控制器 - 纯接口层，只负责接收请求和返回结果
 */
@RestController
@RequestMapping("/api/idphoto")
public class IdPhotoController {

    @Autowired
    private IIdPhotoService idPhotoService;

    /**
     * 预览证件照
     */
    @PostMapping("/preview-idphoto")
    public ImageResponse previewIdPhoto(@RequestBody EditImageRequest request) {
        return idPhotoService.previewIdPhoto(request);
    }

    /**
     * 生成证件照
     */
    @PostMapping("/generate-idphoto")
    public ImageResponse generateIdPhoto(@RequestBody EditImageRequest request) {
        return idPhotoService.generateIdPhoto(request);
    }
}