package com.example.idphoto.order;

import com.example.idphoto.order.dto.CreateOrderRequest;
import com.example.idphoto.order.dto.ManualPayRequest;
import com.example.idphoto.order.dto.OrderResponse;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private OrderService orderService;

    @PostMapping("/create")
    public ResponseEntity<OrderResponse> createOrder(@RequestBody CreateOrderRequest request) {
        try {
            OrderResponse resp = orderService.createOrder(request);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            log.error("Create order failed", e);
            OrderResponse err = new OrderResponse();
            err.setStatus("FAILED");
            err.setMessage(e.getMessage());
            return ResponseEntity.badRequest().body(err);
        }
    }

    @GetMapping("/{orderNo}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable String orderNo) {
        try {
            OrderResponse resp = orderService.getOrder(orderNo);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            log.error("Get order failed: {}", orderNo, e);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/my")
    public ResponseEntity<List<OrderResponse>> getMyOrders(@RequestParam(required = false) String openid) {
        try {
            List<OrderResponse> orders = orderService.getMyOrders(openid);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            log.error("Get my orders failed", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/{orderNo}/manual-pay")
    public ResponseEntity<OrderResponse> manualPay(@PathVariable String orderNo,
                                                    @RequestBody ManualPayRequest request) {
        try {
            OrderResponse resp = orderService.markPaid(orderNo, request.getAdminToken());
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            log.error("Manual pay failed: {}", orderNo, e);
            OrderResponse err = new OrderResponse();
            err.setStatus("FAILED");
            err.setMessage(e.getMessage());
            return ResponseEntity.badRequest().body(err);
        }
    }

    @GetMapping("/{orderNo}/download")
    public ResponseEntity<Resource> download(@PathVariable String orderNo,
                                              @RequestParam String token) {
        try {
            Resource resource = orderService.loadDownload(orderNo, token);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_JPEG);
            headers.setContentDispositionFormData("attachment", orderNo + ".jpg");
            return ResponseEntity.ok().headers(headers).body(resource);
        } catch (Exception e) {
            log.error("Download failed: {}", orderNo, e);
            return ResponseEntity.notFound().build();
        }
    }
}
