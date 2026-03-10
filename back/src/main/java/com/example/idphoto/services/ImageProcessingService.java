package com.example.idphoto.services;

import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.awt.image.RescaleOp;
import java.util.HashMap;
import java.util.Map;

@Service
public class ImageProcessingService {

    private static final Map<String, Dimension> SIZE_MAP = new HashMap<>();
    private static final Map<String, BufferedImage> IMAGE_CACHE = new HashMap<>();
    private static final int MAX_CACHE_SIZE = 50;

    static {
        // 标准证件照尺寸 (300 DPI)
        SIZE_MAP.put("1inch", new Dimension(295, 413));      // 一寸
        SIZE_MAP.put("2inch", new Dimension(413, 579));      // 二寸
        SIZE_MAP.put("small1inch", new Dimension(260, 378)); // 小一寸
    }

    /**
     * 尺寸调整方法 - 支持智能裁剪和保持比例
     */
    public BufferedImage resizeImage(BufferedImage image, String sizeType, boolean keepAspectRatio) {
        if (image == null || sizeType == null) {
            return image;
        }

        Dimension targetSize = SIZE_MAP.get(sizeType);
        if (targetSize == null) {
            // 默认尺寸
            targetSize = new Dimension(295, 413);
        }

        return resizeImageWithCrop(image, targetSize.width, targetSize.height, keepAspectRatio);
    }

    /**
     * 智能裁剪调整尺寸
     */
    private BufferedImage resizeImageWithCrop(BufferedImage image, int targetWidth, int targetHeight, boolean keepAspectRatio) {
        if (!keepAspectRatio) {
            // 直接缩放，可能变形
            BufferedImage resized = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = resized.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(image, 0, 0, targetWidth, targetHeight, null);
            g.dispose();
            return resized;
        }

        // 计算最佳裁剪区域，保持人像居中
        double sourceRatio = (double) image.getWidth() / image.getHeight();
        double targetRatio = (double) targetWidth / targetHeight;

        int cropWidth = image.getWidth();
        int cropHeight = image.getHeight();
        int cropX = 0;
        int cropY = 0;

        if (sourceRatio > targetRatio) {
            // 源图较宽，裁剪宽度
            cropWidth = (int) (image.getHeight() * targetRatio);
            cropX = (image.getWidth() - cropWidth) / 2;
        } else {
            // 源图较高，裁剪高度
            cropHeight = (int) (image.getWidth() / targetRatio);
            cropY = (image.getHeight() - cropHeight) / 2;
        }

        // 确保裁剪区域在图像范围内
        cropX = Math.max(0, cropX);
        cropY = Math.max(0, cropY);
        cropWidth = Math.min(cropWidth, image.getWidth() - cropX);
        cropHeight = Math.min(cropHeight, image.getHeight() - cropY);

        // 创建裁剪后的图像
        BufferedImage cropped = image.getSubimage(cropX, cropY, cropWidth, cropHeight);

        // 高质量缩放 - 保持 ARGB 格式以保留透明度
        BufferedImage resized = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = resized.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.drawImage(cropped, 0, 0, targetWidth, targetHeight, null);
        g.dispose();

        return resized;
    }

    /**
     * 亮度调整 - 保持 ARGB 格式
     */
    public BufferedImage adjustBrightness(BufferedImage image, int brightness) {
        if (image == null || brightness == 50) {
            return image; // 默认值不处理
        }

        // 亮度调整因子 (-1.0 到 1.0)
        float factor = (brightness - 50) / 100.0f;

        BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y);
                int alpha = (rgb >> 24) & 0xFF;

                if (alpha > 0) { // 只处理非透明像素
                    int red = (rgb >> 16) & 0xFF;
                    int green = (rgb >> 8) & 0xFF;
                    int blue = rgb & 0xFF;

                    // 安全地调整亮度，确保值在 0-255 范围内
                    red = clamp((int) (red + red * factor));
                    green = clamp((int) (green + green * factor));
                    blue = clamp((int) (blue + blue * factor));

                    rgb = (alpha << 24) | (red << 16) | (green << 8) | blue;
                }

                result.setRGB(x, y, rgb);
            }
        }

        return result;
    }

    /**
     * 磨皮效果 - 保持 ARGB 格式
     */
    public BufferedImage applySmooth(BufferedImage image, int smoothness) {
        if (image == null || smoothness <= 0) {
            return image;
        }

        // 根据磨皮程度调整模糊半径，限制在合理范围内
        float radius = Math.min(smoothness * 0.3f, 3.0f);
        if (radius < 0.5f) {
            return image;
        }

        try {
            // 创建高斯模糊卷积核
            int size = Math.min((int) (radius * 2 + 1), 7); // 限制卷积核大小
            float[] kernelData = createGaussianKernel(size, radius);
            Kernel kernel = new Kernel(size, size, kernelData);

            ConvolveOp convolve = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
            BufferedImage smoothed = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
            convolve.filter(image, smoothed);

            return smoothed;
        } catch (Exception e) {
            // 如果卷积操作失败，返回原图
            System.err.println("磨皮处理失败: " + e.getMessage());
            return image;
        }
    }

    /**
     * 生成高斯模糊核
     */
    private float[] createGaussianKernel(int size, float sigma) {
        float[] kernel = new float[size * size];
        float sum = 0.0f;
        int center = size / 2;

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                int dx = x - center;
                int dy = y - center;
                float value = (float) Math.exp(-(dx * dx + dy * dy) / (2 * sigma * sigma));
                kernel[y * size + x] = value;
                sum += value;
            }
        }

        // 归一化，确保总和为1
        if (sum > 0) {
            for (int i = 0; i < kernel.length; i++) {
                kernel[i] /= sum;
            }
        }

        return kernel;
    }

    /**
     * 快速预览处理 - 降低质量提高速度
     */
    public BufferedImage processForPreview(BufferedImage image, String sizeType, int brightness, int smoothness) {
        // 快速处理：降低分辨率 + 简化算法
        BufferedImage resized = quickResize(image, 800, 600); // 限制最大尺寸

        if (brightness != 50) {
            resized = quickBrightnessAdjust(resized, brightness);
        }

        if (smoothness > 0) {
            resized = quickSmooth(resized, smoothness);
        }

        return resized;
    }

    /**
     * 快速尺寸调整 - 保持 ARGB 格式
     */
    private BufferedImage quickResize(BufferedImage image, int maxWidth, int maxHeight) {
        int width = image.getWidth();
        int height = image.getHeight();

        if (width <= maxWidth && height <= maxHeight) {
            return image;
        }

        double ratio = Math.min((double) maxWidth / width, (double) maxHeight / height);
        int newWidth = (int) (width * ratio);
        int newHeight = (int) (height * ratio);

        BufferedImage resized = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = resized.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.drawImage(image, 0, 0, newWidth, newHeight, null);
        g.dispose();

        return resized;
    }

    /**
     * 快速亮度调整 - 保持 ARGB 格式
     */
    private BufferedImage quickBrightnessAdjust(BufferedImage image, int brightness) {
        float factor = (brightness - 50) / 50.0f + 1.0f; // 转换为乘数

        // 使用 RescaleOp 进行亮度调整，保持 ARGB 格式
        RescaleOp rescaleOp = new RescaleOp(factor, 0, null);
        BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        rescaleOp.filter(image, result);

        return result;
    }

    /**
     * 快速磨皮 - 保持 ARGB 格式
     */
    private BufferedImage quickSmooth(BufferedImage image, int smoothness) {
        // 使用简单的均值模糊进行快速磨皮
        int radius = Math.min(smoothness / 3, 2);
        if (radius <= 0) return image;

        int size = radius * 2 + 1;
        float weight = 1.0f / (size * size);
        float[] kernel = new float[size * size];
        for (int i = 0; i < kernel.length; i++) {
            kernel[i] = weight;
        }

        try {
            Kernel boxKernel = new Kernel(size, size, kernel);
            ConvolveOp convolve = new ConvolveOp(boxKernel, ConvolveOp.EDGE_NO_OP, null);
            BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
            convolve.filter(image, result);
            return result;
        } catch (Exception e) {
            System.err.println("快速磨皮失败: " + e.getMessage());
            return image;
        }
    }

    /**
     * 颜色值限制在 0-255 范围
     */
    private int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }

    /**
     * 缓存管理
     */
    public void cacheImage(String key, BufferedImage image) {
        if (IMAGE_CACHE.size() >= MAX_CACHE_SIZE) {
            // 简单的LRU：移除第一个元素
            String firstKey = IMAGE_CACHE.keySet().iterator().next();
            IMAGE_CACHE.remove(firstKey);
        }
        IMAGE_CACHE.put(key, image);
    }

    public BufferedImage getCachedImage(String key) {
        return IMAGE_CACHE.get(key);
    }

    public String generateCacheKey(String base64Image, String size, int brightness, int smoothness, String bgColor) {
        return String.format("%s_%s_%d_%d_%s",
                Integer.toHexString(base64Image.hashCode()),
                size, brightness, smoothness, bgColor);
    }

    /**
     * 添加背景色 - 正确处理透明通道
     */
    public BufferedImage addBackground(BufferedImage image, Color bgColor) {
        if (image == null) return null;

        int width = image.getWidth();
        int height = image.getHeight();

        // 创建 RGB 图像作为最终输出
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = result.createGraphics();

        // 设置高质量渲染
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        // 绘制背景色
        g.setColor(bgColor);
        g.fillRect(0, 0, width, height);

        // 绘制原图（透明部分会显示背景色）
        g.drawImage(image, 0, 0, null);
        g.dispose();

        return result;
    }
}