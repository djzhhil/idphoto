package com.example.idphoto.order;

import com.example.idphoto.model.request.EditImageRequest;
import com.example.idphoto.model.response.ImageResponse;
import com.example.idphoto.order.dto.CreateOrderRequest;
import com.example.idphoto.order.dto.OrderResponse;
import com.example.idphoto.service.IIdPhotoService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    private IIdPhotoService idPhotoService;

    @Autowired
    private IdPhotoOrderRepository orderRepository;

    @Value("${idphoto.storage-dir:./data/orders}")
    private String storageDir;

    @Value("${idphoto.default-amount-cent:390}")
    private int defaultAmountCent;

    @Value("${idphoto.download-token-ttl-hours:168}")
    private int downloadTokenTtlHours;

    @Value("${idphoto.admin-token:change-me}")
    private String adminToken;

    @Value("${idphoto.create-throttle-seconds:20}")
    private long createThrottleSeconds;

    private final Map<String, Instant> lastCreateAtByOpenid = new ConcurrentHashMap<>();

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        String openid = normalizeOpenid(request.getOpenid());
        enforceCreateThrottle(openid);

        // Convert CreateOrderRequest to EditImageRequest
        EditImageRequest editRequest = toEditImageRequest(request);

        // Generate the ID photo
        ImageResponse result = idPhotoService.generateIdPhoto(editRequest);

        String resultBase64 = result.getResultImageBase64();
        if (resultBase64 == null || resultBase64.isEmpty()) {
            throw new RuntimeException("Image generation failed: " + result.getMessage());
        }

        // Clean base64 prefix
        String cleanBase64 = resultBase64;
        if (cleanBase64.startsWith("data:")) {
            cleanBase64 = cleanBase64.substring(cleanBase64.indexOf(",") + 1);
        }

        // Generate order number
        String orderNo = "ID" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase();

        // Save image to disk
        Path dir = Paths.get(storageDir);
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new RuntimeException("Cannot create storage directory", e);
        }
        Path outputPath = dir.resolve(orderNo + ".jpg");
        try {
            byte[] imageBytes = Base64.getDecoder().decode(cleanBase64);
            Files.write(outputPath, imageBytes);
        } catch (IOException e) {
            throw new RuntimeException("Cannot save image file", e);
        }

        // Create order entity
        IdPhotoOrder order = new IdPhotoOrder();
        order.setOrderNo(orderNo);
        order.setOpenid(openid);
        order.setServiceType("idphoto");
        order.setSizeId(request.getSize());
        order.setBgColor(request.getBgColor());
        order.setAmountCent(defaultAmountCent);
        order.setStatus(OrderStatus.UNPAID);
        order.setOutputPath(outputPath.toString());
        order.setDownloadToken(UUID.randomUUID().toString().replace("-", ""));
        order.setCreatedAt(Instant.now());
        order.setExpiredAt(Instant.now().plus(downloadTokenTtlHours, ChronoUnit.HOURS));

        orderRepository.save(order);
        log.info("Order created: {}", orderNo);

        return toOrderResponse(order);
    }

    public OrderResponse getOrder(String orderNo) {
        IdPhotoOrder order = orderRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderNo));
        return toOrderResponse(order);
    }

    public List<OrderResponse> getMyOrders(String openid) {
        String uid = normalizeOpenid(openid);
        return orderRepository.findTop50ByOpenidOrderByCreatedAtDesc(uid)
                .stream()
                .map(this::toOrderResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderResponse markPaid(String orderNo, String requestAdminToken) {
        if (requestAdminToken == null || !requestAdminToken.equals(adminToken)) {
            throw new RuntimeException("Invalid admin token");
        }

        IdPhotoOrder order = orderRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderNo));

        // Only allow marking UNPAID or PAID orders as DONE
        if (order.getStatus() != OrderStatus.UNPAID && order.getStatus() != OrderStatus.PAID) {
            throw new RuntimeException("Order is not in a payable state: " + order.getStatus());
        }

        order.setStatus(OrderStatus.DONE);
        order.setPaidAt(Instant.now());
        orderRepository.save(order);
        log.info("Order {} marked as DONE", orderNo);

        return toOrderResponse(order);
    }

    public Resource loadDownload(String orderNo, String token) {
        IdPhotoOrder order = orderRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderNo));

        if (order.getStatus() != OrderStatus.DONE) {
            throw new RuntimeException("Order not paid yet. Status: " + order.getStatus());
        }

        if (order.getDownloadToken() == null || !order.getDownloadToken().equals(token)) {
            throw new RuntimeException("Invalid download token");
        }

        if (order.getExpiredAt() != null && Instant.now().isAfter(order.getExpiredAt())) {
            throw new RuntimeException("Download link expired");
        }

        Path filePath = Paths.get(order.getOutputPath());
        if (!Files.exists(filePath)) {
            throw new RuntimeException("Image file not found on disk");
        }

        return new FileSystemResource(filePath);
    }

    // --- private helpers ---

    private EditImageRequest toEditImageRequest(CreateOrderRequest req) {
        EditImageRequest e = new EditImageRequest();
        e.setImage(req.getImage());
        e.setSize(req.getSize());
        e.setBrightness(req.getBrightness());
        e.setSmoothness(req.getSmoothness());
        e.setBgColor(req.getBgColor());
        e.setPreviewMode(req.getPreviewMode());
        e.setFinalMode(true);
        e.setQuality(req.getQuality() != null ? req.getQuality() : "high");
        e.setMode(req.getMode() != null ? req.getMode() : "edit");
        return e;
    }

    private OrderResponse toOrderResponse(IdPhotoOrder order) {
        OrderResponse resp = new OrderResponse();
        resp.setOrderNo(order.getOrderNo());
        resp.setStatus(order.getStatus().name());
        resp.setAmountCent(order.getAmountCent());
        boolean downloadable = order.getStatus() == OrderStatus.DONE
                && order.getExpiredAt() != null
                && Instant.now().isBefore(order.getExpiredAt());
        resp.setDownloadToken(downloadable ? order.getDownloadToken() : null);
        resp.setDownloadUrl(downloadable
                ? "/api/orders/" + order.getOrderNo() + "/download?token=" + order.getDownloadToken()
                : null);
        resp.setMessage(null);
        resp.setCreatedAt(order.getCreatedAt());
        resp.setPaidAt(order.getPaidAt());
        return resp;
    }

    private String normalizeOpenid(String openid) {
        if (openid == null || openid.trim().isEmpty()) {
            return "guest";
        }
        return openid.trim();
    }

    private void enforceCreateThrottle(String openid) {
        if (createThrottleSeconds <= 0) {
            return;
        }
        Instant now = Instant.now();
        Instant last = lastCreateAtByOpenid.get(openid);
        if (last != null && last.plusSeconds(createThrottleSeconds).isAfter(now)) {
            throw new RuntimeException("Please wait before creating another order");
        }
        lastCreateAtByOpenid.put(openid, now);
    }
}
