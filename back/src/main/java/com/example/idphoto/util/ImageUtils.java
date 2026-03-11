package com.example.idphoto.util;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;

/**
 * 图像工具类 - 提供 Base64 转换和质量因子计算
 */
public class ImageUtils {

    /**
     * 图像转Base64
     */
    public static String imageToBase64(BufferedImage image, String format, float quality) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        if ("jpg".equalsIgnoreCase(format) || "jpeg".equalsIgnoreCase(format)) {
            ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
            ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(quality);
            ImageOutputStream ios = ImageIO.createImageOutputStream(baos);
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
    public static float getQualityFactor(String quality) {
        if (quality == null) return 0.8f;
        switch (quality.toLowerCase()) {
            case "quick": return 0.5f;
            case "high": return 0.95f;
            default: return 0.8f;
        }
    }
}