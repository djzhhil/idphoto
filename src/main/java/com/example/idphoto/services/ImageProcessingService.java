package com.example.idphoto.services;

import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;

@Service
public class ImageProcessingService {

    // 尺寸调整
    public BufferedImage resizeImage(BufferedImage input, String size) {
        int width = input.getWidth();
        int height = input.getHeight();
        switch (size) {
            case "1inch": width = 295; height = 413; break;      // 1寸
            case "2inch": width = 413; height = 626; break;      // 2寸
            case "small1inch": width = 238; height = 336; break; // 小一寸
        }
        Image tmp = input.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = resized.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();
        return resized;
    }

    // 亮度调整
    public BufferedImage adjustBrightness(BufferedImage input, int brightness) {
        float scale = 1f + (brightness - 50) / 50f; // 简单线性增减
        RescaleOp op = new RescaleOp(new float[]{scale, scale, scale, 1f}, new float[4], null);
        BufferedImage output = new BufferedImage(input.getWidth(), input.getHeight(), BufferedImage.TYPE_INT_ARGB);
        op.filter(input, output);
        return output;
    }

    // 简单磨皮（高斯模糊）
    public BufferedImage applySmooth(BufferedImage input, int smoothness) {
        // 这里可用卷积滤镜简单实现磨皮
        // smoothness = 0~100，越大越光滑
        float radius = smoothness / 10f; // 0~10
        if (radius <= 0) return input;
        // 可使用第三方库或者简单模糊
        return input; // 占位，实际可接入 BufferedImageOp 实现
    }
}