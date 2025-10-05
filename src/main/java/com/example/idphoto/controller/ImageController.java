package com.example.idphoto.controller;

import com.example.idphoto.dto.EditImageRequest;
import com.example.idphoto.dto.ImageRequest;
import com.example.idphoto.dto.ImageResponse;
import com.example.idphoto.services.ImageProcessingService;
import com.example.idphoto.services.TencentClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;

@RestController
@RequestMapping("/api")
public class ImageController {

    @Autowired
    private TencentClientService tencentClientService;

    @Autowired
    private ImageProcessingService imageProcessingService;

    //旧方法
    @PostMapping("/remove-bg")
    public ImageResponse processImage(@RequestBody ImageRequest request) {
        try {
            // 1. 去掉 data:image/png;base64, 前缀
            String base64Image = request.getImage().split(",")[1];

            // 2. 调用腾讯云服务进行人像抠图（返回透明背景 PNG）
            byte[] transparentImageBytes = tencentClientService.getPortraitMask(base64Image);

            // 3. 读取为 BufferedImage
            BufferedImage transparentImage = ImageIO.read(new ByteArrayInputStream(transparentImageBytes));

            // 4. 创建背景图
            Color backgroundColor = getColorByName(request.getBackground());
            int width = transparentImage.getWidth();
            int height = transparentImage.getHeight();
            BufferedImage finalImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

            // 5. 绘制背景 + 抠图人像
            Graphics2D g = finalImage.createGraphics();
            g.setColor(backgroundColor);
            g.fillRect(0, 0, width, height);
            g.drawImage(transparentImage, 0, 0, null);
            g.dispose();

            // 6. 转回 Base64
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(finalImage, "jpg", baos);
            String resultBase64 = Base64.getEncoder().encodeToString(baos.toByteArray());

            return new ImageResponse("data:image/jpeg;base64," + resultBase64, "生成成功");

        } catch (Exception e) {
            return new ImageResponse("", "生成失败：" + e.getMessage());
        }
    }

    // 背景色映射
    private Color getColorByName(String name) {
        switch (name.toLowerCase()) {
            case "blue": return new Color(0, 102, 204); // 深蓝
            case "red": return new Color(220, 20, 60);  // 猩红
            case "white":
            default: return Color.WHITE;
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

            // 腾讯抠图
            byte[] transparentBytes = tencentClientService.getPortraitMask(base64Image);
            BufferedImage transparentImage = ImageIO.read(new ByteArrayInputStream(transparentBytes));

            // 调整尺寸
            BufferedImage resizedImage = imageProcessingService.resizeImage(transparentImage, request.getSize());

            // 调整亮度
            BufferedImage brightImage = imageProcessingService.adjustBrightness(resizedImage, request.getBrightness());

            // 磨皮
            BufferedImage smoothImage = imageProcessingService.applySmooth(brightImage, request.getSmoothness());

            // 添加背景色
            Color bgColor = parseColor(request.getBgColor());
            BufferedImage finalImage = new BufferedImage(smoothImage.getWidth(), smoothImage.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g = finalImage.createGraphics();
            g.setColor(bgColor);
            g.fillRect(0, 0, finalImage.getWidth(), finalImage.getHeight());
            g.drawImage(smoothImage, 0, 0, null);
            g.dispose();

            // 转 Base64
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(finalImage, "jpg", baos);
            String resultBase64 = Base64.getEncoder().encodeToString(baos.toByteArray());

            return new ImageResponse("data:image/jpeg;base64," + resultBase64, "生成成功");

        } catch (Exception e) {
            return new ImageResponse("", "生成失败：" + e.getMessage());
        }
    }

    private Color parseColor(String name) {
        if (name == null) return Color.WHITE;
        switch (name.toLowerCase()) {
            case "blue": return new Color(0, 102, 204);
            case "red": return new Color(220, 20, 60);
            case "green": return new Color(82, 196, 26);
            default: return Color.WHITE;
        }
    }
}