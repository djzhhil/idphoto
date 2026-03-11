package com.example.idphoto.service.impl;

import com.example.idphoto.model.IdPhotoSize;
import com.example.idphoto.service.IIdPhotoSizeService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 证件照规格服务实现
 */
@Service
public class IdPhotoSizeServiceImpl implements IIdPhotoSizeService {

    private final Map<String, IdPhotoSize> sizeMap = new HashMap<>();

    public IdPhotoSizeServiceImpl() {
        initializeSizes();
    }

    /**
     * 初始化证件照规格数据
     */
    private void initializeSizes() {
        // 标准证件照（category: common）
        addSize("1inch", "一寸", 295, 413, "标准一寸证件照", "common", true);
        addSize("2inch", "二寸", 413, 579, "标准二寸证件照", "common", true);
        addSize("small1inch", "小一寸", 260, 378, "小一寸证件照", "common", true);
        addSize("big2inch", "大二寸", 449, 623, "大二寸证件照", "common", true);
        addSize("5inch", "五寸", 890, 1240, "五寸证件照", "common", true);

        // 特殊证件照（category: special）
        addSize("idcard", "身份证", 358, 441, "中国身份证照", "special", true);
        addSize("passport", "护照", 124, 181, "护照证件照", "special", true);
        addSize("driving", "驾驶证", 83, 121, "驾驶证证件照", "special", true);
    }

    /**
     * 添加规格到内存存储
     */
    private void addSize(String id, String name, Integer width, Integer height, String description, String category, Boolean isActive) {
        final IdPhotoSize size = new IdPhotoSize(id, name, width, height, description, category, isActive);
        sizeMap.put(id, size);
    }

    @Override
    public List<IdPhotoSize> getAllSizes() {
        return sizeMap.values().stream()
                .filter(size -> size.getIsActive())
                .collect(Collectors.toList());
    }

    @Override
    public List<IdPhotoSize> getSizesByCategory(String category) {
        return sizeMap.values().stream()
                .filter(size -> size.getCategory().equals(category))
                .filter(size -> size.getIsActive())
                .collect(Collectors.toList());
    }

    @Override
    public IdPhotoSize getSizeById(String id) {
        return sizeMap.get(id);
    }

    @Override
    public Map<String, Integer> getDimensionById(String id) {
        IdPhotoSize size = sizeMap.get(id);
        if (size == null) {
            return null;
        }
        Map<String, Integer> dimension = new HashMap<>();
        dimension.put("width", size.getWidth());
        dimension.put("height", size.getHeight());
        return dimension;
    }
}
