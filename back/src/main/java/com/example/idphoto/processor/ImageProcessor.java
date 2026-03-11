package com.example.idphoto.processor;

import java.awt.*;
import java.awt.image.*;
import java.util.HashMap;
import java.util.Map;

/**
 * 图像处理器 - 支持依赖注入的图像处理
 */
public class ImageProcessor {

    private static final int MAX_CACHE_SIZE = 50;

    // 注入证件照规格服务
    private com.example.idphoto.service.IIdPhotoSizeService imageSizeService;

    /**
     * 设置证件照规格服务（用于依赖注入）
     */
    public void setImageSizeService(com.example.idphoto.service.IIdPhotoSizeService imageSizeService) {
        this.imageSizeService = imageSizeService;
    }

    /**
     * 尺寸调整方法 - 支持智能裁剪和保持比例
     */
    public BufferedImage resizeImage(BufferedImage image, String sizeType, boolean keepAspectRatio) {
        if (image == null || sizeType == null) {
            return image;
        }

        // 使用服务获取尺寸，如果不成功则使用默认值（一寸照片）
        int targetWidth = 295;
        int targetHeight = 413;

        if (imageSizeService != null) {
            Map<String, Integer> dimension = imageSizeService.getDimensionById(sizeType);
            if (dimension != null) {
                Integer width = dimension.get("width");
                Integer height = dimension.get("height");
                if (width != null && height != null) {
                    targetWidth = width;
                    targetHeight = height;
                }
            }
        }

        return resizeImageWithCrop(image, targetWidth, targetHeight, keepAspectRatio);
    }

    /**
     * 智能裁剪调整尺寸
     */
    private BufferedImage resizeImageWithCrop(BufferedImage image, int targetWidth, int targetHeight, boolean keepAspectRatio) {
        if (!keepAspectRatio) {
            BufferedImage resized = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = resized.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(image, 0, 0, targetWidth, targetHeight, null);
            g.dispose();
            return resized;
        }

        double sourceRatio = (double) image.getWidth() / image.getHeight();
        double targetRatio = (double) targetWidth / targetHeight;
        int cropWidth = image.getWidth();
        int cropHeight = image.getHeight();
        int cropX = 0;
        int cropY = 0;

        if (sourceRatio > targetRatio) {
            cropWidth = (int) (image.getHeight() * targetRatio);
            cropX = (image.getWidth() - cropWidth) / 2;
        } else {
            cropHeight = (int) (image.getWidth() / targetRatio);
            cropY = (image.getHeight() - cropHeight) / 2;
        }

        cropX = Math.max(0, cropX);
        cropY = Math.max(0, cropY);
        cropWidth = Math.min(cropWidth, image.getWidth() - cropX);
        cropHeight = Math.min(cropHeight, image.getHeight() - cropY);

        BufferedImage cropped = image.getSubimage(cropX, cropY, cropWidth, cropHeight);
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
     * 亮度调整
     */
    public BufferedImage adjustBrightness(BufferedImage image, int brightness) {
        if (image == null || brightness == 50) {
            return image;
        }
        float factor = (brightness - 50) / 100.0f;
        BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y);
                int alpha = (rgb >> 24) & 0xFF;
                if (alpha > 0) {
                    int red = (rgb >> 16) & 0xFF;
                    int green = (rgb >> 8) & 0xFF;
                    int blue = rgb & 0xFF;
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
     * 磨皮效果
     */
    public BufferedImage applySmooth(BufferedImage image, int smoothness) {
        if (image == null || smoothness <= 0) {
            return image;
        }
        float radius = Math.min(smoothness * 0.3f, 3.0f);
        if (radius < 0.5f) {
            return image;
        }
        try {
            int size = Math.min((int) (radius * 2 + 1), 7);
            float[] kernelData = createGaussianKernel(size, radius);
            Kernel kernel = new Kernel(size, size, kernelData);
            ConvolveOp convolve = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
            BufferedImage smoothed = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
            convolve.filter(image, smoothed);
            return smoothed;
        } catch (Exception e) {
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
        if (sum > 0) {
            for (int i = 0; i < kernel.length; i++) {
                kernel[i] /= sum;
            }
        }
        return kernel;
    }

    /**
     * 快速预览处理
     */
    public BufferedImage processForPreview(BufferedImage image, String sizeType, int brightness, int smoothness) {
        BufferedImage resized = quickResize(image, 800, 600);
        if (brightness != 50) {
            resized = quickBrightnessAdjust(resized, brightness);
        }
        if (smoothness > 0) {
            resized = quickSmooth(resized, smoothness);
        }
        return resized;
    }

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

    private BufferedImage quickBrightnessAdjust(BufferedImage image, int brightness) {
        float factor = (brightness - 50) / 50.0f + 1.0f;
        RescaleOp rescaleOp = new RescaleOp(factor, 0, null);
        BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        rescaleOp.filter(image, result);
        return result;
    }

    private BufferedImage quickSmooth(BufferedImage image, int smoothness) {
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

    private int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }

    /**
     * 添加背景色
     */
    public BufferedImage addBackground(BufferedImage image, Color bgColor) {
        if (image == null) return null;
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = result.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setColor(bgColor);
        g.fillRect(0, 0, width, height);
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return result;
    }

    /**
     * 生成缓存键
     */
    public String generateCacheKey(String base64Image, String size, int brightness, int smoothness, String bgColor) {
        return String.format("%s_%s_%d_%d_%s", Integer.toHexString(base64Image.hashCode()), size, brightness, smoothness, bgColor);
    }
}