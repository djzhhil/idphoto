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

    // 原有方法保持不变
    @PostMapping("/remove-bg")
    public ImageResponse processImage(@RequestBody ImageRequest request) {
        try {
            String base64Image = request.getImage().split(",")[1];
            byte[] transparentImageBytes = tencentClientService.getPortraitMask(base64Image);
            BufferedImage transparentImage = ImageIO.read(new ByteArrayInputStream(transparentImageBytes));

            Color backgroundColor = getColorByName(request.getBackground());
            int width = transparentImage.getWidth();
            int height = transparentImage.getHeight();
            BufferedImage finalImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

            Graphics2D g = finalImage.createGraphics();
            g.setColor(backgroundColor);
            g.fillRect(0, 0, width, height);
            g.drawImage(transparentImage, 0, 0, null);
            g.dispose();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(finalImage, "jpg", baos);
            String resultBase64 = Base64.getEncoder().encodeToString(baos.toByteArray());

            return new ImageResponse("data:image/jpeg;base64," + resultBase64, "生成成功");

        } catch (Exception e) {
            return new ImageResponse("", "生成失败：" + e.getMessage());
        }
    }

    private Color getColorByName(String name) {
        switch (name.toLowerCase()) {
            case "blue": return new Color(0, 102, 204);
            case "red": return new Color(220, 20, 60);
            case "white":
            default: return Color.WHITE;
        }
    }
}