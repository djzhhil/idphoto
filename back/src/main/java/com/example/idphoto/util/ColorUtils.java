package com.example.idphoto.util;

import java.awt.*;

/**
 * 颜色工具类 - 提供颜色名称到 Color 对象的转换
 */
public class ColorUtils {

    /**
     * 解析颜色名称为 Color 对象
     */
    public static Color parseColor(String name) {
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

    /**
     * 根据名称获取颜色
     */
    public static Color getColorByName(String name) {
        return parseColor(name);
    }
}