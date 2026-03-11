package com.example.idphoto.service;

import com.example.idphoto.model.request.EditImageRequest;
import com.example.idphoto.model.response.ImageResponse;

/**
 * 证件照服务接口
 */
public interface IIdPhotoService {

    /**
     * 预览证件照
     */
    ImageResponse previewIdPhoto(EditImageRequest request);

    /**
     * 生成证件照
     */
    ImageResponse generateIdPhoto(EditImageRequest request);
}