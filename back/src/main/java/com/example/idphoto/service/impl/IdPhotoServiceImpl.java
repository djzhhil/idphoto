package com.example.idphoto.service.impl;

import com.example.idphoto.model.request.EditImageRequest;
import com.example.idphoto.model.response.ImageResponse;
import com.example.idphoto.processor.ImageProcessor;
import com.example.idphoto.service.IIdPhotoService;
import com.example.idphoto.service.TencentClientService;
import com.example.idphoto.util.ColorUtils;
import com.example.idphoto.util.ImageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 证件照服务实现
 */
@Service
public class IdPhotoServiceImpl implements IIdPhotoService {

    private static final int MAX_CACHE_SIZE = 50;
    private static final Map<String, BufferedImage> IMAGE_CACHE = new HashMap<>();

    @Autowired
    private TencentClientService tencentClientService;

    private final ImageProcessor imageProcessor = new ImageProcessor();

    @Override
    public ImageResponse previewIdPhoto(EditImageRequest request) {
        try {
            if (request.getImage() == null || request.getImage().isEmpty()) {
                return new ImageResponse("", "图片不能为空");
            }

            // 生成缓存键
            String cacheKey = imageProcessor.generateCacheKey(
                    request.getImage(), request.getSize(),
                    request.getBrightness(), request.getSmoothness(), request.getBgColor());

            // 检查缓存
            BufferedImage cachedImage = getCachedImage(cacheKey);
            if (cachedImage != null) {
                String resultBase64 = ImageUtils.imageToBase64(cachedImage, "jpg", 0.7f);
                return new ImageResponse("data:image/jpeg;base64," + resultBase64, "预览生成成功(缓存)");
            }

            // 处理图像
            BufferedImage processedImage = processImage(request, true);

            // 缓存结果
            cacheImage(cacheKey, processedImage);

            // 返回结果
            float quality = ImageUtils.getQualityFactor(request.getQuality());
            String resultBase64 = ImageUtils.imageToBase64(processedImage, "jpg", quality);
            return new ImageResponse("data:image/jpeg;base64," + resultBase64, "预览生成成功");

        } catch (OutOfMemoryError e) {
            System.gc();
            return new ImageResponse("", "处理失败：内存不足，请尝试较小尺寸的图片");
        } catch (Exception e) {
            e.printStackTrace();
            return new ImageResponse("", "预览生成失败：" + e.getMessage());
        }
    }

    @Override
    public ImageResponse generateIdPhoto(EditImageRequest request) {
        try {
            if (request.getImage() == null || request.getImage().isEmpty()) {
                return new ImageResponse("", "图片不能为空");
            }

            // 处理图像（高质量）
            BufferedImage processedImage = processImage(request, false);

            // 高质量输出
            String resultBase64 = ImageUtils.imageToBase64(processedImage, "jpg", 0.9f);
            return new ImageResponse("data:image/jpeg;base64," + resultBase64, "生成成功");

        } catch (OutOfMemoryError e) {
            System.gc();
            return new ImageResponse("", "处理失败：内存不足，请尝试较小尺寸的图片");
        } catch (Exception e) {
            e.printStackTrace();
            return new ImageResponse("", "生成失败：" + e.getMessage());
        }
    }

    /**
     * 统一的图像处理逻辑
     */
    private BufferedImage processImage(EditImageRequest request, boolean isPreview) throws Exception {
        // 去掉 Base64 前缀
        String base64Image = request.getImage();
        if (base64Image.contains(",")) {
            base64Image = base64Image.split(",")[1];
        }

        // 腾讯抠图 - 获取透明背景的人像
        byte[] transparentBytes = tencentClientService.getPortraitMask(base64Image);
        BufferedImage transparentImage = ImageIO.read(new ByteArrayInputStream(transparentBytes));

        BufferedImage processedImage;
        if (isPreview) {
            // 预览模式：快速处理
            processedImage = imageProcessor.processForPreview(
                    transparentImage, request.getSize(),
                    request.getBrightness(), request.getSmoothness());
        } else {
            // 标准处理流程
            BufferedImage resizedImage = imageProcessor.resizeImage(transparentImage, request.getSize(), true);
            BufferedImage brightImage = imageProcessor.adjustBrightness(resizedImage, request.getBrightness());
            processedImage = imageProcessor.applySmooth(brightImage, request.getSmoothness());
        }

        // 添加背景色
        Color bgColor = ColorUtils.parseColor(request.getBgColor());
        return imageProcessor.addBackground(processedImage, bgColor);
    }

    /**
     * 缓存管理
     */
    private void cacheImage(String key, BufferedImage image) {
        if (IMAGE_CACHE.size() >= MAX_CACHE_SIZE) {
            String firstKey = IMAGE_CACHE.keySet().iterator().next();
            IMAGE_CACHE.remove(firstKey);
        }
        IMAGE_CACHE.put(key, image);
    }

    private BufferedImage getCachedImage(String key) {
        return IMAGE_CACHE.get(key);
    }
}