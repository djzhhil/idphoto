package com.example.idphoto.controller;

import com.example.idphoto.dto.EditImageRequest;
import com.example.idphoto.dto.ImageResponse;
import com.example.idphoto.services.ImageProcessingService;
import com.example.idphoto.services.TencentClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;

@RestController
@RequestMapping("/api/idphoto")
public class IdPhotoController {

    @Autowired
    private TencentClientService tencentClientService;

    @Autowired
    private ImageProcessingService imageProcessingService;

    /**
     * 专用预览接口 - 快速处理
     */
    @PostMapping("/preview-idphoto")
    public ImageResponse previewIdPhoto(@RequestBody EditImageRequest request) {
        try {
            if (request.getImage() == null || request.getImage().isEmpty()) {
                return new ImageResponse("", "图片不能为空");
            }

            // 生成缓存键
            String cacheKey = imageProcessingService.generateCacheKey(
                    request.getImage(), request.getSize(),
                    request.getBrightness(), request.getSmoothness(),
                    request.getBgColor()
            );

            // 检查缓存
            BufferedImage cachedImage = imageProcessingService.getCachedImage(cacheKey);
            if (cachedImage != null) {
                String resultBase64 = imageToBase64(cachedImage, "jpg", 0.7f);
                return new ImageResponse("data:image/jpeg;base64," + resultBase64, "预览生成成功(缓存)");
            }

            // 去掉 Base64 前缀
            String base64Image = request.getImage();
            if (base64Image.contains(",")) base64Image = base64Image.split(",")[1];

            // 腾讯抠图 - 获取透明背景的人像
            byte[] transparentBytes = tencentClientService.getPortraitMask(base64Image);
            BufferedImage transparentImage = ImageIO.read(new ByteArrayInputStream(transparentBytes));

            BufferedImage processedImage;
            if (Boolean.TRUE.equals(request.getPreviewMode())) {
                // 预览模式：快速处理
                processedImage = imageProcessingService.processForPreview(
                        transparentImage,
                        request.getSize(),
                        request.getBrightness(),
                        request.getSmoothness()
                );
            } else {
                // 标准处理流程 - 保持 ARGB 格式处理
                BufferedImage resizedImage = imageProcessingService.resizeImage(transparentImage, request.getSize(), true);
                BufferedImage brightImage = imageProcessingService.adjustBrightness(resizedImage, request.getBrightness());
                processedImage = imageProcessingService.applySmooth(brightImage, request.getSmoothness());
            }

            // 最后一步添加背景色
            Color bgColor = parseColor(request.getBgColor());
            BufferedImage finalImage = imageProcessingService.addBackground(processedImage, bgColor);

            // 缓存结果
            imageProcessingService.cacheImage(cacheKey, finalImage);

            // 根据质量参数调整输出
            float quality = getQualityFactor(request.getQuality());
            String resultBase64 = imageToBase64(finalImage, "jpg", quality);

            return new ImageResponse("data:image/jpeg;base64," + resultBase64, "预览生成成功");

        } catch (OutOfMemoryError e) {
            // 内存不足处理
            System.gc();
            return new ImageResponse("", "处理失败：内存不足，请尝试较小尺寸的图片");
        } catch (Exception e) {
            e.printStackTrace();
            return new ImageResponse("", "预览生成失败：" + e.getMessage());
        }
    }

    @PostMapping("/generate-idphoto")
    public ImageResponse generateIdPhoto(@RequestBody EditImageRequest request) {
        try {
            if (request.getImage() == null || request.getImage().isEmpty()) {
                return new ImageResponse("", "图片不能为空");
            }

            // 去掉 Base64 前缀
            String base64Image = request.getImage();
            if (base64Image.contains(",")) base64Image = base64Image.split(",")[1];

            // 腾讯抠图 - 获取透明背景的人像
            byte[] transparentBytes = tencentClientService.getPortraitMask(base64Image);
            BufferedImage transparentImage = ImageIO.read(new ByteArrayInputStream(transparentBytes));

            // 高质量处理流水线 - 保持 ARGB 格式处理
            BufferedImage resizedImage = imageProcessingService.resizeImage(transparentImage, request.getSize(), true);
            BufferedImage brightImage = imageProcessingService.adjustBrightness(resizedImage, request.getBrightness());
            BufferedImage smoothImage = imageProcessingService.applySmooth(brightImage, request.getSmoothness());

            // 最后一步添加背景色
            Color bgColor = parseColor(request.getBgColor());
            BufferedImage finalImage = imageProcessingService.addBackground(smoothImage, bgColor);

            // 高质量输出
            String resultBase64 = imageToBase64(finalImage, "jpg", 0.9f);

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
     * 添加背景色的辅助方法
     */
    /**
     * 添加背景色
     */
    private BufferedImage addBackground(BufferedImage image, Color bgColor) {
        return imageProcessingService.addBackground(image, bgColor);
    }

    /**
     * 图像转Base64
     */
    private String imageToBase64(BufferedImage image, String format, float quality) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // 对于JPEG，可以设置压缩质量
        if ("jpg".equalsIgnoreCase(format) || "jpeg".equalsIgnoreCase(format)) {
            javax.imageio.ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
            javax.imageio.ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(javax.imageio.ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(quality);

            javax.imageio.stream.ImageOutputStream ios = javax.imageio.ImageIO.createImageOutputStream(baos);
            writer.setOutput(ios);
            writer.write(null, new javax.imageio.IIOImage(image, null, null), param);
            writer.dispose();
            ios.close();
        } else {
            ImageIO.write(image, format, baos);
        }

        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    /**
     * 获取质量因子
     */
    private float getQualityFactor(String quality) {
        switch (quality.toLowerCase()) {
            case "quick": return 0.5f;
            case "high": return 0.95f;
            default: return 0.8f;
        }
    }

    private Color parseColor(String name) {
        if (name == null) return Color.WHITE;
        switch (name.toLowerCase()) {
            case "blue": return new Color(0, 102, 204);
            case "red": return new Color(220, 20, 60);
            case "green": return new Color(82, 196, 26);
            case "gray": return new Color(128, 128, 128);
            case "white": return Color.WHITE;
            default: return Color.WHITE;
        }
    }
}
