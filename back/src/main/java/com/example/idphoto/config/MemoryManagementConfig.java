package com.example.idphoto.config;

import org.springframework.context.annotation.Configuration;
import javax.annotation.PreDestroy;
import java.lang.ref.SoftReference;
import java.util.WeakHashMap;

@Configuration
public class MemoryManagementConfig {

    private final WeakHashMap<String, SoftReference<byte[]>> imageCache = new WeakHashMap<>();

    @PreDestroy
    public void cleanup() {
        imageCache.clear();
        System.gc();
    }

    public void cacheImageData(String key, byte[] imageData) {
        imageCache.put(key, new SoftReference<>(imageData));
    }

    public byte[] getCachedImageData(String key) {
        SoftReference<byte[]> ref = imageCache.get(key);
        return ref != null ? ref.get() : null;
    }
}